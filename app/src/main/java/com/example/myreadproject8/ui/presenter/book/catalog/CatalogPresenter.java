package com.example.myreadproject8.ui.presenter.book.catalog;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.service.ChapterService;
import com.example.myreadproject8.ui.activity.CatalogActivity;
import com.example.myreadproject8.ui.adapter.catalog.ChapterTitleAdapter;
import com.example.myreadproject8.ui.fragment.catalog.CatalogFragment;
import com.example.myreadproject8.ui.presenter.base.BasePresenter;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.net.webapi.api.CommonApi;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * created by ycq on 2021/4/18 0018
 * describe：
 */
public class CatalogPresenter implements BasePresenter {
    private static final String TAG = CatalogPresenter.class.getSimpleName();
    private CatalogFragment mCatalogFragment;
    private ChapterService mChapterService;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ArrayList<Chapter> mConvertChapters = new ArrayList<>();
    private int curSortflag = 0; //0正序  1倒序
    private ChapterTitleAdapter mChapterTitleAdapter;
    private Book mBook;

    public CatalogPresenter(CatalogFragment mCatalogFragment) {
        this.mCatalogFragment = mCatalogFragment;
        mChapterService = ChapterService.getInstance();
    }
    @Override
    public void start() {
        mBook = ((CatalogActivity) mCatalogFragment.getActivity()).getmBook();
        mCatalogFragment.getFcChangeSort().setOnClickListener(view -> {
            if (curSortflag == 0) {//当前正序
                curSortflag = 1;
            } else {//当前倒序
                curSortflag = 0;
            }
            if (mChapterTitleAdapter != null) {
                changeChapterSort();
            }
        });
        mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
        if (mChapters.size() != 0) {
            initChapterTitleList();
        }else {
            if ("本地书籍".equals(mBook.getType())){
                ToastUtils.showWarring("本地书籍请先拆分章节！");
                return;
            }
            mCatalogFragment.getPbLoading().setVisibility(View.VISIBLE);
            CommonApi.getBookChapters(mBook.getChapterUrl(), ReadCrawlerUtil.getReadCrawler(mBook.getSource()),false,
                    new ResultCallback() {
                        @Override
                        public void onFinish(Object o, int code) {
                            mChapters = (ArrayList<Chapter>) o;

                            App.runOnUiThread(() -> {
                                mCatalogFragment.getPbLoading().setVisibility(View.GONE);
                                initChapterTitleList();
                            });

                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            ToastUtils.showError("章节目录加载失败！\n" + e.getLocalizedMessage());
                            App.runOnUiThread(() -> mCatalogFragment.getPbLoading().setVisibility(View.GONE));
                        }
                    });
        }
        mCatalogFragment.getLvChapterList().setOnItemClickListener((adapterView, view, i, l) -> {
            Chapter chapter = mChapterTitleAdapter.getItem(i);
            final int position;
            assert chapter != null;
            if (chapter.getNumber() == 0) {
                if (curSortflag == 0) {
                    position = i;
                } else {
                    position = mChapters.size() - 1 - i;
                }
            } else {
                position = chapter.getNumber();
            }
            /*LLog.i(TAG, "position = " + position);
            LLog.i(TAG, "mChapters.size() = " + mChapters.size());*/
            Intent intent = new Intent();
            intent.putExtra(APPCONST.CHAPTER_PAGE, new int[]{position, 0});
            mCatalogFragment.getActivity().setResult(Activity.RESULT_OK, intent);
            mCatalogFragment.getActivity().finish();
        });
    }

    /**
     * 初始化章节目录
     */
    private void initChapterTitleList() {
        //初始化倒序章节
        mConvertChapters.addAll(mChapters);
        Collections.reverse(mConvertChapters);
        //设置布局管理器
        int curChapterPosition;
        curChapterPosition = mBook.getHistoryChapterNum();
        mChapterTitleAdapter = new ChapterTitleAdapter(mCatalogFragment.getContext(), R.layout.listview_chapter_title_item, mChapters, mBook);
        mCatalogFragment.getLvChapterList().setAdapter(mChapterTitleAdapter);
        mCatalogFragment.getLvChapterList().setSelection(curChapterPosition);
    }

    /**
     * 改变章节列表排序（正倒序）
     */
    private void changeChapterSort() {
        if (curSortflag == 0) {
            mChapterTitleAdapter.clear();
            mChapterTitleAdapter.addAll(mChapterTitleAdapter.getmList());
        } else {
            mChapterTitleAdapter.clear();
            mConvertChapters.clear();
            mConvertChapters.addAll(mChapterTitleAdapter.getmList());
            Collections.reverse(mConvertChapters);
            mChapterTitleAdapter.addAll(mConvertChapters);
        }
        mChapterTitleAdapter.notifyDataSetChanged();
        mCatalogFragment.getLvChapterList().setAdapter(mChapterTitleAdapter);
    }


    /**
     * 搜索过滤
     *
     * @param query
     */
    public void startSearch(String query) {
        if (mChapters.size() == 0)  return;
        mChapterTitleAdapter.getFilter().filter(query);
        mCatalogFragment.getLvChapterList().setSelection(0);
    }
}
