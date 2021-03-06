package com.example.myreadproject8.util.net.webapi.api;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.common.URLCONST;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


import static com.example.myreadproject8.util.net.webapi.api.BaseApi.getCommonReturnHtmlStringApi;
import static com.example.myreadproject8.util.net.webapi.api.BaseApi.postLanzousApi;

/**
 * @author fengyue
 * @date 2020/7/1 8:48
 */
public class LanZousApi {

    /**
     * 获取蓝奏云含有key的url
     *
     * @param lanZouUrl
     * @param callback
     */
    public static void getUrl1(String lanZouUrl, final ResultCallback callback) {
        getCommonReturnHtmlStringApi(lanZouUrl, null, "utf-8", true, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(getUrl1((String) o), code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取key
     *
     * @param url
     * @param callback
     */
    public static void getKey(String url, final ResultCallback callback) {
        getCommonReturnHtmlStringApi(url, null, "utf-8", true, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(getKey((String) o), code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取蓝奏云直链
     *
     * @param callback
     */
    public static void getUrl2(String key, final ResultCallback callback, final String referer) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("action", "downprocess");
        params.put("sign", key);
        params.put("ves", 1);
        postLanzousApi(URLCONST.LAN_ZOUS_URL + "/ajaxm.php", params, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(getUrl2((String) o), code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }, referer);
    }

    private static String getUrl1(String html) {
        Document doc = Jsoup.parse(html);
        return URLCONST.LAN_ZOUS_URL + doc.getElementsByClass("ifr2").attr("src");
    }

    private static String getKey(String html) {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String lanzousKeyStart = "var pposturl = '";
        try {
            lanzousKeyStart = spu.getString(App.getMContext().getString(R.string.lanzousKeyStart));
        }catch (Exception e){
            e.printStackTrace();
        }
        return StringHelper.getSubString(html, lanzousKeyStart, "'");
    }

    private static String getUrl2(String o) {
        String[] info = o.split(",");
        String zt = info[0].substring(info[0].indexOf(":") + 1);
        if (!"1".endsWith(zt)) {
            return null;
        }
        String dom = info[1].substring(info[1].indexOf(":") + 2, info[1].lastIndexOf("\""));
        String url = info[2].substring(info[2].indexOf(":") + 2, info[2].lastIndexOf("\""));
        dom = dom.replace("\\", "");
        url = url.replace("\\", "");
        return dom + "/file/" + url;
    }

    /**
     * 获取重定向地址
     *
     * @param path
     *
     */
    public static void getRedirectUrl(final String path, final ResultCallback callback) {
        App.getmApplication().newThread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(path)
                        .openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setRequestProperty("Accept-Language", "zh-cn");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/x-silverlight, */*");
                conn.connect();
                String redirectUrl = conn.getHeaderField("Location");
//                    Log.d("D/Http: RedirectUrl", redirectUrl);
                callback.onFinish(redirectUrl, 1);
            } catch (IOException e) {
                callback.onError(e);
            }finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        });

    }
}
