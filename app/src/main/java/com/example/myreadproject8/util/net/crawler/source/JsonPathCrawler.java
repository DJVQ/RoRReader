package com.example.myreadproject8.util.net.crawler.source;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.entity.rule.SearchRule;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.crawler.base.BaseSourceCrawler;
import com.example.myreadproject8.util.source.JsonPathAnalyzer;
import com.example.myreadproject8.util.string.StringHelper;
import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;



/**
 * @author fengyue
 * @date 2021/2/14 17:52
 */
public class JsonPathCrawler extends BaseSourceCrawler {

    private final JsonPathAnalyzer analyzer;

    public JsonPathCrawler(BookSource source) {
        super(source, new JsonPathAnalyzer());
        this.analyzer = (JsonPathAnalyzer) super.analyzer;
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String json) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        ReadContext rc = analyzer.getReadContext(json);
        SearchRule searchRule = source.getSearchRule();
        if (StringHelper.isEmpty(searchRule.getList())) {
            getBooksNoList(rc, searchRule, books);
        } else {
            getBooks(json, searchRule, books);
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String json) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        ReadContext rc = analyzer.getReadContext(json);
        getChapters(rc, chapters);
        return chapters;
    }

    @Override
    public String getContentFormHtml(String json) {
        ReadContext rc = analyzer.getReadContext(json);
        return getContent(rc);
    }


    protected Object getListObj(Object obj) {
        return analyzer.getReadContext(obj);
    }

    @Override
    protected List getList(String str, Object obj) {
        return analyzer.getReadContextList(str, analyzer.getReadContext(obj));
    }

    @Override
    public Book getBookInfo(String json, Book book) {
        ReadContext rc = analyzer.getReadContext(json);
        return getBookInfo(rc, book);
    }
}
