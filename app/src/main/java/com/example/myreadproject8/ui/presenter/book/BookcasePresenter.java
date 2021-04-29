package com.example.myreadproject8.ui.presenter.book;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.BookcaseStyle;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.BookGroup;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.BookGroupService;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.activity.IndexActivity;
import com.example.myreadproject8.ui.activity.MainActivity;
import com.example.myreadproject8.ui.activity.file.AddLocalBookActivity;
import com.example.myreadproject8.ui.activity.search.SearchBookActivity;
import com.example.myreadproject8.ui.adapter.bookcase.BookcaseAdapter;
import com.example.myreadproject8.ui.adapter.bookcase.BookcaseDetailedAdapter;
import com.example.myreadproject8.ui.adapter.bookcase.BookcaseDragAdapter;
import com.example.myreadproject8.ui.dialog.BookGroupDialog;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.fragment.bookcase.BookcaseFragment;
import com.example.myreadproject8.ui.presenter.base.BasePresenter;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.read.notification.NotificationUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.PermissionsChecker;
import com.example.myreadproject8.widget.custom.DragSortGridView;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by ycq on 2021/4/12 0012
 * describe：书架后端操作
 */
public class BookcasePresenter implements BasePresenter {

    private final BookcaseFragment mBookcaseFragment;//fragment
    private final ArrayList<Book> mBooks = new ArrayList<>();//书目数组
    private BookcaseAdapter mBookcaseAdapter;//adapter
    private final BookService mBookService;
    private final ChapterService mChapterService;
    private final BookGroupService mBookGroupService;
    private final IndexActivity mMainActivity;
    private Setting mSetting;
    private final List<Book> errorLoadingBooks = new ArrayList<>();
    private int finishLoadBookCount = 0;
    private ExecutorService es = Executors.newFixedThreadPool(1);//更新/下载线程池

    public ExecutorService getEs() {
        return es;
    }

    private NotificationUtil notificationUtil;//通知工具类
    private String downloadingBook;//正在下载的书名
    private String downloadingChapter;//正在下载的章节名
    private boolean isDownloadFinish = true;//单本书是否下载完成
    private static boolean isStopDownload = true;//是否停止下载
    private int curCacheChapterNum;//当前下载的章节数
    private int needCacheChapterNum;//需要下载的章节数
    private int successCathe;//成功章节数
    private int errorCathe;//失败章节数
    private int tempCacheChapterNum;//上次下载的章节数
    private int tempCount;//下载超时时间
    private int downloadInterval = 150;//下载间隔
    private Runnable sendDownloadNotification;//发送通知的线程
    private boolean isFirstRefresh = true;//是否首次进入刷新
    private boolean isGroup;
    private IndexActivity.OnGroupChangeListener ogcl;
    private final BookGroupDialog mBookGroupDia;

    private int count = 0;

    public static final String CANCEL_ACTION = "cancelAction";


    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!App.isDestroy(mMainActivity)) {
                        App.runOnUiThread(() -> mBookcaseAdapter.notifyDataSetChanged());
                        finishLoadBookCount++;//增加完成加载数量
                        mBookcaseFragment.getSrlContent().finishRefresh();//去除smartrefreshlayout的加载状态
                    }
                    break;
                case 2:
                    mBookcaseFragment.getSrlContent().finishRefresh();
                    break;
                case 3:
                    mBookcaseAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    showErrorLoadingBooks();//显示更新失败的书籍信息
                    if (App.isApkInDebug(mMainActivity)) {//判断若处于Debug状态
                        if (isFirstRefresh) {//且是第一次刷新
                            initBook();//初始化书籍
                            isFirstRefresh = false;
                        }
                        downloadAll(false, false);
                    }
                    break;
                case 5:
                    if (!App.isDestroy(mMainActivity)) {
                        App.runOnUiThread(() -> {
                            ToastUtils.showError("加载超时");
                        });
                    }
                    mBookcaseFragment.getSrlContent().finishRefresh();
                    break;
                case 6:
                    break;
                case 7:
                    init();
                    break;
                case 8:
                    sendNotification();
                    break;
                case 9:
                    isDownloadFinish = true;//下载完成
                    break;
                case 10:
                    mBookcaseFragment.getTvDownloadTip().setText("正在初始化缓存任务...");
                    mBookcaseFragment.getPbDownload().setProgress(0);
                    mBookcaseFragment.getRlDownloadTip().setVisibility(View.VISIBLE);
                    break;
                case 11:
                    ToastUtils.showInfo("正在后台缓存书籍，具体进度可查看通知栏！");
                    notificationUtil.requestNotificationPermissionDialog(mMainActivity);
                    break;
            }
        }
    };

    //构造方法
    public BookcasePresenter(BookcaseFragment bookcaseFragment) {
        mBookcaseFragment = bookcaseFragment;//全部书籍书架
        mBookService = BookService.getInstance();//书籍数据库服务类
        mChapterService = ChapterService.getInstance();//章节数据库服务类
        mBookGroupService = BookGroupService.getInstance();//书记分组服务类
        mMainActivity = (IndexActivity) (mBookcaseFragment.getActivity());//主活动
        mSetting = SysManager.getSetting();//设置类
        mBookGroupDia = new BookGroupDialog(mMainActivity);//分组对话框
    }

    //启动
    @Override
    public void start() {
        mSetting.setBookcaseStyle(BookcaseStyle.listMode);//列表模式,暂时只有这一种
        notificationUtil = NotificationUtil.getInstance();//初始化通知工具
        sendDownloadNotification = this::sendNotification;//发送下载通知


        getData();//获取数据

        //是否启用下拉刷新（默认启用）
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mBookcaseFragment.getSrlContent().setEnableRefresh(false);
        }
        //设置是否启用内容视图拖动效果
        mBookcaseFragment.getSrlContent().setEnableHeaderTranslationContent(false);
        //设置刷新监听器
        mBookcaseFragment.getSrlContent().setOnRefreshListener(refreshlayout -> initNoReadNum());
        //搜索按钮监听器
        mBookcaseFragment.getLlNoDataTips().setOnClickListener(view -> {
            Intent intent = new Intent(mBookcaseFragment.getContext(), SearchBookActivity.class);
            mBookcaseFragment.startActivity(intent);
        });

        //长按事件监听
        mBookcaseFragment.getGvBook().setOnItemLongClickListener((parent, view, position, id) -> false);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //滑动监听器
            mBookcaseFragment.getGvBook().getmScrollView().setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (!mBookcaseAdapter.ismEditState()) {
                    mBookcaseFragment.getSrlContent().setEnableRefresh(scrollY == 0);
                }
            });
        }

        //全选监听器
        mBookcaseFragment.getmCbSelectAll().setOnClickListener(v -> {
            //设置全选状态
            boolean isChecked = mBookcaseFragment.getmCbSelectAll().isChecked();
            mBookcaseAdapter.setCheckedAll(isChecked);
        });

        //删除监听器
        mBookcaseFragment.getmBtnDelete().setOnClickListener(v -> {
            if (!isGroup) {
                DialogCreator.createCommonDialog(mMainActivity, "批量删除书籍",
                        "确定要删除这些书籍吗？", true, (dialog, which) -> {
                            for (Book book : mBookcaseAdapter.getSelectBooks()) {
                                mBookService.deleteBook(book);
                            }
                            ToastUtils.showSuccess("书籍删除成功！");
                            init();
                            mBookcaseAdapter.setCheckedAll(false);
                        }, null);
            }else {
                DialogCreator.createCommonDialog(mMainActivity, "批量删除/移除书籍",
                        "要删除这些书籍及其所有缓存还是从分组中移除(不会删除书籍)呢？", true,
                        "删除书籍", "从分组中移除" ,(dialog, which) -> {
                            for (Book book : mBookcaseAdapter.getSelectBooks()) {
                                mBookService.deleteBook(book);
                            }
                            ToastUtils.showSuccess("书籍删除成功！");
                            init();
                            mBookcaseAdapter.setCheckedAll(false);
                        }, (dialog, which) -> {
                            for (Book book : mBookcaseAdapter.getSelectBooks()) {
                                book.setGroupId("");
                                mBookService.updateEntity(book);
                            }
                            ToastUtils.showSuccess("书籍已从分组中移除！");
                            init();
                            mBookcaseAdapter.setCheckedAll(false);
                        });
            }
        });

        //加入分组监听器
        mBookcaseFragment.getmBtnAddGroup().setOnClickListener(v -> {
            mBookGroupDia.addGroup(mBookcaseAdapter.getSelectBooks(), new BookGroupDialog.OnGroup(){
                @Override
                public void change() {
                    init();
                    mBookcaseAdapter.setCheckedAll(false);
                    if (hasOnGroupChangeListener())
                        ogcl.onChange();
                }

                @Override
                public void addGroup() {
                    mBookcaseFragment.getmBtnAddGroup().performClick();
                }
            });
        });
    }


    /**
     * description: 获取数据
     * 先初始化DragSortGridView,再更新书籍信息
     */
    public void getData() {
        init();
        if (mSetting.isRefreshWhenStart()) {
            mHandler.postDelayed(this::initNoReadNum, 500);
        }
    }

    /**
     * description:初始化
     * 先初始化书籍信息,若书籍数目不为0则初始化DragSortGridView,并将其显示出来
     */
    public void init() {
        initBook();//初始化书籍信息
        if (mBooks.size() == 0) {//没有找到书籍
            mBookcaseFragment.getGvBook().setVisibility(View.GONE);
            mBookcaseFragment.getLlNoDataTips().setVisibility(View.VISIBLE);
        } else {
            if (mBookcaseAdapter == null) {//初始化adapter
                mBookcaseAdapter = new BookcaseDetailedAdapter(mMainActivity, R.layout.gridview_book_detailed_item, mBooks, false, this, isGroup);
                mBookcaseFragment.getGvBook().setNumColumns(1);
                mBookcaseAdapter.setOnBookCheckedListener(isChecked -> {//设置书籍选定监听器
                    changeCheckedAllStatus();
                    //设置删除和加入分组按钮是否可用
                    setBtnClickable(mBookcaseAdapter.getmCheckedCount() > 0);
                });
                mBookcaseFragment.getGvBook().setDragModel(-1);//设定拖动策略为长按
                mBookcaseFragment.getGvBook().setTouchClashparent(mMainActivity.getViewPagerMain());
                mBookcaseFragment.getGvBook().setOnDragSelectListener(new DragSortGridView.OnDragSelectListener() {
                    @Override
                    public void onDragSelect(View mirror) {//被选中
                        mirror.setBackgroundColor(mMainActivity.getResources().getColor(R.color.colorBackground));
                        mirror.setScaleY(1.05f);
                    }

                    @Override
                    public void onPutDown(View itemView) {
                    }
                });
                mBookcaseFragment.getGvBook().setAdapter(mBookcaseAdapter);//安装适配器
            } else {
                mBookcaseAdapter.notifyDataSetChanged();
            }
            mBookcaseFragment.getLlNoDataTips().setVisibility(View.GONE);
            mBookcaseFragment.getGvBook().setVisibility(View.VISIBLE);//显示书籍
        }
    }

    /**
     * description:初始化书籍信息
     * 先初始化书籍分组信息,再通过书籍分组找到该分组的所有书并加入mBooks,在对mBooks进行排序,设置排序编码,再更新数据库
     */
    private void initBook() {
        mBooks.clear();
        String curBookGroupId = SharedPreUtils.getInstance().getString(mMainActivity.getString(R.string.curBookGroupId), "");
        BookGroup bookGroup = mBookGroupService.getGroupById(curBookGroupId);
        if (bookGroup == null) {
            curBookGroupId = "";
            SharedPreUtils.getInstance().putString(mMainActivity.getString(R.string.curBookGroupId), "");
            SharedPreUtils.getInstance().putString(mMainActivity.getString(R.string.curBookGroupName), "");
            if (hasOnGroupChangeListener())
                ogcl.onChange();
        }
        isGroup = !"".equals(curBookGroupId);
        if (mBookcaseAdapter != null) {
            mBookcaseAdapter.setGroup(isGroup);
        }
        mBooks.addAll(mBookService.getGroupBooks(curBookGroupId));//加入到mBooks

        if (mSetting.getSortStyle() == 1) {//按时间排序
            Collections.sort(mBooks, (o1, o2) -> {
                if (o1.getLastReadTime() < o2.getLastReadTime()){
                    return 1;
                }else if (o1.getLastReadTime() > o2.getLastReadTime()){
                    return -1;
                }
                return 0;
            });
        }else if (mSetting.getSortStyle() == 2){//按名称排序
            Collections.sort(mBooks, (o1, o2) -> {
                Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
                return cmp.compare(o1.getName(), o2.getName());
            });
        }

        for (int i = 0; i < mBooks.size(); i++) {//设置排序编码
            int sort = !isGroup ? mBooks.get(i).getSortCode() : mBooks.get(i).getGroupSort();
            if (sort != i + 1) {
                if (!isGroup) {
                    mBooks.get(i).setSortCode(i + 1);
                }else {
                    mBooks.get(i).setGroupSort(i + 1);
                }
                mBookService.updateEntity(mBooks.get(i));//更新数据库
            }
        }
    }

    /**
     * description:更新书籍信息
     * 首先初始化errorLoadingBooks及finishLoadBookCount再将所有书籍设置为加载状态,若书籍数目大于0则通知fragment更新
     * 对每一本书若为本地书籍或者禁止更新状态,则直接更新layout,
     * 否则就每本书new一个线程进行搜索书籍章节信息,搜到则更新,否则errorLoadingBooks+1,将这段操作加入线程池,最后向handler发送信息,
     * 更新或显示状态
     */
    public void initNoReadNum() {
        errorLoadingBooks.clear();//清除掉加载错误书籍
        finishLoadBookCount = 0;
        for (Book book : mBooks) {//将书籍设置为加载状态
            mBookcaseAdapter.getIsLoading().put(book.getId(), true);
        }
        if (mBooks.size() > 0) {//数目大于0则更新
            mHandler.sendMessage(mHandler.obtainMessage(3));
        }
        for (final Book book : mBooks) {
            if ("本地书籍".equals(book.getType()) || book.getIsCloseUpdate()) {//如果是本地书籍或者关闭更新
                mBookcaseAdapter.getIsLoading().put(book.getId(), false);
                mHandler.sendMessage(mHandler.obtainMessage(1));//发送信息
                continue;
            }
            Thread update = new Thread(() -> {//new一个线程去加载当前书籍
                final ArrayList<Chapter> mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());//找到所有章节
                final ReadCrawler mReadCrawler = ReadCrawlerUtil.getReadCrawler(book.getSource());//获取书源爬虫
                CommonApi.getBookChapters(book.getChapterUrl(), mReadCrawler, true, new ResultCallback() {//从网络获取章节
                    @Override
                    public void onFinish(Object o, int code) {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;//获取爬取的信息
                        int noReadNum = chapters.size() - book.getChapterTotalNum();//获取没有阅读的章节数目
                        book.setNoReadNum(Math.max(noReadNum, 0));//设置...
                        book.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());//获取最新章节标题
                        mBookcaseAdapter.getIsLoading().put(book.getId(), false);//加载完成
                        mChapterService.updateAllOldChapterData(mChapters, chapters, book.getId());//更新章节信息
                        mHandler.sendMessage(mHandler.obtainMessage(1));//发送信息
                        mBookService.updateEntity(book);//更新书籍实体信息
                    }

                    @Override
                    public void onError(Exception e) {//若找不到该书
                        mBookcaseAdapter.getIsLoading().put(book.getId(), false);
                        errorLoadingBooks.add(book);
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            });
            es.submit(update);//加入线程池
        }
        App.getmApplication().newThread(() -> {
            if(count>2){
                System.out.println("cnmdcsl");
            }
            while (true) {

                if (finishLoadBookCount == mBooks.size() ) {
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                    mHandler.sendMessage(mHandler.obtainMessage(2));
                    break;
                }
            }
        });
    }

    /**
     * description:显示更新失败的书籍信息
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
     * description:菜单设置选择事件
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_group:
                mBookcaseFragment.getmBookcasePresenter()//展示分组菜单
                        .showBookGroupMenu(mMainActivity.findViewById(R.id.action_change_group));
                return true;
            case R.id.action_edit:
                editBookcase(true);//编辑书架
                return true;
            case R.id.action_group_man:
                showGroupManDia();
                return true;
            case R.id.action_add_local:
                Intent fileSystemIntent = new Intent(mMainActivity, AddLocalBookActivity.class);
                mMainActivity.startActivity(fileSystemIntent);
                break;
            case R.id.action_download_all:
                if (!SharedPreUtils.getInstance().getBoolean(mMainActivity.getString(R.string.isReadDownloadAllTip), false)) {
                    DialogCreator.createCommonDialog(mMainActivity, "一键缓存",
                            mMainActivity.getString(R.string.all_cathe_tip), true,
                            (dialog, which) -> {
                                downloadAll(true, true);
                                SharedPreUtils.getInstance().putBoolean(mMainActivity.getString(R.string.isReadDownloadAllTip), true);
                            }, null);
                } else {
                    downloadAll(true, true);
                }
                return true;
        }
        return false;
    }

    /**
     * description:显示书籍分组菜单
     * 先初始化弹出菜单,再从mBookGroupDia找到菜单项并填入,并为菜单项添加点击事件监听器并展示出来
     * 当菜单项被选择时向SharedPreferences（RoRReader_pref)加入分组id和分组名,并更新书架
     */
    public void showBookGroupMenu(View view) {
        mBookGroupDia.initBookGroups(false);
        PopupMenu popupMenu = new PopupMenu(mMainActivity, view, Gravity.END);
        popupMenu.getMenu().add(0, 0, 0, "所有书籍");
        for (int i = 0; i < mBookGroupDia.getmGroupNames().length; i++) {
            popupMenu.getMenu().add(0, 0, i + 1, mBookGroupDia.getmGroupNames()[i]);
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {//添加点击监听器
            String curBookGroupId = "";
            String curBookGroupName = "";
            if (menuItem.getOrder() > 0) {
                curBookGroupId = mBookGroupDia.getmBookGroups().get(menuItem.getOrder() - 1).getId();
                curBookGroupName = mBookGroupDia.getmBookGroups().get(menuItem.getOrder() - 1).getName();
            }
            SharedPreUtils.getInstance().putString(mMainActivity.getString(R.string.curBookGroupId), curBookGroupId);
            SharedPreUtils.getInstance().putString(mMainActivity.getString(R.string.curBookGroupName), curBookGroupName);
            ogcl.onChange();//分组管理器监听器改变
            init();//初始化书架
            return true;
        });
        popupMenu.show();
    }

    /**
     * description: 编辑书架
     * 先判断是否可以编辑
     * @param isEdit
     */
    private void editBookcase(boolean isEdit) {
        if (isEdit) {//若进行编辑
            if (canEditBookcase()) {//若能编辑
                mBookcaseFragment.getSrlContent().setEnableRefresh(false);
                mBookcaseAdapter.setmEditState(true);//adapter设置编辑状态
                if (mSetting.getSortStyle() == 0) {//若处于手动排序
                    mBookcaseFragment.getGvBook().setDragModel(DragSortGridView.DRAG_BY_LONG_CLICK);
                }
                mBookcaseFragment.getRlBookEdit().setVisibility(View.VISIBLE);//显示编辑View
                mMainActivity.initMenuAnim();//初始化菜单动画
                //运行菜单动画
                mBookcaseFragment.getRlBookEdit().startAnimation(mMainActivity.getmBottomInAnim());
                setBtnClickable(false);//初始化按钮点击
                changeCheckedAllStatus();//全选按钮状态
                mBookcaseAdapter.notifyDataSetChanged();//更新
            } else {
                ToastUtils.showWarring("当前无任何书籍，无法编辑书架!");
            }
        } else {
            if (mBookcaseFragment.getGvBook().getmScrollView().getScrollY() == 0
                    && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBookcaseFragment.getSrlContent().setEnableRefresh(true);
            }
            mBookcaseFragment.getGvBook().setDragModel(-1);
            mBookcaseAdapter.setmEditState(false);
            mBookcaseFragment.getRlBookEdit().setVisibility(View.GONE);
            mMainActivity.initMenuAnim();
            mBookcaseFragment.getRlBookEdit().startAnimation(mMainActivity.getmBottomOutAnim());
            mBookcaseAdapter.notifyDataSetChanged();
        }
    }

    public boolean canEditBookcase(){
        return mBooks.size() > 0;
    }

    /**
     * description:分组管理对话框
     */
    private void showGroupManDia() {
        MyAlertDialog.build(mMainActivity)
                .setTitle("分组管理")
                .setItems(mMainActivity.getResources().getStringArray(R.array.group_man)
                        , (dialog, which) -> {
                            mBookGroupDia.initBookGroups(false);
                            switch (which){
                                case 0:
                                    mBookGroupDia.showAddOrRenameGroupDia(false, false, 0, new BookGroupDialog.OnGroup() {
                                        @Override
                                        public void change() {
                                            ogcl.onChange();
                                        }

                                        @Override
                                        public void addGroup() {
                                            mBookcaseFragment.getmBtnAddGroup().performClick();
                                        }
                                    });
                                    break;
                                case 1:
                                    mBookGroupDia.showSelectGroupDia((dialog1, which1) -> {
                                        mBookGroupDia.showAddOrRenameGroupDia(true,false, which1, new BookGroupDialog.OnGroup() {
                                            @Override
                                            public void change() {
                                                ogcl.onChange();
                                            }

                                            @Override
                                            public void addGroup() {
                                                mBookcaseFragment.getmBtnAddGroup().performClick();
                                            }
                                        });
                                    });
                                    break;
                                case 2:
                                    mBookGroupDia.showDeleteGroupDia(new BookGroupDialog.OnGroup() {
                                        @Override
                                        public void change() {
                                            ogcl.onChange();
                                            init();
                                        }
                                    });
                                    break;
                            }
                        }).show();
    }

    //分组切换监听器
    public void addOnGroupChangeListener(IndexActivity.OnGroupChangeListener ogcl){
        this.ogcl = ogcl;
    }

    //是否有分组切换监听器
    public boolean hasOnGroupChangeListener(){
        return this.ogcl != null;
    }

/**********************************************缓存书籍***************************************************************/
    /**
     * 缓存所有书籍
     */
    private void downloadAll(boolean isDownloadAllChapters, boolean isFromUser) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        if (mBooks.size() == 0) {
            if (isFromUser)
                ToastUtils.showWarring("当前书架没有任何书籍，无法一键缓存！");
            return;
        }
        App.getmApplication().newThread(() -> {
            ArrayList<Book> needDownloadBooks = new ArrayList<>();
            for (Book book : mBooks) {
                //if (!LocalBookSource.pinshu.toString().equals(book.getSource()) && !"本地书籍".equals(book.getType())
                if (!"本地书籍".equals(book.getType())
                        && book.getIsDownLoadAll()) {
                    needDownloadBooks.add(book);
                }
            }
            if (needDownloadBooks.size() == 0) {
                if (isFromUser)
                    ToastUtils.showWarring("当前书架书籍不支持/已关闭(可在设置开启)一键缓存！");
                return;
            }
            if (isDownloadAllChapters) {
                mHandler.sendEmptyMessage(11);
            }
            downloadFor:
            for (final Book book : needDownloadBooks) {
                isDownloadFinish = false;
                Thread downloadThread = new Thread(() -> {//新开一条线程用于下载
                    //暂存章节数组
                    ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                    int end;
                    if (isDownloadAllChapters) {
                        end = chapters.size();
                    } else {
                        end = book.getHistoryChapterNum() + 5;
                    }
                    addDownload(book, chapters,
                            book.getHistoryChapterNum(), end, true);
                });
                es.submit(downloadThread);
                do {
                    try {
                        Thread.sleep(downloadInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isStopDownload) {
                        break downloadFor;
                    }
                } while (!isDownloadFinish);
            }
            if (isDownloadAllChapters && !isStopDownload) {
                //通知
                Intent mainIntent = new Intent(mMainActivity, IndexActivity.class);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(mMainActivity, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                        .setSmallIcon(R.drawable.ic_download)
                        //通知栏大图标
                        .setLargeIcon(BitmapFactory.decodeResource(App.getApplication().getResources(), R.mipmap.ic_launcher))
                        .setOngoing(false)
                        //点击通知后自动清除
                        .setAutoCancel(true)
                        .setContentTitle("缓存完成")
                        .setContentText("书籍一键缓存完成！")
                        .setContentIntent(mainPendingIntent)
                        .build();
                notificationUtil.notify(1002, notification);
            }
        });
    }

    /**
     * 添加下载
     *
     * @param book
     * @param mChapters
     * @param begin
     * @param end
     */
    public void addDownload(final Book book, final ArrayList<Chapter> mChapters, int begin, int end, boolean isDownloadAll) {
        if ("本地书籍".equals(book.getType())) {
            ToastUtils.showWarring("《" + book.getName() + "》是本地书籍，不能缓存");
            return;
        }
        if (mChapters.size() == 0) {
            if (!isDownloadAll) {
                ToastUtils.showWarring("《" + book.getName() + "》章节目录为空，缓存失败，请刷新后重试");
            }
            return;
        }
        if (SysManager.getSetting().getCatheGap() != 0) {
            downloadInterval = SysManager.getSetting().getCatheGap();
        }
        //取消之前下载
        if (!isDownloadAll) {
            if (!isStopDownload) {
                isStopDownload = true;
                try {
                    Thread.sleep(2 * downloadInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //mHandler.sendMessage(mHandler.obtainMessage(10));
        downloadingBook = book.getName();
        final int finalBegin = Math.max(0, begin);
        final int finalEnd = Math.min(end, mChapters.size());
        needCacheChapterNum = finalEnd - finalBegin;
        curCacheChapterNum = 0;
        tempCacheChapterNum = 0;
        successCathe = 0;
        errorCathe = 0;
        isStopDownload = false;
        ArrayList<Chapter> needDownloadChapters = new ArrayList<>();
        for (int i = finalBegin; i < finalEnd; i++) {
            final Chapter chapter = mChapters.get(i);
            if (StringHelper.isEmpty(chapter.getContent())) {
                needDownloadChapters.add(chapter);
            }
        }
        needCacheChapterNum = needDownloadChapters.size();
        if (!isDownloadAll && needCacheChapterNum > 0) {
            mHandler.sendEmptyMessage(11);
        }
        mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);//发送信息
        for (Chapter chapter : needDownloadChapters) {
            getChapterContent(book, chapter, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    downloadingChapter = chapter.getTitle();
                    mChapterService.saveOrUpdateChapter(chapter, (String) o);
                    successCathe++;
                    curCacheChapterNum++;
                }

                @Override
                public void onError(Exception e) {
                    curCacheChapterNum++;
                    errorCathe++;
                }
            });
            try {
                Thread.sleep(downloadInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (curCacheChapterNum == needCacheChapterNum) {
                if (!isDownloadAll) {
                    isStopDownload = true;
                }
                mHandler.sendMessage(mHandler.obtainMessage(9));
            }
            if (isStopDownload) {
                break;
            }
        }
        if (!isDownloadAll) {
            if (curCacheChapterNum == needCacheChapterNum) {
                ToastUtils.showInfo("《" + book.getName() + "》" + mMainActivity.getString(R.string.download_already_all_tips));
            }
        }
    }


    /**
     * 获取章节内容
     *
     * @param
     * @param
     */
    private void getChapterContent(Book mBook, final Chapter chapter, final ResultCallback resultCallback) {
        if (StringHelper.isEmpty(chapter.getBookId())) {
            chapter.setBookId(mBook.getId());
        }
        ReadCrawler mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, resultCallback);
    }

    /**
     * 发送通知
     */
    private void sendNotification() {
        if (curCacheChapterNum == needCacheChapterNum) {//下载完成
            mHandler.sendEmptyMessage(9);
            notificationUtil.cancelAll();//移除通知栏通知
            return;
        } else {
            Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                    .setSmallIcon(R.drawable.ic_download)
                    //通知栏大图标
                    .setLargeIcon(BitmapFactory.decodeResource(App.getApplication().getResources(), R.mipmap.ic_launcher))
                    .setOngoing(true)
                    //点击通知后自动清除
                    .setAutoCancel(true)
                    .setContentTitle("正在下载：" + downloadingBook +
                            "[" + curCacheChapterNum + "/" + needCacheChapterNum + "]")
                    .setContentText(downloadingChapter == null ? "  " : downloadingChapter)
                    .addAction(R.drawable.ic_stop_black, "停止",
                            notificationUtil.getChancelPendingIntent(cancelDownloadReceiver.class))
                    .build();
            notificationUtil.notify(1000, notification);//启动Notification
        }
        if (tempCacheChapterNum < curCacheChapterNum) {
            tempCount = 1500 / downloadInterval;//下载超时时间设为10毫秒
            tempCacheChapterNum = curCacheChapterNum;
        } else if (tempCacheChapterNum == curCacheChapterNum) {
            tempCount--;
            if (tempCount == 0) {
                isDownloadFinish = true;
                notificationUtil.cancel(1000);
                return;
            }
        }
        mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);//发送通知
    }

    public static class cancelDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo 跳转之前要处理的逻辑
            if (CANCEL_ACTION.equals(intent.getAction())) {
                isStopDownload = true;
            }
        }
    }



    /*****************************************用于返回按钮判断*************************************/
    /**
     * 判断是否处于编辑状态
     *
     * @return
     */
    public boolean ismEditState() {
        if (mBookcaseAdapter != null) {
            return mBookcaseAdapter.ismEditState();
        }
        return false;
    }

    /**
     * 取消编辑状态
     */
    public void cancelEdit() {
        editBookcase(false);
    }

    /**
     * 销毁
     */
    public void destroy() {
        notificationUtil.cancelAll();
        mHandler.removeCallbacks(sendDownloadNotification);
        for (int i = 0; i < 13; i++) {
            mHandler.removeMessages(i + 1);
        }
    }


    //编辑状态下下方按钮
    private void setBtnClickable(boolean isClickable) {
        mBookcaseFragment.getmBtnDelete().setEnabled(isClickable);
        mBookcaseFragment.getmBtnDelete().setClickable(isClickable);
        mBookcaseFragment.getmBtnAddGroup().setEnabled(isClickable);
        mBookcaseFragment.getmBtnAddGroup().setClickable(isClickable);
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        //设置是否全选
        if (mBookcaseAdapter.getmCheckedCount() == mBookcaseAdapter.getmCheckableCount()) {
            mBookcaseAdapter.setIsCheckedAll(true);
        } else if (mBookcaseAdapter.isCheckedAll()) {
            mBookcaseAdapter.setIsCheckedAll(false);
        }
        mBookcaseFragment.getmCbSelectAll().setChecked(mBookcaseAdapter.isCheckedAll());
        //重置全选的文字
        if (mBookcaseAdapter.isCheckedAll()) {
            mBookcaseFragment.getmCbSelectAll().setText("取消");
        } else {
            mBookcaseFragment.getmCbSelectAll().setText("全选");
        }
    }

}