package com.timeguard.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeFormatUtils {
    private TimeFormatUtils() {
    }

    public static String formatDurationShort(long seconds) {
        long m = seconds / 60;
        long s = seconds % 60;
        if (m <= 0) return s + " s";
        return m + " min " + s + " s";
    }

    public static String formatDateTime(long timestampMs) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        return sdf.format(new Date(timestampMs));
    }
}

