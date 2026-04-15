package com.timeguard.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Préférences simples (SharedPreferences) :
 * - Activer/désactiver la surveillance
 * - Valeurs par défaut lors de la création d'une règle
 */
public final class Prefs {
    private Prefs() {
    }

    private static final String PREFS_NAME = "timeguard_prefs";

    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    private static final String KEY_DEFAULT_LIMIT_MIN = "default_limit_min";
    private static final String KEY_DEFAULT_COOLDOWN_MIN = "default_cooldown_min";
    private static final String KEY_CHECK_INTERVAL_MIN = "check_interval_min";
    private static final String KEY_PERSISTENT_NOTIFICATION = "persistent_notification";

    public static final int DEFAULT_LIMIT_MIN = 10;
    public static final int DEFAULT_COOLDOWN_MIN = 5;
    /**
     * Intervalle de vérification “best-effort” (OneTime + replanification).
     * Plus c'est court, plus la conso/batterie peut augmenter.
     */
    public static final int DEFAULT_CHECK_INTERVAL_MIN = 1;

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isMonitoringEnabled(Context context) {
        return sp(context).getBoolean(KEY_MONITORING_ENABLED, false);
    }

    public static void setMonitoringEnabled(Context context, boolean enabled) {
        sp(context).edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply();
    }

    public static int getDefaultLimitMinutes(Context context) {
        return sp(context).getInt(KEY_DEFAULT_LIMIT_MIN, DEFAULT_LIMIT_MIN);
    }

    public static void setDefaultLimitMinutes(Context context, int minutes) {
        sp(context).edit().putInt(KEY_DEFAULT_LIMIT_MIN, minutes).apply();
    }

    public static int getDefaultCooldownMinutes(Context context) {
        return sp(context).getInt(KEY_DEFAULT_COOLDOWN_MIN, DEFAULT_COOLDOWN_MIN);
    }

    public static void setDefaultCooldownMinutes(Context context, int minutes) {
        sp(context).edit().putInt(KEY_DEFAULT_COOLDOWN_MIN, minutes).apply();
    }

    public static int getCheckIntervalMinutes(Context context) {
        return sp(context).getInt(KEY_CHECK_INTERVAL_MIN, DEFAULT_CHECK_INTERVAL_MIN);
    }

    public static void setCheckIntervalMinutes(Context context, int minutes) {
        sp(context).edit().putInt(KEY_CHECK_INTERVAL_MIN, minutes).apply();
    }

    /**
     * Si activé, la notification reste visible (non dismissable) jusqu’à "J’ai compris".
     * Plus voyant, mais plus intrusif.
     */
    public static boolean isPersistentNotificationEnabled(Context context) {
        return sp(context).getBoolean(KEY_PERSISTENT_NOTIFICATION, false);
    }

    public static void setPersistentNotificationEnabled(Context context, boolean enabled) {
        sp(context).edit().putBoolean(KEY_PERSISTENT_NOTIFICATION, enabled).apply();
    }
}
