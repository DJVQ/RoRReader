package com.example.myreadproject8.ui.adapter.bookcase;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.presenter.book.BookcasePresenter;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.BadgeView;
import com.example.myreadproject8.widget.cover_image_view.CoverImageView;
import com.example.myreadproject8.widget.custom.DragAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * created by ycq on 2021/4/11 0011
 * describe：
 */
public class BookcaseAdapter extends DragAdapter {

    private final Map<String, Boolean> isLoading = new HashMap<>();
    private final Map<String, Boolean> mCheckMap = new LinkedHashMap<>();
    private int mCheckedCount = 0;
    protected OnBookCheckedListener mListener;
    protected boolean isCheckedAll;
    protected int mResourceId;
    protected ArrayList<Book> list;
    protected Context mContext;
    protected boolean mEditState;
    protected BookService mBookService;
    protected ChapterService mChapterService;
    protected BookcasePresenter mBookcasePresenter;
    protected boolean isGroup;//是否属于分组
    protected String[] menu = {
            App.getMContext().getResources().getString(R.string.menu_book_Top),
            App.getMContext().getResources().getString(R.string.menu_book_download),
            App.getMContext().getResources().getString(R.string.menu_book_cache),
            App.getMContext().getResources().getString(R.string.menu_book_delete),
            App.getMContext().getResources().getString(R.string.menu_read_book_add)
    };

    public BookcaseAdapter(Context context, int textViewResourceId, ArrayList<Book> objects
            , boolean editState, BookcasePresenter bookcasePresenter, boolean isGroup) {
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mEditState = editState;
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mBookcasePresenter = bookcasePresenter;
        this.isGroup = isGroup;
    }

    @Override
    public void onDataModelMove(int from, int to) {
        Book b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            if (!isGroup) {
                list.get(i).setSortCode(i);
            }else {
                list.get(i).setGroupSort(i);
            }
        }
        mBookService.updateBooks(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return !isGroup ? list.get(position).getSortCode() : list.get(position).getGroupSort();    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void remove(Book item) {
        list.remove(item);
        notifyDataSetChanged();
        mBookService.deleteBook(item);
    }

    public void add(Book item) {
        list.add(item);
        notifyDataSetChanged();
        mBookService.addBook(item);
    }


    protected void showDeleteBookDialog(final Book book) {
        if (!isGroup) {
            DialogCreator.createCommonDialog(mContext, "删除书籍", "确定删除《" + book.getName() + "》及其所有缓存吗？",
                    true, (dialogInterface, i) -> {
                        remove(book);
                        ToastUtils.showSuccess("书籍删除成功！");
                        //mBookcasePresenter.init();
                    }, null);
        }else {
            DialogCreator.createCommonDialog(mContext, "删除/移除书籍", "您是希望删除《" + book.getName() + "》及其所有缓存还是从分组中移除该书籍(不会删除书籍)呢？",
                    true, "删除书籍", "从分组中移除",(dialogInterface, i) -> {
                        remove(book);
                        ToastUtils.showSuccess("书籍删除成功！");
                        //mBookcasePresenter.init();
                    }, (dialog, which) -> {
                        book.setGroupId("");
                        mBookService.updateEntity(book);
                        ToastUtils.showSuccess("书籍已从分组中移除！");
                        //mBookcasePresenter.init();
                    });
        }
    }


    /**
     * 设置是否处于编辑状态
     *
     * @param mEditState
     */
    public void setmEditState(boolean mEditState) {
        if (mEditState) {
            mCheckMap.clear();
            for (Book book : list) {
                mCheckMap.put(book.getId(), false);
            }
            mCheckedCount = 0;
        }
        this.mEditState = mEditState;
        notifyDataSetChanged();
    }

    public boolean ismEditState() {
        return mEditState;
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

    public boolean unionChapterCathe(Book book) throws IOException {
        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
        BufferedReader br = null;
        BufferedWriter bw = null;
        bw = new BufferedWriter(new FileWriter(FileUtils.getFile(APPCONST.TXT_BOOK_DIR + book.getName() + ".txt")));
        if (chapters.size() == 0) {
            return false;
        }
        File bookFile = new File(APPCONST.BOOK_CACHE_PATH + book.getId());
        if (!bookFile.exists()) {
            return false;
        }
        for (Chapter chapter : chapters) {
            if (ChapterService.isChapterCached(chapter.getBookId(), chapter.getTitle())) {
                bw.write("\t" + chapter.getNumber()+"."+chapter.getTitle());
                bw.newLine();
                br = new BufferedReader(new FileReader(APPCONST.BOOK_CACHE_PATH + book.getId()
                        + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY));
                String line = null;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                br.close();
            }
        }
        bw.flush();
        bw.close();
        return true;
    }


    //设置点击切换
    public void setCheckedBook(String bookId) {
        boolean isSelected = mCheckMap.get(bookId);
        if (isSelected) {
            mCheckMap.put(bookId, false);
            --mCheckedCount;
        } else {
            mCheckMap.put(bookId, true);
            ++mCheckedCount;
        }
        notifyDataSetChanged();
    }

    //全选
    public void setCheckedAll(boolean isChecked) {
        mCheckedCount = isChecked ? mCheckMap.size() : 0;
        for (String bookId : mCheckMap.keySet()) {
            mCheckMap.put(bookId, isChecked);
        }
        mListener.onItemCheckedChange(true);
        notifyDataSetChanged();
    }

    public boolean getBookIsChecked(String bookId) {
        return mCheckMap.get(bookId);
    }

    public int getmCheckedCount() {
        return mCheckedCount;
    }

    public int getmCheckableCount() {
        return mCheckMap.size();
    }

    public boolean isCheckedAll() {
        return isCheckedAll;
    }

    public void setIsCheckedAll(boolean isCheckedAll) {
        this.isCheckedAll = isCheckedAll;
    }

    public List<Book> getSelectBooks() {
        List<Book> mSelectBooks = new ArrayList<>();
        for (String bookId : mCheckMap.keySet()) {
            if (mCheckMap.get(bookId)) {
                mSelectBooks.add(mBookService.getBookById(bookId));
            }
        }
        return mSelectBooks;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    /*******************************************缓存书籍*********************************************************/
    private int selectedIndex;//对话框选择下标

    protected void downloadBook(final Book book) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        if ("本地书籍".equals(book.getType())) {
            ToastUtils.showWarring("《" + book.getName() + "》是本地书籍，不能缓存");
            return;
        }
        final int[] begin = new int[1];
        final int[] end = new int[1];
        MyAlertDialog.build(mContext)
                .setTitle("缓存书籍")
                .setSingleChoiceItems(mContext.getResources().getStringArray(R.array.download), selectedIndex,
                        (dialog, which) -> selectedIndex = which).setNegativeButton("取消", ((dialog, which) -> dialog.dismiss())).setPositiveButton("确定",
                (dialog, which) -> {
                    switch (selectedIndex) {
                        case 0:
                            begin[0] = book.getHistoryChapterNum();
                            end[0] = book.getHistoryChapterNum() + 50;
                            break;
                        case 1:
                            begin[0] = book.getHistoryChapterNum() - 50;
                            end[0] = book.getHistoryChapterNum() + 50;
                            break;
                        case 2:
                            begin[0] = book.getHistoryChapterNum();
                            end[0] = 99999;
                            break;
                        case 3:
                            begin[0] = 0;
                            end[0] = 99999;
                            break;
                    }
                    Thread downloadThread = new Thread(() -> {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                        mBookcasePresenter.addDownload(book, chapters, begin[0], end[0], false);
                    });
                    mBookcasePresenter.getEs().submit(downloadThread);
                }).show();
    }

    static class ViewHolder {
        CheckBox cbBookChecked;
        CoverImageView ivBookImg;
        TextView tvBookName;
        BadgeView tvNoReadNum;
        ProgressBar pbLoading;
    }

    public void setOnBookCheckedListener(OnBookCheckedListener listener) {
        mListener = listener;
    }

    //书籍点击监听器
    public interface OnBookCheckedListener {
        void onItemCheckedChange(boolean isChecked);
    }
}
