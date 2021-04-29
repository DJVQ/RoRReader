package com.example.myreadproject8.util.net.webapi.callback;


public interface ResultCallback {

    void onFinish(Object o, int code);

    void onError(Exception e);

}
