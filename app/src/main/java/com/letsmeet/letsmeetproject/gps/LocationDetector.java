package com.letsmeet.letsmeetproject.gps;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import com.letsmeet.letsmeetproject.LocationView;
import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;
import com.letsmeet.letsmeetproject.wifiInfo.WifilistCompare;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LocationDetector {
    private TextView navigateView;
    private Communication communication;
    private WifiScan wifiScan;
    private Context context;
    private Timer navigateTimer;

    private double longitude = 0;
    private double latitude = 0;
    private double longitude_other = 0;
    private double latitude_other = 0;
    private GpsInfo gpsInfo;
    private GpsCompare gpsCompare;
    private WifilistCompare wifilistCompare = new WifilistCompare();

    private LocationView locationView;

    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    String string = (String) msg.obj;
//                    navigateView.setBackgroundColor(90808080);
                    navigateView.setText("目标在:"+string);
                    break;
            }
        };
    };

    public GpsInfo getGpsInfo(){
        return this.gpsInfo;
    }

    public LocationDetector(Context context, TextView navigateView, Communication communication, WifiScan wifiScan, LocationView locationView){
        this.context = context;
        this.navigateView = navigateView;
        this.communication = communication;
        this.wifiScan = wifiScan;
        this.locationView = locationView;
        init();
        startNavigate();
    }

    private void init(){
        navigateTimer = new Timer();
        gpsInfo = new GpsInfo(context,communication);
        gpsCompare = new GpsCompare();
    }

    private void startNavigate(){
        TimerTask navigateTask = new TimerTask() {
            @Override
            public void run() {
                navigateByGps();
//                navigateByWifi();
            }
        };
        navigateTimer.schedule(navigateTask,0,3000);
    }

    private void navigateByWifi(){
        ArrayList<String> myWifilist = wifiScan.wifilist;
        ArrayList<String> otherWifilist = communication.otherWifilist;
        float intersectionUnionRatio = wifilistCompare.getIntersectionUnionRatio(myWifilist,otherWifilist);
        handler.obtainMessage(0,Float.toString(intersectionUnionRatio)).sendToTarget();
    }
    private void navigateByGps(){
        if (gpsChanged(longitude,gpsInfo.longitude)||
                gpsChanged(latitude,gpsInfo.latitude)||
                gpsChanged(longitude_other,communication.receiveLongitude)||
                gpsChanged(latitude_other,communication.receiveLatitude)){
            longitude = gpsInfo.longitude;
            latitude = gpsInfo.latitude;
            longitude_other = communication.receiveLongitude;
            latitude_other = communication.receiveLatitude;
            String navigateString = gpsCompare.compareBtoA(longitude,latitude,longitude_other,latitude_other);
            if (navigateString!=null){
                handler.obtainMessage(0,navigateString).sendToTarget();
            }
            //左上角 方向提示
            locationView.setLocation(longitude,latitude,longitude_other,latitude_other);
            locationView.locationChanged();
        }
    }

    private boolean gpsChanged(double lastNum, double newNum){
        if (Math.abs(lastNum-newNum)>0.00001){
            return true;
        }
        return false;
    }

    public void timerCancel(){
        if (navigateTimer!=null){
            navigateTimer.cancel();
        }
    }
}
