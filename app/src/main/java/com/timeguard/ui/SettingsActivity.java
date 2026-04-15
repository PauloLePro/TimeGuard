package com.timeguard.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.timeguard.R;
import com.timeguard.helpers.Prefs;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_title);

        EditText etDefaultLimit = findViewById(R.id.etDefaultLimit);
        EditText etDefaultCooldown = findViewById(R.id.etDefaultCooldown);
        EditText etCheckInterval = findViewById(R.id.etCheckInterval);
        SwitchMaterial switchPersistentNotif = findViewById(R.id.switchPersistentNotif);
        Button btnSave = findViewById(R.id.btnSave);

        etDefaultLimit.setText(String.valueOf(Prefs.getDefaultLimitMinutes(this)));
        etDefaultCooldown.setText(String.valueOf(Prefs.getDefaultCooldownMinutes(this)));
        etCheckInterval.setText(String.valueOf(Prefs.getCheckIntervalMinutes(this)));
        switchPersistentNotif.setChecked(Prefs.isPersistentNotificationEnabled(this));

        btnSave.setOnClickListener(v -> {
            int limit = parseInt(etDefaultLimit.getText().toString(), Prefs.DEFAULT_LIMIT_MIN);
            int cooldown = parseInt(etDefaultCooldown.getText().toString(), Prefs.DEFAULT_COOLDOWN_MIN);
            int interval = parseInt(etCheckInterval.getText().toString(), Prefs.DEFAULT_CHECK_INTERVAL_MIN);
            Prefs.setDefaultLimitMinutes(this, clamp(limit, 1, 24 * 60));
            Prefs.setDefaultCooldownMinutes(this, clamp(cooldown, 1, 24 * 60));
            Prefs.setCheckIntervalMinutes(this, clamp(interval, 1, 60));
            Prefs.setPersistentNotificationEnabled(this, switchPersistentNotif.isChecked());
            finish();
        });
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
