package com.example.myreadproject8.ui.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.ActivityReciteTableBinding;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.dialog.ReciteDialog;
import com.example.myreadproject8.ui.dialog.ResetReciteNum;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.util.List;

public class ReciteTableActivity extends BaseActivity {
    ActivityReciteTableBinding binding;
    Recite recite;
    float[] m;
    Context context;


    @Override
    protected void bindView() {
        binding = ActivityReciteTableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        initRecite();
    }

    public void initRecite(){
        context = this;
        recite = (Recite)getIntent().getSerializableExtra(APPCONST.Recite);
        binding.tableRecite.setupCoordinator("时间段", "记忆百分比/%", /*这里是横坐标的值*/"","20m", "1h", "9h", "1d", "2d", "6d", "30d","more");

        binding.tableStandardRecite.setupCoordinator("d/h/m", "记忆百分比/%", /*这里是横坐标的值*/"","20m", "1h", "9h", "1d", "2d", "6d", "30d","more");
        binding.tableStandardRecite.addWave(ContextCompat.getColor(this, R.color.colorPrimary), false,
                100f, 58.2f, 44.2f, 35.8f, 33.7f, 27.8f, 25.4f,21.1f,20f);
        int r = 0;
        int j = recite.getReciteNum();
        int k = 0;
        m = new float[recite.getReciteIndex()+1];
        float l = 100;
        m[0] = l;
        int count = 0;
        for(int i=0;i<recite.getReciteIndex();i++){
            if(i==0){
                if(recite.getFirstRecite()){
                    r++;
                    j--;
                    l = 93;
                }else {
                    l = 58.2f;
                    count++;
                }
                m[i+1] = l;
            }else if(i==1){
                if(recite.getSecondRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else
                        l = l * 0.759f;
                    count++;
                }
                m[i+1] = l;
            }else if(i==2){
                if(recite.getThirdRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else if(count == 1)
                        l = l * 0.759f;
                    else
                        l = l * 0.809f;
                    count++;
                }
                m[i+1] = l;
            }else if(i==3){
                if(recite.getFourthRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else if(count == 1)
                        l = l * 0.759f;
                    else if(count == 2)
                        l = l * 0.809f;
                    else
                        l = l * 0.941f;
                    count++;
                }
                m[i+1] = l;
            }else if(i==4){
                if(recite.getFifthRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else if(count == 1)
                        l = l * 0.759f;
                    else if(count == 2)
                        l = l * 0.809f;
                    else if(count == 3)
                        l = l * 0.941f;
                    else
                        l = l * 0.825f;
                    count++;
                }
                m[i+1] = l;
            } else if(i==5){
                if(recite.getSixthRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else if(count == 1)
                        l = l * 0.759f;
                    else if(count == 2)
                        l = l * 0.809f;
                    else if(count == 3)
                        l = l * 0.941f;
                    else if(count == 4)
                        l = l * 0.825f;
                    else
                        l = l * 0.914f;
                    count++;
                }
                m[i+1] = l;
            }else if(i==6){
                if(recite.getSeventhRecite()){
                    r++;
                    j--;
                    if(r<i)
                        k++;
                    l = 93;
                }else {
                    if(count == 0)
                        l = l * 0.582f;
                    else if(count == 1)
                        l = l * 0.759f;
                    else if(count == 2)
                        l = l * 0.809f;
                    else if(count == 3)
                        l = l * 0.941f;
                    else if(count == 4)
                        l = l * 0.825f;
                    else if(count == 5)
                        l = l * 0.914f;
                    else
                        l = l * 0.831f;
                    count++;
                }
                m[i+1] = l;
            }
        }
        if(recite.getReciteIndex() == 0){

        }else
            binding.tableRecite.addWave(ContextCompat.getColor(this, R.color.colorPrimary), false,
                    m);
        binding.reciteAtTime.setText(r+"次");
        binding.reciteNotAtTime.setText(j+"次");
        binding.unReciteTimes.setText(k+"次");

    }
    @Override
    protected void initClick() {
        super.initClick();
        binding.reciteIdeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.llRecite.setVisibility(View.GONE);
                binding.llIdealRecite.setVisibility(View.VISIBLE);
                binding.tableIdealRecite.setupCoordinator("时间段", "记忆百分比/%", /*这里是横坐标的值*/"","20m", "1h", "9h", "1d", "2d", "6d", "30d","more");
                binding.tableIdealRecite.addWave(ContextCompat.getColor(context, R.color.colorPrimary), false,
                        100f, 93f, 92f, 91f, 90.3f, 89.6f, 88.7f,86.8f,85.6f);
                invalidateOptionsMenu();
                supportInvalidateOptionsMenu();
            }
        });

        binding.reciteMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.llIdealRecite.setVisibility(View.GONE);
                binding.llRecite.setVisibility(View.VISIBLE);
                binding.tableRecite.setupCoordinator("时间段", "记忆百分比/%", /*这里是横坐标的值*/"","20m", "1h", "9h", "1d", "2d", "6d", "30d","more");
                if(recite.getReciteIndex() == 0){

                }else
                    binding.tableRecite.addWave(ContextCompat.getColor(context, R.color.colorPrimary), false,
                            m);

            }
        });

        binding.reciteResetNums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetReciteNum reciteDialog = new ResetReciteNum(ReciteTableActivity.this,recite,()->{
                    ToastUtils.showSuccess("更改成功!");
                    finish();
                });
                reciteDialog.show(getSupportFragmentManager(),"addRecite");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecite();
    }
}