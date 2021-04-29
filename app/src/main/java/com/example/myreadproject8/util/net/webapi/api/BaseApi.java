package com.example.myreadproject8.util.net.webapi.api;

import com.example.myreadproject8.common.ErrorCode;
import com.example.myreadproject8.entity.JsonModel;
import com.example.myreadproject8.util.net.HttpUtil;
import com.example.myreadproject8.util.net.webapi.callback.ResultCallback;
import com.example.myreadproject8.util.toast.ToastUtils;

import java.util.Map;


/**
 * created by ycq on 2021/4/13 0013
 * describe：
 */
public class BaseApi {


    /**
     * get通用返回字符串api
     * 通过api获取蓝奏云直链
     * @param url
     * @param params
     * @param callback
     */
    public static void postLanzousApi(String url, Map<String, Object> params, final ResultCallback callback, final String referer) {
        HttpDataSource.httpPost(url, HttpUtil.makePostOutput(params), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o, code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }, referer);
    }


    /**
     * get通用返回Html字符串api
     * @param url
     * @param params
     * @param callback
     */
    public static void getCommonReturnHtmlStringApi(String url, Map<String, Object> params, String charsetName,  boolean isRefresh, final ResultCallback callback) {
        HttpDataSource.httpGet_html(HttpUtil.makeURL(url, params), charsetName, isRefresh ,new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o,code);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("hereisBaseApi"+e);
                callback.onError(e);
//                error(e,callback);
            }
        });
    }


    /**
     * api异常处理
     * @param e
     * @param callback
     */
    private static void error(Exception e, final ResultCallback callback){
      /*  if (e.toString().contains("SocketTimeoutException") || e.toString().contains("UnknownHostException")) {
            TextHelper.showText("网络连接超时，请检查网络");
        }*/
        e.printStackTrace();
        callback.onError(e);
    }


    /**
     * api请求失败处理
     * @param jsonModel
     * @param callback
     */
    private static void noSuccess(JsonModel jsonModel, ResultCallback callback){
        if (!jsonModel.isSuccess()) {
            if (jsonModel.getError() == ErrorCode.no_security) {
                ToastUtils.showWarring("登录过期，请重新登录");
            } else {
                if (jsonModel.getError() == 0) {
                    callback.onFinish(jsonModel.getResult(), -1);
                } else {
                    callback.onFinish(jsonModel.getResult(), jsonModel.getError());
                }
            }
        }
    }
}
