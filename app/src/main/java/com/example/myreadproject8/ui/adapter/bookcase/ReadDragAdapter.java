package com.example.myreadproject8.ui.adapter.bookcase;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.appcompat.app.AlertDialog;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.activity.OpenReadActivity;
import com.example.myreadproject8.ui.activity.book.BookDetailedActivity;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.presenter.book.ReadPresenter;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.string.StringHelper;


import java.util.ArrayList;

/**
 * created by ycq on 2021/4/24 0024
 * describe：书籍表项详情
 */
public class ReadDragAdapter extends ReadAdapter{


    ViewHolder viewHolder = null;
    protected String[] menu = {
            App.getMContext().getResources().getString(R.string.menu_book_detail),
            App.getMContext().getResources().getString(R.string.menu_read_book_delete)
    };

    public ReadDragAdapter(Context context, int textViewResourceId, ArrayList<Book> objects, ReadPresenter readPresenter) {
        super(context, textViewResourceId, objects, readPresenter);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResourceId, null);
            viewHolder.cbBookChecked = convertView.findViewById(R.id.cb_book_select);
            viewHolder.ivBookImg = convertView.findViewById(R.id.iv_book_img);
            viewHolder.tvBookName = convertView.findViewById(R.id.tv_book_name);
            viewHolder.tvNoReadNum = convertView.findViewById(R.id.tv_no_read_num);
            viewHolder.pbLoading = convertView.findViewById(R.id.pb_loading);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position);
        return convertView;
    }

    private void initView(int position) {
        final Book book = getItem(position);
        if (StringHelper.isEmpty(book.getImgUrl())) {
            book.setImgUrl("");
        }

        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(book.getSource());
        viewHolder.ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(),book.getAuthor());

        viewHolder.tvBookName.setText(book.getName());


        viewHolder.cbBookChecked.setVisibility(View.GONE);
        boolean isLoading = false;
        try {
            isLoading = isBookLoading(book.getId());
        } catch (Exception ignored) {
        }
        if (isLoading) {
            viewHolder.pbLoading.setVisibility(View.VISIBLE);
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
        } else {
            viewHolder.pbLoading.setVisibility(View.GONE);
            int notReadNum = book.getChapterTotalNum() - book.getHistoryChapterNum() + book.getNoReadNum() - 1;
            if (notReadNum != 0) {
                viewHolder.tvNoReadNum.setVisibility(View.VISIBLE);
                if (book.getNoReadNum() != 0) {
                    viewHolder.tvNoReadNum.setHighlight(true);
                    if (notReadNum == -1) {
                        notReadNum = book.getNoReadNum() - 1;
                    }
                } else {
                    viewHolder.tvNoReadNum.setHighlight(false);
                }
                viewHolder.tvNoReadNum.setBadgeCount(notReadNum);
            } else {
                viewHolder.tvNoReadNum.setVisibility(View.GONE);
            }

            viewHolder.ivBookImg.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, OpenReadActivity.class);
                intent.putExtra(APPCONST.isRead,"yes");
                intent.putExtra(APPCONST.BOOK, book);
                mBookService.updateEntity(book);
                mContext.startActivity(intent);
            });
            viewHolder.ivBookImg.setOnLongClickListener(v -> {

                AlertDialog bookDialog = MyAlertDialog.build(mContext)
                        .setTitle(book.getName())
                        .setItems(menu, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent intent = new Intent(mContext, BookDetailedActivity.class);
                                    intent.putExtra(APPCONST.BOOK, book);
                                    mContext.startActivity(intent);
                                    break;
                                case 1:
                                    showDeleteBookDialog(book);
                                    break;
                            }
                        })
                        .setNegativeButton(null, null)
                        .setPositiveButton(null, null)
                        .create();
                bookDialog.show();
                return true;
            });
        }

    }

    class ViewHolder extends ReadAdapter.ViewHolder {
    }
}
