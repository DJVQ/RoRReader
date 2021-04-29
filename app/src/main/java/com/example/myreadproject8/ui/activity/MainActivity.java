package com.example.myreadproject8.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.example.myreadproject8.R;


import com.example.myreadproject8.databinding.ActivityMainBinding;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.activity.user.LoginActivity;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.PermissionsChecker;


public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;


    private static final int PERMISSIONS_REQUEST_STORAGE = 1;

    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private PermissionsChecker mPermissionsChecker;



    @Override
    protected void bindView() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }



    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        getSupportActionBar().setTitle("RoR");
        setStatusBarColor(R.color.colorPrimary,true);
    }


    @Override
    protected void initWidget() {
        super.initWidget();
    }

    @Override
    public void initClick(){
        // 申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            mPermissionsChecker = new PermissionsChecker(this);
            requestPermission();
        }

        binding.btnMainIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, IndexActivity.class);
                it.putExtra("startFromSplash",true);
                startActivity(it);
                finish();
            }
        });
        binding.btnMainLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itLogin = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(itLogin);
                finish();
            }
        });

    }



    private void requestPermission(){
        //获取读取和写入SD卡的权限
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE: {
                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //申请权限失败
                    finish();
                    ToastUtils.showWarring("请给予储存权限，否则程序无法正常运行！");
                }
                return;
            }
        }
    }
}