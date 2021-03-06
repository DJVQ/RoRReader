package com.example.myreadproject8.ui.presenter.book;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.BookcaseStyle;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.BookGroupService;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.activity.IndexActivity;
import com.example.myreadproject8.ui.adapter.bookcase.ReadAdapter;
import com.example.myreadproject8.ui.adapter.bookcase.ReadDragAdapter;
import com.example.myreadproject8.ui.dialog.BookGroupDialog;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.fragment.bookcase.BookcaseFragment;
import com.example.myreadproject8.ui.fragment.bookcase.ReadFragment;
import com.example.myreadproject8.ui.presenter.base.BasePresenter;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.read.notification.NotificationUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.custom.DragSortGridView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by ycq on 2021/4/24 0024
 * describe???
 */
public class ReadPresenter implements BasePresenter {
    private final ReadFragment mReadFragment;
    private final ArrayList<Book> mBooks = new ArrayList<>();//????????????
    private ReadAdapter mReadAdapter;
    private final BookService mBookService;
    private final ChapterService mChapterService;
    private final IndexActivity mMainActivity;
    private Setting mSetting;
    private final List<Book> errorLoadingBooks = new ArrayList<>();
    private int finishLoadBookCount = 0;


    private NotificationUtil notificationUtil;//???????????????




    private ExecutorService es = Executors.newFixedThreadPool(1);//??????/???????????????

    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!App.isDestroy(mMainActivity)) {
                        App.runOnUiThread(() -> mReadAdapter.notifyDataSetChanged());
                        finishLoadBookCount++;
                        mReadFragment.getSrlContent().finishRefresh();
                    }
                    break;
                case 2:
                    mReadFragment.getSrlContent().finishRefresh();
                    break;
                case 3:
                    mReadAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    showErrorLoadingBooks();
                    break;


            }
        }
    };
    //????????????
    public ReadPresenter(ReadFragment readFragment) {
        mReadFragment = readFragment;//??????????????????
        mBookService = BookService.getInstance();//????????????????????????
        mChapterService = ChapterService.getInstance();//????????????????????????
        mMainActivity = (IndexActivity) (mReadFragment.getActivity());//?????????
        mSetting = SysManager.getSetting();//?????????
    }
    @Override
    public void start() {
        mSetting.setBookcaseStyle(BookcaseStyle.threePalaceMode);//
        notificationUtil = NotificationUtil.getInstance();//?????????????????????
        getData();
        //??????????????????????????????????????????
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mReadFragment.getSrlContent().setEnableRefresh(false);
        }
        //??????????????????????????????????????????
        mReadFragment.getSrlContent().setEnableHeaderTranslationContent(false);
        //?????????????????????
        mReadFragment.getSrlContent().setOnRefreshListener(refreshlayout -> initNoReadNum());
        //??????????????????
        mReadFragment.getGvBook().setOnItemLongClickListener((parent, view, position, id) -> false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //???????????????
            mReadFragment.getGvBook().getmScrollView().setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                mReadFragment.getSrlContent().setEnableRefresh(scrollY == 0);

            });
        }
    }


    //????????????
    public void getData() {
        init();
        if (mSetting.isRefreshWhenStart()) {
            mHandler.postDelayed(this::initNoReadNum, 500);
        }
    }

    //?????????
    public void init() {
        initBook();
        if (mBooks.size() == 0) {
            mReadFragment.getGvBook().setVisibility(View.GONE);
            mReadFragment.getLlNoDataTips().setVisibility(View.VISIBLE);
        } else {
            if (mReadAdapter == null) {
                mReadAdapter = new ReadDragAdapter(mMainActivity, R.layout.gridview_book_item, mBooks, this);
                mReadFragment.getGvBook().setNumColumns(3);
                mReadFragment.getGvBook().setDragModel(-1);
                mReadFragment.getGvBook().setTouchClashparent(mMainActivity.getViewPagerMain());
                mReadFragment.getGvBook().setOnDragSelectListener(new DragSortGridView.OnDragSelectListener() {
                    @Override
                    public void onDragSelect(View mirror) {
                        mirror.setScaleX(1.05f);
                        mirror.setScaleY(1.05f);
                    }
                    @Override
                    public void onPutDown(View itemView) {
                    }
                });
                mReadFragment.getGvBook().setAdapter(mReadAdapter);

            } else {
                mReadAdapter.notifyDataSetChanged();
            }
            mReadFragment.getLlNoDataTips().setVisibility(View.GONE);
            mReadFragment.getGvBook().setVisibility(View.VISIBLE);
        }
    }

    //???????????????
    private void initBook() {
        mBooks.clear();
        mBooks.addAll(mBookService.getReadBooks());
        if (mSetting.getSortStyle() == 1) {
            Collections.sort(mBooks, (o1, o2) -> {
                if (o1.getLastReadTime() < o2.getLastReadTime()){
                    return 1;
                }else if (o1.getLastReadTime() > o2.getLastReadTime()){
                    return -1;
                }
                return 0;
            });
        }else if (mSetting.getSortStyle() == 2){
            Collections.sort(mBooks, (o1, o2) -> {
                Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
                return cmp.compare(o1.getName(), o2.getName());
            });
        }
        for (int i = 0; i < mBooks.size(); i++) {
            int sort =  mBooks.get(i).getSortCode();
            if (sort != i + 1) {
                mBooks.get(i).setSortCode(i + 1);
                mBookService.updateEntity(mBooks.get(i));
            }
        }
    }

    //??????????????????
    public void initNoReadNum() {
        errorLoadingBooks.clear();
        finishLoadBookCount = 0;
        for (Book book : mBooks) {
            mReadAdapter.getIsLoading().put(book.getId(), true);
        }
        if (mBooks.size() > 0) {
            mHandler.sendMessage(mHandler.obtainMessage(3));
        }
        for (final Book book : mBooks) {
            if ("????????????".equals(book.getType()) || book.getIsCloseUpdate()) {
                mReadAdapter.getIsLoading().put(book.getId(), false);
                mHandler.sendMessage(mHandler.obtainMessage(1));
                continue;
            }
            Thread update = new Thread(() -> {
                final ArrayList<Chapter> mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                final ReadCrawler mReadCrawler = ReadCrawlerUtil.getReadCrawler(book.getSource());
                CommonApi.getBookChapters(book.getChapterUrl(), mReadCrawler, true, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        int noReadNum = chapters.size() - book.getChapterTotalNum();
                        book.setNoReadNum(Math.max(noReadNum, 0));
                        book.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
                        mReadAdapter.getIsLoading().put(book.getId(), false);
                        mChapterService.updateAllOldChapterData(mChapters, chapters, book.getId());
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                        mBookService.updateEntity(book);
                    }

                    @Override
                    public void onError(Exception e) {
                        mReadAdapter.getIsLoading().put(book.getId(), false);
                        errorLoadingBooks.add(book);
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            });
            es.submit(update);
        }
        App.getmApplication().newThread(() -> {
            while (true) {
                if (finishLoadBookCount == mBooks.size()) {
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                    mHandler.sendMessage(mHandler.obtainMessage(2));
                    break;
                }
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    private void showErrorLoadingBooks() {
        StringBuilder s = new StringBuilder();
        for (Book book : errorLoadingBooks) {
            s.append(book.getName());
            s.append("???");
        }
        if (errorLoadingBooks.size() > 0) {
            s.deleteCharAt(s.lastIndexOf("???"));
            s.append(" ????????????");
            ToastUtils.showError(s.toString());
        }
    }

    /**
     * ??????
     */
    public void destroy() {
        notificationUtil.cancelAll();
        for (int i = 0; i < 13; i++) {
            mHandler.removeMessages(i + 1);
        }
    }

}
