package com.example.myreadproject8.util.toast;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;

import es.dmoral.toasty.Toasty;

/**
 * Toast工具类：对Toasty的二次封装
 * https://github.com/GrenderG/Toasty
 */
public class ToastUtils {

    static {
        Toasty.Config.getInstance().setTextSize(14).apply();
    }

    public static void show(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getCurApplication(), msg,
                ContextCompat.getDrawable(App.getCurApplication(), R.drawable.ic_mine_default),
                App.getCurApplication().getResources().getColor(R.color.toast_default),
                App.getCurApplication().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //红色
    public static void showError( String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getCurApplication(), msg,
                ContextCompat.getDrawable(App.getCurApplication(), R.drawable.ic_error_default),
                App.getCurApplication().getResources().getColor(R.color.toast_red),
                App.getCurApplication().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //绿色
    public static void showSuccess( String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getCurApplication(), msg,
                ContextCompat.getDrawable(App.getCurApplication(), R.drawable.ic_success_default),
                App.getCurApplication().getResources().getColor(R.color.toast_green),
                App.getCurApplication().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //蓝色
    public static void showInfo( String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getCurApplication(), msg,
                ContextCompat.getDrawable(App.getCurApplication(), R.drawable.ic_mine_default),
                App.getCurApplication().getResources().getColor(R.color.toast_blue),
                App.getCurApplication().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //黄色
    public static void showWarring( String msg) {
        App.runOnUiThread(() -> Toasty.warning(App.getCurApplication(), msg, Toasty.LENGTH_SHORT, true).show());
    }

    public static void showExit( String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getCurApplication(), msg,
                ContextCompat.getDrawable(App.getCurApplication(), R.drawable.ic_fail_default),
                App.getCurApplication().getResources().getColor(R.color.toast_blue),
                App.getCurApplication().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

}
