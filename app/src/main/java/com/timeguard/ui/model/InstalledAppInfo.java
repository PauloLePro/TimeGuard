package com.timeguard.ui.model;

import android.graphics.drawable.Drawable;

/**
 * Modèle UI : application installée (launchable).
 */
public class InstalledAppInfo {
    public final String label;
    public final String packageName;
    public final Drawable icon;

    public InstalledAppInfo(String label, String packageName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }
}

