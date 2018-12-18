package com.letsmeet.letsmeetproject.communicate;

import android.os.AsyncTask;
import android.util.Log;

import com.letsmeet.letsmeetproject.MyView;
import com.letsmeet.letsmeetproject.setting.Config;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Communication {

    private Socket clientSocket;
    private boolean isReceivingMsgReady;
    private boolean isSendMsgReady;
    public BufferedReader mReader;
    private BufferedWriter mWriter;
    public static String receiveOrient = "0.0";
    public static int otherStepCount = 0;
    public static double receiveLongitude;
    public static double receiveLatitude;

    private String TAG = "Communication";
    private MyView otherView;

    public Communication(MyView otherView){
        this.otherView = otherView;
        initSocket();
    }

    public Communication(){
        initSocket();
    }

    public void send(final String string) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendMsg(string);
                return null;
            }
        }.execute();
    }

    /**
     * 向服务器发送消息
     */
    private void sendMsg(String msg) {
        if (msg==null||msg.length()==0||(!isSendMsgReady)){
            return;
        }
        Log.e(TAG,"send:"+msg);
        try {
            //通过BufferedWriter对象向服务器写数据
            mWriter.write(msg+"\n");
            //调用flush将缓存中的数据写到服务器
            mWriter.flush();
//            Log.e("Communication",msg);
        } catch (IOException e) {
//            Log.e(TAG,"与服务器断开连接");
            isSendMsgReady = false;
            initSocket();
            e.printStackTrace();
        }
    }

    private void initSocket() {
        //另起线程监听输入流
        new InitInternet().start();
    }

    class InitInternet extends Thread {
        @Override
        public void run() {
            try {
                String ip= Config.SERVER_IP;
                int port=Config.SERVER_PORT;
                clientSocket=new Socket(ip,port);
                //从输入流中获取数据
                mReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"utf-8"));
                isReceivingMsgReady=true;
                //从输出流中获取数据
                mWriter=new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"utf-8"));
                isSendMsgReady = true;
//                Log.e(TAG,"连接服务器成功");
                receiveMsg();
            } catch (Exception e) {
                e.printStackTrace();
//                Log.e(TAG,"连接服务器失败");
                try { //若连接服务器失败，则每4s重新连接
                    Thread.sleep(4000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                this.run();
            }
        }
    }

    public void receiveMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        if (isReceivingMsgReady&&mReader.ready()) {
                            JSONObject receiveMsg = new JSONObject(mReader.readLine());
                            Log.e("receiveMsg:",receiveMsg.toString());
                            int status = (int)receiveMsg.get("status");
                            switch (status) {
                                case 0:
                                    receiveOrient = (String)receiveMsg.get("data");
                                    break;
                                case 2:
//                                    Log.e("TAG","对方步伐有更新。");
                                    otherStepCount++;
                                    JSONObject coordinate = (JSONObject)receiveMsg.get("data");;
                                    double x = (double) coordinate.get("curX");
                                    double y = (double) coordinate.get("curY");
                                    otherView.autoAddStep((float) x,(float)y);
                                    break;
                                case 4:
                                    Log.e("TAG","对方Location更新");
                                    JSONObject location = (JSONObject) receiveMsg.get("data");
                                    receiveLongitude = location.getDouble("longitude");
                                    receiveLatitude = location.getDouble("latitude");
                                    Log.e("TAG","longitude_other:"+receiveLongitude);
                                    Log.e("TAG","latitude_other:"+receiveLongitude);
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void closeCommunication(){
        try {
            mWriter.close();
            mReader.close();
            clientSocket.close();
        } catch (Exception e) {
//            Log.e(TAG,"通信资源关闭失败");
        }

    }
}
