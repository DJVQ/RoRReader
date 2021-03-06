package com.example.myreadproject8.ui.popmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.MenuCumtomizeLayoutBinding;
import com.example.myreadproject8.entity.ReadStyle;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.ui.dialog.DialogCreator;
import com.example.myreadproject8.ui.dialog.MyAlertDialog;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.MeUtils;
import com.example.myreadproject8.util.zip.ZipUtils;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class CustomizeLayoutMenu extends FrameLayout {

    private MenuCumtomizeLayoutBinding binding;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();
    private String bgPath;
    private BgImgListAdapter bgImgListAdapter;

    public CustomizeLayoutMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomizeLayoutMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomizeLayoutMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = MenuCumtomizeLayoutBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initWidget();
        initListener();
    }

    private void initWidget() {
        binding.ivBgColor.setImageDrawable(setting.getBgDrawable(setting.getCurReadStyleIndex(), context, 200, 120));
        binding.ivFontColor.setImageDrawable(new ColorDrawable(setting.getTextColor()));
        binding.cbShareLayout.setChecked(setting.isSharedLayout());

        //???????????????
        bgImgListAdapter = new BgImgListAdapter(context);
        bgImgListAdapter.initList();
        binding.bgImgList.setAdapter(bgImgListAdapter);
    }

    public void upColor() {
        int curStyleIndex = setting.getCurReadStyleIndex();
        if (!setting.isDayStyle()){
            ToastUtils.showInfo("??????????????????????????????");
            curStyleIndex = 6;
        }
        binding.ivFontColor.setImageDrawable(new ColorDrawable(setting.getTextColor()));
        binding.ivBgColor.setImageDrawable(setting.getBgDrawable(curStyleIndex, context, 200, 120));
    }

    private void initListener() {
        binding.ivBgColor.setOnClickListener(v -> {
            ColorPickerDialog.newBuilder()
                    .setColor(setting.getBgColor())
                    .setShowAlphaSlider(false)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setDialogId(APPCONST.SELECT_BG_COLOR)
                    .show((FragmentActivity) context);
        });
        //??????????????????
        binding.ivFontColor.setOnClickListener(view ->
                ColorPickerDialog.newBuilder()
                        .setColor(setting.getTextColor())
                        .setShowAlphaSlider(false)
                        .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                        .setDialogId(APPCONST.SELECT_TEXT_COLOR)
                        .show((FragmentActivity) context));
        binding.cbShareLayout.setOnClickListener(v -> {
            if (!setting.isSharedLayout()) {
                DialogCreator.createCommonDialog(context, "??????",
                        context.getString(R.string.share_layout_tip),
                        true, (dialog, which) -> {
                            setting.setSharedLayout(true);
                            setting.sharedLayout();
                            SysManager.saveSetting(setting);
                            binding.cbShareLayout.setChecked(true);
                        }, null);
            } else {
                setting.setSharedLayout(false);
                setting.sharedLayout();
                SysManager.saveSetting(setting);
                binding.cbShareLayout.setChecked(false);
            }
        });
        binding.bgImgList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i == 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                context.startActivityForResult(intent, APPCONST.REQUEST_SELECT_BG);
            } else {
                bgPath = bgImgListAdapter.getItemAssetsFile(i - 1);
                setAssetsBg(bgPath);
            }
        });
        binding.tvSaveLayout.setOnClickListener(v -> {
            CharSequence[] items = new CharSequence[6];
            for (int i = 0; i < items.length; i++) {
                if (i == 5) {
                    items[i] = "????????????";
                    break;
                }
                items[i] = "??????" + (i + 1);
            }
            MyAlertDialog.build(context)
                    .setTitle("???????????????????????????")
                    .setItems(items, (dialog, which) -> {
                        if (which == 5) which = 6;
                        setting.saveLayout(which);
                        SysManager.saveSetting(setting);
                        callback.upStyle();
                        ToastUtils.showSuccess("??????????????????");
                    }).show();
        });
        binding.tvImportLayout.setOnClickListener(v -> importLayout());
        binding.tvExportLayout.setOnClickListener(v -> exportLayout());
        binding.tvResetLayout.setOnClickListener(v -> {
            DialogCreator.createCommonDialog(context, "????????????",
                    "???????????????????????????????????????????????????????????????????????????",
                    true, (dialog, which) -> {
                        setting.resetLayout();
                        SysManager.saveSetting(setting);
                        callback.upBg();
                        callback.upStyle();
                        upColor();
                        ToastUtils.showSuccess("??????????????????");
                    }, null);
        });
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    public void setAssetsBg(String path) {
        setting.setBgIsColor(false);
        setting.setBgIsAssert(true);
        setting.setBgPath(path);
        SysManager.saveSetting(setting);
        upColor();
        callback.upBg();
    }

    public void setCustomBg(String path) {
        setting.setBgIsColor(false);
        setting.setBgIsAssert(false);
        setting.setBgPath(path);
        SysManager.saveSetting(setting);
        upColor();
        callback.upBg();
    }

    private void importLayout() {
        ToastUtils.showInfo("?????????????????????????????????");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        context.startActivityForResult(intent, APPCONST.REQUEST_IMPORT_LAYOUT);
    }

    private void exportLayout() {
        CharSequence[] items = new CharSequence[5];
        for (int i = 0; i < items.length; i++) {
            items[i] = "??????" + (i + 1);
        }
        MyAlertDialog.build(context)
                .setTitle("???????????????????????????")
                .setItems(items, (dialog, which) -> {
                    if (setting.exportLayout(which)){
                        DialogCreator.createTipDialog(context,
                                "??????????????????????????????????????????" + APPCONST.FILE_DIR + "readConfig.zip");
                    }
                }).show();
    }

    public void zip2Layout(String zipPath) {
        try {
            ZipUtils.unzipFile(zipPath, APPCONST.TEM_FILE_DIR);
            String json = FileUtils.readText(APPCONST.TEM_FILE_DIR + "readConfig.fyl");
            if (json == null) {
                ToastUtils.showError("??????????????????????????????????????????????????????");
                return;
            }
            ReadStyle readStyle = GsonExtensionsKt.getGSON().fromJson(json, ReadStyle.class);
            CharSequence[] items = new CharSequence[5];
            for (int i = 0; i < items.length; i++) {
                items[i] = "??????" + (i + 1);
            }
            MyAlertDialog.build(context)
                    .setTitle("????????????????????????")
                    .setItems(items, (dialog, which) -> {
                        setting.importLayout(which, readStyle);
                        if (!readStyle.bgIsColor() && !readStyle.bgIsAssert()){
                            FileUtils.copy(APPCONST.TEM_FILE_DIR + "bg.fyl",APPCONST.BG_FILE_DIR + "bg" + which + ".fyl");
                            readStyle.setBgPath(APPCONST.BG_FILE_DIR + "bg" + which + ".fyl");
                        }
                        FileUtils.deleteFile(APPCONST.TEM_FILE_DIR + "readConfig.fyl");
                        FileUtils.deleteFile(APPCONST.TEM_FILE_DIR + "bg.fyl");
                        ToastUtils.showSuccess("??????????????????");
                        callback.upStyle();
                    }).show();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showError("??????????????????" + e.getLocalizedMessage());
        }
    }

    public interface Callback {
        void upBg();
        void upStyle();
    }

    private static class BgImgListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater mInflater;
        private List<String> assetsFiles;
        final BitmapFactory.Options options = new BitmapFactory.Options();

        BgImgListAdapter(Context context) {
            this.context = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            options.inJustDecodeBounds = false;
            options.inSampleSize = 4;
        }

        void initList() {
            AssetManager am = context.getAssets();
            String[] path;
            try {
                path = am.list("bg");  //????????????,??????????????????????????????????????????
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            assetsFiles = new ArrayList<>();
            Collections.addAll(assetsFiles, path);
        }

        @Override
        public int getCount() {
            return assetsFiles.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        String getItemAssetsFile(int position) {
            return "bg/" + assetsFiles.get(position);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_read_bg, null);
                holder.mImage = convertView.findViewById(R.id.iv_bg);
                holder.mTitle = convertView.findViewById(R.id.tv_desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                holder.mTitle.setText("????????????");
                holder.mTitle.setTextColor(Color.parseColor("#CBCBCB"));
                holder.mImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_image));
            } else {
                String path = assetsFiles.get(position - 1);
                holder.mTitle.setText(getFileName(path));
                holder.mTitle.setTextColor(Color.parseColor("#909090"));
                try {
                    /*BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.mImage.getDrawable();
                    //???????????????????????????????????????????????????
                    if (bitmapDrawable != null && !bitmapDrawable.getBitmap().isRecycled()) {
                        bitmapDrawable.getBitmap().recycle();
                    }*/
                    //?????????????????????
                    Bitmap bmp = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), getItemAssetsFile(position - 1), 256, 256);
                    holder.mImage.setImageBitmap(bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.mImage.setImageBitmap(null);
                }
            }
            return convertView;
        }

        String getFileName(String path) {
            int start = path.lastIndexOf("/");
            int end = path.lastIndexOf(".");
            if (end < 0) end = path.length();
            return path.substring(start + 1, end);
        }

        private static class ViewHolder {
            private TextView mTitle;
            private ImageView mImage;
        }

    }
}
