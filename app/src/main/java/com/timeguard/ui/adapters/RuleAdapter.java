package com.timeguard.ui.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.timeguard.R;
import com.timeguard.data.MonitorRule;

import java.util.ArrayList;
import java.util.List;

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.VH> {
    private static final String TAG = "RuleAdapter";

    public interface Listener {
        void onToggle(MonitorRule rule, boolean enabled);

        void onEdit(MonitorRule rule);

        void onDelete(MonitorRule rule);
    }

    private final Listener listener;
    private final List<MonitorRule> rules = new ArrayList<>();

    public RuleAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<MonitorRule> newRules) {
        rules.clear();
        if (newRules != null) rules.addAll(newRules);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MonitorRule r = rules.get(position);

        h.tvName.setText(r.appName != null ? r.appName : r.packageName);
        int effectiveCooldown = r.cooldownMinutes > 0 ? r.cooldownMinutes : com.timeguard.helpers.Prefs.getDefaultCooldownMinutes(h.itemView.getContext());
        String cooldownText = r.cooldownMinutes > 0 ? (r.cooldownMinutes + " min") : ("défaut (" + effectiveCooldown + " min)");
        String details = r.packageName + "\nLimite: " + r.limitMinutes + " min • Cooldown: " + cooldownText;
        h.tvDetails.setText(details);

        h.switchEnabled.setOnCheckedChangeListener(null);
        h.switchEnabled.setChecked(r.enabled);
        h.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onToggle(r, isChecked));

        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));

        h.ivIcon.setImageDrawable(loadIconSafely(h.itemView.getContext(), r.packageName));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    private static Drawable loadIconSafely(Context context, String pkg) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationIcon(pkg);
        } catch (Exception e) {
            Log.w(TAG, "loadIconSafely failed for " + pkg, e);
            return context.getDrawable(android.R.drawable.sym_def_app_icon);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;
        final TextView tvDetails;
        final SwitchMaterial switchEnabled;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
