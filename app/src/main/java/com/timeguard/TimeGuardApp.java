package com.timeguard;

import android.app.Application;

import com.timeguard.helpers.MonitorScheduler;
import com.timeguard.helpers.NotificationHelper;
import com.timeguard.helpers.PermissionHelper;
import com.timeguard.helpers.Prefs;

/**
 * Application de base : crée le NotificationChannel et (re)programme la surveillance si activée.
 */
public class TimeGuardApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.ensureChannel(this);

        // On (re)programme le worker si la surveillance est activée.
        // Important : WorkManager (périodique) n’est pas précis et peut être retardé (Doze/batterie).
        // Le Worker gère les permissions manquantes sans crash.
        if (Prefs.isMonitoringEnabled(this)) {
            MonitorScheduler.schedulePeriodic(this, PermissionHelper.canRunMonitoring(this));
            // Démarrage "best-effort" (OneTime replanifié).
            MonitorScheduler.kickstartOneShotMonitor(this, 0);
        }
    }
}
