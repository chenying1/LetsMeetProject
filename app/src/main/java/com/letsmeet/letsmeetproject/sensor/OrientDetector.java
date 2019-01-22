package com.letsmeet.letsmeetproject.sensor;

import android.hardware.SensorManager;
import android.util.Log;

import com.letsmeet.letsmeetproject.LocationView;
import com.letsmeet.letsmeetproject.MyView;
import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.util.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class OrientDetector {
    private float rotateDegree; //当前手机角度
    private float lastRotateDegree;
    private float receiveDegree;
    private float lastReceiveDegree;

    private Timer orientSendTimer;
    private Timer orientReceiveTimer;

    private MySensorEventListener mySensorEventListener;
    private Communication communication;
    private MyView myView;
    private MyView otherView;
    private LocationView locationView;
    public float[] angleValues = new float[3];

    public OrientDetector(MyView myView, MyView otherView,LocationView locationView,MySensorEventListener mySensorEventListener, Communication communication){
        this.myView = myView;
        this.otherView = otherView;
        this.locationView = locationView;
        this.mySensorEventListener = mySensorEventListener;
        this.communication = communication;
        init();
        startOrient();
    }

    private void init(){
        orientSendTimer = new Timer();
        orientReceiveTimer = new Timer();
    }

    /**
     * 开始发数据、收数据
     */
    private void startOrient(){
        //定时器每隔1s查看收到的数据
//        orientReceiveTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                //当前传感器接收到的数据，即对方的角度
//                receiveDegree = Communication.receiveOrient;
//                Log.e("OrientDetector:","重绘other箭头:"+receiveDegree);
//                if (Math.abs(lastReceiveDegree - receiveDegree)>1){
//                    otherView.orientChanged(receiveDegree);
//                    lastReceiveDegree = receiveDegree;
//
//                }
//            }
//        }, Config.delay, Config.period);

        //定时器每隔1s发数据 角度
        orientSendTimer.schedule(new TimerTask() {
            public void run() {
                calculateDegree();
//                rotateDegree = mySensorEventListener.rotateDegree;
                Log.e("OrientDetector:","发送方向:"+rotateDegree);
//                if (isOrientChange()){
//                    lastRotateDegree = rotateDegree;
//                    JSONObject sendString = new JSONObject();
//                    String data = Float.toString(rotateDegree);
//                    try {
//                        sendString.put("status",Config.STATUS_ORIENT);
//                        sendString.put("data",data);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    communication.send(sendString.toString());
//                }
            }
        }, Config.delay, Config.period);
    }

    private void calculateDegree(){
        //计算手机朝向
        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R, null, mySensorEventListener.accelerometerValues, mySensorEventListener.magneticValues);
        SensorManager.getOrientation(R, values);
        angleValues = values.clone();
        //获取手机朝向的角度
        rotateDegree = (float) Math.toDegrees(values[0]);
        if (isOrientChange()) {
            myView.orientChanged(rotateDegree);
            locationView.myDegreeChanged(rotateDegree);
            lastRotateDegree = rotateDegree;

            JSONObject sendString = new JSONObject();
            String data = Float.toString(rotateDegree);
            try {
                sendString.put("status",Config.STATUS_ORIENT);
                sendString.put("data",data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            communication.send(sendString.toString());
        }
    }

    private boolean isOrientChange(){
        if (Math.abs(rotateDegree - lastRotateDegree) > 1) {
            return true;
        }else {
            return false;
        }
    }

    public void timerCancel(){
        if (orientSendTimer!=null){
            orientSendTimer.cancel();
        }
        if (orientReceiveTimer!=null){
            orientReceiveTimer.cancel();
        }
    }
}
