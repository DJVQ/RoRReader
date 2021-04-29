package com.example.myreadproject8.util.search;

import androidx.annotation.NonNull;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.crawler.base.BookInfoCrawler;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * created by ycq on 2021/4/13 0013
 * describe：
 */
public class SearchEngine {

    private static final String TAG = "SearchEngine";

    //线程池
    private ExecutorService executorService;
    //调度器
    private Scheduler scheduler;

    private CompositeDisposable compositeDisposable;
    //搜索源
    private List<ReadCrawler> mSourceList = new ArrayList<>();

    //线程数
    private int threadsNum;

    private int searchSiteIndex;
    //搜索成功数目
    private int searchSuccessNum;
    //完成搜索线程数
    private int searchFinishNum;
    //搜索监听器
    private OnSearchListener searchListener;

    //获取搜索线程数
    public SearchEngine() {
        threadsNum = SharedPreUtils.getInstance().getInt(App.getMContext().getString(R.string.threadNum), 8);
    }
    //初始化搜索监听器
    public void setOnSearchListener(OnSearchListener searchListener) {
        this.searchListener = searchListener;
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngine(@NonNull List<ReadCrawler> sourceList) {
        mSourceList.clear();
        mSourceList.addAll(sourceList);//将数据库所有搜索源加入mSourceList
        executorService = Executors.newFixedThreadPool(threadsNum);//新建含threadsNum个线程的线程池
        scheduler = Schedulers.from(executorService);//设置调度器
        compositeDisposable = new CompositeDisposable();//初始化CompositeDisposable，用于防止内存泄漏
    }


    public void stopSearch() {

        if (compositeDisposable != null) compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        searchListener.loadMoreFinish(true);
    }


    /**
     * 刷新引擎
     * 重新载入搜索源
     * @param sourceList
     */
    public void refreshSearchEngine(@NonNull List<ReadCrawler> sourceList) {
        mSourceList.clear();
        mSourceList.addAll(sourceList);
    }

    /**
     * 关闭引擎
     */
    public void closeSearchEngine() {
        executorService.shutdown();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
        compositeDisposable = null;
    }

    /**
     * 搜索关键字
     *
     * @param keyword
     */
    public void search(String keyword) {
        if (mSourceList.size() == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            searchListener.loadMoreFinish(true);
            return;
        }
        searchSuccessNum = 0;
        searchSiteIndex = -1;
        searchFinishNum = 0;
        for (int i = 0; i < Math.min(mSourceList.size(), threadsNum); i++) {
            searchOnEngine(keyword);
        }
    }


    /**
     * 根据书名和作者搜索书籍
     *
     * @param title
     * @param author
     */
    public void search(String title, String author) {
        if (mSourceList.size() == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            searchListener.loadMoreFinish(true);
            return;
        }
        searchSuccessNum = 0;
        searchSiteIndex = -1;
        searchFinishNum = 0;
        for (int i = 0; i < Math.min(mSourceList.size(), threadsNum); i++) {

            searchOnEngine(title, author);
        }
    }

    //搜索
    private synchronized void searchOnEngine(final String title, final String author) {

        searchSiteIndex++;
        if (searchSiteIndex < mSourceList.size()) {
            ReadCrawler crawler = mSourceList.get(searchSiteIndex);
            String searchKey = title;
            if (crawler.getSearchCharset().toLowerCase().equals("gbk")) {
                try {
                    searchKey = URLEncoder.encode(title, crawler.getSearchCharset());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            //此处由于搜索比较耗时，使用Rxjava进行异步操作，返回搜索内容与书本的键值对
            CommonApi.search(searchKey, crawler)//该函数返回observable对象
                    .subscribeOn(scheduler)//执行在scheduler控制的线程
                    .observeOn(AndroidSchedulers.mainThread())//回调在主线程
                    .subscribe(new Observer<ConcurrentMultiValueMap<SearchBookBean, Book>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }//将Disposable加入compositeDisposable，方便统一释放

                        @Override
                        public void onNext(ConcurrentMultiValueMap<SearchBookBean, Book> bookSearchBeans) {
                            searchFinishNum++;
                            if (bookSearchBeans != null) {
                                List<Book> books = bookSearchBeans.getValues(new SearchBookBean(title, author));
                                if (books != null) {
                                    searchSuccessNum++;
                                    searchListener.loadMoreSearchBook(books);//加载书源在搜索界面
                                }
                            }

                            searchOnEngine(title, author);
                        }

                        @Override
                        public void onError(Throwable e) {
                            searchFinishNum++;
                            searchOnEngine(title, author);
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            if (searchFinishNum >= mSourceList.size()) {
                if (searchSuccessNum == 0) {
                    searchListener.searchBookError(new Throwable("未搜索到内容"));
                }
                searchListener.loadMoreFinish(true);

            }
        }

    }


    /**
     * description:根据关键词搜索
     * synchronized是可重入锁,锁死这段程序的入口，保证同一时间只有一个线程进入这段程序
     * 虽然不用synchronized执行程序也没出错，但由于使用了递归想要保证只有一条线程进入
     * 此函数入口，所以使用synchronized比较安全
     */
    private synchronized void searchOnEngine(String keyword) {
        searchSiteIndex++;
        if (searchSiteIndex < mSourceList.size()) {

            ReadCrawler crawler = mSourceList.get(searchSiteIndex);
            String searchKey = keyword;
            //设置编码
            if (crawler.getSearchCharset().toLowerCase().equals("gbk")) {
                try {
                    searchKey = URLEncoder.encode(keyword, crawler.getSearchCharset());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            //为CommonApi.search（Observable）设置观察者，调度器和订阅事件
            CommonApi.search(searchKey, crawler)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ConcurrentMultiValueMap<SearchBookBean, Book>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }
                        @Override
                        public void onNext(ConcurrentMultiValueMap<SearchBookBean, Book> bookSearchBeans) {
                            searchFinishNum++;
                            System.out.println("test1searchengine"+Thread.currentThread());
                            if (bookSearchBeans != null) {
                                searchSuccessNum++;
                                searchListener.loadMoreSearchBook(bookSearchBeans);
                            }
                            //由于synchronized是可重入锁，所以递归调用不会发生死锁
                            //这里递归的原因是想要执行下面searchListener.loadMoreFinish，否者搜索界面进度条会一直加载
                            searchOnEngine(keyword);
                        }
                        @Override
                        public void onError(Throwable e) {
                            searchFinishNum++;
                            searchOnEngine(keyword);
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } else {
            if (searchFinishNum >= mSourceList.size()) {
                if (searchSuccessNum == 0) {
                    searchListener.searchBookError(new Throwable("未搜索到内容"));
                }
                searchListener.loadMoreFinish(true);
            }
        }

    }

    public synchronized void getBookInfo(Book book, BookInfoCrawler bic, OnGetBookInfoListener listener){
        CommonApi.getBookInfo(book, bic)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Book>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Book book) {
                        listener.loadFinish(true);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        listener.loadFinish(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**********************************************************************************/
    public interface OnSearchListener {

        void loadMoreFinish(Boolean isAll);

        void loadMoreSearchBook(ConcurrentMultiValueMap<SearchBookBean, Book> items);

        void loadMoreSearchBook(List<Book> items);

        void searchBookError(Throwable throwable);

    }


    public interface OnGetBookInfoListener{
        void loadFinish(Boolean isSuccess);
    }



    public interface OnGetBookChaptersListener{
        void loadFinish(List<Chapter> chapters, Boolean isSuccess);
    }



    public interface OnGetChapterContentListener{
        void loadFinish(String content, Boolean isSuccess);
    }
}
