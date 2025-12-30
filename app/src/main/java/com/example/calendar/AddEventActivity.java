package com.example.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;

import com.example.calendar.adapter.ReminderAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;
import com.example.calendar.model.Reminder;
import com.example.calendar.utils.ReminderManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // 功能：添加/编辑事件Activity
 */
public class AddEventActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etLocation;
    private Spinner spinnerType, spinnerReminder;
    private RadioButton radioNone, radioDaily, radioWeekly, radioMonthly, radioYearly;
    private Button btnSave, btnCancel, btnAddReminder;
    MaterialCardView btnStartTime, btnEndTime;
    TextView tvStartTime, tvEndTime;
    private SwitchMaterial switchLunar;
    private RecyclerView recyclerReminders;

    private AppDatabase db;
    private Event event; // 如果是编辑模式，会有这个对象
    private List<Reminder> reminderList;
    private ReminderAdapter reminderAdapter;

    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private ExecutorService executorService;

    private MaterialCardView radioGroupRrule;
    private TextView tvRrule;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 申请通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化线程池
        executorService = Executors.newFixedThreadPool(2);

        // 初始化日历对象
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR, 1); // 默认结束时间比开始时间晚1小时

        // 初始化数据库
        db = AppDatabase.getDatabase(this);

        // 初始化提醒列表
        reminderList = new ArrayList<>();

        // 初始化UI
        initUI();

        // 检查是否是编辑模式
        checkEditMode();



    }

    private void initUI() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etLocation = findViewById(R.id.et_location);
        spinnerType = findViewById(R.id.spinner_type);
//        radioNone = findViewById(R.id.radio_none);
//        radioDaily = findViewById(R.id.radio_daily);
//        radioWeekly = findViewById(R.id.radio_weekly);
//        radioMonthly = findViewById(R.id.radio_monthly);
//        radioYearly = findViewById(R.id.radio_yearly);

        radioGroupRrule = findViewById(R.id.radio_group_rrule);
        tvRrule = findViewById(R.id.tv_rrule);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnStartTime = findViewById(R.id.btn_start_time);
        btnEndTime = findViewById(R.id.btn_end_time);
        btnAddReminder = findViewById(R.id.btn_add_reminder);
        switchLunar = findViewById(R.id.switch_lunar);
        recyclerReminders = findViewById(R.id.recycler_reminders);
        tvStartTime = btnStartTime.findViewById(R.id.tv_start_time);
        tvEndTime   = btnEndTime.findViewById(R.id.tv_end_time);

        // 设置提醒列表
        recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        reminderAdapter = new ReminderAdapter(reminderList, this::removeReminder);
        recyclerReminders.setAdapter(reminderAdapter);

        // 设置时间按钮文本
        tvStartTime.setText(dateTimeFormat.format(startCalendar.getTime()));
        tvEndTime.setText(dateTimeFormat.format(endCalendar.getTime()));


        // 设置按钮点击事件
        btnSave.setOnClickListener(v -> saveEvent());
        btnCancel.setOnClickListener(v -> finish());
        btnStartTime.setOnClickListener(v -> showDateTimePicker(true));
        btnEndTime.setOnClickListener(v -> showDateTimePicker(false));
        btnAddReminder.setOnClickListener(v -> addReminder());

        // 重复规则点击弹出选择
        radioGroupRrule.setOnClickListener(v -> showRepeatDialog());

        // 如果是编辑模式，设置初始显示值
        String initialRrule = "永不"; // 默认
        if (event != null && event.getRrule() != null) {
            switch (event.getRrule()) {
                case "FREQ=DAILY": initialRrule = "每天"; break;
                case "FREQ=WEEKLY": initialRrule = "每周"; break;
                case "FREQ=MONTHLY": initialRrule = "每月"; break;
                case "FREQ=YEARLY": initialRrule = "每年"; break;
            }
        }
        tvRrule.setText(initialRrule);
    }


    // 弹窗选择重复规则
    private void showRepeatDialog() {
        String[] options = {"永不", "每天", "每周", "每月", "每年"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择重复规则")
                .setItems(options, (dialog, which) -> {
                    tvRrule.setText(options[which]);
                });
        builder.show();
    }
    private void showDateTimePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;

        // 日期选择（可左右滑动）
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("选择日期")
                        .setSelection(calendar.getTimeInMillis())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            calendar.setTimeInMillis(selection);

            // 时间选择（滑动/拨盘）
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                            .setMinute(calendar.get(Calendar.MINUTE))
                            .setTitleText("选择时间")
                            .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (isStart) {
                    tvStartTime.setText(dateTimeFormat.format(startCalendar.getTime()));
                } else {
                    tvEndTime.setText(dateTimeFormat.format(endCalendar.getTime()));
                }
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void addReminder() {
        // 直接添加一个默认的提醒（0分钟前，即准时提醒）
        Reminder reminder = new Reminder(0, 0); // eventId会在保存时设置，0分钟前
        reminderList.add(reminder);
        reminderAdapter.notifyDataSetChanged();
    }

    private void removeReminder(Reminder reminder) {
        reminderList.remove(reminder);
        reminderAdapter.notifyDataSetChanged();
    }

    private void checkEditMode() {
        // 检查是否有传入的事件ID，如果有则是编辑模式
        long eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId != -1) {
            // 是编辑模式，加载事件数据
            loadEvent(eventId);
        }
    }

    private void loadEvent(long eventId) {
        executorService.execute(() -> {
            try {
                // 从数据库加载事件数据
                event = db.eventDao().getEventById(eventId);

                // 加载事件的提醒
                List<Reminder> eventReminders = db.eventDao().getRemindersByEventId(eventId);

                // 在主线程中填充UI控件
                runOnUiThread(() -> {
                    if (event != null) {
                        etTitle.setText(event.getTitle());
                        etDescription.setText(event.getDescription());
                        etLocation.setText(event.getLocation());

                        // 安全地设置事件类型，避免数组越界
                        int eventType = event.getType();
                        String[] eventTypes = getResources().getStringArray(R.array.event_types);
                        if (eventType >= 0 && eventType < eventTypes.length) {
                            spinnerType.setSelection(eventType);
                        } else {
                            // 如果事件类型超出范围，默认选择第一个类型
                            spinnerType.setSelection(0);
                        }

                        // 设置时间
                        startCalendar.setTimeInMillis(event.getStartTime());
                        endCalendar.setTimeInMillis(event.getEndTime());
                        tvStartTime.setText(dateTimeFormat.format(startCalendar.getTime()));
                        tvEndTime.setText(dateTimeFormat.format(endCalendar.getTime()));

                        // 设置农历开关
                        switchLunar.setChecked(event.isLunar());

 /*                       // 设置重复规则
                        String rrule = event.getRrule();
                        if (rrule != null) {
                            switch (rrule) {
                                case "FREQ=DAILY":
                                    radioDaily.setChecked(true);
                                    break;
                                case "FREQ=WEEKLY":
                                    radioWeekly.setChecked(true);
                                    break;
                                case "FREQ=MONTHLY":
                                    radioMonthly.setChecked(true);
                                    break;
                                case "FREQ=YEARLY":
                                    radioYearly.setChecked(true);
                                    break;
                                default:
                                    radioNone.setChecked(true);
                                    break;
                            }
                        } else {
                            radioNone.setChecked(true);
                        }*/

                        // 设置重复规则（tvRrule显示文字）
                        String rrule = event.getRrule();
                        String repeatText = "永不"; // 默认
                        if (rrule != null) {
                            switch (rrule) {
                                case "FREQ=DAILY":
                                    repeatText = "每天";
                                    break;
                                case "FREQ=WEEKLY":
                                    repeatText = "每周";
                                    break;
                                case "FREQ=MONTHLY":
                                    repeatText = "每月";
                                    break;
                                case "FREQ=YEARLY":
                                    repeatText = "每年";
                                    break;
                                default:
                                    repeatText = "永不";
                                    break;
                            }
                        }
                        tvRrule.setText(repeatText);

                        // 设置提醒列表
                        reminderList.clear();
                        reminderList.addAll(eventReminders);
                        reminderAdapter.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AddEventActivity.this, "加载事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveEvent() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        int type = spinnerType.getSelectedItemPosition();

        if (title.isEmpty()) {
            etTitle.setError("请输入标题");
            return;
        }

        if (endCalendar.before(startCalendar)) {
            Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建或更新事件
        if (event == null) {
            event = new Event();
        }

        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setType(type);
        event.setStartTime(startCalendar.getTimeInMillis());
        event.setEndTime(endCalendar.getTimeInMillis());
        event.setLunar(switchLunar.isChecked());

/*        // 设置重复规则
        String rrule = "";
        if (radioDaily.isChecked()) {
            rrule = "FREQ=DAILY";
        } else if (radioWeekly.isChecked()) {
            rrule = "FREQ=WEEKLY";
        } else if (radioMonthly.isChecked()) {
            rrule = "FREQ=MONTHLY";
        } else if (radioYearly.isChecked()) {
            rrule = "FREQ=YEARLY";
        }
        event.setRrule(rrule);*/


        // 设置重复规则
        String rrule = "";
        switch (tvRrule.getText().toString()) {
            case "每天": rrule = "FREQ=DAILY"; break;
            case "每周": rrule = "FREQ=WEEKLY"; break;
            case "每月": rrule = "FREQ=MONTHLY"; break;
            case "每年": rrule = "FREQ=YEARLY"; break;
            default: rrule = ""; break; // 永不
        }
        event.setRrule(rrule);


        // 在后台线程中保存到数据库
        executorService.execute(() -> {
            try {
                // 保存到数据库
                long eventId;
                if (event.getId() == 0) {
                    // 新增事件
                    eventId = db.eventDao().insertEvent(event);
                    event.setId(eventId);
                } else {
                    // 更新事件
                    db.eventDao().updateEvent(event);
                    eventId = event.getId();
                }

                // 保存提醒
                // 先删除旧的提醒
                db.eventDao().deleteRemindersByEventId(eventId);

                // 保存新的提醒
                for (Reminder reminder : reminderList) {
 /*                   reminder.setEventId(eventId);
                    db.eventDao().insertReminder(reminder);*/

                    reminder.setEventId(eventId);
                    long reminderId = db.eventDao().insertReminder(reminder);
                    reminder.setId((int) reminderId); // 确保 reminderId 不为 0
                }

                // 确保通知权限再设置提醒
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    ReminderManager reminderManager = new ReminderManager(AddEventActivity.this);
                    reminderManager.scheduleReminders(event, reminderList);
                }

                // 设置提醒
/*
                ReminderManager reminderManager = new ReminderManager(AddEventActivity.this);
                reminderManager.scheduleReminders(event, reminderList);
*/

                // 在主线程中返回结果和显示提示
                runOnUiThread(() -> {
                    // 先显示提示再finish
                    Toast.makeText(AddEventActivity.this, "事件保存成功", Toast.LENGTH_SHORT).show();
                    // 返回结果
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                // 在主线程中显示错误提示
                runOnUiThread(() ->
                        Toast.makeText(AddEventActivity.this, "保" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限未授予，日程提醒可能无法正常工作", Toast.LENGTH_LONG).show();
            }
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