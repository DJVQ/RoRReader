package com.example.myreadproject8.ui.adapter.holder.book;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.adapter.holder.ViewHolderImpl;


/**
 * @author fengyue
 * @date 2020/8/17 15:07
 */
public class CatalogHolder extends ViewHolderImpl<Chapter> {
    private TextView tvTitle;
    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_chapter_title_item;
    }

    @Override
    public void initView() {
        tvTitle = findById(R.id.tv_chapter_title);
    }

    @Override
    public void onBind(Chapter data, int pos) {
        if (ChapterService.isChapterCached(data.getBookId(), data.getTitle()) || data.getEnd() > 0) {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.selector_category_load), null, null, null);
        } else {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.selector_category_unload), null, null, null);
        }
        tvTitle.setText(data.getTitle());
    }
}
