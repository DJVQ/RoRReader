package com.example.myreadproject8.ui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.DialogReciteBinding;
import com.example.myreadproject8.databinding.DialogResetReciteNumBinding;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.gen.ReciteDao;
import com.example.myreadproject8.greendao.service.ReciteService;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;

/**
 * created by ycq on 2021/4/26 0026
 * describe：
 */
public class ResetReciteNum extends DialogFragment {
    DialogResetReciteNumBinding binding;

    private ReciteService reciteService;
    private Recite recite;
    private Activity activity;
    private OnSaveResetRecite onSaveRecite;


    public ResetReciteNum(Activity activity,Recite recite,OnSaveResetRecite onSaveRecite){
        this.activity = activity;
        this.onSaveRecite = onSaveRecite;
        this.recite = recite;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogResetReciteNumBinding.inflate(getLayoutInflater());
        reciteService = new ReciteService();
        binding.resetTvCancel.setOnClickListener(v1->dismiss());
        ArrayAdapter<CharSequence> reciteNumAdapter = ArrayAdapter.createFromResource(activity,
                R.array.recite_nums, android.R.layout.simple_spinner_item);
        reciteNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.resetReciteNum.setSelection(0);
        binding.resetReciteNum.setAdapter(reciteNumAdapter);
        binding.resetTvConfirm.setOnClickListener(v->{
            reciteService.resetReciteNum(recite,binding.resetReciteNum.getSelectedItemPosition()+1);
            onSaveRecite.success();
            dismiss();
        });
        return binding.getRoot();
    }

    public interface OnSaveResetRecite{
        void success();
    }
}
