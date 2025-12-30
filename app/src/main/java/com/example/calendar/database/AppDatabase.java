package com.example.calendar.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.calendar.model.Event;
import com.example.calendar.model.Reminder;
import com.example.calendar.model.Subscription;

/**
 * // 功能：应用数据库类，管理所有数据表
 */
@Database(entities = {Event.class, Reminder.class, Subscription.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract EventDao eventDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "calendar_database")
                            .fallbackToDestructiveMigration() // 在版本升级时丢弃旧数据
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}