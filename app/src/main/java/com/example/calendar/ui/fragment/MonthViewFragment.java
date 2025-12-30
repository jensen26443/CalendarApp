package com.example.calendar.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.R;
import com.example.calendar.adapter.DateAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.model.Event;
import com.example.calendar.utils.CalendarUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MonthViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private DateAdapter dateAdapter;
    private TextView tvMonthYear;

    private int year;
    private int month;

    private ExecutorService executorService;
    private AppDatabase db;

    private List<CalendarUtils.DateInfo> dateList;

    public static MonthViewFragment newInstance(int year, int month) {
        MonthViewFragment fragment = new MonthViewFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
        }

        executorService = Executors.newSingleThreadExecutor();
        db = AppDatabase.getDatabase(requireContext());

        //  日期只算一次
        dateList = CalendarUtils.getMonthDateList(year, month);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_month_view, container, false);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));

        tvMonthYear.setText(String.format("%d年%d月", year, month + 1));

        dateAdapter = new DateAdapter(requireContext(), dateList);
        recyclerView.setAdapter(dateAdapter);

        loadEventsForMonth();
        return view;
    }



    /**
     * 刷新事件
     */
    public void refreshEvents() {
        loadEventsForMonth();
    }

    private void loadEventsForMonth() {
        executorService.execute(() -> {
            Calendar start = Calendar.getInstance();
            start.set(year, month, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(year, month,
                    CalendarUtils.getDaysInMonth(year, month),
                    23, 59, 59);
            end.set(Calendar.MILLISECOND, 999);

            List<Event> events = db.eventDao().getEventsByDateRange(
                    start.getTimeInMillis(),
                    end.getTimeInMillis()
            );

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    // 只更新事件
                    dateAdapter.setEvents(events);
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
