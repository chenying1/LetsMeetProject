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
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.sensor.OrientCallback;
import com.letsmeet.letsmeetproject.sensor.StepDetectedCallback;
import com.letsmeet.letsmeetproject.setting.Config;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity1 extends AppCompatActivity implements OrientCallback,StepDetectedCallback {

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private Context context = this;

    //    private ImageView arrowImg;
//    private ImageView otherArrowImg;
//    private TextView orientationText;
//    private TextView otherOrientText;
    private TextView stepDetector;
    private SensorManager sensorManager;
    private Communication communication;

    private float rotateDegree;
    private float lastReceiveDegree;

    private  MySensorEventListener listener;
    private Timer orientSendTimer;
    private Timer orientReceiveTimer;

    private MyView myView;
    private MyView otherView;

    private Button other_addbtn;
    private Button other_subtractbtn;

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
                    stepDetector.setText("步数:"+stepCount);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        other_addbtn = (Button) findViewById(R.id.other_add_btn);
        other_subtractbtn = (Button) findViewById(R.id.other_subtract_btn);


//        arrowImg = (ImageView) findViewById(R.id.arrow_img);
//        otherArrowImg = (ImageView) findViewById(R.id.other_arrow_img);
//        orientationText = (TextView) findViewById(R.id.orientation);
//        otherOrientText = (TextView) findViewById(R.id.other);
        stepDetector = (TextView) findViewById(R.id.stepDetector);

        myView = (MyView) findViewById(R.id.myView);
        otherView = (MyView) findViewById(R.id.otherView);

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

        other_addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherView.bigger();
            }
        });
        other_subtractbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherView.smaller();
            }
        });
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
            jsonObject.put("status",2);

            data.put("curX",myView.getCurX());
            data.put("curY",myView.getCurY());
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        communication.send(jsonObject.toString());
    }

    @Override
    public void stepDetected(String string) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status",3);
            jsonObject.put("data",string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        communication.send(jsonObject.toString());
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

    /**
     * 开始发数据、收数据
     */
    private void startOrient(){
        //定时器每隔1s查看收到的数据
        orientReceiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Float now = Float.parseFloat(Communication.receive);
                if (Math.abs(lastReceiveDegree - now)>1){
//                    handler.obtainMessage(0,Float.toString(now)).sendToTarget();
                    otherView.orientChanged(now);
                    lastReceiveDegree = now;
                }

                int stepCountNew = Communication.otherStepCount;


            }
        }, Config.delay, Config.period);

        //定时器每隔1s发数据
        orientSendTimer.schedule(new TimerTask() {
            float lastRotateDegree = 0;
            public void run() {
                if (Math.abs(rotateDegree - lastRotateDegree) > 1){
                    lastRotateDegree = rotateDegree;
                    JSONObject sendString = new JSONObject();
                    int status = 0;
                    String data = Float.toString(rotateDegree);
                    try {
                        sendString.put("status",status);
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//如果 API level 是小于等于 23(Android 6.0) 时 不需要申请权限
            return;
        }

        String[] PERMS_INITIAL={Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(PERMS_INITIAL,127);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //判断是否需要向用户解释为什么需要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);
            }
            //请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
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
        new WifiScan(context,communication).start();
    }
}

