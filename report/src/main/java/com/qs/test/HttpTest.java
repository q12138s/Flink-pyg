package com.qs.test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author:qisuhai
 * @date:2019/11/20
 * @description:
 */
public class HttpTest {

    public static void main(String[] args) throws Exception {

        String address = "http://localhost:8090/report/send";
        sendData(address,"info");


    }

    /**
     * 模拟 POST 发送日志数据
     * @param address
     * @param info
     */
    public static void sendData(String address, String info) throws Exception {
        URL url = new URL(address);
        //获取http连接对象
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //配置http请求参数
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-type","application/json");
        //用户代理
        urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
        //发送数据
        OutputStream outputStream = urlConnection.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        bos.write(info.getBytes());
        bos.flush();
        bos.close();
        //读取相应数据
        InputStream inputStream = urlConnection.getInputStream();
        byte[] bytes = new byte[1024];
        String str = "";
        while (inputStream.read(bytes,0,1024)!= -1 ){
            str= new String(bytes);
            System.out.println(str+"");
        }
        System.out.println("<<<响应信息："+str);
        System.out.println("<<<响应状态："+urlConnection.getResponseCode());
    }

}
