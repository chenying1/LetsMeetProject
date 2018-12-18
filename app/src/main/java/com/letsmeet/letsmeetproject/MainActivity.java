package com.letsmeet.letsmeetproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.gps.GpsCompare;
import com.letsmeet.letsmeetproject.gps.GpsInfo;
import com.letsmeet.letsmeetproject.gps.LocationCallback;
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.sensor.OrientCallback;
import com.letsmeet.letsmeetproject.sensor.StepDetectedCallback;
import com.letsmeet.letsmeetproject.setting.Config;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OrientCallback,StepDetectedCallback,LocationCallback {

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private Context context = this;
    private TextView stepDetector;
    private SensorManager sensorManager;
    private Communication communication;
    private TextView navigateView;

    private float rotateDegree;
    private float lastReceiveDegree;

    private  MySensorEventListener listener;
    private Timer orientSendTimer;
    private Timer orientReceiveTimer;
    private Timer navigateTimer;

    private MyView myView;
    private MyView otherView;

    private GpsCompare gpsCompare;

    double longitude = 0;
    double latitude = 0;
    double longitude_other = 0;
    double latitude_other = 0;

//    private Button other_addbtn;
////    private Button other_subtractbtn;

    private GpsInfo gpsInfo;

    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    String nowString = (String) msg.obj;
                    float now = Float.parseFloat(nowString);
//                    otherView.orientChanged(now);
                    RotateAnimation animation = new RotateAnimation
                            (lastReceiveDegree, now, Animation.RELATIVE_TO_SELF,
                                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setFillAfter(true);

//                    otherArrowImg.startAnimation(animation);
//                    otherOrientText.setText("对方角度:"+nowString);
                    break;
                case 1:
                    int stepCount = (int) msg.obj;
//                    stepDetector.setText("步数:"+stepCount);
                    break;
                case 2:   //更新导航提示
                    String string = (String) msg.obj;
                    navigateView.setText("目标在:"+string);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        other_addbtn = (Button) findViewById(R.id.other_add_btn);
//        other_subtractbtn = (Button) findViewById(R.id.other_subtract_btn);

//        stepDetector = (TextView) findViewById(R.id.stepDetector);

        myView = (MyView) findViewById(R.id.myView);
        otherView = (MyView) findViewById(R.id.otherView);

        navigateView = (TextView) findViewById(R.id.navigation);

        myView.setArrowColor(getResources().getColor(R.color.deepblue));
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        listener = new MySensorEventListener(this, this,this);
        listener.registerSensor();

        communication = new Communication(otherView);

        orientSendTimer = new Timer();
        orientReceiveTimer = new Timer();

        requestLocationPermission();
        startScan(context);

        startOrient();

        gpsInfo = new GpsInfo(context,communication);
        gpsCompare = new GpsCompare();

        startNavigate();
    }


    /**
     * 步行检测回调函数
     */
    @Override
    public void stepDetected(int stepCount) {
        handler.obtainMessage(1,stepCount).sendToTarget();
        myView.autoAddStep();
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("status",Config.STATUS_STEP_DETECT);
            data.put("curX",myView.getCurX());
            data.put("curY",myView.getCurY());
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        communication.send(jsonObject.toString());
    }

    //发送加速度
    //此方法用于测试，之后可能删除
    @Override
    public void stepDetected(String string) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status",Config.STATUS_ACCELERATE);
            jsonObject.put("data",string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        communication.send(jsonObject.toString());
    }

    /**
     * 导航提醒回调，当位置发生变化时
     * @param navigation
     */
    @Override
    public void navigationCallback(String navigation) {

    }

    /**
     * 传感器数据onChange时回调函数
     * @param orient
     * @param lastOrient
     */
    @Override
    public void orient(float orient, float lastOrient) {
        this.rotateDegree = orient;
        //获取手机朝向的角度
        RotateAnimation animation = new RotateAnimation
                (lastOrient, orient, Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
//        arrowImg.startAnimation(animation);
//        orientationText.setText("角度为："+orient);

        myView.orientChanged(orient);
    }

    private void startNavigate(){
        navigateTimer = new Timer();
        TimerTask navigateTask = new TimerTask() {
            @Override
            public void run() {
                if (gpsChanged(longitude,gpsInfo.longitude)||
                        gpsChanged(latitude,gpsInfo.latitude)||
                        gpsChanged(longitude_other,Communication.receiveLongitude)||
                        gpsChanged(latitude_other,Communication.receiveLatitude)){
                    longitude = gpsInfo.longitude;
                    latitude = gpsInfo.latitude;
                    longitude_other = Communication.receiveLongitude;
                    latitude_other = Communication.receiveLatitude;
                    String navigateString = gpsCompare.compareBtoA(longitude,latitude,longitude_other,latitude_other);
                    if (navigateString!=null){
                        handler.obtainMessage(2,navigateString).sendToTarget();
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


    /**
     * 开始发数据、收数据
     */
    private void startOrient(){
        //定时器每隔1s查看收到的数据
        orientReceiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Float now = Float.parseFloat(Communication.receiveOrient);
                if (Math.abs(lastReceiveDegree - now)>1){
//                    handler.obtainMessage(0,Float.toString(now)).sendToTarget();
                    otherView.orientChanged(now);
                    lastReceiveDegree = now;
                }
                int stepCountNew = Communication.otherStepCount;
            }
        }, Config.delay, Config.period);

        //定时器每隔1s发数据 角度
        orientSendTimer.schedule(new TimerTask() {
            float lastRotateDegree = 0;
            public void run() {
                if (Math.abs(rotateDegree - lastRotateDegree) > 1){
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
        }, Config.delay, Config.period);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientSendTimer!=null){
            orientSendTimer.cancel();
        }

        if (orientReceiveTimer!=null){
            orientReceiveTimer.cancel();
        }

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
                    1);
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
        new WifiScan(context,communication).start();
    }
}

