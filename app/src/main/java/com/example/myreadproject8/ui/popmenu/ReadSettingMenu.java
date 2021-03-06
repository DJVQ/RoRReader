package com.example.myreadproject8.ui.popmenu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.MenuReadSettingBinding;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.Language;
import com.example.myreadproject8.ui.activity.OpenReadActivity;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.widget.page.PageMode;


public class ReadSettingMenu extends FrameLayout {

    private MenuReadSettingBinding binding;

    private View vLastLineSpacing = null;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();


    public ReadSettingMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ReadSettingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadSettingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = MenuReadSettingBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initWidget();
        initListener();
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    private void initWidget() {
        binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize()));
        binding.tvSwitchSt.setVisibility(GONE);
        binding.tvTextFont.setVisibility(GONE);
        initSwitchST(false);
        initComposition();
        initStyleImage();
        initStyle();
        initHVScreen();
    }

    private void initListener() {
        //????????????
        binding.tvReduceTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() > 10) {
                binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize() - 1));
                setting.setReadWordSize(setting.getReadWordSize() - 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //????????????
        binding.tvIncreaseTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() < 60) {
                binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize() + 1));
                setting.setReadWordSize(setting.getReadWordSize() + 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //????????????
        binding.tvSwitchSt.setOnClickListener(v -> {
            initSwitchST(true);
            callback.onRefreshPage();
        });
        //????????????
        binding.tvTextFont.setOnClickListener(v -> callback.onFontClick());
        //????????????
        binding.ivLineSpacing4.setOnClickListener(v -> setLineSpacing(0.6f, 0.4f, 4));
        //????????????
        binding.ivLineSpacing3.setOnClickListener(v -> setLineSpacing(1.2f, 1.1f, 3));
        //????????????
        binding.ivLineSpacing2.setOnClickListener(v -> setLineSpacing(1.8f, 1.8f, 2));
        //????????????
        binding.tvLineSpacing1.setOnClickListener(v -> setLineSpacing(1.0f, 0.9f, 1));
        //???????????????
        binding.tvLineSpacing0.setOnClickListener(v -> ((OpenReadActivity) context).showCustomizeMenu());
        //??????
        binding.tvIntent.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.alertDialogTheme)
                    .setTitle("??????")
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.indent),
                            setting.getIntent(),
                            (dialogInterface, i) -> {
                                setting.setIntent(i);
                                SysManager.saveSetting(setting);
                                callback.onRefreshUI();
                                dialogInterface.dismiss();
                            })
                    .create();
            dialog.show();
        });
        //????????????
        binding.ivCommonStyle.setOnClickListener(v -> selectedStyle(0));
        binding.ivLeatherStyle.setOnClickListener(v -> selectedStyle(1));
        binding.ivProtectEyeStyle.setOnClickListener(v -> selectedStyle(2));
        binding.ivBreenStyle.setOnClickListener(v -> selectedStyle(3));
        binding.ivBlueDeepStyle.setOnClickListener(v -> selectedStyle(4));
        binding.ivCustomStyle.setOnClickListener(v -> {
            setting.saveLayout(5);
            if (setting.isDayStyle()) {
                selectedStyle(5);
            }
            ((OpenReadActivity) context).showCustomizeLayoutMenu();
        });
        //????????????
        binding.readTvAutoPage.setOnClickListener(v -> callback.onAutoPageClick());
        //????????????
        binding.readTvPageMode.setOnClickListener(v -> {
            //????????????????????????
            int checkedItem;
            switch (setting.getPageMode()) {
                case COVER:
                    checkedItem = 0;
                    break;
                case SIMULATION:
                    checkedItem = 1;
                    break;
                case SLIDE:
                    checkedItem = 2;
                    break;
                case VERTICAL_COVER:
                    checkedItem = 3;
                    break;
                case SCROLL:
                    checkedItem = 4;
                    break;
                case NONE:
                    checkedItem = 5;
                    break;
                default:
                    checkedItem = 0;
            }
            MyAlertDialog.build(context)
                    .setTitle("????????????")
                    .setSingleChoiceItems(R.array.page_mode, checkedItem, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                setting.setPageMode(PageMode.COVER);
                                break;
                            case 1:
                                setting.setPageMode(PageMode.SIMULATION);
                                break;
                            case 2:
                                setting.setPageMode(PageMode.SLIDE);
                                break;
                            case 3:
                                setting.setPageMode(PageMode.VERTICAL_COVER);
                                break;
                            case 4:
                                setting.setPageMode(PageMode.SCROLL);
                                break;
                            case 5:
                                setting.setPageMode(PageMode.NONE);
                                break;
                        }
                        dialog.dismiss();
                        SysManager.saveSetting(setting);
                        callback.onPageModeChange();
                    }).show();
        });
        //??????????????????
        binding.readTvHvScreen.setOnClickListener(v -> {
            setting.setHorizontalScreen(!setting.isHorizontalScreen());
            initHVScreen();
            SysManager.saveSetting(setting);
            callback.onHVChange();
        });
        //????????????
        binding.readTvMoreSetting.setOnClickListener(v -> callback.onMoreSettingClick());
    }

    private void initSwitchST(boolean isChange) {
        switch (setting.getLanguage()){
            case normal:
                binding.tvSwitchSt.setSelected(false);
                binding.tvSwitchSt.setText("???");
                if (isChange){
                    setting.setLanguage(Language.traditional);
                    ToastUtils.showInfo("???????????????????????????");
                    initSwitchST(false);
                }
                break;
            case traditional:
                binding.tvSwitchSt.setSelected(true);
                binding.tvSwitchSt.setText("???");
                if (isChange){
                    setting.setLanguage(Language.simplified);
                    initSwitchST(false);
                }
                break;
            case simplified:
                binding.tvSwitchSt.setSelected(true);
                binding.tvSwitchSt.setText("???");
                if (isChange){
                    setting.setLanguage(Language.normal);
                    ToastUtils.showInfo("??????????????????/?????????????????????????????????");
                    initSwitchST(false);
                }
                break;
        }
        if (isChange){
            SysManager.saveSetting(setting);
        }
    }

    public void initStyleImage() {
        binding.ivCommonStyle.setImageDrawable(setting.getBgDrawable(0, context, 50, 50));
        binding.ivLeatherStyle.setImageDrawable(setting.getBgDrawable(1, context, 50, 50));
        binding.ivProtectEyeStyle.setImageDrawable(setting.getBgDrawable(2, context, 50, 50));
        binding.ivBreenStyle.setImageDrawable(setting.getBgDrawable(3, context, 50, 50));
        binding.ivBlueDeepStyle.setImageDrawable(setting.getBgDrawable(4, context, 50, 50));
    }

    public void initStyle() {
        if (!setting.isDayStyle()){
            return;
        }
        binding.ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivBlueDeepStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivCustomStyle.setSelected(false);
        switch (setting.getCurReadStyleIndex()) {
            case 0:
                binding.ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 1:
                binding.ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 2:
                binding.ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 3:
                binding.ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 4:
                binding.ivBlueDeepStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 5:
                binding.ivCustomStyle.setSelected(true);
                break;
        }
    }

    public void initComposition(){
        if (vLastLineSpacing != null) {
            vLastLineSpacing.setSelected(false);
        }
        switch (setting.getComposition()){
            case 0:
                binding.tvLineSpacing0.setSelected(true);
                vLastLineSpacing = binding.tvLineSpacing0;
                break;
            case 2:
                binding.ivLineSpacing2.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing2;
                break;
            case 3:
                binding.ivLineSpacing3.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing3;
                break;
            case 4:
                binding.ivLineSpacing4.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing4;
                break;
            default:
                binding.tvLineSpacing1.setSelected(true);
                vLastLineSpacing = binding.tvLineSpacing1;
                break;
        }
    }


    private void initHVScreen(){
        if (setting.isHorizontalScreen()){
            binding.readTvHvScreen.setText("????????????");
        }else {
            binding.readTvHvScreen.setText("????????????");
        }
    }

    private void setLineSpacing(float lineMultiplier, float paragraphSize, int composition){
        setting.setLineMultiplier(lineMultiplier);
        setting.setParagraphSize(paragraphSize);
        setting.setComposition(composition);
        SysManager.saveSetting(setting);
        initComposition();
        callback.onTextSizeChange();
    }

    private void selectedStyle(int readStyleIndex) {
        setting.setCurReadStyleIndex(readStyleIndex);
        SysManager.saveSetting(setting);
        initWidget();
        if (callback != null) {
            callback.onStyleChange();
        }
    }

    public interface Callback{
        void onRefreshPage();
        void onPageModeChange();
        void onRefreshUI();
        void onStyleChange();
        void onTextSizeChange();
        void onFontClick();
        void onAutoPageClick();
        void onHVChange();
        void onMoreSettingClick();
    }
}
