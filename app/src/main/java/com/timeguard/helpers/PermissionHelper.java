package com.timeguard.helpers;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Helpers permissions (usage stats + notifications).
 */
public final class PermissionHelper {
    private static final String TAG = "PermissionHelper";

    public static final int REQ_POST_NOTIFICATIONS = 2001;

    private PermissionHelper() {
    }

    public static boolean hasUsageStatsAccess(Context context) {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Throwable t) {
            Log.w(TAG, "hasUsageStatsAccess failed", t);
            return false;
        }
    }

    public static void openUsageAccessSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "openUsageAccessSettings failed", e);
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            boolean runtimeGranted = ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
            // En plus de la runtime permission, l'utilisateur peut désactiver les notifs dans les réglages.
            return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void requestNotificationPermissionIfNeeded(Activity activity) {
        if (Build.VERSION.SDK_INT < 33) return;
        if (hasNotificationPermission(activity)) return;
        ActivityCompat.requestPermissions(
                activity,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                REQ_POST_NOTIFICATIONS
        );
    }

    /**
     * Conditions minimales côté app pour “essayer” de surveiller :
     * - usage stats accordé
     * (si la permission notifications manque, on surveille quand même mais on ne pourra pas notifier correctement).
     */
    public static boolean canRunMonitoring(Context context) {
        return hasUsageStatsAccess(context);
    }
}

