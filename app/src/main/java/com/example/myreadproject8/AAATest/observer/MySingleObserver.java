package com.example.myreadproject8.AAATest.observer;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public abstract class MySingleObserver<T> implements SingleObserver<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onError(Throwable e) {

    }
}
