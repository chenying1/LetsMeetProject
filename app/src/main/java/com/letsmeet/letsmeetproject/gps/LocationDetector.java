package com.letsmeet.letsmeetproject.gps;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import com.letsmeet.letsmeetproject.communicate.Communication;

import java.util.Timer;
import java.util.TimerTask;

public class LocationDetector {
    private TextView navigateView;
    private Communication communication;
    private Context context;
    private Timer navigateTimer;

    double longitude = 0;
    double latitude = 0;
    double longitude_other = 0;
    double latitude_other = 0;
    private GpsInfo gpsInfo;
    private GpsCompare gpsCompare;

    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    String string = (String) msg.obj;
                    navigateView.setText("目标在:"+string);
                    break;
            }
        };
    };

    public LocationDetector(Context context, TextView navigateView,Communication communication){
        this.context = context;
        this.navigateView = navigateView;
        this.communication = communication;
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
                }
            }
        };
        navigateTimer.schedule(navigateTask,0,3000);
    }

    private boolean gpsChanged(double lastNum, double newNum){
        if (Math.abs(lastNum-newNum)>0.00001){
            return true;
        }
        return false;
    }
}
