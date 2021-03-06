//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.example.myreadproject8.util.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.string.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;



public class NetworkUtils {
    public static final int SUCCESS = 10000;
    public static final int ERROR_CODE_NONET = 10001;
    public static final int ERROR_CODE_OUTTIME = 10002;
    public static final int ERROR_CODE_ANALY = 10003;
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String> errorMap = new HashMap<>();

    static {
        errorMap.put(ERROR_CODE_NONET, "没有网络");
        errorMap.put(ERROR_CODE_OUTTIME, "网络连接超时");
        errorMap.put(ERROR_CODE_ANALY, "数据解析失败");
    }

    public static String getErrorTip(int code) {
        return errorMap.get(code);
    }

    public static boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) App.getMContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }
    /**
     * 获取绝对地址
     */
    public static String getAbsoluteURL(String baseURL, String relativePath) {
        if (StringHelper.isEmpty(relativePath)) return "";
        if (StringHelper.isEmpty(baseURL)) return relativePath;
        String header = null;
        if (StringUtils.startWithIgnoreCase(relativePath, "@header:")) {
            header = relativePath.substring(0, relativePath.indexOf("}") + 1);
            relativePath = relativePath.substring(header.length());
        }
        try {
            URL absoluteUrl = new URL(baseURL);
            URL parseUrl = new URL(absoluteUrl, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }

    public static String getAbsoluteURL(URL baseURL, String relativePath) {
        if (baseURL == null) return relativePath;
        String header = null;
        if (StringUtils.startWithIgnoreCase(relativePath, "@header:")) {
            header = relativePath.substring(0, relativePath.indexOf("}") + 1);
            relativePath = relativePath.substring(header.length());
        }
        try {
            URL parseUrl = new URL(baseURL, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }
    public static boolean isUrl(String urlStr) {
        if (urlStr == null) return false;
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }
    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }
}
