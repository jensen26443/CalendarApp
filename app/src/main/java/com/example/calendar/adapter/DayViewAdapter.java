package com.example.calendar.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.EventDetailsActivity;
import com.example.calendar.R;
import com.example.calendar.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * // 功能：日视图适配器
 */
public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.HourViewHolder> {
    
    private List<String> hours;
    private Map<Integer, List<Event>> eventMap = new HashMap<>();

    public DayViewAdapter() {
        // 创建24小时列表
        hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
        }
    }
    public void setEvents(List<Event> events) {
        eventMap.clear();
        for (Event e : events) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.getStartTime());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            eventMap.computeIfAbsent(hour, k -> new ArrayList<>()).add(e);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_hour, parent, false);
        return new HourViewHolder(view);
    }


//    @Override
//    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
//        holder.tvTime.setText(hours.get(position));
//
//        List<Event> events = eventMap.get(position);
//        if (events != null && !events.isEmpty()) {
//            holder.tvEvent.setText(events.get(0).getTitle());
//        } else {
//            holder.tvEvent.setText("");
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        holder.tvTime.setText(hours.get(position));

        // RecyclerView 复用，先清空
        holder.eventContainer.removeAllViews();

        List<Event> events = eventMap.get(position);
        if (events == null || events.isEmpty()) {
            return;
        }

        for (Event event : events) {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText(event.getTitle());
            tv.setTextSize(14f);
            tv.setTextColor(0xFF333333);
            tv.setPadding(8, 4, 8, 4);
//            tv.setBackgroundResource(R.drawable.event_bg); // 可选

            // 点击跳转事件详情
            tv.setOnClickListener(v -> {
                Intent intent = new Intent(
                        holder.itemView.getContext(),
                        EventDetailsActivity.class
                );
                intent.putExtra("event_id", event.getId());
                holder.itemView.getContext().startActivity(intent);
            });

            holder.eventContainer.addView(tv);
        }
    }



    @Override
    public int getItemCount() {
        return hours.size();
    }
    
    static class HourViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        LinearLayout eventContainer;

        
        HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            eventContainer = itemView.findViewById(R.id.event_container);
        }
    }
}