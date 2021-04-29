package com.example.myreadproject8.Application;

import android.util.Log;

import com.example.myreadproject8.R;
import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.enums.BookcaseStyle;
import com.example.myreadproject8.enums.LocalBookSource;
import com.example.myreadproject8.greendao.GreenDaoManager;
import com.example.myreadproject8.greendao.entity.rule.BookSource;
import com.example.myreadproject8.util.net.crawler.read.ReadCrawlerUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.utils.CacheHelper;

import static com.example.myreadproject8.Application.App.getVersionCode;


public class SysManager {

    private static Setting mSetting;

    /**
     * 获取设置
     *
     * @return
     */
    public static Setting getSetting() {
        if (mSetting != null) {
            return mSetting;
        }
        mSetting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
        if (mSetting == null) {
            mSetting = getDefaultSetting();
            saveSetting(mSetting);
        }
        return mSetting;
    }

    public static Setting getNewSetting() {
        Setting setting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
        if (setting == null) {
            setting = getDefaultSetting();
            saveSetting(setting);
        }
        return setting;
    }


    public static Setting getReadSetting() {
        Setting setting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_READ_SETTING);
        setting = getDefaultSetting();
        saveReadSetting(setting);
        return setting;
    }

    /**
     * 保存设置
     *
     * @param setting
     */
    public static void saveReadSetting(Setting setting) {
        CacheHelper.saveObject(setting, APPCONST.FILE_NAME_READ_SETTING);
    }


    /**
     * 保存设置
     *
     * @param setting
     */
    public static void saveSetting(Setting setting) {
        CacheHelper.saveObject(setting, APPCONST.FILE_NAME_SETTING);
    }


    /**
     * 默认设置
     *
     * @return
     */
    private static Setting getDefaultSetting() {
        Setting setting = new Setting();
        setting.setDayStyle(true);
        setting.setBookcaseStyle(BookcaseStyle.listMode);
        setting.setNewestVersionCode(getVersionCode());
        setting.setAutoSyn(false);
        setting.setMatchChapter(true);
        setting.setMatchChapterSuitability(0.7f);
        setting.setCatheGap(150);
        setting.setRefreshWhenStart(true);
        setting.setOpenBookStore(true);
        setting.setSettingVersion(APPCONST.SETTING_VERSION);
        setting.setHorizontalScreen(false);
        setting.initReadStyle();
        setting.setCurReadStyleIndex(1);
        return setting;
    }

    public static void regetmSetting() {
        mSetting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
    }


    /**
     * 重置设置
     */

    public static void resetSetting() {
        Setting setting = getSetting();
        switch (setting.getSettingVersion()) {
            case 10:
            default:
                setting.initReadStyle();
                setting.setCurReadStyleIndex(1);
                setting.setSharedLayout(true);
                Log.d("SettingVersion", "" + 10);
            case 11:
                Log.d("SettingVersion", "" + 11);
            case 12:
                Log.d("SettingVersion", "" + 12);
        }
        setting.setSettingVersion(APPCONST.SETTING_VERSION);
        saveSetting(setting);
    }

    public static void resetSource() {

//        BookSource source = new BookSource();
//        source.setSourceEName("cangshu99");//这是内置书源标识，必填
//        source.setSourceName("99藏书");//设置书源名称
//        source.setSourceGroup("内置书源");//设置书源分组
//        source.setEnable(true);//设置书源可用性
//        source.setSourceUrl("com.example.myreadproject8.util.net.crawler.read.CansShu99ReadCrawler");//这是书源完整类路径，必填
//        source.setOrderNum(0);//内置书源一般设置排序为0
//        GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source);//添加书源进数据库
    }
}
