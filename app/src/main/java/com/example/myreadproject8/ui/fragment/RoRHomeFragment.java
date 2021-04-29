package com.example.myreadproject8.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myreadproject8.databinding.FragmentRorHomeBinding;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;
import com.example.myreadproject8.widget.search_view.SearchView;


public class
RoRHomeFragment extends BaseFragment {

    private FragmentRorHomeBinding binding;
    // 1. 初始化搜索框变量
    private SearchView searchView;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentRorHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }



}