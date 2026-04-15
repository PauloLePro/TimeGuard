package com.timeguard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.timeguard.helpers.MonitorScheduler;
import com.timeguard.helpers.PermissionHelper;
import com.timeguard.helpers.Prefs;

/**
 * Bonus : relance le worker après redémarrage.
 * Note : l'exécution réelle dépend d'Android (optimisations, etc.).
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        if (Prefs.isMonitoringEnabled(context)) {
            Log.d(TAG, "BOOT_COMPLETED -> reschedule monitoring");
            MonitorScheduler.schedulePeriodic(context, PermissionHelper.canRunMonitoring(context));
            MonitorScheduler.kickstartOneShotMonitor(context, 0);
        }
    }
}
