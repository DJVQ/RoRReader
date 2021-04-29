package com.example.myreadproject8.ui.fragment.bookcase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.example.myreadproject8.databinding.FragmentReadBinding;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;
import com.example.myreadproject8.ui.fragment.HomeFragment;
import com.example.myreadproject8.ui.presenter.book.ReadPresenter;
import com.example.myreadproject8.widget.custom.DragSortGridView;
import com.google.android.material.tabs.TabLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.Arrays;
import java.util.List;


public class ReadFragment extends Fragment {
    FragmentReadBinding binding;
    private ReadPresenter mReadPresenter;
    public ReadFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReadBinding.inflate(inflater, container, false);
        mReadPresenter = new ReadPresenter(this);
        mReadPresenter.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mReadPresenter.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mReadPresenter.init();
    }

    public LinearLayout getLlNoDataTips() {
        return binding.llReadNoDataTips;
    }

    public DragSortGridView getGvBook() {
        return binding.readGvBook;
    }
    public SmartRefreshLayout getSrlContent() {
        return binding.srlReadContent;
    }

    public ReadPresenter getmReadPresenter() {
        return mReadPresenter;
    }

    public boolean isRecreate() {
        return binding == null;
    }

    public RelativeLayout getRlBookEdit() {
        return binding.rlReadBookEdit;
    }

    public CheckBox getmCbSelectAll() {
        return binding.readBookSelectedAll;
    }

    public Button getmBtnDelete() {
        return binding.readBookBtnDelete;
    }

}