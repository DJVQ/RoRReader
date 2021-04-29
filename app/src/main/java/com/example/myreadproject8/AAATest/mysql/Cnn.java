package com.example.myreadproject8.AAATest.mysql;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class Cnn extends AsyncTask<String, Void, String> {

        //定义Context上下文环境，方便在其他活动中使用此类方法来调用startActivity方法
        Context context;
        //发布PHP网页的服务器地址
        String connStr ;

        public void setConnStr(String connStr) {
                this.connStr = connStr;
        }

        public Cnn(Context context){
                this.context = context;
        }

        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onPostExecute(String s) {}

        /*
         * 使用Android + PHP + MySQL的方式连接数据库检验登录信息
         * 连接MySQL数据库的操作放在PHP网页中执行
         * */
        @Override
        protected String doInBackground(String... voids) {
                //查询MySQL数据库返回值
                String result = "";

                //获取用户信息
                String user = voids[0];
                String pass = voids[1];

                try {
                        /**/
                        //创建URL对象
                        URL url = new URL(connStr);
                        //调用url的openConnection()来获取HttpURLConnection对象实例
                        HttpURLConnection http =(HttpURLConnection) url.openConnection();
                        //设置http请求使用的方法为POST
                        http.setRequestMethod("POST");
                        //打开http读写开关
                        http.setDoInput(true);
                        http.setDoOutput(true);

                        //用http的getOutputStream()方法获取输出流
                        OutputStream ops = http.getOutputStream();

                        //定义BufferWriter writer将文本写入输出流
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops,"UTF-8"));
                        //in_user[在PHP文件中已定义]=user in_pass[...]=pass
                        String data = URLEncoder.encode("in_user","UTF-8")+"="+ URLEncoder.encode(user,"UTF-8")
                                +"&&"+ URLEncoder.encode("in_pass","UTF-8")+"="+ URLEncoder.encode(pass,"UTF-8");
                        //写入输出流
                        writer.write(data);
                        //输出
                        writer.flush();
                        //关闭writer
                        writer.close();
                        //关闭输出流
                        ops.close();

                        //用http的getInputStream()方法获取输入流
                        InputStream ips = http.getInputStream();
                        //定义BufferRead reader将文本从输入流读入
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips,"ISO-8859-1"));
                        String line = "";
                        //判断读入数据是否存在
                        while ((line = reader.readLine())!=null){
                                result += line;
                        }
                        reader.close();
                        ips.close();;
                        http.disconnect();
                        return result;

                } catch (MalformedURLException e) {
                        result = e.getMessage();
                } catch (IOException e) {
                        result = e.getMessage();
                }
                return result;
        }

}
