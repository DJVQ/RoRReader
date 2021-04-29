package com.example.myreadproject8.ui.activity.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.example.myreadproject8.AAATest.observer.MySingleObserver;
import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;

import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.SearchHistory;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.service.BookSourceService;
import com.example.myreadproject8.greendao.service.SearchHistoryService;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.activity.book.BookDetailedActivity;
import com.example.myreadproject8.ui.activity.book.BookSourceActivity;
import com.example.myreadproject8.ui.adapter.bookcase.SearchBookAdapter;
import com.example.myreadproject8.ui.adapter.search.SearchHistoryAdapter;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MultiChoiceDialog;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.crawler.base.ReadCrawler;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.search.SearchEngine;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.source.BookSourceManager;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchBookActivity extends BaseActivity {

    private com.example.myreadproject8.databinding.ActivitySearchBinding binding;

    //adapter
    private SearchBookAdapter mSearchBookAdapter;
    private String searchKey;//搜索关键字
    //搜索类
    private ArrayList<SearchBookBean> mBooksBean = new ArrayList<>();
    //一个搜索类对应多本书(可能多个搜索源都搜到了这本书)
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks = new ConcurrentMultiValueMap<>();
    //搜索历史
    private ArrayList<SearchHistory> mSearchHistories = new ArrayList<>();
    //热门
    private ArrayList<String> mSuggestions = new ArrayList<>();
    //搜索历史服务类
    private SearchHistoryService mSearchHistoryService;
    //搜索历史adapter
    private SearchHistoryAdapter mSearchHistoryAdapter;
    //所有搜索源
    private int allThreadCount;
    //搜索引擎
    private SearchEngine searchEngine;
    //设置
    private Setting mSetting;
    //页面菜单
    private Menu menu;
    //对话框
    private AlertDialog mDisableSourceDia;

    private BookSourceService mBookSourceService = new BookSourceService();
    //默认热搜推荐
    private static String[] suggestion = {"鲁迅", "乡村", "毛泽东", "学习", "马云", "英语", "霍金"};
    private static String[] suggestion2 = {"网络", "电子商务", "计算机", "科学", "算法", "安卓", "阅读", "智能"};

    //msgHander
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    search();
                    break;
                case 2:
                    binding.srlSearchBookList.finishRefresh();
                    break;
                case 3:
                    binding.fabSearchStop.setVisibility(View.GONE);
                    break;
            }
        }
    };


    //绑定页面
    @Override
    protected void bindView() {
        binding = com.example.myreadproject8.databinding.ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    //设置toolbar
    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("搜索");
    }

    //初始化数据
    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //获取设置
        mSetting = SysManager.getSetting();
        //初始化历史记录服务对象
        mSearchHistoryService = SearchHistoryService.getInstance();


        //初始化搜索引擎
        searchEngine = new SearchEngine();
        //设置setOnSearchListener
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            //完成加载时
            @Override
            public void loadMoreFinish(Boolean isAll) {
                binding.rpb.setIsAutoLoading(false);
                binding.fabSearchStop.setVisibility(View.GONE);
            }

            //完成搜索时
            @Override
            public void loadMoreSearchBook(ConcurrentMultiValueMap<SearchBookBean, Book> items) {
                mBooks.addAll(items);
                mSearchBookAdapter.addAll(new ArrayList<>(items.keySet()), searchKey);
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }


            @Override
            public void loadMoreSearchBook(List<Book> items) {

            }

            //发生错误时
            @Override
            public void searchBookError(Throwable throwable) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });

        mBookSourceService.initLocalBookSource();

    }


    //初始化组件
    @Override
    protected void initWidget() {
        super.initWidget();
        //初始化推荐
        initSuggestionList();
        binding.etSearchKey.requestFocus();//get the focus
        //enter事件，监听键盘enter
        binding.etSearchKey.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //此时搜索
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
            return false;
        });

        //设置过滤器
        switch (mSetting.getSearchFilter()) {
            case 0:
                binding.rbAllSearch.setChecked(true);
                break;
            case 1:
            default:
                binding.rbFuzzySearch.setChecked(true);
                break;
            case 2:
                binding.rbPreciseSearch.setChecked(true);
                break;
        }

        //根据选择改变过滤器
        binding.rgSearchFilter.setOnCheckedChangeListener((group, checkedId) -> {
            int searchFilter;
            switch (checkedId) {
                case R.id.rb_all_search:
                default:
                    searchFilter = 0;
                    break;
                case R.id.rb_fuzzy_search:
                    searchFilter = 1;
                    break;
                case R.id.rb_precise_search:
                    searchFilter = 2;
                    break;
            }
            mSetting.setSearchFilter(searchFilter);
            SysManager.saveSetting(mSetting);
        });

        //搜索框改变事件
        binding.etSearchKey.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                searchKey = editable.toString();

                //判断旧是否为空文本
                if (StringHelper.isEmpty(searchKey)) {
                    //搜索
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }

            }

        });


        //设置布局管理器
        binding.rvSearchBooksList.setLayoutManager(new LinearLayoutManager(this));

        //下拉刷新
        binding.srlSearchBookList.setOnRefreshListener(refreshLayout -> {
            stopSearch();
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //初始化历史记录列表
        initHistoryList();
    }

    //初始化点击事件
    @Override
    protected void initClick() {
        super.initClick();

        //换一批点击事件
        binding.llRefreshSuggestBooks.setOnClickListener(new RenewSuggestionBook());

        //搜索按钮点击事件
        binding.tvSearchConform.setOnClickListener(view -> mHandler.sendMessage(mHandler.obtainMessage(1)));

        //suggestion搜索事件
        binding.tgSuggestBook.setOnTagClickListener(tag -> {
            binding.etSearchKey.setText(tag);
            binding.etSearchKey.setSelection(tag.length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //历史记录搜索事件
        binding.lvHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            binding.etSearchKey.setText(mSearchHistories.get(position).getContent());
            binding.etSearchKey.setSelection(mSearchHistories.get(position).getContent().length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //清空历史记录
        binding.llClearHistory.setOnClickListener(v -> {
            mSearchHistoryService.clearHistory();
            initHistoryList();
        });
        //长按清除单个历史记录
        binding.lvHistoryList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (mSearchHistories.get(position) != null) {
                mSearchHistoryService.deleteHistory(mSearchHistories.get(position));
                initHistoryList();
            }
            return true;
        });

        //停止搜索选项
        binding.fabSearchStop.setOnClickListener(v -> {
            stopSearch();
        });
    }


    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        this.menu = menu;
        initSourceGroupMenu();
        return true;
    }


    //创建菜单前初始化
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.action_disable_source).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    //菜单项选择事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_disable_source) {
            showDisableSourceDia();
        }else if (item.getItemId() == R.id.action_source_man) {
            startActivityForResult(new Intent(this, BookSourceActivity.class),
                    APPCONST.REQUEST_BOOK_SOURCE);
        } else {
            if (item.getGroupId() == R.id.source_group) {
                item.setChecked(true);
                SharedPreUtils sp = SharedPreUtils.getInstance();
                if (getString(R.string.all_source).equals(item.getTitle().toString())) {
                    sp.putString("searchGroup", "");
                } else {
                    sp.putString("searchGroup", item.getTitle().toString());
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_BOOK_SOURCE) {
                initSourceGroupMenu();
            }
        }
    }

    /**
     * 初始化书源分组菜单
     */
    public void initSourceGroupMenu() {
        if (menu == null) return;
        String searchGroup = SharedPreUtils.getInstance().getString("searchGroup");
        menu.removeGroup(R.id.source_group);
        MenuItem item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.all_source);
        MenuItem localItem = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.local_source);
        if ("".equals(searchGroup)) {
            item.setChecked(true);
        } else if (getString(R.string.local_source).equals(searchGroup)) {
            localItem.setChecked(true);
        }
        List<String> groupList = BookSourceManager.getEnableNoLocalGroupList();
        for (String groupName : groupList) {
            item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
            if (groupName.equals(searchGroup)) item.setChecked(true);
        }
        menu.setGroupCheckable(R.id.source_group, true, true);
    }

    private void showDisableSourceDia() {
        if (mDisableSourceDia != null) {
            mDisableSourceDia.show();
            return;
        }
        List<BookSource> sources = BookSourceManager.getAllBookSourceByOrderNum();
        CharSequence[] mSourcesName = new CharSequence[sources.size()];
        boolean[] isDisables = new boolean[sources.size()];
        int dSourceCount = 0;
        int i = 0;
        for (BookSource source : sources) {
            mSourcesName[i] = source.getSourceName();
            boolean isDisable = !source.getEnable();
            if (isDisable) dSourceCount++;
            isDisables[i++] = isDisable;
        }

        mDisableSourceDia = new MultiChoiceDialog().create(this, "选择禁用的书源",
                mSourcesName, isDisables, dSourceCount, (dialog, which) -> {
                    BookSourceManager.saveDatas(sources)
                            .subscribe(new MySingleObserver<Boolean>() {
                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {
                                    if (aBoolean){
                                        ToastUtils.showSuccess("保存成功");
                                    }
                                }
                            });
                }, null, new DialogCreator.OnMultiDialogListener() {
                    @Override
                    public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                        sources.get(which).setEnable(!isChecked);
                    }

                    @Override
                    public void onSelectAll(boolean isSelectAll) {
                        for (BookSource source : sources) {
                            source.setEnable(!isSelectAll);
                        }
                    }
                });
    }

    /**
     * 初始化建议书目
     */
    private void initSuggestionList() {

        binding.tgSuggestBook.setTags(suggestion);

    }

    private void parseSuggestionList(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray names = json.getJSONObject("data").getJSONArray("popWords");
            for (int i = 0; i < names.length(); i++) {
                mSuggestions.add(names.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class RenewSuggestionBook implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            String[] s = binding.tgSuggestBook.getTags();
            if (Arrays.equals(s, suggestion)) {
                binding.tgSuggestBook.setTags(suggestion2);
            } else {
                binding.tgSuggestBook.setTags(suggestion);
            }
        }
    }

    /**
     * 初始化历史列表
     */
    private void initHistoryList() {
        mSearchHistories = mSearchHistoryService.findAllSearchHistory();
        if (mSearchHistories == null || mSearchHistories.size() == 0) {
            binding.llHistoryView.setVisibility(View.GONE);
        } else {
            mSearchHistoryAdapter = new SearchHistoryAdapter(this, R.layout.listview_search_history_item, mSearchHistories);
            binding.lvHistoryList.setAdapter(mSearchHistoryAdapter);
            binding.llHistoryView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化搜索列表
     */
    private void initSearchList() {
        //initmBooksBean();
        binding.rvSearchBooksList.setVisibility(View.VISIBLE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
    }


    /**
     * 获取搜索数据
     */
    private void getData() {
        initSearchList();
        mBooksBean.clear();
        mBooks.clear();
        List<ReadCrawler> readCrawlers = ReadCrawlerUtil
                .getEnableReadCrawlers(SharedPreUtils.getInstance().getString("searchGroup"));
        allThreadCount = readCrawlers.size();
        if (allThreadCount == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            binding.rpb.setIsAutoLoading(false);
            return;
        }
        /*for (ReadCrawler readCrawler : readCrawlers) {
            searchBookByCrawler(readCrawler, readCrawler.getSearchCharset());
        }*/
        //为搜索引擎设置搜索源
        searchEngine.initSearchEngine(readCrawlers);
        //搜索
        searchEngine.search(searchKey);
    }

    /**
     * 搜索
     */
    private void search() {
        binding.rpb.setIsAutoLoading(true);

        //若搜索值为空
        if (StringHelper.isEmpty(searchKey)) {
            stopSearch();
            binding.rpb.setIsAutoLoading(false);
            binding.rvSearchBooksList.setVisibility(View.GONE);
            binding.llSuggestBooksView.setVisibility(View.VISIBLE);
            binding.fabSearchStop.setVisibility(View.GONE);
            initHistoryList();
            binding.rvSearchBooksList.setAdapter(null);
            binding.srlSearchBookList.setEnableRefresh(false);
        } else {
            //书籍展示
            mSearchBookAdapter = new SearchBookAdapter(this, mBooks, searchEngine, searchKey);
            binding.rvSearchBooksList.setAdapter(mSearchBookAdapter);
            //进入书籍详情页
            mSearchBookAdapter.setOnItemClickListener((view, pos) -> {
                SearchBookBean data = mSearchBookAdapter.getItem(pos);
                ArrayList<Book> books = (ArrayList<Book>) mBooks.getValues(data);
                searchBookBean2Book(data, books.get(0));
                Intent intent = new Intent(this, BookDetailedActivity.class);
                intent.putExtra(APPCONST.SEARCH_BOOK_BEAN, books);
                startActivity(intent);
            });
            binding.srlSearchBookList.setEnableRefresh(true);
            binding.rvSearchBooksList.setVisibility(View.VISIBLE);
            binding.llSuggestBooksView.setVisibility(View.GONE);
            binding.llHistoryView.setVisibility(View.GONE);
            binding.fabSearchStop.setVisibility(View.VISIBLE);
            getData();
            mSearchHistoryService.addOrUpadteHistory(searchKey);
            //收起软键盘
            InputMethodManager imm = (InputMethodManager) App.getMContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(binding.etSearchKey.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void stopSearch() {
        searchEngine.stopSearch();
        mHandler.sendEmptyMessage(3);
    }

    @Override
    public void onBackPressed() {
        if (StringHelper.isEmpty(searchKey)) {
            super.onBackPressed();
        } else {
            binding.etSearchKey.setText("");//清空搜索框
        }
    }

    @Override
    protected void onDestroy() {
        stopSearch();
        for (int i = 0; i < 9; i++) {
            mHandler.removeMessages(i + 1);
        }
        super.onDestroy();
    }

    //设置书籍信息
    private void searchBookBean2Book(SearchBookBean bean, Book book) {
        if (StringHelper.isEmpty(book.getType()) && !StringHelper.isEmpty(bean.getType()))
            book.setType(bean.getType());
        if (StringHelper.isEmpty(book.getType()) && !StringHelper.isEmpty(bean.getType()))
            book.setType(bean.getType());
        if (StringHelper.isEmpty(book.getDesc()) && !StringHelper.isEmpty(bean.getDesc()))
            book.setDesc(bean.getDesc());
        if (StringHelper.isEmpty(book.getStatus()) && !StringHelper.isEmpty(bean.getStatus()))
            book.setStatus(bean.getStatus());
        if (StringHelper.isEmpty(book.getWordCount()) && !StringHelper.isEmpty(bean.getWordCount()))
            book.setWordCount(bean.getWordCount());
        if (StringHelper.isEmpty(book.getNewestChapterTitle()) && !StringHelper.isEmpty(bean.getLastChapter()))
            book.setNewestChapterTitle(bean.getLastChapter());
        if (StringHelper.isEmpty(book.getUpdateDate()) && !StringHelper.isEmpty(bean.getUpdateTime()))
            book.setUpdateDate(bean.getUpdateTime());
        if (StringHelper.isEmpty(book.getImgUrl()) && !StringHelper.isEmpty(bean.getImgUrl()))
            book.setImgUrl(bean.getImgUrl());
    }
}
