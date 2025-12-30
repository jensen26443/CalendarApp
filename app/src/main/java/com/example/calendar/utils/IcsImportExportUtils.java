package com.example.calendar.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.calendar.model.Event;
import com.example.calendar.model.Reminder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.calendar.model.Event;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * // 功能：ICS文件导入导出工具类
 */
public class IcsImportExportUtils {
    private static final String TAG = "ICS_UTIL";
    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault());
    private static final SimpleDateFormat LOCAL_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());


    static {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }



    /**
     * 从 URI 读取 ICS 文件内容
     */
    public static String readIcsFile(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "读取ICS文件失败", e);
            return null;
        }
    }

    /**
     * 解析 ICS 内容为 Event 列表
     */
    public static List<Event> parseIcsContent(String icsContent) {
        List<Event> events = new ArrayList<>();

        if (icsContent == null || icsContent.isEmpty()) {
            return events;
        }

        String[] lines = icsContent.split("\n");
        Event currentEvent = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.equals("BEGIN:VEVENT")) {
                currentEvent = new Event();
            }

            else if (line.equals("END:VEVENT")) {
                if (currentEvent != null && currentEvent.getStartTime() > 0) {

                    // 全天事件兜底：没有 DTEND 就 +1 天
                    if (currentEvent.getEndTime() == 0) {
                        currentEvent.setEndTime(
                                currentEvent.getStartTime() + 24 * 60 * 60 * 1000L
                        );
                    }

                    events.add(currentEvent);
                }
                currentEvent = null;
            }

            else if (currentEvent != null) {

                // 标题（支持 SUMMARY;LANGUAGE=xx）
                if (line.startsWith("SUMMARY")) {
                    currentEvent.setTitle(unescapeText(getIcsValue(line)));
                }

                // 描述
                else if (line.startsWith("DESCRIPTION")) {
                    currentEvent.setDescription(unescapeText(getIcsValue(line)));
                }

                // 地点
                else if (line.startsWith("LOCATION")) {
                    currentEvent.setLocation(unescapeText(getIcsValue(line)));
                }

                // 开始时间（支持 VALUE=DATE / UTC / 本地）
                else if (line.startsWith("DTSTART")) {
                    long start = parseIcsDateTime(line);
                    currentEvent.setStartTime(start);
                }

                // 结束时间
                else if (line.startsWith("DTEND")) {
                    long end = parseIcsDateTime(line);
                    currentEvent.setEndTime(end);
                }

                // RRULE（可选）
                else if (line.startsWith("RRULE")) {
                    currentEvent.setRrule(getIcsValue(line));
                }
            }
        }

        Log.d(TAG, "解析完成，共解析事件数: " + events.size());
        return events;
    }

    /**
     * 解析 ICS 时间字段
     * 支持：
     * - DTSTART;VALUE=DATE:20250101
     * - DTSTART:20251229T080000Z
     * - DTSTART:20251229T080000
     */
    private static long parseIcsDateTime(String line) {
        try {
            String value = getIcsValue(line);

            // 全天事件（DATE）
            if (line.contains("VALUE=DATE")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                sdf.setTimeZone(TimeZone.getDefault());
                Date date = sdf.parse(value);
                return date != null ? date.getTime() : 0;
            }

            // UTC 时间（Z）
            if (value.endsWith("Z")) {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyyMMdd'T'HHmmss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(value);
                return date != null ? date.getTime() : 0;
            }

            // 本地时间
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyyMMdd'T'HHmmss", Locale.US);
            sdf.setTimeZone(TimeZone.getDefault());
            Date date = sdf.parse(value);
            return date != null ? date.getTime() : 0;

        } catch (Exception e) {
            Log.e(TAG, "解析ICS时间失败: " + line, e);
            return 0;
        }
    }

    /**
     * 获取 ICS 行中冒号后的值
     * 例如：SUMMARY;LANGUAGE=en-us:China: New Year's Day
     */
    private static String getIcsValue(String line) {
        int index = line.indexOf(':');
        if (index == -1) return "";
        return line.substring(index + 1).trim();
    }

    /**
     * 反转义 ICS 文本
     */
    private static String unescapeText(String text) {
        if (text == null) return null;
        return text
                .replace("\\n", "\n")
                .replace("\\,", ",")
                .replace("\\;", ";")
                .replace("\\\\", "\\");
    }

    /*
    *//**
     * 从URI读取ICS文件内容
     * @param context 上下文
     * @param uri 文件URI
     * @return ICS文件内容
     *//*
    public static String readIcsFile(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "读取ICS文件失败", e);
            return null;
        }
    }
    
    *//**
     * 解析ICS内容
     * @param icsContent ICS文件内容
     * @return 事件列表
     *//*
    public static List<Event> parseIcsContent(String icsContent) {
        List<Event> events = new ArrayList<>();
        
        if (icsContent == null || icsContent.isEmpty()) {
            return events;
        }
        
        // 解析ICS内容
        String[] lines = icsContent.split("\n");
        Event currentEvent = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("BEGIN:VEVENT")) {
                currentEvent = new Event();
            } else if (line.startsWith("END:VEVENT")) {
                if (currentEvent != null) {
                    events.add(currentEvent);
                    currentEvent = null;
                }
            } else if (currentEvent != null) {
                if (line.startsWith("SUMMARY:")) {
                    currentEvent.setTitle(unescapeText(line.substring(8)));
                } else if (line.startsWith("DESCRIPTION:")) {
                    currentEvent.setDescription(unescapeText(line.substring(12)));
                } else if (line.startsWith("LOCATION:")) {
                    currentEvent.setLocation(unescapeText(line.substring(9)));
                } else if (line.startsWith("DTSTART:")) {
                    try {
                        String dateStr = line.substring(8);
                        if (dateStr.endsWith("Z")) {
                            currentEvent.setStartTime(UTC_FORMAT.parse(dateStr).getTime());
                        } else {
                            currentEvent.setStartTime(LOCAL_FORMAT.parse(dateStr).getTime());
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "解析开始时间失败", e);
                    }
                } else if (line.startsWith("DTEND:")) {
                    try {
                        String dateStr = line.substring(6);
                        if (dateStr.endsWith("Z")) {
                            currentEvent.setEndTime(UTC_FORMAT.parse(dateStr).getTime());
                        } else {
                            currentEvent.setEndTime(LOCAL_FORMAT.parse(dateStr).getTime());
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "解析结束时间失败", e);
                    }
                } else if (line.startsWith("RRULE:")) {
                    currentEvent.setRrule(line.substring(6));
                }
            }
        }
        
        return events;
    }
    */


    /**
     * 生成ICS内容
     * @param events 事件列表
     * @return ICS文件内容
     */
    public static String generateIcsContent(List<Event> events) {
        StringBuilder ics = new StringBuilder();
        
        // ICS文件头
        ics.append("BEGIN:VCALENDAR\n");
        ics.append("VERSION:2.0\n");
        ics.append("PRODID:-//MyCalendarApp//StudentHomework//CN\n");
        
        // 添加每个事件
        for (Event event : events) {
            ics.append(generateEventIcs(event));
        }
        
        // ICS文件尾
        ics.append("END:VCALENDAR\n");
        
        return ics.toString();
    }
    
    /**
     * 生成单个事件的ICS内容
     * @param event 事件对象
     * @return 事件的ICS内容
     */
    private static String generateEventIcs(Event event) {
        StringBuilder ics = new StringBuilder();
        
        ics.append("BEGIN:VEVENT\n");
        ics.append("UID:").append(event.getId()).append("@mycalendarapp\n");
        if (event.getTitle() != null) {
            ics.append("SUMMARY:").append(escapeText(event.getTitle())).append("\n");
        }
        if (event.getDescription() != null) {
            ics.append("DESCRIPTION:").append(escapeText(event.getDescription())).append("\n");
        }
        if (event.getLocation() != null) {
            ics.append("LOCATION:").append(escapeText(event.getLocation())).append("\n");
        }
        
        // 时间格式化
        if (event.getStartTime() > 0) {
            ics.append("DTSTART:").append(UTC_FORMAT.format(new Date(event.getStartTime()))).append("\n");
        }
        if (event.getEndTime() > 0) {
            ics.append("DTEND:").append(UTC_FORMAT.format(new Date(event.getEndTime()))).append("\n");
        }
        
        // 重复规则
        if (event.getRrule() != null && !event.getRrule().isEmpty()) {
            ics.append("RRULE:").append(event.getRrule()).append("\n");
        }
        
        ics.append("END:VEVENT\n");
        
        return ics.toString();
    }
    
    /**
     * 转义文本中的特殊字符
     * @param text 原始文本
     * @return 转义后的文本
     */
    private static String escapeText(String text) {
        if (text == null) return "";
        
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }
    
    /**
     * 反转义文本中的特殊字符
     * @param text 转义后的文本
     * @return 原始文本
     */
//    private static String unescapeText(String text) {
//        if (text == null) return "";
//
//        return text.replace("\\n", "\n")
//                .replace("\\,", ",")
//                .replace("\\;", ";")
//                .replace("\\\\", "\\");
//    }
}