package com.letsmeet.letsmeetproject.sendAllData;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.JsonReader;
import android.util.Log;

import com.letsmeet.letsmeetproject.MyView;
import com.letsmeet.letsmeetproject.gps.GpsInfo;
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.sensor.OrientDetector;
import com.letsmeet.letsmeetproject.util.MyUtil;
import com.letsmeet.letsmeetproject.util.SystemUtil;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;



public class SendAllData {
    private MySensorEventListener listener;
    private GpsInfo gpsInfo;
    private WifiScan wifiScan;
    private OrientDetector orientDetector;

    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float pressure;
    private float light;
    private float[] angleValues = new float[3];
    private int satelliteNum;
    private ArrayList<Float> satelliteSnr = new ArrayList<>();
    private Location location;
    private ArrayList<String> wifiInfo = new ArrayList<>();
    private SendClient sendClient;

    private Timer timer = null;  //定时器
    private TimerTask task = null;

    private List<ScanResult> results = new ArrayList<>();
    private final String TAG = "SendAllData";
    private int count = 0;
    private long timestamp;
    final String modelType = SystemUtil.getSystemModel();

    private boolean isCollectData = false;
    private int stepLenght = 10;
    private int frequency = 20;
    private int period = 1000/frequency;  //ms   采样周期
    private Set<String> parameters = new HashSet<>();
    private String user;
    private boolean isParametersInit = false;
    private MyView myView;
    private float curX;
    private float curY;
    private long locationId;

    public SendAllData(MySensorEventListener listener,GpsInfo gpsInfo,WifiScan wifiScan,OrientDetector orientDetector,String user,MyView myView){
        this.listener = listener;
        this.gpsInfo = gpsInfo;
        this.wifiScan = wifiScan;
        this.orientDetector = orientDetector;
        this.user = user;
        this.myView = myView;
        init();
    }


//    0:加速度
//    1:地磁场
//    2:方位角
//    3:陀螺仪
//    4:压力计
//    5:光强度
//    6:GPS
//    7:卫星
//    8:WiFi

//    status 1 表示采集数据初始化  2：表示采集的数据

    private void init(){
        sendClient = new SendClient();
    }

    class MyTimerTask extends TimerTask{
        MyTimerTask(){
            isParametersInit = false;
        }

        private void initParameters(){
            //初始化 告诉服务器采集参数有哪些
            locationId = 0;
            JSONObject sendJson = new JSONObject();
            JSONObject dataJson = new JSONObject();
            try {
                String parameters_str = MyUtil.serializeToString(parameters);
                sendJson.put("status",1);
                dataJson.put("user",user);
                dataJson.put("modelType",modelType);
                dataJson.put("period",period);
                dataJson.put("parameters",parameters_str);
                sendJson.put("data",dataJson.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            isParametersInit = sendClient.sendMsg(sendJson.toString());
        }

        private void updateLocationId(){
            if ((isEqual(curX,myView.getCurX()))&&(isEqual(curY,myView.getCurY()))){
                return;
            }else {
                curX = myView.getCurX();
                curY = myView.getCurY();
                locationId++;
            }
        }
        private void sendData(){
            updateLocationId();
            accelerometerValues = listener.accelerometerValues.clone();
            magneticValues = listener.magneticValues.clone();
            gyroscopeValues = listener.gyroscopeValues.clone();
            pressure = listener.pressure;
            light = listener.light;
            angleValues = orientDetector.angleValues.clone();
            satelliteSnr.clear();
            satelliteSnr.addAll(gpsInfo.satelliteSnr);
            satelliteNum = satelliteSnr.size();
            location = gpsInfo.location;
            wifiInfo.clear();
            results.clear();
            results.addAll(wifiScan.results);
            if (results!=null){
                for (ScanResult result:results){
                    JSONObject wifi = new JSONObject();
                    try {
                        wifi.put("SSID",result.SSID);
                        wifi.put("BSSID",result.BSSID);
                        wifi.put("level",result.level);
                        wifi.put("frequency",result.frequency);
                        wifi.put("timestamp",result.timestamp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    wifiInfo.add(wifi.toString());
                }
            }
            String sendString = "";
            JSONObject data = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            try {
//                data.put("modelType",modelType);
                data.put("locationId",locationId);
                data.put("coordinateX",curX);
                data.put("coordinateY",curY);
                if (parameters.contains("0")){
                    Log.e(TAG,"0发送加速度");
                    data.put("accX",accelerometerValues[0]);
                    data.put("accY",accelerometerValues[1]);
                    data.put("accZ",accelerometerValues[2]);
                }
                if (parameters.contains("1")){
                    Log.e(TAG,"1地磁场");
                    data.put("magX",magneticValues[0]);
                    data.put("magY",magneticValues[1]);
                    data.put("magZ",magneticValues[2]);
                }
                if (parameters.contains("2")){
                    Log.e(TAG,"2方位角");
                    data.put("azimuth",angleValues[0]);
                    data.put("pitch",angleValues[1]);
                    data.put("roll",angleValues[2]);
                }
                if (parameters.contains("3")){
                    Log.e(TAG,"3陀螺仪");
                    data.put("gyroX",gyroscopeValues[0]);
                    data.put("gyroY",gyroscopeValues[1]);
                    data.put("gyroZ",gyroscopeValues[2]);
                }
                if (parameters.contains("4")){
                    Log.e(TAG,"4压力计");
                    data.put("pressure",pressure);
                }
                if (parameters.contains("5")){
                    Log.e(TAG,"5光强度");
                    data.put("light",light);
                }
                if (parameters.contains("6")){
                    Log.e(TAG,"6 GPS");
                    data.put("longitude",location.getLongitude());
                    data.put("latitude",location.getLatitude());
                    data.put("altitude",location.getAltitude());
                    data.put("speed",location.getSpeed());
                    data.put("bearing",location.getBearing());
                }
                if (parameters.contains("7")){
                    Log.e(TAG,"7 卫星");
                    data.put("satelliteNum",satelliteNum);
                    data.put("satelliteSnr",satelliteSnr);
                }
                if (parameters.contains("8")){
                    Log.e(TAG,"8 WiFi");
                    data.put("wifiInfo",MyUtil.serializeToString(wifiInfo));
                }
//                data.put("period",period);
                timestamp = System.currentTimeMillis();
                data.put("timestamp",timestamp);

//                jsonObject.put("count",count++);
                jsonObject.put("data",data.toString());
                jsonObject.put("status",2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendString = jsonObject.toString();
            Log.e(TAG,"sendString:"+sendString);
            Log.e(TAG,"stepLen:"+stepLenght);
            Log.e(TAG,"parameter:"+parameters);
            sendClient.sendMsg(sendString);
        }
        @Override
        public void run() {
            if (!isParametersInit){
                initParameters();
            } else {
                sendData();
            }
        }
    }

    //序列化
//    public String serializeToString(Object obj){
//        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//        ObjectOutputStream objOut = null;
//        String str = null;
//        try {
//            objOut = new ObjectOutputStream(byteOut);
//            objOut.writeObject(obj);
//            str = byteOut.toString("ISO-8859-1");//此处只能是ISO-8859-1,但是不会影响中文使用
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return str;
//    }

    public void setFrequency(int frequency){
        this.frequency = frequency;
        this.period = 1000/frequency;
    }

    public int getFrequency(){
        return frequency;
    }

    public void setStepLenght(int stepLenght){
        this.stepLenght = stepLenght;
    }

    public int getStepLenght(){
        return stepLenght;
    }

    public void startSendData(){
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        if (task!=null){
            task.cancel();
            task=null;
        }
        timer = new Timer();
        task = new MyTimerTask();
        Log.e(TAG,"开始采集数据");
        timer.schedule(task,0,period);
    }

    public void finishSendData(){
        if (timer!=null){
            timer.cancel();
            timer = null;
        }
    }

    public boolean isCollectData() {
        return isCollectData;
    }

    public void setCollectData(boolean collectData) {
        isCollectData = collectData;
    }

    public Set<String> getParameters() {
        return parameters;
    }

    public void setParameters(Set<String> parameters) {
        this.parameters.clear();
        this.parameters.addAll(parameters);
    }

    private boolean isEqual(float f1, float f2){
        if (Math.abs(f1-f2)<0.0001){
            return true;
        }
        return false;
    }
}
