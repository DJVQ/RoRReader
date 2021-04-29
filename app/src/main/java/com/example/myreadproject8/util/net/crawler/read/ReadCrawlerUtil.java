package com.example.myreadproject8.util.net.crawler.read;


import android.text.TextUtils;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.enums.LocalBookSource;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.util.net.crawler.base.BaseSourceCrawler;
import com.example.myreadproject8.util.net.crawler.base.BaseSourceCrawlerNoInfo;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.source.JsonPathCrawler;
import com.example.myreadproject8.util.net.crawler.source.MatcherCrawler;
import com.example.myreadproject8.util.net.crawler.source.XpathCrawler;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.source.BookSourceManager;
import com.example.myreadproject8.util.string.StringHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import static com.example.myreadproject8.common.APPCONST.JSON_PATH;
import static com.example.myreadproject8.common.APPCONST.MATCHER;
import static com.example.myreadproject8.common.APPCONST.XPATH;


/**
 * @author fengyue
 * @date 2020/5/17 11:45
 */
public class ReadCrawlerUtil {
    private ReadCrawlerUtil() {
    }

    public static ArrayList<ReadCrawler> getReadCrawlers() {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString(App.getMContext().getString(R.string.searchSource), null);
        ArrayList<ReadCrawler> readCrawlers = new ArrayList<>();
        if (searchSource == null) {
            StringBuilder sb = new StringBuilder();
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
                sb.append(bookSource.toString());
                sb.append(",");
                readCrawlers.add(getReadCrawler(bookSource.toString()));
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            searchSource = sb.toString();
            spu.putString(App.getMContext().getString(R.string.searchSource), searchSource);
        } else if (!"".equals(searchSource)) {
            String[] sources = searchSource.split(",");
            for (String source : sources) {
                readCrawlers.add(getReadCrawler(source));
            }
        }
        return readCrawlers;
    }

    public static List<String> getAllSources() {
        List<String> sources = new ArrayList<>();
        for (LocalBookSource bookSource : LocalBookSource.values()) {
            if (bookSource.equals(LocalBookSource.fynovel))
                continue;
            sources.add(bookSource.text);
        }
        return sources;
    }

    public static HashMap<CharSequence, Boolean> getDisableSources() {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString(App.getMContext().getString(R.string.searchSource), null);
        HashMap<CharSequence, Boolean> mSources = new LinkedHashMap<>();
        if (searchSource == null) {
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
                mSources.put(bookSource.text, false);
            }
        } else {
            String[] ableSources = searchSource.split(",");
            bookSourceFor:
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
                for (String ableSource : ableSources) {
                    if (ableSource.equals(bookSource.toString())) {
                        mSources.put(bookSource.text, false);
                        continue bookSourceFor;
                    }
                }
                mSources.put(bookSource.text, true);
            }
        }
        return mSources;
    }

    public static void resetReadCrawlers() {
        StringBuilder sb = new StringBuilder();
        for (LocalBookSource bookSource : LocalBookSource.values()) {
            if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                continue;
            sb.append(bookSource.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        SharedPreUtils.getInstance().putString(App.getMContext().getString(R.string.searchSource), sb.toString());
    }

    public synchronized static void addReadCrawler(LocalBookSource... bookSources) {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString(App.getMContext().getString(R.string.searchSource));
        if ("".equals(searchSource)) {
            resetReadCrawlers();
            return;
        }
        StringBuilder sb = new StringBuilder(searchSource);
        for (LocalBookSource bookSource : bookSources) {
            sb.append(",");
            sb.append(bookSource.toString());
        }
        SharedPreUtils.getInstance().putString(App.getMContext().getString(R.string.searchSource), sb.toString());
    }

    public synchronized static void removeReadCrawler(String... bookSources) {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString(App.getMContext().getString(R.string.searchSource), null);
        if (searchSource == null) {
            return;
        }
        String[] ableSources = searchSource.split(",");
        for (int i = 0; i < ableSources.length; i++) {
            String ableSource = ableSources[i];
            for (String bookSource : bookSources) {
                if (ableSource.equals(bookSource)) {
                    ableSources[i] = "";
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String ableSource : ableSources) {
            if ("".equals(ableSource)) continue;
            sb.append(ableSource);
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        spu.putString(App.getMContext().getString(R.string.searchSource), sb.toString());
    }

    public static ReadCrawler getReadCrawler(String bookSource) {
        /*try {
            if (NetworkUtils.isUrl(bookSource)){
                BookSource source = BookSourceManager.getBookSourceByUrl(bookSource);
                if (source.getSearchRule().isRelatedWithInfo()){
                    return new MatcherCrawler(source);
                }else {
                    return new MatcherCrawlerNoInfo(source);
                }
            }
            ResourceBundle rb = ResourceBundle.getBundle("crawler");
            String readCrawlerPath = rb.getString(bookSource);
            Class clz = Class.forName(readCrawlerPath);
            return (ReadCrawler) clz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new FYReadCrawler();
        }*/
        return getReadCrawler(BookSourceManager.getBookSourceByStr(bookSource));
    }

    public static ReadCrawler getReadCrawler(BookSource source) {
        return getReadCrawler(source, false);
    }
    public static ReadCrawler getReadCrawler(BookSource source, boolean isInfo) {
        try {
            if (StringHelper.isEmpty(source.getSourceEName())) {
                BaseSourceCrawler crawler;
                if (source.getSourceType() == null) source.setSourceType(MATCHER);
                switch (source.getSourceType()) {
                    case MATCHER:
                    default:
                        crawler = new MatcherCrawler(source);
                        break;
                    case XPATH:
                        crawler = new XpathCrawler(source);
                        break;
                    case JSON_PATH:
                        crawler = new JsonPathCrawler(source);
                        break;
                }
                if (source.getSearchRule().isRelatedWithInfo() || isInfo) {
                    return crawler;
                } else {
                    return new BaseSourceCrawlerNoInfo(crawler);
                }
            } else {

                Class clz = Class.forName(source.getSourceUrl());
                return (ReadCrawler) clz.newInstance();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return new FYReadCrawler();
        }
    }

    public static List<ReadCrawler> getEnableReadCrawlers() {
        return getEnableReadCrawlers("");
    }

    public static List<ReadCrawler> getEnableReadCrawlers(String group) {
        List<ReadCrawler> crawlers = new ArrayList<>();
        List<BookSource> sources = TextUtils.isEmpty(group) ?
                BookSourceManager.getEnabledBookSource() :
                BookSourceManager.getEnableSourceByGroup(group);
        for (BookSource source : sources) {
            crawlers.add(getReadCrawler(source));
        }
        return crawlers;
    }


    public static String getReadCrawlerClz(String bookSource) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("crawler");
            return rb.getString(bookSource);
        } catch (Exception e) {
            return "";
        }
    }
}
