package com.example.myreadproject8.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;
import com.example.myreadproject8.databinding.FragmentHomeBinding;


public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding binding;
    private View view;


    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }


    @Override
    protected void initClick() {
        view = binding.getRoot();
        binding.funTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               System.out.println("rnmtq"+ getActivity().getIntent().getAction());
//                BookSource source = new BookSource();
//                source.setSourceEName("cangshu99");//这是内置书源标识，必填
//                source.setSourceName("99藏书");//设置书源名称
//                source.setSourceGroup("内置书源");//设置书源分组
//                source.setEnable(true);//设置书源可用性
//                source.setSourceUrl("com.example.myreadproject8.util.net.crawler.read.CansShu99ReadCrawler");//这是书源完整类路径，必填
//                source.setOrderNum(0);//内置书源一般设置排序为0
//                GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source);//添加书源进数据库
                SysManager.resetSource();

            }
        });
    }
}