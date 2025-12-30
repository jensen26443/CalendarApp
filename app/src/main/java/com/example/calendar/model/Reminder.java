package com.example.calendar.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

/**
 * // 功能：提醒实体类，对应RFC5545标准中的VALARM
 */
@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private long id; // 主键
    
    private long eventId; // 关联日程ID
    private int minutes; // 提前提醒分钟数（如15、60）

    // 构造方法
    public Reminder() {}

    @Ignore
    public Reminder(long eventId, int minutes) {
        this.eventId = eventId;
        this.minutes = minutes;
    }

    // Getter和Setter方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}