package com.letsmeet.letsmeetproject.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GpsInfo {
    private Context context;

    private LocationManager lm;

    private LocationListener locationListener;
    private Location location;  //位置信息
    private List<GpsSatellite> satelliteList = new ArrayList<>(); // 卫星信息
    private static final String TAG = "GpsInfo";

    public GpsInfo(Context context){
        this.context = context;
        init();
    }

    private void init(){
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        locationListener = new MyLocationListener();
        updateLocation(location);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 8, locationListener);
        lm.addGpsStatusListener(statusListener);
    }

    //更新位置信息
    private void updateLocation(Location newLocation){
        if (newLocation!=null){
           this.location = newLocation;
           Log.e(TAG,"经度:"+location.getLatitude());
            Log.e(TAG,"纬度:"+location.getLongitude());
            Log.e(TAG,"高度:"+location.getAltitude());
            Log.e(TAG,"方向:"+location.getBearing());
            Log.e(TAG,"速度:"+location.getSpeed());
        }
    }

    //更新GPS状态
    private void updateGpsStatus(int event, GpsStatus status) {
        if (status == null) {
            return;
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            satelliteList.clear();
            int index = 0;
            while (it.hasNext() && index <= maxSatellites) {
                GpsSatellite s = it.next();
                if (s.getSnr() != 0)//只有信躁比不为0的时候才算搜到了卫星
                {
                    satelliteList.add(s);
                }
                index++;
            }
        }
        Log.e(TAG, "卫星个数:" + satelliteList.size());
        for (int i = 0; i < satelliteList.size(); i++) {
            Log.e(TAG, "卫星信噪比:" + satelliteList.get(i).getSnr());
        }
    }
    /**
     * 卫星状态监听器
     */
    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        // GPS状态变化时的回调，如卫星数发生改变
        public void onGpsStatusChanged(int event) {
            GpsStatus status = null;
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                status = lm.getGpsStatus(null); //取当前状态
            }
            updateGpsStatus(event, status);
        }
    };


    class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                updateLocation(lm.getLastKnownLocation(provider));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateLocation(null);
        }
    }

}
