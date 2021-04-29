package com.example.myreadproject8.AAATest.mysql;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.myreadproject8.ui.activity.user.LoginActivity;


public class RegisterCnn extends Cnn{


    public RegisterCnn(Context context) {
        super(context);
    }

    @Override
    public void setConnStr(String connStr) {
        super.setConnStr(connStr);
    }

    @Override
    protected void onPostExecute(String s) {
        if(s.equals("ok")){
            Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginActivity.class));
        }else if(s.equals("exist")){
            Toast.makeText(context, "用户名已存在，请重新输入", Toast.LENGTH_SHORT).show();
        }else if(s.equals("connErr")){
            Toast.makeText(context, "数据库连接失败", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "未知错误", Toast.LENGTH_SHORT).show();
        }
    }
}

