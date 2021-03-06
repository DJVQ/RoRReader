package com.example.myreadproject8.ui.adapter.filesystem;



import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.ui.adapter.base.BaseListAdapter;
import com.example.myreadproject8.ui.adapter.holder.file.FileHolder;
import com.example.myreadproject8.ui.adapter.holder.IViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public class FileSystemAdapter extends BaseListAdapter<File> {

    //记录item是否被选中的Map
    private HashMap<File, Boolean> mCheckMap = new HashMap<>();

    private int mCheckedCount = 0;

    private BookService mBookService = new BookService();

    @Override
    protected IViewHolder<File> createViewHolder(int viewType) {
        return new FileHolder(mCheckMap);
    }

    @Override
    public void refreshItems(List<File> list) {
        mCheckMap.clear();
        for(File file : list){
            mCheckMap.put(file,false);
        }
        super.refreshItems(list);
    }

    @Override
    public void addItem(File value) {
        mCheckMap.put(value,false);
        super.addItem(value);
    }

    @Override
    public void addItem(int index, File value) {
        mCheckMap.put(value, false);
        super.addItem(index, value);
    }

    @Override
    public void addItems(List<File> values) {
        for(File file : values){
            mCheckMap.put(file, false);
        }
        super.addItems(values);
    }

    @Override
    public void removeItem(File value) {
        mCheckMap.remove(value);
        super.removeItem(value);
    }

    @Override
    public void removeItems(List<File> value) {
        //删除在HashMap中的文件
        for (File file : value){
            mCheckMap.remove(file);
            //因为，能够被移除的文件，肯定是选中的
            --mCheckedCount;
        }
        //删除列表中的文件
        super.removeItems(value);
    }

    //设置点击切换
    public void setCheckedItem(int pos){
        File file = getItem(pos);
        if (isFileLoaded(file.getAbsolutePath())) return;

        boolean isSelected = mCheckMap.get(file);
        if (isSelected){
            mCheckMap.put(file, false);
            --mCheckedCount;
        }
        else{
            mCheckMap.put(file, true);
            ++mCheckedCount;
        }
        notifyDataSetChanged();
    }

    public void setCheckedAll(boolean isChecked){
        Set<Map.Entry<File, Boolean>> entrys = mCheckMap.entrySet();
        mCheckedCount = 0;
        for (Map.Entry<File, Boolean> entry:entrys){
            //必须是文件，必须没有被收藏
            if (entry.getKey().isFile()){
                if (!isFileLoaded(entry.getKey().getAbsolutePath())) {
                    entry.setValue(isChecked);
                }else {
                    entry.setValue(false);
                }
                //如果选中，则增加点击的数量
                if (isChecked){
                    ++mCheckedCount;
                }
            }
        }
        //通知RecyclerView更新
        notifyDataSetChanged();
    }

    private boolean isFileLoaded(String path){
        //如果是已加载的文件，则点击事件无效。
        if (mBookService.findBookByPath(path) != null){
            return true;
        }
        return false;
    }

    public int getCheckableCount(){
        List<File> files = getItems();
        int count = 0;
        for (File file : files){
            if (!isFileLoaded(file.getAbsolutePath()) && file.isFile())
                ++count;
        }
        return count;
    }

    public boolean getItemIsChecked(int pos){
        File file = getItem(pos);
        return mCheckMap.get(file);
    }

    public List<File> getCheckedFiles(){
        List<File> files = new ArrayList<>();
        Set<Map.Entry<File, Boolean>> entrys = mCheckMap.entrySet();
        for (Map.Entry<File, Boolean> entry:entrys){
            if (entry.getValue()){
                files.add(entry.getKey());
            }
        }
        return files;
    }

    public int getCheckedCount(){
        return mCheckedCount;
    }

    public HashMap<File, Boolean> getCheckMap(){
        return mCheckMap;
    }
}
