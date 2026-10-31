package com.sandeep.vaanibridge;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * VaaniBridge Phase 9: Call History Adapter
 */
public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {

    private List<CallLogModel> logs = new ArrayList<>();

    public void setLogs(List<CallLogModel> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogModel log = logs.get(position);
        holder.tvPhoneNumber.setText(log.getReceiverPhoneNumber());
        holder.tvStatus.setText(log.getCallStatus() != null ? log.getCallStatus() : "COMPLETED");
        
        if ("FAILED".equals(log.getCallStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#CF6679"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#03DAC5"));
        }

        // Format timestamp: 2023-10-27T14:30:00 -> 2023-10-27 14:30
        String timeStr = log.getCallStartTime();
        if (timeStr != null && timeStr.contains("T")) {
            timeStr = timeStr.substring(0, 16).replace("T", " ");
        }
        holder.tvTime.setText(timeStr);
        
        String metrics = String.format(Locale.getDefault(), "Turns: %d | Latency: %dms", 
                log.getTotalTranslationsCount(), log.getAverageLatencyMs());
        holder.tvMetrics.setText(metrics);
        
        String languages = (log.getCallerLanguage() != null ? log.getCallerLanguage().substring(0, 2).toUpperCase() : "TE") 
                + " ↔ " + 
                (log.getReceiverLanguage() != null ? log.getReceiverLanguage().substring(0, 2).toUpperCase() : "HI");
        holder.tvLanguages.setText(languages);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber, tvStatus, tvTime, tvMetrics, tvLanguages;

        ViewHolder(View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMetrics = itemView.findViewById(R.id.tvMetrics);
            tvLanguages = itemView.findViewById(R.id.tvLanguages);
        }
    }
}
