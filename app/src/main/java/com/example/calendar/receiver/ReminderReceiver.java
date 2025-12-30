package com.example.calendar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.calendar.utils.ReminderManager;

/**
 * // 功能：提醒广播接收器，接收定时提醒广播并显示通知
 */
public class ReminderReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra("event_id", -1);
        String title = intent.getStringExtra("event_title");
        long time = intent.getLongExtra("event_time", System.currentTimeMillis());
        String location = intent.getStringExtra("event_location");
        
        if (eventId != -1 && title != null) {
            ReminderManager reminderManager = new ReminderManager(context);
            reminderManager.showReminderNotification(eventId, title, time, location);
        }
    }
}