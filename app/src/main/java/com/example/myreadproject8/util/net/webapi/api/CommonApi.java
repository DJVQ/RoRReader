package com.example.myreadproject8.util.net.webapi.api;

import com.example.myreadproject8.common.URLCONST;
import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.OkHttpUtils;
import com.example.myreadproject8.util.net.crawler.base.BookInfoCrawler;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.FYReadCrawler;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.string.StringHelper;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * created by ycq on 2021/4/13 0013
 * describe：
 */
public class CommonApi extends BaseApi{
    /**
     * 获取章节列表
     *
     * @param url
     * @param callback
     */
    public static void getBookChapters(String url, final ReadCrawler rc, boolean isRefresh, final ResultCallback callback) {
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        getCommonReturnHtmlStringApi(url, null, charset, isRefresh, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getChaptersFromHtml((String) o), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取章节列表
     *
     * @param url
     */
    public static Observable<List<Chapter>> getBookChapters(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            try {
                emitter.onNext(rc.getChaptersFromHtml(OkHttpUtils.getHtml(finalUrl, charset)));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }


    /**
     * 获取章节正文
     *
     * @param url
     * @param callback
     */

    public static void getChapterContent(String url, final ReadCrawler rc, final ResultCallback callback) {
        int tem = url.indexOf("\"");
        if (tem != -1) {
            url = url.substring(0, tem);
        }
        String charset = rc.getCharset();
        if (rc instanceof FYReadCrawler) {
            if (url.contains("47.105.152.62")) {
                url.replace("47.105.152.62", "novel.fycz.xyz");
            }
            if (!url.contains("novel.fycz.xyz")) {
                url = URLCONST.nameSpace_FY + url;
            }
        }
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        getCommonReturnHtmlStringApi(url, null, charset, true, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getContentFormHtml((String) o), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取章节正文
     *
     * @param url
     */

    public static Observable<String> getChapterContent(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            try {
                emitter.onNext(rc.getContentFormHtml(OkHttpUtils.getHtml(finalUrl, charset)));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }


    /**
     * 搜索书籍
     * @param key
     * @param callback
     */

    public static void search(String key, final ReadCrawler rc, final ResultCallback callback) {
        String charset = "utf-8";
        charset = rc.getCharset();
        getCommonReturnHtmlStringApi(makeSearchUrl(rc.getSearchLink(), key), null, charset, false, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getBooksFromSearchHtml((String) o), code);
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 搜索书籍
     * @param key
     */
    public static Observable<ConcurrentMultiValueMap<SearchBookBean, Book>> search(String key, final ReadCrawler rc) {
        String charset = "utf-8";
        charset = rc.getCharset();
        String finalCharset = charset;
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        return Observable.create(emitter -> {
            try {
                if (rc.isPost()) {
                    String url = rc.getSearchLink();
                    String[] urlInfo = url.split(",");
                    url = urlInfo[0];
                    String body = makeSearchUrl(urlInfo[1], key);
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset)));
                } else {
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(makeSearchUrl(rc.getSearchLink(), key), finalCharset)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }


    public static String makeSearchUrl(String url, String key) {
        return url.replace("{key}", key);
    }


    /**
     * 获取书籍详细信息
     *
     * @param book
     */
    public static Observable<Book> getBookInfo(final Book book, final BookInfoCrawler bic) {
        String url;
        if (StringHelper.isEmpty(book.getInfoUrl())) {
            url = book.getChapterUrl();
        } else {
            url = book.getInfoUrl();
        }
        url = NetworkUtils.getAbsoluteURL(bic.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            try {
                emitter.onNext(bic.getBookInfo(OkHttpUtils.getHtml(finalUrl, bic.getCharset()), book));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }


    /**
     * 获取书籍详细信息
     *
     * @param book
     * @param callback
     */
    public static void getBookInfo(final Book book, final BookInfoCrawler bic, final ResultCallback callback) {
        String url = book.getInfoUrl();
        if (StringHelper.isEmpty(url)) {
            url = book.getChapterUrl();
        }
        url = NetworkUtils.getAbsoluteURL(bic.getNameSpace(), url);
        getCommonReturnHtmlStringApi(url, null, bic.getCharset(), false, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(bic.getBookInfo((String) o, book), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


}
