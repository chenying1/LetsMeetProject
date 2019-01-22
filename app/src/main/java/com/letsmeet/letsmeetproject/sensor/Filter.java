package com.letsmeet.letsmeetproject.sensor;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;

public class Filter {
    private static int fs = 20;  //采样频率
    private static double v = 1.5; //下截止频率
    private static double v1 = 2.5; //上截止频率
    private static int n = 6; //滤波器的阶数
    //生成滤波器参数
    private static IirFilterCoefficients iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.bandpass, n,
            v / fs, v1 / fs);

    public static double[] filter(double[] value){
        //过滤
        value = IIRFilter(value, iirFilterCoefficients.a, iirFilterCoefficients.b);
        return value;
    }

    private static double[] IIRFilter(double[] signal, double[] a, double[] b) {
        double[] in = new double[b.length];
        double[] out = new double[a.length-1];
        double[] outData = new double[signal.length];

        for (int i = 0; i < signal.length; i++) {
            System.arraycopy(in, 0, in, 1, in.length - 1);
            in[0] = signal[i];
            //calculate y based on a and b coefficients
            //and in and out.
            float y = 0;
            for(int j = 0 ; j < b.length ; j++){
                y += b[j] * in[j];
            }
            for(int j = 0;j < a.length-1;j++){
                y -= a[j+1] * out[j];
            }
            //shift the out array
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            outData[i] = y;
        }
        return outData;
    }
}
