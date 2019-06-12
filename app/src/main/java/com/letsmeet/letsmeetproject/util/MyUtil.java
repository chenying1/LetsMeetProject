package com.letsmeet.letsmeetproject.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MyUtil {

    //序列化
    public static String serializeToString(Object obj){
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        String str = null;
        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            str = byteOut.toString("ISO-8859-1");//此处只能是ISO-8859-1,但是不会影响中文使用
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
