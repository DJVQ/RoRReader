package com.example.myreadproject8.ui.adapter.holder;

import android.view.View;
import android.view.ViewGroup;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public interface IViewHolder<T> {
    View createItemView(ViewGroup parent);
    void initView();
    void onBind(T data,int pos);
    void onClick();
}
