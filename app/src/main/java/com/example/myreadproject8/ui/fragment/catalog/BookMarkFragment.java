package com.example.myreadproject8.ui.fragment.catalog;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentBookMarkBinding;
import com.example.myreadproject8.ui.presenter.book.catalog.BookMarkPresenter;


public class BookMarkFragment extends Fragment {

    private FragmentBookMarkBinding binding;
    private BookMarkPresenter mBookMarkPresenter;


    public BookMarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookMarkBinding.inflate(inflater, container, false);
        mBookMarkPresenter = new BookMarkPresenter(this);
        mBookMarkPresenter.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public ListView getLvBookmarkList() {
        return binding.lvBookmarkList;
    }

    public BookMarkPresenter getmBookMarkPresenter() {
        return mBookMarkPresenter;
    }
}