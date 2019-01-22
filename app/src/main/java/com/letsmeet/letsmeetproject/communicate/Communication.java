package com.letsmeet.letsmeetproject.communicate;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.letsmeet.letsmeetproject.LocationView;
import com.letsmeet.letsmeetproject.MyView;
import com.letsmeet.letsmeetproject.util.Config;
import com.letsmeet.letsmeetproject.util.NavigateTip;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Communication {
    private Socket clientSocket;
    private boolean isReceivingMsgReady;
    private boolean isSendMsgReady;
    public BufferedReader mReader;
    private BufferedWriter mWriter;
    public static float receiveOrient = 0;
    public static int otherStepCount = 0;
    public double receiveLongitude;
    public double receiveLatitude;

    public ArrayList<String> otherWifilist = new ArrayList<>();

    private String TAG = "Communication";
    private MyView otherView;
    private LocationView locationView;
    private TextView navigateView;

    private String ip= Config.SERVER_IP;
    private int port=Config.SERVER_PORT;

//    private int[] colors = {90808080,1E90FF,
//    };

    private String user;

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    int tipStatus = (int) msg.obj;
                    String tipString = NavigateTip.getTip(tipStatus);
                    Log.e(TAG+"N","tipStatus:"+tipStatus);
                    Log.e(TAG+"N","tipString:"+tipString);
                    Log.e(TAG+"N","BackgroundColor:"+NavigateTip.getColor(tipStatus));
                    navigateView.setBackgroundColor(NavigateTip.getColor(tipStatus));
                    navigateView.setText(tipString);
                    break;
            }
        };
    };

    public Communication(MyView otherView,LocationView locationView,TextView navigateView,String user){
        this.user = user;
        this.otherView = otherView;
        this.locationView = locationView;
        this.navigateView = navigateView;
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
                clientSocket=new Socket(ip,port);
                //从输入流中获取数据
                mReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"utf-8"));
                isReceivingMsgReady=true;
                //从输出流中获取数据
                mWriter=new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"utf-8"));
                isSendMsgReady = true;
                JSONObject init = new JSONObject();
                init.put("status",5);
                init.put("data",user);
                sendMsg(init.toString());
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
                            int status = (int)receiveMsg.get("status");
                            switch (status) {
                                case 0:  //对方方向更新
                                    String s = receiveMsg.getString("data");
                                    receiveOrient = Float.parseFloat(s);
                                    locationView.otherDegreeChanged(receiveOrient);
                                    otherView.orientChanged(receiveOrient);
                                    break;
                                case 1:   //对方发送的wifi数据  wifi数据在服务器端处理，不再转发过来
                                    break;
                                case 2:  //对方轨迹更新
                                    otherStepCount++;
                                    JSONObject coordinate = new JSONObject(receiveMsg.getString("data"));
                                    double x = (double) coordinate.get("curX");
                                    double y = (double) coordinate.get("curY");
                                    otherView.autoAddStep((float) x,(float)y);
                                    break;
                                case 4:    //对方GPS位置更新   GPS数据在服务器端处理，不再转发过来
                                    break;
                                case 5:   //导航提示
                                    JSONObject navigate = (JSONObject) receiveMsg.get("data");
                                    Log.e("Communication","接收到导航提示："+navigate);
                                    int sita = navigate.getInt("sita");
                                    int tipStatus = navigate.getInt("tip_status");
                                    int accuracy = navigate.getInt("accuracy");
                                    locationView.locationChanged(sita,accuracy);
                                    Log.e("Communication",NavigateTip.getTip(tipStatus));
                                    handler.obtainMessage(0,tipStatus).sendToTarget();
                                    break;
                            }
                        }
                        Thread.sleep(500);
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
