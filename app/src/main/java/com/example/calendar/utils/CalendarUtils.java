package com.example.calendar.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * // 功能：日历工具类，提供日期计算和处理功能
 */
public class CalendarUtils {
    
    /**
     * 获取指定月份的天数
     * @param year 年份
     * @param month 月份 (0-11)
     * @return 该月的天数
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取指定日期是星期几
     * @param year 年份
     * @param month 月份 (0-11)
     * @param day 日期
     * @return 星期几 (1-7, 1表示星期日)
     */
    public static int getDayOfWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    /**
     * 获取指定月份第一天是星期几
     * @param year 年份
     * @param month 月份 (0-11)
     * @return 星期几 (1-7, 1表示星期日)
     */
    public static int getFirstDayOfMonth(int year, int month) {
        return getDayOfWeek(year, month, 1);
    }
    
    /**
     * 获取指定日期的时间戳(毫秒)
     * @param year 年份
     * @param month 月份 (0-11)
     * @param day 日期
     * @return 时间戳
     */
    public static long getTimeInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * 从时间戳获取年份
     * @param timeInMillis 时间戳
     * @return 年份
     */
    public static int getYearFromTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.YEAR);
    }
    
    /**
     * 从时间戳获取月份
     * @param timeInMillis 时间戳
     * @return 月份 (0-11)
     */
    public static int getMonthFromTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.MONTH);
    }
    
    /**
     * 从时间戳获取日期
     * @param timeInMillis 时间戳
     * @return 日期
     */
    public static int getDayFromTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取指定月份的日期列表，包括上个月和下个月的填充日期
     * @param year 年份
     * @param month 月份 (0-11)
     * @return 日期列表
     */
    public static List<DateInfo> getMonthDateList(int year, int month) {
        List<DateInfo> dateList = new ArrayList<>();
        
        // 当月第一天是星期几
        int firstDayOfWeek = getFirstDayOfMonth(year, month);
        
        // 上个月的天数
        int prevMonth = month == 0 ? 11 : month - 1;
        int prevYear = month == 0 ? year - 1 : year;
        int daysInPrevMonth = getDaysInMonth(prevYear, prevMonth);
        
        // 当月天数
        int daysInMonth = getDaysInMonth(year, month);
        
        // 添加上个月的日期
        int prevDaysToShow = firstDayOfWeek - 1;
        for (int i = prevDaysToShow; i > 0; i--) {
            int day = daysInPrevMonth - i + 1;
            long timeInMillis = getTimeInMillis(prevYear, prevMonth, day);
            dateList.add(new DateInfo(prevYear, prevMonth, day, timeInMillis, true));
        }
        
        // 添加当月日期
        for (int day = 1; day <= daysInMonth; day++) {
            long timeInMillis = getTimeInMillis(year, month, day);
            dateList.add(new DateInfo(year, month, day, timeInMillis, false));
        }
        
        // 计算还需要多少天来填满6行7列
        int totalCells = 42; // 6行×7列
        int remainingCells = totalCells - dateList.size();
        
        // 添加下个月的日期
        int nextMonth = month == 11 ? 0 : month + 1;
        int nextYear = month == 11 ? year + 1 : year;
        for (int day = 1; day <= remainingCells; day++) {
            long timeInMillis = getTimeInMillis(nextYear, nextMonth, day);
            dateList.add(new DateInfo(nextYear, nextMonth, day, timeInMillis, true));
        }
        
        return dateList;
    }

    /**
     * 判断给定日期是否是今天
     */
    public  static boolean isToday(DateInfo dateInfo) {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH); // Calendar.MONTH 是 0-based
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        return dateInfo.getYear() == currentYear &&
                dateInfo.getMonth() == currentMonth &&
                dateInfo.getDay() == currentDay;
    }
    
    /**
     * 日期信息类
     */
    public static class DateInfo {
        private int year;
        private int month;
        private int day;
        private long timeInMillis;
        private boolean isOtherMonth; // 是否为非当前月的日期
        
        public DateInfo(int year, int month, int day, long timeInMillis, boolean isOtherMonth) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.timeInMillis = timeInMillis;
            this.isOtherMonth = isOtherMonth;
        }
        
        // Getter方法
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getDay() { return day; }
        public long getTimeInMillis() { return timeInMillis; }
        public boolean isOtherMonth() { return isOtherMonth; }
        
        @Override
        public String toString() {
            return year + "-" + (month + 1) + "-" + day;
        }
    }
}