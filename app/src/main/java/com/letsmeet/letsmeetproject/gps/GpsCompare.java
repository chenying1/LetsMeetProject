package com.letsmeet.letsmeetproject.gps;

public class GpsCompare {

    private static double longitude;
    private static double latitude;

    public static double otherLongitude;
    public static double otherLatitude;

    private boolean isEqual(double f1, double f2){
        if (Math.abs(f1-f2)<0.001){
            return true;
        }
        return false;
    }

    private boolean isEqual(double dist){
        if (Math.abs(dist)<=0.0001){
            return true;
        }
        return false;
    }
    private boolean moreThan(double dist){
        if (dist>0.0001){
            return true;
        }
        return false;
    }
    private boolean lessThan(double dist){
        if (dist<-0.0001){
            return true;
        }
        return false;
    }


    public String compareBtoA(double longitudeA, double latitudeA, double longitudeB, double latitudeB){

        if (isEqual(longitudeA,0)||isEqual(latitudeA,0)||isEqual(longitudeB,0)||isEqual(latitudeB,0)){
            return null;
        }

        double lon_dist = longitudeB - longitudeA;
        double lat_dist = latitudeB - latitudeA;

        if (isEqual(lon_dist)){
            if (moreThan(lat_dist)){
                return "正北";
            }else if (lessThan(lat_dist)){
                return "正南";
            }
        }else if (moreThan(lon_dist)){
            if (isEqual(lat_dist)){
                return "东";
            }else if (moreThan(lat_dist)){
                return "东北";
            }else if (lessThan(lat_dist)){
                return "东南";
            }
        }else if (lessThan(lon_dist)){
            if (isEqual(lat_dist)){
                return "西";
            } else if (moreThan(lat_dist)){
                return "西北";
            } else if (lessThan(lat_dist)){
                return "西南";
            }
        }
        return "相遇";
    }

}
