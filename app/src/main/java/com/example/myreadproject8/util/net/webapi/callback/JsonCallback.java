package com.example.myreadproject8.util.net.webapi.callback;


import com.example.myreadproject8.entity.JsonModel;

/**
 * Created by zhao on 2016/10/25.
 */

public interface JsonCallback {

    void onFinish(JsonModel jsonModel);

    void onError(Exception e);

}
