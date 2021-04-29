package com.example.myreadproject8.util.net.crawler.base;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;

import java.util.ArrayList;



/**
 * @author fengyue
 * @date 2020/4/28 16:18
 */

public interface ReadCrawler {
    String getSearchLink();  // 书源的搜索url
    String getCharset(); // 书源的字符编码
    String getSearchCharset(); // 书源搜索关键字的字符编码，和书源的字符编码就行
    String getNameSpace(); // 书源主页地址
    Boolean isPost(); // 是否以post请求搜索
    ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html); // 搜索书籍规则
    ArrayList<Chapter> getChaptersFromHtml(String html); // 获取书籍章节列表规则
    String getContentFormHtml(String html); // 获取书籍内容规则
}
