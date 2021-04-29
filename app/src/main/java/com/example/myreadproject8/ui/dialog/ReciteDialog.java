package com.example.myreadproject8.ui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.DialogReciteBinding;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.service.ReciteService;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;

/**
 * created by ycq on 2021/4/24 0024
 * describe：
 */
public class ReciteDialog extends DialogFragment {
    DialogReciteBinding binding;
    private Recite recite;
    private Activity activity;
    private OnSaveRecite onSaveRecite;
    private ReciteService reciteService;
    boolean mIsResetRecite = false;

    public ReciteDialog(Activity activity,Recite recite,OnSaveRecite onSaveRecite){
        this.activity = activity;
        this.recite = recite;
        this.onSaveRecite = onSaveRecite;
    }

    public ReciteDialog(Activity activity,Recite recite,OnSaveRecite onSaveRecite,boolean isResetRecite){
        this.activity = activity;
        this.recite = recite;
        this.onSaveRecite = onSaveRecite;
        mIsResetRecite = isResetRecite;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DialogReciteBinding.inflate(getLayoutInflater());
        if(mIsResetRecite){
            binding.tvCancel.setOnClickListener(v1->dismiss());
            return binding.getRoot();

        }
        reciteService = new ReciteService();
        binding.needReciteContent.setText(recite.getReciteContent());
        ArrayAdapter<CharSequence> reciteNumAdapter = ArrayAdapter.createFromResource(activity,
                R.array.recite_nums, android.R.layout.simple_spinner_item);
        reciteNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.reciteNum.setSelection(0);
        binding.reciteNum.setAdapter(reciteNumAdapter);

        binding.tvCancel.setOnClickListener(v1->dismiss());
        binding.tvConfirm.setOnClickListener(v->{
            if(StringHelper.isEmpty(binding.etReciteTitle.getText().toString())||StringHelper.isEmpty(binding.needReciteContent.getText().toString())){
                ToastUtils.showWarring("标题或内容不能为空!");
                return;
            }
            recite.setReciteT(binding.etReciteTitle.getText().toString());
            recite.setReciteContent(binding.needReciteContent.getText().toString());
            recite.setAddDate(System.currentTimeMillis());
            recite.setReciteNum(binding.reciteNum.getSelectedItemPosition()+1);
            for(Recite recite:reciteService.findAllRecite()){
                System.out.println("myTest"+recite.getReciteNum());
            }
            if(reciteService.findReciteByT(binding.etReciteTitle.getText().toString())!=null){
                ToastUtils.showWarring("已经存在相同的标题!");
                return;
            }
            reciteService.addRecite(recite);
            onSaveRecite.success();
            dismiss();
        });
        return binding.getRoot();
    }


    public interface OnSaveRecite{
        void success();
    }
}
