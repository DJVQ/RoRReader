package com.example.myreadproject8.ui.adapter.booksource;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.holder.ViewHolderImpl;
import com.example.myreadproject8.util.source.BookSourceManager;


/**
 * @author fengyue
 * @date 2020/9/30 18:43
 */
public class SourceExchangeHolder extends ViewHolderImpl<Book> {
    //书源标题
    TextView sourceTvTitle;
    //书源最新章节
    TextView sourceTvChapter;
    //是否选中
    ImageView sourceIv;
    //获取布局
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_change_source;
    }

    @Override
    public void initView() {
        sourceTvTitle = findById(R.id.tv_source_name);
        sourceTvChapter = findById(R.id.tv_lastChapter);
        sourceIv = findById(R.id.iv_checked);
    }

    //设置表项
    @Override
    public void onBind(Book data, int pos) {
        sourceTvTitle.setText(BookSourceManager.getSourceNameByStr(data.getSource()));
        sourceTvChapter.setText(data.getNewestChapterTitle());
        if (Boolean.parseBoolean(data.getNewestChapterId()))
            sourceIv.setVisibility(View.VISIBLE);
        else
            sourceIv.setVisibility(View.GONE);
    }
}
