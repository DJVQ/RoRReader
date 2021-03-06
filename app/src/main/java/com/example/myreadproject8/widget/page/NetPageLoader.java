package com.example.myreadproject8.widget.page;


import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.string.StringHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



public class NetPageLoader extends PageLoader {
    private static final String TAG = "PageFactory";
    private ChapterService mChapterService;
    private ReadCrawler mReadCrawler;
    private List<Chapter> loadingChapters = new CopyOnWriteArrayList<>();

    public NetPageLoader(PageView pageView, Book collBook, ChapterService mChapterService,
                         ReadCrawler mReadCrawler, Setting setting) {
        super(pageView, collBook, setting);
        this.mChapterService = mChapterService;
        this.mReadCrawler = mReadCrawler;
    }

    /*private List<BookChapterBean> convertTxtChapter(List<Chapter> bookChapters) {
        List<BookChapterBean> txtChapters = new ArrayList<>(bookChapters.size());
        for (Chapter bean : bookChapters) {
            BookChapterBean chapter = new BookChapterBean();
            chapter.setBookId(bean.getBookId());
            chapter.setTitle(bean.getTitle());
            chapter.setLink(bean.getUrl());
            txtChapters.add(chapter);
        }
        return txtChapters;
    }*/

    @Override
    public void refreshChapterList() {
        List<Chapter> chapters = mChapterService.findBookAllChapterByBookId(mCollBook.getId());
        if (chapters == null) return;

        // ??? BookChapter ???????????????????????? Chapter
//        mChapterList = convertTxtChapter(chapters);
        mChapterList = chapters;
        isChapterListPrepare = true;

        // ??????????????????????????????????????????
        if (mPageChangeListener != null) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }

        // ?????????????????????
        if (!isChapterOpen()) {
            // ????????????
            openChapter();
        }
    }

    @Override
    protected BufferedReader getChapterReader(Chapter chapter) throws FileNotFoundException {
        File file = new File(APPCONST.BOOK_CACHE_PATH + mCollBook.getId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!file.exists()) return null;
        BufferedReader br = new BufferedReader(new FileReader(file));
        return br;
    }

    @Override
    public boolean hasChapterData(Chapter chapter) {
        return ChapterService.isChapterCached(mCollBook.getId(), chapter.getTitle());
    }

    // ???????????????????????????
    @Override
    boolean parsePrevChapter() {
        boolean isRight = super.parsePrevChapter();

        if (mStatus == STATUS_FINISH) {
            loadPrevChapter();
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }
        return isRight;
    }

    // ????????????????????????
    @Override
    boolean parseCurChapter() {
        boolean isRight = super.parseCurChapter();

        if (mStatus == STATUS_FINISH) {
            loadPrevChapter();
            loadNextChapter();
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }
        return isRight;
    }

    // ???????????????????????????
    @Override
    boolean parseNextChapter() {
        boolean isRight = super.parseNextChapter();

        if (mStatus == STATUS_FINISH) {
            loadNextChapter();
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }

        return isRight;
    }

    /**
     * ????????????????????????????????????
     */
    private void loadPrevChapter() {
        if (mPageChangeListener != null) {
            int end = mCurChapterPos;
            int begin = end - 1;
            if (begin < 0) {
                begin = 0;
            }
            requestChapters(begin, end);
        }
    }

    /**
     * ??????????????????????????????????????????
     */
    private void loadCurrentChapter() {
        if (mPageChangeListener != null) {
            int begin = mCurChapterPos;
            int end = mCurChapterPos;

            // ??????????????????????????????
            if (end < mChapterList.size()) {
                end = end + 1;
                if (end >= mChapterList.size()) {
                    end = mChapterList.size() - 1;
                }
            }

            // ???????????????????????????
            if (begin != 0) {
                begin = begin - 1;
                if (begin < 0) {
                    begin = 0;
                }
            }
            requestChapters(begin, end);
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void loadNextChapter() {
        if (mPageChangeListener != null) {

            // ?????????????????????
            int begin = mCurChapterPos + 1;
            int end = begin + 3;

            // ??????????????????????????????
            if (begin >= mChapterList.size()) {
                // ?????????????????????????????????????????????????????????
                return;
            }

            if (end > mChapterList.size()) {
                end = mChapterList.size() - 1;
            }
            requestChapters(begin, end);
        }
    }

    private void requestChapters(int start, int end) {
        // ???????????????
        if (start < 0) {
            start = 0;
        }

        if (end >= mChapterList.size()) {
            end = mChapterList.size() - 1;
        }


        List<Chapter> chapters = new ArrayList<>();

        // ????????????????????????????????????/????????????
        for (int i = start; i <= end; ++i) {
            Chapter txtChapter = mChapterList.get(i);
            if (!hasChapterData(txtChapter) && !loadingChapters.contains(txtChapter)) {
                chapters.add(txtChapter);
            }
        }

        if (!chapters.isEmpty()) {
            loadingChapters.addAll(chapters);
            for (Chapter chapter : chapters) {
                getChapterContent(chapter);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param chapter
     */
    public void getChapterContent(Chapter chapter) {
        CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, new ResultCallback() {
            @Override
            public void onFinish(final Object o, int code) {
                loadingChapters.remove(chapter);
                String content = (String) o;
                if (StringHelper.isEmpty(content)) content = "??????????????????";
                mChapterService.saveOrUpdateChapter(chapter, content);
                if (isClose()) return;
                if (getPageStatus() == PageLoader.STATUS_LOADING && mCurChapterPos == chapter.getNumber()) {
                    App.runOnUiThread(() -> {
                        if (isPrev) {
                            openChapterInLastPage();
                        } else {
                            openChapter();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                loadingChapters.remove(chapter);
                if (isClose()) return;
                if (mCurChapterPos == chapter.getNumber())
                    App.runOnUiThread(() -> chapterError("??????????????????????????????\n" + e.getLocalizedMessage()));
                e.printStackTrace();
            }
        });
    }

}

