package com.letsmeet.letsmeetproject.http;

import android.util.Log;

import com.letsmeet.letsmeetproject.util.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil{
    String urlstrings = "";
    String sendMsg;
    private HttpResponse httpResponse;

    public HttpUtil(String url, HttpResponse httpResponse){
        this.urlstrings = "http://"+Config.SERVER_IP+":"+Config.SERVER_PORT_APPLICATION+"/"+url;
        this.httpResponse = httpResponse;
    }

    public String sendMsg(String msg){
        Log.e("HttpUtil",urlstrings+" "+msg);
        sendMsg = msg;
        new MyThread().start();
        return "";
    }

    private class MyThread extends Thread{
        @Override
        public void run() {
            try{
                URL url = new URL(urlstrings);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                connection.connect();

                DataOutputStream write = new DataOutputStream(connection.getOutputStream());
                write.writeBytes(sendMsg);
                write.flush();
                write.close();

                int responseCode = connection.getResponseCode();
                if (responseCode==HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                    String result = reader.readLine();
                    httpResponse.httpResponseCallback(result);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public interface HttpResponse{
        void httpResponseCallback(String responseResult);
    }

}
