package com.example.calendar.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.calendar.MainActivity;
import com.example.calendar.R;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Subscription;
import com.example.calendar.utils.IcsImportExportUtils;
import com.example.calendar.utils.NetworkUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 订阅同步服务，定期同步网络日历订阅
 */
public class SubscriptionSyncService extends Service {
    private static final String TAG = "SubscriptionSyncService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "subscription_sync_service";
    
    private ScheduledExecutorService scheduler;
    private AppDatabase db;
    private Handler mainHandler;
    private boolean isForegroundStarted = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "订阅同步服务启动");
        db = AppDatabase.getDatabase(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 初始化定时任务执行器
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // 启动定时同步任务
        startPeriodicSync();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动前台服务
        startForegroundService();
        return START_STICKY; // 服务被杀死后会重启
    }
    
    /**
     * 创建通知渠道（Android 8.0及以上版本需要）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "订阅同步服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("用于定期同步网络日历订阅");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
    
    /**
     * 启动前台服务
     */
    private void startForegroundService() {
        if (!isForegroundStarted) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("日历订阅同步服务")
                    .setContentText("正在后台同步您的日历订阅")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
            isForegroundStarted = true;
        }
    }
    
    /**
     * 启动定期同步任务
     */
    private void startPeriodicSync() {
        // 每小时检查一次是否需要同步
        scheduler.scheduleAtFixedRate(this::checkAndSyncSubscriptions, 0, 1, TimeUnit.HOURS);
    }
    
    /**
     * 检查并同步订阅
     */
    private void checkAndSyncSubscriptions() {
        try {
            List<Subscription> subscriptions = db.eventDao().getAllSubscriptions();
            long currentTime = System.currentTimeMillis();
            
            for (Subscription subscription : subscriptions) {
                // 如果订阅启用且到了同步时间，则执行同步
                if (subscription.isEnabled() && subscription.getUpdateInterval() > 0) {
                    long nextSyncTime = subscription.getLastUpdateTime() + subscription.getUpdateInterval();
                    if (currentTime >= nextSyncTime) {
                        syncSubscription(subscription);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查订阅同步时出错", e);
        }
    }
    
    /**
     * 同步单个订阅
     * @param subscription 订阅对象
     */
    private void syncSubscription(Subscription subscription) {
        new Thread(() -> {
            try {
                // 下载订阅日历文件
                String icsContent = NetworkUtils.downloadSubscriptionCalendar(subscription);
                
                if (icsContent != null) {
                    // 解析ICS内容
                    List<com.example.calendar.model.Event> events = IcsImportExportUtils.parseIcsContent(icsContent);
                    
                    // 保存事件到数据库
                    for (com.example.calendar.model.Event event : events) {
                        // 为订阅事件设置特殊标识，避免与用户自建事件冲突
                        event.setType(3); // 3表示订阅事件
                        event.setSubscriptionId(subscription.getId()); // 关联订阅ID
                        db.eventDao().insertEvent(event);
                    }
                    
                    // 更新最后同步时间
                    subscription.setLastUpdateTime(System.currentTimeMillis());
                    db.eventDao().updateSubscription(subscription);
                    
                    Log.d(TAG, "订阅 \"" + subscription.getName() + "\" 同步完成");
                } else {
                    Log.w(TAG, "订阅 \"" + subscription.getName() + "\" 同步失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "同步订阅 \"" + subscription.getName() + "\" 时出错", e);
            }
        }).start();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不支持绑定
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) {
            scheduler.shutdown();
        }
        Log.d(TAG, "订阅同步服务停止");
    }
}