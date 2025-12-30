package com.example.calendar.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.EventDetailsActivity;
import com.example.calendar.R;
import com.example.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：周视图适配器（每格可显示多个事件，支持点击弹窗）
 */
public class WeekViewAdapter extends RecyclerView.Adapter<WeekViewAdapter.HourViewHolder> {

    private List<String> hours;
    // key: hour*7 + dayOfWeekIndex
    private Map<Integer, List<Event>> eventMap = new HashMap<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");

    public WeekViewAdapter() {
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
            int dayOfWeekIndex = c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if (dayOfWeekIndex < 0) dayOfWeekIndex += 7;
            int key = hour * 7 + dayOfWeekIndex;
            eventMap.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_hour, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        holder.tvTime.setText(hours.get(position));

        // 清空旧事件
        holder.llMon.removeAllViews();
        holder.llTue.removeAllViews();
        holder.llWed.removeAllViews();
        holder.llThu.removeAllViews();
        holder.llFri.removeAllViews();
        holder.llSat.removeAllViews();
        holder.llSun.removeAllViews();

        // 每一天添加事件
        addEventsToCell(holder.llMon, position, 0);
        addEventsToCell(holder.llTue, position, 1);
        addEventsToCell(holder.llWed, position, 2);
        addEventsToCell(holder.llThu, position, 3);
        addEventsToCell(holder.llFri, position, 4);
        addEventsToCell(holder.llSat, position, 5);
        addEventsToCell(holder.llSun, position, 6);
    }

//    private void addEventsToCell(LinearLayout cell, int hour, int dayOfWeekIndex) {
//        int key = hour * 7 + dayOfWeekIndex;
//        List<Event> events = eventMap.get(key);
//        if (events != null) {
//            for (Event e : events) {
//                TextView tv = new TextView(cell.getContext());
//                tv.setText(e.getTitle());
//                tv.setTextSize(12);
//                tv.setTextColor(0xFF000000);
//                tv.setPadding(2, 2, 2, 2);
//                tv.setOnClickListener(v -> {
//                    // 点击事件弹窗显示详情
//                    String message = "标题: " + e.getTitle() + "\n"
//                            + "开始: " + sdf.format(e.getStartTime()) + "\n"
//                            + "结束: " + sdf.format(e.getEndTime()) + "\n"
//                            + "备注: " + e.getDescription();
//                    new AlertDialog.Builder(cell.getContext())
//                            .setTitle("事件详情")
//                            .setMessage(message)
//                            .setPositiveButton("确定", null)
//                            .show();
//                });
//                cell.addView(tv);
//            }
//        }
//    }

    private void addEventsToCell(LinearLayout cell, int hour, int dayOfWeekIndex) {
        int key = hour * 7 + dayOfWeekIndex;
        List<Event> events = eventMap.get(key);
        if (events != null) {
            for (Event e : events) {
                TextView tv = new TextView(cell.getContext());
                tv.setText(e.getTitle());
                tv.setTextSize(12);
                tv.setTextColor(0xFF000000);
                tv.setPadding(2, 2, 2, 2);

                tv.setOnClickListener(v -> {
                    // 点击事件跳转到 EventDetailsActivity，通过 event_id 加载
                    Intent intent = new Intent(cell.getContext(), com.example.calendar.EventDetailsActivity.class);
                    intent.putExtra("event_id", e.getId()); // 传事件ID
                    cell.getContext().startActivity(intent);
                });

                cell.addView(tv);
            }
        }
    }

    @Override
    public int getItemCount() {
        return hours.size();
    }

    static class HourViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        LinearLayout llMon, llTue, llWed, llThu, llFri, llSat, llSun;

        HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            llMon = itemView.findViewById(R.id.ll_mon);
            llTue = itemView.findViewById(R.id.ll_tue);
            llWed = itemView.findViewById(R.id.ll_wed);
            llThu = itemView.findViewById(R.id.ll_thu);
            llFri = itemView.findViewById(R.id.ll_fri);
            llSat = itemView.findViewById(R.id.ll_sat);
            llSun = itemView.findViewById(R.id.ll_sun);
        }
    }
}
