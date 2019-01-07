package com.letsmeet.letsmeetproject.sendAllData;

import android.location.Location;
import android.net.wifi.ScanResult;

import com.letsmeet.letsmeetproject.gps.GpsInfo;
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.setting.SystemUtil;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class SendAllData {
    private MySensorEventListener listener;
    private GpsInfo gpsInfo;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float pressure;
    private float[] angleValues = new float[3];
    private int satelliteNum;
    private ArrayList<Float> satelliteSnr = new ArrayList<>();
    private Location location;
    ArrayList<String> wifiInfo = new ArrayList<>();
    private SendClient sendClient;
    private int period = 50;  //ms   采样周期
    private Timer timer = new Timer();  //定时器
    private TimerTask task;
    private WifiScan wifiScan;
    List<ScanResult> results;
//    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
    private final String TAG = "SendAllData";
    private int count = 0;
    private long timestamp;


    public SendAllData(MySensorEventListener listener,GpsInfo gpsInfo,WifiScan wifiScan){
        this.listener = listener;
        this.gpsInfo = gpsInfo;
        this.wifiScan = wifiScan;
        init();
    }


    private void init(){
        sendClient = new SendClient();
        final String modelType = SystemUtil.getSystemModel();
        task = new TimerTask() {
            @Override
            public void run() {
                accelerometerValues = listener.accelerometerValues.clone();
                magneticValues = listener.accelerometerValues.clone();
                gyroscopeValues = listener.gyroscopeValues.clone();
                pressure = listener.pressure;
                angleValues = listener.angleValues.clone();
                satelliteSnr = gpsInfo.satelliteSnr;
                satelliteNum = satelliteSnr.size();
                location = gpsInfo.location;
                wifiInfo.clear();
                results = wifiScan.results;
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
                JSONObject sendJson = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                try {
                    sendJson.put("modelType",modelType);
                    sendJson.put("accX",accelerometerValues[0]);
                    sendJson.put("accY",accelerometerValues[1]);
                    sendJson.put("accZ",accelerometerValues[2]);
                    sendJson.put("magX",magneticValues[0]);
                    sendJson.put("magY",magneticValues[1]);
                    sendJson.put("magZ",magneticValues[2]);
                    sendJson.put("gyroX",gyroscopeValues[0]);
                    sendJson.put("gyroY",gyroscopeValues[1]);
                    sendJson.put("gyroZ",gyroscopeValues[2]);

                    sendJson.put("azimuth",angleValues[0]);
                    sendJson.put("pitch",angleValues[1]);
                    sendJson.put("roll",angleValues[2]);

                    sendJson.put("pressure",pressure);
                    sendJson.put("satelliteNum",satelliteNum);
                    sendJson.put("satelliteSnr",satelliteSnr);
                    sendJson.put("longitude",location.getLongitude());
                    sendJson.put("latitude",location.getLatitude());
                    sendJson.put("altitude",location.getAltitude());
                    sendJson.put("speed",location.getSpeed());
                    sendJson.put("bearing",location.getBearing());
                    sendJson.put("period",period);
                    timestamp = System.currentTimeMillis();
                    sendJson.put("timestamp",timestamp);
//                    sendJson.put("vTime",df.format(timestamp));
                    sendJson.put("wifiInfo",wifiInfo.toString());
                    jsonObject.put("count",count++);
                    jsonObject.put("data",sendJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendString = jsonObject.toString();
                sendClient.sendMsg(sendString);
            }
        };
        timer.schedule(task,0,period);
    }

}
