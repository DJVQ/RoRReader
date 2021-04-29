package com.example.myreadproject8.entity;

import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.service.BookService;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ycq on 2021/4/11 0011
 * describe：暂存book实体
 */
public class BookEntity {
    private String bookName;
    private String author;
    private int ImageId;

    public BookEntity(String bookName, String author,int imageId){
        this.bookName = bookName;
        this.author = author;
        this.ImageId = imageId;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthor() {
        return author;
    }

    public int getImageId() {
        return ImageId;
    }
}
