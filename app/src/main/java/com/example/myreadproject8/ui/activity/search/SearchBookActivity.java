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
    private String searchKey;//???????????????
    //?????????
    private ArrayList<SearchBookBean> mBooksBean = new ArrayList<>();
    //??????????????????????????????(??????????????????????????????????????????)
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks = new ConcurrentMultiValueMap<>();
    //????????????
    private ArrayList<SearchHistory> mSearchHistories = new ArrayList<>();
    //??????
    private ArrayList<String> mSuggestions = new ArrayList<>();
    //?????????????????????
    private SearchHistoryService mSearchHistoryService;
    //????????????adapter
    private SearchHistoryAdapter mSearchHistoryAdapter;
    //???????????????
    private int allThreadCount;
    //????????????
    private SearchEngine searchEngine;
    //??????
    private Setting mSetting;
    //????????????
    private Menu menu;
    //?????????
    private AlertDialog mDisableSourceDia;

    private BookSourceService mBookSourceService = new BookSourceService();
    //??????????????????
    private static String[] suggestion = {"??????", "??????", "?????????", "??????", "??????", "??????", "??????"};
    private static String[] suggestion2 = {"??????", "????????????", "?????????", "??????", "??????", "??????", "??????", "??????"};

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


    //????????????
    @Override
    protected void bindView() {
        binding = com.example.myreadproject8.databinding.ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    //??????toolbar
    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("??????");
    }

    //???????????????
    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //????????????
        mSetting = SysManager.getSetting();
        //?????????????????????????????????
        mSearchHistoryService = SearchHistoryService.getInstance();


        //?????????????????????
        searchEngine = new SearchEngine();
        //??????setOnSearchListener
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            //???????????????
            @Override
            public void loadMoreFinish(Boolean isAll) {
                binding.rpb.setIsAutoLoading(false);
                binding.fabSearchStop.setVisibility(View.GONE);
            }

            //???????????????
            @Override
            public void loadMoreSearchBook(ConcurrentMultiValueMap<SearchBookBean, Book> items) {
                mBooks.addAll(items);
                mSearchBookAdapter.addAll(new ArrayList<>(items.keySet()), searchKey);
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }


            @Override
            public void loadMoreSearchBook(List<Book> items) {

            }

            //???????????????
            @Override
            public void searchBookError(Throwable throwable) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });

        mBookSourceService.initLocalBookSource();

    }


    //???????????????
    @Override
    protected void initWidget() {
        super.initWidget();
        //???????????????
        initSuggestionList();
        binding.etSearchKey.requestFocus();//get the focus
        //enter?????????????????????enter
        binding.etSearchKey.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //????????????
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
            return false;
        });

        //???????????????
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

        //???????????????????????????
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

        //?????????????????????
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

                //???????????????????????????
                if (StringHelper.isEmpty(searchKey)) {
                    //??????
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }

            }

        });


        //?????????????????????
        binding.rvSearchBooksList.setLayoutManager(new LinearLayoutManager(this));

        //????????????
        binding.srlSearchBookList.setOnRefreshListener(refreshLayout -> {
            stopSearch();
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //???????????????????????????
        initHistoryList();
    }

    //?????????????????????
    @Override
    protected void initClick() {
        super.initClick();

        //?????????????????????
        binding.llRefreshSuggestBooks.setOnClickListener(new RenewSuggestionBook());

        //????????????????????????
        binding.tvSearchConform.setOnClickListener(view -> mHandler.sendMessage(mHandler.obtainMessage(1)));

        //suggestion????????????
        binding.tgSuggestBook.setOnTagClickListener(tag -> {
            binding.etSearchKey.setText(tag);
            binding.etSearchKey.setSelection(tag.length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //????????????????????????
        binding.lvHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            binding.etSearchKey.setText(mSearchHistories.get(position).getContent());
            binding.etSearchKey.setSelection(mSearchHistories.get(position).getContent().length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //??????????????????
        binding.llClearHistory.setOnClickListener(v -> {
            mSearchHistoryService.clearHistory();
            initHistoryList();
        });
        //??????????????????????????????
        binding.lvHistoryList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (mSearchHistories.get(position) != null) {
                mSearchHistoryService.deleteHistory(mSearchHistories.get(position));
                initHistoryList();
            }
            return true;
        });

        //??????????????????
        binding.fabSearchStop.setOnClickListener(v -> {
            stopSearch();
        });
    }


    //????????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        this.menu = menu;
        initSourceGroupMenu();
        return true;
    }


    //????????????????????????
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.action_disable_source).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    //?????????????????????
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
     * ???????????????????????????
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

        mDisableSourceDia = new MultiChoiceDialog().create(this, "?????????????????????",
                mSourcesName, isDisables, dSourceCount, (dialog, which) -> {
                    BookSourceManager.saveDatas(sources)
                            .subscribe(new MySingleObserver<Boolean>() {
                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {
                                    if (aBoolean){
                                        ToastUtils.showSuccess("????????????");
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
     * ?????????????????????
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
     * ?????????????????????
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
     * ?????????????????????
     */
    private void initSearchList() {
        //initmBooksBean();
        binding.rvSearchBooksList.setVisibility(View.VISIBLE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
    }


    /**
     * ??????????????????
     */
    private void getData() {
        initSearchList();
        mBooksBean.clear();
        mBooks.clear();
        List<ReadCrawler> readCrawlers = ReadCrawlerUtil
                .getEnableReadCrawlers(SharedPreUtils.getInstance().getString("searchGroup"));
        allThreadCount = readCrawlers.size();
        if (allThreadCount == 0) {
            ToastUtils.showWarring("?????????????????????????????????????????????");
            binding.rpb.setIsAutoLoading(false);
            return;
        }
        /*for (ReadCrawler readCrawler : readCrawlers) {
            searchBookByCrawler(readCrawler, readCrawler.getSearchCharset());
        }*/
        //??????????????????????????????
        searchEngine.initSearchEngine(readCrawlers);
        //??????
        searchEngine.search(searchKey);
    }

    /**
     * ??????
     */
    private void search() {
        binding.rpb.setIsAutoLoading(true);

        //??????????????????
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
            //????????????
            mSearchBookAdapter = new SearchBookAdapter(this, mBooks, searchEngine, searchKey);
            binding.rvSearchBooksList.setAdapter(mSearchBookAdapter);
            //?????????????????????
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
            //???????????????
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
            binding.etSearchKey.setText("");//???????????????
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

    //??????????????????
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
