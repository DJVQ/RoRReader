package com.example.myreadproject8.greendao;


import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.greendao.gen.DaoMaster;
import com.example.myreadproject8.greendao.gen.DaoSession;
import com.example.myreadproject8.greendao.util.MySQLiteOpenHelper;

/*
 * created by ycq on 2021/4/3
 * describe：GreenDao管理工具
 */
public class GreenDaoManager {
    private static GreenDaoManager instance;
    private static DaoMaster daoMaster;
    private DaoSession mDaoSession;

    private static MySQLiteOpenHelper mySQLiteOpenHelper;

    public static GreenDaoManager getInstance() {
        if (instance == null) {
            instance = new GreenDaoManager();
        }
        return instance;
    }

    public GreenDaoManager(){

        mySQLiteOpenHelper = new MySQLiteOpenHelper(App.getCurApplication(), "read" , null);
        daoMaster = new DaoMaster(mySQLiteOpenHelper.getWritableDatabase());
        mDaoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return getInstance().mDaoSession;
    }

    public DaoSession getSession(){
        return mDaoSession;
    }

}
