package com.example.myreadproject8.ui.adapter.holder.book;

import android.widget.CheckBox;
import android.widget.TextView;

import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.holder.ViewHolderImpl;
import com.example.myreadproject8.widget.cover_image_view.CoverImageView;

import java.io.File;
import java.util.HashMap;

/**
 * created by ycq on 2021/4/11 0011
 * describe：
 */
public class BookHolder extends ViewHolderImpl<Book> {
    private TextView mBTvName;
    private CheckBox mBSelect;
    private TextView mATvName;
    private TextView mNTvName;
    private TextView mHTvName;
    private CoverImageView mCoverImageView;


    private HashMap<Book, Boolean> mBSelectedMap;
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_book;
    }

    @Override
    public void initView() {
        mBTvName = findById(R.id.tv_book_name);
        mBSelect = findById(R.id.m_book_select);
        mATvName = findById(R.id.tv_book_author);
        mHTvName = findById(R.id.tv_book_history_chapter);
        mNTvName = findById(R.id.tv_book_newest_chapter);
        mCoverImageView = findById(R.id.iv_book_img);
    }

    @Override
    public void onBind(Book data, int pos) {
        setBook(data);
    }

    private void setBook(Book book){
        mBTvName.setText(book.getName());
        mATvName.setText(book.getAuthor());
        mHTvName.setText(book.getHistoryChapterId());
        mNTvName.setText(book.getNewestChapterId() + book.getNewestChapterTitle());
        mCoverImageView.load(book.getChapterUrl(),book.getName(),book.getAuthor());
    }


}
