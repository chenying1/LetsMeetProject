package com.letsmeet.letsmeetproject.wifiInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.letsmeet.letsmeetproject.communicate.Communication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.WIFI_SERVICE;

public class WifiScan extends Thread{
    private WifiManager wifiManager;
    private static boolean flag = true;
    private Context context;
    private Communication communication;
    private HashMap<String,Integer> hashMap;
    private int status_wifiinfo = 1;

    private BroadcastReceiver mReceiver;

    public WifiScan(Context context, Communication communication){
        this.context = context;
        this.communication = communication;
        hashMap = new HashMap<>();
        register();
    }

    @Override
    public void run() {
        while (flag) {
            startWifiScan();
            try {
                Thread.sleep(5000);
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
                    List<ScanResult> results = wifiManager.getScanResults();
                    if (results !=null) {
                        JSONObject sendString = new JSONObject();
                        try {
                            sendString.put("status",status_wifiinfo);
                            Set<String> set = new HashSet<>();
                            for (ScanResult scanResult : results) {
//                                JSONObject msg = new JSONObject();

                                set.add(scanResult.BSSID);
//                                msg.put("SSID",scanResult.SSID);
//                                msg.put("BSSID",scanResult.BSSID);
//                                msg.put("level",scanResult.level);
//                                msg.put("time",scanResult.timestamp);
//                                sendString.put("data",msg.toString());
//
//                                String BSSID = scanResult.BSSID;
//                                int level = scanResult.level;
//                                if (!hashMap.containsKey(BSSID)){
//                                    hashMap.put(BSSID,level);
//                                    communication.send(sendString.toString());
//                                } else {
//                                    if (Math.abs(hashMap.get(BSSID)-level)>=10) {
//                                        hashMap.put(BSSID,level);
//                                        communication.send(sendString.toString());
//                                    } else {
//                                        Log.e("wifiscan","不需要更新的数据"+BSSID);
//                                    }
//                                }
//
//                                if (hashMap.size()>200){
//                                    hashMap.clear();
//                                }
                            }
                            sendString.put("data",set);
                            communication.send(sendString.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        IntentFilter filter =new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mReceiver, filter);
    }

    private void startWifiScan(){
        boolean scanResult =wifiManager.startScan(); //最好检查下返回值，因为这个方法可能会调用失败
//        Log.e("MainActivity1","startScan()执行成功: " + scanResult);
    }
}
