package com.letsmeet.letsmeetproject.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MySensorEventListener implements SensorEventListener {

    float[] accelerometerValues = new float[3];
    float[] magneticValues = new float[3];
    int step_count = 0;
    SensorManager sensorManager;
    Context context;
    float rotateDegree = 0;
    float lastRotateDegree = 0;
    OrientCallback orientCallback;
    StepDetectedCallback stepDetectedCallback;
    List<Float> list = new ArrayList<>();
    Timer accelerTimer = new Timer();
    TimerTask task = null;
    double acValues = 0;
    int len = 50;
    private final String TAG = "MySensorEventListener";

    public MySensorEventListener(Context context, OrientCallback orientCallback, StepDetectedCallback stepDetectedCallback){
        this.context = context;
        this.orientCallback = orientCallback;
        this.stepDetectedCallback = stepDetectedCallback;
    }

    public MySensorEventListener(Context context, OrientCallback orientCallback){
        this.context = context;
        this.orientCallback = orientCallback;
    }

    /**
    *  注册传感器
     */
    public void registerSensor(){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //地磁传感器
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //加速度传感器
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //单次计步传感器
        Sensor stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //计步总数传感器
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//获取计步总数传感器
        //陀螺仪传感器
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //注册SensorEventListener使其生效
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);


        task = new TimerTask() {
            @Override
            public void run() {
                //加速度
                if (acValues>0) {
                    list.add((float) acValues);
                    if (list.size()>=len*2) {
                        len = list.size();
                        stepDetectedCallback.stepDetected(list.toString());
                    }
                }
            }
        };
        accelerTimer.schedule(task,0,50);
    }



    /**
     * 注销方向监听器
     */
    public void unregisterOrient() {
        if (sensorManager != null){
            sensorManager.unregisterListener(this);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //加速度和地磁场    步行检测
        int sensorType = event.sensor.getType();
        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
                double ac = Math.pow(accelerometerValues[0],2)+Math.pow(accelerometerValues[1],2)+Math.pow(accelerometerValues[2],2);
                acValues = Math.pow(ac,0.5);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                if (event.values[0]==1.0f){
                    step_count++;
                    stepDetectedCallback.stepDetected(step_count);
//                Log.e("TAG","检测到一个步伐了");
                }
//            stepDetector.setText("步数:"+event.values[0]);
                break;
            case Sensor.TYPE_GYROSCOPE:
//                Log.e(TAG,"陀螺仪的数据为："+"x:"+event.values[0]+",y:"+event.values[1]+",z:"+event.values[2]);
                break;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometerValues = event.values.clone();
            double ac = Math.pow(accelerometerValues[0],2)+Math.pow(accelerometerValues[1],2)+Math.pow(accelerometerValues[2],2);
            acValues = Math.pow(ac,0.5);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magneticValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if (event.values[0]==1.0f){
                step_count++;
                stepDetectedCallback.stepDetected(step_count);
//                Log.e("TAG","检测到一个步伐了");
            }
//            stepDetector.setText("步数:"+event.values[0]);
        }

        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R, values);
        //获取手机朝向的角度
        rotateDegree = (float) Math.toDegrees(values[0]);

        if (isOrientChange()) {
            orientCallback.orient(rotateDegree, lastRotateDegree);
            lastRotateDegree = rotateDegree;
        }
    }

    public boolean isOrientChange(){
        if (Math.abs(rotateDegree - lastRotateDegree) > 1) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
