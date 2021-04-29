package com.example.myreadproject8.Application;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.util.net.HttpUtil;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.source.BookSourceManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * created by ycq on 2021/4/10 0010
 * describe：
 */
public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    private static App application = new App();
    private static final Handler handler = new Handler();
    private ExecutorService mFixedThreadPool;



    private void firstInit() {
        SharedPreUtils sru = SharedPreUtils.getInstance();
        if (!sru.getBoolean("firstInit")){
            BookSourceManager.initDefaultSources();
            sru.putBoolean("firstInit", true);
        }
    }


    public void newThread(Runnable runnable) {
        try {
            mFixedThreadPool.execute(runnable);
        } catch (Exception e) {
            //e.printStackTrace();
            mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//初始化线程池
            mFixedThreadPool.execute(runnable);
        }
    }

    public void shutdownThreadPool() {
        mFixedThreadPool.shutdownNow();
    }

    private boolean isFolderExist(String dir) {
        File folder = Environment.getExternalStoragePublicDirectory(dir);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        firstInit();
        HttpUtil.trustAllHosts();//信任所有证书
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//初始化线程池
    }

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    public static App getmApplication(){
        return application;
    }
    public static Application getApplication() {
        Application application = null;
        try{
            Class atClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = atClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create","curApp class1:"+application);
        }catch (Exception e){
            Log.d("fw_create","e:"+e.toString());
        }

        if(application != null)
            return application;

        try{
            Class atClass = Class.forName("android.app.AppGlobals");
            Method currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create","curApp class2:"+application);
        }catch (Exception e){
            Log.d("fw_create","e:"+e.toString());
        }

        return application;
    }

    public static Application getCurApplication(){
        Application application = null;
        try{
            Class atClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = atClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create","curApp class1:"+application);
        }catch (Exception e){
            Log.d("fw_create","e:"+e.toString());
        }

        if(application != null)
            return application;

        try{
            Class atClass = Class.forName("android.app.AppGlobals");
            Method currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create","curApp class2:"+application);
        }catch (Exception e){
            Log.d("fw_create","e:"+e.toString());
        }

        return application;
    }

    public static Context getMContext() {
        return getCurApplication();
    }

    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static Handler getHandler(){
        return handler;
    }


    /**
     * 判断Activity是否Destroy
     * @param mActivity
     * @return
     */
    public static boolean isDestroy(Activity mActivity) {
        return mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed();
    }

    /**
     * 获取app版本号
     *
     * @return
     */
    public static int getVersionCode() {
        try {
            PackageManager manager = application.getPackageManager();
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);

            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取app版本号(String)
     *
     * @return
     */
    public static String getStrVersionName() {
        try {
            PackageManager manager = application.getPackageManager();
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);

            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     * @param absPath apk包的绝对路径
     */
    public static int apkInfo(String absPath) {
        PackageManager pm = application.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            int versionCode = pkgInfo.versionCode;
            Log.i(TAG, String.format("PkgInfo: %s", versionCode));
            return versionCode;
        }
        return 0;
    }


    @TargetApi(26)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel downloadChannel = new NotificationChannel(APPCONST.channelIdDownload, "下载通知", NotificationManager.IMPORTANCE_LOW);
        downloadChannel.enableLights(true);//是否在桌面icon右上角展示小红点
        downloadChannel.setLightColor(Color.RED);//小红点颜色
        downloadChannel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(downloadChannel);

        NotificationChannel readChannel = new NotificationChannel(APPCONST.channelIdRead, "朗读通知", NotificationManager.IMPORTANCE_LOW);
        readChannel.enableLights(true);//是否在桌面icon右上角展示小红点
        readChannel.setLightColor(Color.RED);//小红点颜色
        readChannel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(readChannel);
    }
}
