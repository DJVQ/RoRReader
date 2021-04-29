package com.example.myreadproject8.util.read.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myreadproject8.Application.App;


/**
 * @author fengyue
 * @date 2020/8/14 22:04
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    public static final String CANCEL_ACTION = "cancelAction";

    @Override
    public void onReceive(Context context, Intent intent) {
        //todo 跳转之前要处理的逻辑
        if (CANCEL_ACTION.equals(intent.getAction())){
            App.getmApplication().shutdownThreadPool();
        }
    }
}
