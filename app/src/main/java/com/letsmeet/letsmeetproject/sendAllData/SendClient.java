package com.letsmeet.letsmeetproject.sendAllData;

import android.util.Log;

import com.letsmeet.letsmeetproject.util.Config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SendClient {
    private Socket clientSocket;
    private boolean isSendMsgReady;
    private final String ip = Config.SERVER_IP;
    private final int port = Config.SERVER_PORT_SENDALLDATA;
    private BufferedWriter mWriter;
    private final String TAG = "SendClient";

    public SendClient(){
        initSocket();
    }


    /**
     * 向服务器发送消息
     */
    public void sendMsg(String msg) {
        if (msg==null||msg.length()==0||(!isSendMsgReady)){
            return;
        }
        try {
            mWriter.write(msg+"\n");
            mWriter.flush();
            Log.e(TAG,msg);
        } catch (IOException e) {
            isSendMsgReady = false;
            initSocket();
            e.printStackTrace();
        }
    }

    private void initSocket() {
        new InitInternet().start();
    }


    class InitInternet extends Thread {
        @Override
        public void run() {
            try {
                clientSocket=new Socket(ip,port);
                mWriter=new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"utf-8"));
                isSendMsgReady = true;
                Log.e(TAG,"连接服务器成功");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,"连接服务器失败");
                try { //若连接服务器失败，则每4s重新连接
                    Thread.sleep(4000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                this.run();
            }
        }
    }
}
