package com.timeguard.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.timeguard.R;
import com.timeguard.receivers.NotificationActionReceiver;

/**
 * Notifications locales (NotificationCompat + channel haute importance).
 */
public final class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static final String CHANNEL_ID = "timeguard_alerts";

    private NotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID);
        if (existing != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.notif_channel_desc));
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 250, 100, 250});
        nm.createNotificationChannel(channel);
    }

    public static void showLimitExceeded(
            Context context,
            long ruleId,
            String appName,
            String packageName,
            long observedSeconds,
            int limitMinutes,
            int cooldownMinutes
    ) {
        if (!PermissionHelper.hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission missing -> skip notify");
            return;
        }

        ensureChannel(context);

        int notificationId = (int) (packageName.hashCode() & 0x7fffffff);

        String durationText = TimeFormatUtils.formatDurationShort(observedSeconds);
        String text = "Vous utilisez " + appName + " depuis " + durationText + ". Limite: " + limitMinutes + " min.";

        // Action: "J'ai compris"
        PendingIntent ack = PendingIntent.getBroadcast(
                context,
                notificationId,
                NotificationActionReceiver.intentAck(context, notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: "Rappeler plus tard" (ré-enqueue un one-shot après le cooldown ou 5 min min).
        long delay = Math.max(5, Math.max(1, cooldownMinutes));
        PendingIntent snooze = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                NotificationActionReceiver.intentSnooze(context, notificationId, delay),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: "Ouvrir l'application"
        PendingIntent openApp = createOpenAppPendingIntent(context, packageName, notificationId + 2);

        int accentColor = ContextCompat.getColor(context, R.color.tg_logo_blue);
        Bitmap appLogo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(appLogo)
                .setContentTitle(context.getString(R.string.notif_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_MAX) // heads-up si possible
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(accentColor)
                .setVibrate(new long[]{0, 250, 100, 250})
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .addAction(0, context.getString(R.string.notif_action_ack), ack)
                .addAction(0, context.getString(R.string.notif_action_snooze), snooze);

        if (openApp != null) {
            b.addAction(0, context.getString(R.string.notif_action_open_app), openApp);
        }

        boolean persistent = Prefs.isPersistentNotificationEnabled(context);
        if (persistent) {
            // Plus voyant : reste affiché tant que l'utilisateur n'a pas acquitté.
            b.setOngoing(true);
            b.setAutoCancel(false);
            b.setColorized(true);
        } else {
            b.setOngoing(false);
            b.setAutoCancel(true);
            b.setColorized(false);
        }

        NotificationManagerCompat.from(context).notify(notificationId, b.build());
    }

    private static PendingIntent createOpenAppPendingIntent(Context context, String packageName, int requestCode) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent == null) return null;
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return PendingIntent.getActivity(
                    context,
                    requestCode,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } catch (Exception e) {
            Log.w(TAG, "createOpenAppPendingIntent failed", e);
            return null;
        }
    }
}
