package com.example.myreadproject8.ui.presenter.book.catalog;

import android.app.Activity;
import android.content.Intent;

import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.BookMark;
import com.example.myreadproject8.greendao.service.BookMarkService;
import com.example.myreadproject8.ui.activity.CatalogActivity;
import com.example.myreadproject8.ui.adapter.catalog.BookMarkAdapter;
import com.example.myreadproject8.ui.fragment.catalog.BookMarkFragment;
import com.example.myreadproject8.ui.presenter.base.BasePresenter;

import java.util.ArrayList;

/**
 * created by ycq on 2021/4/18 0018
 * describe：
 */
public class BookMarkPresenter implements BasePresenter {
    private BookMarkFragment mBookMarkFragment;
    private BookMarkService mBookMarkService;
    private ArrayList<BookMark> mBookMarks = new ArrayList<>();
    private BookMarkAdapter mBookMarkAdapter;
    private Book mBook;

    public BookMarkPresenter(BookMarkFragment mBookMarkFragment) {
        this.mBookMarkFragment = mBookMarkFragment;
        mBookMarkService = new BookMarkService();
    }

    @Override
    public void start() {
        mBook = ((CatalogActivity) mBookMarkFragment.getActivity()).getmBook();;
        initBookMarkList();
        mBookMarkFragment.getLvBookmarkList().setOnItemClickListener((parent, view, position, id) -> {
            BookMark bookMark = mBookMarks.get(position);
            int chapterPos = bookMark.getBookMarkChapterNum();
            int pagePos = bookMark.getBookMarkReadPosition();
            Intent intent = new Intent();
            intent.putExtra(APPCONST.CHAPTER_PAGE, new int[]{chapterPos, pagePos});
            mBookMarkFragment.getActivity().setResult(Activity.RESULT_OK, intent);
            mBookMarkFragment.getActivity().finish();
        });

        mBookMarkFragment.getLvBookmarkList().setOnItemLongClickListener((parent, view, position, id) -> {
            if (mBookMarks.get(position) != null) {
                mBookMarkService.deleteBookMark(mBookMarks.get(position));
                initBookMarkList();
            }
            return true;
        });
    }

    private void initBookMarkList() {
        mBookMarks = (ArrayList<BookMark>) mBookMarkService.findBookAllBookMarkByBookId(mBook.getId());
        mBookMarkAdapter = new BookMarkAdapter(mBookMarkFragment.getActivity(), R.layout.listview_chapter_title_item, mBookMarks);
        mBookMarkFragment.getLvBookmarkList().setAdapter(mBookMarkAdapter);
    }

    /**
     * 搜索过滤
     * @param query
     */
    public void startSearch(String query) {
        mBookMarkAdapter.getFilter().filter(query);
        mBookMarkFragment.getLvBookmarkList().setSelection(0);
    }
}