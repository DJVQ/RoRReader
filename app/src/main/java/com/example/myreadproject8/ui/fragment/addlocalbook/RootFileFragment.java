package com.example.myreadproject8.ui.fragment.addlocalbook;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.myreadproject8.R;
import com.example.myreadproject8.databinding.FragmentRootFileBookBinding;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.ui.adapter.filesystem.FileSystemAdapter;
import com.example.myreadproject8.ui.fragment.BaseFileFragment;
import com.example.myreadproject8.util.file.FileStack;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.widget.DividerItemDecoration;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RootFileFragment extends BaseFileFragment {
    private static final String TAG = "FileCategoryFragment";

    private FragmentRootFileBookBinding binding;

    private FileStack mFileStack;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentRootFileBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        mFileStack = new FileStack();
        setUpAdapter();
    }

    private void setUpAdapter(){
        mAdapter = new FileSystemAdapter();
        binding.fileCategoryRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fileCategoryRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
        binding.fileCategoryRvContent.setAdapter(mAdapter);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    File file = mAdapter.getItem(pos);
                    if (file.isDirectory()){
                        //?????????????????????
                        FileStack.FileSnapshot snapshot = new FileStack.FileSnapshot();
                        snapshot.filePath = binding.fileCategoryTvPath.getText().toString();
                        snapshot.files = new ArrayList<>(mAdapter.getItems());
                        snapshot.scrollOffset = binding.fileCategoryRvContent.computeVerticalScrollOffset();
                        mFileStack.push(snapshot);
                        //?????????????????????
                        toggleFileTree(file);
                    }
                    else {

                        //??????????????????????????????????????????????????????
                        String path = mAdapter.getItem(pos).getAbsolutePath();
                        if (BookService.getInstance().findBookByPath(path) != null){
                            return;
                        }
                        //????????????
                        mAdapter.setCheckedItem(pos);
                        //??????
                        if (mListener != null){
                            mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                        }
                    }
                }
        );

        binding.fileCategoryTvBackLast.setOnClickListener(v -> backLast());
    }

    public boolean backLast(){
        FileStack.FileSnapshot snapshot = mFileStack.pop();
        int oldScrollOffset = binding.fileCategoryRvContent.computeHorizontalScrollOffset();
        if (snapshot == null) return false;
        binding.fileCategoryTvPath.setText(snapshot.filePath);
        mAdapter.refreshItems(snapshot.files);
        binding.fileCategoryRvContent.scrollBy(0,snapshot.scrollOffset - oldScrollOffset);
        //??????
        if (mListener != null){
            mListener.onCategoryChanged();
        }
        return true;
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        File root = Environment.getExternalStorageDirectory();
        toggleFileTree(root);
    }

    private void toggleFileTree(File file){
        //?????????
        binding.fileCategoryTvPath.setText(getString(R.string.file_path,file.getPath()));
        //????????????
        File[] files = file.listFiles(new SimpleFileFilter());
        //?????????List
        List<File> rootFiles = Arrays.asList(files);
        //??????
        Collections.sort(rootFiles, new FileComparator());
        //??????
        mAdapter.refreshItems(rootFiles);
        //??????
        if (mListener != null){
            mListener.onCategoryChanged();
        }
    }

    @Override
    public int getFileCount(){
        int count = 0;
        Set<Map.Entry<File, Boolean>> entrys = mAdapter.getCheckMap().entrySet();
        for (Map.Entry<File, Boolean> entry:entrys){
            if (!entry.getKey().isDirectory()){
                ++count;
            }
        }
        return count;
    }

    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2){
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            }
            if (o2.isDirectory() && o1.isFile()) {
                return 1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class SimpleFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().startsWith(".")){
                return false;
            }
            //????????????????????????0
            if (pathname.isDirectory() && (pathname.list() == null || pathname.list().length == 0)) {
                return false;
            }

            /**
             * ???????????????TXT???????????????
             */
            //??????????????????,????????????txt?????????
            if (!pathname.isDirectory() &&
                    (pathname.length() == 0 || !pathname.getName().endsWith(FileUtils.SUFFIX_TXT))){
                return false;

            }
            return true;
        }
    }
}