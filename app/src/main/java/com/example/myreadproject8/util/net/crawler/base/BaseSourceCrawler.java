package com.example.myreadproject8.util.net.crawler.base;

import android.text.TextUtils;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.entity.rule.InfoRule;
import com.example.myreadproject8.greendao.entity.rule.SearchRule;
import com.example.myreadproject8.greendao.entity.rule.TocRule;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.OkHttpUtils;
import com.example.myreadproject8.util.source.BaseAnalyzer;
import com.example.myreadproject8.util.source.MatcherAnalyzer;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.string.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * @author fengyue
 * @date 2021/2/14 18:28
 */
public abstract class BaseSourceCrawler implements ReadCrawler, BookInfoCrawler {

    protected final BookSource source;
    protected BaseAnalyzer analyzer;

    protected BaseSourceCrawler(BookSource source, BaseAnalyzer analyzer) {
        this.source = source;
        this.analyzer = analyzer;
    }

    @Override
    public String getSearchLink() {
        String[] urls = source.getSearchRule().getSearchUrl().split(",");
        String url = NetworkUtils.getAbsoluteURL(source.getSourceUrl(), urls[0]);
        if (urls.length > 1) {
            url += ",";
            url += urls[1];
        }
        return url;
    }

    @Override
    public String getCharset() {
        return MatcherAnalyzer.getCharset(source.getSourceCharset());
    }

    @Override
    public String getSearchCharset() {
        if (StringHelper.isEmpty(source.getSearchRule().getCharset())) {
            return MatcherAnalyzer.getCharset(source.getSourceCharset());
        }
        return MatcherAnalyzer.getCharset(source.getSearchRule().getCharset());
    }

    @Override
    public String getNameSpace() {
        return source.getSourceUrl();
    }

    @Override
    public Boolean isPost() {
        return source.getSearchRule().getSearchUrl().contains(",");
    }

    /**
     * searchRule???list????????????????????????
     *
     * @param obj
     * @param searchRule
     * @param books
     */
    protected void getBooksNoList(Object obj, SearchRule searchRule, ConcurrentMultiValueMap<SearchBookBean, Book> books) {
        List<String> names = analyzer.getStringList(searchRule.getName(), obj);
        //???????????????????????????????????????
        if (names.size() == 0) {
            Book book = new Book();
            getBookInfo(obj, book);
            if (!StringHelper.isEmpty(book.getName())) {
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } else {
            List<String> authors = analyzer.getStringList(searchRule.getAuthor(), obj);
            List<String> types = analyzer.getStringList(searchRule.getType(), obj);
            List<String> descs = analyzer.getStringList(searchRule.getDesc(), obj);
            List<String> wordCounts = analyzer.getStringList(searchRule.getWordCount(), obj);
            List<String> statuss = analyzer.getStringList(searchRule.getStatus(), obj);
            List<String> lastChapters = analyzer.getStringList(searchRule.getLastChapter(), obj);
            List<String> updateTimes = analyzer.getStringList(searchRule.getUpdateTime(), obj);
            List<String> imgs = analyzer.getStringList(searchRule.getImgUrl(), obj);
            List<String> chapterUrls = analyzer.getStringList(searchRule.getTocUrl(), obj);
            List<String> infoUrls = analyzer.getStringList(searchRule.getInfoUrl(), obj);
            for (int i = 0; i < names.size(); i++) {
                Book book = new Book();
                book.setName(names.get(i));
                book.setSource(source.getSourceUrl());
                if (authors.size() > i) book.setAuthor(authors.get(i));
                if (types.size() > i) book.setType(types.get(i));
                if (descs.size() > i) book.setDesc(descs.get(i));
                if (wordCounts.size() > i) book.setWordCount(wordCounts.get(i));
                if (statuss.size() > i) book.setStatus(statuss.get(i));
                if (lastChapters.size() > i) book.setNewestChapterTitle(lastChapters.get(i));
                if (updateTimes.size() > i) book.setUpdateDate(updateTimes.get(i));
                if (imgs.size() > i)
                    book.setImgUrl(imgs.get(i));
                if (chapterUrls.size() > i)
                    book.setChapterUrl(chapterUrls.get(i));
                if (infoUrls.size() > i)
                    book.setInfoUrl(infoUrls.get(i));
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
    }

    /**
     * searchRule???list???????????????????????????
     *
     * @param obj
     * @param searchRule
     * @param books
     */
    protected void getBooks(Object obj, SearchRule searchRule, ConcurrentMultiValueMap<SearchBookBean, Book> books) {
        List bookList = getList(searchRule.getList(), obj);
        if (bookList.size() == 0) {
            Book book = new Book();
            getBookInfo(obj, book);
            if (!StringHelper.isEmpty(book.getName())) {
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } else {
            for (Object bookObj : bookList) {
                bookObj = getListObj(bookObj);
                String name = analyzer.getString(searchRule.getName(), bookObj);
                if (TextUtils.isEmpty(name)) continue;
                String author = analyzer.getString(searchRule.getAuthor(), bookObj);
                String type = analyzer.getString(searchRule.getType(), bookObj);
                String desc = analyzer.getString(searchRule.getDesc(), bookObj);
                String wordCount = analyzer.getString(searchRule.getWordCount(), bookObj);
                String status = analyzer.getString(searchRule.getStatus(), bookObj);
                String lastChapter = analyzer.getString(searchRule.getLastChapter(), bookObj);
                String updateTime = analyzer.getString(searchRule.getUpdateTime(), bookObj);
                String imgUrl = analyzer.getString(searchRule.getImgUrl(), bookObj);
                String tocUrl = analyzer.getString(searchRule.getTocUrl(), bookObj);
                String infoUrl = analyzer.getString(searchRule.getInfoUrl(), bookObj);
                Book book = new Book();
                book.setName(name);
                book.setAuthor(author);
                book.setType(type);
                book.setDesc(desc);
                book.setWordCount(wordCount);
                book.setStatus(status);
                book.setNewestChapterTitle(lastChapter);
                book.setUpdateDate(updateTime);
                book.setImgUrl(imgUrl);
                book.setChapterUrl(tocUrl);
                book.setInfoUrl(infoUrl);
                book.setSource(source.getSourceUrl());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
    }



    /**
     * ??????????????????
     *
     * @param obj
     * @param chapters
     */
    public void getChapters(Object obj, ArrayList<Chapter> chapters) {
        TocRule tocRule = source.getTocRule();
        String baseUrl = NetworkUtils.getAbsoluteURL(getNameSpace(),
                analyzer.getString(tocRule.getChapterBaseUrl(), obj));
        if (StringHelper.isEmpty(baseUrl)) baseUrl = getNameSpace();
        if (StringHelper.isEmpty(tocRule.getChapterList())) {
            getChaptersNoList(obj, tocRule, baseUrl, chapters);
        } else {
            getChapters(obj, tocRule, baseUrl, chapters);
        }
        getNextPageChapters(obj, tocRule, baseUrl, chapters);
    }

    /**
     * tocRule???list????????????????????????
     *
     * @param obj
     * @param tocRule
     * @param baseUrl
     * @param chapters
     */
    protected void getChaptersNoList(Object obj, TocRule tocRule, String baseUrl, ArrayList<Chapter> chapters) {
        List<String> titles = analyzer.getStringList(tocRule.getChapterName(), obj);
        List<String> urls = analyzer.getStringList(tocRule.getChapterName(), obj);
        String lastTile = null;
        int i = 0;
        for (int j = 0; j < titles.size(); j++) {
            String title = titles.get(j);
            if (!StringHelper.isEmpty(lastTile) && lastTile.equals(title)) continue;
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            if (urls.size() > j) chapter.setUrl(NetworkUtils.getAbsoluteURL(baseUrl, urls.get(j)));
            chapters.add(chapter);
            lastTile = title;
        }
    }

    /**
     * tocRule???list???????????????????????????
     *
     * @param obj
     * @param tocRule
     * @param baseUrl
     * @param chapters
     */
    protected void getChapters(Object obj, TocRule tocRule, String baseUrl, ArrayList<Chapter> chapters) {
        List chapterList = getList(tocRule.getChapterList(), obj);
        String lastTile = null;
        int i = 0;
        for (Object chapterObj : chapterList) {
            chapterObj = getListObj(chapterObj);
            String title = analyzer.getString(tocRule.getChapterName(), chapterObj);
            if (!StringHelper.isEmpty(lastTile) && lastTile.equals(title)) continue;
            String url = analyzer.getString(tocRule.getChapterUrl(), chapterObj);
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            chapter.setUrl(NetworkUtils.getAbsoluteURL(baseUrl, url));
            chapters.add(chapter);
            lastTile = title;
        }
    }

    /**
     * ???????????????????????????
     *
     * @param obj
     * @param tocRule
     * @param baseUrl
     * @param chapters
     */
    protected void getNextPageChapters(Object obj, TocRule tocRule, String baseUrl, ArrayList<Chapter> chapters) {
        if (!StringHelper.isEmpty(tocRule.getTocUrlNext())) {
            String nextPageUrl = NetworkUtils.getAbsoluteURL(baseUrl,
                    analyzer.getString(tocRule.getTocUrlNext(), obj));
            if (!StringHelper.isEmpty(nextPageUrl)) {
                List<Chapter> nextChapters;
                try {
                    nextChapters = getChaptersFromHtml(OkHttpUtils.getHtml(nextPageUrl, getCharset()));
                    for (Chapter nextChapter : nextChapters) {
                        nextChapter.setNumber(chapters.size());
                        chapters.add(nextChapter);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param obj
     * @return
     */
    protected String getContent(Object obj) {
        String content = StringUtils.fromHtml(analyzer.
                getString(source.getContentRule().getContent(), obj));
        return getNextPageContent(obj, content);
    }

    /**
     * ???????????????????????????
     *
     * @param obj
     * @param content
     * @return
     */
    protected String getNextPageContent(Object obj, String content) {
        if (!StringHelper.isEmpty(source.getContentRule().getContentUrlNext())) {
            String baseUrl = NetworkUtils.getAbsoluteURL(getNameSpace(),
                    analyzer.getString(source.getContentRule().getContentBaseUrl(), obj));
            if (StringHelper.isEmpty(baseUrl)) baseUrl = getNameSpace();
            String nextPageUrl = NetworkUtils.getAbsoluteURL(baseUrl,
                    analyzer.getString(source.getContentRule().getContentUrlNext(), obj));
            if (!StringHelper.isEmpty(nextPageUrl)) {
                try {
                    content += getContentFormHtml(OkHttpUtils.getHtml(nextPageUrl, getCharset()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * ??????????????????
     *
     * @param obj
     * @param book
     * @return
     */
    protected Book getBookInfo(Object obj, Book book) {
        InfoRule infoRule = source.getInfoRule();
        if (StringHelper.isEmpty(book.getName()))
            book.setName(analyzer.getString(infoRule.getName(), obj));
        book.setSource(source.getSourceUrl());
        if (StringHelper.isEmpty(book.getAuthor()))
            book.setAuthor(analyzer.getString(infoRule.getAuthor(), obj));
        if (StringHelper.isEmpty(book.getType()))
            book.setType(analyzer.getString(infoRule.getType(), obj));
        if (StringHelper.isEmpty(book.getDesc()))
            book.setDesc(analyzer.getString(infoRule.getDesc(), obj));
        if (StringHelper.isEmpty(book.getStatus()))
            book.setStatus(analyzer.getString(infoRule.getStatus(), obj));
        if (StringHelper.isEmpty(book.getWordCount()))
            book.setWordCount(analyzer.getString(infoRule.getWordCount(), obj));
        if (StringHelper.isEmpty(book.getNewestChapterTitle()))
            book.setNewestChapterTitle(analyzer.getString(infoRule.getLastChapter(), obj));
        if (StringHelper.isEmpty(book.getUpdateDate()))
            book.setUpdateDate(analyzer.getString(infoRule.getUpdateTime(), obj));
        if (StringHelper.isEmpty(book.getImgUrl()))
            book.setImgUrl(analyzer.getString(infoRule.getImgUrl(), obj));
        if (StringHelper.isEmpty(book.getChapterUrl()))
            book.setChapterUrl(analyzer.getString(infoRule.getTocUrl(), obj));
        return book;
    }

    /**
     * ??????????????????????????????/????????????
     *
     * @param obj
     * @return
     */
    protected Object getListObj(Object obj) {
        return obj;
    }

    /**
     * ????????????/????????????
     *
     * @param str
     * @param obj
     * @return
     */
    protected List getList(String str, Object obj) {
        return analyzer.getStringList(str, obj);
    }

    public abstract Book getBookInfo(String json, Book book);
}
