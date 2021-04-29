package com.example.myreadproject8.util.net.crawler.read;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;

import java.util.ArrayList;

/**
 * created by ycq on 2021/4/22 0022
 * describe：
 */
public class MyTestCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "http://www.mingzhuxiaoshuo.com/";
    public static final String NOVEL_SEARCH = "http://www.mingzhuxiaoshuo.com/Search.asp?SearchFromB={key}";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "utf-8";

    @Override
    public String getSearchLink() {
        return null;
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public String getSearchCharset() {
        return null;
    }

    @Override
    public String getNameSpace() {
        return null;
    }

    @Override
    public Boolean isPost() {
        return null;
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        return null;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return null;
    }

    @Override
    public String getContentFormHtml(String html) {
        return null;
    }
}
