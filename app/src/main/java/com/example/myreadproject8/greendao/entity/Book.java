package com.example.myreadproject8.greendao.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;
import java.util.Objects;

/**
 * created by ycq on 2021/4/10 0010
 * describe：书籍实体
 */
@Entity
public class Book implements Serializable {
    @Transient//手动定义serialVersionUID，避免InvalidClassException异常,不写入数据表
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;//书名
    private String chapterUrl;//书目Url（本地书籍为：本地书籍地址）
    private String infoUrl;//书目详情Url（本地书籍为：文件编码）
    private String imgUrl;//封面
    private String desc;//简介
    private String author;//作者
    private String type;//类型(本地书籍为：本地书籍)
    private String updateDate;//更新时间
    private String wordCount;//字数
    private String status;//状态
    private String newestChapterId;//最新章节id
    private String newestChapterTitle;//最新章节标题
    private String newestChapterUrl;//最新章节url
    private String historyChapterId;//上次关闭时的章节ID
    private int historyChapterNum;//上次关闭时的章节数

    private int sortCode;//排序编码
    private int noReadNum;//未读章数量
    private int chapterTotalNum;//总章节数
    private int lastReadPosition;//上次阅读到的章节的位置
    private boolean isCloseUpdate;//是否关闭更新
    private boolean isDownLoadAll = true;//是否一键缓存
    private String groupId;//分组id
    private int groupSort;//分组排序
    private String tag;
    private long lastReadTime;//上次阅读时间
    private String source;//书源
    private boolean isRead;
    @Generated(hash = 699006044)
    public Book(String id, String name, String chapterUrl, String infoUrl,
            String imgUrl, String desc, String author, String type,
            String updateDate, String wordCount, String status,
            String newestChapterId, String newestChapterTitle,
            String newestChapterUrl, String historyChapterId, int historyChapterNum,
            int sortCode, int noReadNum, int chapterTotalNum, int lastReadPosition,
            boolean isCloseUpdate, boolean isDownLoadAll, String groupId,
            int groupSort, String tag, long lastReadTime, String source,
            boolean isRead) {
        this.id = id;
        this.name = name;
        this.chapterUrl = chapterUrl;
        this.infoUrl = infoUrl;
        this.imgUrl = imgUrl;
        this.desc = desc;
        this.author = author;
        this.type = type;
        this.updateDate = updateDate;
        this.wordCount = wordCount;
        this.status = status;
        this.newestChapterId = newestChapterId;
        this.newestChapterTitle = newestChapterTitle;
        this.newestChapterUrl = newestChapterUrl;
        this.historyChapterId = historyChapterId;
        this.historyChapterNum = historyChapterNum;
        this.sortCode = sortCode;
        this.noReadNum = noReadNum;
        this.chapterTotalNum = chapterTotalNum;
        this.lastReadPosition = lastReadPosition;
        this.isCloseUpdate = isCloseUpdate;
        this.isDownLoadAll = isDownLoadAll;
        this.groupId = groupId;
        this.groupSort = groupSort;
        this.tag = tag;
        this.lastReadTime = lastReadTime;
        this.source = source;
        this.isRead = isRead;
    }
    @Generated(hash = 1839243756)
    public Book() {
    }

    @Override
    public Object clone() {
        try{
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return  gson.fromJson(json,Book.class);
        }catch(Exception ignored){}
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(name, book.name) &&
                Objects.equals(chapterUrl, book.chapterUrl) &&
                Objects.equals(author, book.author) &&
                Objects.equals(source, book.source);
    }
    public int hashCode() {
        return Objects.hash(name, chapterUrl, author, source);
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getChapterUrl() {
        return this.chapterUrl;
    }
    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }
    public String getInfoUrl() {
        return this.infoUrl;
    }
    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }
    public String getImgUrl() {
        return this.imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    public String getDesc() {
        return this.desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getAuthor() {
        return this.author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUpdateDate() {
        return this.updateDate;
    }
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
    public String getWordCount() {
        return this.wordCount;
    }
    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getNewestChapterId() {
        return this.newestChapterId;
    }
    public void setNewestChapterId(String newestChapterId) {
        this.newestChapterId = newestChapterId;
    }
    public String getNewestChapterTitle() {
        return this.newestChapterTitle;
    }
    public void setNewestChapterTitle(String newestChapterTitle) {
        this.newestChapterTitle = newestChapterTitle;
    }
    public String getNewestChapterUrl() {
        return this.newestChapterUrl;
    }
    public void setNewestChapterUrl(String newestChapterUrl) {
        this.newestChapterUrl = newestChapterUrl;
    }
    public String getHistoryChapterId() {
        return this.historyChapterId;
    }
    public void setHistoryChapterId(String historyChapterId) {
        this.historyChapterId = historyChapterId;
    }
    public int getHistoryChapterNum() {
        return this.historyChapterNum;
    }
    public void setHistoryChapterNum(int historyChapterNum) {
        this.historyChapterNum = historyChapterNum;
    }
    public int getSortCode() {
        return this.sortCode;
    }
    public void setSortCode(int sortCode) {
        this.sortCode = sortCode;
    }
    public int getNoReadNum() {
        return this.noReadNum;
    }
    public void setNoReadNum(int noReadNum) {
        this.noReadNum = noReadNum;
    }
    public int getChapterTotalNum() {
        return this.chapterTotalNum;
    }
    public void setChapterTotalNum(int chapterTotalNum) {
        this.chapterTotalNum = chapterTotalNum;
    }
    public int getLastReadPosition() {
        return this.lastReadPosition;
    }
    public void setLastReadPosition(int lastReadPosition) {
        this.lastReadPosition = lastReadPosition;
    }
    public boolean getIsCloseUpdate() {
        return this.isCloseUpdate;
    }
    public void setIsCloseUpdate(boolean isCloseUpdate) {
        this.isCloseUpdate = isCloseUpdate;
    }
    public boolean getIsDownLoadAll() {
        return this.isDownLoadAll;
    }
    public void setIsDownLoadAll(boolean isDownLoadAll) {
        this.isDownLoadAll = isDownLoadAll;
    }
    public String getGroupId() {
        return this.groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public int getGroupSort() {
        return this.groupSort;
    }
    public void setGroupSort(int groupSort) {
        this.groupSort = groupSort;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public long getLastReadTime() {
        return this.lastReadTime;
    }
    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
    public String getSource() {
        return this.source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public boolean getIsRead() {
        return this.isRead;
    }
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }


}
