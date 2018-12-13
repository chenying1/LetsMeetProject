package com.letsmeet.letsmeetproject.gprsInfo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GetGPRSInfo {
    private Context context;
    TelephonyManager mTelephonyManager;
    List<CellInfo> infos;
    private final String TAG = "GetGPRSInfo";
    Timer timer = new Timer();
    TimerTask task = null;

    public GetGPRSInfo(Context context){
        this.context = context;
        task = new TimerTask() {
            @Override
            public void run() {
                getBaseStationInformation();
            }
        };
        timer.schedule(task,0,3000);

    }
    /**
     * 获取 基站 信息
     * @return
     */
    public String getBaseStationInformation(){
        if(mTelephonyManager==null){
            mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        // 返回值MCC + MNC （注意：电信的mnc 对应的是 sid）
        String operator = mTelephonyManager.getNetworkOperator();
        int mcc = -1;
        int mnc = -1;
        if(operator!=null&&operator.length()>3){
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3));
        }
        Log.e(TAG,"mcc:"+mcc+"  mnc:"+mnc);

        if (infos!=null){
            infos.clear();
        }

        // 获取邻区基站信息
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            infos = mTelephonyManager.getAllCellInfo();
        }

        StringBuffer sb = new StringBuffer("邻区基站总数 : " + infos.size() + "\n");

        for (CellInfo info1 : infos) { // 根据邻区总数进行循环

            if (info1 instanceof CellInfoLte){
                CellInfoLte info = (CellInfoLte) info1;
                CellIdentityLte cellIdentity = info.getCellIdentity();
                CellSignalStrengthLte cellSignalStrengthLte = info.getCellSignalStrength();

                sb.append(" 基站信息 : " ); // 取出当前邻区的LAC
                sb.append("\ndbm:"+ cellSignalStrengthLte.getDbm());
                sb.append("\nlevel:"+cellSignalStrengthLte.getLevel());

            }

//            sb.append("\n 邻区CID : " + info1.getCid()); // 取出当前邻区的CID
//            sb.append("\n 邻区基站信号强度BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度
        }


        int type = mTelephonyManager.getNetworkType();
       if(type == TelephonyManager.NETWORK_TYPE_CDMA        // 电信cdma网
                || type == TelephonyManager.NETWORK_TYPE_1xRTT
                || type == TelephonyManager.NETWORK_TYPE_EVDO_0
                || type == TelephonyManager.NETWORK_TYPE_EVDO_A
               || type == TelephonyManager.NETWORK_TYPE_EVDO_B
               || type == TelephonyManager.NETWORK_TYPE_LTE){

       }

//        Toast.makeText(context,"type:= "+type,Toast.LENGTH_LONG).show();
        //需要判断网络类型，因为获取数据的方法不一样
//        if(type == TelephonyManager.NETWORK_TYPE_CDMA        // 电信cdma网
//                || type == TelephonyManager.NETWORK_TYPE_1xRTT
//                || type == TelephonyManager.NETWORK_TYPE_EVDO_0
//                || type == TelephonyManager.NETWORK_TYPE_EVDO_A
//                || type == TelephonyManager.NETWORK_TYPE_EVDO_B
//                || type == TelephonyManager.NETWORK_TYPE_LTE){
//            CdmaCellLocation cdma = (CdmaCellLocation) mTelephonyManager.getCellLocation();
//            if(cdma!=null){
//                sb.append(" MCC = " + mcc );
//                sb.append("\n cdma.getBaseStationLatitude()"+cdma.getBaseStationLatitude()/14400 +"\n"
//                        +"cdma.getBaseStationLongitude() "+cdma.getBaseStationLongitude()/14400 +"\n"
//                        +"cdma.getBaseStationId()(cid)  "+cdma.getBaseStationId()
//                        +"\n  cdma.getNetworkId()(lac)   "+cdma.getNetworkId()
//                        +"\n  cdma.getSystemId()(mnc)   "+cdma.getSystemId());
//            }else{
//                sb.append("can not get the CdmaCellLocation");
//            }
//
//        }else if(type == TelephonyManager.NETWORK_TYPE_GPRS         // 移动和联通GSM网
//                || type == TelephonyManager.NETWORK_TYPE_EDGE
//                || type == TelephonyManager.NETWORK_TYPE_HSDPA
//                || type == TelephonyManager.NETWORK_TYPE_UMTS
//                || type == TelephonyManager.NETWORK_TYPE_LTE){
//            GsmCellLocation gsm = (GsmCellLocation) mTelephonyManager.getCellLocation();
//            if(gsm!=null){
//                sb.append("  gsm.getCid()(cid)   "+gsm.getCid()+"  \n "//移动联通 cid
//                        +"gsm.getLac()(lac) "+gsm.getLac()+"  \n "             //移动联通 lac
//                        +"gsm.getPsc()  "+gsm.getPsc());
//            }else{
//                sb.append("can not get the GsmCellLocation");
//            }
//        }else if(type == TelephonyManager.NETWORK_TYPE_UNKNOWN){              //未知
//            Toast.makeText(context,"电话卡不可用！",Toast.LENGTH_LONG).show();
//        }
        Log.e(TAG,"type: "+type);
        Log.e(TAG, " 获取邻区基站信息:" + sb.toString());
        return sb.toString();
    }

}
