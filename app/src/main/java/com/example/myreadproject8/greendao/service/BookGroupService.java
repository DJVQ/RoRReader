package com.example.myreadproject8.greendao.service;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.BookGroup;
import com.example.myreadproject8.greendao.gen.BookGroupDao;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;

import java.util.List;



/**
 * @author fengyue
 * @date 2020/9/26 12:14
 */
public class BookGroupService extends BaseService{
    private static volatile BookGroupService sInstance;

    public static BookGroupService getInstance() {
        if (sInstance == null){
            synchronized (BookGroupService.class){
                if (sInstance == null){
                    sInstance = new BookGroupService();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取所有书籍分组
     * @return
     */
    public List<BookGroup> getAllGroups(){
        return GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .orderAsc(BookGroupDao.Properties.Num)
                .list();
    }

    /**
     * 通过I的获取书籍分组
     * @param groupId
     * @return
     */
    public BookGroup getGroupById(String groupId){
        return GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .where(BookGroupDao.Properties.Id.eq(groupId))
                .unique();
    }

    /**
     * 添加书籍分组
     * @param bookGroup
     */
    public void addBookGroup(BookGroup bookGroup){
        bookGroup.setNum(countBookGroup());
        bookGroup.setId(StringHelper.getStringRandom(25));
        addEntity(bookGroup);
    }

    /**
     * 删除书籍分组
     * @param bookGroup
     */
    public void deleteBookGroup(BookGroup bookGroup){
        deleteEntity(bookGroup);
    }

    public void deleteGroupById(String id){
        GreenDaoManager.getInstance().getSession().getBookGroupDao().deleteByKey(id);
    }

    public void createPrivateGroup(){
        BookGroup bookGroup = new BookGroup();
        bookGroup.setName("私密书架");
        addBookGroup(bookGroup);
        SharedPreUtils.getInstance().putString("privateGroupId", bookGroup.getId());
    }

    public void deletePrivateGroup(){
        String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
        deleteGroupById(privateGroupId);
        BookService.getInstance().deleteBooksByGroupId(privateGroupId);
    }

    /**
     * 当前是否为私密书架
     * @return
     */
    public boolean curGroupIsPrivate(){
        String curBookGroupId = SharedPreUtils.getInstance().getString(App.getMContext().getString(R.string.curBookGroupId), "");
        String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
        return !StringHelper.isEmpty(curBookGroupId) && curBookGroupId.equals(privateGroupId);
    }

    private int countBookGroup(){
        return (int) GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .count();
    }

}
