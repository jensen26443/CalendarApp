package com.example.calendar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.calendar.service.SubscriptionSyncService;
import com.example.calendar.utils.ReminderManager;

/**
 * // 功能：开机广播接收器，用于重启提醒服务和订阅同步服务
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "设备启动完成，正在重启服务");
            
            // 重新设置所有提醒
            ReminderManager reminderManager = new ReminderManager(context);
            // 在实际应用中，这里应该从数据库中恢复所有提醒
            
            // 启动订阅同步服务
            Intent syncServiceIntent = new Intent(context, SubscriptionSyncService.class);
            context.startService(syncServiceIntent);
            
            Log.d(TAG, "服务重启完成");
        }
    }
}