package com.example.myreadproject8.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

/**
 * created by ycq on 2021/4/24 0024
 * describe：背诵区内容
 */
@Entity
public class Recite implements Serializable {
    @Transient//手动定义serialVersionUID，避免InvalidClassException异常,不写入数据表
    private static final long serialVersionUID = 1L;
    @Id
    Long id;//id

    String reciteT;

    String reciteContent;

    String Author;

    String bookName;

    long addDate;

    //背诵次数
    int reciteNum;
    boolean firstRecite;
    boolean secondRecite;
    boolean thirdRecite;
    boolean fourthRecite;
    boolean fifthRecite;
    boolean sixthRecite;
    boolean seventhRecite;
    long sortCode;

    int reciteNums;

    int reciteIndex;

    @Generated(hash = 1307617523)
    public Recite(Long id, String reciteT, String reciteContent, String Author,
            String bookName, long addDate, int reciteNum, boolean firstRecite,
            boolean secondRecite, boolean thirdRecite, boolean fourthRecite,
            boolean fifthRecite, boolean sixthRecite, boolean seventhRecite,
            long sortCode, int reciteNums, int reciteIndex) {
        this.id = id;
        this.reciteT = reciteT;
        this.reciteContent = reciteContent;
        this.Author = Author;
        this.bookName = bookName;
        this.addDate = addDate;
        this.reciteNum = reciteNum;
        this.firstRecite = firstRecite;
        this.secondRecite = secondRecite;
        this.thirdRecite = thirdRecite;
        this.fourthRecite = fourthRecite;
        this.fifthRecite = fifthRecite;
        this.sixthRecite = sixthRecite;
        this.seventhRecite = seventhRecite;
        this.sortCode = sortCode;
        this.reciteNums = reciteNums;
        this.reciteIndex = reciteIndex;
    }

    @Generated(hash = 278849458)
    public Recite() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReciteT() {
        return this.reciteT;
    }

    public void setReciteT(String reciteT) {
        this.reciteT = reciteT;
    }

    public String getReciteContent() {
        return this.reciteContent;
    }

    public void setReciteContent(String reciteContent) {
        this.reciteContent = reciteContent;
    }

    public long getAddDate() {
        return this.addDate;
    }

    public void setAddDate(long addDate) {
        this.addDate = addDate;
    }

    public int getReciteNum() {
        return this.reciteNum;
    }

    public void setReciteNum(int reciteNum) {
        this.reciteNum = reciteNum;
    }

    public boolean getFirstRecite() {
        return this.firstRecite;
    }

    public void setFirstRecite(boolean firstRecite) {
        this.firstRecite = firstRecite;
    }

    public boolean getSecondRecite() {
        return this.secondRecite;
    }

    public void setSecondRecite(boolean secondRecite) {
        this.secondRecite = secondRecite;
    }

    public boolean getThirdRecite() {
        return this.thirdRecite;
    }

    public void setThirdRecite(boolean thirdRecite) {
        this.thirdRecite = thirdRecite;
    }

    public boolean getFourthRecite() {
        return this.fourthRecite;
    }

    public void setFourthRecite(boolean fourthRecite) {
        this.fourthRecite = fourthRecite;
    }

    public boolean getFifthRecite() {
        return this.fifthRecite;
    }

    public void setFifthRecite(boolean fifthRecite) {
        this.fifthRecite = fifthRecite;
    }

    public long getSortCode() {
        return this.sortCode;
    }

    public void setSortCode(long sortCode) {
        this.sortCode = sortCode;
    }

    public int getReciteNums() {
        return this.reciteNums;
    }

    public void setReciteNums(int reciteNums) {
        this.reciteNums = reciteNums;
    }

    public boolean getSixthRecite() {
        return this.sixthRecite;
    }

    public void setSixthRecite(boolean sixthRecite) {
        this.sixthRecite = sixthRecite;
    }

    public boolean getSeventhRecite() {
        return this.seventhRecite;
    }

    public void setSeventhRecite(boolean seventhRecite) {
        this.seventhRecite = seventhRecite;
    }

    public String getAuthor() {
        return this.Author;
    }

    public void setAuthor(String Author) {
        this.Author = Author;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getReciteIndex() {
        return this.reciteIndex;
    }

    public void setReciteIndex(int reciteIndex) {
        this.reciteIndex = reciteIndex;
    }

}
