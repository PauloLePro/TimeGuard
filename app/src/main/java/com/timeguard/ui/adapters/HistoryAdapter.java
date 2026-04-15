package com.timeguard.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timeguard.R;
import com.timeguard.data.NotificationLog;
import com.timeguard.helpers.TimeFormatUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<NotificationLog> items = new ArrayList<>();

    public void submitList(List<NotificationLog> logs) {
        items.clear();
        if (logs != null) items.addAll(logs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NotificationLog l = items.get(position);
        holder.tvTitle.setText((l.appName != null ? l.appName : l.packageName) + " • " + l.packageName);
        String details = "Durée constatée: " + TimeFormatUtils.formatDurationShort(l.observedSeconds)
                + " • Limite: " + l.limitMinutes + " min\n"
                + "Envoyé: " + TimeFormatUtils.formatDateTime(l.sentAtMs);
        holder.tvDetails.setText(details);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvDetails;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}

