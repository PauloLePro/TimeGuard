package com.timeguard.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timeguard.R;
import com.timeguard.ui.model.InstalledAppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {

    public interface Listener {
        void onAppClicked(InstalledAppInfo app);
    }

    private final Listener listener;
    private final List<InstalledAppInfo> all = new ArrayList<>();
    private final List<InstalledAppInfo> filtered = new ArrayList<>();

    public AppAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<InstalledAppInfo> apps) {
        all.clear();
        filtered.clear();
        if (apps != null) {
            all.addAll(apps);
            filtered.addAll(apps);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filtered.clear();
        if (q.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (InstalledAppInfo a : all) {
                if (a.label.toLowerCase(Locale.ROOT).contains(q) || a.packageName.toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(a);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        InstalledAppInfo app = filtered.get(position);
        holder.ivIcon.setImageDrawable(app.icon);
        holder.tvName.setText(app.label);
        holder.tvPackage.setText(app.packageName);
        holder.itemView.setOnClickListener(v -> listener.onAppClicked(app));
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;
        final TextView tvPackage;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvPackage = itemView.findViewById(R.id.tvPackage);
        }
    }
}

