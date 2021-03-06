package com.example.myreadproject8.ui.adapter.bookcase;

import android.app.Activity;
import android.text.TextUtils;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.base.BaseListAdapter;
import com.example.myreadproject8.ui.adapter.holder.IViewHolder;
import com.example.myreadproject8.ui.adapter.holder.search.SearchBookHolder;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.search.SearchEngine;
import com.example.myreadproject8.util.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * @author fengyue
 * @date 2020/10/2 10:08
 */
public class SearchBookAdapter extends BaseListAdapter<SearchBookBean> {
    private Activity activity;
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;
    private String keyWord;

    public SearchBookAdapter(Activity activity, ConcurrentMultiValueMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine, String keyWord) {
        this.activity = activity;
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
        this.keyWord = keyWord;
    }

    //创建一个搜索书目
    @Override
    protected IViewHolder<SearchBookBean> createViewHolder(int viewType) {
        return new SearchBookHolder(activity, mBooks, searchEngine, keyWord);
    }

    //将所有数目加入recycleView
    public void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(getItems());
        List<SearchBookBean> filterDataS = new ArrayList<>();

        //选择过滤器
        switch (SysManager.getSetting().getSearchFilter()) {
            case 0:
                filterDataS.addAll(newDataS);
                break;
            case 1:
            default://包含即可
                for (SearchBookBean ssb : newDataS) {
                    if (StringUtils.isContainEachOther(ssb.getName(), keyWord) ||
                            StringUtils.isContainEachOther(ssb.getAuthor(), keyWord)) {
                        filterDataS.add(ssb);
                    }
                }
                break;
            case 2://完全相等
                for (SearchBookBean ssb : newDataS) {
                    if (StringUtils.isEqual(ssb.getName(), keyWord) ||
                            StringUtils.isEqual(ssb.getAuthor(), keyWord)) {
                        filterDataS.add(ssb);
                    }
                }
                break;
        }

        if (filterDataS.size() > 0) {
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(filterDataS);
            } else {
                //存在
                for (SearchBookBean temp : filterDataS) {
                    boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            break;
                        }
                    }
                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.getName())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }

            synchronized (this) {
                App.runOnUiThread(() -> {
                    mList = copyDataS;
                    notifyDataSetChanged();
                });
            }
        }
    }

    private void sort(List<SearchBookBean> bookBeans) {
        //排序，基于最符合关键字的搜书结果应该是最短的
        //TODO ;这里只做了简单的比较排序，还需要继续完善
        Collections.sort(bookBeans, (o1, o2) -> {
            if (o1.getName().equals(keyWord))
                return -1;
            if (o2.getName().equals(keyWord))
                return 1;
            if (o1.getAuthor() != null && o1.getAuthor().equals(keyWord))
                return -1;
            if (o2.getAuthor() != null && o2.getAuthor().equals(keyWord))
                return 1;
            return Integer.compare(o1.getName().length(), o2.getName().length());
        });
    }

    private int getAddIndex(List<SearchBookBean> beans, SearchBookBean bean) {
        int maxWeight = 0;
        int index = -1;
        if (TextUtils.equals(keyWord, bean.getName())) {
            maxWeight = 5;
        }else if (TextUtils.equals(keyWord, bean.getAuthor())) {
            maxWeight = 3;
        }
        for (int i = 0; i < beans.size(); i++) {
            SearchBookBean searchBook = beans.get(i);
            int weight = 0;
            if (TextUtils.equals(bean.getName(), searchBook.getName())) {
                weight = 4;
            } else if (TextUtils.equals(bean.getAuthor(), searchBook.getAuthor())) {
                weight = 2;
            } else if (bean.getName().length() <= searchBook.getName().length()) {
                weight = 1;
            }
            if (weight > maxWeight) {
                index = i;
                maxWeight = weight;
            }
        }
        if (maxWeight == 5 || maxWeight == 3) index = 0;
        return index;
    }
}
