package com.example.myreadproject8.util.media;

import android.content.Context;
import android.os.Bundle;

import androidx.loader.content.CursorLoader;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public class LoadCreater {
    public static final int ALL_BOOK_FILE = 1;

    public static CursorLoader create(Context context, int id, Bundle bundle) {
        LocalFileLoader loader = null;
        switch (id){
            case ALL_BOOK_FILE:
                loader = new LocalFileLoader(context);
                break;
            default:
                loader = null;
                break;
        }
        if (loader != null) {
            return loader;
        }

        throw new IllegalArgumentException("The id of Loader is invalid!");
    }
}

