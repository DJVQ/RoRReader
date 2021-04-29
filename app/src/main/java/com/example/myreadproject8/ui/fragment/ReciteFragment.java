package com.example.myreadproject8.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentReadBinding;
import com.example.myreadproject8.databinding.FragmentReciteBinding;
import com.example.myreadproject8.ui.presenter.recite.RecitePresenter;
import com.example.myreadproject8.widget.custom.DragSortGridView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;


public class ReciteFragment extends Fragment {
    private FragmentReciteBinding binding;

    private RecitePresenter mRecitePresenter;



    public ReciteFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReciteBinding.inflate(inflater,container,false);
        mRecitePresenter = new RecitePresenter(this);
        mRecitePresenter.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecitePresenter.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecitePresenter.init();
    }

    public LinearLayout getLlReciteNoDataTips(){
        return binding.llReciteNoDataTips;
    }

    public DragSortGridView getGvRecite(){
        return binding.reciteGvBook;
    }
    public SmartRefreshLayout getSrlContent(){
        return binding.srlReciteContent;
    }
    public RecitePresenter getRecitePresenter(){
        return mRecitePresenter;
    }
    public boolean isRecreate(){
        return binding == null;
    }
    public RelativeLayout getReciteEdit(){
        return binding.rlReciteEdit;
    }
    public CheckBox getmCbSelectAll() {
        return binding.reciteSelectedAll;
    }

    public Button getmBtnDelete() {
        return binding.reciteBtnDelete;
    }


}