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
 * describe：
 */
public class ReadPresenter implements BasePresenter {
    private final ReadFragment mReadFragment;
    private final ArrayList<Book> mBooks = new ArrayList<>();//书目数组
    private ReadAdapter mReadAdapter;
    private final BookService mBookService;
    private final ChapterService mChapterService;
    private final IndexActivity mMainActivity;
    private Setting mSetting;
    private final List<Book> errorLoadingBooks = new ArrayList<>();
    private int finishLoadBookCount = 0;


    private NotificationUtil notificationUtil;//通知工具类




    private ExecutorService es = Executors.newFixedThreadPool(1);//更新/下载线程池

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
    //构造方法
    public ReadPresenter(ReadFragment readFragment) {
        mReadFragment = readFragment;//全部书籍书架
        mBookService = BookService.getInstance();//书籍数据库服务类
        mChapterService = ChapterService.getInstance();//章节数据库服务类
        mMainActivity = (IndexActivity) (mReadFragment.getActivity());//主活动
        mSetting = SysManager.getSetting();//设置类
    }
    @Override
    public void start() {
        mSetting.setBookcaseStyle(BookcaseStyle.threePalaceMode);//
        notificationUtil = NotificationUtil.getInstance();//初始化通知工具
        getData();
        //是否启用下拉刷新（默认启用）
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mReadFragment.getSrlContent().setEnableRefresh(false);
        }
        //设置是否启用内容视图拖动效果
        mReadFragment.getSrlContent().setEnableHeaderTranslationContent(false);
        //设置刷新监听器
        mReadFragment.getSrlContent().setOnRefreshListener(refreshlayout -> initNoReadNum());
        //长按事件监听
        mReadFragment.getGvBook().setOnItemLongClickListener((parent, view, position, id) -> false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //滑动监听器
            mReadFragment.getGvBook().getmScrollView().setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                mReadFragment.getSrlContent().setEnableRefresh(scrollY == 0);

            });
        }
    }


    //获取数据
    public void getData() {
        init();
        if (mSetting.isRefreshWhenStart()) {
            mHandler.postDelayed(this::initNoReadNum, 500);
        }
    }

    //初始化
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

    //初始化书籍
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

    //检查书籍更新
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
            if ("本地书籍".equals(book.getType()) || book.getIsCloseUpdate()) {
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
     * 显示更新失败的书籍信息
     */
    private void showErrorLoadingBooks() {
        StringBuilder s = new StringBuilder();
        for (Book book : errorLoadingBooks) {
            s.append(book.getName());
            s.append("、");
        }
        if (errorLoadingBooks.size() > 0) {
            s.deleteCharAt(s.lastIndexOf("、"));
            s.append(" 更新失败");
            ToastUtils.showError(s.toString());
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        notificationUtil.cancelAll();
        for (int i = 0; i < 13; i++) {
            mHandler.removeMessages(i + 1);
        }
    }

}
