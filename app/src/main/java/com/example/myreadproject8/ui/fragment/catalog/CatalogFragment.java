package com.example.myreadproject8.ui.fragment.catalog;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentCatalogBinding;
import com.example.myreadproject8.ui.presenter.book.catalog.CatalogPresenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class CatalogFragment extends Fragment {
    private FragmentCatalogBinding binding;

    private CatalogPresenter mCatalogPresent;

    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        mCatalogPresent = new CatalogPresenter(this);
        mCatalogPresent.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public RelativeLayout getRlCatalog() {
        return binding.rlCatalog;
    }

    public ListView getLvChapterList() {
        return binding.lvChapterList;
    }

    public FloatingActionButton getFcChangeSort() {
        return binding.changeSort;
    }

    public CatalogPresenter getmCatalogPresent() {
        return mCatalogPresent;
    }

    public ProgressBar getPbLoading() {
        return binding.pbLoading;
    }
}