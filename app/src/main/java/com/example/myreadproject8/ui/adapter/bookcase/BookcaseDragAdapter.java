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
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.presenter.book.BookcasePresenter;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;




public class BookcaseDragAdapter extends BookcaseAdapter {
    ViewHolder viewHolder = null;
    protected String[] menu = {
            App.getMContext().getResources().getString(R.string.menu_book_detail),
            App.getMContext().getResources().getString(R.string.menu_book_Top),
            App.getMContext().getResources().getString(R.string.menu_book_download),
            App.getMContext().getResources().getString(R.string.menu_book_cache),
            App.getMContext().getResources().getString(R.string.menu_book_delete)
    };
    public BookcaseDragAdapter(Context context, int textViewResourceId, ArrayList<Book> objects,
                               boolean editState, BookcasePresenter bookcasePresenter, boolean isGroup) {
        super(context, textViewResourceId, objects, editState, bookcasePresenter, isGroup);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() instanceof BookcaseDetailedAdapter.ViewHolder) {
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
        final Book book = (Book) getItem(position);
        if (StringHelper.isEmpty(book.getImgUrl())) {
            book.setImgUrl("");
        }

        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(book.getSource());
        viewHolder.ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(),book.getAuthor());

        viewHolder.tvBookName.setText(book.getName());

        if (mEditState) {
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
            viewHolder.pbLoading.setVisibility(View.GONE);
            viewHolder.ivBookImg.setOnClickListener(v -> {
                setCheckedBook(book.getId());
                mListener.onItemCheckedChange(getBookIsChecked(book.getId()));
            });
            viewHolder.cbBookChecked.setVisibility(View.VISIBLE);
            viewHolder.cbBookChecked.setChecked(getBookIsChecked(book.getId()));
        } else {
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
            }
            viewHolder.ivBookImg.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, OpenReadActivity.class);
                intent.putExtra(APPCONST.BOOK, book);
                mBookService.updateEntity(book);
                mContext.startActivity(intent);
            });
            viewHolder.ivBookImg.setOnLongClickListener(v -> {
                if (!ismEditState()) {
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
                                                if (!isGroup) {
                                                    book.setSortCode(0);
                                                }else {
                                                    book.setGroupSort(0);
                                                }
                                                mBookService.updateEntity(book);
                                                mBookcasePresenter.init();
                                                ToastUtils.showSuccess("书籍《" + book.getName() + "》移至顶部成功！");
                                                break;
                                            case 2:
                                                downloadBook(book);
                                                break;
                                            case 3:
                                                App.getmApplication().newThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            if (unionChapterCathe(book)) {
                                                                DialogCreator.createTipDialog(mContext,
                                                                        "缓存导出成功，导出目录："
                                                                                + APPCONST.TXT_BOOK_DIR);
                                                            } else {
                                                                DialogCreator.createTipDialog(mContext,
                                                                        "章节目录为空或未找到缓存文件，缓存导出失败！");
                                                            }
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                            DialogCreator.createTipDialog(mContext,
                                                                    "章节目录为空或未找到缓存文件，缓存导出失败！");
                                                        }
                                                    }
                                                });
                                                break;
                                            case 4:
                                                showDeleteBookDialog(book);
                                                break;
                                        }
                                    })
                            .setNegativeButton(null, null)
                            .setPositiveButton(null, null)
                            .create();
                    bookDialog.show();
                    return true;
                }
                return false;
            });
        }

    }

    class ViewHolder extends BookcaseAdapter.ViewHolder {
    }
}
