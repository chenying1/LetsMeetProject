package com.letsmeet.letsmeetproject.sensor;

import android.widget.TextView;

import com.letsmeet.letsmeetproject.MyView;
import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.util.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class StepDetector {
    private Timer timer;
    private TimerTask task;

    private MySensorEventListener sensorEventListener;
    private ArrayList<Crest> accelerateValues;
    public int count ;
    private Communication communication;
    private MyView myView;
    private TextView textView;
    private int stepCount = 0;

//    ArrayList<Double> acc_test = new ArrayList<>();
//    ArrayList<Double> list = new ArrayList<>();

//    private Handler handler = new Handler() {
//        public void handleMessage(android.os.Message msg) {
//            switch (msg.what) {
//                case 0:
//                    textView.setText("步数："+stepCount);
//                    break;
//            }
//        };
//    };

    public StepDetector(MySensorEventListener sensorEventListener, MyView myView, Communication communication, TextView step){
        this.myView = myView;
        this.sensorEventListener = sensorEventListener;
        this.communication = communication;
        this.textView = step;
        init();
    }

    private void init(){
        accelerateValues = new ArrayList<>();

        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                //采集传感器数据
                Crest crest = new Crest(sensorEventListener.crest);
                accelerateValues.add(crest);
//                acc_test.add(crest.value);
                if (accelerateValues.size()>=20){
                    count = detected(accelerateValues);
                    stepCount += count;
//                    handler.obtainMessage(0,"step").sendToTarget();
//                    JSONObject test = new JSONObject();
//                    for (int i=0;i<accelerateValues.size();i++){
//                        list.add(accelerateValues.get(i).value);
//                    }
//                    try {
//                        test.put("status",3);
//                        test.put("count",count);
//                        test.put("data",list.toString());
//                        test.put("acc",acc_test.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    communication.send(test.toString());
                    accelerateValues.clear();
                    if (count<=0){
                        return;
                    }
                    //更新UI
                    while (count>0){
                        myView.autoAddStep();
                        count--;
                    }
                    //将移动之后的位置发送给服务器
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        jsonObject.put("status",Config.STATUS_STEP_DETECT);
                        data.put("curX",myView.getCurX());
                        data.put("curY",myView.getCurY());
                        jsonObject.put("data",data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    communication.send(jsonObject.toString());
                }
            }
        };
        timer.schedule(task,3000,50);
    }

    private int detected(ArrayList<Crest> list){
        ArrayList<Crest> highlist = new ArrayList<>();
        Crest lastCrest = list.get(0);
        boolean isUp = false;
        for (int i=1;i<list.size();i++){
            //找波峰 波谷  偶数下标为波谷，基数下标为波峰
            if (isUp){
                if (list.get(i).value<lastCrest.value){
                    highlist.add(new Crest(lastCrest));
                    isUp = false;
                }else {
                    //检测到最后一个，且属于上升的状态，也作为一个峰
                    if (i==(list.size()-1)){
                        highlist.add(new Crest(lastCrest));
                    }
                }
                lastCrest = list.get(i);
            } else {
                if (list.get(i).value>lastCrest.value){
                    //添加波谷
                    highlist.add(new Crest(lastCrest));
                    isUp = true;
                }
                lastCrest = list.get(i);
            }
        }

        //去除伪波峰
        for(int i=1;i<highlist.size();i+=2){
            //波峰大小  1.2g-2g
            if (highlist.get(i).value<11.5){
                highlist.set(i,null);
                highlist.set(i-1,null);
            }
        }
        removeNull(highlist);

        //去除波峰波谷差值小的
        for(int i=1;i<highlist.size();i+=2){
            if (highlist.get(i).value-highlist.get(i-1).value<2.5){
                highlist.set(i,null);
                highlist.set(i-1,null);
            }
        }
        removeNull(highlist);

        int lastIndex = 1;
        long curTime;
        long lastTime;
        for (int i=3;i<highlist.size();i+=2){
            //去除波峰的时间间隔小于200ms的
            curTime = highlist.get(i).timestamp;
            lastTime = highlist.get(lastIndex).timestamp;
            if (curTime - lastTime<200000000){
                if (highlist.get(lastIndex).value<=highlist.get(i).value){
                    highlist.set(lastIndex,null);
                    lastIndex = i;
                }else {
                    highlist.set(i,null);
                }
            }
        }
        removeNull(highlist);
        int num = highlist.size();
        highlist.clear();
        return num/2;
    }

    private void removeNull(ArrayList<Crest> list){
        for (Iterator<Crest> ite = list.iterator(); ite.hasNext();) {
            Crest str = ite.next();
            if (str==null) {
                ite.remove();
            }
        }
    }

    public void timerCancel(){
        if (timer!=null){
            timer.cancel();
        }
    }
}
