package com.example.calendar.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.calendar.model.Subscription;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * // 功能：网络工具类，处理网络订阅相关的功能
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    
    /**
     * 检查网络连接状态
     * @param context 上下文
     * @return 是否有网络连接
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }
    
    /**
     * 下载订阅日历文件
     * @param subscription 订阅对象
     * @return 下载的内容，如果失败返回null
     */
    public static String downloadSubscriptionCalendar(Subscription subscription) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(subscription.getUrl())
                    .build();
            
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e(TAG, "下载失败，HTTP状态码: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "下载过程中发生异常", e);
            return null;
        }
    }
}