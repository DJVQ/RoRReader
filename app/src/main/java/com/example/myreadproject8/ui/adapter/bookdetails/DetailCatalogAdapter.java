package com.example.myreadproject8.ui.adapter.bookdetails;


import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.ui.adapter.base.BaseListAdapter;
import com.example.myreadproject8.ui.adapter.holder.IViewHolder;
import com.example.myreadproject8.ui.adapter.holder.book.CatalogHolder;

/**
 * @author fengyue
 * @date 2020/8/17 15:06
 */
public class DetailCatalogAdapter extends BaseListAdapter<Chapter> {
    @Override
    protected IViewHolder<Chapter> createViewHolder(int viewType) {
        return new CatalogHolder();
    }
}
