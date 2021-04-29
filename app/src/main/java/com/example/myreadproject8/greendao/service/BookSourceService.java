package com.example.myreadproject8.greendao.service;

import android.database.Cursor;
import android.util.Log;

import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.greendao.gen.BookSourceDao;

/**
 * created by ycq on 2021/4/22 0022
 * describe：
 */
public class BookSourceService extends BaseService{


    private void addLocalSource(String eName,String name,String path){
        BookSource source = new BookSource();
        source.setSourceEName(eName);//这是内置书源标识，必填
        source.setSourceName(name);//设置书源名称
        source.setSourceGroup("内置书源");//设置书源分组
        source.setEnable(true);//设置书源可用性
        source.setSourceUrl("com.example.myreadproject8.util.net.crawler.read."+path);//这是书源完整类路径，必填
        source.setOrderNum(0);//内置书源一般设置排序为0
        GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source);//添加书源进数据库
    }

    private long checkLocalSourceNum(){


        return GreenDaoManager.getInstance().getSession().getBookSourceDao()
                .queryBuilder()
                .where(BookSourceDao.Properties.SourceGroup.eq("内置书源"))
                .count();
    }
    //初始化本地书源
    public void initLocalBookSource(){
        long count = checkLocalSourceNum();
        if(count < 1){
            addLocalSource("cangshu99","藏书99","CansShu99ReadCrawler");
            addLocalSource("miaobi","妙笔阁","MiaoBiReadCrawler");
        }else {
            Log.d("内置书源数目:",""+count);
        }
    }
}
