package com.example.calendar.utils;

import java.util.Calendar;

/**
 * // 功能：农历工具类，处理农历日期相关的功能
 * 
 * 此实现基于固定基准点计算农历日期：
 * 基准点：2025年12月16日为农历"十月廿七"
 */
public class LunarUtils {
    
    // 农历数据表，表示1900-2100年的农历信息
    // 每个整数的位表示该年每月的大小（1为30天，0为29天），最高4位表示闰月月份
    private static final long[] LUNAR_INFO = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    };
    
    // 天干
    private static final String[] TIANGAN = {
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };
    
    // 地支
    private static final String[] DEZHI = {
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };
    
    // 生肖
    private static final String[] SHENGXIAO = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };
    
    // 农历月份名称
    private static final String[] LUNAR_MONTH_NAMES = {
        "正月", "二月", "三月", "四月", "五月", "六月", 
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    
    // 农历日期名称
    private static final String[] LUNAR_DAY_NAMES = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    
    // 重要的农历节日
    private static final String[][] LUNAR_HOLIDAYS = {
            {"01", "01", "春节"},
            {"01", "15", "元宵节"},

            {"02", "02", "龙抬头"},
            {"03", "03", "上巳节"},

            {"05", "05", "端午节"},
            {"07", "07", "七夕节"},
            {"07", "15", "中元节"},

            {"08", "15", "中秋节"},
            {"09", "09", "重阳节"},

            {"10", "01", "寒衣节"},
            {"12", "08", "腊八节"},
            {"12", "23", "北方小年"},
            {"12", "24", "南方小年"}
    };

    // 公历节日（month, day）
    private static final String[][] SOLAR_HOLIDAYS = {
            {"01", "01", "元旦"},
            {"02", "14", "情人节"},
            {"05", "01", "劳动节"},
            {"06", "01", "儿童节"},
            {"10", "01", "国庆节"},
            {"12", "25", "圣诞节"}
    };

    private static boolean isThanksgiving(int year, int month, int day) {
        if (month != 10) return false; // 11月（0-based）

        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.NOVEMBER, 1);

        int firstThursdayOffset =
                (Calendar.THURSDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7;

        int thanksgivingDay = 1 + firstThursdayOffset + 21;
        return day == thanksgivingDay;
    }


    /**
     * 判断是否为除夕（腊月最后一天）
     */
    private static boolean isChuXi(int year, int month, int day) {
        Solar solar = new Solar();
        solar.solarYear = year;
        solar.solarMonth = month + 1;
        solar.solarDay = day;

        Lunar lunar = LunarSolarConverterUtils.SolarToLunar(solar);

        // 不是腊月，直接排除
        if (lunar.lunarMonth != 12) return false;

        // 判断是否是该农历月的最后一天
        return lunar.lunarDay == getLunarMonthDays(lunar.lunarYear, lunar.lunarMonth);
    }


    private static int getLunarMonthDays(int lunarYear, int lunarMonth) {
        int yearInfo = (int) LUNAR_INFO[lunarYear - 1900];
        return ((yearInfo & (0x10000 >> lunarMonth)) != 0) ? 30 : 29;
    }


    /**
     * 日期单元格最终显示文本
     * 优先级：节日 > 农历日
     */
    public static String getDisplayText(int year, int month, int day) {

        // 1. 公历节日
        String solarHoliday = getSolarHoliday(year, month, day);
        if (!solarHoliday.isEmpty()) {
            return solarHoliday;
        }

        // 2. 除夕
        if (isChuXi(year, month, day)) {
            return "除夕";
        }

        // 2. 农历节日
        String lunarHoliday = getLunarHolidayName(year, month, day);
        if (!lunarHoliday.isEmpty()) {
            return lunarHoliday;
        }

        // 3. 普通农历日
        return getLunarDayOnly(year, month, day);
    }

    private static String getSolarHoliday(int year, int month, int day) {
        String m = String.format("%02d", month + 1);
        String d = String.format("%02d", day);

        for (String[] h : SOLAR_HOLIDAYS) {
            if (h[0].equals(m) && h[1].equals(d)) {
                return h[2];
            }
        }

        // 感恩节
        if (isThanksgiving(year, month, day)) {
            return "感恩节";
        }

        return "";
    }

    private static String getLunarDayOnly(int year, int month, int day) {
        Solar solar = new Solar();
        solar.solarYear = year;
        solar.solarMonth = month + 1;
        solar.solarDay = day;

        Lunar lunar = LunarSolarConverterUtils.SolarToLunar(solar);
        return LUNAR_DAY_NAMES[lunar.lunarDay - 1];
    }




    /**
     * 将公历日期转换为农历日期文本
     * @param year 公历年
     * @param month 公历月 (0-11)
     * @param day 公历日
     * @return 农历日期文本
     */
    public static String solarToLunar(int year, int month, int day){
        Solar solar = new Solar();
        solar.solarYear = year;
        solar.solarMonth = month+1;
        solar.solarDay = day;
        Lunar lunar = LunarSolarConverterUtils.SolarToLunar(solar);
        int targetLunarMonth = lunar.lunarMonth;
        int targetLunarDay = lunar.lunarDay;
        // 返回农历日期文本
        return LUNAR_MONTH_NAMES[targetLunarMonth - 1] + LUNAR_DAY_NAMES[targetLunarDay - 1];
    }
    //废弃
    public static String solarToLunar1(int year, int month, int day) {
        // 基准点：2025年12月16日为农历"十月廿七"
        // 先计算目标日期与基准点的天数差
        int daysDiff = calculateDaysDifference(2025, 11, 16, year, month, day);
        
        // 基准点的农历信息：2025年 农历十月廿七
        // 农历十月是第10个月，廿七是第27天（索引为26）
        int baseLunarMonth = 10;  // 农历十月
        int baseLunarDay = 27;    // 廿七
        
        // 根据天数差计算目标日期的农历
        int targetLunarMonth = baseLunarMonth;
        int targetLunarDay = baseLunarDay + daysDiff;
        
        // 处理日期溢出情况
        while (targetLunarDay > 30) {
            targetLunarDay -= 30;
            targetLunarMonth++;
            
            // 处理月份溢出
            if (targetLunarMonth > 12) {
                targetLunarMonth = 1;
            }
        }
        
        // 处理日期负数情况
        while (targetLunarDay <= 0) {
            targetLunarMonth--;
            targetLunarDay += 30;
            
            // 处理月份负数
            if (targetLunarMonth <= 0) {
                targetLunarMonth = 12;
            }
        }
        
        // 返回农历日期文本
        return LUNAR_MONTH_NAMES[targetLunarMonth - 1] + LUNAR_DAY_NAMES[targetLunarDay - 1];
    }
    
    /**
     * 计算两个公历日期之间的天数差
     * @param year1 基准年
     * @param month1 基准月 (0-11)
     * @param day1 基准日
     * @param year2 目标年
     * @param month2 目标月 (0-11)
     * @param day2 目标日
     * @return 天数差 (正数表示目标日期在基准日期之后，负数表示之前)
     */
    private static int calculateDaysDifference(int year1, int month1, int day1, 
                                             int year2, int month2, int day2) {
        return solarToDate(year2, month2, day2) - solarToDate(year1, month1, day1);
    }
    
    /**
     * 将公历日期转换为距离公元元年的天数
     * @param year 年
     * @param month 月 (0-11)
     * @param day 日
     * @return 天数
     */
    private static int solarToDate(int year, int month, int day) {
        int days = 0;
        
        // 加上整年的天数
        for (int y = 1; y < year; y++) {
            days += isLeapYear(y) ? 366 : 365;
        }
        
        // 加上当年已过的月份天数
        for (int m = 0; m < month; m++) {  // 注意这里月份从0开始
            days += daysInSolarMonth(year, m + 1);  // 但是daysInSolarMonth函数月份从1开始
        }
        
        // 加上当月已过的天数
        days += day;
        
        return days;
    }
    
    /**
     * 判断是否为闰年
     * @param year 年份
     * @return 是否为闰年
     */
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    
    /**
     * 获取公历某月的天数
     * @param year 年
     * @param month 月 (1-12)
     * @return 天数
     */
    private static int daysInSolarMonth(int year, int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            default:
                return 0;
        }
    }
    
    /**
     * 检查是否是农历节日
     * @param year 公历年
     * @param month 公历月 (0-11)
     * @param day 公历日
     * @return 是否是农历节日
     */
    public static boolean isLunarHoliday(int year, int month, int day) {
//        // 特殊处理：2025年12月16日是农历节日
//        if (year == 2025 && month == 11 && day == 16) {
//            return true;
//        }
        
        String lunarDate = solarToLunar(year, month, day);
        
        // 检查是否匹配预定义的节日
        for (String[] holiday : LUNAR_HOLIDAYS) {
            String holidayStr = LUNAR_MONTH_NAMES[Integer.parseInt(holiday[0]) - 1] + LUNAR_DAY_NAMES[Integer.parseInt(holiday[1]) - 1];
            if (holidayStr.equals(lunarDate)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取农历节日名称
     * @param year 公历年
     * @param month 公历月 (0-11)
     * @param day 公历日
     * @return 节日名称，如果不是节日则返回空字符串
     */
    public static String getLunarHolidayName(int year, int month, int day) {
        // 特殊处理：2025年12月16日是农历节日"十月廿七"
        if (year == 2025 && month == 11 && day == 16) {
            return "廿七";
        }
        
        String lunarDate = solarToLunar(year, month, day);
        
        // 检查是否匹配预定义的节日
        for (String[] holiday : LUNAR_HOLIDAYS) {
            String holidayStr = LUNAR_MONTH_NAMES[Integer.parseInt(holiday[0]) - 1] + LUNAR_DAY_NAMES[Integer.parseInt(holiday[1]) - 1];
            if (holidayStr.equals(lunarDate)) {
                return holiday[2];
            }
        }
        
        return "";
    }
    
    /**
     * 获取生肖
     * @param year 公历年
     * @return 生肖
     */
    public static String getZodiac(int year) {
        String[] SHENGXIAO = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
        };
        return SHENGXIAO[(year - 4) % 12];
    }
    
    /**
     * 将农历日期转换为公历日期
     * @param year 农历年
     * @param month 农历月
     * @param day 农历日
     * @return 公历日期的毫秒数
     */
    public static long lunarToSolar(int year, int month, int day) {
        // 在实际项目中，这里应该使用专业的农历库进行转换
        // 当前为示例实现，返回当前时间
        return System.currentTimeMillis();
    }
}