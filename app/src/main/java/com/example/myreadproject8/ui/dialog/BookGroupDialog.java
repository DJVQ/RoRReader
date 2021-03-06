package com.example.myreadproject8.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.BookGroup;
import com.example.myreadproject8.greendao.service.BookGroupService;
import com.example.myreadproject8.greendao.service.BookService;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;



/**
 * @author fengyue
 * @date 2021/1/8 23:58
 */
public class BookGroupDialog {
    private ArrayList<BookGroup> mBookGroups = new ArrayList<>();//书籍分组
    private CharSequence[] mGroupNames;//书籍分组名称
    private final BookGroupService mBookGroupService;
    private Handler mHandler = new Handler();
    private Context mContext;

    public BookGroupDialog(Context context) {
        this.mBookGroupService = BookGroupService.getInstance();
        mContext = context;
    }

    public ArrayList<BookGroup> getmBookGroups() {
        return mBookGroups;
    }

    public CharSequence[] getmGroupNames() {
        return mGroupNames;
    }

    //初始化书籍分组
    public void initBookGroups(boolean isAdd) {
        mBookGroups.clear();
        mBookGroups.addAll(mBookGroupService.getAllGroups());
        boolean openPrivate = SharedPreUtils.getInstance().getBoolean("openPrivate");
        if (openPrivate) {
            String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
            mBookGroups.remove(BookGroupService.getInstance().getGroupById(privateGroupId));
        }
        mGroupNames = new CharSequence[isAdd ? mBookGroups.size() + 1 : mBookGroups.size()];
        for (int i = 0; i < mBookGroups.size(); i++) {
            BookGroup bookGroup = mBookGroups.get(i);
            String groupName = bookGroup.getName();
//            mGroupNames[i] = groupName.getBytes().length > 20 ? groupName.substring(0, 8) + "···" : groupName;
            mGroupNames[i] = groupName;
        }
        if (isAdd) {
            mGroupNames[mBookGroups.size()] = "添加分组";
        }
    }


    /**
     * 加入分组
     * @param book
     */
    public void addGroup(Book book, OnGroup onGroup){
        List<Book> books = new ArrayList<>();
        books.add(book);
        addGroup(books, onGroup);
    }

    /**
     * 加入批量分组
     * @param mSelectBooks
     * @param onGroup
     */
    public void addGroup(List<Book> mSelectBooks, OnGroup onGroup){
        initBookGroups(true);
        showSelectGroupDia((dialog, which) -> {
            if (which < mBookGroups.size()) {
                BookGroup bookGroup = mBookGroups.get(which);
                for (Book book : mSelectBooks) {
                    if (!bookGroup.getId().equals(book.getGroupId())) {
                        book.setGroupId(bookGroup.getId());
                        book.setGroupSort(0);
                    }
                }
                BookService.getInstance().updateBooks(mSelectBooks);
                ToastUtils.showSuccess("成功将《" + mSelectBooks.get(0).getName() + "》"
                        + (mSelectBooks.size() > 1 ? "等" : "")
                        + "加入[" + bookGroup.getName() + "]分组");
                if (onGroup != null) onGroup.change();
            } else if (which == mBookGroups.size()) {
                showAddOrRenameGroupDia(false, true, 0, onGroup);
            }
        });
    }
    /**
     * 添加/重命名分组对话框
     */
    public void showAddOrRenameGroupDia(boolean isRename, boolean isAddGroup, int groupNum, OnGroup onGroup){
        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_dialog, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_lay);
        int maxLen = 20;
        textInputLayout.setCounterMaxLength(maxLen);
        EditText editText = textInputLayout.getEditText();
        editText.setHint("请输入分组名");
        BookGroup bookGroup = !isRename ? new BookGroup() : mBookGroups.get(groupNum);
        String oldName = bookGroup.getName();
        if (isRename) {
            editText.setText(oldName);
        }
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mHandler.postDelayed(() ->{
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        }, 220);
        AlertDialog newGroupDia = MyAlertDialog.build(mContext)
                .setTitle(!isRename ? "新建分组" : "重命名分组")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("确认", null)
                .setNegativeButton("取消", null)
                .show();
        Button posBtn = newGroupDia.getButton(AlertDialog.BUTTON_POSITIVE);
        posBtn.setEnabled(false);
        posBtn.setOnClickListener(v1 -> {
            CharSequence newGroupName = editText.getText().toString();
            for (CharSequence oldGroupName : mGroupNames){
                if (oldGroupName.equals(newGroupName)){
                    ToastUtils.showWarring("分组[" + newGroupName + "]已存在，无法" + (!isRename ? "添加！" : "重命名！"));
                    return;
                }
            }
            bookGroup.setName(newGroupName.toString());
            if (!isRename) {
                mBookGroupService.addBookGroup(bookGroup);
            }else {
                mBookGroupService.updateEntity(bookGroup);
                SharedPreUtils spu = SharedPreUtils.getInstance();
                if (spu.getString(mContext.getString(R.string.curBookGroupName), "").equals(oldName)){
                    spu.putString(mContext.getString(R.string.curBookGroupName), newGroupName.toString());
                    if (onGroup != null) onGroup.change();
                }
            }
            ToastUtils.showSuccess("成功" +
                    (!isRename ? "添加分组[" : "成功将[" + oldName + "]重命名为[")
                    + bookGroup.getName() + "]");
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            newGroupDia.dismiss();
            if (isAddGroup){
                if (onGroup != null) onGroup.addGroup();
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editText.getText().toString();
                if (editText.getText().length() > 0 && editText.getText().length() <= maxLen && !text.equals(oldName)) {
                    posBtn.setEnabled(true);
                } else {
                    posBtn.setEnabled(false);
                }
            }
        });
    }

    /**
     * 删除分组对话框
     */
    public void showDeleteGroupDia(OnGroup onGroup) {
        boolean[] checkedItems = new boolean[mGroupNames.length];
        new MultiChoiceDialog().create(mContext, "删除分组", mGroupNames
                , checkedItems, 0, (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            mBookGroupService.deleteEntity(mBookGroups.get(i));
                            sb.append(mBookGroups.get(i).getName()).append("、");
                        }
                    }
                    if (sb.length() > 0){
                        sb.deleteCharAt(sb.lastIndexOf("、"));
                    }
                    SharedPreUtils spu = SharedPreUtils.getInstance();
                    if (mBookGroupService.getGroupById(spu.getString(mContext.getString(R.string.curBookGroupId), "")) == null){
                        spu.putString(mContext.getString(R.string.curBookGroupId), "");
                        spu.putString(mContext.getString(R.string.curBookGroupName), "");
                        onGroup.change();
                    }
                    ToastUtils.showSuccess("分组[" + sb.toString() + "]删除成功！");
                }, null, null);
    }
    //显示选择书籍对话框
    public void showSelectGroupDia(DialogInterface.OnClickListener onClickListener){
        MyAlertDialog.build(mContext)
                .setTitle("选择分组")
                .setItems(mGroupNames, onClickListener)
                .setCancelable(false)
                .setPositiveButton("取消", null)
                .show();
    }

    public abstract static class OnGroup{
        public abstract void change();

        public void addGroup() {

        }
    }
}
