package com.example.myreadproject8.util.utils.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.myreadproject8.AAATest.observer.MySingleObserver
import com.example.myreadproject8.Application.App
import com.example.myreadproject8.Application.SysManager
import com.example.myreadproject8.entity.ReadStyle
import com.example.myreadproject8.entity.Setting

import com.example.myreadproject8.greendao.GreenDaoManager
import com.example.myreadproject8.greendao.entity.*
import com.example.myreadproject8.greendao.entity.rule.BookSource
import com.example.myreadproject8.util.file.FileUtils
import com.example.myreadproject8.util.gson.GSON
import com.example.myreadproject8.util.gson.fromJsonArray
import com.example.myreadproject8.util.gson.fromJsonObject
import com.example.myreadproject8.util.sharedpre.SharedPreUtils
import com.example.myreadproject8.util.utils.storage.DocumentUtil
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import java.io.File

object Restore {

    fun restore(context: Context, uri: Uri, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach { doc ->
                for (fileName in Backup.backupFileNames) {
                    if (doc.name == fileName) {
                        DocumentUtil.readBytes(context, doc.uri)?.let {
                            FileUtils.getFile(Backup.backupPath + File.separator + fileName)
                                    .writeBytes(it)
                        }
                    }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        restore(Backup.backupPath, callBack)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    fun restore(path: String, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            try {
                val file = FileUtils.getFile(path + File.separator + "myBooks.json")
                val json = file.readText()
                GSON.fromJsonArray<Book>(json)?.forEach { bookshelf ->
                    /*if (bookshelf.noteUrl != null) {
                        DbHelper.getDaoSession().bookShelfBeanDao.insertOrReplace(bookshelf)
                    }
                    if (bookshelf.bookInfoBean.noteUrl != null) {
                        DbHelper.getDaoSession().bookInfoBeanDao.insertOrReplace(bookshelf.bookInfoBean)
                    }*/
                    GreenDaoManager.getInstance().session.bookDao.insertOrReplace(bookshelf)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "mySearchHistory.json")
                val json = file.readText()
                GSON.fromJsonArray<SearchHistory>(json)?.let {
                    GreenDaoManager.getInstance().session.searchHistoryDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "myBookMark.json")
                val json = file.readText()
                GSON.fromJsonArray<BookMark>(json)?.let {
                    GreenDaoManager.getInstance().session.bookMarkDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "myBookGroup.json")
                val json = file.readText()
                GSON.fromJsonArray<BookGroup>(json)?.let {
                    GreenDaoManager.getInstance().session.bookGroupDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "replaceRule.json")
                val json = file.readText()
                GSON.fromJsonArray<ReplaceRuleBean>(json)?.let {
                    GreenDaoManager.getInstance().session.replaceRuleBeanDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "bookSource.json")
                val json = file.readText()
                GSON.fromJsonArray<BookSource>(json)?.let {
                    GreenDaoManager.getInstance().session.bookSourceDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val settingFile = FileUtils.getFile(path + File.separator + "setting.json")
                val settingJson = settingFile.readText()
                val readStyleFile = FileUtils.getFile(path + File.separator + "readStyles.json")
                val readStylesJson = readStyleFile.readText()
                val readStyles = GSON.fromJsonObject<List<ReadStyle>>(readStylesJson)
                val setting = GSON.fromJsonObject<Setting>(settingJson)
                if (setting != null) {
                    setting.readStyles = readStyles
                }
                SysManager.saveSetting(setting)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Preferences.getSharedPreferences(App.getMContext(), path, "config")?.all?.map {
                val edit = SharedPreUtils.getInstance()
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
                edit.putInt("versionCode", App.getVersionCode())
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        //App.getApplication().initNightTheme()
                        callBack?.restoreSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }


    interface CallBack {
        fun restoreSuccess()
        fun restoreError(msg: String)
    }

}