package com.example.myreadproject8.enums;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;

/**
 * created by ycq on 2021/4/3 0003
 * describe：
 */
public enum LocalBookSource {
    local("本地书籍"),
    fynovel("RoR小说"),
    zuopin(App.getApplication().getString(R.string.read_zuopin)),
    miaobi(App.getApplication().getString(R.string.read_miaobi)),
    cangshu99(App.getApplication().getString(R.string.read_cangshu99));


    public String text;
    LocalBookSource(String text) {
        this.text = text;
    }

    public static LocalBookSource get(int var0) {
        return values()[var0];
    }
}
