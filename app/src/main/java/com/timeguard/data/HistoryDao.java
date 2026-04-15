package com.timeguard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM notification_history ORDER BY sentAtMs DESC")
    LiveData<List<NotificationLog>> observeAll();

    @Insert
    long insert(NotificationLog log);

    @Query("DELETE FROM notification_history")
    void clear();
}

