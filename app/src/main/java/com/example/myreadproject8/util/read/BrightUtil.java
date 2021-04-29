package com.example.myreadproject8.util.read;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * created by ycq on 2021/4/18 0018
 * describe：亮度工具
 */
public class BrightUtil {
    /**
     * description: 获取屏幕亮度
     */
    public static int getScreenBrightness(AppCompatActivity activity){
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try{
            nowBrightnessValue = Settings.System.getInt(
                    resolver,Settings.System.SCREEN_BRIGHTNESS
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * description: 设置亮度
     */
    public static void setBrightness(AppCompatActivity activity,int brightness){
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness * (1f / 255f));
        activity.getWindow().setAttributes(lp);
    }

    /**
     * description: 亮度跟随系统
     */
    public static void followSystemBright(AppCompatActivity activity){
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(lp);
    }
}
