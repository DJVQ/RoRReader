package com.example.myreadproject8.ui.fragment.booksource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myreadproject8.AAATest.observer.MySingleObserver;
import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentLocalSourceBinding;
import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.ui.activity.book.BookSourceActivity;
import com.example.myreadproject8.ui.adapter.booksource.LocalSourceAdapter;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;
import com.example.myreadproject8.util.messenge.RxUtils;
import com.example.myreadproject8.util.source.BookSourceManager;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.DividerItemDecoration;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;


/**
 * @author fengyue
 * @date 2021/2/10 18:46
 */
public class LocalSourceFragment extends BaseFragment {
    private FragmentLocalSourceBinding binding;
    private BookSourceActivity sourceActivity;
    private List<BookSource> mBookSources;
    private LocalSourceAdapter mAdapter;

    public LocalSourceFragment() {
    }

    public LocalSourceFragment(BookSourceActivity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentLocalSourceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        Single.create((SingleOnSubscribe<List<BookSource>>) emitter -> emitter.onSuccess(BookSourceManager.getAllLocalSource())).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<List<BookSource>>() {
            @Override
            public void onSuccess(@NonNull List<BookSource> sources) {
                mBookSources = sources;
                initSourceList();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
            }
        });

    }

    private void initSourceList() {
        mAdapter = new LocalSourceAdapter(mBookSources);
        mAdapter.setOnItemClickListener((view, pos) -> {
            mAdapter.setCheckedItem(pos);
            if (mAdapter.getCheckedCount() == mAdapter.getItemCount()) {
                binding.tvSelectAll.setText(R.string.cancle_select_all);
            } else {
                binding.tvSelectAll.setText(R.string.select_all);
            }
        });
        binding.tvSelectAll.setOnClickListener(v -> {
            if (mAdapter.getCheckedCount() != mAdapter.getItemCount()) {
                mAdapter.setCheckedAll(true);
                binding.tvSelectAll.setText(R.string.cancle_select_all);
            } else {
                mAdapter.setCheckedAll(false);
                binding.tvSelectAll.setText(R.string.select_all);
            }
        });
        binding.tvEnableSelected.setOnClickListener(v -> changeSourcesStatus(true));
        binding.tvDisableSelected.setOnClickListener(v -> changeSourcesStatus(false));
        binding.tvCheckSelected.setOnClickListener(v -> ToastUtils.showInfo("校验功能即将上线"));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(mAdapter);
        //设置分割线
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        mAdapter.refreshItems(mBookSources);
    }

    private void changeSourcesStatus(boolean isEnable) {
        List<BookSource> sources = mAdapter.getCheckedBookSources();
        if (sources.size() == 0) {
            ToastUtils.showWarring("请选择书源");
        } else {
            for (BookSource source : sources) {
                source.setEnable(isEnable);
            }
            Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplaceInTx(sources);
                emitter.onSuccess(true);
            }).compose(RxUtils::toSimpleSingle)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    public void startSearch(String newText) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(newText);
        }
    }

}
