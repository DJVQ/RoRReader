package com.example.myreadproject8.greendao.service;

import android.database.Cursor;

import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.gen.DaoSession;


/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public class BaseService {

    /**
     * description:增删改查
     */
    public void addEntity(Object entity){
        DaoSession daoSession = GreenDaoManager.getInstance().getSession();
        daoSession.insert(entity);
    }

    public void updateEntity(Object entity){
        DaoSession daoSession = GreenDaoManager.getInstance().getSession();
        daoSession.update(entity);
    }

    public void deleteEntity(Object entity){
        DaoSession daoSession = GreenDaoManager.getInstance().getSession();
        daoSession.delete(entity);
    }

    /**
     * description:通过sql查找
     */
    public Cursor selectBySql(String sql, String[] selectionArgs){

        Cursor cursor = null;
        try{
            DaoSession daoSession = GreenDaoManager.getInstance().getSession();
            cursor = daoSession.getDatabase().rawQuery(sql,selectionArgs);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return  cursor;
    }

    /**
     * description:执行SQL进行增删改
     */
    public void rawQuery(String sql, String[] selectionArgs){
        DaoSession daoSession = GreenDaoManager.getInstance().getSession();
        Cursor cursor = daoSession.getDatabase().rawQuery(sql,selectionArgs);
    }
}
