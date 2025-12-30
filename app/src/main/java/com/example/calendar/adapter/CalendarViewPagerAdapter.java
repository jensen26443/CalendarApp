package com.example.calendar.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.calendar.ui.fragment.DayViewFragment;
import com.example.calendar.ui.fragment.MonthViewFragment;
import com.example.calendar.ui.fragment.WeekViewFragment;

import java.util.Calendar;

/**
 * // 功能：日历视图Pager适配配器，管理月/周/日视图的切换
 */
public class CalendarViewPagerAdapter extends FragmentStateAdapter {
    
    private int startYear;
    private int startMonth;
    public static final int VIEW_TYPE_MONTH = 0;
    public static final int VIEW_TYPE_WEEK = 1;
    public static final int VIEW_TYPE_DAY = 2;
    
    private int currentViewType = VIEW_TYPE_MONTH; // 默认月视图
    
    public CalendarViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int startYear, int startMonth) {
        super(fragmentActivity);
        this.startYear = startYear;
        this.startMonth = startMonth;
    }
    
    public void setViewType(int viewType) {
        this.currentViewType = viewType;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (currentViewType) {
            case VIEW_TYPE_WEEK:
                return WeekViewFragment.newInstance();
            case VIEW_TYPE_DAY:
                return DayViewFragment.newInstance();
            case VIEW_TYPE_MONTH:
            default:
                // 计算position对应的年月
                Calendar calendar = Calendar.getInstance();
                calendar.set(startYear, startMonth, 1);
                calendar.add(Calendar.MONTH, position - 100); // 居中显示当前月份
                
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                
                return MonthViewFragment.newInstance(year, month);
        }
    }
    
    @Override
    public int getItemCount() {
        // 统一返回足够大的数值，确保各种视图都能正常工作
        return 201;
    }
    
    /**
     * 获取指定位置的年月信息
     * @param position 位置
     * @return 包含年月信息的数组，[0]为年，[1]为月
     */
    public int[] getDateInfo(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(startYear, startMonth, 1);
        calendar.add(Calendar.MONTH, position - 100); // 居中显示当前月份
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        
        return new int[]{year, month};
    }
    
    /**
     * 获取当前视图类型
     * @return 当前视图类型
     */
    public int getCurrentViewType() {
        return currentViewType;
    }
}