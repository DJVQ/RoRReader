package com.example.myreadproject8.ui.fragment.booksource;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentDIYSourceBinding;
import com.example.myreadproject8.ui.activity.book.BookSourceActivity;
import com.example.myreadproject8.ui.fragment.base.BaseFragment;


public class DIYSourceFragment extends BaseFragment {
    private FragmentDIYSourceBinding binding;
    private final BookSourceActivity sourceActivity;
    private boolean isSearch;
    public DIYSourceFragment() {
        sourceActivity = (BookSourceActivity) getActivity();
    }

    public DIYSourceFragment(BookSourceActivity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentDIYSourceBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    public void startSearch(String newText) {
        isSearch = !TextUtils.isEmpty(newText);
    }
}