package com.example.calendar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;
import com.example.calendar.model.Subscription;
import com.example.calendar.utils.IcsImportExportUtils;
import com.example.calendar.utils.NetworkUtils;
import com.example.calendar.utils.EventRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // 功能：订阅管理Activity
 */
public class SubscriptionActivity extends AppCompatActivity {
    
    private EditText etName, etUrl;
    private Button btnSave, btnCancel;
    private Button  btnClear;
    
    private AppDatabase db;
    private ExecutorService executorService;
    private Handler mainHandler;
    private SubscriptionActivity eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化数据库
        db = AppDatabase.getDatabase(this);
        
        // 初始化线程池和主线程Handler
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化UI
        initUI();
        
        // 显示上次订阅的信息
        showLastSubscription();
    }
    
    private void initUI() {
        etName = findViewById(R.id.et_name);
        etUrl = findViewById(R.id.et_url);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        btnClear = findViewById(R.id.btn_clear);
        
        btnSave.setOnClickListener(v -> saveSubscription());
        btnCancel.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> showClearSubscribedDialog());

    }
    
    private void showLastSubscription() {
        executorService.execute(() -> {
            // 获取最后一个订阅记录
            List<Subscription> subscriptions = db.eventDao().getAllSubscriptions();
            if (!subscriptions.isEmpty()) {
                Subscription lastSubscription = subscriptions.get(subscriptions.size() - 1);
                mainHandler.post(() -> {
                    etName.setText(lastSubscription.getName());
                    etUrl.setText(lastSubscription.getUrl());
                });
            }
        });
    }
    
    private void saveSubscription() {
        String name = etName.getText().toString().trim();
        String url = etUrl.getText().toString().trim();
        
        if (name.isEmpty()) {
            etName.setError("请输入订阅名称");
            return;
        }
        
        if (url.isEmpty()) {
            etUrl.setError("请输入订阅地址");
            return;
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            etUrl.setError("请输入有效的URL地址");
            return;
        }
        
        // 检查网络连接
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 在后台线程中执行数据库操作
        executorService.execute(() -> {
            // 创建订阅对象，更新频率设为0表示只执行一次
            Subscription subscription = new Subscription(name, url, 0);
            subscription.setLastUpdateTime(System.currentTimeMillis());
            
            // 保存到数据库
            long id = db.eventDao().insertSubscription(subscription);
            subscription.setId(id);
            
            // 立即同步订阅数据
            syncSubscriptionNow(subscription);
            
            // 在主线程中显示结果
            mainHandler.post(() -> {
                Toast.makeText(this, "订阅添加成功并已同步", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
    
    /**
     * 立即同步订阅数据（保存时立即执行）
     * @param subscription 订阅对象
     */
    private void syncSubscriptionNow(Subscription subscription) {
        try {
            // 下载订阅日历文件
            String icsContent = NetworkUtils.downloadSubscriptionCalendar(subscription);
            
            if (icsContent != null) {
                // 解析ICS内容
                List<Event> events = IcsImportExportUtils.parseIcsContent(icsContent);
                
                // 保存事件到数据库
                for (Event event : events) {

                    Log.d("ICS_EVENT",
                            "title=" + event.getTitle()
                                    + ", start=" + event.getStartTime()
                                    + ", end=" + event.getEndTime());

                    // 为订阅事件设置特殊标识，避免与用户自建事件冲突
                    event.setType(3); // 3表示订阅事件
                    db.eventDao().insertEvent(event);
                }
                
                // 更新最后同步时间
                subscription.setLastUpdateTime(System.currentTimeMillis());
                db.eventDao().updateSubscription(subscription);
                
                // 在主线程显示成功消息
                mainHandler.post(() -> 
                    Toast.makeText(SubscriptionActivity.this, 
                        "订阅 \"" + subscription.getName() + "\" 同步完成", 
                        Toast.LENGTH_SHORT).show());
            } else {
                mainHandler.post(() -> 
                    Toast.makeText(SubscriptionActivity.this, 
                        "订阅 \"" + subscription.getName() + "\" 同步失败", 
                        Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            mainHandler.post(() -> 
                Toast.makeText(SubscriptionActivity.this, 
                    "同步过程中发生错误: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
        }
    }


    private void showClearSubscribedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空订阅")
                .setMessage("将删除所有订阅的日历事件，是否继续？")
                .setPositiveButton("确定", (d, w) -> {
                    doClearSubscribedEvents(); // 执行
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void doClearSubscribedEvents() {
        Executors.newSingleThreadExecutor().execute(() -> {

            int count = db.eventDao().countSubscribedEvents();
            Log.d("SUB_CLEAR", "before clear count=" + count);

            db.eventDao().deleteAllSubscribedEvents();

            count = db.eventDao().countSubscribedEvents();
            Log.d("SUB_CLEAR", "after clear count=" + count);

            runOnUiThread(() -> {
                Toast.makeText(this, "订阅事件已清空", Toast.LENGTH_SHORT).show();
            });
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}