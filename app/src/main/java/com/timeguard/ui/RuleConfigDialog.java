package com.timeguard.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.timeguard.R;
import com.timeguard.data.MonitorRule;
import com.timeguard.helpers.Prefs;

/**
 * Dialogue simple pour créer/modifier une règle.
 */
public final class RuleConfigDialog {

    public interface Callback {
        void onSaved(MonitorRule rule);
    }

    private RuleConfigDialog() {
    }

    public static void showCreate(Activity activity, MonitorRule draft, Callback cb) {
        show(activity, draft, cb);
    }

    public static void showEdit(Activity activity, MonitorRule existing, Callback cb) {
        // Copie défensive (évite les effets de bord si l’utilisateur annule).
        MonitorRule copy = new MonitorRule();
        copy.id = existing.id;
        copy.packageName = existing.packageName;
        copy.appName = existing.appName;
        copy.limitMinutes = existing.limitMinutes;
        copy.cooldownMinutes = existing.cooldownMinutes;
        copy.enabled = existing.enabled;
        copy.lastNotifiedAtMs = existing.lastNotifiedAtMs;
        show(activity, copy, cb);
    }

    private static void show(Activity activity, MonitorRule rule, Callback cb) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_rule_config, null, false);

        TextView tvTitle = v.findViewById(R.id.tvAppTitle);
        TextView tvPkg = v.findViewById(R.id.tvAppPackage);
        EditText etLimit = v.findViewById(R.id.etLimitMinutes);
        EditText etCooldown = v.findViewById(R.id.etCooldownMinutes);
        SwitchMaterial swEnabled = v.findViewById(R.id.switchEnabled);

        tvTitle.setText(rule.appName != null ? rule.appName : rule.packageName);
        tvPkg.setText(rule.packageName);

        etLimit.setText(String.valueOf(Math.max(1, rule.limitMinutes)));
        if (rule.cooldownMinutes > 0) {
            etCooldown.setText(String.valueOf(rule.cooldownMinutes));
        } else {
            // Optionnel : vide ou "0" => défaut.
            etCooldown.setText("");
        }
        swEnabled.setChecked(rule.enabled);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_rule_title)
                .setView(v)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    rule.limitMinutes = clamp(parseInt(etLimit.getText().toString(), Prefs.getDefaultLimitMinutes(activity)), 1, 24 * 60);
                    int rawCooldown = parseIntAllowEmpty(etCooldown.getText().toString(), 0);
                    // 0 => valeur par défaut (voir Prefs). Sinon clamp.
                    rule.cooldownMinutes = rawCooldown <= 0 ? 0 : clamp(rawCooldown, 1, 24 * 60);
                    rule.enabled = swEnabled.isChecked();
                    cb.onSaved(rule);
                })
                .show();
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int parseIntAllowEmpty(String s, int fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        if (t.isEmpty()) return fallback;
        return parseInt(t, fallback);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
