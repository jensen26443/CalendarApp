package com.example.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;
import com.example.calendar.utils.ReminderManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.widget.Toolbar;

/**
 * // 功能：事件详情Activity，显示事件的详细信息
 */
public class EventDetailsActivity extends AppCompatActivity {
    
    private TextView tvTitle, tvStartTime, tvEndTime, tvLocation, tvType, tvDescription;
    private Button btnEdit, btnDelete;
    
    private AppDatabase db;
    private Event event;
    private ExecutorService executorService;
    
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 显示左上角返回箭头
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 不显示默认标题
        }

        toolbar.setNavigationOnClickListener(v -> {
            // 返回上一个页面
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化数据库和线程池
        db = AppDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
        
        // 初始化UI
        initUI();
        
        // 获取事件ID并加载事件数据
        long eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId != -1) {
            loadEvent(eventId);
        } else {
            Toast.makeText(this, "无效的事件ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initUI() {
        tvTitle = findViewById(R.id.tv_title);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvLocation = findViewById(R.id.tv_location);
        tvType = findViewById(R.id.tv_type);
        tvDescription = findViewById(R.id.tv_description);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        
        // 设置按钮点击事件
        btnEdit.setOnClickListener(v -> {
            if (event != null) {
                Intent intent = new Intent(EventDetailsActivity.this, AddEventActivity.class);
                intent.putExtra("event_id", event.getId());
                // 使用 startActivityForResult 以便在编辑完成后刷新主界面
                startActivityForResult(intent, 1);
            }
        });
        
        btnDelete.setOnClickListener(v -> {
            if (event != null) {
                deleteEvent();
            }
        });
    }
    
    private void loadEvent(long eventId) {
        executorService.execute(() -> {
            try {
                // 从数据库获取事件
                event = db.eventDao().getEventById(eventId);
                
                // 在主线程中更新UI
                runOnUiThread(this::updateUI);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailsActivity.this, "加载事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void updateUI() {
        if (event != null) {
            tvTitle.setText(event.getTitle());
            tvStartTime.setText(dateTimeFormat.format(new Date(event.getStartTime())));
            tvEndTime.setText(dateTimeFormat.format(new Date(event.getEndTime())));
            tvLocation.setText(event.getLocation() != null ? event.getLocation() : "无");
            tvDescription.setText(event.getDescription() != null ? event.getDescription() : "无");
            
            // 设置事件类型
            String[] eventTypes = getResources().getStringArray(R.array.event_types);
            if (event.getType() >= 0 && event.getType() < eventTypes.length) {
                tvType.setText(eventTypes[event.getType()]);
            } else {
                tvType.setText("未知");
            }
        }
    }
    
    private void deleteEvent() {
        executorService.execute(() -> {
            try {
                // 取消提醒
                ReminderManager reminderManager = new ReminderManager(EventDetailsActivity.this);
                reminderManager.cancelReminders(event.getId());
                
                // 从数据库删除事件
                db.eventDao().deleteEvent(event);
                
                // 在主线程中返回结果和显示提示
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailsActivity.this, "事件删除成功", Toast.LENGTH_SHORT).show();
                    // 设置结果码为 RESULT_OK 以便主界面刷新
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                // 在主线程中显示错误提示
                runOnUiThread(() -> 
                    Toast.makeText(EventDetailsActivity.this, "删除事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 如果是从编辑页面返回，且操作成功，则设置结果码以便主界面刷新
        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}