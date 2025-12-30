package com.example.calendar.utils;

import com.example.calendar.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * // 功能：重复规则工具类，处理重复日程的相关计算
 */
public class RecurrenceUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    
    /**
     * 计算重复事件在未来30天内的所有实例
     * @param event 原始事件
     * @param days 未来天数
     * @return 重复事件实例列表
     */
    public static List<Event> getRecurrenceInstances(Event event, int days) {
        List<Event> instances = new ArrayList<>();
        
        if (event.getRrule() == null || event.getRrule().isEmpty()) {
            instances.add(event);
            return instances;
        }
        
        // 解析重复规则
        String rrule = event.getRrule();
        if (rrule.contains("FREQ=DAILY")) {
            return getDailyRecurrences(event, days);
        } else if (rrule.contains("FREQ=WEEKLY")) {
            return getWeeklyRecurrences(event, days);
        } else if (rrule.contains("FREQ=MONTHLY")) {
            return getMonthlyRecurrences(event, days);
        } else if (rrule.contains("FREQ=YEARLY")) {
            return getYearlyRecurrences(event, days);
        }
        
        // 如果无法识别规则，只返回原始事件
        instances.add(event);
        return instances;
    }
    
    /**
     * 获取每日重复事件实例
     * @param event 原始事件
     * @param days 未来天数
     * @return 重复事件实例列表
     */
    private static List<Event> getDailyRecurrences(Event event, int days) {
        List<Event> instances = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        
        long duration = event.getEndTime() - event.getStartTime();
        
        for (int i = 0; i < days; i++) {
            Event instance = cloneEvent(event);
            instance.setStartTime(calendar.getTimeInMillis());
            instance.setEndTime(calendar.getTimeInMillis() + duration);
            instances.add(instance);
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return instances;
    }
    
    /**
     * 获取每周重复事件实例
     * @param event 原始事件
     * @param days 未来天数
     * @return 重复事件实例列表
     */
    private static List<Event> getWeeklyRecurrences(Event event, int days) {
        List<Event> instances = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        
        long duration = event.getEndTime() - event.getStartTime();
        
        for (int i = 0; i < days; i += 7) {
            Event instance = cloneEvent(event);
            instance.setStartTime(calendar.getTimeInMillis());
            instance.setEndTime(calendar.getTimeInMillis() + duration);
            instances.add(instance);
            
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        
        return instances;
    }
    
    /**
     * 获取每月重复事件实例
     * @param event 原始事件
     * @param days 未来天数
     * @return 重复事件实例列表
     */
    private static List<Event> getMonthlyRecurrences(Event event, int days) {
        List<Event> instances = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        
        long duration = event.getEndTime() - event.getStartTime();
        
        // 计算大约几个月
        int months = days / 30;
        
        for (int i = 0; i <= months; i++) {
            Event instance = cloneEvent(event);
            instance.setStartTime(calendar.getTimeInMillis());
            instance.setEndTime(calendar.getTimeInMillis() + duration);
            instances.add(instance);
            
            calendar.add(Calendar.MONTH, 1);
        }
        
        return instances;
    }
    
    /**
     * 获取每年重复事件实例
     * @param event 原始事件
     * @param days 未来天数
     * @return 重复事件实例列表
     */
    private static List<Event> getYearlyRecurrences(Event event, int days) {
        List<Event> instances = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        
        long duration = event.getEndTime() - event.getStartTime();
        
        // 计算大约几年
        int years = days / 365;
        
        for (int i = 0; i <= years; i++) {
            Event instance = cloneEvent(event);
            instance.setStartTime(calendar.getTimeInMillis());
            instance.setEndTime(calendar.getTimeInMillis() + duration);
            instances.add(instance);
            
            calendar.add(Calendar.YEAR, 1);
        }
        
        return instances;
    }
    
    /**
     * 克隆事件对象
     * @param event 原始事件
     * @return 克隆的事件
     */
    private static Event cloneEvent(Event event) {
        Event cloned = new Event();
        cloned.setTitle(event.getTitle());
        cloned.setDescription(event.getDescription());
        cloned.setLocation(event.getLocation());
        cloned.setType(event.getType());
        cloned.setRrule(event.getRrule());
        cloned.setLunar(event.isLunar());
        cloned.setLunarDate(event.getLunarDate());
        return cloned;
    }
}