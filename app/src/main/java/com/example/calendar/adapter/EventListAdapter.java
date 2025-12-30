package com.example.calendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.calendar.R;
import com.example.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * // 功能：事件列表适配器，用于在事件列表中显示事件
 */
public class EventListAdapter extends BaseAdapter {
    
    private Context context;
    private List<Event> events;
    
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public EventListAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }
    
    public void updateEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return events != null ? events.size() : 0;
    }
    
    @Override
    public Object getItem(int position) {
        return events != null ? events.get(position) : null;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event_list, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvLocation = convertView.findViewById(R.id.tv_location);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Event event = events.get(position);
        
        // 设置事件标题
        holder.tvTitle.setText(event.getTitle());
        
        // 设置事件时间
        String startTime = timeFormat.format(new Date(event.getStartTime()));
        String endTime = timeFormat.format(new Date(event.getEndTime()));
        holder.tvTime.setText(startTime + " - " + endTime);
        
        // 设置事件地点
        holder.tvLocation.setText(event.getLocation() != null ? event.getLocation() : "无地点");
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvLocation;
    }
}