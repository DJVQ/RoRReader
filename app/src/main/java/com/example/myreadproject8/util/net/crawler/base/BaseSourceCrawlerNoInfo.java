package com.example.myreadproject8.util.net.crawler.base;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;

import java.util.ArrayList;



/**
 * @author fengyue
 * @date 2021/2/14 18:28
 */
public class BaseSourceCrawlerNoInfo implements ReadCrawler {
    protected final BaseSourceCrawler crawler;

    public BaseSourceCrawlerNoInfo(BaseSourceCrawler crawler) {
        this.crawler = crawler;
    }

    @Override
    public String getSearchLink() {
        return crawler.getSearchLink();
    }

    @Override
    public String getCharset() {
        return crawler.getCharset();
    }

    @Override
    public String getSearchCharset() {
        return crawler.getSearchCharset();
    }

    @Override
    public String getNameSpace() {
        return crawler.getNameSpace();
    }

    @Override
    public Boolean isPost() {
        return crawler.isPost();
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        return crawler.getBooksFromSearchHtml(html);
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return crawler.getChaptersFromHtml(html);
    }

    @Override
    public String getContentFormHtml(String html) {
        return crawler.getContentFormHtml(html);
    }
}
