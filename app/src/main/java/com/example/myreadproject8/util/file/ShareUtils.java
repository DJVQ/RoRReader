package com.example.myreadproject8.util.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.myreadproject8.BuildConfig;
import com.example.myreadproject8.R;

import java.io.File;

/**
 * created by ycq on 2021/4/17 0017
 * describe：
 */
public class ShareUtils {
    public static void share(Context context, int stringRes) {
        share(context, context.getString(stringRes));
    }

    public static void share(Context context, String extraText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public static void share(Context context, File share, String title, String mimeType){
        //noinspection ResultOfMethodCallIgnored
        share.setReadable(true, false);
        Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", share);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.setType(mimeType);
        context.startActivity(Intent.createChooser(intent, title));
    }
}
