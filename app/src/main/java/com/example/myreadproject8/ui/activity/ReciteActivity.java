package com.example.myreadproject8.ui.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.ActivityReciteBinding;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.service.ReciteService;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.DateHelper;

public class ReciteActivity extends BaseActivity {
    ActivityReciteBinding binding;
    Recite recite;
    ReciteService reciteService;
    Context context;

    @Override
    protected void bindView() {
        binding = ActivityReciteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        context = this;
        recite = (Recite)getIntent().getSerializableExtra(APPCONST.Recite);
        reciteService = new ReciteService();
        binding.reciteDetailTitle.setText(recite.getReciteT());
        binding.reciteDetailContent.setText("\t\t\t\t"+recite.getReciteContent());
        binding.reciteDetailContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.reciteDetailContent.setVerticalScrollBarEnabled(true);
        binding.reciteDetailNums.setText("第\t"+recite.getReciteNums()+"\t次打开");
        for(int i=0;i<recite.getReciteNum();i++){
            if(i==0){
                binding.rvReciteStatusFirst.setVisibility(View.VISIBLE);
                if(recite.getFirstRecite())
                    binding.rvReciteStatusFirst.setImageResource(R.drawable.ic_item_category_download);
            }else if(i==1){
                binding.rvReciteStatusSecond.setVisibility(View.VISIBLE);
                if(recite.getSecondRecite())
                    binding.rvReciteStatusSecond.setImageResource(R.drawable.ic_item_category_download);
            }else if(i==2){
                binding.rvReciteStatusThird.setVisibility(View.VISIBLE);
                if(recite.getThirdRecite())
                    binding.rvReciteStatusThird.setImageResource(R.drawable.ic_item_category_download);
            }else if(i==3){
                binding.rvReciteStatusFourth.setVisibility(View.VISIBLE);
                if(recite.getFourthRecite())
                    binding.rvReciteStatusFourth.setImageResource(R.drawable.ic_item_category_download);
            }else if(i==4){
                binding.rvReciteStatusFifth.setVisibility(View.VISIBLE);
                if(recite.getFifthRecite())
                    binding.rvReciteStatusFifth.setImageResource(R.drawable.ic_item_category_download);
            } else if(i==5){
                binding.rvReciteStatusSixth.setVisibility(View.VISIBLE);
                if(recite.getSixthRecite())
                    binding.rvReciteStatusSixth.setImageResource(R.drawable.ic_item_category_download);
            }else if(i==6){
                binding.rvReciteStatusSeventh.setVisibility(View.VISIBLE);
                if(recite.getSeventhRecite())
                    binding.rvReciteStatusSeventh.setImageResource(R.drawable.ic_item_category_download);
            }
        }
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null);{
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        setStatusBarColor(R.color.colorPrimary,true);
        getSupportActionBar().setTitle("背诵");
        getSupportActionBar().setSubtitle(recite.getBookName()+"——"+recite.getAuthor());
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.actionHideRecite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.reciteDetailContent.getVisibility() == View.VISIBLE) {
                    binding.reciteDetailContent.setVisibility(View.INVISIBLE);
                }else {
                    binding.reciteDetailContent.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.actionHasRecite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] flag = new boolean[7];
                for(int i = 0;i < flag.length;i++){
                    flag[i] = false;
                }
                int [] diff = DateHelper.timeDiff(recite.getAddDate(),System.currentTimeMillis());
                //按背诵时间段来设置计划完成情况
                if(diff[2] > 20 && diff[1] < 1 && diff[0] < 1){
                    recite.setFirstRecite(true);//20分钟~1小时
                    recite.setReciteIndex(1);
                    flag[0] = true;
                }else if(diff[1] >= 1 && diff[0] < 1 && diff[1] < 8){
                    if(recite.getReciteNum()>1) {
                        recite.setSecondRecite(true);//1小时~8小时
                        recite.setReciteIndex(2);
                        flag[1] = true;
                    }
                }else if(diff[1] >= 8 && diff[0] < 1){
                    if(recite.getReciteNum()>2) {
                        recite.setThirdRecite(true);//8小时~1天
                        recite.setReciteIndex(3);
                        flag[2] = true;
                    }
                }else if(diff[0] >= 1 && diff[0] < 2 ){
                    if(recite.getReciteNum()>3) {
                        recite.setFourthRecite(true);//1~2天
                        recite.setReciteIndex(4);
                        flag[3] = true;
                    }
                }else if(diff[0] >= 2 && diff[0] < 6){
                    if(recite.getReciteNum()>4) {
                        recite.setFifthRecite(true);//2~36天
                        recite.setReciteIndex(5);
                        flag[4] = true;
                    }
                }else if(diff[0] >= 6 && diff[0] < 30 ){
                    if(recite.getReciteNum()>5) {
                        recite.setSixthRecite(true);//6~30天
                        recite.setReciteIndex(6);
                        flag[5] = true;
                    }
                }else if(diff[0] >= 30){
                    if(recite.getReciteNum()>6) {
                        recite.setSeventhRecite(true);//30天~
                        recite.setReciteIndex(7);
                        flag[6] = true;
                    }
                }
                reciteService.updateEntity(recite);
                if(flag[0]||flag[1]||flag[2]||flag[3]||flag[4]||flag[5]||flag[6])
                    ToastUtils.showSuccess("背诵成功！");
                else {
                    ToastUtils.showSuccess("本次背诵成功,但由于不在有效时段内,故不纳入统计,记忆有效时段为20min~1hour;1hour~8hour;8hour~1day;1day~2day;2day~30day;30day~");
                }
                finish();
            }
        });

        binding.actionReciteOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.actionCheckDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReciteTableActivity.class);
                intent.putExtra(APPCONST.Recite,recite);
                context.startActivity(intent);
                finish();
            }
        });
    }
}