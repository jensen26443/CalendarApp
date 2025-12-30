package com.example.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calendar.adapter.EventListAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // 功能：事件列表Activity，显示同一天的所有事件
 */
public class EventListActivity extends AppCompatActivity {
    
    private ListView listView;
    private TextView tvDate;
    private Button btnAddEvent;
    
    private EventListAdapter adapter;
    private List<Event> events;
    
    private AppDatabase db;
    private ExecutorService executorService;
    
    private int year;
    private int month;
    private int day;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化数据库和线程池
        db = AppDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
        
        // 获取传入的日期参数
        year = getIntent().getIntExtra("year", 0);
        month = getIntent().getIntExtra("month", 0);
        day = getIntent().getIntExtra("day", 0);
        
        // 初始化UI
        initUI();
        
        // 加载事件数据
        loadEvents();
    }
    
    private void initUI() {
        listView = findViewById(R.id.list_view);
        tvDate = findViewById(R.id.tv_date);
        btnAddEvent = findViewById(R.id.btn_add_event);
        
        // 设置日期显示
        Date date = new Date(year - 1900, month, day);
        tvDate.setText(dateFormat.format(date));
        
        // 设置添加事件按钮点击事件
        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            startActivity(intent);
        });
    }
    
    private void loadEvents() {
        executorService.execute(() -> {
            try {
                // 计算当天的开始和结束时间戳
                long startTime = new Date(year - 1900, month, day).getTime();
                long endTime = new Date(year - 1900, month, day, 23, 59, 59).getTime();
                
                // 查询数据库获取事件
                events = db.eventDao().getEventsByDateRange(startTime, endTime);
                
                // 在主线程中更新UI
                runOnUiThread(() -> {
                    updateUI();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EventListActivity.this, "加载事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateUI() {
        if (adapter == null) {
            adapter = new EventListAdapter(this, events);
            listView.setAdapter(adapter);
            
            // 设置列表项点击事件
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Event event = events.get(position);
                Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                intent.putExtra("event_id", event.getId());
                startActivity(intent);
            });
        } else {
            adapter.updateEvents(events);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载事件数据，以防在其他地方有修改
        loadEvents();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}