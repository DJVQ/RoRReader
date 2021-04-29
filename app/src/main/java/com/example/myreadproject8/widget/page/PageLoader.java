package com.example.myreadproject8.widget.page;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.Font;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.BookMarkE;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.BookMarkEService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.util.file.IOUtils;
import com.example.myreadproject8.util.messenge.RxUtils;
import com.example.myreadproject8.util.read.ChapterContentHelp;
import com.example.myreadproject8.util.read.ScreenUtils;
import com.example.myreadproject8.util.string.StringUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.MeUtils;
import com.example.myreadproject8.util.utils.bitmap.BitmapUtil;
import com.gyf.immersionbar.ImmersionBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static android.graphics.Paint.FAKE_BOLD_TEXT_FLAG;
import static android.graphics.Paint.UNDERLINE_TEXT_FLAG;


/**
 * Created by fengyue on 20-11-21
 */

public abstract class PageLoader {
    private static final String TAG = "PageLoader";
    private boolean isMark = false;
    private String isRead;
    private List<BookMarkE> mBookMarkE;
    private BookMarkEService mBookMarkEService;
    // 当前页面的状态
    public static final int STATUS_LOADING = 1;         // 正在加载
    public static final int STATUS_LOADING_CHAPTER = 2; // 正在加载目录
    public static final int STATUS_FINISH = 3;          // 加载完成
    public static final int STATUS_ERROR = 4;           // 加载错误 (一般是网络加载情况)
    public static final int STATUS_EMPTY = 5;           // 空数据
    public static final int STATUS_PARING = 6;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 7;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 8;  // 获取到的目录为空

    private String errorMsg = "";
    // 默认的显示参数配置
    public static final int DEFAULT_MARGIN_HEIGHT = 28;
    public static final int DEFAULT_MARGIN_WIDTH = 15;
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TITLE_SIZE = 4;
    private static final int TIP_ALPHA = 180;

    // 当前章节列表
    protected List<Chapter> mChapterList;
    // 书本对象
    protected Book mCollBook;
    // 监听器
    protected OnPageChangeListener mPageChangeListener;

    private Context mContext;
    // 页面显示类
    private PageView mPageView;
    // 当前显示的页
    private TxtPage mCurPage;
    // 上一章的页面列表缓存
    private TxtChapter mPreChapter;
    // 当前章节的页面列表
    private TxtChapter mCurChapter;
    // 下一章的页面列表缓存
    private TxtChapter mNextChapter;

    // 绘制电池的画笔
    private Paint mBatteryPaint;
    // 绘制提示的画笔
    private TextPaint mTipPaint;
    // 绘制标题的画笔
    private TextPaint mTitlePaint;
    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private Paint mBgPaint;
    // 绘制书籍内容的画笔
    public TextPaint mTextPaint;
    // 阅读器的配置选项
    private Setting mSettingManager;
    // 被遮盖的页，或者认为被取消显示的页
    private TxtPage mCancelPage;
    //下划线画笔
    private Paint markLinePaint;

    String indent;
    private Disposable mPreLoadNextDisp;
    private Disposable mPreLoadPrevDisp;
    private CompositeDisposable compositeDisposable;

    /*****************params**************************/
    // 当前的状态
    protected int mStatus = STATUS_LOADING;
    // 判断章节列表是否加载完成
    protected boolean isChapterListPrepare;

    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean isFirstOpen = true;
    private boolean isClose;
    // 页面的翻页效果模式
    private PageMode mPageMode;
    // 加载器的颜色主题
//    private PageStyle mPageStyle;
    //书籍绘制区域的宽高
    private int mVisibleWidth;
    private int mVisibleHeight;
    //应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    //字体的颜色
    private int mTextColor;
    //字体类型
    private Typeface mTypeFace;
    //标题的大小
    private float mTitleSize;
    //字体的大小
    private float mTextSize;
    //行间距
    private float mTextInterval;
    //标题的行间距
    private float mTitleInterval;
    //段落距离(基于行间距的额外距离)
    private float mTextPara;
    private float mTitlePara;
    //电池的百分比
    private int mBatteryLevel;
    //当前页面的背景
    private int mBgColor;
    // 当前章
    protected int mCurChapterPos = 0;
    //是否向前翻页
    protected boolean isPrev;
    //上一章的记录
    private int mLastChapterPos = 0;
    private int readTextLength; //已读字符数
    private boolean resetReadAloud; //是否重新朗读
    private int readAloudParagraph = -1; //正在朗读章节

    private Bitmap bgBitmap;
    private ChapterContentHelp contentHelper = new ChapterContentHelp();


    /*****************************init params*******************************/
    public PageLoader(PageView pageView, Book collBook, Setting setting) {
        mPageView = pageView;
        mContext = pageView.getContext();
        mCollBook = collBook;
        mChapterList = new ArrayList<>(1);
        compositeDisposable = new CompositeDisposable();
        // 获取配置管理器
        mSettingManager = setting;
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        /*// 初始化书籍
        prepareBook();*/
    }

    public PageLoader(PageView pageView, Book collBook, Setting setting,List<BookMarkE> bookMarkE,String isRead) {
        this.isRead = isRead;
        mBookMarkEService = new BookMarkEService();
        mBookMarkE = bookMarkE;
        mPageView = pageView;
        mContext = pageView.getContext();
        mCollBook = collBook;
        mChapterList = new ArrayList<>(1);
        compositeDisposable = new CompositeDisposable();
        // 获取配置管理器
        mSettingManager = setting;
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        /*// 初始化书籍
        prepareBook();*/
    }

    public void init() {
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        // 初始化书籍
        prepareBook();
    }

    /**
     * 刷新界面
     */
    public void refreshUi() {
        initData();
        initPaint();
        upMargin();
    }

    private void initData() {
        /*// 获取配置管理器
        mSettingManager = SysManager.getSetting();*/
        // 获取配置参数
        mPageMode = mSettingManager.getPageMode();
        //获取字体
        getFont(mSettingManager.getFont());
        indent = StringUtils.repeat(StringUtils.halfToFull(" "), mSettingManager.getIntent());

        initBgBitmap();
        // 配置文字有关的参数
        setUpTextParams();
    }

    private void initBgBitmap() {
        if (!mSettingManager.bgIsColor()) {
            String bgPath = mSettingManager.getBgPath();
            if (bgPath == null) {
                return;
            }
            Resources resources = App.getApplication().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();//获取屏幕大小
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            try {
                if (mSettingManager.bgIsAssert()) {
                    bgBitmap = MeUtils.getFitAssetsSampleBitmap(mContext.getAssets(), bgPath, width, height);
                } else {
                    bgBitmap = BitmapUtil.getFitSampleBitmap(bgPath, width, height);
                }
                if (bgBitmap == null) {
                    ToastUtils.showError("背景加载失败，已加载默认背景");
                    bgBitmap = MeUtils.getFitAssetsSampleBitmap(mContext.getAssets(), "bg/p01.jpg", width, height);
                }
            } catch (Exception e) {
                ToastUtils.showError("背景加载失败，已加载默认背景");
                bgBitmap = MeUtils.getFitAssetsSampleBitmap(mContext.getAssets(), "bg/p01.jpg", width, height);
            }
        }
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private void setUpTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(mSettingManager.getReadWordSize());
        mTitleSize = mTextSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE);
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize * mSettingManager.getLineMultiplier() / 2);
        mTitleInterval = (int) (mTitleSize * mSettingManager.getLineMultiplier() / 2);
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize * mSettingManager.getParagraphSize());
        mTitlePara = (int) (mTitleSize * mSettingManager.getParagraphSize());
    }

    private void initPaint() {
        // 绘制提示的画笔
        mTipPaint = new TextPaint();
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setTypeface(mTypeFace);
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.setLetterSpacing(mSettingManager.getTextLetterSpacing());//设置水平间距
        }
        mTextPaint.setTypeface(mTypeFace);
        mTextPaint.setAntiAlias(true);

        //绘制标注下划线的笔
        markLinePaint = new TextPaint();
        markLinePaint.setColor(mTextColor);
        markLinePaint.setTextSize(mTextSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            markLinePaint.setLetterSpacing(mSettingManager.getTextLetterSpacing());//设置水平间距
        }
        markLinePaint.setTypeface(mTypeFace);
        markLinePaint.setAntiAlias(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTitleSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTitlePaint.setLetterSpacing(mSettingManager.getTextLetterSpacing());
        }
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.create(mTypeFace, Typeface.BOLD));
        mTitlePaint.setAntiAlias(true);

        // 绘制电池的画笔
        mBatteryPaint = new TextPaint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);
        mBatteryPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE - 3));
        mBatteryPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/number.ttf"));

        // 初始化页面样式
        setPageStyle();
    }

    private void initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode);
        mPageView.setBgColor(mBgColor);
    }

    /****************************** public method***************************/
    /**
     * 跳转到上一章
     *
     * @return
     */
    public boolean skipPreChapter() {
        isPrev = false;
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到下一章
     *
     * @return
     */
    public boolean skipNextChapter() {
        isPrev = false;
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到指定章节
     *
     * @param pos:从 0 开始。
     */
    public void skipToChapter(int pos) {
        isPrev = false;
        // 设置参数
        mCurChapterPos = pos;

        // 将上一章的缓存设置为null
        mPreChapter = null;
        // 如果当前下一章缓存正在执行，则取消
        if (mPreLoadNextDisp != null) {
            mPreLoadNextDisp.dispose();
        }
        if (mPreLoadPrevDisp != null) {
            mPreLoadPrevDisp.dispose();
        }
        // 将下一章缓存设置为null
        mNextChapter = null;

        // 打开指定章节
        openChapter();
    }

    /**
     * 跳转到指定的页
     *
     * @param pos
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return false;
        }
        mCurPage = getCurPage(pos);
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 翻到上一页
     *
     * @return
     */
    public boolean skipToPrePage() {
        return mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     *
     * @return
     */
    public boolean skipToNextPage() {
        return mPageView.autoNextPage();
    }

    /**
     * 翻到下一页,无动画
     */
    private void noAnimationToNextPage() {
        if (getPagePos() < mCurChapter.getPageSize() - 1) {
            skipToPage(getPagePos() + 1);
            return;
        }
        skipNextChapter();
    }

    /**
     * 翻到上一页,无动画
     */
    void noAnimationToPrePage() {
        if (getPagePos() > 0) {
            skipToPage(getPagePos() - 1);
            return;
        }
        skipPreChapter();
        skipToPage(mCurChapter.getPageSize() - 1);
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (!mPageView.isRunning() && !mSettingManager.isShowStatusBar()) {
            mPageView.drawCurPage(mPageMode == PageMode.SCROLL);
        }
    }

    /**
     * 更新电量
     *
     * @param level
     */
    public void updateBattery(int level) {
        if (!mPageView.isRunning() && !mSettingManager.isShowStatusBar()) {
            mBatteryLevel = level;
            mPageView.drawCurPage(mPageMode == PageMode.SCROLL);
        }
    }

    /**
     * 设置提示的文字大小
     *
     * @param textSize:单位为 px。
     */
    public void setTipTextSize(int textSize) {
        mTipPaint.setTextSize(textSize);

        // 如果屏幕大小加载完成
        mPageView.drawCurPage(false);
    }

    /**
     * 设置文字相关参数
     */
    public void setTextSize() {
        // 设置文字相关参数
        setUpTextParams();
        initPaint();
        refreshPagePara();
    }

    /**
     * 设置夜间模式
     *
     * @param dayMode
     */
    public void setNightMode(boolean dayMode) {
        if (!dayMode) {
            mBatteryPaint.setColor(Color.WHITE);
        } else {
            mBatteryPaint.setColor(Color.BLACK);
        }
    }

    /**
     * 设置页面样式
     */
    public void setPageStyle() {
        int textColorId;
        int bgColorId;
        textColorId = mSettingManager.getTextColor();
        bgColorId = mSettingManager.getBgColor();

        mTextColor = textColorId;
        mBgColor = bgColorId;
        mBatteryPaint.setColor(Color.BLACK);

        // 设置当前颜色样式
        mTipPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTextColor);
        mTextPaint.setColor(mTextColor);
        markLinePaint.setColor(mTextColor);
        mBatteryPaint.setColor(mTextColor);

        mBatteryPaint.setAlpha(TIP_ALPHA);

        mPageView.drawCurPage(false);
    }

    /**
     * 翻页动画
     *
     * @param pageMode:翻页模式
     * @see
     */
    public void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;

        mPageView.setPageMode(mPageMode);

        // 重新绘制当前页
        mPageView.drawCurPage(false);
    }

    /**
     * 设置字体
     *
     * @param font
     */
    public void setFont(Font font) {
        mSettingManager = SysManager.getSetting();
        //获取字体
        getFont(font);
        mTipPaint.setTypeface(mTypeFace);
        mTextPaint.setTypeface(mTypeFace);
        markLinePaint.setTypeface(mTypeFace);
        mTitlePaint.setTypeface(mTypeFace);
        refreshPagePara();
    }

    public void refreshPagePara() {
        // 取消缓存
        mPreChapter = null;
        mNextChapter = null;

        // 如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurChapter.getPageSize()) {
                mCurPage.position = mCurChapter.getPageSize() - 1;
            }

            // 重新获取指定页面
            mCurPage = mCurChapter.getPage(mCurPage.position);
        }

        mPageView.drawCurPage(false);
    }

    /**
     * 获取字体
     *
     * @param font
     */
    public void getFont(Font font) {
        String fontFileName = mSettingManager.getFont().toString() + ".ttf";
        if (font == Font.本地字体) {
            fontFileName = mSettingManager.getLocalFontName();
        }
        File fontFile = new File(APPCONST.FONT_BOOK_DIR + fontFileName);
        if (font == Font.默认字体 || !fontFile.exists()) {
            mTypeFace = null;
            if (!fontFile.exists()) {
                mSettingManager.setFont(Font.默认字体);
                SysManager.saveSetting(mSettingManager);
            }
        } else {
            try {
                mTypeFace = Typeface.createFromFile(fontFile);
            } catch (Exception e) {
                ToastUtils.showError(e.getLocalizedMessage());
                mSettingManager.setFont(Font.默认字体);
                SysManager.saveSetting(mSettingManager);
            }
        }
    }

    /**
     * 刷新章节
     *
     * @param chapter
     */
    public void refreshChapter(Chapter chapter) {
        chapter.setContent(null);
        ChapterService.getInstance().deleteChapterCacheFile(chapter);
        openChapter();
    }

    /**
     * 设置内容与屏幕的间距 单位为 px
     */
    public void upMargin() {
        prepareDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 设置页面切换监听
     *
     * @param listener
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }
    }

    /**
     * 获取当前页的状态
     *
     * @return
     */
    public int getPageStatus() {
        return mStatus;
    }

    /**
     * 获取书籍信息
     *
     * @return
     */
    public Book getCollBook() {
        return mCollBook;
    }

    /**
     * 获取章节目录。
     *
     * @return
     */
    public List<Chapter> getChapterCategory() {
        return mChapterList;
    }

    /**
     * 获取当前页的页码
     *
     * @return
     */
    public int getPagePos() {
        if (mCurPage == null) {
            return 0;
        }
        return mCurPage.position;
    }

    /**
     * 获取总页码
     *
     * @return
     */
    public int getAllPagePos() {
        if (mCurChapter == null) {
            return 0;
        }
        return mCurChapter.getPageSize();
    }

    /**
     * 获取当前章节的章节位置
     *
     * @return
     */
    public int getChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 获取距离屏幕的上高度
     *
     * @return
     */
    public int getMarginTop() {
        return mMarginTop;
    }

    /**
     * 获取距离屏幕的上高度
     *
     * @return
     */
    public int getMarginBottom() {
        return mMarginBottom;
    }

    /**
     * 初始化书籍
     */
    private void prepareBook() {
        mCurChapterPos = mCollBook.getHistoryChapterNum();
        mLastChapterPos = mCurChapterPos;
    }

    /**
     * 打开指定章节
     */
    public void openChapter() {
        isFirstOpen = false;

        if (!mPageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING_CHAPTER;
            mPageView.drawCurPage(false);
            return;
        }

        // 如果获取到的章节目录为空
        if (mChapterList.isEmpty()) {
            mStatus = STATUS_CATEGORY_EMPTY;
            mPageView.drawCurPage(false);
            return;
        }

        if (parseCurChapter()) {
            // 如果章节从未打开
            if (!isChapterOpen) {
                int position = mCollBook.getLastReadPosition();

                // 防止记录页的页号，大于当前最大页号
                if (position >= mCurChapter.getPageSize()) {
                    position = mCurChapter.getPageSize() - 1;
                }
                mCurPage = getCurPage(position);
                mCancelPage = mCurPage;
                // 切换状态
                isChapterOpen = true;
            } else {
                mCurPage = getCurPage(0);
            }
        } else {
            mCurPage = new TxtPage();
        }

        mPageView.drawCurPage(false);
    }

    /**
     * 解析章节并跳转到最后一页
     */
    protected void openChapterInLastPage() {
        if (parseCurChapter()) {
            mCurPage = getCurPage(getAllPagePos() - 1);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawCurPage(false);
    }

    public void chapterError(String msg) {
        //加载错误
        mStatus = STATUS_ERROR;
        errorMsg = msg;
        mPageView.drawCurPage(false);
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        if (mPreLoadNextDisp != null) {
            mPreLoadNextDisp.dispose();
        }
        if (mPreLoadPrevDisp != null) {
            mPreLoadPrevDisp.dispose();
        }

        clearList(mChapterList);

        mChapterList = null;
        mPreChapter = null;
        mCurChapter = null;
        mNextChapter = null;
        mPageView = null;
        mCurPage = null;
    }

    private void clearList(List list) {
        if (list != null) {
            list.clear();
        }
    }

    public boolean isClose() {
        return isClose;
    }

    public boolean isChapterOpen() {
        return isChapterOpen;
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     * @return
     */
    private TxtChapter loadPageList(int chapterPos) throws Exception {
        // 获取章节
        Chapter chapter = mChapterList.get(chapterPos);
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null;
        }
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        TxtChapter txtChapter = loadPages(chapter, reader);

        return txtChapter;
    }


    /*******************************abstract method***************************************/

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     *
     * @param chapter
     * @return
     */
    protected abstract BufferedReader getChapterReader(Chapter chapter) throws Exception;

    /**
     * 章节数据是否存在
     *
     * @return
     */
    public abstract boolean hasChapterData(Chapter chapter);

    /***********************************default method***********************************************/
    //绘制页面
    void drawPage(Bitmap bitmap, boolean isUpdate) {
        drawBackground(mPageView.getBgBitmap());//绘制背景
        if (!isUpdate) {
            drawContent(bitmap);//绘制内容
        }
        //更新绘制
        mPageView.invalidate();
    }

    /**
     * 绘制背景(位图)
     */
    private synchronized void drawBackground(Bitmap bitmap) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (!mSettingManager.bgIsColor()) {
            if (bgBitmap == null || bgBitmap.isRecycled()) {
                initBgBitmap();
            }
            Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bgBitmap, null, mDestRect, null);
        } else {
            canvas.drawColor(mBgColor);
        }
        drawBackground(canvas);//传入画布
    }

    public Bitmap getBgBitmap() {
        initBgBitmap();
        return bgBitmap;
    }

    /**
     * 绘制背景(画布)
     */
    private void drawBackground(Canvas canvas) {
        if (canvas == null) return;
        int tipMarginHeight = ScreenUtils.dpToPx(3);//dp转px
        String progress = (mStatus != STATUS_FINISH) ? ""//未加载完成则置空
                : getReadProgress(getChapterPos(), mChapterList.size(), getPagePos(), getAllPagePos());//加载完成则计算进度
        //绘制背景
        if (!mSettingManager.isShowStatusBar()) {//不显示状态栏
            if (!mChapterList.isEmpty()) {
                /*****初始化标题的参数********/
                //这里绘制text的y的起始点是text的基线的位置，而不是从text的头部的位置（章节顶部标题提示位置）
                float tipTop = tipMarginHeight - mTipPaint.getFontMetrics().top;
                //根据状态不一样，数据不一样
                if (mStatus != STATUS_FINISH) {//若未加载完成
                    if (isChapterListPrepare) {
                        String title = mChapterList.get(mCurChapterPos).getTitle();//获取章节标题
                        //替换标题内容（根据替换规则）
                        title = contentHelper.replaceContent(mCollBook.getName() + "-" + mCollBook.getAuthor(), mCollBook.getSource(), title, true);
                        canvas.drawText(title, mMarginLeft, tipTop, mTipPaint);//绘制顶部提示
                    }
                } else {//若加载完成
                    //直接替换
                    String title = contentHelper.replaceContent(mCollBook.getName() + "-" + mCollBook.getAuthor(), mCollBook.getSource(), mCurPage.title, true);
                    //过长则后面省略
                    title = TextUtils.ellipsize(title, mTipPaint, mDisplayWidth - mMarginLeft - mMarginRight - mTipPaint.measureText(progress), TextUtils.TruncateAt.END).toString();
                    //绘制
                    canvas.drawText(title, mMarginLeft, tipTop, mTipPaint);
                    /******绘制页码********/
                    // 计算底部的字显示的位置Y
                    float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
                    //要绘制的内容
                    String percent = (mCurPage.position + 1) + "/" + mCurChapter.getPageSize();
                    //绘制
                    canvas.drawText(percent, mMarginLeft, y, mTipPaint);
                }

                /*******绘制进度*******/
                //计算位置
                float progressTipLeft = mDisplayWidth - mMarginRight - mTipPaint.measureText(progress);
                //绘制
                canvas.drawText(progress, progressTipLeft, tipTop, mTipPaint);
            }
        } else {//显示状态栏
            float tipBottom = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
            //根据状态不一样，数据不一样
            if (mStatus != STATUS_FINISH) {
                if (isChapterListPrepare) {
                    canvas.drawText(mChapterList.get(mCurChapterPos).getTitle()
                            , mMarginLeft, tipBottom, mTipPaint);
                }
            } else {
                /******绘制页码********/
                String percent = (mCurPage.position + 1) + "/" + mCurChapter.getPageSize();
                //页码的x坐标
                float tipLeft = mDisplayWidth - 2 * mMarginRight - mTipPaint.measureText(percent + progress);
                canvas.drawText(percent, tipLeft, tipBottom, mTipPaint);

                String title = TextUtils.ellipsize(mCurPage.title, mTipPaint, tipLeft - 2 * mMarginRight, TextUtils.TruncateAt.END).toString();
                canvas.drawText(title, mMarginLeft, tipBottom, mTipPaint);
            }
            //绘制进度
            float progressTipLeft = mDisplayWidth - mMarginRight - mTipPaint.measureText(progress);
            canvas.drawText(progress, progressTipLeft, tipBottom, mTipPaint);
        }

        //绘制电池
        if (!mSettingManager.isShowStatusBar()) {//若不显示状态栏


            int visibleRight = mDisplayWidth - mMarginRight;
            int visibleBottom = mDisplayHeight - tipMarginHeight;//3px

            int outFrameWidth = (int) mTipPaint.measureText("xxx");
            int outFrameHeight = (int) mTipPaint.getTextSize();

            int polarHeight = ScreenUtils.dpToPx(6);
            int polarWidth = ScreenUtils.dpToPx(2);
            int border = 1;
            int innerMargin = 1;

            //电极的制作
            int polarLeft = visibleRight - polarWidth;
            int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
            Rect polar = new Rect(polarLeft, polarTop, visibleRight,
                    polarTop + polarHeight - ScreenUtils.dpToPx(2));

            mBatteryPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(polar, mBatteryPaint);

            //外框的制作
            int outFrameLeft = polarLeft - outFrameWidth;
            int outFrameTop = visibleBottom - outFrameHeight;
            int outFrameBottom = visibleBottom - ScreenUtils.dpToPx(2);
            Rect outFrame = new Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom);

            mBatteryPaint.setStyle(Paint.Style.STROKE);
            mBatteryPaint.setStrokeWidth(border);
            canvas.drawRect(outFrame, mBatteryPaint);


            //绘制电量
            mBatteryPaint.setStyle(Paint.Style.FILL);
            Paint.FontMetrics fontMetrics = mBatteryPaint.getFontMetrics();
            String batteryLevel = String.valueOf(mBatteryLevel);
            float batTextLeft = outFrameLeft + (outFrameWidth - mBatteryPaint.measureText(batteryLevel)) / 2 - ScreenUtils.dpToPx(1) / 2f;
            float batTextBaseLine = visibleBottom - outFrameHeight / 2f - fontMetrics.top / 2 - fontMetrics.bottom / 2 - ScreenUtils.dpToPx(1);
            canvas.drawText(batteryLevel, batTextLeft, batTextBaseLine, mBatteryPaint);

            /******绘制当前时间********/
            //底部的字显示的位置Y
            float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
            String time = StringUtils.dateConvert(System.currentTimeMillis(), "HH:mm");
            float x = (mDisplayWidth - mTipPaint.measureText(time)) / 2;
            canvas.drawText(time, x, y, mTipPaint);
        }
    }

    //绘制内容
    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);//新建画布
        int position;
        int markEPosition;
        int startTemp;
        if (mPageMode == PageMode.SCROLL) {//若是滚动
            canvas.drawColor(mBgColor);//设置背景色
        }
        /******绘制内容****/

        if (mStatus != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (mStatus) {
                case STATUS_LOADING:
                    tip = "正在加载章节内容...";
                    break;
                case STATUS_LOADING_CHAPTER:
                    tip = "正在加载目录列表...";
                    break;
                case STATUS_ERROR:
                    tip = "章节内容加载失败\n" + errorMsg;
                    break;
                case STATUS_EMPTY:
                    tip = "章节内容为空";
                    break;
                case STATUS_PARING:
                    tip = "正在拆分章节请等待...";
                    break;
                case STATUS_PARSE_ERROR:
                    tip = "文件解析错误";
                    break;
                case STATUS_CATEGORY_EMPTY:
                    tip = "目录列表为空";
                    break;
            }
            if (mStatus == STATUS_ERROR) {
                drawErrorMsg(canvas, tip, 0);
            } else {
                //将提示语句放到正中间
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float textHeight = fontMetrics.top - fontMetrics.bottom;//计算文字高度
                float textWidth = mTextPaint.measureText(tip);//计算文字宽度
                float pivotX = (mDisplayWidth - textWidth) / 2;
                float pivotY = (mDisplayHeight - textHeight) / 2;
                canvas.drawText(tip, pivotX, pivotY, mTextPaint);
            }
        } else {
            float top;
            if (mPageMode == PageMode.SCROLL) {//滚动
                top = -mTextPaint.getFontMetrics().top;
            } else {
                top = mMarginTop - mTextPaint.getFontMetrics().top;
                if (mSettingManager.isShowStatusBar()) {
                    top += ImmersionBar.getStatusBarHeight((Activity) mContext);
                }
            }
            Paint.FontMetrics fontMetricsForTitle = mTitlePaint.getFontMetrics();
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            //设置总距离
            float interval = mTextInterval + mTextPaint.getTextSize();
            float para = mTextPara + mTextPaint.getTextSize();
            float titleInterval = mTitleInterval + mTitlePaint.getTextSize();//行距
            float titlePara = mTitlePara + mTextPaint.getTextSize();//段距
            String str = null;
            int ppp = 0;//文字位置
            //对标题进行绘制
            boolean isLight;//是否显示高亮
            int titleLen = 0;


            for (int i = 0; i < mCurPage.titleLines; ++i) {//遍历每一行
                str = mCurPage.lines.get(i);

                titleLen += str.length();
                isLight = readAloudParagraph == 0;
                mTitlePaint.setColor(isLight ? mContext.getResources().getColor(R.color.sys_color) : mTextColor);

                //设置顶部间距
                if (i == 0) {
                    top += mTitlePara;//每次下移一行
                }
                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;//居中
                //绘制当前行标题



                for(position = 0;position<str.length();position++) {//遍历该行
                    canvas.drawText(String.valueOf(str.charAt(position)), start, top, mTitlePaint);
                    start += mTitlePaint.measureText(String.valueOf(str.charAt(position)));
                }
//                    System.out.println("4/28test"+String.valueOf(str.charAt(position)));
//                canvas.drawText("__", start, top+mTitleInterval/2, mTitlePaint);

                //char字体参数
                float leftposition = start;//左
                float rightposition = 0;//右
                float bottomposition = top + mTitlePaint.getFontMetrics().descent;//底
                float TextHeight = Math.abs(fontMetricsForTitle.ascent) + Math.abs(fontMetricsForTitle.descent);//高度

                if (mCurPage.txtLists != null) {
                    for (TxtChar c : mCurPage.txtLists.get(i).getCharsData()) {//遍历每行每个字,设置每个char的属性
                        rightposition = leftposition + c.getCharWidth();//计算字体右边位置
                        Point tlp = new Point();
                        c.setTopLeftPosition(tlp);//左上
                        tlp.x = (int) leftposition;
                        tlp.y = (int) (bottomposition - TextHeight);

                        Point blp = new Point();
                        c.setBottomLeftPosition(blp);//左下
                        blp.x = (int) leftposition;
                        blp.y = (int) bottomposition;

                        Point trp = new Point();
                        c.setTopRightPosition(trp);//右上
                        trp.x = (int) rightposition;
                        trp.y = (int) (bottomposition - TextHeight);

                        Point brp = new Point();
                        c.setBottomRightPosition(brp);//右下
                        brp.x = (int) rightposition;
                        brp.y = (int) bottomposition;
                        ppp++;
                        c.setIndex(ppp);

                        leftposition = rightposition;
                    }
                }

                //设置尾部间距
                if (i == mCurPage.titleLines - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }


            //对内容进行绘制
            int strLength = 0;

            for (int i = mCurPage.titleLines; i < mCurPage.lines.size(); ++i) {//遍历内容每一行,从标题下一行开始绘制
                str = mCurPage.lines.get(i);
                strLength = strLength + str.length();//行文本长度
                //段长
                int paragraphLength = mCurPage.position == 0 ? strLength + titleLen : mCurChapter.getPageLength(mCurPage.position - 1) + strLength;
                isLight = readAloudParagraph == mCurChapter.getParagraphIndex(paragraphLength);
                mTextPaint.setColor(isLight ? mContext.getResources().getColor(R.color.sys_color) : mTextColor);
//                mTextPaint.setFlags(FAKE_BOLD_TEXT_FLAG);
                if (!mSettingManager.isTightCom()) {//若不是紧凑排版
                    //staticLayout:用于绘制文字的类
                    Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                    if (needScale(str)) {//是否有间距
                        drawScaledText(canvas, str, width, mTextPaint, top, i, mCurPage.txtLists);
                    } else {
                        startTemp = mMarginLeft;
                        for(position = 0;position<str.length();position++) {
                            canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, mTextPaint);
                            if("yes".equals(isRead)) {
                                try {
                                    mBookMarkE = mBookMarkEService.isMarkLists(mCollBook.getId(), mCurChapterPos, mCurPage.position, i, position);
                                } catch (Exception e) {

                                }
                                if (mBookMarkE != null) {
                                    for (int markPosition = 0; markPosition < mBookMarkE.size(); markPosition++) {
                                        switch (mBookMarkE.get(markPosition).getMarkKind()) {
                                            case "line":
                                                markLinePaint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
                                                canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, markLinePaint);//绘制当前字符串
                                                break;
                                            case "markE":
                                                break;
                                            case "color":
                                                mTextPaint.setColor(mContext.getResources().getColor(R.color.md_red_500));
                                                canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, mTextPaint);//绘制当前字符串
                                                mTextPaint.setColor(mTextColor);
                                                break;
                                        }
                                    }
                                }
                            }
                            startTemp += mTextPaint.measureText(String.valueOf(str.charAt(position)));
                        }
//                        canvas.drawText(str, mMarginLeft, top, mTextPaint);
                    }
                } else {//紧凑排版，没有任何间距
//                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                    startTemp = mMarginLeft;
                    for(position = 0;position<str.length();position++) {
                        canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, mTextPaint);
                        if("yes".equals(isRead)) {
                            try {
                                mBookMarkE = mBookMarkEService.isMarkLists(mCollBook.getId(), mCurChapterPos, mCurPage.position, i, position);
                            } catch (Exception e) {}
                            if (mBookMarkE != null) {
                                for (int markPosition = 0; markPosition < mBookMarkE.size(); markPosition++) {
                                    switch (mBookMarkE.get(markPosition).getMarkKind()) {
                                        case "line":
                                            markLinePaint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                                            canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, markLinePaint);//绘制当前字符串
                                            break;
                                        case "markE":
                                            break;
                                        case "color":
                                            mTextPaint.setColor(mContext.getResources().getColor(R.color.md_red_500));
                                            canvas.drawText(String.valueOf(str.charAt(position)), startTemp, top, mTextPaint);//绘制当前字符串
                                            mTextPaint.setColor(mTextColor);
                                            break;
                                    }
                                }
                            }
                        }

                        startTemp += mTextPaint.measureText(String.valueOf(str.charAt(position)));
                    }
                }
                //记录文字位置 --开始
                float leftposition = mMarginLeft;
                if (isFirstLineOfParagraph(str)) {//段落第一行缩进
                    //canvas.drawText(blanks, x, top, mTextPaint);
                    float bw = StaticLayout.getDesiredWidth(indent, mTextPaint);
                    leftposition += bw;
                }
                float rightposition = 0;
                float bottomposition = top + mTextPaint.getFontMetrics().descent;
                float textHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);

                if (mCurPage.txtLists != null) {
                    for (TxtChar c : mCurPage.txtLists.get(i).getCharsData()) {
                        rightposition = leftposition + c.getCharWidth();
                        Point tlp = new Point();
                        c.setTopLeftPosition(tlp);
                        tlp.x = (int) leftposition;
                        tlp.y = (int) (bottomposition - textHeight);

                        Point blp = new Point();
                        c.setBottomLeftPosition(blp);
                        blp.x = (int) leftposition;
                        blp.y = (int) bottomposition;

                        Point trp = new Point();
                        c.setTopRightPosition(trp);
                        trp.x = (int) rightposition;
                        trp.y = (int) (bottomposition - textHeight);

                        Point brp = new Point();
                        c.setBottomRightPosition(brp);
                        brp.x = (int) rightposition;
                        brp.y = (int) bottomposition;

                        leftposition = rightposition;

                        ppp++;
                        c.setIndex(ppp);
                    }
                }
                //记录文字位置 --若发现\n则表示段落结束,否则为行结束
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
                }
            }
        }
    }

    //绘制有间距的文字
    private void drawScaledText(Canvas canvas, String line, float lineWidth, TextPaint paint, float top, int y, List<TxtLine> txtLists) {
        float x = mMarginLeft;

        if (isFirstLineOfParagraph(line)) {
            canvas.drawText(indent, x, top, paint);//绘制空格
            float bw = StaticLayout.getDesiredWidth(indent, paint);
            x += bw;
            line = line.substring(mSettingManager.getIntent());//去掉缩进字符
        }
        int gapCount = line.length() - 1;
        int i = 0;

        TxtLine txtList = new TxtLine();//每一行
        txtList.setCharsData(new ArrayList<>());//

        float d = ((mDisplayWidth - (mMarginLeft + mMarginRight)) - lineWidth) / gapCount;//每个字分得的间距
        for (; i < line.length(); i++) {//绘制行的字符不包括空格
            isMark = false;
            String c = String.valueOf(line.charAt(i));//将char转换为文本
            float cw = StaticLayout.getDesiredWidth(c, paint);//字符串宽度

            canvas.drawText(c, x, top, paint);//绘制当前字符串
            if("yes".equals(isRead)) {
                try {
                    mBookMarkE = mBookMarkEService.isMarkLists(mCollBook.getId(), mCurChapterPos, mCurPage.position, y, i);
                } catch (Exception e) {}
                if (mBookMarkE != null) {
                    for (int position = 0; position < mBookMarkE.size(); position++) {
                        switch (mBookMarkE.get(position).getMarkKind()) {
                            case "line":
                                markLinePaint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                                canvas.drawText(c, x, top, markLinePaint);//绘制当前字符串
                                break;
                            case "color":
                                paint.setColor(mContext.getResources().getColor(R.color.md_red_500));
                                canvas.drawText(c, x, top, paint);//绘制当前字符串
                                paint.setColor(mTextColor);
                                isMark = true;
                                break;
                            case "backColor":
                                break;
                        }
                    }
                }
            }

            //去除字符串
            TxtChar txtChar = new TxtChar();
            txtChar.setChardata(line.charAt(i));
            //设置宽度,头一个和最后一个字符只分得一半间距
            if (i == 0) txtChar.setCharWidth(cw + d / 2);
            if (i == gapCount) txtChar.setCharWidth(cw + d / 2);
            txtChar.setCharWidth(cw + d);
            ;//字宽
            //txtChar.Index = y;//每页每个字的位置
            txtList.getCharsData().add(txtChar);
            //往后变换位置,cw+d为增加间距后一个字符的位置
            x += cw + d;
        }
        if (txtLists != null) {
            txtLists.set(y, txtList);//pzl
        }
    }

    private void drawErrorMsg(Canvas canvas, String msg, float offset) {
        float textInterval = mTextInterval + mTextPaint.getTextSize();
        Layout tempLayout = new StaticLayout(msg, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
        List<String> linesData = new ArrayList<>();
        for (int i = 0; i < tempLayout.getLineCount(); i++) {
            linesData.add(msg.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
        }
        float pivotY = (mDisplayHeight - textInterval * linesData.size()) / 2f - offset;
        for (String str : linesData) {
            float textWidth = mTextPaint.measureText(str);
            float pivotX = (mDisplayWidth - textWidth) / 2;
            canvas.drawText(str, pivotX, pivotY, mTextPaint);
            pivotY += textInterval;
        }
    }

    //判断是不是段第一行
    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == (char) 12288 && line.charAt(1) == (char) 12288;
    }

    private boolean needScale(String line) {//判断不是空行
        return line != null && line.length() != 0 && line.charAt(line.length() - 1) != '\n';
    }

    //获取信息
    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 设置边距
        mMarginTop = mSettingManager.isShowStatusBar() ?
                ScreenUtils.dpToPx(mSettingManager.getPaddingTop() + DEFAULT_MARGIN_HEIGHT - 8) :
                ScreenUtils.dpToPx(mSettingManager.getPaddingTop() + DEFAULT_MARGIN_HEIGHT);
        mMarginBottom = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - (mMarginLeft + mMarginRight);
        mVisibleHeight = !mSettingManager.isShowStatusBar() ? mDisplayHeight - (mMarginTop + mMarginBottom)
                : mDisplayHeight - (mMarginTop + mMarginBottom) - mPageView.getStatusBarHeight();

        // 重置 PageMode
        mPageView.setPageMode(mPageMode);


        // 取消缓存
        mPreChapter = null;
        mNextChapter = null;

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(false);
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter();
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos);
                // 重新设置文章指针的位置
                mCurPage = getCurPage(mCurPage.position);
            }
            mPageView.drawCurPage(false);
        }
    }

    /**
     * 翻阅上一页
     *
     * @return
     */
    boolean prev() {

        // 判断是否能翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在上一页
            TxtPage prevPage = getPrevPage();
            if (prevPage != null) {
                mCancelPage = mCurPage;
                mCurPage = prevPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasPrevChapter()) {//上一章为空,则不翻页
            return false;
        }

        //加载完成但上一页不存在(上一章存在)
        mCancelPage = mCurPage;//将可能被遮盖或取消的页面设为当前页面
        if (parsePrevChapter()) {//若能加载上一章
            mCurPage = getPrevLastPage();//将当前页面设为上一章
            if (mStatus == STATUS_LOADING)
                mStatus = STATUS_FINISH;
        } else {//若不能加载上一章
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();//加载下一页面
        return true;
    }

    /**
     * 解析上一章数据
     *
     * @return:数据是否解析成功
     */
    boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        mNextChapter = mCurChapter;

        // 判断是否具有上一章缓存
        if (mPreChapter != null) {
            mCurChapter = mPreChapter;
            mPreChapter = null;

            // 回调
            chapterChangeCallback();
        } else {
            dealLoadPageList(prevChapter);
        }
        // 预加载上一页面
        preLoadPrevChapter();
        return mCurChapter != null;
    }

    private boolean hasPrevChapter() {
        //判断是否上一章节为空
        if (mCurChapterPos - 1 < 0) {
            return false;
        }
        return true;
    }

    /**
     * 翻到下一页
     *
     * @return:是否允许翻页
     */
    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在下一页
            TxtPage nextPage = getNextPage();
            if (nextPage != null) {
                mCancelPage = mCurPage;
                mCurPage = nextPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        // 解析下一章数据
        if (parseNextChapter()) {
            mCurPage = mCurChapter.getPage(0);
            if (mStatus == STATUS_LOADING)
                mStatus = STATUS_FINISH;
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    private boolean hasNextChapter() {
        // 判断是否到达目录最后一章
        if (mCurChapterPos + 1 >= mChapterList.size()) {
            return false;
        }
        return true;
    }

    boolean parseCurChapter() {
        // 解析数据
        dealLoadPageList(mCurChapterPos);
        // 预加载上一页和下一页面
        preLoadPrevChapter();
        preLoadNextChapter();
        return mCurChapter != null;
    }

    /**
     * 解析下一章数据
     *
     * @return:返回解析成功还是失败
     */
    boolean parseNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        mPreChapter = mCurChapter;

        // 是否下一章数据已经预加载了
        if (mNextChapter != null) {
            mCurChapter = mNextChapter;
            mNextChapter = null;
            // 回调
            chapterChangeCallback();
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter);
        }
        // 预加载下一页面
        preLoadNextChapter();
        return mCurChapter != null;
    }

    private void dealLoadPageList(int chapterPos) {
        try {
            mCurChapter = loadPageList(chapterPos);
            if (mCurChapter != null) {
                if (mCurChapter.getTxtPageList().isEmpty()) {
                    mStatus = STATUS_EMPTY;

                    // 添加一个空数据
                    TxtPage page = new TxtPage();
                    page.lines = new ArrayList<>(1);
                    mCurChapter.addPage(page);
                } else {
                    mStatus = STATUS_FINISH;
                }
            } else {
                mStatus = STATUS_LOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCurChapter = null;
            mStatus = STATUS_ERROR;
        }

        // 回调
        chapterChangeCallback();
    }

    private void chapterChangeCallback() {
        if (mPageChangeListener != null) {
            readAloudParagraph = -1;
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageChange(0, resetReadAloud);
            resetReadAloud = true;
            mPageChangeListener.onPageCountChange(mCurChapter != null ? mCurChapter.getPageSize() : 0);
        }
    }

    // 预加载上一章
    private void preLoadPrevChapter() {
        int prevChapter = mCurChapterPos - 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasPrevChapter()
                || !hasChapterData(mChapterList.get(prevChapter))) {
            return;
        }
        //如果之前正在加载则取消
        if (mPreLoadPrevDisp != null) {
            mPreLoadPrevDisp.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<TxtChapter>) e -> e.onSuccess(loadPageList(prevChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadPrevDisp = d;
                    }

                    @Override
                    public void onSuccess(TxtChapter txtChapter) {
                        if (txtChapter.getPosition() == mCurChapterPos - 1)
                            mPreChapter = txtChapter;
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                        mPreChapter = null;
                    }
                });
    }

    // 预加载下一章
    private void preLoadNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter()
                || !hasChapterData(mChapterList.get(nextChapter))) {
            return;
        }
        //如果之前正在加载则取消
        if (mPreLoadNextDisp != null) {
            mPreLoadNextDisp.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<TxtChapter>) e -> e.onSuccess(loadPageList(nextChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadNextDisp = d;
                    }

                    @Override
                    public void onSuccess(TxtChapter txtChapter) {
                        if (txtChapter.getPosition() == mCurChapterPos + 1)
                            mNextChapter = txtChapter;
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                        mNextChapter = null;
                    }
                });
    }

    // 取消翻页
    void pageCancel() {
        if (mCurPage.position == 0 && mCurChapterPos > mLastChapterPos) { // 加载到下一章取消了
            if (mPreChapter != null) {
                cancelNextChapter();
            } else {
                if (parsePrevChapter()) {
                    mCurPage = getPrevLastPage();
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else if (mCurChapter == null
                || (mCurPage.position == mCurChapter.getPageSize() - 1
                && mCurChapterPos < mLastChapterPos)) {  // 加载上一章取消了

            if (mNextChapter != null) {
                cancelPreChapter();
            } else {
                if (parseNextChapter()) {
                    mCurPage = mCurChapter.getPage(0);
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage;
        }
    }

    private void cancelNextChapter() {
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        mNextChapter = mCurChapter;
        mCurChapter = mPreChapter;
        mPreChapter = null;

        chapterChangeCallback();

        mCurPage = getPrevLastPage();
        mCancelPage = null;
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;
        // 重置页面列表
        mPreChapter = mCurChapter;
        mCurChapter = mNextChapter;
        mNextChapter = null;

        chapterChangeCallback();

        mCurPage = getCurPage(0);
        mCancelPage = null;
    }

    /**************************************private method********************************************/
    /**
     * 将章节数据，解析成页面列表
     * 加载页面
     * @param chapter：章节信息
     * @param br：章节的文本流
     * @return
     */
    private TxtChapter loadPages(Chapter chapter, BufferedReader br) {
        TxtChapter txtChapter = new TxtChapter(chapter.getNumber());//初始化章节序号

        List<String> lines = new ArrayList<>();//所有字符，每行为一个String字符串
        List<TxtLine> txtLists = new ArrayList<>();//所有行
        float rHeight = mVisibleHeight;
        int titleLinesCount = 0;//标题行数
        boolean showTitle = true; // 是否展示标题
        boolean firstLine = true;//是否是第一行
        String paragraph = chapter.getTitle();//默认展示标题
        paragraph = paragraph.trim() + "\n";//先用trim() 方法用于删除paragraph的头尾空白符,再加上换行符。
        try {
            while (showTitle || (paragraph = br.readLine()) != null) {//若展示标题或br第一行不为空
                paragraph = contentHelper.replaceContent(mCollBook.getName() + "-" + mCollBook.getAuthor(), mCollBook.getSource(), paragraph, true);//替换规则
                if (firstLine && !showTitle) {//是第一行且不显示标题
                    paragraph = paragraph.replace(chapter.getTitle(), "");//将标题替换为空字符串
                    firstLine = false;//第一行置为假
                }
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", "").trim();//正则表达式替换所有空白字符
                    // 如果只有换行符，那么就不执行
                    if (paragraph.equals("")) continue;
                    paragraph = indent + paragraph + "\n";//加上缩进的空白字符组成段落
                } else {//若展示标题则正文绘制部分高度减少
                    //设置 title 的顶部间距
                    rHeight -= mTitlePara;
                }
                addParagraphLength(txtChapter, paragraph.length());//将该章节该段落长度记录
                int wordCount = 0;
                String subStr = null;
                while (paragraph.length() > 0) {
                    //当前空间，是否容得下一行文字
                    if (showTitle) {
                        rHeight -= mTitlePaint.getTextSize();
                    } else {
                        rHeight -= mTextPaint.getTextSize();
                    }
                    // 一页已经填充满了，创建 TextPage
                    if (rHeight <= 0) {
                        // 创建Page
                        TxtPage page = new TxtPage();
                        page.position = txtChapter.getTxtPageList().size();//设置序号
                        page.title = chapter.getTitle();//设置标题
                        page.lines = new ArrayList<>(lines);//所有字符，每行为一个String字符串
                        page.txtLists = new ArrayList<>(txtLists);//所有行,每行为txtLine类型
                        page.titleLines = titleLinesCount;//标题行数
                        txtChapter.addPage(page);//先加入这页到本章
                        addTxtPageLength(txtChapter, page.getContent().length());
                        // 重置Lines
                        lines.clear();
                        txtLists.clear();
                        rHeight = mVisibleHeight;
                        titleLinesCount = 0;
                        continue;
                    }

                    //测量一行长度
                    if (mSettingManager.isTightCom()) {//紧凑排版
                        if (showTitle) {//显示标题
                            wordCount = mTitlePaint.breakText(paragraph,//如果宽度超过最大宽度则提前停止
                                    true, mVisibleWidth, null);
                        } else {
                            wordCount = mTextPaint.breakText(paragraph,
                                    true, mVisibleWidth, null);
                        }


                        subStr = paragraph.substring(0, wordCount);//该行长度
                        if (paragraph.substring(wordCount).equals("\n")) {
                            subStr += "\n";
                        }
                    } else {
                        Layout tempLayout;
                        if (showTitle) {//超出则换行
                            tempLayout = new StaticLayout(paragraph, mTitlePaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                        } else {
                            tempLayout = new StaticLayout(paragraph, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                        }
                        wordCount = tempLayout.getLineEnd(0);
                        subStr = paragraph.substring(0, wordCount);
                    }

                    if (!subStr.equals("\n")) {
                        //将一行字节，存储到lines中
                        lines.add(subStr);
                        //begin
                        //记录每个字的位置
                        char[] cs = subStr.replace((char) 12288, ' ').trim().toCharArray();//去除空格
                        TxtLine txtList = new TxtLine();//每一行
                        txtList.setCharsData(new ArrayList<>());
                        for (char c : cs) {
                            String mesasrustr = String.valueOf(c);
                            float charwidth = mTextPaint.measureText(mesasrustr);
                            if (showTitle) {
                                charwidth = mTitlePaint.measureText(mesasrustr);
                            }
                            TxtChar txtChar = new TxtChar();
                            txtChar.setChardata(c);
                            txtChar.setCharWidth(charwidth);//字宽
                            txtChar.setIndex(66);//每页每个字的位置
                            txtList.getCharsData().add(txtChar);
                        }
                        txtLists.add(txtList);
                        //end
                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount += 1;
                            rHeight -= mTitleInterval;
                        } else {
                            rHeight -= mTextInterval;
                        }
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount);
                }

                //增加段落的间距
                if (!showTitle && lines.size() != 0) {
                    rHeight = rHeight - mTextPara + mTextInterval;
                }

                if (showTitle) {
                    rHeight = rHeight - mTitlePara + mTitleInterval;
                    showTitle = false;
                }
            }

            if (lines.size() != 0) {
                //创建Page
                TxtPage page = new TxtPage();
                page.position = txtChapter.getTxtPageList().size();
                page.title = chapter.getTitle();
                page.lines = new ArrayList<>(lines);
                page.txtLists = new ArrayList<>(txtLists);
                page.titleLines = titleLinesCount;
                txtChapter.addPage(page);
                addTxtPageLength(txtChapter, page.getContent().length());
                //重置Lines
                lines.clear();
                txtLists.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
        }
        return txtChapter;
    }


    /**
     * 添加TxtChapter的长度(页)
     *
     * @param txtChapter
     * @param length
     */
    private void addTxtPageLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getTxtPageLengthList().isEmpty()) {
            txtChapter.addTxtPageLength(length);
        } else {
            txtChapter.addTxtPageLength(txtChapter.getTxtPageLengthList().get(txtChapter.getTxtPageLengthList().size() - 1) + length);
        }
    }

    /**
     * 添加TxtChapter段落长度
     *
     * @param txtChapter
     * @param length
     */
    private void addParagraphLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getParagraphLengthList().isEmpty()) {
            txtChapter.addParagraphLength(length);
        } else {
            txtChapter.addParagraphLength(txtChapter.getParagraphLengthList().get(txtChapter.getParagraphLengthList().size() - 1) + length);
        }
    }

    /**
     * @return:获取初始显示的页面
     */
    private TxtPage getCurPage(int pos) {
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos, resetReadAloud);
            resetReadAloud = true;
        }
        return mCurChapter.getPage(pos);
    }

    public TxtPage curPage() {
        return mCurPage;
    }

    /**
     * @return:获取上一个页面
     */
    private TxtPage getPrevPage() {
        int pos = mCurPage.position - 1;
        if (pos < 0) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos, resetReadAloud);
            resetReadAloud = true;
        }
        return mCurChapter.getPage(pos);
    }

    /**
     * @return:获取下一的页面
     */
    private TxtPage getNextPage() {
        int pos = mCurPage.position + 1;
        if (pos >= mCurChapter.getPageSize()) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos, resetReadAloud);
            resetReadAloud = true;
        }
        return mCurChapter.getPage(pos);
    }

    /**
     * @return:获取上一个章节的最后一页
     */
    private TxtPage getPrevLastPage() {
        int pos = mCurChapter.getPageSize() - 1;

        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos, resetReadAloud);
            resetReadAloud = true;
        }

        return mCurChapter.getPage(pos);
    }

    /**
     * 根据当前状态，决定是否能够翻页
     *
     * @return
     */
    private boolean canTurnPage() {

        if (!isChapterListPrepare) {
            return false;
        }

        if (mStatus == STATUS_PARSE_ERROR
                || mStatus == STATUS_PARING) {//本地文件加载错误或正在解析则不允许翻页
            return false;
        } else if (mStatus == STATUS_ERROR) {//加载错误则将状态设为正在加载
            mStatus = STATUS_LOADING;
        }
        return true;
    }

    /**
     * 获取书籍进度
     *
     * @param durChapterIndex
     * @param chapterAll
     * @param durPageIndex
     * @param durPageAll
     * @return
     */
    private static String getReadProgress(int durChapterIndex, int chapterAll, int durPageIndex, int durPageAll) {
        DecimalFormat df = new DecimalFormat("0.0%");
        if (chapterAll == 0 || (durPageAll == 0 && durChapterIndex == 0)) {
            return "0.0%";
        } else if (durPageAll == 0) {
            return df.format((durChapterIndex + 1.0f) / chapterAll);
        }
        String percent = df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll);
        if (percent.equals("100.0%") && (durChapterIndex + 1 != chapterAll || durPageIndex + 1 != durPageAll)) {
            percent = "99.9%";
        }
        return percent;
    }

    /**
     * * @return curPageLength 当前页字数
     */
    public int curPageLength() {
        if (getCurPage(getPagePos()) == null) return 0;
        if (getPageStatus() != STATUS_FINISH) return 0;
        String str;
        int strLength = 0;
        TxtPage txtPage = getCurPage(getPagePos());
        if (txtPage != null) {
            for (int i = txtPage.getTitleLines(); i < txtPage.size(); ++i) {
                str = txtPage.getLine(i);
                strLength = strLength + str.length();
            }
        }
        return strLength;
    }

    /**
     * @return 本页内容
     */
    public String getContent() {
        if (mCurChapter == null) return null;
        if (mCurChapter.getPageSize() == 0) return null;
        TxtPage txtPage = mCurPage;
        StringBuilder s = new StringBuilder();
        int size = txtPage.lines.size();
        //int start = mPageMode == PageMode.SCROLL ? Math.min(Math.max(0, linePos), size - 1) : 0;
        int start = 0;
        for (int i = start; i < size; i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * @return 本章未读内容
     */
    public String getUnReadContent() {
        if (mCurPage == null) return null;
        if (mCurChapter == null || mCurChapter.getPageSize() == 0) return null;
        StringBuilder s = new StringBuilder();
        String content = getContent();
        if (content != null) {
            s.append(content);
        }
        int mCurPagePos = getPagePos();
        content = getContentStartPage(mCurPagePos + 1);
        if (content != null) {
            s.append(content);
        }
        readTextLength = mCurPagePos > 0 ? mCurChapter.getPageLength(mCurPagePos - 1) : 0;
        /*if (mPageMode == PageAnimation.Mode.SCROLL) {
            for (int i = 0; i < Math.min(Math.max(0, linePos), curChapter().txtChapter.getPage(mCurPagePos).size() - 1); i++) {
                readTextLength += curChapter().txtChapter.getPage(mCurPagePos).getLine(i).length();
            }
        }*/
        return s.toString();
    }


    /**
     * @param page 开始页数
     * @return 从page页开始的的当前章节所有内容
     */
    private String getContentStartPage(int page) {
        if (mCurChapter == null) return null;
        if (mCurChapter.getTxtPageList().isEmpty()) return null;
        StringBuilder s = new StringBuilder();
        if (mCurChapter.getPageSize() > page) {
            for (int i = page; i < mCurChapter.getPageSize(); i++) {
                s.append(mCurChapter.getPage(i).getContent());
            }
        }
        return s.toString();
    }



    /**
     * --------------------
     * 检测获取按压坐标所在位置的字符，没有的话返回null
     * --------------------
     * author: huangwei
     * 2017年7月4日上午10:23:19
     */
    TxtChar detectPressTxtChar(float down_X2, float down_Y2) {
        TxtPage txtPage = mCurPage;
        if (txtPage == null) return null;
        List<TxtLine> txtLines = txtPage.txtLists;
        if (txtLines == null) return null;
        int linePosition = 0;
        int charPosition = 0;
        for (TxtLine l : txtLines) {//遍历所有行
            List<TxtChar> txtChars = l.getCharsData();//获取每一行字符
            if (txtChars != null) {
                for (TxtChar c : txtChars) {//遍历获取到的字符
                    Point leftPoint = c.getBottomLeftPosition();//左下
                    Point rightPoint = c.getBottomRightPosition();//右下
                    if (leftPoint != null && down_Y2 > leftPoint.y) {
                        break;// 说明是在下一行
                    }
                    if (leftPoint != null && rightPoint != null && down_X2 >= leftPoint.x && down_X2 <= rightPoint.x) {//传入的横坐标大于字符左下横坐标而小于字符右下横坐标
                        System.out.println("linePosition"+linePosition+"CharPosition"+charPosition);
                        c.setLineIndex(linePosition);
                        c.setCharIndex(charPosition);
                        return c;//此时找到对应的字符
                    }
                    charPosition++;
                }
                linePosition++;
            }

        }
        return null;
    }

    public boolean isPrev() {
        return isPrev;
    }

    public void setPrev(boolean prev) {
        isPrev = prev;
    }

    public void setmStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    /*****************************************interface*****************************************/

    public interface OnPageChangeListener {
        /**
         * 作用：章节切换的时候进行回调
         *
         * @param pos:切换章节的序号
         */
        void onChapterChange(int pos);

        /**
         * 作用：章节目录加载完成时候回调
         *
         * @param chapters：返回章节目录
         */
        void onCategoryFinish(List<Chapter> chapters);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         *
         * @param count:页面的数量
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         *
         * @param pos:当前的页面的序号
         */
        void onPageChange(int pos, boolean resetRead);

    }
}