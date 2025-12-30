package com.example.calendar.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

/**
 * // 功能：订阅实体类，用于管理网络日历订阅
 */
@Entity(tableName = "subscriptions")
public class Subscription {
    @PrimaryKey(autoGenerate = true)
    private long id; // 主键
    
    private String name; // 订阅名称
    private String url; // 订阅地址
    private long updateInterval; // 更新频率（毫秒）
    private long lastUpdateTime; // 最后更新时间
    private boolean isEnabled = true; // 是否启用

    // 构造方法
    public Subscription() {}

    @Ignore
    public Subscription(String name, String url, long updateInterval) {
        this.name = name;
        this.url = url;
        this.updateInterval = updateInterval;
    }

    // Getter和Setter方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}