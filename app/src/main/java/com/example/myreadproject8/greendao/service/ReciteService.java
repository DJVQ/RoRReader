package com.example.myreadproject8.greendao.service;

import android.provider.CalendarContract;

import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.Book;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.gen.ReciteDao;
import com.example.myreadproject8.util.string.StringHelper;

import java.util.List;

/**
 * created by ycq on 2021/4/24 0024
 * describe：背诵区服务类
 */
public class ReciteService extends BaseService{
    private static volatile ReciteService sInstance;

    public static ReciteService getInstance(){
        if(sInstance == null){
            synchronized (ReciteService.class){
                if(sInstance != null){
                    sInstance = new ReciteService();
                }
            }
        }
        return sInstance;
    }

    public void addRecite(Recite recite){
        recite.setSortCode(1);
        recite.setFirstRecite(false);
        recite.setSecondRecite(false);
        recite.setThirdRecite(false);
        recite.setFourthRecite(false);
        recite.setFifthRecite(false);
        recite.setSixthRecite(false);
        recite.setFifthRecite(false);
        if(recite.getAuthor()==null){
            recite.setAuthor("佚名");
        }
        if(recite.getBookName()==null){
            recite.setBookName("");
        }
        recite.setReciteIndex(0);
        recite.setReciteNums(0);
        recite.setId(System.currentTimeMillis());
        recite.setAddDate(System.currentTimeMillis());
        addEntity(recite);
    }

    public List<Recite> findAllRecite(){

        return  GreenDaoManager.getInstance().getSession().getReciteDao()
                .queryBuilder()
                .orderAsc(ReciteDao.Properties.Id)
                .list();
    }



    public Recite findReciteByT(String getReciteT){
        try {
            return GreenDaoManager.getInstance().getSession().getReciteDao()
                    .queryBuilder()
                    .where(ReciteDao.Properties.ReciteT.eq(getReciteT))
                    .unique();
        }catch (Exception e){
            e.printStackTrace();
            return GreenDaoManager.getInstance().getSession().getReciteDao()
                    .queryBuilder()
                    .where(ReciteDao.Properties.ReciteT.eq(getReciteT))
                    .list().get(0);
        }
    }

    public void deleteRecite(Recite recite){
        try{
            deleteEntity(recite);
        }catch (Exception ignored){

        }
    }

    public void resetReciteNum(Recite recite,int num){
        recite.setFirstRecite(false);
        recite.setSecondRecite(false);
        recite.setThirdRecite(false);
        recite.setFourthRecite(false);
        recite.setFifthRecite(false);
        recite.setSixthRecite(false);
        recite.setFifthRecite(false);
        recite.setReciteNum(num);
        recite.setReciteIndex(0);
        recite.setAddDate(System.currentTimeMillis());
        updateEntity(recite);
    }

}
