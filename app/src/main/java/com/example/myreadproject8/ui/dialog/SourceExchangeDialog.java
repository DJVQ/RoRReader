package com.example.myreadproject8.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myreadproject8.databinding.DialogBookSourceBinding;
import com.example.myreadproject8.entity.SearchBookBean;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.ui.adapter.booksource.SourceExchangeAdapter;
import com.example.myreadproject8.util.mulvalmap.ConcurrentMultiValueMap;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.search.SearchEngine;
import com.example.myreadproject8.widget.RefreshProgressBar;

import java.util.ArrayList;
import java.util.List;



/**
 * 换源dialog
 */

public class SourceExchangeDialog extends Dialog {

    private static final String TAG = "SourceExchangeDialog";

    private DialogBookSourceBinding binding;

    //搜索引擎
    private SearchEngine searchEngine;
    //换源适配器
    private SourceExchangeAdapter mAdapter;
    //换源监听器
    private OnSourceChangeListener listener;

    //需要弹出此对话框的activity
    private Activity mActivity;
    //搜索值对应的书
    private Book mShelfBook;
    //搜索值对应的不同搜索源的书
    private List<Book> aBooks;

    private AlertDialog mErrorDia;
    //搜索源序号
    private int sourceIndex = -1;

    /***************************************************************************/
    public SourceExchangeDialog(@NonNull Activity activity, Book bookBean) {
        super(activity);
        mActivity = activity;
        mShelfBook = bookBean;
    }
    //设置当前搜索到的书
    public void setShelfBook(Book mShelfBook) {
        this.mShelfBook = mShelfBook;
    }
    //设置搜索值对应的不同搜索源的书
    public void setABooks(List<Book> aBooks){
        this.aBooks = aBooks;
    }
    //设置搜索源序号
    public void setSourceIndex(int sourceIndex){
        this.sourceIndex = sourceIndex;
    }
    //设置换源监听器
    public void setOnSourceChangeListener(OnSourceChangeListener listener) {
        this.listener = listener;
    }

    //获取搜索源对应的书
    public List<Book> getaBooks(){return aBooks;}
    /*****************************Initialization********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogBookSourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpWindow();
        initData();
        initClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //执行业务逻辑
        if (aBooks.size() == 0) {
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
            binding.ivStopSearch.setVisibility(View.VISIBLE);
            binding.rpb.setIsAutoLoading(true);
        }else {
            if (mAdapter.getItemCount() == 0) {
                mAdapter.addItems(aBooks);
            }
        }
    }

    /**
     * 设置Dialog显示的位置
     */
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        binding.it.toolbar.setTitle(mShelfBook.getName());
        binding.it.toolbar.setSubtitle(mShelfBook.getAuthor());
        //dialogTvTitle.setText(mShelfBook.getName() + "(" + mShelfBook.getAuthor() + ")");

        if (aBooks == null) {
            aBooks = new ArrayList<>();
        }

        //初始化适配器，为对话框绑定适配器
        mAdapter = new SourceExchangeAdapter();
        binding.dialogRvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        binding.dialogRvContent.setAdapter(mAdapter);

        //初始化搜索引擎
        searchEngine = new SearchEngine();
        searchEngine.initSearchEngine(ReadCrawlerUtil.getEnableReadCrawlers());
    }

    //初试化点击事件
    private void initClick() {
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            @Override
            public void loadMoreFinish(Boolean isAll) {
                synchronized (RefreshProgressBar.class) {
                    binding.rpb.setIsAutoLoading(false);//刷新进度条默认不动
                    binding.ivStopSearch.setVisibility(View.GONE);//停止按键默认不可见
                }
            }

            @Override
            public void loadMoreSearchBook(ConcurrentMultiValueMap<SearchBookBean, Book> items) {

            }

            @Override
            public void loadMoreSearchBook(List<Book> items) {
                //确保只有一个结果，（只想要一个结果）
                if (items != null && items.size() != 0) {
                    Book bean = items.get(0);
                    if (bean.getSource().equals(mShelfBook.getSource())) {
                        bean.setNewestChapterId("true");
                        sourceIndex = mAdapter.getItemSize();
                    }
                    mAdapter.addItem(items.get(0));
                    aBooks.add(bean);
                }
            }

            @Override
            public void searchBookError(Throwable throwable) {
                dismiss();
                DialogCreator.createTipDialog(mActivity, "未搜索到该书籍，书源加载失败！");
            }
        });

        mAdapter.setOnItemClickListener((view, pos) -> {
            if (listener == null) return;
            Book newBook = mAdapter.getItem(pos);
            if (mShelfBook.getSource() == null) {
                listener.onSourceChanged(newBook, pos);
                searchEngine.stopSearch();
                return;
            }
            if (mShelfBook.getSource().equals(newBook.getSource())) return;
            //当前书籍换为newBook（选中书源的书籍）
            mShelfBook = newBook;
            listener.onSourceChanged(newBook, pos);
            mAdapter.getItem(pos).setNewestChapterId("true");
            if (sourceIndex > -1)
                mAdapter.getItem(sourceIndex).setNewestChapterId("false");
            sourceIndex = pos;
            mAdapter.notifyDataSetChanged();
            dismiss();
        });

        binding.ivStopSearch.setOnClickListener(v -> searchEngine.stopSearch());
        binding.ivRefreshSearch.setOnClickListener(v -> {
            searchEngine.stopSearch();
            binding.ivStopSearch.setVisibility(View.VISIBLE);
            mAdapter.clear();
            aBooks.clear();
            mAdapter.notifyDataSetChanged();
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
        });
    }


    /**************************Interface**********************************/
    public interface OnSourceChangeListener {
        void onSourceChanged(Book bean, int pos);
    }

}
