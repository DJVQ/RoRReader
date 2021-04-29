package com.example.myreadproject8.greendao.service;

import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.BookMark;
import com.example.myreadproject8.greendao.entity.BookMarkE;
import com.example.myreadproject8.greendao.gen.BookMarkDao;
import com.example.myreadproject8.greendao.gen.BookMarkEDao;
import com.example.myreadproject8.util.string.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ycq on 2021/4/27 0027
 * describe：
 */
public class BookMarkEService extends BaseService {
    private static volatile BookMarkService sInstance;

    public static BookMarkService getInstance() {
        if (sInstance == null){
            synchronized (BookMarkService.class){
                if (sInstance == null){
                    sInstance = new BookMarkService();
                }
            }
        }
        return sInstance;
    }


    /**
     * 获取书的所有标注
     *
     * @return
     */
    public List<BookMarkE> findBookAllBookMarkByBookId(String bookId) {
        if (bookId == null) {
            return new ArrayList<>();
        }
        return GreenDaoManager.getInstance().getSession().getBookMarkEDao()
                .queryBuilder()
                .where(BookMarkEDao.Properties.MarkEBookId.eq(bookId))
                .orderAsc(BookMarkEDao.Properties.Number)
                .list();
    }


    /**
     * 添加标注
     * @param bookMarkE
     */
    public void addBookMarkE(BookMarkE bookMarkE) {
        bookMarkE.setId(StringHelper.getStringRandom(25));
        bookMarkE.setNumber(countBookMarkETotalNumByBookId(bookMarkE.getMarkEBookId()) + 1);
        addEntity(bookMarkE);
    }

    /**
     * 通过id查询书籍标注总数
     * @return
     */
    public int countBookMarkETotalNumByBookId(String bookId){
        return (int) GreenDaoManager.getInstance().getSession().getBookMarkEDao()
                .queryBuilder()
                .where(BookMarkEDao.Properties.MarkEBookId.eq(bookId))
                .count();
    }

    /**
     * 删除标注
     * @param bookMarkE
     */
    public void deleteBookMark(BookMarkE bookMarkE){
        deleteEntity(bookMarkE);
    }



    public List<BookMarkE> isMarkLists(String mBookId, int mChapter, int mPage, int mLine, int mChar){//寻找包含当前标记的标注集合
        List<BookMarkE> mMarkEList = GreenDaoManager.getInstance().getSession().getBookMarkEDao()
                .queryBuilder()
                .where(BookMarkEDao.Properties.MarkEBookId.eq(mBookId),BookMarkEDao.Properties.BookMarkEChapterNum.eq(mChapter),BookMarkEDao.Properties.BookMarkEPagePosition.eq(mPage)//书名,章节序号,页面序号符合要求
                ,BookMarkEDao.Properties.MarkFirstLinePosition.le(mLine), BookMarkEDao.Properties.MarkLastLinePosition.ge(mLine))
                .list();
        List<BookMarkE> mMarkEList2 = new ArrayList<>();
        if(mMarkEList!=null) {
            for (int i = 0; i < mMarkEList.size();i++) {
                if(mMarkEList.get(i).getMarkLastLinePosition() - mMarkEList.get(i).getMarkFirstLinePosition() > 1) {//三行以上
                    if (mLine - mMarkEList.get(i).getMarkFirstLinePosition() == 0) {
                        if(mChar >= mMarkEList.get(i).getMarkFirstCharPosition())
                            mMarkEList2.add(mMarkEList.get(i));
                    } else if (mLine - mMarkEList.get(i).getMarkLastLinePosition() == 0) {
                        if(mChar <= mMarkEList.get(i).getMarkLastCharPosition())
                            mMarkEList2.add(mMarkEList.get(i));
                    }else {
                        mMarkEList2.add(mMarkEList.get(i));
                    }
                }else if (mMarkEList.get(i).getMarkLastLinePosition() - mMarkEList.get(i).getMarkFirstLinePosition() == 1){//两行
                    if (mLine - mMarkEList.get(i).getMarkFirstLinePosition() == 0) {
                        if(mChar >= mMarkEList.get(i).getMarkFirstCharPosition())
                            mMarkEList2.add(mMarkEList.get(i));
                    } else if (mLine - mMarkEList.get(i).getMarkLastLinePosition() == 0) {
                        if(mChar <= mMarkEList.get(i).getMarkLastCharPosition())
                            mMarkEList2.add(mMarkEList.get(i));
                    }
                }else {//一行
                    if(mChar >= mMarkEList.get(i).getMarkFirstCharPosition() && mChar <= mMarkEList.get(i).getMarkLastCharPosition())
                        mMarkEList2.add(mMarkEList.get(i));
                }
            }
        }
        return mMarkEList2;
    }

    public void deleteSelectMarkE(String mBookId, int mChapter, int mPage, int mFirstLine, int mLastLine){
        List<BookMarkE> mMarkEList = GreenDaoManager.getInstance().getSession().getBookMarkEDao()
                .queryBuilder()
                .where(BookMarkEDao.Properties.MarkEBookId.eq(mBookId),BookMarkEDao.Properties.BookMarkEChapterNum.eq(mChapter),BookMarkEDao.Properties.BookMarkEPagePosition.eq(mPage)//书名,章节序号,页面序号符合要求
                        ,BookMarkEDao.Properties.MarkFirstLinePosition.le(mFirstLine), BookMarkEDao.Properties.MarkLastLinePosition.ge(mLastLine))
                .list();
        if(mMarkEList!=null) {
            for (int i = 0;i<mMarkEList.size() ;i++){
                deleteEntity(mMarkEList.get(i));
            }
        }
    }


}
