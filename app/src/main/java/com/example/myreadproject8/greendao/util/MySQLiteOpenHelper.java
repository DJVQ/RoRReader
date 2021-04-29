package com.example.myreadproject8.greendao.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.example.myreadproject8.greendao.gen.BookDao;
import com.example.myreadproject8.greendao.gen.BookGroupDao;
import com.example.myreadproject8.greendao.gen.BookMarkDao;
import com.example.myreadproject8.greendao.gen.BookSourceDao;
import com.example.myreadproject8.greendao.gen.ChapterDao;
import com.example.myreadproject8.greendao.gen.DaoMaster;
import com.example.myreadproject8.greendao.gen.ReplaceRuleBeanDao;
import com.example.myreadproject8.greendao.gen.SearchHistoryDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;


import org.greenrobot.greendao.database.Database;

/**
 * Created by ycq on 2021/04/03.
 *
 * @des 数据库升级
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    private Context mContext;


    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        mContext = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //noinspection unchecked
        MigrationHelper.migrate(db,
                new MigrationHelper.ReCreateAllTableListener() {
                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                },
                BookDao.class, ChapterDao.class, SearchHistoryDao.class,
                BookMarkDao.class, BookGroupDao.class, ReplaceRuleBeanDao.class,
                BookSourceDao.class
        );
    }



}
