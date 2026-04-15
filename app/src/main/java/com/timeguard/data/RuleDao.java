package com.timeguard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RuleDao {

    @Query("SELECT * FROM monitor_rules ORDER BY appName COLLATE NOCASE")
    LiveData<List<MonitorRule>> observeAll();

    @Query("SELECT * FROM monitor_rules WHERE enabled = 1")
    List<MonitorRule> getEnabledRulesSync();

    @Query("SELECT * FROM monitor_rules WHERE packageName = :pkg LIMIT 1")
    MonitorRule getByPackageSync(String pkg);

    @Query("SELECT * FROM monitor_rules WHERE id = :id LIMIT 1")
    MonitorRule getByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertIgnore(MonitorRule rule);

    @Update
    int update(MonitorRule rule);

    @Delete
    int delete(MonitorRule rule);

    @Query("UPDATE monitor_rules SET enabled = :enabled WHERE id = :id")
    int setEnabled(long id, boolean enabled);

    @Query("UPDATE monitor_rules SET lastNotifiedAtMs = :timestampMs WHERE id = :id")
    int setLastNotifiedAt(long id, long timestampMs);
}

