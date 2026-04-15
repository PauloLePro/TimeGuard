package com.timeguard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Historique local des notifications envoyées (debug / transparence).
 */
@Entity(tableName = "notification_history")
public class NotificationLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String packageName;
    public String appName;

    public long observedSeconds;
    public int limitMinutes;

    public long sentAtMs;
}

