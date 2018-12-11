package com.letsmeet.letsmeetproject.wifiInfo;

public class WifiEntity {

    private String SSID;   //网络名称
    private String BSSID;  //MAC地址
    private int level;  //信号衰减强度

    private String Capabilities; //加密方案
    private String Frequency;  //频率
    private String Timestamp;  //时间戳

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
