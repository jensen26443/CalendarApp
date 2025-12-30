package com.example.calendar.ui.fragment;

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
import com.example.calendar.adapter.DayViewAdapter;
import com.example.calendar.database.AppDatabase;
import com.example.calendar.database.EventDao;
import com.example.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // 功能：日视图Fragment，显示一天的日程安排
 */
public class DayViewFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private DayViewAdapter adapter;
    private TextView tvDate;

    private ExecutorService executorService;

    private AppDatabase db;
    
    public static DayViewFragment newInstance() {
        DayViewFragment fragment = new DayViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_day_view, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEventsForToday();
    }

    private void loadEventsForToday() {
        executorService.execute(() -> {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 999);

            List<Event> events = db.eventDao().getEventsByDateRange(
                    start.getTimeInMillis(),
                    end.getTimeInMillis()
            );

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.setEvents(events);
                });
            }
        });
    }




    private void initViews(View view) {
        tvDate = view.findViewById(R.id.tv_date);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 设置当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
        
        // 设置适配器
        adapter = new DayViewAdapter();
        recyclerView.setAdapter(adapter);
    }
}