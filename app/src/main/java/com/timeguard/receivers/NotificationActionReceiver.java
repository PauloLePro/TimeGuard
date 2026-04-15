package com.timeguard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.timeguard.helpers.MonitorScheduler;

/**
 * Actions de notification : "J'ai compris" et "Rappeler plus tard".
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotifActionReceiver";

    private static final String ACTION_ACK = "com.timeguard.action.ACK";
    private static final String ACTION_SNOOZE = "com.timeguard.action.SNOOZE";

    private static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    private static final String EXTRA_DELAY_MINUTES = "extra_delay_minutes";

    public static Intent intentAck(Context context, int notificationId) {
        Intent i = new Intent(context, NotificationActionReceiver.class);
        i.setAction(ACTION_ACK);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return i;
    }

    public static Intent intentSnooze(Context context, int notificationId, long delayMinutes) {
        Intent i = new Intent(context, NotificationActionReceiver.class);
        i.setAction(ACTION_SNOOZE);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        i.putExtra(EXTRA_DELAY_MINUTES, delayMinutes);
        return i;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        if (ACTION_ACK.equals(action)) {
            NotificationManagerCompat.from(context).cancel(notificationId);
            return;
        }

        if (ACTION_SNOOZE.equals(action)) {
            NotificationManagerCompat.from(context).cancel(notificationId);
            long delay = intent.getLongExtra(EXTRA_DELAY_MINUTES, 5L);
            Log.d(TAG, "Snooze requested, scheduling one-shot in " + delay + " min");
            MonitorScheduler.kickstartOneShotMonitor(context, delay);
        }
    }
}
