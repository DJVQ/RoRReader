package com.example.myreadproject8.util.file;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by ycq on 2021/4/3 0003
 * describe：释放资源
 */
public class IOUtils {
    public static void close(Closeable... closeables){
        for (Closeable closeable : closeables){
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}