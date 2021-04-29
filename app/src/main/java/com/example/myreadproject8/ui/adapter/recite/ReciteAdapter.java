package com.example.myreadproject8.ui.adapter.recite;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.service.ReciteService;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.presenter.recite.RecitePresenter;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.custom.DragAdapter;

import java.util.ArrayList;

/**
 * created by ycq on 2021/4/25 0025
 * describe：
 */
public class ReciteAdapter extends DragAdapter {
    protected int mResourceId;
    protected ArrayList<Recite> list;
    protected Context mContext;
    protected ReciteService mReciteService;
    protected RecitePresenter mRecitePresenter;

    protected String[] menu = {
        App.getMContext().getResources().getString(R.string.menu_recite_check),
        App.getMContext().getResources().getString(R.string.menu_recite_d)
    };

    public ReciteAdapter(Context context,int textViewResourceId,ArrayList<Recite> objects,RecitePresenter recitePresenter){
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mReciteService = new ReciteService();
        mRecitePresenter = recitePresenter;
    }
    @Override
    public void onDataModelMove(int from, int to) {
        Recite r = list.remove(from);
        list.add(to,r);
        for(int i = 0;i<list.size();i++){
            list.get(i).setSortCode(i);
        }
        mReciteService.updateEntity(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getSortCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void remove(Recite item){
        list.remove(item);
        notifyDataSetChanged();
        mReciteService.deleteRecite(item);
    }

    public void add(Recite item){
        list.add(item);
        notifyDataSetChanged();
        mReciteService.addRecite(item);
    }

    protected  void showDeleteReciteDialog(final Recite recite){
        DialogCreator.createCommonDialog(mContext,"移出背诵区","确定移除"+recite.getReciteT()+"吗?",
                true,(dialogInterface,i)->{
                    remove(recite);
                    ToastUtils.showSuccess("移出背诵区成功!");
                },null);
    }

    static class ViewHolder{
        TextView tvReciteT;
    }

}
