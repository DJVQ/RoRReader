package com.example.myreadproject8.widget.page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouas666 on 18-1-23.
 * 书籍chapter
 */

public class TxtChapter {

    private int position;//章节序号
    private List<TxtPage> txtPageList = new ArrayList<>();//所有页面
    private List<Integer> txtPageLengthList = new ArrayList<>();//章节页面长度列表
    private List<Integer> paragraphLengthList = new ArrayList<>();//章节段落长度列表
    private Status status = Status.LOADING;//章节状态,默认为加载中
    private String msg;

    TxtChapter(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    List<TxtPage> getTxtPageList() {
        return txtPageList;
    }

    void addPage(TxtPage txtPage) {
        txtPageList.add(txtPage);
    }

    int getPageSize() {
        return txtPageList.size();
    }

    TxtPage getPage(int page) {//返回第page页
        if (!txtPageList.isEmpty()) {
            return txtPageList.get(Math.max(0, Math.min(page, txtPageList.size() - 1)));
        }
        return null;
    }

    Status getStatus() {
        return status;
    }

    void setStatus(Status mStatus) {
        this.status = mStatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    int getPageLength(int position) {
        if (txtPageLengthList != null && position >= 0 && position < txtPageLengthList.size()) {
            return txtPageLengthList.get(position);
        }
        return -1;
    }

    void addTxtPageLength(int length) {
        txtPageLengthList.add(length);
    }

    List<Integer> getTxtPageLengthList() {
        return txtPageLengthList;
    }

    List<Integer> getParagraphLengthList() {
        return paragraphLengthList;
    }

    void addParagraphLength(int length) {
        paragraphLengthList.add(length);
    }

    int getParagraphIndex(int length) {
        for (int i = 0; i < paragraphLengthList.size(); i++) {
            if ((i == 0 || paragraphLengthList.get(i - 1) < length) && length <= paragraphLengthList.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public enum Status {
        LOADING, FINISH, ERROR, EMPTY, CATEGORY_EMPTY, CHANGE_SOURCE,
    }

}
