package com.example.myreadproject8.greendao.service;

import android.database.Cursor;

import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.Chapter;
import com.example.myreadproject8.greendao.gen.ChapterDao;
import com.example.myreadproject8.util.file.FileUtils;
import com.example.myreadproject8.util.file.IOUtils;
import com.example.myreadproject8.util.string.StringHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;




public class ChapterService extends BaseService {
    private static volatile ChapterService sInstance;

    public static ChapterService getInstance() {
        if (sInstance == null){
            synchronized (ChapterService.class){
                if (sInstance == null){
                    sInstance = new ChapterService();
                }
            }
        }
        return sInstance;
    }
    private List<Chapter> findChapters(String sql, String[] selectionArgs) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        try {
            Cursor cursor = selectBySql(sql, selectionArgs);
            if (cursor == null) return chapters;
            while (cursor.moveToNext()) {
                Chapter chapter = new Chapter();
                chapter.setId(cursor.getString(0));
                chapter.setBookId(cursor.getString(1));
                chapter.setNumber(cursor.getInt(2));
                chapter.setTitle(cursor.getString(3));
                chapter.setUrl(cursor.getString(4));
                chapter.setContent(cursor.getString(5));
                chapter.setStart(cursor.getInt(6));
                chapter.setEnd(cursor.getInt(7));
                chapters.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return chapters;
        }
        return chapters;
    }

    /**
     * ??????ID?????????
     *
     * @param id
     * @return
     */
    public Chapter getChapterById(String id) {
        ChapterDao chapterDao = GreenDaoManager.getInstance().getSession().getChapterDao();
        return chapterDao.load(id);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public List<Chapter> findBookAllChapterByBookId(String bookId) {

        if (StringHelper.isEmpty(bookId)) return new ArrayList<>();


        String sql = "select * from chapter where book_id = ? order by number";

        return findChapters(sql, new String[]{bookId});
    }

    /**
     * ????????????
     *
     * @param chapter
     */
    public void addChapter(Chapter chapter, String content) {
        chapter.setId(StringHelper.getStringRandom(25));
        addEntity(chapter);
        saveChapterCacheFile(chapter, content);
    }

    /**
     * ????????????
     *
     * @param bookId
     * @param title
     * @return
     */
    public Chapter findChapterByBookIdAndTitle(String bookId, String title) {
        Chapter chapter = null;
        try {
            String sql = "select id from chapter where book_id = ? and title = ?";
            Cursor cursor = selectBySql(sql, new String[]{bookId, title});
            if (cursor == null) return null;
            if (cursor.moveToNext()) {
                String id = cursor.getString(0);
                chapter = getChapterById(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapter;
    }

    /**
     * ????????????????????????
     *
     * @param bookId
     */
    public void deleteBookALLChapterById(String bookId) {
        GreenDaoManager.getInstance().getSession().getChapterDao()
                .queryBuilder()
                .where(ChapterDao.Properties.BookId.eq(bookId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        deleteAllChapterCacheFile(bookId);
    }

    /**
     * ????????????
     */
    public void updateChapter(Chapter chapter) {
        ChapterDao chapterDao = GreenDaoManager.getInstance().getSession().getChapterDao();
        chapterDao.update(chapter);
    }

    /**
     * ??????????????????
     *
     * @param bookId
     * @param from
     * @param to
     * @return
     */
    public List<Chapter> findChapter(String bookId, int from, int to) {
        String sql = "select * from " +
                "(select row_number()over(order by number)rownumber,* from chapter where bookId = ? order by number)a " +
                "where rownumber >= ? and rownumber <= ?";

        return findChapters(sql, new String[]{bookId, String.valueOf(from), String.valueOf(to)});
    }


    /**
     * ?????????????????????
     *
     * @param chapter
     */
    public void saveOrUpdateChapter(Chapter chapter, String content) {
        chapter.setContent(APPCONST.BOOK_CACHE_PATH + chapter.getBookId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!StringHelper.isEmpty(chapter.getId())) {
            updateEntity(chapter);
        } else {
            addChapter(chapter, content);
        }
        saveChapterCacheFile(chapter, content);
    }

    /**
     * ??????????????????
     */
    public void addChapters(List<Chapter> chapters) {
        ChapterDao chapterDao = GreenDaoManager.getInstance().getSession().getChapterDao();
        chapterDao.insertInTx(chapters);
    }

    /**
     * ????????????
     *
     * @param chapter
     */
    public void saveChapterCacheFile(Chapter chapter, String content) {
        if (StringHelper.isEmpty(content)) {
            return;
        }

        File file = getBookFile(chapter.getBookId(), chapter.getTitle());
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(content.replace(chapter.getTitle(), ""));
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bw);
        }
    }

    /**
     * ??????????????????
     *
     * @param chapter
     */
    public void deleteChapterCacheFile(Chapter chapter) {
        File file = getBookFile(chapter.getBookId(), chapter.getTitle());
        file.delete();
    }

    /**
     * ????????????????????????
     * @param chapter
     * @return
     */
    public String getChapterCatheContent(Chapter chapter){
        File file = new File(APPCONST.BOOK_CACHE_PATH + chapter.getBookId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!file.exists()) return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder s = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null){
                s.append(line);
                s.append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            IOUtils.close(br);
        }
    }

    /**
     * ??????????????????
     *
     * @param newChapters
     */
    public void updateAllOldChapterData(ArrayList<Chapter> mChapters, ArrayList<Chapter> newChapters, String bookId) {
        int i;
        for (i = 0; i < mChapters.size() && i < newChapters.size(); i++) {
            Chapter oldChapter = mChapters.get(i);
            Chapter newChapter = newChapters.get(i);
            if (!oldChapter.getTitle().equals(newChapter.getTitle())) {
                oldChapter.setTitle(newChapter.getTitle());
                oldChapter.setUrl(newChapter.getUrl());
                oldChapter.setContent(null);
                saveOrUpdateChapter(oldChapter, null);
            }
        }
        if (mChapters.size() < newChapters.size()) {
            int start = mChapters.size();
            for (int j = mChapters.size(); j < newChapters.size(); j++) {
                newChapters.get(j).setId(StringHelper.getStringRandom(25));
                newChapters.get(j).setBookId(bookId);
                mChapters.add(newChapters.get(j));
//                mChapterService.addChapter(newChapters.get(j));
            }
            addChapters(mChapters.subList(start, mChapters.size()));
        } else if (mChapters.size() > newChapters.size()) {
            for (int j = newChapters.size(); j < mChapters.size(); j++) {
                deleteEntity(mChapters.get(j));
                deleteChapterCacheFile(mChapters.get(j));
            }
            mChapters.subList(0, newChapters.size());
        }
    }


    /**
     * ??????????????????????????????????????? (???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???)
     *
     * @param folderName : bookId
     * @param fileName:  chapterName
     * @return
     */
    public static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(APPCONST.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_FY);
        return file.exists();
    }


    private void deleteAllChapterCacheFile(String bookId) {
        FileUtils.deleteFile(APPCONST.BOOK_CACHE_PATH + bookId);
    }

    /**
     * ???????????????????????????
     *
     * @param folderName
     * @param fileName
     * @return
     */
    public static File getBookFile(String folderName, String fileName) {
        return FileUtils.getFile(APPCONST.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_FY);
    }

}
