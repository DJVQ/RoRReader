package com.example.myreadproject8.util.net;

import android.util.Log;

import com.example.myreadproject8.Application.TrustAllCerts;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * description:处理okhttp3请求
 */
public class OkHttpUtils {

    public static OkHttpClient okHttpClient = HttpUtil.getOkHttpClient();

    /**
     * 同步获取html文件，默认编码utf-8
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(url, "utf-8");
    }
    public static String getHtml(String url, String encodeType) throws IOException {
        return getHtml(url, null, encodeType);
    }

    public static String getHtml(String url, RequestBody requestBody, String encodeType) throws IOException {

        Request.Builder builder = new Request.Builder()
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "Keep-Alive")
                //.addHeader("Charsert", "utf-8")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
        if (requestBody != null) {
            builder.post(requestBody);
            Log.d("HttpPost URl", url);
        }else {
            Log.d("HttpGet URl", url);
        }
        //创建request
        Request request = builder
                .url(url)
                .build();
        //创建response
        Response response = okHttpClient
                .newCall(request)
                .execute();
        ResponseBody body=response.body();
        if (body == null) {
            return "";
        } else {
            //以encodeType类型编码生成bodyStr
            String bodyStr = new String(body.bytes(), encodeType);
            Log.d("Http: read finish", bodyStr);
            return bodyStr;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public static InputStream getInputStream(String url) throws IOException {
        Request.Builder builder = new Request.Builder()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
        Request request = builder
                .url(url)
                .build();
        Response response = okHttpClient
                .newCall(request)
                .execute();
        if (response.body() == null){
            return null;
        }
        return response.body().byteStream();
    }
}
