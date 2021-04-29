package com.example.myreadproject8.AAATest.mysql;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.ui.activity.IndexActivity;
import com.example.myreadproject8.util.toast.ToastUtils;


public class LoginCnn extends Cnn{
    private Activity activity;

    public LoginCnn(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void setConnStr(String connStr) {
        super.setConnStr(connStr);
    }

    @Override
    protected void onPostExecute(String s) {
        if(s.equals("success")){
            activity.finish();
            ToastUtils.showSuccess("登录成功");
            Intent intent = new Intent(new Intent(context, IndexActivity.class));
            intent.putExtra(APPCONST.LOGIN,"login");
            context.startActivity(intent);

        }else if(s.equals("passError")){
            ToastUtils.showError("密码错误,请重新输入");
        }else if(s.equals("noUser")){
            ToastUtils.showWarring("用户不存在");
        }else if(s.equals("connErr")){
            ToastUtils.showError("数据库连接失败");
        }else{
            ToastUtils.showError("未知错误");
        }
    }
}
