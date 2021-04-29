package com.example.myreadproject8.ui.presenter.recite;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.service.ReciteService;
import com.example.myreadproject8.ui.activity.IndexActivity;
import com.example.myreadproject8.ui.adapter.recite.ReciteAdapter;
import com.example.myreadproject8.ui.adapter.recite.ReciteDetailAdapter;
import com.example.myreadproject8.ui.fragment.ReciteFragment;
import com.example.myreadproject8.ui.presenter.base.BasePresenter;
import com.example.myreadproject8.widget.BadgeView;
import com.example.myreadproject8.widget.custom.DragSortGridView;
import com.itingchunyu.badgeview.BadgeTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by ycq on 2021/4/25 0025
 * describe：
 */
public class RecitePresenter implements BasePresenter {
    private final ReciteFragment mReciteFragment;
    private final ArrayList<Recite> mRecite = new ArrayList<>();
    private ReciteAdapter mReciteAdapter;
    private ReciteService mReciteService;
    private IndexActivity mMainActivity;
    private BadgeTextView mBadgeTextView;

    @SuppressLint("HandlerLeak")
    public final android.os.Handler mHander = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 1:
                    if(!App.isDestroy(mMainActivity)) {
                        App.runOnUiThread(()->mReciteAdapter.notifyDataSetChanged());
                    }
                    mReciteFragment.getSrlContent().finishRefresh();
                    break;
                case 2:
                    mReciteFragment.getSrlContent().finishRefresh();
                    break;
                case 3:
                    mReciteAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    break;
                case 5:
                    break;

            }
        }
    };
    private ExecutorService es = Executors.newFixedThreadPool(1);//更新/下载线程池


    public RecitePresenter(ReciteFragment reciteFragment){
        mReciteFragment = reciteFragment;
        mReciteService = new ReciteService();
        mMainActivity = (IndexActivity)(mReciteFragment.getActivity());
    }


    @Override
    public void start() {
        //获取数据
        getData();

        //设置是否允许刷新
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            mReciteFragment.getSrlContent().setEnableRefresh(false);
        mReciteFragment.getSrlContent().setEnableHeaderTranslationContent(false);
        //设置刷新监听器
        mReciteFragment.getSrlContent().setOnRefreshListener(refreshlayout -> init());
        //长按事件监听
        mReciteFragment.getGvRecite().setOnItemLongClickListener((parent, view, position, id) -> false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mReciteFragment.getGvRecite().getmScrollView().setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                mReciteFragment.getSrlContent().setEnableRefresh(scrollY == 0);
            });
        }
    }

    private void getData(){

        init();
    }




    /**
     * description: 初始化
     */
    public void init(){
        initRecite();//初始化背诵区内容
        if(mRecite.size() == 0){
            mReciteFragment.getGvRecite().setVisibility(View.GONE);
            mReciteFragment.getLlReciteNoDataTips().setVisibility(View.VISIBLE);
        }else {
            if(mReciteAdapter == null){
                mReciteAdapter = new ReciteDetailAdapter(mMainActivity, R.layout.item_recite,mRecite,this);
                mReciteFragment.getGvRecite().setNumColumns(1);
                mReciteFragment.getGvRecite().setDragModel(-1);
                mReciteFragment.getGvRecite().setTouchClashparent(mMainActivity.getViewPagerMain());
                mReciteFragment.getGvRecite().setOnDragSelectListener(new DragSortGridView.OnDragSelectListener() {
                    @Override
                    public void onDragSelect(View mirror) {
                        mirror.setBackgroundColor(mMainActivity.getResources().getColor(R.color.colorBackground));
                        mirror.setScaleY(1.05f);
                    }

                    @Override
                    public void onPutDown(View itemView) {

                    }
                });
                mReciteFragment.getGvRecite().setAdapter(mReciteAdapter);
            }else {
                mReciteAdapter.notifyDataSetChanged();
            }
            mReciteFragment.getLlReciteNoDataTips().setVisibility(View.GONE);
            mReciteFragment.getGvRecite().setVisibility(View.VISIBLE);
        }
        mHander.sendMessage(mHander.obtainMessage(2));
    }

    private void initRecite(){
        mRecite.clear();
        mRecite.addAll(mReciteService.findAllRecite());
        Collections.sort(mRecite,(o1,o2)->{
            if(o1.getId()>o2.getId()){
                return 1;
            }else if(o1.getId()<o2.getId()){
                return -1;
            }
            return 0;
        });

        for(int i = 0;i<mRecite.size();i++){
            long sort = mRecite.get(i).getSortCode();
            if(sort != i+1){
                mRecite.get(i).setSortCode(i+1);
            }
            mReciteService.updateEntity(mRecite.get(i));//更新数据库
        }
    }





    /**
     * description:销毁
     */
    public void destroy(){
        for(int i =0;i<5;i++){
            mHander.removeMessages(i+1);
        }
    }



}
