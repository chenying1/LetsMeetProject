package com.letsmeet.letsmeetproject.wifiInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WifilistCompare {
    private ArrayList<String> myWifilist;
    private ArrayList<String> otherWifilist;

    public WifilistCompare(){

    }

    /**
     * 获取自身和对方wifi列表的BSSID并集个数
     * @param myWifilist
     * @param otherWifilist
     * @return
     */
    private int wifiUnionNum(ArrayList<String> myWifilist,ArrayList<String> otherWifilist) throws JSONException{
        Set<String> wifiBssidUnion = new HashSet<>();
        JSONObject wifijson;
        for (String s : myWifilist){
                wifijson = new JSONObject(s);
                wifiBssidUnion.add(wifijson.getString("BSSID"));
        }
        for (String s : otherWifilist){
                wifijson = new JSONObject(s);
                wifiBssidUnion.add(wifijson.getString("BSSID"));
        }
        return wifiBssidUnion.size();
    }

    /**
     * 获取自身和对方wifi列表的BSSID交集个数
     * @param myWifilist
     * @param otherWifilist
     */
    private int wifiIntersection(ArrayList<String> myWifilist, ArrayList<String> otherWifilist) throws JSONException {
        JSONObject mWifiJson;
        JSONObject oWifiJson;
        int num = 0;
        for (String ms : myWifilist){
            mWifiJson = new JSONObject(ms);
            String mBSSID = mWifiJson.getString("BSSID");
            for (String os : otherWifilist){
                oWifiJson = new JSONObject(os);
                String oBSSID = oWifiJson.getString("BSSID");
                if (mBSSID.equals(oBSSID)){
                    num++;
                }
            }
        }
        return num;
    }

    public float getIntersectionUnionRatio(ArrayList<String> myWifilist, ArrayList<String> otherWifilist){
        float result = 0;
        try {
            int intersection = wifiIntersection(myWifilist,otherWifilist);
            int union = wifiUnionNum(myWifilist,otherWifilist);
            if (union!=0){
                result = (float) intersection/union;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


}
