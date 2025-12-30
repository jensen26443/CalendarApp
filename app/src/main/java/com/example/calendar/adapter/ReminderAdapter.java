package com.example.calendar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.R;
import com.example.calendar.model.Reminder;

import java.util.List;

/**
 * // 功能：提醒列表适配器
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    
    private List<Reminder> reminderList;
    private OnReminderRemoveListener removeListener;
    
    public ReminderAdapter(List<Reminder> reminderList, OnReminderRemoveListener removeListener) {
        this.reminderList = reminderList;
        this.removeListener = removeListener;
    }
    
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        int minutes = reminder.getMinutes();
        
        // 修改显示文本为"准时提醒"而不是"0分钟前"
        String reminderText;
        if (minutes == 0) {
            reminderText = "准时提醒";
        } else if (minutes < 60) {
            reminderText = minutes + " 分钟前";
        } else if (minutes < 1440) {
            reminderText = (minutes / 60) + " 小时前";
        } else {
            reminderText = (minutes / 1440) + " 天前";
        }
        
        holder.tvReminder.setText(reminderText);
        holder.btnDelete.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onReminderRemove(reminder);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return reminderList != null ? reminderList.size() : 0;
    }
    
    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminder;
        ImageButton btnDelete;
        
        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReminder = itemView.findViewById(R.id.tv_reminder);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
    
    public interface OnReminderRemoveListener {
        void onReminderRemove(Reminder reminder);
    }
}