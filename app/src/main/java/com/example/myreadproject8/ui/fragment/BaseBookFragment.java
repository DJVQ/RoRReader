package com.example.myreadproject8.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.bookcase.BookcaseAdapter;
import com.example.myreadproject8.ui.adapter.filesystem.FileSystemAdapter;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * created by ycq on 2021/4/11 0011
 * describe：
 */
public class BaseBookFragment extends BaseFragment {

    protected BookcaseAdapter mBAdapter;
    protected OnBookCheckedListener mBListener;
    protected  boolean isCheckedAll;


    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }



    public void setChecked(boolean checked){
        isCheckedAll = checked;
    }

    //当前fragment是否全选
    public boolean isCheckedAll() {
        return isCheckedAll;
    }





    //文件点击监听
    public interface OnBookCheckedListener {
        void onItemCheckedChange(boolean isChecked);
        void onCategoryChanged();
    }

    public BookcaseAdapter getAdapter(){
        return mBAdapter;
    }
}
