package com.example.myreadproject8.util.source;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.myreadproject8.AAATest.observer.MyObserver;
import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.enums.LocalBookSource;
import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.gen.BookSourceDao;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.gson.GsonUtils;
import com.example.myreadproject8.util.messenge.RxUtils;
import com.example.myreadproject8.util.net.NetworkUtils;
import com.example.myreadproject8.util.net.OkHttpUtils;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;



public class BookSourceManager {

    public static List<BookSource> getEnabledBookSource() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.Enable.eq(true))
                .orderRaw(BookSourceDao.Properties.Weight.columnName + " DESC")
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    public static List<BookSource> getAllBookSource() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .orderRaw(getBookSourceSort())
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    public static List<BookSource> getEnabledBookSourceByOrderNum() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.Enable.eq(true))
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    public static List<BookSource> getAllBookSourceByOrderNum() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    public static List<BookSource> getEnableSourceByGroup(String group) {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.Enable.eq(true))
                .where(BookSourceDao.Properties.SourceGroup.like("%" + group + "%"))
                .orderRaw(BookSourceDao.Properties.Weight.columnName + " DESC")
                .list();
    }

    /**
     * ??????book.getSource()????????????
     *
     * @param str
     * @return
     */
    public static BookSource getBookSourceByStr(String str) {
        if (NetworkUtils.isUrl(str)) {
            return getBookSourceByUrl(str);
        } else {
            return getBookSourceByEName(str);
        }
    }

    /**
     * ??????url????????????
     *
     * @param url
     * @return
     */
    public static BookSource getBookSourceByUrl(String url) {
        if (url == null) return getDefaultSource();
        BookSource source = GreenDaoManager.getDaoSession().getBookSourceDao().load(url);
        if (source == null) return getDefaultSource();
        return source;
    }

    /**
     * ??????????????????
     *
     * @param ename
     * @return
     */
    @Nullable
    public static BookSource getBookSourceByEName(String ename) {
        if (ename == null) return getDefaultSource();
        if ("local".equals(ename)) return getLocalSource();
        BookSource source = GreenDaoManager.getDaoSession().getBookSourceDao().
                queryBuilder()
                .where(BookSourceDao.Properties.SourceEName.eq(ename))
                .unique();
        if (source == null) return getDefaultSource();
        return source;
    }

    /**
     * ??????book.getSource()???????????????
     *
     * @param str
     * @return
     */
    public static String getSourceNameByStr(String str) {
        return getBookSourceByStr(str).getSourceName();
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private static BookSource getDefaultSource() {
        BookSource bookSource = new BookSource();
        bookSource.setSourceUrl("com.example.myreadproject8.util.net.crawler.read.FYReadCrawler");
        bookSource.setSourceName("????????????");
        bookSource.setSourceEName("fynovel");
        bookSource.setSourceGroup("????????????");
        return bookSource;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public static BookSource getLocalSource() {
        BookSource bookSource = new BookSource();
        bookSource.setSourceEName("local");
        bookSource.setSourceName("????????????");
        return bookSource;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static List<BookSource> getAllLocalSource() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.SourceEName.isNotNull())
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static List<BookSource> getAllNoLocalSource() {
        return GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.SourceEName.isNull())
                .orderAsc(BookSourceDao.Properties.OrderNum)
                .list();
    }

    /**
     * ????????????
     *
     * @param source
     */
    public static void removeBookSource(BookSource source) {
        if (source == null) return;
        GreenDaoManager.getDaoSession().getBookSourceDao().delete(source);
    }

    public static void removeBookSources(List<BookSource> sources) {
        if (sources == null) return;
        GreenDaoManager.getDaoSession().getBookSourceDao().deleteInTx(sources);
    }


    public static String getBookSourceSort() {
        switch (SharedPreUtils.getInstance().getInt("SourceSort", 0)) {
            case 1:
                return BookSourceDao.Properties.Weight.columnName + " DESC";
            case 2:
                return BookSourceDao.Properties.SourceName.columnName + " COLLATE LOCALIZED ASC";
            default:
                return BookSourceDao.Properties.OrderNum.columnName + " ASC";
        }
    }

    public static void addBookSource(List<BookSource> bookSources) {
        for (BookSource bookSource : bookSources) {
            addBookSource(bookSource);
        }
    }

    public static boolean addBookSource(BookSource bookSource) {
        if (TextUtils.isEmpty(bookSource.getSourceName()) || TextUtils.isEmpty(bookSource.getSourceUrl()))
            return false;
        if (bookSource.getSourceUrl().endsWith("/")) {
            bookSource.setSourceUrl(bookSource.getSourceUrl().replaceAll("/+$", ""));
        }
        BookSource temp = GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                .where(BookSourceDao.Properties.SourceUrl.eq(bookSource.getSourceUrl())).unique();
        if (temp != null) {
            bookSource.setOrderNum(temp.getOrderNum());
        } else {
            bookSource.setOrderNum((int) (GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder().count() + 1));
        }
        GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(bookSource);
        return true;
    }

    public static Single<Boolean> saveData(BookSource bookSource) {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (bookSource.getOrderNum() == 0) {
                bookSource.setOrderNum((int) (GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder().count() + 1));
            }
            GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(bookSource);
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static Single<Boolean> saveDatas(List<BookSource> sources) {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            for (BookSource source : sources) {
                if (source.getOrderNum() == 0) {
                    source.setOrderNum((int) (GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder().count() + 1));
                }
            }
            GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplaceInTx(sources);
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static Single<Boolean> toTop(BookSource source) {
        return Single.create((SingleOnSubscribe<Boolean>) e -> {
            List<BookSource> List = getAllBookSourceByOrderNum();
            for (int i = 0; i < List.size(); i++) {
                List.get(i).setOrderNum(i + 1);
            }
            source.setOrderNum(0);
            GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplaceInTx(List);
            GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source);
            e.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static List<String> getEnableNoLocalGroupList() {
        List<String> groupList = new ArrayList<>();
        String sql = "SELECT DISTINCT "
                + BookSourceDao.Properties.SourceGroup.columnName
                + " FROM " + BookSourceDao.TABLENAME
                + " WHERE " + BookSourceDao.Properties.Enable.name + " = 1";
        Cursor cursor = GreenDaoManager.getDaoSession().getDatabase().rawQuery(sql, null);
        if (!cursor.moveToFirst()) return groupList;
        do {
            String group = cursor.getString(0);
            if (TextUtils.isEmpty(group) || TextUtils.isEmpty(group.trim())) continue;
            for (String item : group.split("\\s*[,;??????]\\s*")) {
                if (TextUtils.isEmpty(item) || groupList.contains(item) || item.equals("????????????"))
                    continue;
                groupList.add(item);
            }
        } while (cursor.moveToNext());
        Collections.sort(groupList);
        return groupList;
    }

    public static List<String> getNoLocalGroupList() {
        List<String> groupList = new ArrayList<>();
        String sql = "SELECT DISTINCT " + BookSourceDao.Properties.SourceGroup.columnName + " FROM " + BookSourceDao.TABLENAME;
        Cursor cursor = GreenDaoManager.getDaoSession().getDatabase().rawQuery(sql, null);
        if (!cursor.moveToFirst()) return groupList;
        do {
            String group = cursor.getString(0);
            if (TextUtils.isEmpty(group) || TextUtils.isEmpty(group.trim())) continue;
            for (String item : group.split("\\s*[,;??????]\\s*")) {
                if (TextUtils.isEmpty(item) || groupList.contains(item) || item.equals("????????????"))
                    continue;
                groupList.add(item);
            }
        } while (cursor.moveToNext());
        Collections.sort(groupList);
        return groupList;
    }

    public static Observable<List<BookSource>> importSource(String string) {
        if (StringHelper.isEmpty(string)) return null;
        string = string.trim();
        if (NetworkUtils.isIPv4Address(string)) {
            string = String.format("http://%s:65501", string);
        }
        if (StringUtils.isJsonType(string)) {
            return importBookSourceFromJson(string.trim())
                    .compose(RxUtils::toSimpleSingle);
        } else if (StringUtils.isCompressJsonType(string)) {
            return importBookSourceFromJson(StringUtils.unCompressJson(string))
                    .compose(RxUtils::toSimpleSingle);
        }
        if (NetworkUtils.isUrl(string)) {
            String finalString = string;
            return Observable.create((ObservableEmitter<String> e) -> e.onNext(OkHttpUtils.getHtml(finalString)))
                    .flatMap(BookSourceManager::importBookSourceFromJson)
                    .compose(RxUtils::toSimpleSingle);
        }
        return Observable.error(new Exception("??????Json???Url??????"));
    }

    private static Observable<List<BookSource>> importBookSourceFromJson(String json) {
        return Observable.create(e -> {
            List<BookSource> successImportSources = new ArrayList<>();
            if (StringUtils.isJsonArray(json)) {
                try {
                    List<BookSource> bookSources = GsonUtils.parseJArray(json, BookSource.class);
                    for (BookSource bookSource : bookSources) {
                        if (bookSource.containsGroup("??????")) {
                            GreenDaoManager.getDaoSession().getBookSourceDao().queryBuilder()
                                    .where(BookSourceDao.Properties.SourceUrl.eq(bookSource.getSourceUrl()))
                                    .buildDelete().executeDeleteWithoutDetachingEntities();
                        } else {
                            if (addBookSource(bookSource)) {
                                successImportSources.add(bookSource);
                            }
                        }
                    }
                    e.onNext(successImportSources);
                    e.onComplete();
                    return;
                } catch (Exception ignored) {
                }
            }
            if (StringUtils.isJsonObject(json)) {
                try {
                    BookSource bookSource = GsonUtils.parseJObject(json, BookSource.class);
                    if (addBookSource(bookSource))
                        successImportSources.add(bookSource);
                    e.onNext(successImportSources);
                    e.onComplete();
                    return;
                } catch (Exception ignored) {
                }
            }
            e.onError(new Throwable("????????????"));
        });
    }


    public static void initDefaultSources() {
        Log.d("initDefaultSources", "execute");
        GreenDaoManager.getDaoSession().getBookSourceDao().deleteAll();
        String searchSource = SharedPreUtils.getInstance().getString(App.getMContext().getString(R.string.searchSource));
        boolean isEmpty = StringHelper.isEmpty(searchSource);
        for (LocalBookSource source : LocalBookSource.values()) {
            if (source == LocalBookSource.local || source == LocalBookSource.fynovel) continue;
            BookSource source1 = new BookSource();
            source1.setSourceEName(source.toString());
            source1.setSourceName(source.text);
            source1.setSourceGroup("????????????");
            source1.setEnable(isEmpty || searchSource.contains(source.toString()));
            source1.setSourceUrl(ReadCrawlerUtil.getReadCrawlerClz(source.toString()));
            source1.setOrderNum(0);
            GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source1);
        }
        String referenceSources = FileUtils.readAssertFile(App.getMContext(),
                "ReferenceSources.json");
        Observable<List<BookSource>> observable = BookSourceManager.importBookSourceFromJson(referenceSources);
        observable.subscribe(new MyObserver<List<BookSource>>() {
            @Override
            public void onNext(@NonNull List<BookSource> sources) {

            }
        });
    }
}
