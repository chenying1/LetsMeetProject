package com.letsmeet.letsmeetproject.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.letsmeet.letsmeetproject.MyView;

public class MySensorEventListener implements SensorEventListener {
    public float[] accelerometerValues = new float[3];
    public float[] magneticValues = new float[3];
    public float[] gyroscopeValues = new float[3];
    public float pressure;
    public float[] angleValues = new float[3];

    private SensorManager sensorManager;
    private Context context;
    public float rotateDegree = 0;
    private float lastRotateDegree = 0;
    private double acValues = 0;
    private long timestamp;
    public Crest crest = new Crest();  //加速度传感器当前的值
    private final String TAG = "MySensorEventListener";
    private MyView myView;

    public MySensorEventListener(Context context, MyView myView){
        this.context = context;
        this.myView = myView;
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
        //气压计
        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        //注册SensorEventListener使其生效
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI);
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
                timestamp = event.timestamp;
                double ac = Math.pow(accelerometerValues[0],2)+Math.pow(accelerometerValues[1],2)+Math.pow(accelerometerValues[2],2);
                acValues = Math.pow(ac,0.5);
                crest.setValue(acValues);
                crest.setTimestamp(timestamp);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                //计步检测  Android自带的计步检测传感器，由于不灵敏，
                break;
            case Sensor.TYPE_GYROSCOPE:
                //陀螺仪传感器的数据  后期可能要用上
                gyroscopeValues = event.values.clone();
//                Log.e(TAG,"陀螺仪的数据为："  +"x:"+event.values[0]+",y:"+event.values[1]+",z:"+event.values[2]);
                break;
            case Sensor.TYPE_PRESSURE:
                pressure = event.values[0];
                break;
        }
        calculateDegree();
    }

    private void calculateDegree(){
        //计算手机朝向
        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R, values);
        angleValues = values.clone();
        //获取手机朝向的角度
        rotateDegree = (float) Math.toDegrees(values[0]);
        if (isOrientChange()) {
            myView.orientChanged(rotateDegree);
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
