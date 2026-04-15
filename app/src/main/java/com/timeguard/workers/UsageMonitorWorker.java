package com.timeguard.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.timeguard.data.MonitorRule;
import com.timeguard.data.NotificationLog;
import com.timeguard.data.RuleRepository;
import com.timeguard.helpers.NotificationHelper;
import com.timeguard.helpers.PermissionHelper;
import com.timeguard.helpers.Prefs;
import com.timeguard.helpers.UsageStatsHelper;

import java.util.List;

/**
 * Worker dédié :
 * - lit les règles actives
 * - tente d’identifier l’app au premier plan via UsageEvents
 * - calcule depuis combien de temps elle est au premier plan
 * - compare à la limite
 * - envoie une notification si nécessaire (anti-spam + cooldown)
 *
 * Limitations :
 * - PeriodicWorkRequest a un intervalle min de 15 minutes, et Android peut retarder.
 * - Donc la notification peut arriver en retard pour des limites courtes.
 */
public class UsageMonitorWorker extends Worker {
    private static final String TAG = "UsageMonitorWorker";

    // Planification adaptative : plus on s'approche de la limite, plus on vérifie souvent (sans trop impacter la batterie).
    private static final long NEAR_LIMIT_WINDOW_MS = 60_000L; // 1 minute avant la limite
    private static final long MIN_FAST_CHECK_SECONDS = 10L;   // pas plus fréquent que toutes les 10s

    public UsageMonitorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        long nextDelaySeconds = -1L; // calculé au fil du worker, replanifié en finally.
        try {
            if (!Prefs.isMonitoringEnabled(context)) {
                Log.d(TAG, "Monitoring disabled -> noop");
                return Result.success();
            }

            if (!PermissionHelper.hasUsageStatsAccess(context)) {
                Log.d(TAG, "Usage stats permission missing -> noop");
                return Result.success();
            }

            RuleRepository repo = RuleRepository.getInstance(context);
            List<MonitorRule> enabled = repo.getEnabledRulesSync();
            if (enabled == null || enabled.isEmpty()) {
                Log.d(TAG, "No enabled rules -> noop");
                return Result.success();
            }

            UsageStatsHelper.ForegroundAppInfo fg =
                    UsageStatsHelper.getForegroundApp(context, 2 * 60 * 60 * 1000L, context.getPackageName());

            if (fg == null) {
                Log.d(TAG, "Foreground app unknown (no reliable data)");
                nextDelaySeconds = baseIntervalSeconds(context);
                return Result.success();
            }

            MonitorRule rule = repo.getRuleByPackageSync(fg.packageName);
            if (rule == null || !rule.enabled) {
                Log.d(TAG, "No matching rule for foreground: " + fg.packageName);
                nextDelaySeconds = baseIntervalSeconds(context);
                return Result.success();
            }

            long now = System.currentTimeMillis();
            long observedMs = now - fg.sinceMs;
            if (observedMs < 0) observedMs = 0;
            long observedSeconds = observedMs / 1000L;

            int limitMin = Math.max(1, rule.limitMinutes);
            long limitMs = limitMin * 60_000L;
            if (observedMs < limitMs) {
                Log.d(TAG, "Limit not reached yet for " + fg.packageName + " observed=" + observedSeconds + "s");
                nextDelaySeconds = computeNextDelaySeconds(context, limitMs - observedMs);
                return Result.success();
            }

            int cooldown = rule.cooldownMinutes > 0 ? rule.cooldownMinutes : Prefs.getDefaultCooldownMinutes(context);
            long cooldownMs = Math.max(1, cooldown) * 60_000L;

            long last = rule.lastNotifiedAtMs;
            if (last > 0 && now - last < cooldownMs) {
                Log.d(TAG, "Cooldown active -> skip notify");
                nextDelaySeconds = computeCooldownDelaySeconds(context, cooldownMs - (now - last));
                return Result.success();
            }

            // Anti-spam : on persiste immédiatement avant d’envoyer (réduit les doublons en cas de relance).
            repo.setLastNotifiedAtSync(rule.id, now);

            NotificationHelper.showLimitExceeded(
                    context,
                    rule.id,
                    safe(rule.appName, fg.packageName),
                    fg.packageName,
                    observedSeconds,
                    limitMin,
                    cooldown
            );

            NotificationLog log = new NotificationLog();
            log.packageName = fg.packageName;
            log.appName = safe(rule.appName, fg.packageName);
            log.observedSeconds = observedSeconds;
            log.limitMinutes = limitMin;
            log.sentAtMs = now;
            repo.insertHistoryAsync(log);

            Log.d(TAG, "Notification sent for " + fg.packageName + " observed=" + observedSeconds + "s");
            nextDelaySeconds = baseIntervalSeconds(context);
            return Result.success();
        } finally {
            // Replanification "best-effort" (OneTime) :
            // - WorkManager périodique est min 15 min => trop lent pour des limites à 1-5 min.
            // - Ici on chaîne un OneTime de manière adaptative :
            //   * intervalle "base" (Paramètres) quand rien de particulier
            //   * plus fréquent quand on s'approche d'une limite sur une app surveillée
            scheduleNextBestEffort(context, nextDelaySeconds);
        }
    }

    private static String safe(String appName, String fallback) {
        if (appName == null || appName.trim().isEmpty()) return fallback;
        return appName;
    }

    private void scheduleNextBestEffort(Context context, long computedDelaySeconds) {
        if (!Prefs.isMonitoringEnabled(context)) return;
        long delay = computedDelaySeconds > 0 ? computedDelaySeconds : baseIntervalSeconds(context);
        // Hard clamp pour éviter une boucle trop agressive.
        delay = Math.max(10L, Math.min(delay, 60L * 60L)); // [10s .. 1h]
        com.timeguard.helpers.MonitorScheduler.appendNextOneShotMonitorSeconds(context, delay);
    }

    private static long baseIntervalSeconds(Context context) {
        // Même si UsageStats n'est pas encore accordé, on laisse une re-tentative (l'utilisateur peut l'activer).
        int intervalMin = Prefs.getCheckIntervalMinutes(context);
        int safeMin = Math.max(1, Math.min(intervalMin, 60));
        return safeMin * 60L;
    }

    private static long computeNextDelaySeconds(Context context, long remainingToLimitMs) {
        long base = baseIntervalSeconds(context);
        if (remainingToLimitMs <= 0) return Math.min(base, 60L);

        // Si on est loin de la limite, on reste sur l'intervalle "base".
        if (remainingToLimitMs > NEAR_LIMIT_WINDOW_MS) {
            return base;
        }

        // Si on s'approche, on vérifie plus souvent, mais sans descendre trop bas.
        long remainingSeconds = Math.max(1L, remainingToLimitMs / 1000L);
        long fast = Math.max(MIN_FAST_CHECK_SECONDS, Math.min(remainingSeconds, 60L));
        return Math.min(base, fast);
    }

    private static long computeCooldownDelaySeconds(Context context, long remainingCooldownMs) {
        long base = baseIntervalSeconds(context);
        long remainingSeconds = Math.max(10L, remainingCooldownMs / 1000L);
        return Math.min(base, remainingSeconds);
    }
}
