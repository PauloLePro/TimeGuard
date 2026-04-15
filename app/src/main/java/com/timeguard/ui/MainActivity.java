package com.timeguard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.timeguard.R;
import com.timeguard.data.MonitorRule;
import com.timeguard.data.RuleRepository;
import com.timeguard.helpers.MonitorScheduler;
import com.timeguard.helpers.PermissionHelper;
import com.timeguard.helpers.Prefs;
import com.timeguard.ui.adapters.RuleAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView tvMonitoringStatus;
    private SwitchMaterial switchMonitoring;
    private TextView tvUsageAccessStatus;
    private TextView tvNotificationStatus;
    private Button btnUsageSettings;
    private Button btnRequestNotifications;
    private Button btnSelectApps;
    private Button btnHistory;
    private Button btnSettings;
    private TextView tvEmptyRules;
    private RecyclerView rvRules;

    private RuleRepository repo;
    private RuleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = RuleRepository.getInstance(this);

        bindViews();
        setupRulesList();
        setupActions();

        refreshStatuses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatuses();
    }

    private void bindViews() {
        tvMonitoringStatus = findViewById(R.id.tvMonitoringStatus);
        switchMonitoring = findViewById(R.id.switchMonitoring);
        tvUsageAccessStatus = findViewById(R.id.tvUsageAccessStatus);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        btnUsageSettings = findViewById(R.id.btnUsageSettings);
        btnRequestNotifications = findViewById(R.id.btnRequestNotifications);
        btnSelectApps = findViewById(R.id.btnSelectApps);
        btnHistory = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);
        tvEmptyRules = findViewById(R.id.tvEmptyRules);
        rvRules = findViewById(R.id.rvRules);
    }

    private void setupRulesList() {
        adapter = new RuleAdapter(new RuleAdapter.Listener() {
            @Override
            public void onToggle(MonitorRule rule, boolean enabled) {
                repo.setRuleEnabledAsync(rule.id, enabled);
            }

            @Override
            public void onEdit(MonitorRule rule) {
                RuleConfigDialog.showEdit(MainActivity.this, rule, updated -> repo.saveRuleAsync(updated));
            }

            @Override
            public void onDelete(MonitorRule rule) {
                confirmDelete(rule);
            }
        });
        rvRules.setLayoutManager(new LinearLayoutManager(this));
        rvRules.setAdapter(adapter);

        repo.observeRules().observe(this, this::onRulesChanged);
    }

    private void setupActions() {
        btnUsageSettings.setOnClickListener(v -> PermissionHelper.openUsageAccessSettings(this));

        btnRequestNotifications.setOnClickListener(v -> PermissionHelper.requestNotificationPermissionIfNeeded(this));

        btnSelectApps.setOnClickListener(v -> startActivity(new Intent(this, AppSelectionActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        switchMonitoring.setOnCheckedChangeListener(null);
        switchMonitoring.setChecked(Prefs.isMonitoringEnabled(this));
        switchMonitoring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.setMonitoringEnabled(this, isChecked);
            refreshMonitoringUi(isChecked);

            boolean canRun = PermissionHelper.canRunMonitoring(this);
            MonitorScheduler.schedulePeriodic(this, isChecked && canRun);
            if (isChecked) {
                // Démarrage immédiat "best-effort" (sinon le périodique peut attendre jusqu'à 15 minutes).
                MonitorScheduler.kickstartOneShotMonitor(this, 0);
            } else {
                MonitorScheduler.cancelOneShot(this);
            }

            if (isChecked && !canRun) {
                showUsagePermissionHint();
            }
        });
    }

    private void onRulesChanged(List<MonitorRule> rules) {
        adapter.submitList(rules);
        boolean empty = rules == null || rules.isEmpty();
        tvEmptyRules.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvRules.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void refreshStatuses() {
        boolean monitoring = Prefs.isMonitoringEnabled(this);
        refreshMonitoringUi(monitoring);

        boolean usageGranted = PermissionHelper.hasUsageStatsAccess(this);
        boolean notifGranted = PermissionHelper.hasNotificationPermission(this);

        tvUsageAccessStatus.setText(getString(R.string.status_usage_access) + " : " + (usageGranted ? getString(R.string.granted) : getString(R.string.not_granted)));
        tvNotificationStatus.setText(getString(R.string.status_notifications) + " : " + (notifGranted ? getString(R.string.granted) : getString(R.string.not_granted)));

        btnRequestNotifications.setEnabled(!notifGranted);

        // Si l'utilisateur vient d'accorder l'accès UsageStats, on (re)programme automatiquement.
        MonitorScheduler.schedulePeriodic(this, monitoring && usageGranted);
    }

    private void refreshMonitoringUi(boolean enabled) {
        tvMonitoringStatus.setText(enabled ? R.string.monitoring_active : R.string.monitoring_inactive);
    }

    private void showUsagePermissionHint() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.pedagogy_usage_access)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void confirmDelete(MonitorRule rule) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_rule)
                .setPositiveButton(R.string.yes, (d, w) -> repo.deleteRuleAsync(rule))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQ_POST_NOTIFICATIONS) {
            Log.d(TAG, "POST_NOTIFICATIONS result received");
            refreshStatuses();
        }
    }
}
