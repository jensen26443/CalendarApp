package com.example.calendar.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.util.List;

/**
 * // 功能：日程事件实体类，对应RFC5545标准中的VEVENT
 */
@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    private long id; // 主键
    
    private String title; // 标题（SUMMARY）
    private String description; // 描述（DESCRIPTION）
    private String location; // 地点（LOCATION）
    private long startTime; // 开始时间（毫秒，对应DTSTART）
    private long endTime; // 结束时间（毫秒，对应DTEND）
    private int type; // 类型（0=工作，1=生活，2=节日，3=订阅，自定义枚举）
    private String rrule; // 重复规则（RRULE，如"FREQ=WEEKLY;BYDAY=MO,TU"）
    private boolean isLunar; // 是否农历日期
    private String lunarDate; // 农历日期（如"正月十五"，仅isLunar=true时有效）
    private long subscriptionId; // 订阅ID（如果是从订阅导入的事件）

    // 构造方法
    public Event() {}

    // 带参数的构造方法
    @Ignore
    public Event(String title, String description, String location, long startTime, long endTime, int type) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    // Getter和Setter方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = rrule;
    }

    public boolean isLunar() {
        return isLunar;
    }

    public void setLunar(boolean lunar) {
        isLunar = lunar;
    }

    public String getLunarDate() {
        return lunarDate;
    }

    public void setLunarDate(String lunarDate) {
        this.lunarDate = lunarDate;
    }
    
    public long getSubscriptionId() {
        return subscriptionId;
    }
    
    public void setSubscriptionId(long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}