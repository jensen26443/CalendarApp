package com.example.calendar.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.calendar.EventDetailsActivity;
import com.example.calendar.R;
import com.example.calendar.receiver.ReminderReceiver;
import com.example.calendar.model.Event;
import com.example.calendar.model.Reminder;

import java.util.Calendar;
import java.util.List;

/**
 * // 功能：提醒管理类，负责注册和取消提醒任务
 */
public class ReminderManager {
    
    private static final String CHANNEL_ID = "calendar_reminder_channel";
    private static final String CHANNEL_NAME = "日程提醒";
    private static final String CHANNEL_DESCRIPTION = "日程提醒通知";
    
    private Context context;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;
    
    public ReminderManager(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    /**
     * 创建通知渠道（Android 8.0及以上版本需要）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * 为事件设置提醒
     * @param event 事件对象
     * @param reminders 提醒列表
     */
    public void scheduleReminders(Event event, List<Reminder> reminders) {


        // 取消之前的提醒
        cancelReminders(event.getId());
        
        // 如果没有提醒，则直接返回
        if (reminders == null || reminders.isEmpty()) {
            return;
        }
        
        // 获取重复事件的所有实例（未来30天内）
        List<Event> events = RecurrenceUtils.getRecurrenceInstances(event, 30);
        
        // 为每个事件实例设置提醒
        for (Event e : events) {
            // 为每个提醒设置闹钟
            for (Reminder reminder : reminders) {
                scheduleReminder(e, reminder);
            }
        }
    }


    
    /**
     * 为单个提醒设置闹钟
     * @param event 事件对象
     * @param reminder 提醒对象
     */
    private void scheduleReminder(Event event, Reminder reminder) {
        // 计算触发时间
        long triggerTime = event.getStartTime() - reminder.getMinutes() * 60 * 1000L;

        // 防止溢出或已经过去
        if (triggerTime <= System.currentTimeMillis()) {
            Log.d("ReminderManager", "提醒时间已过或计算错误: eventId=" + event.getId() + ", triggerTime=" + triggerTime);
            return;
        }

        // 设置 AlarmManager
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_time", event.getStartTime());
        intent.putExtra("event_location", event.getLocation());

        int requestCode = (int) (event.getId() * 100000 + reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d("ReminderManager", "设置闹钟: eventId=" + event.getId() +
                ", reminderId=" + reminder.getId() +
                ", triggerTime=" + triggerTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    /**
     * 取消事件的所有提醒
     * @param eventId 事件ID
     */
    public void cancelReminders(long eventId) {
        // 这里需要知道所有相关的reminder IDs才能取消
        // 在实际实现中，可以从数据库查询所有相关的reminder
        // 为了简化，我们假设requestCode是基于eventId的
        Intent intent = new Intent(context, ReminderReceiver.class);
        for (int i = 0; i < 1000; i++) { // 假设最多有1000个提醒
            int requestCode = (int) (eventId * 1000 + i);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
    
    /**
     * 显示提醒通知
     * @param eventId 事件ID
     * @param title 事件标题
     * @param time 事件时间
     * @param location 事件地点
     */
    public void showReminderNotification(long eventId, String title, long time, String location) {
        // 创建点击通知后打开的意图
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("event_id", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("日程提醒: " + title)
                .setContentText(getNotificationContent(time, location))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // 使用默认声音和振动
        
        notificationManager.notify((int) eventId, builder.build());
    }
    
    /**
     * 构造通知内容文本
     * @param time 事件时间
     * @param location 事件地点
     * @return 格式化的内容文本
     */
    private String getNotificationContent(long time, String location) {
        String timeStr = android.text.format.DateFormat.format("MM月dd日 HH:mm", time).toString();
        if (location != null && !location.isEmpty()) {
            return timeStr + " 在 " + location;
        } else {
            return timeStr;
        }
    }
}