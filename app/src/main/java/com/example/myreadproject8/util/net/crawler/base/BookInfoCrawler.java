package com.example.myreadproject8.util.net.crawler.base;


import com.example.myreadproject8.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2020/5/19 19:50
 */
public interface BookInfoCrawler {
    String getNameSpace();
    String getCharset();
    Book getBookInfo(String html, Book book);
}
