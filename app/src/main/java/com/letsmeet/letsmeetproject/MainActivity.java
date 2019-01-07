package com.letsmeet.letsmeetproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.gps.LocationDetector;
import com.letsmeet.letsmeetproject.sendAllData.SendAllData;
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.sensor.OrientDetector;
import com.letsmeet.letsmeetproject.sensor.StepDetector;
import com.letsmeet.letsmeetproject.setting.SystemUtil;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private Context context = this;
    private Communication communication;
    private TextView navigateView;
    private  MySensorEventListener listener;
    private MyView myView;
    private MyView otherView;

    private LocationView locationView;
    private WifiScan wifiScan;
    private LocationDetector locationDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myView = (MyView) findViewById(R.id.myView);
        otherView = (MyView) findViewById(R.id.otherView);
        navigateView = (TextView) findViewById(R.id.navigation);

        locationView = (LocationView) findViewById(R.id.locationView);

        myView.setArrowColor(getResources().getColor(R.color.deepblue));
        listener = new MySensorEventListener(this,myView);
        listener.registerSensor();
        communication = new Communication(otherView);
        requestLocationPermission();
        startScan(context);
        locationDetector = new LocationDetector(context,navigateView,communication,wifiScan,locationView);
        new OrientDetector(myView,otherView,listener,communication);
        new StepDetector(listener,myView,communication);
        Log.e("SystemUtil",SystemUtil.getSystemModel()+" "+SystemUtil.getSystemVersion());
        SendAllData sendAllData = new SendAllData(listener,locationDetector.getGpsInfo(),wifiScan);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放传感器资源
        listener.unregisterOrient();
    }


    /**
     * 获取wifi列表必须获得位置权限
     */
    public void requestLocationPermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//如果 API level 是小于等于 23(Android 6.0) 时 不需要显式申请权限
            return;
        }
        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            //判断是否需要向用户解释为什么需要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);
            }
            //请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * 开始扫描wifi信息
     * @param context
     */
    public void startScan(Context context){
        LocationManager locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // 未打开位置开关，可能导致定位失败或定位不准，提示用户或做相应处理
            Toast.makeText(context,"未打开GPS,可能无法扫描wifi", Toast.LENGTH_SHORT).show();
        }
        wifiScan = new WifiScan(context,communication);
        //开启wifiscan的扫描线程，每隔一段时间扫描一次
        wifiScan.start();
//        new WifiScan(context,communication).start();
    }
}

