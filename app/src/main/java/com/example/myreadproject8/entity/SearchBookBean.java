package com.example.myreadproject8.entity;

import java.util.Objects;

/**
 * @author fengyue
 * @date 2020/5/19 9:19
 */
public class SearchBookBean {
    //书名
    private String name;
    //作者
    private String author;
    //类型
    private String type;
    //简介
    private String desc;
    //状态
    private String status;
    //字数
    private String wordCount;
    //最新章节
    private String lastChapter;
    //更新时间
    private String updateTime;
    //图片地址
    private String imgUrl;

    public SearchBookBean() {
    }

    public SearchBookBean(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    //重写equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchBookBean that = (SearchBookBean) o;
        //比较作者
        if (author == null){
            return name.equals(that.name);
        }
        //比较书名
        if (name == null) return false;
        return name.equals(that.name) &&
                author.equals(that.author);
    }

    //重写hashcode方法
    @Override
    public int hashCode() {
        return Objects.hash(name, author);
    }
}
