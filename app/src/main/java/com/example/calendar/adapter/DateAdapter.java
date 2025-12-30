package com.example.calendar.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.EventDetailsActivity;
import com.example.calendar.MainActivity;
import com.example.calendar.R;
import com.example.calendar.model.Event;
import com.example.calendar.utils.CalendarUtils;
import com.example.calendar.utils.LunarUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * // 功能：日期适配器，用于在月视图中显示日期
 */
public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    
    private List<CalendarUtils.DateInfo> dateList;
    private List<Event> events;
    private OnDateClickListener onDateClickListener;
    private Context context;
    
    public DateAdapter(Context context, List<CalendarUtils.DateInfo> dateList) {
        this.context = context;
        this.dateList = dateList;
    }
    
    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }
    
    /**
     * 设置事件列表
     * @param events 事件列表
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        CalendarUtils.DateInfo dateInfo = dateList.get(position);
        
        // 设置日期
        holder.tvDay.setText(String.valueOf(dateInfo.getDay()));
        
 /*       // 设置农历日期
        String lunarText = LunarUtils.solarToLunar(dateInfo.getYear(), dateInfo.getMonth(), dateInfo.getDay());
        holder.tvLunar.setText(lunarText);*/

        String lunarText = LunarUtils.getDisplayText(
                dateInfo.getYear(),
                dateInfo.getMonth(),
                dateInfo.getDay()
        );
        holder.tvLunar.setText(lunarText);
        
        // 设置其他月份日期的颜色
        if (dateInfo.isOtherMonth()) {
            holder.tvDay.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
            holder.tvLunar.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        } else {
            holder.tvDay.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
            holder.tvLunar.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }
        
        // 如果是节日，设置特殊颜色
        if (LunarUtils.isLunarHoliday(dateInfo.getYear(), dateInfo.getMonth(), dateInfo.getDay())) {
            holder.tvLunar.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }
        
        // 检查当天是否有事件
        boolean hasEvent = hasEventOnDate(dateInfo);
        holder.tvEventIndicator.setVisibility(hasEvent ? View.VISIBLE : View.GONE);

        boolean isToday = CalendarUtils.isToday(dateInfo);
        if (isToday) {
            // 设置带边框的背景
            holder.itemView.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.bg_border_blue)
            );
            // 文字颜色设为强调色（可选，比如深蓝或黑色）
            //holder.tvDay.setTextColor(context.getColor(android.R.color.black));
            //holder.tvLunar.setTextColor(context.getColor(android.R.color.darker_gray));
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            // 如果有事件，点击进入事件详情页面
            if (hasEvent) {
                // 检查同一天是否有多个事件
                List<Event> eventsOnDate = getEventsOnDate(dateInfo);
                if (eventsOnDate.size() > 1) {
                    // 如果有多个事件，跳转到事件列表页面
                    Intent intent = new Intent(context, com.example.calendar.EventListActivity.class);
                    intent.putExtra("year", dateInfo.getYear());
                    intent.putExtra("month", dateInfo.getMonth());
                    intent.putExtra("day", dateInfo.getDay());
                    context.startActivity(intent);
                } else if (eventsOnDate.size() == 1) {
                    // 只有一个事件，直接打开详情页
                    long eventId = eventsOnDate.get(0).getId();
                    openEventDetails(eventId);
                }
                return;
            }
            
            // 如果没有事件或者获取事件ID失败，执行原来的日期点击监听器
            if (onDateClickListener != null) {
                onDateClickListener.onDateClick(dateInfo);
            }
        });
    }
    
    /**
     * 打开事件详情页面
     * @param eventId 事件ID
     */
    private void openEventDetails(long eventId) {
        // 检查context是否是MainActivity的实例
        if (context instanceof MainActivity) {
            // 使用MainActivity的方法启动事件详情页面
            ((MainActivity) context).startEventDetailsActivity(eventId);
        } else {
            // 回退到直接启动事件详情页面
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("event_id", eventId);
            context.startActivity(intent);
        }
    }
    
    /**
     * 获取指定日期的所有事件
     * @param dateInfo 日期信息
     * @return 该日期的所有事件列表
     */
    private List<Event> getEventsOnDate(CalendarUtils.DateInfo dateInfo) {
        List<Event> eventsOnDate = new ArrayList<>();
        if (events == null || events.isEmpty()) {
            return eventsOnDate;
        }
        
        long dateDayStart = getDayStartTimestamp(dateInfo.getTimeInMillis());
        
        for (Event event : events) {
            long eventDayStart = getDayStartTimestamp(event.getStartTime());
            if (eventDayStart == dateDayStart) {
                eventsOnDate.add(event);
            }
        }
        
        return eventsOnDate;
    }
    
    /**
     * 检查指定日期是否有事件
     * @param dateInfo 日期信息
     * @return 是否有事件
     */
    private boolean hasEventOnDate(CalendarUtils.DateInfo dateInfo) {
        if (events == null || events.isEmpty()) {
            return false;
        }
        
        // 使用Set去重，避免同一天多个事件导致重复计算
        Set<Long> eventDays = new HashSet<>();
        for (Event event : events) {
            long eventDayStart = getDayStartTimestamp(event.getStartTime());
            eventDays.add(eventDayStart);
        }
        
        long dateDayStart = getDayStartTimestamp(dateInfo.getTimeInMillis());
        return eventDays.contains(dateDayStart);
    }
    
    /**
     * 获取指定时间戳当天的起始时间戳（00:00:00）
     * @param timestamp 时间戳
     * @return 当天起始时间戳
     */
    private long getDayStartTimestamp(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    @Override
    public int getItemCount() {
        return dateList != null ? dateList.size() : 0;
    }
    
    /**
     * 更新日期列表
     * @param dateList 新的日期列表
     */
    public void updateDateList(List<CalendarUtils.DateInfo> dateList) {
        this.dateList = dateList;
        notifyDataSetChanged();
    }
    
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvLunar;
        TextView tvEventIndicator;
        
        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvLunar = itemView.findViewById(R.id.tv_lunar);
            tvEventIndicator = itemView.findViewById(R.id.tv_event_indicator);
        }
    }
    
    public interface OnDateClickListener {
        void onDateClick(CalendarUtils.DateInfo dateInfo);
    }
}