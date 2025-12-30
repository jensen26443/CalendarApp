package com.example.calendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.calendar.adapter.CalendarViewPagerAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;
import com.example.calendar.service.SubscriptionSyncService;
import com.example.calendar.ui.fragment.MonthViewFragment;
import com.example.calendar.utils.FileExportUtils;
import com.example.calendar.utils.IcsImportExportUtils;

import java.util.Calendar;
import java.util.List;

/**
 * 主 Activity（右上角操作栏版本）
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private CalendarViewPagerAdapter adapter;

    // 右上角控件
    private ImageButton btnAddEvent;
    private ImageButton btnMore;
    private TextView btnToday;
    private TextView btnViewMode;


    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_ADD_EVENT = 1002;
    private static final int REQUEST_CODE_EVENT_DETAILS = 1003;

    private int currentYear;
    private int currentMonth;

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initFilePicker();
        requestStoragePermissions();

        // 启动订阅同步服务
        startService(new Intent(this, SubscriptionSyncService.class));

        initUI();
    }

    private void initUI() {
        viewPager = findViewById(R.id.view_pager);

        btnAddEvent = findViewById(R.id.btn_add_event);
        btnMore = findViewById(R.id.btn_more);
        btnToday = findViewById(R.id.btn_today);
        btnViewMode = findViewById(R.id.btn_view_mode);
        btnViewMode.setOnClickListener(v -> showViewModeMenu());

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        adapter = new CalendarViewPagerAdapter(this, currentYear, currentMonth);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(100, false);

        // + 添加事件
        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEventActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EVENT);
        });

        // 今天
        btnToday.setOnClickListener(v -> goToCurrentMonth());



/*        // 日 / 周 / 月 下拉
        spinnerViewMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        switchToView(CalendarViewPagerAdapter.VIEW_TYPE_MONTH);
                        break;
                    case 1:
                        switchToView(CalendarViewPagerAdapter.VIEW_TYPE_WEEK);
                        break;
                    case 2:
                        switchToView(CalendarViewPagerAdapter.VIEW_TYPE_DAY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/

        // 更多菜单
        btnMore.setOnClickListener(v -> showMoreMenu());
    }

    private void showViewModeMenu() {
        PopupMenu menu = new PopupMenu(this, btnViewMode);
        menu.getMenu().add("日视图");
        menu.getMenu().add("周视图");
        menu.getMenu().add("月视图");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "日视图":
                    switchToView(CalendarViewPagerAdapter.VIEW_TYPE_DAY);
                    btnViewMode.setText("日 ▾");
                    break;
                case "周视图":
                    switchToView(CalendarViewPagerAdapter.VIEW_TYPE_WEEK);
                    btnViewMode.setText("周 ▾");
                    break;
                case "月视图":
                    switchToView(CalendarViewPagerAdapter.VIEW_TYPE_MONTH);
                    btnViewMode.setText("月 ▾");
                    break;
            }
            return true;
        });
        menu.show();
    }


    private void showMoreMenu() {
        PopupMenu menu = new PopupMenu(this, btnMore);
        menu.getMenu().add("导入");
        menu.getMenu().add("导出");
        menu.getMenu().add("订阅");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "导入":
                    importEvents();
                    break;
                case "导出":
                    exportEvents();
                    break;
                case "订阅":
                    startActivity(new Intent(this, SubscriptionActivity.class));
                    break;
            }
            return true;
        });
        menu.show();
    }

    private void goToCurrentMonth() {
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH);

        adapter = new CalendarViewPagerAdapter(this, currentYear, currentMonth);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(100, false);
        btnViewMode.setText("月 ▾");

        refreshCurrentView();
    }

    private void switchToView(int viewType) {
        adapter.setViewType(viewType);
        viewPager.setAdapter(adapter);

        if (viewType == CalendarViewPagerAdapter.VIEW_TYPE_MONTH) {
            viewPager.setCurrentItem(100, false);
        } else {
            viewPager.setCurrentItem(0, false);
        }
    }

    private void refreshCurrentView() {
        adapter.notifyDataSetChanged();

        int position = viewPager.getCurrentItem();
        if (adapter.getCurrentViewType() == CalendarViewPagerAdapter.VIEW_TYPE_MONTH) {
            int[] dateInfo = adapter.getDateInfo(position);
            MonthViewFragment fragment = (MonthViewFragment)
                    getSupportFragmentManager().findFragmentByTag("f" + position);
            if (fragment != null) {
                fragment.refreshEvents();
            }
        }
    }

    private void initFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::importEventsFromFile
        );
    }

    private void importEvents() {
        filePickerLauncher.launch("text/calendar");
    }

    private void importEventsFromFile(Uri uri) {
        if (uri == null) return;

        new Thread(() -> {
            try {
                String content = IcsImportExportUtils.readIcsFile(this, uri);
                List<Event> events = IcsImportExportUtils.parseIcsContent(content);

                AppDatabase db = AppDatabase.getDatabase(this);
                for (Event e : events) {
                    e.setType(4);
                    db.eventDao().insertEvent(e);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "成功导入 " + events.size() + " 个事件", Toast.LENGTH_SHORT).show();
                    refreshCurrentView();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "导入失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void exportEvents() {
        new Thread(() -> {
            try {
                List<Event> events = AppDatabase.getDatabase(this)
                        .eventDao().getAllEvents();

                runOnUiThread(() ->
                        FileExportUtils.exportEventsToICS(
                                this,
                                events,
                                "calendar_export_" + System.currentTimeMillis() + ".ics"
                        ));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
        }
    }

    public void startEventDetailsActivity(long eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("event_id", eventId);
        startActivityForResult(intent, REQUEST_CODE_EVENT_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_ADD_EVENT ||
                requestCode == REQUEST_CODE_EVENT_DETAILS)
                && resultCode == RESULT_OK) {
            refreshCurrentView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentView();
    }
}
