package com.example.myreadproject8.ui.activity.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myreadproject8.AAATest.mysql.LoginCnn;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.util.toast.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    EditText usr,pas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usr = findViewById(R.id.username);
        pas = findViewById(R.id.password);
    }

    public void loginBtn(View view){
        String user = usr.getText().toString();
        String pass = pas.getText().toString();

        if(user.equals("")){
            ToastUtils.showWarring("请输入用户名");
            return;
        }else if(pass.equals("")){
            ToastUtils.showWarring("请输入密码");
            return;
        }else {
            LoginCnn login = new LoginCnn(this);
            login.setConnStr(getResources().getString(R.string.loginConnStr));
            login.execute(user,pass);

        }
    }

    public void registerBtn(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}