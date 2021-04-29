package com.example.myreadproject8.ui.activity.book;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.example.myreadproject8.AAATest.observer.MySingleObserver;
import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.common.URLCONST;
import com.example.myreadproject8.databinding.ActivityBookDetailBinding;
import com.example.myreadproject8.entity.SharedBook;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.activity.CatalogActivity;
import com.example.myreadproject8.ui.activity.OpenReadActivity;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.adapter.bookcase.BookTagAdapter;
import com.example.myreadproject8.ui.adapter.bookdetails.DetailCatalogAdapter;
import com.example.myreadproject8.ui.dialog.BookGroupDialog;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.SourceExchangeDialog;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.file.IOUtils;
import com.example.myreadproject8.util.file.ShareUtils;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;
import com.example.myreadproject8.util.messenge.RxUtils;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.BookInfoCrawler;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.source.BookSourceManager;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.string.StringUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.bitmap.BitmapUtil;
import com.example.myreadproject8.util.utils.bitmap.BlurTransformation;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class BookDetailedActivity extends BaseActivity {

    //√
    private ActivityBookDetailBinding binding;

    //当前页面的书籍对象
    private Book mBook;
    //搜索值对应的书本列表
    private ArrayList<Book> aBooks;
    //对Book进行操作的类
    private BookService mBookService;
    //对Chapter进行操作的类
    private ChapterService mChapterService;
    //搜索器（爬虫）
    private ReadCrawler mReadCrawler;
    //章节列表适配器
    private DetailCatalogAdapter mCatalogAdapter;
    //章节列表
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    //最新章节
    private ArrayList<Chapter> mNewestChapters = new ArrayList<>();
    //是否在书架
    private boolean isCollected;
    //换源对话框
    private SourceExchangeDialog mSourceDialog;
    //书源序号
    private int sourceIndex;
    //分组对话框
    private BookGroupDialog mBookGroupDia;
    //其他标记
    private List<String> tagList = new ArrayList<>();


    /**
     * description: Handler
     * √
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage( Message msg) {
            switch(msg.what){
                case 1:
                    if(!"本地书籍".equals(mBook.getType())){
                        mChapters.clear();
                        mNewestChapters.clear();
                        //初始化书籍信息
                        initBookInfo();
                        //初始化章节信息
                        initChapters(true);
                        //章节适配器
                        mCatalogAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2://换源
                    createChangeSourceDia();
                    break;
                case 3:
                    binding.pbLoading.setVisibility(View.GONE);
                    DialogCreator.createTipDialog(BookDetailedActivity.this,"未搜索到书籍，书源加载失败！");
                    break;
                case 4:
                    binding.pbLoading.setVisibility(View.GONE);
                    initOtherInfo();
                    break;
            }
        }
    };

    @Override
    protected void bindView() {
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * description: 初始化数据
     * √
     */
    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //初始化服务类
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        //接收多个不同的搜索源对象
        aBooks = (ArrayList<Book>) getIntent().getSerializableExtra(APPCONST.SEARCH_BOOK_BEAN);
        //接收搜索源标号
        sourceIndex = getIntent().getIntExtra(APPCONST.SOURCE_INDEX,0);
        //获取当前搜索源对应的书籍
        if(aBooks !=null){
            mBook = aBooks.get(sourceIndex);
        }else{
            mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
        }
        //初始化isCollected标识（是否已经存在书架中）
        isCollected = isBookCollected();
        if(isCollected){//若存在书架，则立即加载所有章节
            mChapters = (ArrayList<Chapter>)mChapterService.findBookAllChapterByBookId(mBook.getId());
        }
        //Dialog
        mSourceDialog = new SourceExchangeDialog(this,mBook);
        if(isBookSourceNotExist()){
            DialogCreator.createCommonDialog(this,"未知源",
                    "当前书籍不存在，是否切换搜索源?",false,(dialog,which)->{
                        mSourceDialog.show();
                    },null);
        }
        //初始化分组对话框
        mBookGroupDia = new BookGroupDialog(this);
        //初始化搜索器
        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
    }

    /**
     * description:初始化其他标记
     * √
     */
    public void initTagList(){
        tagList.clear();
        //类型
        String type = mBook.getType();
        if(!StringHelper.isEmpty(type))
            tagList.add("0:"+type);
        //字数
        String wordCount = mBook.getWordCount();
        if(!StringHelper.isEmpty(wordCount))
            tagList.add("1:"+wordCount);
        String status = mBook.getStatus();
        //状态
        if(!StringHelper.isEmpty(status))
            tagList.add("2:"+status);
        binding.ih.tflBookTag.setAdapter(new BookTagAdapter(this, tagList, 13));
    }

    /**
     * description:安装toolbar
     * √
     */
    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary,true);
        getSupportActionBar().setTitle(mBook.getName());
    }

    /**
     * description:初始化widget
     * √
     */
    @Override
    protected void initWidget() {
        super.initWidget();
        //获取书籍信息
        initBookInfo();

        //catalog(章节)初始化
        mCatalogAdapter = new DetailCatalogAdapter();
        binding.ic.bookDetailRvCatalog.setLayoutManager(new LinearLayoutManager(this));
        binding.ic.bookDetailRvCatalog.setAdapter(mCatalogAdapter);

        //初始化章节目录
        initChapters(false);

        mCatalogAdapter.setOnItemClickListener((view, pos) -> {
            mBook.setHistoryChapterNum(mChapters.size() - pos -1);
            mBook.setLastReadPosition(0);
            goReadActivity();
        });

        if(isCollected){
            binding.ib.bookDetailTvAdd.setText("移除书籍");
            binding.ib.bookDetailTvOpen.setText("继续阅读");
        }

        if(aBooks != null && aBooks.size()>0){
            if(isCollected){
                for(int i = 0;i < aBooks.size(); i++){
                    Book book = aBooks.get(i);
                    if(book.getSource().equals(mBook.getSource())){
                        book.setNewestChapterId("true");
                        sourceIndex = i;
                        break;
                    }
                }
            }else {
                aBooks.get(sourceIndex).setNewestChapterId("true");
            }
        }
        mSourceDialog.setABooks(aBooks);
        mSourceDialog.setSourceIndex(sourceIndex);
    }

    /**
     * description:初始化点击事件
     * √
     */
    @Override
    protected void initClick() {
        super.initClick();
        binding.ic.bookDetailTvCatalogMore.setOnClickListener(v->goToMoreChapter());
        binding.ib.flAddBookcase.setOnClickListener(view->{
            if(!isCollected){
                mBook.setNoReadNum(mChapters.size());
                mBook.setChapterTotalNum(0);
                mBookService.addBook(mBook);
                for(Chapter chapter : mChapters){
                    chapter.setId(StringHelper.getStringRandom(25));
                    chapter.setBookId(mBook.getId());
                }
                mChapterService.addChapters(mChapters);
                isCollected = true;
                ToastUtils.showSuccess("成功加入书架");
                binding.ib.bookDetailTvAdd.setText("移除书籍");
            }else {
                mBookService.deleteBookById(mBook.getId());
                isCollected = false;
                mBook.setHistoryChapterNum(0);
                mBook.setHistoryChapterId("未开始阅读");
                mBook.setLastReadPosition(0);
                ToastUtils.showSuccess("成功移除书籍");
                binding.ib.bookDetailTvAdd.setText("加入书架");
                binding.ib.bookDetailTvOpen.setText("开始阅读");
            }
        });
        binding.ib.flOpenBook.setOnClickListener(view -> goReadActivity());

        //换源对话框
        mSourceDialog.setOnSourceChangeListener((bean, pos) -> {
            Book bookTem = (Book)mBook.clone();
            bookTem.setChapterUrl(bean.getChapterUrl());
            if(!StringHelper.isEmpty(bean.getImgUrl())){
                bookTem.setImgUrl(bean.getImgUrl());
            }
            if (!StringHelper.isEmpty(bean.getType())) {
                bookTem.setType(bean.getType());
            }
            if (!StringHelper.isEmpty(bean.getDesc())) {
                bookTem.setDesc(bean.getDesc());
            }
            bookTem.setSource(bean.getSource());
            if (isCollected) {
                mBookService.updateBook(mBook, bookTem);
            }
            mBook = bookTem;
            mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
    }

    /**
     * description:
     * √
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if("本地书籍".equals((mBook.getType()))){
            getMenuInflater().inflate(R.menu.menu_book_detail_local,menu);
        }else {
            getMenuInflater().inflate(R.menu.menu_book_detail,menu);

        }
        return true;
    }

    /**
     * description:
     * √
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if("本地书籍".equals(mBook.getType())){
            MenuItem groupSetting = menu.findItem(R.id.action_group_setting);
            groupSetting.setVisible(isCollected);
        }else {
            MenuItem isUpdate = menu.findItem(R.id.action_is_update);
            MenuItem groupSetting = menu.findItem(R.id.action_group_setting);
            if(isCollected){
                isUpdate.setVisible(true);
                groupSetting.setVisible(true);
                isUpdate.setChecked(!mBook.getIsCloseUpdate());
            }else {
                isUpdate.setVisible(false);
                groupSetting.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * description: 导航栏点击
     * √
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_change_source:
                if(!NetworkUtils.isNetWorkAvailable()){
                    ToastUtils.showError("无网络连接！");
                    return true;
                }
                mSourceDialog.show();
                break;
            case R.id.action_share:
                shareBook();
                break;
            case R.id.action_reload:  //重新加载
                mHandler.sendEmptyMessage(1);
                break;
            case R.id.action_is_update://是否更新
                mBook.setIsCloseUpdate(!mBook.getIsCloseUpdate());
                mBookService.updateEntity(mBook);
                break;
            case R.id.action_open_link:  //打开链接
                Uri uri = Uri.parse(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(),mBook.getChapterUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.action_group_setting://设置分组
                mBookGroupDia.addGroup(mBook, new BookGroupDialog.OnGroup() {
                    @Override
                    public void change() {

                    }

                    @Override
                    public void addGroup() {

                    }
                });
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * description: 阅读/章节界面反馈结果处理
     * √
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK){
            switch (requestCode){
                case APPCONST.REQUEST_READ:
                    if(data == null){
                        return;
                    }
                    boolean isCollected = data.getBooleanExtra(APPCONST.RESULT_IS_COLLECTED,false);
                    int lastReadPosition = data.getIntExtra(APPCONST.RESULT_LAST_READ_POSITION,0);
                    int historyChapterPos = data.getIntExtra(APPCONST.RESULT_HISTORY_CHAPTER,0);
                    if(isCollected){
                        binding.ib.bookDetailTvAdd.setText("移除书籍");
                        binding.ib.bookDetailTvOpen.setText("继续阅读");
                        this.isCollected = true;
                        if(mChapters != null && mChapters.size()!=0){
                            mBook.setHistoryChapterNum(historyChapterPos);
                            mBook.setLastReadPosition(lastReadPosition);
                        }
                    }else {
                        mBook.setHistoryChapterNum(0);
                        mBook.setHistoryChapterId("未开始阅读");
                        mBook.setLastReadPosition(0);
                    }
                    mCatalogAdapter.notifyDataSetChanged();
                    break;
                case APPCONST.REQUEST_CHAPTER_PAGE:
                    int[] chapterAndPage = data.getIntArrayExtra(APPCONST.CHAPTER_PAGE);
                    mBook.setHistoryChapterNum(chapterAndPage[0]);
                    mBook.setLastReadPosition(chapterAndPage[1]);
                    goReadActivity();
                    break;
            }
        }

    }

    @Override
    protected void processLogic() {
        super.processLogic();
    }

    /***************************************************************************/
    /**
     * description:判断是否在书架
     * √
     */
    private boolean isBookCollected() {
        Book book = mBookService.findBookByAuthorAndName(mBook.getName(), mBook.getAuthor());
        if (book == null) {
            return false;
        } else {
            mBook = book;
            return true;
        }
    }
    /**
     * description:
     * √
     */
    private boolean isBookSourceNotExist(){
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        return source.getSourceEName() == null ;
    }
    /**
     * description:初始化书籍信息
     * √
     */
    private void initBookInfo(){
        binding.ih.bookDetailTvAuthor.setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        }
        initTagList();
        binding.ic.bookDetailTvDesc.setText("");
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        binding.ih.bookDetailSource.setText(String.format("书源：%s",source.getSourceName()));
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        if(rc instanceof BookInfoCrawler && StringHelper.isEmpty(mBook.getImgUrl())){
            binding.pbLoading.setVisibility(View.VISIBLE);
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            CommonApi.getBookInfo(mBook, bic, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    if(!App.isDestroy(BookDetailedActivity.this)){
                        mHandler.sendMessage(mHandler.obtainMessage(4));
                    }
                }

                @Override
                public void onError(Exception e) {
                    ToastUtils.showError("书籍加载失败");
                }
            });
        }else {
            initOtherInfo();
        }
    }

    //初始化其他书籍信息
    private void initOtherInfo(){
        binding.ic.bookDetailTvDesc.setText(String.format("\t\t\t\t%s",mBook.getDesc()));
        initTagList();
        if(!App.isDestroy(this)){
            binding.ih.bookDetailIvCover.load(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(),mBook.getImgUrl()),mBook.getName(),mBook.getAuthor());
        }
    }

    private RequestBuilder<Drawable> defaultCover(){
        return Glide.with(this)
                .load(R.mipmap.default_cover)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this,25)));
    }

    /**
     * description: 换源对话框
     */
    private  void createChangeSourceDia(){
        if(aBooks == null){
            mHandler.sendMessage(mHandler.obtainMessage(3));
            return;
        }
    }

    /**
     * description: 初始化章节目录
     */
    private void initChapters(boolean isChangeSource){
        if(mChapters.size()==0 && !"本地书籍".equals(mBook.getType())){
                if(isCollected){
                    mChapters = (ArrayList<Chapter>)mChapterService.findBookAllChapterByBookId(mBook.getId());
                }
            CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, isChangeSource, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                    mBook.setNewestChapterTitle(chapters.get(chapters.size()-1).getTitle());
                    if(isCollected){
                        int noReadNum = chapters.size() - mBook.getChapterTotalNum();
                        mBook.setNoReadNum(Math.max(noReadNum,0));
                        mChapterService.updateAllOldChapterData(mChapters, chapters, mBook.getId());
                        mBookService.updateEntity(mBook);
                        if(isChangeSource && SysManager.getSetting().isMatchChapter()){
                            if(mBookService.matchHistoryChapterPos(mBook,chapters)){
                                ToastUtils.showSuccess("历史章节匹配成功");
                            }else {
                                ToastUtils.showError("历史章节匹配失败");
                            }
                        }
                    }
                    mChapters = chapters;
                    int end = Math.max(0,mChapters.size() - 6);
                    for(int i = mChapters.size() - 1;i >= end; i--){
                        mNewestChapters.add(mChapters.get(i));
                    }
                    App.runOnUiThread(() -> mCatalogAdapter.refreshItems(mNewestChapters));
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("here is BookDetailActivity: "+e);
                    e.printStackTrace();
                    ToastUtils.showError("最新章节加载失败");
                }
            });
        }else {
            int end = Math.max(0,mChapters.size() - 6);
            for(int i = mChapters.size() - 1; i >= end; i--){
                mNewestChapters.add(mChapters.get(i));
                mCatalogAdapter.refreshItems(mNewestChapters);
            }
        }
    }


    /**
     * description: 前往阅读界面
     */
    private void goReadActivity(){
        if(!isCollected){
            mBookService.addBook(mBook);
        }
        Intent intent = new Intent(this, OpenReadActivity.class);
        intent.putExtra(APPCONST.isRead,"no");
        intent.putExtra(APPCONST.BOOK,mBook);
        intent.putExtra("isCollected",isCollected);
        startActivityForResult(intent,APPCONST.REQUEST_READ);
    }


    /**
     * description: 分享书籍
     * √
     */
    private void shareBook(){
        if("本地书籍".equals(mBook.getType())){
            File file = new File(mBook.getChapterUrl());
            if(!file.exists()){
                ToastUtils.showWarring("书籍源文件不存在，无法分享！");
                return;
            }
            try {
                ShareUtils.share(this,file,mBook.getName()+".txt","text/plain");
            }catch (Exception e){
                String dest = APPCONST.SHARE_FILE_DIR + File.separator + mBook.getName()+".txt";
                FileUtils.copy(mBook.getChapterUrl(),dest);
                ShareUtils.share(this,new File(dest),mBook.getName() + ".txt","text/plain");
            }
            return;
        }
        ToastUtils.showInfo("正在生成分享图片");

        //生成分享图片
        Single.create((SingleOnSubscribe<File>)emitter->{
            //使用url
            String url = SharedPreUtils.getInstance().getString(getString(R.string.downloadLink), URLCONST.LAN_ZOUS_URL);
            if(url == null)
                url = "";

            int maxLength = 1273 - 1 - url.length();

            SharedBook sharedBook = SharedBook.bookToSharedBook(mBook);

            url = url + "#" + GsonExtensionsKt.getGSON().toJson(sharedBook);

            Bitmap bitmap;

            //生成二维码
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            bitmap = QRCodeEncoder.syncEncodeQRCode(url, 360);
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            //具体图片生成步骤
            File share = makeShareFile(bitmap);
            if(share == null){
                ToastUtils.showError("分享图片生成失败");
            }
            emitter.onSuccess(share);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<File>() {
                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull File file) {
                        share(file);
                    }
                });
    }


    /**
     * 分享生成的图片
     *√
     * @param share
     */
    private void share(File share) {
        ShareUtils.share(this, share, "分享书籍", "image/png");
    }

    /**
     * description:生成分享图片
     */
    private File makeShareFile(Bitmap QRCode){
        FileOutputStream fos = null;
        try{
            //从assets中找到图床
            Bitmap back = BitmapFactory.decodeStream(getResources().getAssets().open("share.png")).copy(Bitmap.Config.ARGB_8888,true);
            int backWidth = back.getWidth();
            int backHeight = back.getHeight();

            int margin = 60;
            int marginTop = 24;

            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(true);
            Bitmap img = Bitmap.createBitmap(binding.ih.bookDetailIvCover.getDrawingCache()).copy(Bitmap.Config.ARGB_8888,true);
            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(false);
            img = BitmapUtil.getBitmap(img,152,209);

            //画布
            Canvas cv = new Canvas(back);
            cv.drawBitmap(img,margin,margin+marginTop*2,null);

            //画笔
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);// 设置抗锯齿
            textPaint.setFilterBitmap(true);//设置为true,则图像在动画进行中会滤掉对Bitmap图像的优化操作,加快显示速度
            textPaint.setColor(Color.BLACK);//设置颜色
            textPaint.setTextSize(40);

            //如果字符串超长，则返回按规则截断并添加省略号的字符串。
            String name = TextUtils.ellipsize(mBook.getName(),textPaint,backWidth - margin + marginTop * 3 -img.getWidth(),TextUtils.TruncateAt.END).toString();
            cv.drawText(name,margin+marginTop+img.getWidth(),margin+marginTop*4,textPaint);


            textPaint.setColor(getResources().getColor(R.color.origin));
            textPaint.setTextSize(32);
            cv.drawText(mBook.getAuthor(),margin+marginTop+img.getWidth(),margin+marginTop*6,textPaint);


            textPaint.setColor(Color.BLACK);
            cv.drawText(mBook.getType() == null ? "" : mBook.getType(), margin + marginTop + img.getWidth(), margin + marginTop * 8, textPaint);
            assert mBook.getSource() != null;
            cv.drawText("书源: "+BookSourceManager.getSourceNameByStr(mBook.getSource()),margin+img.getWidth()+marginTop,margin+marginTop*10,textPaint);

            int textSize = 35;
            int textInterval = textSize/2;
            textPaint.setTextSize(textSize);

            drawDesc(getDescLines(backWidth - margin*2,textPaint),textPaint,cv,margin+marginTop*4+img.getHeight(),margin,textInterval);

            cv.drawBitmap(QRCode,backWidth-QRCode.getWidth(),backHeight-QRCode.getHeight(),null);

            cv.save();//保存
            cv.restore();//储存

            File share = FileUtils.getFile(APPCONST.SHARE_FILE_DIR+mBook.getName()+"_share.png");
            fos = new FileOutputStream(share);
            back.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.flush();
            Log.i("tag","saveBitmap success"+share.getAbsolutePath());

            //回收
            back.recycle();
            img.recycle();
            QRCode.recycle();

            return share;

        }catch(Exception e){
            e.printStackTrace();
            ToastUtils.showError(e.getLocalizedMessage()+"");
            return null;
        }finally{
            IOUtils.close(fos);
        }
    }
    /**
     * 绘制简介
     *
     * @param lines
     * @param textPaint
     * @param canvas
     * @param top
     * @param left
     * @param textInterval
     */
    private void drawDesc(List<String> lines, TextPaint textPaint, Canvas canvas, int top, int left, int textInterval) {
        float interval = textInterval + textPaint.getTextSize();//行距
        for (String line : lines) {
            canvas.drawText(line, left, top, textPaint);
            top += interval;
        }
    }

    /**
     * 生成简介lines
     *
     * @param width
     * @param textPaint
     * @return
     */

    private List<String> getDescLines(int width, TextPaint textPaint) {
        List<String> lines = new ArrayList<>();
        //半角字符转全角字符（空两格）
        String desc = StringUtils.halfToFull("  ") + mBook.getDesc();
        int i = 0;
        int wordCount = 0;
        String subStr = null;
        while (desc.length() > 0) {
            if (i == 9) {//最多9行
                lines.add(TextUtils.ellipsize(desc, textPaint, width / 1.8f, TextUtils.TruncateAt.END).toString());
                break;
            }
            wordCount = textPaint.breakText(desc, true, width, null);
            //截取当前第一行
            subStr = desc.substring(0, wordCount);
            //加入line列表
            lines.add(subStr);
            //去除当前第一行
            desc = desc.substring(wordCount);
            i++;
        }
        return lines;
    }

    /**
     * 章节列表
     */
    public void goToMoreChapter() {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra(APPCONST.BOOK, mBook);
        startActivityForResult(intent, APPCONST.REQUEST_CHAPTER_PAGE);
    }

}