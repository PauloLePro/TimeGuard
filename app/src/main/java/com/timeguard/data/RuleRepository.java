package com.timeguard.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Couche de persistance simple (Room + Executor).
 * - UI : observe via LiveData
 * - Worker : accès sync autorisé (déjà en background)
 */
public class RuleRepository {
    private static final String TAG = "RuleRepository";

    private static volatile RuleRepository INSTANCE;

    private final RuleDao ruleDao;
    private final HistoryDao historyDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private RuleRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        ruleDao = db.ruleDao();
        historyDao = db.historyDao();
    }

    public static RuleRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RuleRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RuleRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<MonitorRule>> observeRules() {
        return ruleDao.observeAll();
    }

    public LiveData<List<NotificationLog>> observeHistory() {
        return historyDao.observeAll();
    }

    public void saveRuleAsync(MonitorRule rule) {
        io.execute(() -> {
            try {
                upsertRule(rule);
            } catch (Exception e) {
                Log.e(TAG, "saveRuleAsync failed", e);
            }
        });
    }

    public void deleteRuleAsync(MonitorRule rule) {
        io.execute(() -> {
            try {
                ruleDao.delete(rule);
            } catch (Exception e) {
                Log.e(TAG, "deleteRuleAsync failed", e);
            }
        });
    }

    public void setRuleEnabledAsync(long ruleId, boolean enabled) {
        io.execute(() -> {
            try {
                ruleDao.setEnabled(ruleId, enabled);
            } catch (Exception e) {
                Log.e(TAG, "setRuleEnabledAsync failed", e);
            }
        });
    }

    public void clearHistoryAsync() {
        io.execute(() -> {
            try {
                historyDao.clear();
            } catch (Exception e) {
                Log.e(TAG, "clearHistoryAsync failed", e);
            }
        });
    }

    public void insertHistoryAsync(NotificationLog log) {
        io.execute(() -> {
            try {
                historyDao.insert(log);
            } catch (Exception e) {
                Log.e(TAG, "insertHistoryAsync failed", e);
            }
        });
    }

    // --- Sync APIs (Worker) ---

    public List<MonitorRule> getEnabledRulesSync() {
        return ruleDao.getEnabledRulesSync();
    }

    public MonitorRule getRuleByPackageSync(String pkg) {
        return ruleDao.getByPackageSync(pkg);
    }

    public void setLastNotifiedAtSync(long ruleId, long timestampMs) {
        ruleDao.setLastNotifiedAt(ruleId, timestampMs);
    }

    public MonitorRule getRuleByIdSync(long ruleId) {
        return ruleDao.getByIdSync(ruleId);
    }

    public void upsertRule(MonitorRule rule) {
        // Upsert manuel pour éviter l'effet "REPLACE => nouvel id" lorsque l’index unique déclenche un conflit.
        long newId = ruleDao.insertIgnore(rule);
        if (newId == -1L) {
            MonitorRule existing = ruleDao.getByPackageSync(rule.packageName);
            if (existing != null) {
                rule.id = existing.id;
                ruleDao.update(rule);
            } else {
                Log.w(TAG, "Conflit d'insert mais rule introuvable (race condition?)");
            }
        } else {
            rule.id = newId;
        }
    }
}

