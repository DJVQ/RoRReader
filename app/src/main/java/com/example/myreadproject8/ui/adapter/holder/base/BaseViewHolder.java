package com.example.myreadproject8.ui.adapter.holder.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadproject8.ui.adapter.holder.IViewHolder;

/**
 * created by ycq on 2021/4/3 0003
 * describe：用于创建RecyclerView.ViewHolder的基类
 */
public class BaseViewHolder<T> extends RecyclerView.ViewHolder{
    public IViewHolder<T> holder;

    public BaseViewHolder(View itemView, IViewHolder<T> holder) {
        super(itemView);
        this.holder = holder;
        holder.initView();
    }
}
