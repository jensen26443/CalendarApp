package com.example.calendar.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.calendar.model.Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件导出工具类
 */
public class FileExportUtils {
    private static final String TAG = "FileExportUtils";
    
    /**
     * 导出事件为ICS文件
     * @param context 上下文
     * @param events 事件列表
     * @param fileName 文件名
     */
    public static void exportEventsToICS(Context context, List<Event> events, String fileName) {
        try {
            String icsContent = IcsImportExportUtils.generateIcsContent(events);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上版本使用分区存储
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/calendar");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Calendar");
                
                Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(icsContent.getBytes());
                        outputStream.close();
                        Toast.makeText(context, "导出成功: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Android 10以下版本直接写入文件
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Calendar");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                
                File file = new File(directory, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(icsContent.getBytes());
                fos.close();
                
                Toast.makeText(context, "导出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "导出ICS文件失败", e);
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}