package com.example.myreadproject8.ui.adapter.booksource;


import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.base.BaseListAdapter;
import com.example.myreadproject8.ui.adapter.holder.IViewHolder;

/**
 * @author fengyue
 * @date 2020/9/30 18:42
 */
public class SourceExchangeAdapter extends BaseListAdapter<Book> {
    @Override
    protected IViewHolder createViewHolder(int viewType) {
        return new SourceExchangeHolder();
    }
}
