package com.letsmeet.letsmeetproject.util;

import android.graphics.Color;

public class NavigateTip {
    //	未知
    public final static int UNKNOW = 0;
    private static String UNKNOW_S = "等待连接......";
    //正北
    public final static int NORTH = 1;
    private static String NORTH_S = "目标位于正北方";
    //	正南
    public final static int SOUTH = 2;
    private static String SOUTH_S = "目标位于正南方";
    //	正东
    public final static int EAST = 3;
    private static String EAST_S = "目标位于正东方";
    //	正西
    public final static int WEST = 4;
    private static String WEST_S = "目标位于正西方";
    //	东北
    public final static int NORTH_EAST = 5;
    private static String NORTH_EAST_S = "目标位于东北方";
    //	东南
    public final static int SOUTH_EAST = 6;
    private static String SOUTH_EAST_S = "目标位于东南方";
    //	西北
    public final static int NORTH_WEST = 7;
    private static String NORTH_WEST_S = "目标位于西北方";
    //	西南
    public final static int SOUTH_WEST = 8;
    private static String SOUTH_WEST_S = "目标位于西南方";
    //	正在远离
    public final static int FAR_AWAY = 9;
    private static String FAR_AWAY_S = "正在远离目标，请改变当前方向前行";
    //	正在靠近
    public final static int NEAR = 10;
    private static String NEAR_S = "正在靠近目标，请保持当前方向继续前进";
    //	继续前进
    public final static int KEEP = 11;
    private static String KEEP_S = "无法判断，请继续前进以试探周围方向";
    //	相遇
    public final static int MEET = 12;
    private static String MEET_S = "相遇";

    private static int RED = Color.parseColor("#FF6347");
    private static int GREEN = Color.parseColor("#3cb371");
    private static int PURPLE = Color.parseColor("#7b68ee");
    private static int BLUE = Color.parseColor("#4682b4");
    private static int YELLOW = Color.parseColor("#ff8c00");

    public static String getTip(int status){
        switch (status){
            case UNKNOW:
                return UNKNOW_S;
            case NORTH:
                return NORTH_S;
            case SOUTH:
                return SOUTH_S;
            case EAST:
                return EAST_S;
            case WEST:
                return WEST_S;
            case NORTH_EAST:
                return NORTH_EAST_S;
            case SOUTH_EAST:
                return SOUTH_EAST_S;
            case NORTH_WEST:
                return NORTH_WEST_S;
            case SOUTH_WEST:
                return SOUTH_WEST_S;
            case FAR_AWAY:
                return FAR_AWAY_S;
            case NEAR:
                return NEAR_S;
            case KEEP:
                return KEEP_S;
            case MEET:
                return MEET_S;
        }
        return null;
    }

    public static int getColor(int status){
        switch (status){
            case UNKNOW:
                return BLUE;
            case NORTH:
                return PURPLE;
            case SOUTH:
                return PURPLE;
            case EAST:
                return PURPLE;
            case WEST:
                return PURPLE;
            case NORTH_EAST:
                return PURPLE;
            case SOUTH_EAST:
                return PURPLE;
            case NORTH_WEST:
                return PURPLE;
            case SOUTH_WEST:
                return PURPLE;
            case FAR_AWAY:  //9
                return RED;
            case NEAR:  //10
                return GREEN;
            case KEEP:  //11
                return GREEN;
            case MEET:
                return YELLOW;
        }
        return 0;
    }
}
