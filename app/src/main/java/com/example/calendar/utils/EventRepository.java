package com.example.calendar.utils;

import android.content.Context;

import com.example.calendar.database.AppDatabase;
import com.example.calendar.database.EventDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {

    private final EventDao eventDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        eventDao = db.eventDao();
    }

    // 清空订阅事件
    public void clearSubscribedEvents() {
        executor.execute(() -> eventDao.deleteAllSubscribedEvents());

    }
}
