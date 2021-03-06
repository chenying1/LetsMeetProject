package com.letsmeet.letsmeetproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.gps.GpsInfo;
import com.letsmeet.letsmeetproject.http.HttpUtil;
import com.letsmeet.letsmeetproject.sendAllData.SendAllData;
import com.letsmeet.letsmeetproject.sensor.MySensorEventListener;
import com.letsmeet.letsmeetproject.sensor.OrientDetector;
import com.letsmeet.letsmeetproject.sensor.StepDetector;
import com.letsmeet.letsmeetproject.setting.MySettingActivity;
import com.letsmeet.letsmeetproject.wifiInfo.WifiScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements HttpUtil.HttpResponse {
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private Context context = this;
    private Communication communication;
    private TextView navigateView;
    private  MySensorEventListener listener;
    private MyView myView;
    private MyView otherView;

    private LocationView locationView;
    private WifiScan wifiScan;
    private GpsInfo gpsInfo;

    private OrientDetector orientDetector;
    private StepDetector stepDetector;
    private TextView stepDetectorView;
    private Dialog diaFinish;
    private ImageView imageView;
    private SharedPreferences sharedPreferences;
    private String TAG = "MainActivity";
    private SendAllData sendAllData = null;
    private Dialog beginNavigate;

    private HttpUtil httpUtil;
    String url = "navigateId";
    private static int navigateId;
    EditText navigateIdEdit;

    private String user;

    @Override
    public void httpResponseCallback(String responseResult) {
        Log.e(TAG,responseResult);
        JSONObject rcv;
        try {
            rcv = new JSONObject(responseResult);
            navigateId = rcv.getInt("navigateId");
            navigateIdEdit.setText(navigateId+"");
        } catch (JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        user = intent.getStringExtra("user");
        myView = (MyView) findViewById(R.id.myView);
        otherView = (MyView) findViewById(R.id.otherView);
        navigateView = (TextView) findViewById(R.id.navigation);
        locationView = (LocationView) findViewById(R.id.locationView);
        stepDetectorView = (TextView) findViewById(R.id.step);
        myView.setArrowColor(getResources().getColor(R.color.deepblue));
        listener = new MySensorEventListener(this,myView,locationView);
        listener.registerSensor();
        communication = new Communication(otherView,locationView,navigateView,user);
        requestLocationPermission();
        gpsInfo = new GpsInfo(this,communication);
        gpsInfo.register();
        wifiScan = new WifiScan(context,communication);
        wifiScan.start();
        orientDetector = new OrientDetector(myView,otherView,locationView,listener,communication);
        stepDetector = new StepDetector(listener,myView,communication,stepDetectorView);
        sendAllData = new SendAllData(listener,gpsInfo,wifiScan,orientDetector,user,myView);

        String packageName = this.getClass().getPackage().getName();
        sharedPreferences = getSharedPreferences(packageName+"_preferences", Context.MODE_PRIVATE); //创建SharedPreferences对象

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaFinish.show();
            }
        });
        Context context = MainActivity.this;
        diaFinish = new Dialog(context, R.style.meet_dialog_style);
        diaFinish.setContentView(R.layout.meet);
        ImageView imageView = (ImageView) diaFinish.findViewById(R.id.meet_img);
//        imageView.setBackgroundResource(R.mipmap.iv_android);
//        imageView.setAlpha(1);
        //选择true的话点击其他地方可以使dialog消失，为false的话不会消失
        diaFinish.setCanceledOnTouchOutside(true); // Sets whether this dialog is
        Window w = diaFinish.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        diaFinish.onWindowAttributesChanged(lp);
        imageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        diaFinish.dismiss();
                    }
                });
        initNavigate();
        httpUtil = new HttpUtil(url,this);

//        JSONObject ret = new JSONObject();
////        try {
////            ret.put("status",5);
////            ret.put("data",user);
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////        communication.send(ret.toString());

    }

    private void initNavigate(){
        beginNavigate = new Dialog(context,R.style.navigate_dailog);
        beginNavigate.setContentView(R.layout.navigate_begin);
        beginNavigate.setCanceledOnTouchOutside(true);
        Window w = beginNavigate.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        beginNavigate.onWindowAttributesChanged(lp);

        beginNavigate.setTitle("开始导航");

//        beginNavigate.setPositiveButton

        Button addInBtn = (Button) beginNavigate.findViewById(R.id.add_in_navigate);
        Button creatBtn = (Button) beginNavigate.findViewById(R.id.creat_navigate);

        navigateIdEdit = (EditText) beginNavigate.findViewById(R.id.navigateId);
        Log.e("Main4:",user);
        addInBtn.setOnClickListener(new View.OnClickListener() {
            JSONObject ret = new JSONObject();
            @Override
            public void onClick(View v) {
                Log.e(TAG,"点击了加入导航按钮");
                Log.e(TAG,"id:"+navigateIdEdit.getText().toString());
                navigateId = Integer.parseInt(navigateIdEdit.getText().toString());
                try {
                    ret.put("status",6);
                    JSONObject data = new JSONObject();
                    data.put("navigateId",navigateId);
                    data.put("user",user);
                    Log.e(TAG,data.toString()+" "+user);
                    ret.put("data",data);
                    Log.e(TAG,ret.toString());
                    communication.send(ret.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });
        creatBtn.setOnClickListener(new View.OnClickListener() {
            JSONObject ret = new JSONObject();
            @Override
            public void onClick(View v) {

                try {
                    ret.put("data","for navigateId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG,"点击了加入创建导航按钮"+ret.toString());
                httpUtil.sendMsg(ret.toString());
                Log.e(TAG,"id:"+navigateIdEdit.getText().toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isCollectData = sharedPreferences.getBoolean("dataCollectSwitch",false);
        Log.e(TAG,"isCollectData:"+isCollectData);

        int frequency=Integer.parseInt(sharedPreferences.getString("frequency","20"));//根据key获取对应的数据
        Log.e(TAG,"frequency:"+frequency);
        Set<String> parameters = sharedPreferences.getStringSet("parameter",new HashSet<String>());
        Log.e(TAG,"parameters:"+parameters);
        int stepLen = Integer.parseInt(sharedPreferences.getString("stepLength","10"));
        Log.e(TAG,"stepLen:"+stepLen);

        boolean isChanged = false;
        if (sendAllData!=null){
            if (sendAllData.getFrequency()!=frequency){
                sendAllData.setFrequency(frequency);
                isChanged = true;
            }
            if (sendAllData.getStepLenght()!=stepLen){
                sendAllData.setStepLenght(stepLen);
                isChanged = true;
            }
            if (!isSetEqual(sendAllData.getParameters(),parameters)){
                sendAllData.setParameters(parameters);
                isChanged = true;
            }
            if (sendAllData.isCollectData()!=isCollectData){
                sendAllData.setCollectData(isCollectData);
                Log.e(TAG,"sendAllData.isCollectData():"+sendAllData.isCollectData());
                isChanged = true;
            }

            if (isChanged){
                Log.e(TAG,"isChanged");
                if (isCollectData){
//                    若参数有变化，且是开启收集数据的状态，则需要重新启动收集数据的程序
                    sendAllData.startSendData();
                }else {
//                    若参数有变化，且是关闭的状态
                    sendAllData.finishSendData();
                }
            }
        }

    }

    private boolean isSetEqual(Set<String> set1, Set<String> set2){
        if (set1==null&&set2==null){
            return true;
        }
        if (set1==null||set2==null||set1.size()!=set2.size()){
            return false;
        }
        if (set1.size()==0&&set2.size()==0){
            return true;
        }
        Iterator iterator1 = set1.iterator();
        Iterator iterator2 = set2.iterator();
        while (iterator2.hasNext()){
            if (!set1.contains(iterator2.next())){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放传感器资源
        listener.unregisterOrient();
        orientDetector.timerCancel();
        stepDetector.timerCancel();
        wifiScan.unregister();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.setting:
//                Log.e("Main","点击了设置");
                Intent intent = new Intent(this,MySettingActivity.class);
                startActivityForResult(intent,0);
                break;
            case R.id.clear:
//                Log.e("Main","点击了清屏");
                myView.viewClear();
                otherView.viewClear();
                break;
            case R.id.over:
//                Log.e("Main","点击了结束");
                diaFinish.show();
                break;
            case R.id.beginNavigate:
                beginNavigate.show();
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void startWifiScan(Context context){
//        LocationManager locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            // 未打开位置开关，可能导致定位失败或定位不准，提示用户或做相应处理
//            Toast.makeText(context,"未打开GPS,可能无法扫描wifi", Toast.LENGTH_SHORT).show();
//        }
        wifiScan = new WifiScan(context,communication);
        //开启wifiscan的扫描线程，每隔一段时间扫描一次
        wifiScan.start();
    }
}

