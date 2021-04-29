package com.example.myreadproject8.ui.adapter.holder;

import android.content.Intent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadproject8.AAATest.observer.MyObserver;
import com.example.myreadproject8.AAATest.observer.MySingleObserver;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.entity.ReplaceRuleBean;
import com.example.myreadproject8.ui.adapter.replacerule.ReplaceRuleAdapter;
import com.example.myreadproject8.ui.dialog.ReplaceDialog;
import com.example.myreadproject8.util.file.ShareUtils;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;
import com.example.myreadproject8.util.read.ReplaceRuleManager;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.swipemenu.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * created by ycq on 2021/4/22 0022
 * describe：
 */
public class ReplaceRuleHolder extends ViewHolderImpl<ReplaceRuleBean> {
    private RelativeLayout rlContent;
    private TextView tvRuleSummary;
    private Button btTop;
    private Button btBan;
    private Button btShare;
    private Button btDelete;
    private AppCompatActivity activity;
    private ReplaceRuleAdapter.OnSwipeListener onSwipeListener;

    public ReplaceRuleHolder(AppCompatActivity activity, ReplaceRuleAdapter.OnSwipeListener onSwipeListener) {
        this.activity = activity;
        this.onSwipeListener = onSwipeListener;
    }


    @Override
    protected int getItemLayoutId() {
        return R.layout.item_replace_rule;
    }

    @Override
    public void initView() {
        rlContent = findById(R.id.rl_content);
        tvRuleSummary = findById(R.id.tv_rule_summary);
        btTop = findById(R.id.bt_top);
        btBan = findById(R.id.bt_ban);
        btShare = findById(R.id.bt_share);
        btDelete = findById(R.id.btnDelete);
    }

    @Override
    public void onBind(ReplaceRuleBean data, int pos) {
        banOrUse(data);

        rlContent.setOnClickListener(v -> {
            ReplaceDialog replaceDialog = new ReplaceDialog(activity, data,
                    () -> {
                        banOrUse(data);
                        ToastUtils.showSuccess("内容替换规则修改成功！");
                        refreshUI();
                    });
            replaceDialog.show(activity.getSupportFragmentManager(), "");
        });

        btTop.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            ReplaceRuleManager.toTop(data)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean){
                                onSwipeListener.onTop(pos, data);
                            }
                        }
                    });
        });

        btBan.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            data.setEnable(!data.getEnable());
            ReplaceRuleManager.saveData(data)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                banOrUse(data);
                                refreshUI();
                            }
                        }
                    });
        });

        btShare.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            List<ReplaceRuleBean> shareRuleBean = new ArrayList<>();
            shareRuleBean.add(data);
            ShareUtils.share(activity, GsonExtensionsKt.getGSON().toJson(shareRuleBean));
        });
        btDelete.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                ReplaceRuleManager.delData(data);
                e.onNext(true);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            onSwipeListener.onDel(pos, data);
                            refreshUI();
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showError("删除失败");
                        }
                    });

        });
    }

    private void banOrUse(ReplaceRuleBean data){
        if (data.getEnable()) {
            tvRuleSummary.setTextColor(getContext().getResources().getColor(R.color.textPrimary));
            if (StringHelper.isEmpty(data.getReplaceSummary())) {
                tvRuleSummary.setText(String.format("%s->%s", data.getRegex(), data.getReplacement()));
            }else {
                tvRuleSummary.setText(data.getReplaceSummary());
            }
            btBan.setText(getContext().getString(R.string.ban));
        } else {
            tvRuleSummary.setTextColor(getContext().getResources().getColor(R.color.textSecondary));
            if (StringHelper.isEmpty(data.getReplaceSummary())) {
                tvRuleSummary.setText(String.format("(禁用中)%s->%s", data.getRegex(), data.getReplacement()));
            }else {
                tvRuleSummary.setText(String.format("(禁用中)%s", data.getReplaceSummary()));
            }
            btBan.setText(R.string.enable_use);
        }
    }

    private void refreshUI(){
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_NEED_REFRESH, true);
        activity.setResult(AppCompatActivity.RESULT_OK, result);
    }
}
