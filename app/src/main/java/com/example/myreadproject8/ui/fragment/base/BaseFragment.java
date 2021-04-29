package com.example.myreadproject8.ui.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * created by ycq on 2021/4/3 0003
 * describe：Fragment基类
 */
public abstract class BaseFragment extends Fragment {
    protected CompositeDisposable mDisposable;

    private View root = null;

    /**
     * description: 绑定视图
     */
    protected abstract View bindView(LayoutInflater inflater, ViewGroup container);

    protected void addDisposable(Disposable d){
        if (mDisposable == null){
            mDisposable = new CompositeDisposable();
        }
        mDisposable.add(d);
    }

    /**
     * description: 初始化数据
     */
    protected void initData(Bundle savedInstanceState){
    }

    /**
     * description: 初始化点击事件
     */
    protected void initClick(){
    }

    /**
     * description: 逻辑使用区
     */
    protected void processLogic(){
    }

    /**
     * description: 初始化零件
     */
    protected void initWidget(Bundle savedInstanceState){
    }

    /******************************lifecycle area*****************************************/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = bindView(inflater, container);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData(savedInstanceState);
        initWidget(savedInstanceState);
        initClick();
        processLogic();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mDisposable != null){
            mDisposable.clear();
        }
    }

    /**************************公共类*******************************************/
    public String getName(){
        return getClass().getName();
    }

    /**
     * description:获取控件
     */
    protected <VT> VT getViewById(int id){
        if (root == null){
            return  null;
        }
        return (VT) root.findViewById(id);
    }
}
