package com.example.calendar.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.calendar.model.Event;
import com.example.calendar.model.Reminder;

import java.util.List;

/**
 * // 功能：日程事件数据访问对象，提供对Event表的增删改查操作
 */
@Dao
public interface EventDao {
    
    // 插入单个事件
    @Insert
    long insertEvent(Event event);
    
    // 更新事件
    @Update
    void updateEvent(Event event);
    
    // 删除事件
    @Delete
    void deleteEvent(Event event);
    
    // 获取所有事件
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    List<Event> getAllEvents();
    
    // 根据ID获取事件
    @Query("SELECT * FROM events WHERE id = :id")
    Event getEventById(long id);
    
    // 根据日期范围获取事件
    @Query("SELECT * FROM events WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime ASC")
    List<Event> getEventsByDateRange(long startTime, long endTime);
    
    // 根据标题和时间获取事件（用于检测重复）
    @Query("SELECT * FROM events WHERE title = :title AND startTime = :startTime")
    List<Event> getEventsByTitleAndTime(String title, long startTime);
    
    // 根据类型获取事件
    @Query("SELECT * FROM events WHERE type = :type ORDER BY startTime ASC")
    List<Event> getEventsByType(int type);
    
    // 插入提醒
    @Insert
    long insertReminder(Reminder reminder);
    
    // 更新提醒
    @Update
    void updateReminder(Reminder reminder);
    
    // 删除提醒
    @Delete
    void deleteReminder(Reminder reminder);
    
    // 根据事件ID获取所有提醒
    @Query("SELECT * FROM reminders WHERE eventId = :eventId")
    List<Reminder> getRemindersByEventId(long eventId);
    
    // 根据ID获取提醒
    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(long id);
    
    // 删除事件的所有提醒
    @Query("DELETE FROM reminders WHERE eventId = :eventId")
    void deleteRemindersByEventId(long eventId);
    
    // 插入订阅
    @Insert
    long insertSubscription(com.example.calendar.model.Subscription subscription);
    
    // 更新订阅
    @Update
    void updateSubscription(com.example.calendar.model.Subscription subscription);
    
    // 删除订阅
    @Delete
    void deleteSubscription(com.example.calendar.model.Subscription subscription);
    
    // 获取所有订阅
    @Query("SELECT * FROM subscriptions")
    List<com.example.calendar.model.Subscription> getAllSubscriptions();
    
    // 根据ID获取订阅
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    com.example.calendar.model.Subscription getSubscriptionById(long id);
    
    // 获取启用的订阅
    @Query("SELECT * FROM subscriptions WHERE isEnabled = 1")
    List<com.example.calendar.model.Subscription> getEnabledSubscriptions();


    // 删除所有订阅事件
    @Query("DELETE FROM events WHERE type = 3")
    void deleteAllSubscribedEvents();

    // 查询订阅事件数量
    @Query("SELECT COUNT(*) FROM events WHERE type = 3")
    int countSubscribedEvents();
}