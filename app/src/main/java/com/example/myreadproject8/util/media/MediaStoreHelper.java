package com.example.myreadproject8.util.media;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public class MediaStoreHelper {

    /**
     * 获取媒体库中所有的书籍文件
     * <p>
     * 暂时只支持 TXT
     *
     * @param activity
     * @param resultCallback
     */
    public static void getAllBookFile(FragmentActivity activity, MediaResultCallback resultCallback) {
        // 将文件的获取处理交给 LoaderManager。
        activity.getSupportLoaderManager()
                .initLoader(LoadCreater.ALL_BOOK_FILE, null, new MediaLoaderCallbacks(activity, resultCallback));
    }

    public interface MediaResultCallback {
        void onResultCallback(List<File> files);
    }

    /**
     * Loader 回调处理
     */
    static class MediaLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        protected WeakReference<Context> mContext;
        protected MediaResultCallback mResultCallback;

        public MediaLoaderCallbacks(Context context, MediaResultCallback resultCallback) {
            mContext = new WeakReference<>(context);
            mResultCallback = resultCallback;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return LoadCreater.create(mContext.get(), id, args);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            LocalFileLoader localFileLoader = (LocalFileLoader) loader;
            localFileLoader.parseData(data, mResultCallback);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
