package com.example.calendar.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.R;
import com.example.calendar.adapter.WeekViewAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // 功能：周视图Fragment，显示一周的日程安排
 */
public class WeekViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeekViewAdapter adapter;
    private TextView[] dateTextViews;

    private ExecutorService executorService;

    private AppDatabase db;

    public static WeekViewFragment newInstance() {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();

        db = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_view, container, false);
        initViews(view);
        updateWeekDates();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEventsForWeek();
    }
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置适配器
        adapter = new WeekViewAdapter();
        recyclerView.setAdapter(adapter);

        // 初始化日期文本视图数组
        dateTextViews = new TextView[7];
        dateTextViews[0] = view.findViewById(R.id.tv_monday_date);
        dateTextViews[1] = view.findViewById(R.id.tv_tuesday_date);
        dateTextViews[2] = view.findViewById(R.id.tv_wednesday_date);
        dateTextViews[3] = view.findViewById(R.id.tv_thursday_date);
        dateTextViews[4] = view.findViewById(R.id.tv_friday_date);
        dateTextViews[5] = view.findViewById(R.id.tv_saturday_date);
        dateTextViews[6] = view.findViewById(R.id.tv_sunday_date);
    }

    private void loadEventsForWeek() {
        executorService.execute(() -> {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);


            int diff = start.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if (diff < 0) diff += 7;
            start.add(Calendar.DAY_OF_MONTH, -diff);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 6);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 999);

            // 查询数据库中本周的事件
            List<Event> events = db.eventDao().getEventsByDateRange(
                    start.getTimeInMillis(),
                    end.getTimeInMillis()
            );

            // 更新UI
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.setEvents(events);
                });
            }
        });
    }



    /**
     * 更新周视图中的日期显示
     */
    private void updateWeekDates() {
        Calendar calendar = Calendar.getInstance();
        // 设置到本周的第一天（周一）
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        // 获取今天的年月日
        Calendar today = Calendar.getInstance();

        // 为每一天设置日期
        for (int i = 0; i < 7; i++) {
            String dateText = dateFormat.format(calendar.getTime());
            dateTextViews[i].setText(dateText);
            // 判断是否是今天
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                     dateTextViews[i].setTextColor(getResources().getColor(R.color.black));
                    dateTextViews[i].setTypeface(null, Typeface.BOLD);
            }
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
    }
}