package com.example.myreadproject8.ui.adapter.holder.search;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.ui.adapter.bookcase.BookTagAdapter;
import com.example.myreadproject8.ui.adapter.holder.ViewHolderImpl;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.crawler.base.BookInfoCrawler;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.search.SearchEngine;
import com.example.myreadproject8.util.source.BookSourceManager;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.utils.KeyWordUtils;
import com.example.myreadproject8.widget.cover_image_view.CoverImageView;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ycq on 2021/4/14 0014
 * describe：
 */
public class SearchBookHolder extends ViewHolderImpl<SearchBookBean> {
    private Activity activity;
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;
    private String keyWord;
    private List<String> tagList = new ArrayList<>();

    public SearchBookHolder(Activity activity, ConcurrentMultiValueMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine, String keyWord) {
        this.activity = activity;
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
        this.keyWord = keyWord;
    }


    private CoverImageView ivBookImg;
    private TextView tvBookName;
    private TagFlowLayout tflBookTag;
    private TextView tvDesc;
    private TextView tvAuthor;
    private TextView tvSource;
    private TextView tvNewestChapter;

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_search_book_item;
    }

    @Override
    public void initView() {
        ivBookImg = findById(R.id.iv_book_img);
        tvBookName = findById(R.id.tv_book_name);
        tflBookTag = findById(R.id.tfl_book_tag);
        tvAuthor = findById(R.id.tv_book_author);
        tvDesc = findById(R.id.tv_book_desc);
        tvSource = findById(R.id.tv_book_source);
        tvNewestChapter = findById(R.id.tv_book_newest_chapter);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBind(SearchBookBean data, int pos) {
        //获取每一个搜索源中（若存在）的书籍对象
        List<Book> aBooks = mBooks.getValues(data);
        //获取搜到的书的搜索源数目
        int bookCount = aBooks.size();
        Book book = aBooks.get(0);
        //获取搜索源
        BookSource source = BookSourceManager.getBookSourceByStr(book.getSource());
        //获取搜索器
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        //初始化搜索界面书目
        books2SearchBookBean(data, aBooks);
        //设置搜索界面书目图片
        if (!StringHelper.isEmpty(data.getImgUrl())) {
            if (!App.isDestroy((Activity) getContext())) {
                ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), data.getImgUrl()), data.getName(), data.getAuthor());
            }
        }
        //把搜索关键词标红
        KeyWordUtils.setKeyWord(tvBookName, data.getName(), keyWord);
        if (!StringHelper.isEmpty(data.getAuthor())) {
            KeyWordUtils.setKeyWord(tvAuthor, data.getAuthor(), keyWord);
        }
        //初始化状态
        initTagList(data);
        //设置最新章节
        if (!StringHelper.isEmpty(data.getLastChapter())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, data.getLastChapter()));
        }
        //设置简介
        if (!StringHelper.isEmpty(data.getDesc())) {
            tvDesc.setText(String.format("简介:%s", data.getDesc()));
        }
        //设置搜索源名称
        tvSource.setText(getContext().getString(R.string.source_title_num, source.getSourceName(), bookCount));
        //设置一定条件下刷新页面数据，1秒一次
        App.getHandler().postDelayed(() -> {
            if (needGetInfo(data) && rc instanceof BookInfoCrawler) {
                if (tvBookName.getTag() == null || !(Boolean) tvBookName.getTag()) {
                    tvBookName.setTag(true);
                } else {
                    initOtherInfo(data, rc);
                    return;
                }
                Log.i(book.getName(), "initOtherInfo");
                BookInfoCrawler bic = (BookInfoCrawler) rc;
                searchEngine.getBookInfo(book, bic, isSuccess -> {
                    if (isSuccess) {
                        List<Book> books = new ArrayList<>();
                        books.add(book);
                        books2SearchBookBean(data, books);
                        initOtherInfo(data, rc);
                    } else {
                        tvBookName.setTag(false);
                    }
                });
            }
        }, 1000);
    }

    private void initOtherInfo(SearchBookBean book, ReadCrawler rc) {
        //简介
        if (StringHelper.isEmpty(tvDesc.getText().toString())) {
            tvDesc.setText(String.format("简介:%s", book.getDesc()));
        }
        if (StringHelper.isEmpty(tvNewestChapter.getText().toString())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getLastChapter()));
        }
        if (StringHelper.isEmpty(tvAuthor.getText().toString())) {
            KeyWordUtils.setKeyWord(tvAuthor, book.getAuthor(), keyWord);
        }
        //图片
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(), book.getAuthor());
        }
    }

    private void initTagList(SearchBookBean data) {
        tagList.clear();
        String type = data.getType();
        if (!StringHelper.isEmpty(type))
            tagList.add("0:" + type);
        String wordCount = data.getWordCount();
        if (!StringHelper.isEmpty(wordCount))
            tagList.add("1:" + wordCount);
        String status = data.getStatus();
        if (!StringHelper.isEmpty(status))
            tagList.add("2:" + status);
        tflBookTag.setAdapter(new BookTagAdapter(activity, tagList, 11));
    }

    private void books2SearchBookBean(SearchBookBean bookBean, List<Book> books) {
        //为搜索页面的SearchBookBean填入信息
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getAuthor())) break;
            String author = book.getAuthor();
            if (!StringHelper.isEmpty(author)) {
                bookBean.setAuthor(author);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getType())) break;
            String type = book.getType();
            if (!StringHelper.isEmpty(type)) {
                bookBean.setType(type);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getDesc())) break;
            String desc = book.getDesc();
            if (!StringHelper.isEmpty(desc)) {
                bookBean.setDesc(desc);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getStatus())) break;
            String status = book.getStatus();
            if (!StringHelper.isEmpty(status)) {
                bookBean.setStatus(status);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getWordCount())) break;
            String wordCount = book.getWordCount();
            if (!StringHelper.isEmpty(wordCount)) {
                bookBean.setWordCount(wordCount);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getLastChapter())) break;
            String lastChapter = book.getNewestChapterTitle();
            if (!StringHelper.isEmpty(lastChapter)) {
                bookBean.setLastChapter(lastChapter);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getUpdateTime())) break;
            String updateTime = book.getUpdateDate();
            if (!StringHelper.isEmpty(updateTime)) {
                bookBean.setUpdateTime(updateTime);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getImgUrl())) break;
            String imgUrl = book.getImgUrl();
            if (!StringHelper.isEmpty(imgUrl)) {
                bookBean.setImgUrl(imgUrl);
                break;
            }
        }
    }

    private boolean needGetInfo(SearchBookBean bookBean) {
        if (StringHelper.isEmpty(bookBean.getAuthor())) return true;
        if (StringHelper.isEmpty(bookBean.getType())) return true;
        if (StringHelper.isEmpty(bookBean.getDesc())) return true;
        if (StringHelper.isEmpty(bookBean.getLastChapter())) return true;
        return StringHelper.isEmpty(bookBean.getImgUrl());
    }
}