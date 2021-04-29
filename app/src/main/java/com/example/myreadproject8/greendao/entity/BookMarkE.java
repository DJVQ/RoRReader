package com.example.myreadproject8.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * created by ycq on 2021/4/27 0027
 * describe：
 */
@Entity
public class BookMarkE {
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    String Id;

    //书Id
    @NotNull
    String markEBookId;

    //标记内容
    String markEContent;

    //标注:"markE";颜色:"color";下划线:"line"
    private String markKind;
    @NotNull
    private int bookMarkEChapterNum;//标注章节位置
    private int bookMarkEPagePosition;//标注页面位置



    //颜色种类
    private String colorKind;


    private int markFirstLinePosition;
    private int markLastLinePosition;
    private int markFirstCharPosition;
    private int markLastCharPosition;






    private int Number;//标注序号






    @Generated(hash = 485630698)
    public BookMarkE(String Id, @NotNull String markEBookId, String markEContent,
            String markKind, int bookMarkEChapterNum, int bookMarkEPagePosition,
            String colorKind, int markFirstLinePosition, int markLastLinePosition,
            int markFirstCharPosition, int markLastCharPosition, int Number) {
        this.Id = Id;
        this.markEBookId = markEBookId;
        this.markEContent = markEContent;
        this.markKind = markKind;
        this.bookMarkEChapterNum = bookMarkEChapterNum;
        this.bookMarkEPagePosition = bookMarkEPagePosition;
        this.colorKind = colorKind;
        this.markFirstLinePosition = markFirstLinePosition;
        this.markLastLinePosition = markLastLinePosition;
        this.markFirstCharPosition = markFirstCharPosition;
        this.markLastCharPosition = markLastCharPosition;
        this.Number = Number;
    }






    @Generated(hash = 1024588231)
    public BookMarkE() {
    }






    public String getId() {
        return this.Id;
    }






    public void setId(String Id) {
        this.Id = Id;
    }






    public String getMarkEBookId() {
        return this.markEBookId;
    }






    public void setMarkEBookId(String markEBookId) {
        this.markEBookId = markEBookId;
    }






    public String getMarkEContent() {
        return this.markEContent;
    }






    public void setMarkEContent(String markEContent) {
        this.markEContent = markEContent;
    }






    public String getMarkKind() {
        return this.markKind;
    }






    public void setMarkKind(String markKind) {
        this.markKind = markKind;
    }






    public int getBookMarkEChapterNum() {
        return this.bookMarkEChapterNum;
    }






    public void setBookMarkEChapterNum(int bookMarkEChapterNum) {
        this.bookMarkEChapterNum = bookMarkEChapterNum;
    }






    public int getBookMarkEPagePosition() {
        return this.bookMarkEPagePosition;
    }






    public void setBookMarkEPagePosition(int bookMarkEPagePosition) {
        this.bookMarkEPagePosition = bookMarkEPagePosition;
    }






    public String getColorKind() {
        return this.colorKind;
    }






    public void setColorKind(String colorKind) {
        this.colorKind = colorKind;
    }






    public int getMarkFirstLinePosition() {
        return this.markFirstLinePosition;
    }






    public void setMarkFirstLinePosition(int markFirstLinePosition) {
        this.markFirstLinePosition = markFirstLinePosition;
    }






    public int getMarkLastLinePosition() {
        return this.markLastLinePosition;
    }






    public void setMarkLastLinePosition(int markLastLinePosition) {
        this.markLastLinePosition = markLastLinePosition;
    }






    public int getMarkFirstCharPosition() {
        return this.markFirstCharPosition;
    }






    public void setMarkFirstCharPosition(int markFirstCharPosition) {
        this.markFirstCharPosition = markFirstCharPosition;
    }






    public int getMarkLastCharPosition() {
        return this.markLastCharPosition;
    }






    public void setMarkLastCharPosition(int markLastCharPosition) {
        this.markLastCharPosition = markLastCharPosition;
    }






    public int getNumber() {
        return this.Number;
    }






    public void setNumber(int Number) {
        this.Number = Number;
    }



    
    
}
