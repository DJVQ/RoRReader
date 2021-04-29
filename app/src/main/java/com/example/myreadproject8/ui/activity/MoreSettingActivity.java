package com.example.myreadproject8.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.ActivityMoreSettingBinding;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MultiChoiceDialog;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import static com.example.myreadproject8.common.APPCONST.BOOK_CACHE_PATH;

public class MoreSettingActivity extends BaseActivity {
    private ActivityMoreSettingBinding binding;
    private boolean needRefresh;
    private boolean upMenu;

    private Setting mSetting;
    private boolean isVolumeTurnPage;
    private int resetScreenTime;
    private int catheCap;
    private boolean noMenuTitle;
    private int sortStyle;

    private ArrayList<Book> mBooks;
    int booksCount;
    CharSequence[] mBooksName;
    int threadNum;


    //选择一键缓存书籍对话框
    private AlertDialog mDownloadAllDia;
    @Override
    protected void bindView() {
        binding = ActivityMoreSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        needRefresh = false;
        mSetting = SysManager.getSetting();
        isVolumeTurnPage = mSetting.isVolumeTurnPage();
        resetScreenTime = mSetting.getResetScreen();
        catheCap = mSetting.getCatheGap();
        sortStyle = mSetting.getSortStyle();
        noMenuTitle = mSetting.isNoMenuChTitle();
        threadNum = SharedPreUtils.getInstance().getInt(getString(R.string.threadNum), 8);
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        setUpToolbar();
    }

    private void setUpToolbar() {
        getSupportActionBar().setTitle("设置");
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        initSwitchStatus();
        if (sortStyle == 1) {
            binding.tvBookSort.setText(getString(R.string.time_sort));
        } else if (sortStyle == 2) {
            binding.tvBookSort.setText(getString(R.string.book_name_sort));
        }
        binding.tvThreadNum.setText(getString(R.string.cur_thread_num, threadNum));
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.rlVolume.setOnClickListener(
                (v) -> {
                    if (isVolumeTurnPage) {
                        isVolumeTurnPage = false;
                    } else {
                        isVolumeTurnPage = true;
                    }
                    binding.scVolume.setChecked(isVolumeTurnPage);
                    mSetting.setVolumeTurnPage(isVolumeTurnPage);
                    SysManager.saveSetting(mSetting);
                }
        );

        binding.rlContentReplace.setOnClickListener(v -> startActivity(new Intent(this, ReplaceRuleActivity.class)));
        binding.rlNoMenuTitle.setOnClickListener(
                (v) -> {
                    upMenu = true;
                    if (noMenuTitle) {
                        noMenuTitle = false;
                    } else {
                        noMenuTitle = true;
                    }
                    binding.scNoMenuTitle.setChecked(noMenuTitle);
                    mSetting.setNoMenuChTitle(noMenuTitle);
                    SysManager.saveSetting(mSetting);
                }
        );
        binding.llBookSort.setOnClickListener(v -> {
            MyAlertDialog.build(this)
                    .setTitle(getString(R.string.book_sort))
                    .setSingleChoiceItems(R.array.book_sort, sortStyle, (dialog, which) -> {
                        sortStyle = which;
                        mSetting.setSortStyle(sortStyle);
                        SysManager.saveSetting(mSetting);
                        if (sortStyle == 0) {
                            binding.tvBookSort.setText(getString(R.string.manual_sort));
                            if (!SharedPreUtils.getInstance().getBoolean("manualSortTip")) {
                                DialogCreator.createTipDialog(this, "可在书架编辑状态下长按移动书籍进行排序！");
                                SharedPreUtils.getInstance().putBoolean("manualSortTip", true);
                            }
                        } else if (sortStyle == 1) {
                            binding.tvBookSort.setText(getString(R.string.time_sort));
                        } else if (sortStyle == 2) {
                            binding.tvBookSort.setText(getString(R.string.book_name_sort));
                        }
                        dialog.dismiss();
                    }).setNegativeButton("取消", null).show();
        });

        binding.llThreadNum.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_number_picker, null);
            NumberPicker threadPick = view.findViewById(R.id.number_picker);
            threadPick.setMaxValue(1024);
            threadPick.setMinValue(1);
            threadPick.setValue(threadNum);
            threadPick.setOnScrollListener((view1, scrollState) -> {

            });
            MyAlertDialog.build(this)
                    .setTitle("搜索线程数")
                    .setView(view)
                    .setPositiveButton("确定", (dialog, which) -> {
                        threadNum = threadPick.getValue();
                        SharedPreUtils.getInstance().putInt(getString(R.string.threadNum), threadNum);
                        binding.tvThreadNum.setText(getString(R.string.cur_thread_num, threadNum));
                    }).setNegativeButton("取消", null)
                    .show();
        });

        binding.llDownloadAll.setOnClickListener(v -> {
            App.runOnUiThread(() -> {
                if (mDownloadAllDia != null) {
                    mDownloadAllDia.show();
                    return;
                }

                initmBooks();

                if (mBooks.size() == 0) {
                    ToastUtils.showWarring("当前书架没有支持缓存的书籍！");
                    return;
                }

                int booksCount = mBooks.size();
                CharSequence[] mBooksName = new CharSequence[booksCount];
                boolean[] isDownloadAll = new boolean[booksCount];
                int daBookCount = 0;
                for (int i = 0; i < booksCount; i++) {
                    Book book = mBooks.get(i);
                    mBooksName[i] = book.getName();
                    isDownloadAll[i] = book.getIsDownLoadAll();
                    if (isDownloadAll[i]) {
                        daBookCount++;
                    }
                }

                mDownloadAllDia = new MultiChoiceDialog().create(this, "一键缓存的书籍",
                        mBooksName, isDownloadAll, daBookCount, (dialog, which) -> {
                            BookService.getInstance().updateBooks(mBooks);
                        }, null, new DialogCreator.OnMultiDialogListener() {
                            @Override
                            public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                                mBooks.get(which).setIsDownLoadAll(isChecked);
                            }

                            @Override
                            public void onSelectAll(boolean isSelectAll) {
                                for (Book book : mBooks) {
                                    book.setIsDownLoadAll(isSelectAll);
                                }
                            }
                        });

            });
        });

        binding.rlResetScreen.setOnClickListener(v -> binding.scResetScreen.performClick());
        binding.rlCatheGap.setOnClickListener(v -> binding.scCatheGap.performClick());

        binding.rlDeleteCathe.setOnClickListener(v -> {
            App.runOnUiThread(() -> {
                File catheFile = getCacheDir();
                String catheFileSize = FileUtils.getFileSize(FileUtils.getDirSize(catheFile));

                File eCatheFile = new File(BOOK_CACHE_PATH);
                String eCatheFileSize;
                if (eCatheFile.exists() && eCatheFile.isDirectory()) {
                    eCatheFileSize = FileUtils.getFileSize(FileUtils.getDirSize(eCatheFile));
                } else {
                    eCatheFileSize = "0";
                }
                CharSequence[] cathes = {"章节缓存：" + eCatheFileSize, "图片缓存：" + catheFileSize};
                boolean[] catheCheck = {true, true};
                new MultiChoiceDialog().create(this, "清除缓存", cathes, catheCheck, 2,
                        (dialog, which) -> {
                            String tip = "";
                            if (catheCheck[0]) {
                                BookService.getInstance().deleteAllBookCathe();
                                tip += "章节缓存 ";
                            }
                            if (catheCheck[1]) {
                                FileUtils.deleteFile(catheFile.getAbsolutePath());
                                tip += "图片缓存 ";
                            }
                            if (tip.length() > 0) {
                                tip += "清除成功";
                                ToastUtils.showSuccess(tip);
                            }
                        }, null, null);
            });
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSpinner();
    }

    @Override
    public void finish() {
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_NEED_REFRESH, needRefresh);
        result.putExtra(APPCONST.RESULT_UP_MENU, upMenu);
        setResult(AppCompatActivity.RESULT_OK, result);
        super.finish();
        super.finish();
    }

    private void initSpinner() {
        // initSwitchStatus() be called earlier than onCreate(), so setSelection() won't work
        ArrayAdapter<CharSequence> resetScreenAdapter = ArrayAdapter.createFromResource(this,
                R.array.reset_screen_time, android.R.layout.simple_spinner_item);
        resetScreenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.scResetScreen.setAdapter(resetScreenAdapter);
        int resetScreenSelection = 0;
        switch (resetScreenTime) {
            case 0:
                resetScreenSelection = 0;
                break;
            case 1:
                resetScreenSelection = 1;
                break;
            case 3:
                resetScreenSelection = 2;
                break;
            case 5:
                resetScreenSelection = 3;
                break;
        }
        binding.scResetScreen.setSelection(resetScreenSelection);
        binding.scResetScreen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        resetScreenTime = 0;
                        break;
                    case 1:
                        resetScreenTime = 1;
                        break;
                    case 2:
                        resetScreenTime = 3;
                        break;
                    case 3:
                        resetScreenTime = 5;
                        break;
                }
                mSetting.setResetScreen(resetScreenTime);
                SysManager.saveSetting(mSetting);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> catheGapAdapter = ArrayAdapter.createFromResource(this,
                R.array.cathe_chapter_gap, android.R.layout.simple_spinner_item);
        catheGapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.scCatheGap.setAdapter(catheGapAdapter);

        if (catheCap == 0) {
            catheCap = 150;
            mSetting.setCatheGap(catheCap);
            SysManager.saveSetting(mSetting);
        }
        int catheGapSelection = catheCap / 50 - 1;

        binding.scCatheGap.setSelection(catheGapSelection);

        binding.scCatheGap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catheCap = (position + 1) * 50;
                mSetting.setCatheGap(catheCap);
                SysManager.saveSetting(mSetting);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void initmBooks() {
        if (mBooks != null) {
            return;
        }
        mBooks = (ArrayList<Book>) BookService.getInstance().getAllBooksNoHide();

        Iterator<Book> mBooksIter = mBooks.iterator();
        while (mBooksIter.hasNext()) {
            Book book = mBooksIter.next();
            if ("本地书籍".equals(book.getType())) {
                mBooksIter.remove();
            }
        }
        booksCount = mBooks.size();
        mBooksName = new CharSequence[booksCount];

        for (int i = 0; i < booksCount; i++) {
            Book book = mBooks.get(i);
            mBooksName[i] = book.getName();
        }
    }

    private void initSwitchStatus() {
        binding.scVolume.setChecked(isVolumeTurnPage);
        binding.scNoMenuTitle.setChecked(noMenuTitle);
    }
}