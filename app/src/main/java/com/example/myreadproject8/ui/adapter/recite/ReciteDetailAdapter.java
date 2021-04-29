package com.example.myreadproject8.ui.adapter.recite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;

import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.ui.activity.ReciteActivity;
import com.example.myreadproject8.ui.activity.ReciteTableActivity;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.presenter.recite.RecitePresenter;
import com.example.myreadproject8.util.utils.DateHelper;
import com.itingchunyu.badgeview.BadgeTextView;

import java.util.ArrayList;

/**
 * created by ycq on 2021/4/25 0025
 * describe：
 */
public class ReciteDetailAdapter extends ReciteAdapter{
    ViewHolder viewHolder = null;
    Context mContext;
    BadgeTextView mBadgeTextView;


    public ReciteDetailAdapter(Context context, int textViewResourceId, ArrayList<Recite> objects, RecitePresenter recitePresenter) {
        super(context, textViewResourceId, objects, recitePresenter);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        viewHolder = new ViewHolder();
        convertView = LayoutInflater.from(mContext).inflate(mResourceId,null);
        viewHolder.tvAddFirstTime = convertView.findViewById(R.id.tv_recite_time);
        viewHolder.tvReciteT = convertView.findViewById(R.id.tv_recite_title);
        viewHolder.ivReciteFirst = convertView.findViewById(R.id.recite_first);
        viewHolder.ivReciteSecond = convertView.findViewById(R.id.recite_second);
        viewHolder.ivReciteThird = convertView.findViewById(R.id.recite_third);
        viewHolder.ivReciteFourth = convertView.findViewById(R.id.recite_fourth);
        viewHolder.ivReciteFifth = convertView.findViewById(R.id.recite_fifth);
        viewHolder.ivReciteSixth = convertView.findViewById(R.id.recite_sixth);
        viewHolder.ivReciteSeventh = convertView.findViewById(R.id.recite_seventh);
        viewHolder.mNeedReciteTimes = convertView.findViewById(R.id.m_need_reciteTime);
        viewHolder.llRecite = convertView.findViewById(R.id.ll_recite);
        mBadgeTextView = new BadgeTextView(mContext);
        initView(position);
        return convertView;
    }

    private void initView(int position){
        final Recite recite = (Recite)getItem(position);
        int [] diff = DateHelper.timeDiff(recite.getAddDate(),System.currentTimeMillis());
        //按背诵时间段来设置计划完成情况
        if(diff[2] > 20 && diff[1] < 1 && diff[0] < 1){
            recite.setReciteIndex(1);
        }else if(diff[1] >= 1 && diff[0] < 1 && diff[1] < 8){
            recite.setReciteIndex(2);
        }else if(diff[1] >= 8 && diff[0] < 1){
            recite.setReciteIndex(3);
        }else if(diff[0] >= 1 && diff[0] < 2 ){
            recite.setReciteIndex(4);
        }else if(diff[0] >= 2 && diff[0] < 6){
            recite.setReciteIndex(5);
        }else if(diff[0] >= 6 && diff[0] < 30 ){
            recite.setReciteIndex(6);
        }else if(diff[0] >= 30){
            recite.setReciteIndex(7);
        }
        viewHolder.tvReciteT.setText(recite.getReciteT());
        viewHolder.tvAddFirstTime.setText(DateHelper.longToTime(recite.getAddDate()));

        for(int i=0;i<recite.getReciteNum();i++){
            if(i==0){
                viewHolder.ivReciteFirst.setVisibility(View.VISIBLE);
                if(recite.getFirstRecite())
                    viewHolder.ivReciteFirst.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==1){
                viewHolder.ivReciteSecond.setVisibility(View.VISIBLE);
                if(recite.getSecondRecite())
                    viewHolder.ivReciteSecond.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==2){
                viewHolder.ivReciteThird.setVisibility(View.VISIBLE);
                if(recite.getThirdRecite())
                    viewHolder.ivReciteThird.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==3){
                viewHolder.ivReciteFourth.setVisibility(View.VISIBLE);
                if(recite.getFourthRecite())
                    viewHolder.ivReciteFourth.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==4){
                viewHolder.ivReciteFifth.setVisibility(View.VISIBLE);
                if(recite.getFifthRecite())
                    viewHolder.ivReciteFifth.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==5){
                viewHolder.ivReciteSixth.setVisibility(View.VISIBLE);
                if(recite.getSixthRecite())
                    viewHolder.ivReciteSixth.setImageResource(R.drawable.ic_item_category_finish);
            }else if(i==6){
                viewHolder.ivReciteSeventh.setVisibility(View.VISIBLE);
                if(recite.getSeventhRecite())
                    viewHolder.ivReciteSeventh.setImageResource(R.drawable.ic_item_category_finish);
            }
        }

        viewHolder.llRecite.setOnClickListener(v->{
            Intent intent = new Intent(mContext, ReciteActivity.class);
            intent.putExtra(APPCONST.Recite,recite);
            recite.setReciteNums(recite.getReciteNums()+1);
            mReciteService.updateEntity(recite);
            mContext.startActivity(intent);
        });
        viewHolder.llRecite.setOnLongClickListener(v->{
            AlertDialog reciteDialog = MyAlertDialog.build(mContext)
                    .setTitle(recite.getReciteT())
                    .setItems(menu,(dialog,which)->{
                        switch (which){
                            case 0:
                                Intent intent = new Intent(mContext, ReciteTableActivity.class);
                                intent.putExtra(APPCONST.Recite,recite);
                                mContext.startActivity(intent);
                                break;
                            case 1:
                                showDeleteReciteDialog(recite);
                                break;
                        }
                    })
                    .setNegativeButton(null,null)
                    .setPositiveButton(null,null)
                    .create();
            reciteDialog.show();
            return true;
        });
        if (recite.getReciteIndex()==1){
            if (!recite.getFirstRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        } else if (recite.getReciteIndex()==2) {
            if (!recite.getSecondRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }else if (recite.getReciteIndex()==3) {
            if (!recite.getThirdRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }else if (recite.getReciteIndex()==4) {
            if (!recite.getFourthRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }else if (recite.getReciteIndex()==5) {
            if (!recite.getFifthRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }else if (recite.getReciteIndex()==6) {
            if (!recite.getSixthRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }else if (recite.getReciteIndex()==7) {
            if (!recite.getSeventhRecite()) {
                mBadgeTextView.setTargetView(viewHolder.mNeedReciteTimes);
            }
        }

    }

    static class ViewHolder extends ReciteAdapter.ViewHolder{
        TextView tvAddFirstTime;
        ImageView ivReciteFirst;
        ImageView ivReciteSecond;
        ImageView ivReciteThird;
        ImageView ivReciteFourth;
        ImageView ivReciteFifth;
        ImageView ivReciteSixth;
        ImageView ivReciteSeventh;
        LinearLayout llRecite;
        TextView mNeedReciteTimes;
    }
}
