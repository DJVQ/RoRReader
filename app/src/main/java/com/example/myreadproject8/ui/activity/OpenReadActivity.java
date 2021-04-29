package com.example.myreadproject8.ui.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myreadproject8.ActivityManage;
import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.common.URLCONST;
import com.example.myreadproject8.databinding.ActivityOpenReadBinding;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.Font;
import com.example.myreadproject8.enums.LocalBookSource;
import com.example.myreadproject8.enums.ReadStyle;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.BookMark;
import com.example.myreadproject8.greendao.entity.BookMarkE;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.entity.ReplaceRuleBean;
import com.example.myreadproject8.greendao.service.BookMarkEService;
import com.example.myreadproject8.greendao.service.BookMarkService;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.activity.read.FontsActivity;
import com.example.myreadproject8.ui.dialog.ReciteDialog;
import com.example.myreadproject8.util.file.ShareUtils;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.read.BrightUtil;
import com.example.myreadproject8.util.read.ScreenUtils;
import com.example.myreadproject8.util.read.SystemBarUtils;
import com.example.myreadproject8.util.read.SystemUtil;
import com.example.myreadproject8.util.read.notification.NotificationClickReceiver;
import com.example.myreadproject8.util.read.notification.NotificationUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.string.StringUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.DateHelper;
import com.example.myreadproject8.util.utils.storage.Backup;
import com.example.myreadproject8.widget.BubblePopupView;
import com.example.myreadproject8.widget.page.LocalPageLoader;
import com.example.myreadproject8.widget.page.PageLoader;
import com.example.myreadproject8.widget.page.PageMode;
import com.example.myreadproject8.widget.page.PageView;
import com.example.myreadproject8.widget.page.TxtChar;
import com.gyf.immersionbar.ImmersionBar;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.myreadproject8.util.file.UriFileUtil.getPath;

import com.example.myreadproject8.ui.dialog.CopyContentDialog;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.dialog.ReplaceDialog;
import com.example.myreadproject8.ui.dialog.SourceExchangeDialog;
import com.example.myreadproject8.ui.popmenu.AutoPageMenu;
import com.example.myreadproject8.ui.popmenu.BrightnessEyeMenu;
import com.example.myreadproject8.ui.popmenu.CustomizeComMenu;
import com.example.myreadproject8.ui.popmenu.CustomizeLayoutMenu;
import com.example.myreadproject8.ui.popmenu.ReadSettingMenu;

/**
 * @author fengyue
 * @date 2020/10/21 16:46
 */
public class OpenReadActivity extends BaseActivity implements ColorPickerDialogListener, View.OnTouchListener{
    private static final String TAG = OpenReadActivity.class.getSimpleName();

    /*****************************View***********************************/
    private ActivityOpenReadBinding binding;

    /***************************variable*********************************/
    private Book mBook;
    private List<BookMarkE> mBookMarkE;
    private BookMarkEService mBookMarkEService;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ChapterService mChapterService;
    private BookService mBookService;
    private BookMarkService mBookMarkService;
    private NotificationUtil notificationUtil;
    private Setting mSetting;

    private boolean isCollected = true;//是否在书架中
    private boolean autoPage = false;//是否自动翻页
    private boolean loadFinish = false;
    private int curCacheChapterNum;//缓存章节数
    private int needCacheChapterNum;
    private PageLoader mPageLoader;//页面加载器

    private int screenTimeout;//息屏时间
    private Runnable keepScreenRunnable;//息屏线程
    private Runnable autoPageRunnable;
    private Runnable sendDownloadNotification;
    private static boolean isStopDownload = true;

    private int tempCacheChapterNum;
    private int tempCount;
    private String downloadingChapter;

    private ReadCrawler mReadCrawler;

    private int downloadInterval = 150;

    private SourceExchangeDialog mSourceDialog;

    private boolean hasChangeSource;

    private int pagePos;//页面序号
    private int chapterPos;//章节序号

    private Animation mTopInAnim;//顶部动画
    private Animation mTopOutAnim;
    private Animation mBottomInAnim;//底部动画
    private Animation mBottomOutAnim;
    private int lastX,lastY;


    //接受电池信息和时间更新的广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){//电池变化广播
                //电池剩余电量
                int level = intent.getIntExtra("level",0);
                try{
                    mPageLoader.updateBattery(level);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else if(Intent.ACTION_TIME_TICK.equals(intent.getAction())){//时间变化广播
                try{
                    mPageLoader.updateTime();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    init();
                    break;
                case 2:
                    try{
                        int chapterPos = msg.arg1;
                        int pagePos = msg.arg2;
                        mPageLoader.skipToChapter(chapterPos);
                        mPageLoader.skipToPage(pagePos);
                    }catch (Exception e){
                        //ToastUtils.showError("章节跳转失败\n"+e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    break;
                case 4:
                    saveLastChapterReadPosition();
                    screenOffTimerStart();
                    initMenu();
                    break;
                case 5:
                    if(mPageLoader != null){
                        mPageLoader.refreshUi();
                    }
                    break;
                case 6:
                    mPageLoader.setmStatus(PageLoader.STATUS_LOADING);
                    break;
                case 7:
                    ToastUtils.showWarring("无网络连接！");
                    mPageLoader.chapterError("无网络连接！");
                    break;
                case 8:
                    binding.pbLoading.setVisibility(View.GONE);
                    break;
                case 9:
                    ToastUtils.showInfo("正在缓存书籍，具体进度可查看通知栏！");
                    notificationUtil.requestNotificationPermissionDialog(OpenReadActivity.this);
                    break;
            }
        }
    };



    /**************************override***********************************/
    @Override
    protected void bindView() {
        binding = ActivityOpenReadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState !=null){
            pagePos = savedInstanceState.getInt("pagePos");
            chapterPos = savedInstanceState.getInt("chapterPos");
        }else {
            pagePos = -1;
            chapterPos = -1;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mBook != null){
            outState.putInt("pagePos",mBook.getLastReadPosition());
            outState.putInt("chapterPos",mBook.getHistoryChapterNum());
        }
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        getSupportActionBar().setTitle(mBook.getName());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBookMarkEService = new BookMarkEService();
        try {
            mBookMarkE = mBookMarkEService.findBookAllBookMarkByBookId(mBook.getId());
        }catch (Exception e){}
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mBookMarkService = BookMarkService.getInstance();


        String isRead = (String) getIntent().getSerializableExtra("isRead");
//        mSetting.setCanSelectText("yes".equals(isRead));
        if("yes".equals(isRead)){
            mSetting = SysManager.getReadSetting();
            mSetting.setCurReadStyleIndex(0);
        }else
            mSetting = SysManager.getSetting();
        mSetting.setCanSelectText(true);
        if("yes".equals(isRead))
            SysManager.saveSetting(mSetting);
        else
            SysManager.saveReadSetting(mSetting);
        if(!loadBook()){
            finish();
            return;
        }
        if(pagePos !=-1 &&chapterPos != -1){
            mBook.setHistoryChapterNum(chapterPos);
            mBook.setLastReadPosition(pagePos);
        }

        //息屏时间
        screenTimeout = mSetting.getResetScreen()*60;
        //保持屏幕常亮
        keepScreenRunnable = this::unKeepScreenOn;
        autoPageRunnable = this::nextPage;
        sendDownloadNotification = this::sendNotification;

        notificationUtil = NotificationUtil.getInstance();
        isCollected = getIntent().getBooleanExtra("isCollected",true);
        hasChangeSource = getIntent().getBooleanExtra("hasChangeSource",false);

        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());

        mPageLoader = binding.readPvContent.getPageLoader(mBook,mReadCrawler,mSetting,mBookMarkEService.findBookAllBookMarkByBookId(mBook.getId()),isRead());
        mSourceDialog = new SourceExchangeDialog(this,mBook);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        ImmersionBar.with(this).fullScreen(true).init();
        //隐藏StatusBar
        binding.readPvContent.post(
                this::hideSystemBar
        );
        if(!mSetting.isBrightFollowSystem()){
            BrightUtil.setBrightness(this,mSetting.getBrightProgress());
        }
        binding.pbLoading.setVisibility(VISIBLE);
        initEyeView();
        initSettingListener();
        initTopMenu();
        initBottomMenu();
        setOrientation(mSetting.isHorizontalScreen());
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClick() {
        super.initClick();
        binding.readPvContent.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                screenOffTimerStart();
                return !hideReadMenu();
            }

            @Override
            public void center() {
                toggleMenu(true);
            }

            @Override
            public void prePage() {
                mPageLoader.setPrev(true);
            }

            @Override
            public void nextPage(boolean hasNextChange) {
                mPageLoader.setPrev(false);
                if(!hasNextChange){
                    if(autoPage){
                        autoPageStop();
                    }
                }
            }

            @Override
            public void cancel() {

            }

            @Override
            public void onTouchClearCursor() {
                binding.cursorLeft.setVisibility(View.INVISIBLE);
                binding.cursorRight.setVisibility(View.INVISIBLE);
                longPressMenu.hidePopupListWindow();
            }

            @Override
            public void onLongPress() {
                if(!binding.readPvContent.isRunning()){
                    selectTextCursorShow();
                    if("yes".equals(isRead()))
                        showAction();
                    else
                        showScanAction();
                }
            }
        });

        mPageLoader.setOnPageChangeListener(new PageLoader.OnPageChangeListener() {
            @Override
            public void onChapterChange(int pos) {
                mBook.setHistoryChapterId(mChapters.get(pos).getTitle());
            }

            @Override
            public void onCategoryFinish(List<Chapter> chapters) {

            }

            @Override
            public void onPageCountChange(int count) {

            }

            @Override
            public void onPageChange(int pos, boolean resetRead) {
                mHandler.sendMessage(mHandler.obtainMessage(4));

            }
        });

        binding.readSbChapterProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    binding.readTvPageTip.setText(String.format("%s/%s",progress+1,seekBar.getMax()+1));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //进行切换
                int pagePos = seekBar.getProgress();
                if(pagePos != mPageLoader.getPagePos()&&pagePos<mPageLoader.getAllPagePos()){
                    mPageLoader.skipToPage(pagePos);
                }
            }
        });

        initBottomMenuClick();

        mSourceDialog.setOnSourceChangeListener((bean,pos)->{
            Book bookTem = (Book)mBook.clone();
            bookTem.setChapterUrl(bean.getChapterUrl());
            bookTem.setSource(bean.getSource());
            if (!StringHelper.isEmpty(bean.getImgUrl())) {
                bookTem.setImgUrl(bean.getImgUrl());
            }
            if (!StringHelper.isEmpty(bean.getType())) {
                bookTem.setType(bean.getType());
            }
            if (!StringHelper.isEmpty(bean.getDesc())) {
                bookTem.setDesc(bean.getDesc());
            }
            if (isCollected) {
                mBookService.updateBook(mBook, bookTem);
            }
            mBook = bookTem;
            toggleMenu(true);
            Intent intent = new Intent(this, OpenReadActivity.class)
                    .putExtra(APPCONST.BOOK, mBook)
                    .putExtra("hasChangeSource", true);
            if (!isCollected) {
                intent.putExtra("isCollected", false);
            }
            finish();
            startActivity(intent);
        });
        initReadLongPressPop();
        initTextMarkPop();
        initScanLongPressPop();
        binding.cursorLeft.setOnTouchListener(this);
        binding.cursorRight.setOnTouchListener(this);
        binding.rlContent.setOnTouchListener(this);
    }

    protected void initBottomMenuClick(){
        //设置
        if("yes".equals(isRead())){
            binding.readTvSetting.setVisibility(GONE);
        }
        binding.readTvSetting.setOnClickListener(v->{
            toggleMenu(false);
            binding.readSettingMenu.startAnimation(mBottomInAnim);
            binding.readSettingMenu.setVisibility(VISIBLE);
        });
        //上一章
        binding.readTvPreChapter.setOnClickListener(v->mPageLoader.skipPreChapter());
        //下一章
        binding.readTvNextChapter.setOnClickListener(v->mPageLoader.skipNextChapter());
        //护眼
        binding.readTvBrightnessEye.setOnClickListener(v->{
            hideReadMenu();
            binding.readBrightnessEyeMenu.initWidget();
            binding.readBrightnessEyeMenu.setVisibility(VISIBLE);
            binding.readBrightnessEyeMenu.startAnimation(mBottomInAnim);
        });
        //目录
        binding.readTvCategory.setOnClickListener(v->{
            //切换菜单
            toggleMenu(true);
            //跳转
            mHandler.postDelayed(()->{
                Intent intent = new Intent(this,CatalogActivity.class);
                intent.putExtra(APPCONST.BOOK,mBook);
                this.startActivityForResult(intent,APPCONST.REQUEST_CHAPTER_PAGE);
            },mBottomOutAnim.getDuration());
        });
        //跳转链接
        binding.llChapterView.setOnClickListener(v->{
            if(mChapters !=null&&mChapters.size() !=0){
                Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
                String url = NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(),curChapter.getUrl());
                if(!"本地书籍".equals(mBook.getType())&&!StringHelper.isEmpty(url)){
                    try{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(url);
                        intent.setData(uri);
                        startActivity(intent);
                    }catch (Exception e){
                        ToastUtils.showError(e.getLocalizedMessage());
                    }
                }
            }
        });

    }

    @Override
    protected void processLogic() {
        super.processLogic();
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver,intentFilter);
        //当书籍Collected且id不为空时保存上次阅读信息
        if (isCollected && !StringHelper.isEmpty(mBook.getId())) {
            //保存上次阅读信息
            SharedPreUtils.getInstance().putString(getString(R.string.lastRead), mBook.getId());
        }
        //保存最近阅读时间
        mBook.setLastReadTime(DateHelper.getLongDate());
        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeTurnPage = SysManager.getSetting().isVolumeTurnPage();
        if (binding.readAblTopMenu.getVisibility() != View.VISIBLE &&
                binding.readCustomizeLayoutMenu.getVisibility() != View.VISIBLE &&
                binding.readAutoPageMenu.getVisibility() != View.VISIBLE &&
                binding.readCustomizeMenu.getVisibility() != View.VISIBLE &&
                binding.readSettingMenu.getVisibility() != View.VISIBLE &&
                binding.readBrightnessEyeMenu.getVisibility() != View.VISIBLE) {
            if (binding.readPvContent.getSelectMode() != PageView.SelectMode.Normal) {
                clearSelect();
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (isVolumeTurnPage) {
                        return mPageLoader.skipToPrePage();
                    }

                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isVolumeTurnPage) {
                        return mPageLoader.skipToNextPage();
                    }

            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (hideReadMenu()) {
            return;
        }
        finish();
    }

    @Override
    public void finish() {
        if (!isCollected) {
            DialogCreator.createCommonDialog(this, "加入书架", "喜欢本书就加入书架吧", true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveLastChapterReadPosition();
                            isCollected = true;
                            exit();
                        }
                    }
                    , (dialog, which) -> {
                        mBookService.deleteBookById(mBook.getId());
                        exit();
                    });
        } else {
            saveLastChapterReadPosition();
            exit();
        }
    }

    private void exit() {
        // 返回给BookDetail
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_IS_COLLECTED, isCollected);
        if (mPageLoader != null) {
            result.putExtra(APPCONST.RESULT_LAST_READ_POSITION, mPageLoader.getPagePos());
            result.putExtra(APPCONST.RESULT_HISTORY_CHAPTER, mPageLoader.getChapterPos());
        }
        setResult(AppCompatActivity.RESULT_OK, result);
        if (!ActivityManage.isExist(IndexActivity.class)) {
            Intent intent = new Intent(this, IndexActivity.class);
            startActivity(intent);
        }
        Backup.INSTANCE.autoBack();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacks(keepScreenRunnable);
        mHandler.removeCallbacks(autoPageRunnable);
        /*mHandler.removeCallbacks(sendDownloadNotification);
        notificationUtil.cancelAll();
        App.getApplication().shutdownThreadPool();*/
        if (autoPage) {
            autoPageStop();
        }
        for (int i = 0; i < 9; i++) {
            mHandler.removeMessages(i + 1);
        }
        if (mPageLoader != null) {
            mPageLoader.closeBook();
            mPageLoader = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.action_load_finish, loadFinish);
        if ("本地书籍".equals(mBook.getType())) {
            menu.findItem(R.id.action_change_source).setVisible(false);
            menu.findItem(R.id.action_open_link).setVisible(false);
            menu.findItem(R.id.action_download).setVisible(false);
            menu.findItem(R.id.action_replace_content).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_change_source) {
            mSourceDialog.show();
        } else if (itemId == R.id.action_reload) {
            mPageLoader.setPrev(false);
            if (!"本地书籍".equals(mBook.getType())) {
                mChapterService.deleteChapterCacheFile(mChapters.get(mPageLoader.getChapterPos()));
            }
            mPageLoader.refreshChapter(mChapters.get(mPageLoader.getChapterPos()));
        } else if (itemId == R.id.action_add_bookmark) {
            if (mChapters == null || mChapters.size() == 0) {
                if ("本地书籍".equals(mBook.getType())) {
                    ToastUtils.showWarring("请等待章节拆分完成后再添加书签");
                } else {
                    ToastUtils.showError("章节目录为空，添加书签失败!");
                }
                return true;
            }
            Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
            BookMark bookMark = new BookMark();
            bookMark.setBookId(mBook.getId());
            bookMark.setTitle(curChapter.getTitle());
            bookMark.setBookMarkChapterNum(mPageLoader.getChapterPos());
            bookMark.setBookMarkReadPosition(mPageLoader.getPagePos());
            mBookMarkService.addOrUpdateBookMark(bookMark);
            DialogCreator.createTipDialog(this, "《" + mBook.getName() +
                    "》：" + bookMark.getTitle() + "[" + (bookMark.getBookMarkReadPosition() + 1) +
                    "]\n书签添加成功，书签列表可在目录界面查看！");
            return true;
        } else if (itemId == R.id.action_replace_content) {
            Intent ruleIntent = new Intent(this, ReplaceRuleActivity.class);
            startActivityForResult(ruleIntent, APPCONST.REQUEST_REFRESH_READ_UI);
        } else if (itemId == R.id.action_copy_content) {
            new CopyContentDialog(this, mPageLoader.getContent()).show();
        } else if (itemId == R.id.action_open_link) {
            Uri uri = Uri.parse(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(), mBook.getChapterUrl()));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (itemId == R.id.action_download) {
            download();
        }else if(itemId == R.id.manage_markE){
            Intent markEIntent = new Intent(this,MarkEActivity.class);
            markEIntent.putExtra(APPCONST.BookMarkE, (Parcelable) mBookMarkE);
            markEIntent.putExtra(APPCONST.BOOK,mBook);
            startActivity(markEIntent);
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * 结果回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_FONT:
                    Font font = (Font) data.getSerializableExtra(APPCONST.FONT);
                    mSetting.setFont(font);
//                    init();
                    App.runOnUiThread(() -> mPageLoader.setFont(font));
                    break;
                case APPCONST.REQUEST_CHAPTER_PAGE:
                    int[] chapterAndPage = data.getIntArrayExtra(APPCONST.CHAPTER_PAGE);
                    /*LLog.i(TAG, "chapterAndPage == null" + (chapterAndPage == null));
                    LLog.i(TAG, "chapterAndPage.length" + (chapterAndPage.length));*/
                    if (chapterAndPage == null) {
                        ToastUtils.showError("章节跳转失败!");
                        return;
                    }
                    try {
                        skipToChapterAndPage(chapterAndPage[0], chapterAndPage[1]);
                    } catch (Exception e) {
                        ToastUtils.showError("章节跳转失败，请截图联系作者。\n" +
                                e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case APPCONST.REQUEST_REFRESH_READ_UI:
                    screenTimeout = mSetting.getResetScreen() * 60;
                    screenOffTimerStart();
                    boolean needRefresh = data.getBooleanExtra(APPCONST.RESULT_NEED_REFRESH, false);
                    boolean upMenu = data.getBooleanExtra(APPCONST.RESULT_UP_MENU, false);
                    if (needRefresh) {
                        mHandler.sendEmptyMessage(5);
                    }
                    if (upMenu) {
                        initTopMenu();
                    }
                    break;
                case APPCONST.REQUEST_SELECT_BG:
                    String bgPath = getPath(this, data.getData());
                    binding.readCustomizeLayoutMenu.setCustomBg(bgPath);
                    break;
                case APPCONST.REQUEST_IMPORT_LAYOUT:
                    String zipPath = getPath(this, data.getData());
                    binding.readCustomizeLayoutMenu.zip2Layout(zipPath);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Callback that is invoked when a color is selected from the color picker dialog.
     *
     * @param dialogId The dialog id used to create the dialog instance.
     * @param color    The selected color
     */
    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case APPCONST.SELECT_TEXT_COLOR:
                mSetting.setTextColor(color);
                mPageLoader.setTextSize();
                break;
            case APPCONST.SELECT_BG_COLOR:
                mSetting.setBgIsColor(true);
                mSetting.setBgColor(color);
                mPageLoader.refreshUi();
                break;
        }
        if(isRead().equals("yes"))
            SysManager.saveReadSetting(mSetting);
        else
            SysManager.saveSetting(mSetting);
        binding.readCustomizeLayoutMenu.upColor();
    }

    /**
     * Callback that is invoked when the color picker dialog was dismissed.
     *
     * @param dialogId The dialog id used to create the dialog instance.
     */
    @Override
    public void onDialogDismissed(int dialogId) {

    }


    /**************************method*********************************/
    /**
     * 初始化
     */
    private void init() {
        if (App.isDestroy(this)) return;
        screenOffTimerStart();
        mPageLoader.init();
        mPageLoader.refreshChapterList();
        loadFinish = true;
        invalidateOptionsMenu();
        mHandler.sendMessage(mHandler.obtainMessage(8));
    }

    private void initMenu() {
        if (mChapters != null && mChapters.size() != 0) {
            Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
            String url = curChapter.getUrl();
            binding.tvChapterTitleTop.setText(curChapter.getTitle());
            binding.tvChapterUrl.setText(StringHelper.isEmpty(url) ? curChapter.getId() :
                    NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(), url));
            binding.readSbChapterProgress.setProgress(mPageLoader.getPagePos());
            binding.readSbChapterProgress.setMax(mPageLoader.getAllPagePos() - 1);
            binding.readTvPageTip.setText(String.format("%s/%s",
                    binding.readSbChapterProgress.getProgress() + 1, binding.readSbChapterProgress.getMax() + 1));
        }
    }


    /************************章节相关*************************/
    /**
     * 章节数据网络同步
     */
    private void getData() {
        mChapters = (ArrayList<Chapter>)mChapterService.findBookAllChapterByBookId(mBook.getId());
        if(!isCollected||mChapters.size() == 0 ||("本地书籍".equals(mBook.getType())&&!ChapterService.isChapterCached(mBook.getId(),mChapters.get(0).getTitle())
        )) {
            if ("本地书籍".equals(mBook.getType())) {
                if (!new File(mBook.getChapterUrl()).exists()) {
                    ToastUtils.showWarring("书籍缓存为空且源文件不存在，书籍加载失败！");
                    finish();
                    return;
                }
                if (mChapters.size() != 0 && mChapters.get(0).getEnd() > 0) {
                    initChapters();
                    return;
                }
                ((LocalPageLoader) mPageLoader).loadChapters(new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        mBook.setChapterTotalNum(chapters.size());
                        mBook.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
                        mBookService.updateEntity(mBook);
                        if (mChapters.size() == 0) {
                            updateAllOldChapterData(chapters);
                        }
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        mChapters.clear();
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });

            }else {
                mPageLoader.setmStatus(PageLoader.STATUS_LOADING_CHAPTER);
                CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, false, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        mPageLoader.setmStatus(PageLoader.STATUS_LOADING);
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        updateAllOldChapterData(chapters);
                        initChapters();
                    }

                    @Override
                    public void onError(Exception e) {
//                settingChange = true;
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            }
        }else {
            initChapters();
        }
    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    private void updateAllOldChapterData(ArrayList<Chapter> newChapters) {
        for (Chapter newChapter : newChapters) {
            newChapter.setId(StringHelper.getStringRandom(25));
            newChapter.setBookId(mBook.getId());
            mChapters.add(newChapter);
//                mChapterService.addChapter(newChapters.get(j));
        }
        mChapterService.addChapters(mChapters);
    }

    /**
     * 初始化章节
     */
    private void initChapters() {
        mBook.setNoReadNum(0);
        mBook.setChapterTotalNum(mChapters.size());
        if (!StringHelper.isEmpty(mBook.getId())) {
            mBookService.updateEntity(mBook);
        }
        if (mChapters.size() == 0) {
            ToastUtils.showWarring("该书查询不到任何章节");
            mHandler.sendMessage(mHandler.obtainMessage(8));
        } else {
            if (mBook.getHistoryChapterNum() < 0) {
                mBook.setHistoryChapterNum(0);
            } else if (mBook.getHistoryChapterNum() >= mChapters.size()) {
                mBook.setHistoryChapterNum(mChapters.size() - 1);
            }
            if ("本地书籍".equals(mBook.getType())) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return;
            }
            if (hasChangeSource) {
                mBookService.matchHistoryChapterPos(mBook, mChapters);
            }
            mHandler.sendMessage(mHandler.obtainMessage(1));
            mHandler.sendMessage(mHandler.obtainMessage(4));
        }
    }
    /**
     * 跳转到指定章节的指定页面
     *
     * @param chapterPos
     * @param pagePos
     */
    private void skipToChapterAndPage(final int chapterPos, final int pagePos) {
        mPageLoader.setPrev(false);
        if (StringHelper.isEmpty(mChapters.get(chapterPos).getContent())) {
            if ("本地书籍".equals(mBook.getType())) {
                ToastUtils.showWarring("该章节无内容！");
                return;
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(2, chapterPos, pagePos));
    }

    /**
     * 保存最后阅读章节的进度
     */
    public void saveLastChapterReadPosition() {
        if (!StringHelper.isEmpty(mBook.getId()) && mPageLoader.getPageStatus() == PageLoader.STATUS_FINISH) {
            mBook.setLastReadPosition(mPageLoader.getPagePos());
            mBook.setHistoryChapterNum(mPageLoader.getChapterPos());
            mBookService.updateEntity(mBook);
        }
    }
    /************************书籍相关*************************************/
    /**
     * description:
     * @return 是否加载成功
     */
    private boolean loadBook(){
        //是否直接打开本地txt文件
        String path = null;
        if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
            Uri uri = getIntent().getData();
            if(uri != null){
                path = getPath(this,uri);
            }
        }
        if(!StringHelper.isEmpty(path)){
            //本地书籍路径不为空，添加书籍
            addLocalBook(path);
        }else {
            //路径为空，说明不是直接打开txt文件
            mBook = (Book)getIntent().getSerializableExtra(APPCONST.BOOK);
            //mBook为空，说明是从快捷方式启动
            if(mBook == null){
                String bookId = SharedPreUtils.getInstance().getString(getString(R.string.lastRead),"");
                if("".equals(bookId)){//没有上次阅读信息
                    ToastUtils.showWarring("当前没有阅读任何书籍，无法加载上次阅读书籍！");
                    finish();
                    return false;
                }else {//有信息
                    mBook = mBookService.getBookById(bookId);
                    if(mBook == null){//上次阅读的书籍不存在
                        ToastUtils.showWarring("上次阅读的书籍已经不存在/移除书架，无法加载！");
                        finish();
                        return false;
                    }//存在就继续执行
                }
            }
        }
        return true;
    }


    /**
     * description:添加本地书籍
     * @param path
     */
    private void addLocalBook(String path){
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        Book book = new Book();
        book.setName(file.getName().replace(".txt",""));
        book.setChapterUrl(path);
        book.setType(getString(R.string.local_book));
        book.setHistoryChapterId("未开始阅读");
        book.setNewestChapterTitle("未拆分章节");
        book.setAuthor(getString(R.string.local_book));
        book.setSource(LocalBookSource.local.toString());
        book.setDesc("无");
        book.setIsCloseUpdate(true);
        //判断书籍是否已添加
        Book existBook = mBookService.findBookByAuthorAndName(book.getName(),book.getAuthor());
        if(book.equals(existBook)){
            mBook =existBook;
            return;
        }

        mBookService.addBook(book);

        mBook = book;
    }

    /**
     * 添加到书架并缓存书籍
     */
    private void addBookToCaseAndDownload() {
        DialogCreator.createCommonDialog(this, this.getString(R.string.tip), this.getString(R.string.download_no_add_tips), true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadBook();
                isCollected = true;
            }
        }, (dialog, which) -> dialog.dismiss());
    }
    /****************息屏相关*****************/
    /**
     * 取消亮屏保持
     */
    private void unKeepScreenOn() {
        keepScreenOn(false);
    }

    /**
     * @param keepScreenOn 是否保持亮屏
     */
    public void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 重置黑屏时间
     */
    private void screenOffTimerStart() {
        if (screenTimeout <= 0) {
            keepScreenOn(true);
            return;
        }
        int screenOffTime = screenTimeout * 1000 - SystemUtil.getScreenOffTime(this);
        if (screenOffTime > 0) {
            mHandler.removeCallbacks(keepScreenRunnable);
            keepScreenOn(true);
            mHandler.postDelayed(keepScreenRunnable, screenOffTime);
        } else {
            keepScreenOn(false);
        }
    }

    /*******************************自动翻页相关************************************/
    /**
     * 自动翻页
     */
    private void autoPage() {
        mHandler.removeCallbacks(autoPageRunnable);
        if (autoPage) {
            mPageLoader.setPageMode(PageMode.AUTO);
            mPageLoader.skipToNextPage();
            mHandler.postDelayed(autoPageRunnable, mSetting.getAutoScrollSpeed() * 1000);
        }
    }

    /**
     * 停止自动翻页
     */
    public void autoPageStop() {
        autoPage = false;
        mPageLoader.setPageMode(mSetting.getPageMode());
        autoPage();
    }

    /**
     * 下一页
     */
    private void nextPage() {
        App.runOnUiThread(() -> {
            screenOffTimerStart();
            autoPage();
        });
    }


    /***************************缓存相关***************************/
    private int selectedIndex;//对话框选择下标

    protected void downloadBook(){
        if("本地书籍".equals(mBook.getType())){
            ToastUtils.showWarring("《"+mBook.getName()+"》是本地书籍，不需要缓存");
            return;
        }
        if(!NetworkUtils.isNetWorkAvailable()){
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        App.runOnUiThread(()->{
            MyAlertDialog.build(this)
                    .setTitle("缓存书籍")
                    .setSingleChoiceItems(getResources().getStringArray(R.array.download), selectedIndex,
                            (dialog, which) -> selectedIndex = which).setNegativeButton("取消", ((dialog, which) -> dialog.dismiss())).setPositiveButton("确定",
                    (dialog, which) -> {
                        switch (selectedIndex) {
                            case 0:
                                addDownload(mPageLoader.getChapterPos(), mPageLoader.getChapterPos() + 50);
                                break;
                            case 1:
                                addDownload(mPageLoader.getChapterPos() - 50, mPageLoader.getChapterPos() + 50);
                                break;
                            case 2:
                                addDownload(mPageLoader.getChapterPos(), mChapters.size());
                                break;
                            case 3:
                                addDownload(0, mChapters.size());
                                break;
                        }
                    }).show();
        });

    }

    private void addDownload(int begin,int end){
        if(SysManager.getSetting().getCatheGap()!=0){
            downloadInterval = SysManager.getSetting().getCatheGap();
        }
        //计算断点章节
        final  int finalBegin = Math.max(0,begin);
        final int finalEnd = Math.min(end,mChapters.size());
        needCacheChapterNum = finalEnd - finalBegin;
        curCacheChapterNum = 0;
        isStopDownload = false;
        ArrayList<Chapter> needDownloadChapters = new ArrayList<>();
        for(int i = finalBegin;i<finalEnd;i++){
            final  Chapter chapter = mChapters.get(i);
            if(StringHelper.isEmpty(chapter.getContent())){
                needDownloadChapters.add(chapter);
            }
        }
        needCacheChapterNum = needDownloadChapters.size();
        if(needCacheChapterNum>0){
            mHandler.sendEmptyMessage(9);
            mHandler.postDelayed(sendDownloadNotification,2*downloadInterval);
        }
        App.getmApplication().newThread(()->{
            for(Chapter chapter:needDownloadChapters){
                if(StringHelper.isEmpty(chapter.getBookId())){
                    chapter.setId(mBook.getId());
                }
                CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        downloadingChapter = chapter.getTitle();
                        mChapterService.saveOrUpdateChapter(chapter,(String) o);
                        curCacheChapterNum++;
                    }

                    @Override
                    public void onError(Exception e) {
                        curCacheChapterNum++;
                    }
                });
                try {
                    Thread.sleep(downloadInterval);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                if(isStopDownload){
                    break;
                }
            }
            if(curCacheChapterNum == needCacheChapterNum){
                ToastUtils.showInfo("《"+mBook.getName()+getString(R.string.download_already_all_tips));
            }
        });
    }

    private void sendNotification(){
        if(curCacheChapterNum == needCacheChapterNum){
            notificationUtil.cancel(1001);
            return;
        }else {
            Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                    .setSmallIcon(R.drawable.ic_download)
                    //通知栏大图标
                    .setLargeIcon(BitmapFactory.decodeResource(App.getApplication().getResources(),R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    //点击通知后自动清除
                    .setContentTitle("正在下载："+mBook.getName()+"["+curCacheChapterNum+"/"+needCacheChapterNum+"]")
                    .setContentText(downloadingChapter == null ? "  ":downloadingChapter)
                    .addAction(R.drawable.ic_stop_black,"停止",
                            notificationUtil.getChancelPendingIntent(cancelDownloadReceiver.class))
                    .build();
            notificationUtil.notify(1001,notification);
        }
        if(tempCacheChapterNum < curCacheChapterNum){
            tempCount = 1500/downloadInterval;
            tempCacheChapterNum = curCacheChapterNum;
        }else if(tempCacheChapterNum == curCacheChapterNum){
            tempCount--;
            if(tempCount == 0){
                notificationUtil.cancel(1001);
                return;
            }
        }
        mHandler.postDelayed(sendDownloadNotification,2*downloadInterval);
    }

    public static class cancelDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo 跳转之前要处理的逻辑
            if (NotificationClickReceiver.CANCEL_ACTION.equals(intent.getAction())) {
                isStopDownload = true;
            }
        }
    }



    /**************************护眼相关*********************************/
    private View vProtectEye;

    private void initEyeView() {
        ViewGroup content = findViewById(android.R.id.content);
        vProtectEye = new FrameLayout(this);
        vProtectEye.setBackgroundColor(mSetting.isProtectEye() ? getFilterColor(mSetting.getBlueFilterPercent()) : Color.TRANSPARENT);          //设置透明
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL     //不触碰
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE            //不可定焦
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;           //不可触
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        content.addView(vProtectEye, params);
    }
    /**
     * 开启护眼模式
     */
    public void openEye() {
        if (vProtectEye == null) {
            initEyeView();
        }
        vProtectEye.setBackgroundColor(getFilterColor(mSetting.getBlueFilterPercent()));
    }

    /**
     * 关闭护眼模式
     */
    public void closeEye() {
        if (vProtectEye == null) {
            initEyeView();
        }
        vProtectEye.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 过滤蓝光
     *
     * @param blueFilterPercent 蓝光过滤比例[10-30-80]
     */
    public int getFilterColor(int blueFilterPercent) {
        int realFilter = blueFilterPercent;
        if (realFilter < 10) {
            realFilter = 10;
        } else if (realFilter > 80) {
            realFilter = 80;
        }
        int a = (int) (realFilter / 80f * 180);
        int r = (int) (200 - (realFilter / 80f) * 190);
        int g = (int) (180 - (realFilter / 80f) * 170);
        int b = (int) (60 - realFilter / 80f * 60);
        return Color.argb(a, r, g, b);
    }


    /******************设置相关*****************/
    /**
     * 初始化详细设置
     */
    private void initSettingListener(){
        binding.readSettingMenu.setOnClickListener(null);
        binding.readSettingMenu.setListener(this, new ReadSettingMenu.Callback() {
            @Override
            public void onRefreshPage() {
                mPageLoader.refreshPagePara();
            }

            @Override
            public void onPageModeChange() {
                mPageLoader.setPageMode(mSetting.getPageMode());
            }

            @Override
            public void onRefreshUI() {
                mPageLoader.refreshUi();
            }

            @Override
            public void onStyleChange() {
                changeStyle();
            }

            @Override
            public void onTextSizeChange() {
                mPageLoader.setTextSize();
            }

            @Override
            public void onFontClick() {
                hideReadMenu();
                mHandler.postDelayed(()->{
                    Intent intent = new Intent(OpenReadActivity.this, FontsActivity.class);
                    startActivityForResult(intent,APPCONST.REQUEST_FONT);
                },mBottomOutAnim.getDuration());
            }

            @Override
            public void onAutoPageClick() {
                hideReadMenu();
                ToastUtils.showInfo("自动翻页开启");
                autoPage = !autoPage;
                autoPage();
            }

            @Override
            public void onHVChange() {
                setOrientation(mSetting.isHorizontalScreen());
            }

            @Override
            public void onMoreSettingClick() {
                hideReadMenu();
                mHandler.postDelayed(()->{
                    Intent intent = new Intent(OpenReadActivity.this,MoreSettingActivity.class);
                    startActivityForResult(intent,APPCONST.REQUEST_REFRESH_READ_UI);
                },mBottomOutAnim.getDuration());
            }
        });
        binding.readCustomizeMenu.setOnClickListener(null);
        binding.readCustomizeMenu.setListener(new CustomizeComMenu.Callback() {
            @Override
            public void onTextPChange() {
                mPageLoader.setTextSize();
                mSetting.setComposition(0);
                if(isRead().equals("yes"))
                    SysManager.saveReadSetting(mSetting);
                else
                    SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
            }

            @Override
            public void onMarginChange() {
                mPageLoader.upMargin();
                mSetting.setComposition(0);
                if(isRead().equals("yes"))
                    SysManager.saveReadSetting(mSetting);
                else
                    SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
            }

            @Override
            public void onRefreshUI() {
                mPageLoader.refreshUi();
            }

            @Override
            public void onReset() {
                mPageLoader.setTextSize();
                mPageLoader.upMargin();
                mSetting.setComposition(1);
                if(isRead().equals("yes"))
                    SysManager.saveReadSetting(mSetting);
                else
                    SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
                ToastUtils.showInfo("已重置各间距为默认值");
            }
        });
        binding.readAutoPageMenu.setOnClickListener(null);
        binding.readAutoPageMenu.setListener(new AutoPageMenu.Callback() {
            @Override
            public void onSpeedChange() {
                binding.readPvContent.autoPageOnSpeedChange();
                autoPage();
            }

            @Override
            public void onExitClick() {
                ToastUtils.showInfo("自动翻页关闭");
                autoPageStop();
                hideReadMenu();
            }
        });
        binding.readCustomizeLayoutMenu.setOnClickListener(null);
        binding.readCustomizeLayoutMenu.setListener(this, new CustomizeLayoutMenu.Callback() {
            @Override
            public void upBg() {
                mPageLoader.refreshUi();
            }

            @Override
            public void upStyle() {
                binding.readSettingMenu.initStyleImage();
            }
        });
        binding.readBrightnessEyeMenu.setOnClickListener(null);
        binding.readBrightnessEyeMenu.setListener(this, new BrightnessEyeMenu.Callback() {
            @Override
            public void onProtectEyeChange() {
                if(mSetting.isProtectEye()){
                    openEye();
                }else {
                    closeEye();
                }
            }

            @Override
            public void upProtectEye() {
                openEye();
            }
        });
    }


    /**
     * 设置屏幕方向
     */
    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation(boolean isHorizontalScreen) {
        if (isHorizontalScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    /**
     * 改变阅读风格
     */
    private void changeStyle() {
        upBrightnessEye();
        mPageLoader.refreshUi();
    }

    /**
     * 更新亮度和护眼
     */
    private void upBrightnessEye() {
        if (!mSetting.isBrightFollowSystem()) {
            BrightUtil.setBrightness(this, mSetting.getBrightProgress());
        } else {
            BrightUtil.followSystemBright(this);
        }
        if (mSetting.isProtectEye()) {
            openEye();
        } else {
            closeEye();
        }
    }
    protected void download() {
        if (!isCollected) {
            addBookToCaseAndDownload();
        } else {
            downloadBook();
        }
    }

    public void showCustomizeMenu() {
        binding.readCustomizeMenu.initWidget();
        binding.readCustomizeMenu.setVisibility(VISIBLE);
        binding.readCustomizeMenu.startAnimation(mBottomInAnim);
    }

    public void showCustomizeLayoutMenu() {
        hideReadMenu();

        binding.readCustomizeLayoutMenu.upColor();
        binding.readCustomizeLayoutMenu.setVisibility(VISIBLE);
        binding.readCustomizeLayoutMenu.startAnimation(mBottomInAnim);
    }

    /********************菜单相关*************************/
    /**
     * 初始化顶部菜单
     */
    private void initTopMenu(){
        int statusBarHeight = ImmersionBar.getStatusBarHeight(this);
        binding.readAblTopMenu.setPadding(0,statusBarHeight,0,0);
        if(mSetting.isNoMenuChTitle()){
            binding.llChapterView.setVisibility(GONE);
            binding.toolbar.getLayoutParams().height = 60 + ImmersionBar.getStatusBarHeight(this);
        }else {
            binding.llChapterView.setVisibility(VISIBLE);
            binding.toolbar.getLayoutParams().height = 45 + ImmersionBar.getStatusBarHeight(this);
        }
    }

    private void initBottomMenu(){
        if(true){
            //还需要设=设置mBottomMenu的底部高度
            if(ImmersionBar.hasNavigationBar(this)) {
                int height = ImmersionBar.getNavigationBarHeight(this);
                binding.vwNavigationBar.getLayoutParams().height = height;
                binding.readSettingMenu.setNavigationBarHeight(height);
                binding.readCustomizeMenu.setNavigationBarHeight(height);
                binding.readCustomizeLayoutMenu.setNavigationBarHeight(height);
                binding.readAutoPageMenu.setNavigationBarHeight(height);
                binding.readBrightnessEyeMenu.setNavigationBarHeight(height);
            }
        }else {
            //设置mBottomMenu的底部距离
            binding.vwNavigationBar.getLayoutParams().height = 0;
        }
    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        hideSystemBar();
        boolean flag = false;
        if (binding.readAblTopMenu.getVisibility() == VISIBLE) {
            toggleMenu(true);
            flag = true;
        }
        if (binding.readSettingMenu.getVisibility() == View.VISIBLE) {
            binding.readSettingMenu.setVisibility(GONE);
            binding.readSettingMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readCustomizeMenu.getVisibility() == VISIBLE) {
            binding.readCustomizeMenu.setVisibility(GONE);
            binding.readCustomizeMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readCustomizeLayoutMenu.getVisibility() == VISIBLE) {
            binding.readCustomizeLayoutMenu.setVisibility(GONE);
            binding.readCustomizeLayoutMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readAutoPageMenu.getVisibility() == VISIBLE) {
            binding.readAutoPageMenu.setVisibility(GONE);
            binding.readAutoPageMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readBrightnessEyeMenu.getVisibility() == VISIBLE) {
            binding.readBrightnessEyeMenu.setVisibility(GONE);
            binding.readBrightnessEyeMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        return flag;
    }
    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu(boolean hideStatusBar) {
        toggleMenu(hideStatusBar, false);
    }

    public void toggleMenu(boolean hideStatusBar, boolean home) {
        initMenuAnim();
        if (binding.readAblTopMenu.getVisibility() == View.VISIBLE) {
            //关闭
            binding.readAblTopMenu.startAnimation(mTopOutAnim);
            binding.readLlBottomMenu.startAnimation(mBottomOutAnim);
            binding.readAblTopMenu.setVisibility(GONE);
            binding.readLlBottomMenu.setVisibility(GONE);
            if (hideStatusBar) {
                hideSystemBar();
            }
            return;
        }
        if (autoPage) {
            binding.readAutoPageMenu.setVisibility(VISIBLE);
            binding.readAutoPageMenu.startAnimation(mBottomInAnim);
            return;
        }
        binding.readAblTopMenu.setVisibility(View.VISIBLE);
        binding.readLlBottomMenu.setVisibility(View.VISIBLE);
        binding.readAblTopMenu.startAnimation(mTopInAnim);
        binding.readLlBottomMenu.startAnimation(mBottomInAnim);
        showSystemBar();
    }

    //初始化菜单动画
    private void initMenuAnim() {
        if (mTopInAnim != null) return;
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    private void showSystemBar() {
        //显示
        SystemBarUtils.showUnStableStatusBar(this);
        SystemBarUtils.showUnStableNavBar(this);
    }
    private void hideSystemBar() {
        //隐藏
        if (binding.readAblTopMenu.getVisibility() != VISIBLE) {
            if (!mSetting.isShowStatusBar()) {
                SystemBarUtils.hideStableStatusBar(this);
            }
            SystemBarUtils.hideStableNavBar(this);
        }

    }

    /***********************弹出菜单相关*************************/
    //长按弹出菜单
    private final List<String> longPressMenuItems = new ArrayList<>();
    private BubblePopupView longPressMenu;
    private BubblePopupView.PopupListListener longPressMenuListener;
    //标注菜单
    private final List<String> textMarkItems = new ArrayList<>();
    private BubblePopupView textMarkMenu;
    private BubblePopupView.PopupListListener textMarkMenuListener;
    //浏览页面弹出菜单
    private final List<String> scanLongPressMenuItems = new ArrayList<>();
    private BubblePopupView scanLongPressMenu;
    private BubblePopupView.PopupListListener scanLongPressMenuListener;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.cursor_left || v.getId() == R.id.cursor_right){
            int ea = event.getAction();
            switch(ea){
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                    lastY = (int) event.getRawY();
                    longPressMenu.hidePopupListWindow();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    int l = v.getLeft() + dx;
                    int b = v.getBottom() + dy;
                    int r = v.getRight() + dx;
                    int t = v.getTop() + dy;

                    v.layout(l,t,r,b);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    v.postInvalidate();

                    //移动过程中要画线
                    binding.readPvContent.setSelectMode(PageView.SelectMode.SelectMoveForward);

                    int hh = binding.cursorLeft.getHeight();
                    int ww = binding.cursorRight.getWidth();

                    if(v.getId() == R.id.cursor_left){
                        if(lastY < binding.readPvContent.getLastSelectTxtChar().getBottomRightPosition().y) {
                            if(lastY > binding.readPvContent.getLastSelectTxtChar().getTopRightPosition().y && lastX > binding.readPvContent.getLastSelectTxtChar().getTopLeftPosition().x) {
                                binding.readPvContent.setFirstSelectTxtChar(binding.readPvContent.getLastSelectTxtChar());
                            }else{
                                binding.readPvContent.setFirstSelectTxtChar(binding.readPvContent.getCurrentTxtChar(lastX + ww, lastY - hh));
                            }
                        }else {
                            binding.readPvContent.setFirstSelectTxtChar(binding.readPvContent.getLastSelectTxtChar());
                        }
                        if (binding.readPvContent.getFirstSelectTxtChar() != null) {
                                binding.cursorLeft.setX(binding.readPvContent.getFirstSelectTxtChar().getTopLeftPosition().x - ww);
                                binding.cursorLeft.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomLeftPosition().y);
                        }
                    }else {
                        if(lastY>binding.readPvContent.getFirstSelectTxtChar().getTopLeftPosition().y){
                            if(lastY<binding.readPvContent.getFirstSelectTxtChar().getBottomLeftPosition().y && lastX <binding.readPvContent.getFirstSelectTxtChar().getBottomRightPosition().x){
                                binding.readPvContent.setLastSelectTxtChar(binding.readPvContent.getFirstSelectTxtChar());
                            }else {
                                binding.readPvContent.setLastSelectTxtChar(binding.readPvContent.getCurrentTxtChar(lastX - ww, lastY - hh));

                            }
                        }else {
                            binding.readPvContent.setLastSelectTxtChar(binding.readPvContent.getFirstSelectTxtChar());
                        }

                        if(binding.readPvContent.getLastSelectTxtChar() !=null){
                            binding.cursorRight.setX(binding.readPvContent.getLastSelectTxtChar().getBottomRightPosition().x);
                            binding.cursorRight.setY(binding.readPvContent.getLastSelectTxtChar().getBottomRightPosition().y);
                        }
                    }

                    binding.readPvContent.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                        showAction();
                        break;
                default:
                    break;
            }
        }
        return true;
    }
    /**
     * 显示长按菜单
     */
    public void showAction() {
        float x, y;
        if (binding.cursorLeft.getX() - binding.cursorRight.getX() > 0) {
            x = binding.cursorRight.getX() + (binding.cursorLeft.getX() - binding.cursorRight.getX()) / 2 + ScreenUtils.dpToPx(12);
        } else {
            x = binding.cursorLeft.getX() + (binding.cursorRight.getX() - binding.cursorLeft.getX()) / 2 + ScreenUtils.dpToPx(12);
        }
        if ((binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(60)) < 0) {
            longPressMenu.setShowBottom(true);
            y = binding.cursorLeft.getY() + binding.cursorLeft.getHeight() * 3 / 5;
        } else {
            longPressMenu.setShowBottom(false);
            y = binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(5);
        }
        longPressMenu.showPopupListWindow(binding.rlContent, 0, x, y,
                longPressMenuItems, longPressMenuListener);
    }

    /**
     * 显示标注菜单
     */
    public void showMarkAction() {
        float x, y;
        if (binding.cursorLeft.getX() - binding.cursorRight.getX() > 0) {
            x = binding.cursorRight.getX() + (binding.cursorLeft.getX() - binding.cursorRight.getX()) / 2 + ScreenUtils.dpToPx(12);
        } else {
            x = binding.cursorLeft.getX() + (binding.cursorRight.getX() - binding.cursorLeft.getX()) / 2 + ScreenUtils.dpToPx(12);
        }
        if ((binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(60)) < 0) {
            textMarkMenu.setShowBottom(true);
            y = binding.cursorLeft.getY() + binding.cursorLeft.getHeight() * 3 / 5;
        } else {
            textMarkMenu.setShowBottom(false);
            y = binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(5);
        }
        textMarkMenu.showPopupListWindow(binding.rlContent, 0, x, y,
                textMarkItems, textMarkMenuListener);
    }

    public void showScanAction(){
        float x, y;
        if (binding.cursorLeft.getX() - binding.cursorRight.getX() > 0) {
            x = binding.cursorRight.getX() + (binding.cursorLeft.getX() - binding.cursorRight.getX()) / 2 + ScreenUtils.dpToPx(12);
        } else {
            x = binding.cursorLeft.getX() + (binding.cursorRight.getX() - binding.cursorLeft.getX()) / 2 + ScreenUtils.dpToPx(12);
        }
        if ((binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(60)) < 0) {
            scanLongPressMenu.setShowBottom(true);
            y = binding.cursorLeft.getY() + binding.cursorLeft.getHeight() * 3 / 5;
        } else {
            scanLongPressMenu.setShowBottom(false);
            y = binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(5);
        }
        scanLongPressMenu.showPopupListWindow(binding.rlContent, 0, x, y,
                scanLongPressMenuItems, scanLongPressMenuListener);
    }



    /**
     * 显示
     */
    private void selectTextCursorShow() {
        if (binding.readPvContent.getFirstSelectTxtChar() == null || binding.readPvContent.getLastSelectTxtChar() == null)
            return;
        //show Cursor on current position
        cursorShow();
        //set current word selected
        binding.readPvContent.invalidate();

//        hideSnackBar();
    }

    /**
     * 显示选择
     */
    private void cursorShow() {
        binding.cursorLeft.setVisibility(View.VISIBLE);
        binding.cursorRight.setVisibility(View.VISIBLE);
        int hh = binding.cursorLeft.getHeight();
        int ww = binding.cursorLeft.getWidth();
        if (binding.readPvContent.getFirstSelectTxtChar() != null) {
            binding.cursorLeft.setX(binding.readPvContent.getFirstSelectTxtChar().getTopLeftPosition().x - ww);
            binding.cursorLeft.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomLeftPosition().y);
            binding.cursorRight.setX(binding.readPvContent.getFirstSelectTxtChar().getBottomRightPosition().x);
            binding.cursorRight.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomRightPosition().y);
        }
    }

    /**
     * 长按选择按钮
     */
    private void initReadLongPressPop(){
        longPressMenuItems.add("复制");
        longPressMenuItems.add("标注");
        longPressMenuItems.add("搜索");
        longPressMenuItems.add("分享");
        longPressMenuItems.add("背诵");
        longPressMenuItems.add("清除标注");
        longPressMenu = new BubblePopupView(this);
        //是否跟随手指显示，默认false，设置true后翻转高度无效，永远在上方显示
        longPressMenu.setShowTouchLocation(true);
        longPressMenu.setFocusable(true);
        longPressMenuListener = new BubblePopupView.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                String selectString;
                switch (position){
                    case 0:
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null,binding.readPvContent.getSelectStr());
                        if(clipboard != null){
                            clipboard.setPrimaryClip(clipData);
                            ToastUtils.showInfo("所选内容已经复制到剪贴板");
                        }
                        clearSelect();
                        break;
                    case 1:
                        if(!mBook.getSource().equals("local")){
                            ToastUtils.showError("只有本地书籍才能进行标注!"+mBook.getSource());
                        }else {
                            showMarkAction();
                        }
                        break;

                    case 2:
                        selectString = StringUtils.deleteWhitespace(binding.readPvContent.getSelectStr());
                        MyAlertDialog.build(OpenReadActivity.this)
                                .setTitle(R.string.search_b)
                                .setItems(R.array.search_way,(dialog,which)->{
                                    String url = "";
                                    switch(which){
                                        case 0:
                                            url = URLCONST.BAI_DU_SEARCH;
                                            break;
                                        case 1:
                                            url = URLCONST.GOOGLE_SEARCH;
                                            break;
                                        case 2:
                                            url = URLCONST.YOU_DAO_SEARCH;
                                            break;
                                    }
                                    url = url.replace("{key}",selectString);
                                    Log.d("SEARCH_URL",url);
                                    try{
                                        Uri uri = Uri.parse(url);
                                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(intent);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        ToastUtils.showError(e.getLocalizedMessage());
                                    }
                                }).setNegativeButton("取消",null)
                                .show();
                        clearSelect();
                        break;


                    case 3:
                        selectString = binding.readPvContent.getSelectStr();
                        ShareUtils.share(OpenReadActivity.this,selectString);
                        clearSelect();
                        break;

                    case 4:
                        Recite recite = new Recite();
                        recite.setReciteT("");
                        recite.setReciteContent(binding.readPvContent.getSelectStr().trim());
                        recite.setAuthor(mBook.getAuthor());
                        recite.setBookName(mBook.getName());
                        ReciteDialog reciteDialog = new ReciteDialog(OpenReadActivity.this,recite,()->{
                            ToastUtils.showSuccess("加入背诵区成功");
                            clearSelect();
                            mPageLoader.refreshUi();
                        });
                        reciteDialog.show(getSupportFragmentManager(),"addRecite");
                        clearSelect();
                        mPageLoader.refreshUi();
                        break;
                    case 5:
                        mBookMarkEService.deleteSelectMarkE(mPageLoader.getCollBook().getId(),mPageLoader.getChapterPos(),mPageLoader.getPagePos(),binding.readPvContent.getFirstSelectTxtChar().getLineIndex(),
                                binding.readPvContent.getLastSelectTxtChar().getLineIndex());
                        ToastUtils.showSuccess("清除成功!");
                        clearSelect();
                        mPageLoader.refreshUi();

                }
            }
        };
    }


    public void initTextMarkPop() {
        textMarkItems.add("字体颜色");
        textMarkMenu = new BubblePopupView(this);
        textMarkMenu.setShowTouchLocation(true);
        textMarkMenu.setFocusable(true);

        textMarkMenuListener = new BubblePopupView.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                BookMarkE bookMarkE = new BookMarkE();
                bookMarkE.setMarkEBookId(mPageLoader.getCollBook().getId());//书籍id
                bookMarkE.setBookMarkEChapterNum(mPageLoader.getChapterPos());//章节序号
                bookMarkE.setBookMarkEPagePosition(mPageLoader.getPagePos());//页面序号
                bookMarkE.setMarkFirstLinePosition(binding.readPvContent.getFirstSelectTxtChar().getLineIndex());//首个字首行
                bookMarkE.setMarkLastLinePosition(binding.readPvContent.getLastSelectTxtChar().getLineIndex());//首个字末行
                bookMarkE.setMarkFirstCharPosition(binding.readPvContent.getFirstSelectTxtChar().getCharIndex());//首个字首行位置
                bookMarkE.setMarkLastCharPosition(binding.readPvContent.getLastSelectTxtChar().getCharIndex());//最后一个字末行位置
                switch(position){
                    case 0:
                        bookMarkE.setMarkKind("color");//标注种类：下划线
                        mBookMarkEService.addBookMarkE(bookMarkE);
                        clearSelect();
                        mPageLoader.refreshUi();
                }

            }
        };
    }

    public void initScanLongPressPop(){
        scanLongPressMenuItems.add("替换");
        scanLongPressMenuItems.add("搜索");
        scanLongPressMenu = new BubblePopupView(this);
        scanLongPressMenu.setShowTouchLocation(true);
        scanLongPressMenu.setFocusable(true);
        scanLongPressMenuListener = new BubblePopupView.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {

                String selectString;
                switch(position){
                    case 0:
                        if(mBook.getType().equals("本地书籍")) {
                            ToastUtils.showError("本地书籍不能替换!");
                            clearSelect();
                            mPageLoader.refreshUi();
                            break;
                        }
                        ReplaceRuleBean oldRuleBean = new ReplaceRuleBean();
                        oldRuleBean.setReplaceSummary("");
                        oldRuleBean.setEnable(true);
                        oldRuleBean.setRegex(binding.readPvContent.getSelectStr().trim());
                        oldRuleBean.setIsRegex(false);
                        oldRuleBean.setReplacement("");
                        oldRuleBean.setSerialNumber(0);
                        oldRuleBean.setUseTo(String.format("%s;%s",mBook.getSource(),mBook.getName()+"-"+mBook.getAuthor()));
                        ReplaceDialog replaceDialog = new ReplaceDialog(OpenReadActivity.this,oldRuleBean
                                ,()->{
                            ToastUtils.showSuccess("内容替换规则添加成功！");
                            clearSelect();
                            mPageLoader.refreshUi();
                        });
                        replaceDialog.show(getSupportFragmentManager(),"replaceRule");
                        break;
                    case 1:
                        selectString = StringUtils.deleteWhitespace(binding.readPvContent.getSelectStr());
                        MyAlertDialog.build(OpenReadActivity.this)
                                .setTitle(R.string.search_b)
                                .setItems(R.array.search_way,(dialog,which)->{
                                    String url = "";
                                    switch(which){
                                        case 0:
                                            url = URLCONST.BAI_DU_SEARCH;
                                            break;
                                        case 1:
                                            url = URLCONST.GOOGLE_SEARCH;
                                            break;
                                        case 2:
                                            url = URLCONST.YOU_DAO_SEARCH;
                                            break;
                                    }
                                    url = url.replace("{key}",selectString);
                                    Log.d("SEARCH_URL",url);
                                    try{
                                        Uri uri = Uri.parse(url);
                                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(intent);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        ToastUtils.showError(e.getLocalizedMessage());
                                    }
                                }).setNegativeButton("取消",null)
                                .show();
                        clearSelect();
                        break;
                }
            }
        };
    }

    /**
     * 清除选择
     */
    private void clearSelect() {
        binding.cursorLeft.setVisibility(View.INVISIBLE);
        binding.cursorRight.setVisibility(View.INVISIBLE);
        longPressMenu.hidePopupListWindow();
        binding.readPvContent.clearSelect();
    }

    private String isRead(){
        return (String) getIntent().getSerializableExtra("isRead");
    }

}

