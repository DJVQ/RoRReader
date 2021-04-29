package com.example.myreadproject8.ui.activity.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myreadproject8.AAATest.mysql.RegisterCnn;
import com.example.myreadproject8.R;
import com.example.myreadproject8.util.toast.ToastUtils;

public class RegisterActivity extends AppCompatActivity {


    private EditText editTextTextEmailAddress,editTextTextPassword,editTextTextPassword2;
    private String userName,password1,password2;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){
        editTextTextEmailAddress=findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword=findViewById(R.id.editTextTextPassword);
        editTextTextPassword2=findViewById(R.id.editTextTextPassword2);
        mCheckBox = findViewById(R.id.cb_agreement);
    }

    public void registerBtn(View view) {
        userName=editTextTextEmailAddress.getText().toString().trim();
        password1=editTextTextPassword.getText().toString().trim();
        password2=editTextTextPassword2.getText().toString().trim();
        //判断输入框内容
        if(userName.equals("")){
            ToastUtils.showWarring("请输入用户名");
            return;
        }else if(password1.equals("")){
            ToastUtils.showWarring("请输入密码");
            return;
        }else if(password2.equals("")){
            ToastUtils.showWarring("请再次输入密码");
            return;
        }else if (!(password1.equals(password2))){
            ToastUtils.showWarring("两次密码不一致，请重新输入");
            return;
        }else if(!mCheckBox.isChecked()){
            ToastUtils.showWarring("请先勾选用户协议!");
            return;
        }else{
            //RegisterBackground register = new RegisterBackground(this);
            //register.execute(userName,password1);
            RegisterCnn register = new RegisterCnn(this);
            register.setConnStr(getResources().getString(R.string.registerConnStr));
            register.execute(userName,password1);
        }
    }

}