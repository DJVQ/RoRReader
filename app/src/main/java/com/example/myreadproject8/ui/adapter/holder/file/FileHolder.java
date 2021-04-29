package com.example.myreadproject8.ui.adapter.holder.file;



import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.ui.adapter.holder.ViewHolderImpl;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.string.StringUtils;

import java.io.File;
import java.util.HashMap;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public class FileHolder extends ViewHolderImpl<File> {
    private ImageView mIvIcon;
    private CheckBox mCbSelect;
    private TextView mTvName;
    private LinearLayout mLlBrief;
    private TextView mTvTag;
    private TextView mTvSize;
    private TextView mTvDate;
    private TextView mTvSubCount;

    private HashMap<File, Boolean> mSelectedMap;
    public FileHolder(HashMap<File, Boolean> selectedMap){
        mSelectedMap = selectedMap;
    }

    @Override
    public void initView() {
        mIvIcon = findById(R.id.file_iv_icon);
        mCbSelect = findById(R.id.file_cb_select);
        mTvName = findById(R.id.file_tv_name);
        mLlBrief = findById(R.id.file_ll_brief);
        mTvTag = findById(R.id.file_tv_tag);
        mTvSize = findById(R.id.file_tv_size);
        mTvDate = findById(R.id.file_tv_date);
        mTvSubCount = findById(R.id.file_tv_sub_count);
    }

    @Override
    public void onBind(File data, int pos) {
        //判断是文件还是文件夹
        if (data.isDirectory()){
            setFolder(data);
        }
        else {
            setFile(data);
        }
    }

    private void setFile(File file){
        //选择

        if (BookService.getInstance().findBookByPath(file.getAbsolutePath()) != null){
            mIvIcon.setImageResource(R.drawable.ic_read_default);
            mIvIcon.setVisibility(View.VISIBLE);
            mCbSelect.setVisibility(View.GONE);
        }
        else {
            boolean isSelected = mSelectedMap.get(file);
            mCbSelect.setChecked(isSelected);
            mIvIcon.setVisibility(View.GONE);
            mCbSelect.setVisibility(View.VISIBLE);
        }

        mLlBrief.setVisibility(View.VISIBLE);
        mTvSubCount.setVisibility(View.GONE);

        mTvName.setText(file.getName());
        mTvSize.setText(FileUtils.getFileSize(file.length()));
        mTvDate.setText(StringUtils.dateConvert(file.lastModified(), APPCONST.FORMAT_FILE_DATE));
    }

    public void setFolder(File folder){
        //图片
        mIvIcon.setVisibility(View.VISIBLE);
        mCbSelect.setVisibility(View.GONE);
        mIvIcon.setImageResource(R.drawable.ic_folder);
        //名字
        mTvName.setText(folder.getName());
        //介绍
        mLlBrief.setVisibility(View.GONE);
        mTvSubCount.setVisibility(View.VISIBLE);

        mTvSubCount.setText(getContext().getString(R.string.file_sub_count, folder.list().length));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_file;
    }
}
