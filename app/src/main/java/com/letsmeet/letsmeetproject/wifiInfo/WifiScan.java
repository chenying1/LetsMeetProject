package com.letsmeet.letsmeetproject.wifiInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.letsmeet.letsmeetproject.communicate.Communication;
import com.letsmeet.letsmeetproject.setting.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.WIFI_SERVICE;

public class WifiScan extends Thread{
    public WifiManager wifiManager;
    private static boolean flag = true;
    private Context context;
    private Communication communication;
    private BroadcastReceiver mReceiver;
    public List<ScanResult> results = new ArrayList<>();
    private final String TAG = "WifiScan";
    public ArrayList<String> wifilist = new ArrayList<>();

    public WifiScan(Context context, Communication communication){
        this.context = context;
        this.communication = communication;
        register();
//        startWifiScan();
    }

    @Override
    public void run() {
        while (flag) {
            Log.e(TAG,"run");
            startWifiScan();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 注册广播，扫描wifi
     */
    private void register() {
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    results = wifiManager.getScanResults();
                    if (results !=null) {
                        JSONObject sendString = new JSONObject();
                        try {
                            sendString.put("status",Config.STATUS_WIFI);
                            wifilist.clear();
                            for (ScanResult scanResult : results) {
                                JSONObject wifi = new JSONObject();
                                wifi.put("BSSID",scanResult.BSSID);
                                wifi.put("level",scanResult.level);
                                wifilist.add(wifi.toString());
                            }
                            Log.e(TAG,"results:"+results.size()+" "+results);
                            sendString.put("data",serializeToString(wifilist));
                            communication.send(sendString.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
//                startWifiScan();
            }
        };

        IntentFilter filter =new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mReceiver, filter);
    }

    //序列化
    public String serializeToString(Object obj) throws Exception{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(obj);
        String str = byteOut.toString("ISO-8859-1");//此处只能是ISO-8859-1,但是不会影响中文使用
        return str;
    }

    private void startWifiScan(){
        boolean scanResult =wifiManager.startScan(); //最好检查下返回值，因为这个方法可能会调用失败
        Log.e(TAG,"startScan()执行成功: " + scanResult);
    }


}
