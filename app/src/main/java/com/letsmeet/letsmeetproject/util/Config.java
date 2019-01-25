package com.letsmeet.letsmeetproject.util;

public class Config {
    //服务器ip地址、端口号
    public static final String SERVER_IP = "222.20.73.169";
//    public static final String SERVER_IP = "39.105.206.148";
    public static final int SERVER_PORT = 1234;
    public static final int SERVER_PORT_SENDALLDATA = 1235;

    //定时器周期ms
    public static final int period = 1000;
    //定时器延迟ms
    public static final int delay = 0;

    //发送至服务器的status
    //角度
    public static final int STATUS_ORIENT = 0;
    //wifi
    public static final int STATUS_WIFI = 1;
    //计步检测
    public static final int STATUS_STEP_DETECT = 2;
    //计步检测 之 加速度
    public static final int STATUS_ACCELERATE = 3;
    //GPS 经纬度
    public static final int STATUS_LOCATION = 4;

}
