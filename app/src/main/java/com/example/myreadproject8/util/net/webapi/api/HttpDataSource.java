package com.example.myreadproject8.util.net.webapi.api;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.common.URLCONST;
import com.example.myreadproject8.entity.JsonModel;
import com.example.myreadproject8.util.net.HttpUtil;
import com.example.myreadproject8.util.net.webapi.callback.HttpCallback;
import com.example.myreadproject8.util.net.webapi.callback.JsonCallback;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.net.webdav.RSAUtilV2;
import com.example.myreadproject8.util.string.StringHelper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * created by ycq on 2021/4/13 0013
 * describe：
 */
public class HttpDataSource {
    /**
     * http请求 (get) :获取html
     * @param url
     * @param callback
     */
    public static void httpGet_html(String url, final String charsetName,  boolean isRefresh, final ResultCallback callback){
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest_okHttp(url, isRefresh, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response.toString());
                        callback.onFinish(response.toString(),0);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {
                Log.d("Local", "read finish：" + response);
                callback.onFinish(response, 0);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("hereisHttpDataSourse"+e);
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });
    }

    /**
     * http请求 (get)
     * @param url
     * @param callback
     */
    public static void httpGet(String url, final JsonCallback callback) {
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest(url, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response.toString());
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
//                        jsonModel.setResult(jsonModel.getResult().replace("\n",""));
//                        test(jsonModel.getResult());
//                        String str = new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n",""),Base64.DEFAULT),APPCONST.privateKey));
                        if (URLCONST.isRSA && !StringHelper.isEmpty(jsonModel.getResult())) {
                            jsonModel.setResult(StringHelper.decode(new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n", ""), Base64.DEFAULT), APPCONST.privateKey))));
                        }
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {

            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });

    }

    public static void httpGet(String url, final ResultCallback callback) {
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest(url, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response.toString());
                        callback.onFinish(response.toString(), 1);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {

            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });
    }

    /**
     * http请求 (post)
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final JsonCallback callback) {
        Log.d("HttpPost:", url + "&" + output);
        HttpUtil.sendPostRequest(url, output, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
                        if (URLCONST.isRSA && !StringHelper.isEmpty(jsonModel.getResult())) {
                            jsonModel.setResult(StringHelper.decode(new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n", ""), Base64.DEFAULT), APPCONST.privateKey))));
                        }
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {
                Log.e("http", response);
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * http请求 (post)
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final ResultCallback callback) {
        Log.d("HttpPost:", url + "&" + output);
        HttpUtil.sendPostRequest(url, output, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }
            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        callback.onFinish(response.toString(), 1);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(String response) {
                Log.e("http", response);
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * http请求 (post) 获取蓝奏云直链
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final ResultCallback callback, final String referer) {
        Log.d("HttpPost:", url + "&" + output);
        HttpUtil.sendPostRequest(url, output, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }
            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        callback.onFinish(response.toString(), 1);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {
                Log.e("http", response);
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }, referer);
    }


}


