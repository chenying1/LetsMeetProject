package com.letsmeet.letsmeetproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.letsmeet.letsmeetproject.gps.AngleUtil;

public class LocationView extends View {

    private Path arrowPath; //箭头的路径
    private int arrowR = 12; // 箭头半径
    private Paint arrowPaint;
    public Paint arrowPaint2;

    private Context context ;


    private int centerX;
    private int centerY;

    private int myLocationX;
    private int myLocationY;
    private int otherLocationX;
    private int otherLocationY;
    private int len = 100000;

    private float myDegree = 0;
    private float otherDegree = 90;

    private double LonA = 0;
    private double LatA = 0;
    private double LonB = 114.50923027;
    private double LatB = 30.51217833;

    private int viewWidth;
    private int viewHeight;
    private boolean isViewDouble = false;

    private double sita = 0;
    private float r = 100;  //半径

    private GestureDetector gestureDetector = null;
    private boolean isStart = false;

    private float translateX = 0;
    private float translateY = 0;

    private int lastX;
    private int lastY;

    private final String TAG = "LocationView";

    public LocationView(Context context) {
        super(context);
    }

    public LocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArrow(canvas);
        drawOtherArrow(canvas);
    }

    //onSizeChanged在onDraw之前执行
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        centerX = w/2;
        centerY = h/2;
        myLocationX = centerX;
        myLocationY = centerY;
    }

    private void init(){
        // 初始化箭头路径
        arrowPath = new Path();
        arrowPath.arcTo(new RectF(-arrowR, -arrowR, arrowR, arrowR), 0, -180);
        arrowPath.lineTo(0, (-2.2f * arrowR));
        arrowPath.close();

        // 画箭头画笔1
        arrowPaint = new Paint();
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(1);
        arrowPaint.setColor(Color.WHITE);
        // 画箭头画笔2
        arrowPaint2 = new Paint(Paint.DITHER_FLAG);
        arrowPaint2.setColor(getResources().getColor(R.color.red));
        arrowPaint2.setAntiAlias(true);
        arrowPaint2.setDither(true);

        otherLocationX = myLocationX;
        otherLocationY = myLocationY;

        gestureDetector = new GestureDetector(context,new GestureListener());
    }

    //绘制箭头
    private void drawArrow(Canvas canvas) {
        arrowPath.arcTo(new RectF(-arrowR, -arrowR, arrowR, arrowR), 0, -180);
        arrowPath.lineTo(0, (-2.2f * arrowR));
        arrowPath.close();

        canvas.save();
        canvas.translate(myLocationX, myLocationY); // 平移画布
        canvas.rotate(myDegree); // 转动画布
        //画箭头
        canvas.drawPath(arrowPath, arrowPaint2);
        canvas.drawCircle(0,0,arrowR,arrowPaint2);
        canvas.drawCircle(0,0,arrowR*0.9f,arrowPaint);
        canvas.restore(); // 恢复画布
    }

    //绘制箭头
    private void drawOtherArrow(Canvas canvas) {
        canvas.save();
        canvas.translate(otherLocationX, otherLocationY); // 平移画布
        canvas.rotate(otherDegree); // 转动画布
        //画箭头
        canvas.drawPath(arrowPath, arrowPaint2);
        canvas.drawCircle(0,0,arrowR,arrowPaint2);
        canvas.drawCircle(0,0,arrowR*0.9f,arrowPaint);
        canvas.restore(); // 恢复画布
    }

    //相对位置方向改变重绘
    public void locationChanged(){
        sita = AngleUtil.getAngle(LonA,LatA,LonB,LatB);
//        int x = (int) (len*(LonB - LonA));
//        int y = (int) (len*(LatB - LatA));
        int x = (int) (r * Math.sin(sita));
        int y = (int) (r * Math.cos(sita));
        otherLocationX = myLocationX + x;
        otherLocationY = myLocationY - y;
        Log.e(TAG,"LonA:"+this.LonA+" LatA:"+this.LatA+" LonB:"+this.LonB+" LatB:"+this.LatB+" x:"+x+" y:"+y);
        Log.e(TAG,"otherLocationX:"+otherLocationX+" otherLocationY:"+otherLocationY);
        Log.e(TAG,"sita:"+sita);
        postInvalidate();
    }

    public void setLocation(double LonA, double LatA, double LonB, double LatB){
        this.LonA = LonA;
        this.LatA = LatA;
//        this.LonB = LonB;
//        this.LatB = LatB;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //计算移动的距离
                int offX = x - lastX;
                int offY = y - lastY;
                //调用layout方法来重新放置它的位置
                layout(getLeft()+offX, getTop()+offY,
                        getRight()+offX    , getBottom()+offY);
                break;
        }
                gestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 实现界面的滑动平移
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isViewDouble = isViewDouble?false:true;
            viewTranslate();
            return super.onDoubleTap(e);
        }
    }

    private void viewTranslate(){
        int width;
        int height;
        if (isViewDouble){
            width = viewWidth*2;
            height = viewHeight*2;
        }else {
            width = viewWidth/2;
            height = viewHeight/2;
        }
        ConstraintLayout.LayoutParams focusItemParams = new ConstraintLayout.LayoutParams(width,height);
        this.setLayoutParams(focusItemParams);//focusView为你需要设置位置的VIEW
    }
}
