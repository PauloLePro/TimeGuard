package com.timeguard.helpers;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.timeguard.workers.UsageMonitorWorker;

import java.util.concurrent.TimeUnit;

/**
 * Centralise l’enqueue/cancel WorkManager.
 *
 * Limitations Android (important) :
 * - PeriodicWorkRequest a un intervalle min de 15 minutes.
 * - Même à 15 min, le système peut retarder selon batterie/Doze.
 * => Donc la notification peut être en retard, surtout pour des limites courtes.
 */
public final class MonitorScheduler {
    private static final String TAG = "MonitorScheduler";

    public static final String UNIQUE_PERIODIC_WORK = "TimeGuardPeriodicMonitor";
    public static final String UNIQUE_ONESHOT_WORK = "TimeGuardOneShotMonitor";

    private MonitorScheduler() {
    }

    public static void schedulePeriodic(Context context, boolean shouldRun) {
        if (!shouldRun) {
            cancelPeriodic(context);
            return;
        }

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(UsageMonitorWorker.class, 15, TimeUnit.MINUTES)
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .addTag(UNIQUE_PERIODIC_WORK)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_PERIODIC_WORK, ExistingPeriodicWorkPolicy.UPDATE, request);

        Log.d(TAG, "Periodic monitoring scheduled");
    }

    public static void cancelPeriodic(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK);
        Log.d(TAG, "Periodic monitoring canceled");
    }

    public static void cancelOneShot(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_ONESHOT_WORK);
        Log.d(TAG, "OneShot monitoring canceled");
    }

    /**
     * Lance (ou remplace) la surveillance OneTime immédiatement ou après un délai.
     * Utile quand l'utilisateur active la surveillance, ou via l'action "Rappeler plus tard".
     */
    public static void kickstartOneShotMonitor(Context context, long delayMinutes) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsageMonitorWorker.class)
                .setInitialDelay(Math.max(0, delayMinutes), TimeUnit.MINUTES)
                .addTag(UNIQUE_ONESHOT_WORK)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_ONESHOT_WORK, ExistingWorkPolicy.REPLACE, req);
        Log.d(TAG, "OneShot monitoring kickstarted in " + delayMinutes + " min");
    }

    public static void kickstartOneShotMonitorSeconds(Context context, long delaySeconds) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsageMonitorWorker.class)
                .setInitialDelay(Math.max(0, delaySeconds), TimeUnit.SECONDS)
                .addTag(UNIQUE_ONESHOT_WORK)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_ONESHOT_WORK, ExistingWorkPolicy.REPLACE, req);
        Log.d(TAG, "OneShot monitoring kickstarted in " + delaySeconds + " sec");
    }

    /**
     * Planifie la prochaine exécution en la chaînant après la courante (APPEND).
     * Évite de cancel un worker en cours.
     */
    public static void appendNextOneShotMonitor(Context context, long delayMinutes) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsageMonitorWorker.class)
                .setInitialDelay(Math.max(0, delayMinutes), TimeUnit.MINUTES)
                .addTag(UNIQUE_ONESHOT_WORK)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_ONESHOT_WORK, ExistingWorkPolicy.APPEND, req);
        Log.d(TAG, "OneShot monitoring appended in " + delayMinutes + " min");
    }

    public static void appendNextOneShotMonitorSeconds(Context context, long delaySeconds) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsageMonitorWorker.class)
                .setInitialDelay(Math.max(0, delaySeconds), TimeUnit.SECONDS)
                .addTag(UNIQUE_ONESHOT_WORK)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_ONESHOT_WORK, ExistingWorkPolicy.APPEND, req);
        Log.d(TAG, "OneShot monitoring appended in " + delaySeconds + " sec");
    }
}
