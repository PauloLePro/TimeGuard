package com.timeguard.helpers;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Lecture des événements d'usage pour tenter de déterminer l'app au premier plan et depuis quand.
 *
 * Remarques importantes :
 * - Les événements ne sont pas garantis (selon OEM, restrictions, etc.).
 * - Android peut retarder ou omettre certains events.
 * - On fait au mieux, en restant robuste (null / incohérences => pas de crash).
 */
public final class UsageStatsHelper {
    private static final String TAG = "UsageStatsHelper";

    private UsageStatsHelper() {
    }

    public static class ForegroundAppInfo {
        public final String packageName;
        public final long sinceMs;

        public ForegroundAppInfo(String packageName, long sinceMs) {
            this.packageName = packageName;
            this.sinceMs = sinceMs;
        }
    }

    @Nullable
    public static ForegroundAppInfo getForegroundApp(Context context, long lookbackMs, String selfPackage) {
        if (!PermissionHelper.hasUsageStatsAccess(context)) return null;

        long now = System.currentTimeMillis();
        long begin = Math.max(0, now - Math.max(lookbackMs, 60_000)); // au moins 1 min.

        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return null;

        UsageEvents events;
        try {
            events = usm.queryEvents(begin, now);
        } catch (Throwable t) {
            Log.w(TAG, "queryEvents failed", t);
            return null;
        }
        if (events == null) return null;

        String currentPkg = null;
        long currentSince = 0L;

        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            try {
                events.getNextEvent(event);
            } catch (Throwable t) {
                // Sécurité : sur certains devices, des événements peuvent être corrompus.
                Log.w(TAG, "getNextEvent failed", t);
                continue;
            }

            String pkg = event.getPackageName();
            if (pkg == null) continue;

            // Ignorer TimeGuard lui-même pour éviter les faux positifs.
            if (pkg.equals(selfPackage)) {
                continue;
            }

            int type = event.getEventType();
            long ts = event.getTimeStamp();

            if (isForegroundEvent(type)) {
                currentPkg = pkg;
                currentSince = ts;
            } else if (isBackgroundEvent(type)) {
                if (pkg.equals(currentPkg)) {
                    currentPkg = null;
                    currentSince = 0L;
                }
            }
        }

        if (currentPkg == null || currentSince <= 0L || currentSince > now) {
            return null;
        }
        return new ForegroundAppInfo(currentPkg, currentSince);
    }

    private static boolean isForegroundEvent(int type) {
        return type == UsageEvents.Event.MOVE_TO_FOREGROUND
                || type == UsageEvents.Event.ACTIVITY_RESUMED;
    }

    private static boolean isBackgroundEvent(int type) {
        return type == UsageEvents.Event.MOVE_TO_BACKGROUND
                || type == UsageEvents.Event.ACTIVITY_PAUSED
                || type == UsageEvents.Event.ACTIVITY_STOPPED;
    }
}
