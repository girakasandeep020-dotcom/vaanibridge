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

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    private List<CallLog> logs = new ArrayList<>();

    public void setLogs(List<CallLog> logs) {
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
        CallLog log = logs.get(position);
        holder.tvPhoneNumber.setText(log.receiverPhoneNumber);
        holder.tvStatus.setText(log.callStatus != null ? log.callStatus : "COMPLETED");
        
        if ("FAILED".equals(log.callStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#CF6679"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#03DAC5"));
        }

        // Simple date parsing or just use raw string from backend
        holder.tvTime.setText(log.callStartTime != null ? log.callStartTime.replace("T", " ") : "N/A");
        
        String metrics = String.format("Turns: %d | Latency: %dms", log.totalTranslationsCount, log.averageLatencyMs);
        holder.tvMetrics.setText(metrics);
        
        String languages = (log.callerLanguage != null ? log.callerLanguage.substring(0, 2).toUpperCase() : "TE") 
                + " ↔ " + 
                (log.receiverLanguage != null ? log.receiverLanguage.substring(0, 2).toUpperCase() : "HI");
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
