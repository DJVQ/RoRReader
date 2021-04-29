package com.example.myreadproject8.ui.adapter.bookcase;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.presenter.book.ReadPresenter;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.BadgeView;
import com.example.myreadproject8.widget.cover_image_view.CoverImageView;
import com.example.myreadproject8.widget.custom.DragAdapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * created by ycq on 2021/4/24 0024
 * describe：精读区适配器
 */
public abstract class ReadAdapter extends DragAdapter {
    private final Map<String, Boolean> isLoading = new HashMap<>();
    protected ReadAdapter.OnReadCheckedListener mListener;

    protected int mResourceId;
    protected ArrayList<Book> list;
    protected Context mContext;

    protected BookService mBookService;
    protected ChapterService mChapterService;
    protected ReadPresenter mReadPresenter;

    protected String[] menu = {
            App.getMContext().getResources().getString(R.string.menu_book_Top),
            App.getMContext().getResources().getString(R.string.menu_read_book_delete)
    };

    public ReadAdapter(Context context, int textViewResourceId, ArrayList<Book> objects
            , ReadPresenter readPresenter) {
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mReadPresenter = readPresenter;

    }


    @Override
    public void onDataModelMove(int from, int to) {
        Book b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSortCode(i);
        }
        mBookService.updateBooks(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Book getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getSortCode() ;
    }

    public void remove(Book item) {
        list.remove(item);
        notifyDataSetChanged();
        mBookService.removeReadBook(item);
    }

    public void add(Book item) {
        list.add(item);
        notifyDataSetChanged();
        mBookService.addReadBook(item);
    }

    protected void showDeleteBookDialog(final Book book) {

        DialogCreator.createCommonDialog(mContext, "移除书籍", "确定将《" + book.getName() + "》移出精读区吗？", true, (dialogInterface, i) -> {
            remove(book);
            ToastUtils.showSuccess("已将书籍移出精读区！");
            mReadPresenter.init();
            }, null);

    }




    /**
     * getter方法
     *
     * @return
     */
    public Map<String, Boolean> getIsLoading() {
        return isLoading;
    }
    public boolean isBookLoading(String bookID) {
        return isLoading.get(bookID);
    }










    static class ViewHolder {
        CheckBox cbBookChecked;
        CoverImageView ivBookImg;
        TextView tvBookName;
        BadgeView tvNoReadNum;
        ProgressBar pbLoading;
    }



    //书籍点击监听器
    public interface OnReadCheckedListener {
        void onItemCheckedChange(boolean isChecked);
    }
}
