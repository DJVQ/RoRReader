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

    //???
    private ActivityBookDetailBinding binding;

    //???????????????????????????
    private Book mBook;
    //??????????????????????????????
    private ArrayList<Book> aBooks;
    //???Book??????????????????
    private BookService mBookService;
    //???Chapter??????????????????
    private ChapterService mChapterService;
    //?????????????????????
    private ReadCrawler mReadCrawler;
    //?????????????????????
    private DetailCatalogAdapter mCatalogAdapter;
    //????????????
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    //????????????
    private ArrayList<Chapter> mNewestChapters = new ArrayList<>();
    //???????????????
    private boolean isCollected;
    //???????????????
    private SourceExchangeDialog mSourceDialog;
    //????????????
    private int sourceIndex;
    //???????????????
    private BookGroupDialog mBookGroupDia;
    //????????????
    private List<String> tagList = new ArrayList<>();


    /**
     * description: Handler
     * ???
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage( Message msg) {
            switch(msg.what){
                case 1:
                    if(!"????????????".equals(mBook.getType())){
                        mChapters.clear();
                        mNewestChapters.clear();
                        //?????????????????????
                        initBookInfo();
                        //?????????????????????
                        initChapters(true);
                        //???????????????
                        mCatalogAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2://??????
                    createChangeSourceDia();
                    break;
                case 3:
                    binding.pbLoading.setVisibility(View.GONE);
                    DialogCreator.createTipDialog(BookDetailedActivity.this,"??????????????????????????????????????????");
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
     * description: ???????????????
     * ???
     */
    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //??????????????????
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        //????????????????????????????????????
        aBooks = (ArrayList<Book>) getIntent().getSerializableExtra(APPCONST.SEARCH_BOOK_BEAN);
        //?????????????????????
        sourceIndex = getIntent().getIntExtra(APPCONST.SOURCE_INDEX,0);
        //????????????????????????????????????
        if(aBooks !=null){
            mBook = aBooks.get(sourceIndex);
        }else{
            mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
        }
        //?????????isCollected???????????????????????????????????????
        isCollected = isBookCollected();
        if(isCollected){//?????????????????????????????????????????????
            mChapters = (ArrayList<Chapter>)mChapterService.findBookAllChapterByBookId(mBook.getId());
        }
        //Dialog
        mSourceDialog = new SourceExchangeDialog(this,mBook);
        if(isBookSourceNotExist()){
            DialogCreator.createCommonDialog(this,"?????????",
                    "??????????????????????????????????????????????",false,(dialog,which)->{
                        mSourceDialog.show();
                    },null);
        }
        //????????????????????????
        mBookGroupDia = new BookGroupDialog(this);
        //??????????????????
        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
    }

    /**
     * description:?????????????????????
     * ???
     */
    public void initTagList(){
        tagList.clear();
        //??????
        String type = mBook.getType();
        if(!StringHelper.isEmpty(type))
            tagList.add("0:"+type);
        //??????
        String wordCount = mBook.getWordCount();
        if(!StringHelper.isEmpty(wordCount))
            tagList.add("1:"+wordCount);
        String status = mBook.getStatus();
        //??????
        if(!StringHelper.isEmpty(status))
            tagList.add("2:"+status);
        binding.ih.tflBookTag.setAdapter(new BookTagAdapter(this, tagList, 13));
    }

    /**
     * description:??????toolbar
     * ???
     */
    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary,true);
        getSupportActionBar().setTitle(mBook.getName());
    }

    /**
     * description:?????????widget
     * ???
     */
    @Override
    protected void initWidget() {
        super.initWidget();
        //??????????????????
        initBookInfo();

        //catalog(??????)?????????
        mCatalogAdapter = new DetailCatalogAdapter();
        binding.ic.bookDetailRvCatalog.setLayoutManager(new LinearLayoutManager(this));
        binding.ic.bookDetailRvCatalog.setAdapter(mCatalogAdapter);

        //?????????????????????
        initChapters(false);

        mCatalogAdapter.setOnItemClickListener((view, pos) -> {
            mBook.setHistoryChapterNum(mChapters.size() - pos -1);
            mBook.setLastReadPosition(0);
            goReadActivity();
        });

        if(isCollected){
            binding.ib.bookDetailTvAdd.setText("????????????");
            binding.ib.bookDetailTvOpen.setText("????????????");
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
     * description:?????????????????????
     * ???
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
                ToastUtils.showSuccess("??????????????????");
                binding.ib.bookDetailTvAdd.setText("????????????");
            }else {
                mBookService.deleteBookById(mBook.getId());
                isCollected = false;
                mBook.setHistoryChapterNum(0);
                mBook.setHistoryChapterId("???????????????");
                mBook.setLastReadPosition(0);
                ToastUtils.showSuccess("??????????????????");
                binding.ib.bookDetailTvAdd.setText("????????????");
                binding.ib.bookDetailTvOpen.setText("????????????");
            }
        });
        binding.ib.flOpenBook.setOnClickListener(view -> goReadActivity());

        //???????????????
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
     * ???
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if("????????????".equals((mBook.getType()))){
            getMenuInflater().inflate(R.menu.menu_book_detail_local,menu);
        }else {
            getMenuInflater().inflate(R.menu.menu_book_detail,menu);

        }
        return true;
    }

    /**
     * description:
     * ???
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if("????????????".equals(mBook.getType())){
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
     * description: ???????????????
     * ???
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_change_source:
                if(!NetworkUtils.isNetWorkAvailable()){
                    ToastUtils.showError("??????????????????");
                    return true;
                }
                mSourceDialog.show();
                break;
            case R.id.action_share:
                shareBook();
                break;
            case R.id.action_reload:  //????????????
                mHandler.sendEmptyMessage(1);
                break;
            case R.id.action_is_update://????????????
                mBook.setIsCloseUpdate(!mBook.getIsCloseUpdate());
                mBookService.updateEntity(mBook);
                break;
            case R.id.action_open_link:  //????????????
                Uri uri = Uri.parse(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(),mBook.getChapterUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.action_group_setting://????????????
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
     * description: ??????/??????????????????????????????
     * ???
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
                        binding.ib.bookDetailTvAdd.setText("????????????");
                        binding.ib.bookDetailTvOpen.setText("????????????");
                        this.isCollected = true;
                        if(mChapters != null && mChapters.size()!=0){
                            mBook.setHistoryChapterNum(historyChapterPos);
                            mBook.setLastReadPosition(lastReadPosition);
                        }
                    }else {
                        mBook.setHistoryChapterNum(0);
                        mBook.setHistoryChapterId("???????????????");
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
     * description:?????????????????????
     * ???
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
     * ???
     */
    private boolean isBookSourceNotExist(){
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        return source.getSourceEName() == null ;
    }
    /**
     * description:?????????????????????
     * ???
     */
    private void initBookInfo(){
        binding.ih.bookDetailTvAuthor.setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        }
        initTagList();
        binding.ic.bookDetailTvDesc.setText("");
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        binding.ih.bookDetailSource.setText(String.format("?????????%s",source.getSourceName()));
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
                    ToastUtils.showError("??????????????????");
                }
            });
        }else {
            initOtherInfo();
        }
    }

    //???????????????????????????
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
     * description: ???????????????
     */
    private  void createChangeSourceDia(){
        if(aBooks == null){
            mHandler.sendMessage(mHandler.obtainMessage(3));
            return;
        }
    }

    /**
     * description: ?????????????????????
     */
    private void initChapters(boolean isChangeSource){
        if(mChapters.size()==0 && !"????????????".equals(mBook.getType())){
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
                                ToastUtils.showSuccess("????????????????????????");
                            }else {
                                ToastUtils.showError("????????????????????????");
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
                    ToastUtils.showError("????????????????????????");
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
     * description: ??????????????????
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
     * description: ????????????
     * ???
     */
    private void shareBook(){
        if("????????????".equals(mBook.getType())){
            File file = new File(mBook.getChapterUrl());
            if(!file.exists()){
                ToastUtils.showWarring("??????????????????????????????????????????");
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
        ToastUtils.showInfo("????????????????????????");

        //??????????????????
        Single.create((SingleOnSubscribe<File>)emitter->{
            //??????url
            String url = SharedPreUtils.getInstance().getString(getString(R.string.downloadLink), URLCONST.LAN_ZOUS_URL);
            if(url == null)
                url = "";

            int maxLength = 1273 - 1 - url.length();

            SharedBook sharedBook = SharedBook.bookToSharedBook(mBook);

            url = url + "#" + GsonExtensionsKt.getGSON().toJson(sharedBook);

            Bitmap bitmap;

            //???????????????
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            bitmap = QRCodeEncoder.syncEncodeQRCode(url, 360);
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            //????????????????????????
            File share = makeShareFile(bitmap);
            if(share == null){
                ToastUtils.showError("????????????????????????");
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
     * ?????????????????????
     *???
     * @param share
     */
    private void share(File share) {
        ShareUtils.share(this, share, "????????????", "image/png");
    }

    /**
     * description:??????????????????
     */
    private File makeShareFile(Bitmap QRCode){
        FileOutputStream fos = null;
        try{
            //???assets???????????????
            Bitmap back = BitmapFactory.decodeStream(getResources().getAssets().open("share.png")).copy(Bitmap.Config.ARGB_8888,true);
            int backWidth = back.getWidth();
            int backHeight = back.getHeight();

            int margin = 60;
            int marginTop = 24;

            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(true);
            Bitmap img = Bitmap.createBitmap(binding.ih.bookDetailIvCover.getDrawingCache()).copy(Bitmap.Config.ARGB_8888,true);
            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(false);
            img = BitmapUtil.getBitmap(img,152,209);

            //??????
            Canvas cv = new Canvas(back);
            cv.drawBitmap(img,margin,margin+marginTop*2,null);

            //??????
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);// ???????????????
            textPaint.setFilterBitmap(true);//?????????true,???????????????????????????????????????Bitmap?????????????????????,??????????????????
            textPaint.setColor(Color.BLACK);//????????????
            textPaint.setTextSize(40);

            //?????????????????????????????????????????????????????????????????????????????????
            String name = TextUtils.ellipsize(mBook.getName(),textPaint,backWidth - margin + marginTop * 3 -img.getWidth(),TextUtils.TruncateAt.END).toString();
            cv.drawText(name,margin+marginTop+img.getWidth(),margin+marginTop*4,textPaint);


            textPaint.setColor(getResources().getColor(R.color.origin));
            textPaint.setTextSize(32);
            cv.drawText(mBook.getAuthor(),margin+marginTop+img.getWidth(),margin+marginTop*6,textPaint);


            textPaint.setColor(Color.BLACK);
            cv.drawText(mBook.getType() == null ? "" : mBook.getType(), margin + marginTop + img.getWidth(), margin + marginTop * 8, textPaint);
            assert mBook.getSource() != null;
            cv.drawText("??????: "+BookSourceManager.getSourceNameByStr(mBook.getSource()),margin+img.getWidth()+marginTop,margin+marginTop*10,textPaint);

            int textSize = 35;
            int textInterval = textSize/2;
            textPaint.setTextSize(textSize);

            drawDesc(getDescLines(backWidth - margin*2,textPaint),textPaint,cv,margin+marginTop*4+img.getHeight(),margin,textInterval);

            cv.drawBitmap(QRCode,backWidth-QRCode.getWidth(),backHeight-QRCode.getHeight(),null);

            cv.save();//??????
            cv.restore();//??????

            File share = FileUtils.getFile(APPCONST.SHARE_FILE_DIR+mBook.getName()+"_share.png");
            fos = new FileOutputStream(share);
            back.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.flush();
            Log.i("tag","saveBitmap success"+share.getAbsolutePath());

            //??????
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
     * ????????????
     *
     * @param lines
     * @param textPaint
     * @param canvas
     * @param top
     * @param left
     * @param textInterval
     */
    private void drawDesc(List<String> lines, TextPaint textPaint, Canvas canvas, int top, int left, int textInterval) {
        float interval = textInterval + textPaint.getTextSize();//??????
        for (String line : lines) {
            canvas.drawText(line, left, top, textPaint);
            top += interval;
        }
    }

    /**
     * ????????????lines
     *
     * @param width
     * @param textPaint
     * @return
     */

    private List<String> getDescLines(int width, TextPaint textPaint) {
        List<String> lines = new ArrayList<>();
        //??????????????????????????????????????????
        String desc = StringUtils.halfToFull("  ") + mBook.getDesc();
        int i = 0;
        int wordCount = 0;
        String subStr = null;
        while (desc.length() > 0) {
            if (i == 9) {//??????9???
                lines.add(TextUtils.ellipsize(desc, textPaint, width / 1.8f, TextUtils.TruncateAt.END).toString());
                break;
            }
            wordCount = textPaint.breakText(desc, true, width, null);
            //?????????????????????
            subStr = desc.substring(0, wordCount);
            //??????line??????
            lines.add(subStr);
            //?????????????????????
            desc = desc.substring(wordCount);
            i++;
        }
        return lines;
    }

    /**
     * ????????????
     */
    public void goToMoreChapter() {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra(APPCONST.BOOK, mBook);
        startActivityForResult(intent, APPCONST.REQUEST_CHAPTER_PAGE);
    }

}