package com.example.myreadproject8.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.FragmentMineBinding;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.ui.activity.MoreSettingActivity;
import com.example.myreadproject8.ui.activity.book.BookSourceActivity;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.storage.Backup;
import com.example.myreadproject8.util.utils.storage.Restore;

import java.util.ArrayList;


public class MineFragment extends BaseFragment {
    private FragmentMineBinding binding;

    private Setting mSetting;
    private String[] backupMenu;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    backup();
                    break;
                case 3:
                    restore();
                    break;
            }
        }
    };

    public MineFragment() {
    }

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSetting = SysManager.getSetting();
        backupMenu = new String[]{
                App.getMContext().getResources().getString(R.string.menu_backup_backup),
                App.getMContext().getResources().getString(R.string.menu_backup_restore),
        };
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.mineRlBookSource.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), BookSourceActivity.class));
        });
        binding.mineRlBackup.setOnClickListener(v -> {
            AlertDialog bookDialog = MyAlertDialog.build(getContext())
                    .setTitle(getContext().getResources().getString(R.string.menu_bookcase_backup))
                    .setItems(backupMenu, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                mHandler.sendMessage(mHandler.obtainMessage(2));
                                break;
                            case 1:
                                mHandler.sendMessage(mHandler.obtainMessage(3));
                                break;
                        }
                    })
                    .setNegativeButton(null, null)
                    .setPositiveButton(null, null)
                    .create();
            bookDialog.show();
        });
        binding.mineRlSetting.setOnClickListener(v -> {
            Intent settingIntent = new Intent(getActivity(), MoreSettingActivity.class);
            startActivity(settingIntent);
        });
    }

    /**
     * ??????
     */
    private void backup() {
        ArrayList<Book> mBooks = (ArrayList<Book>) BookService.getInstance().getAllBooks();
        if (mBooks.size() == 0) {
            ToastUtils.showWarring("?????????????????????????????????????????????");
            return;
        }
        DialogCreator.createCommonDialog(getContext(), "????????????????", "?????????????????????????????????", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    /*if (mBackupAndRestore.backup("localBackup")) {
                        DialogCreator.createTipDialog(getContext(), "????????????????????????????????????" + APPCONST.BACKUP_FILE_DIR);
                    } else {
                        DialogCreator.createTipDialog(getContext(), "???????????????????????????????????????");
                    }*/
                    Backup.INSTANCE.backup(App.getMContext(), APPCONST.BACKUP_FILE_DIR, new Backup.CallBack() {
                        @Override
                        public void backupSuccess() {
                            DialogCreator.createTipDialog(getContext(), "????????????????????????????????????" + APPCONST.BACKUP_FILE_DIR);
                        }

                        @Override
                        public void backupError(@io.reactivex.annotations.NonNull String msg) {
                            DialogCreator.createTipDialog(getContext(), "???????????????????????????????????????");
                        }
                    }, false);
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    /**
     * ??????
     */
    private void restore() {
        DialogCreator.createCommonDialog(getContext(), "????????????????", "????????????????????????????????????", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    /*if (mBackupAndRestore.restore("localBackup")) {
                        mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "???????????????\n??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                        mSetting = SysManager.getSetting();
                        ToastUtils.showSuccess("?????????????????????");
                    } else {
                        DialogCreator.createTipDialog(getContext(), "???????????????????????????????????????????????????????????????");
                    }*/
                    Restore.INSTANCE.restore(APPCONST.BACKUP_FILE_DIR, new Restore.CallBack() {
                        @Override
                        public void restoreSuccess() {
                            mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "???????????????\n??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                            SysManager.regetmSetting();
                            ToastUtils.showSuccess("?????????????????????");
                        }

                        @Override
                        public void restoreError(@io.reactivex.annotations.NonNull String msg) {
                            DialogCreator.createTipDialog(getContext(), "???????????????????????????????????????????????????????????????");
                        }
                    });
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    public boolean isRecreate() {
        return binding == null;
    }
}