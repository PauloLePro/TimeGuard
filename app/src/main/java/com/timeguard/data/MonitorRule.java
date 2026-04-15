package com.timeguard.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Règle de surveillance : une règle par package (index unique).
 */
@Entity(
        tableName = "monitor_rules",
        indices = {@Index(value = {"packageName"}, unique = true)}
)
public class MonitorRule {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String packageName;
    public String appName;

    public int limitMinutes;

    /**
     * Cooldown minimal entre deux notifications pour cette règle.
     * 0 => utiliser la valeur par défaut (voir Prefs).
     */
    public int cooldownMinutes;

    public boolean enabled;

    /**
     * Timestamp (ms) du dernier envoi de notification pour éviter le spam.
     */
    public long lastNotifiedAtMs;
}

