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

import com.letsmeet.letsmeetproject.util.Config;
import com.letsmeet.letsmeetproject.util.NavigateTip;

public class LocationView extends View {

    private Path arrowPath; //箭头的路径
    private int arrowR = 11; // 箭头半径
    private Paint arrowPaint;
    private Paint arrowPaint2;
    private Paint circlePaint;  //外部画圆的笔
    private Paint last_circlePaint;
    private Context context ;

    private Path arrowPath_other = new Path();
    private Path arrowPath_Self = new Path();

    private int centerX;
    private int centerY;

    private int myLocationX;
    private int myLocationY;
    private int otherLocationX;
    private int otherLocationY;

    private float myDegree = 0;
    private float otherDegree = 0;

    private double lonA = 0;
    private double latA = 0;
    private double lonB = 0;
    private double latB = 0;

    private int viewWidth;
    private int viewHeight;
    private boolean isViewDouble = false;

    private final int DEFAULT_R = 115;
    private final int MAX_R = 180;
    private final int Min_R = 50;

    private int circle_r = DEFAULT_R;  //外边框的圆半径
    private int last_circle_r = DEFAULT_R;

    private int dis = 20;

//    private GestureDetector gestureDetector = null;

    private int lastX;
    private int lastY;

    private int accuracy;

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

//    绘制准确度   目前用不上
    private void drawText(Canvas canvas){
        String text = accuracy+"";
        arrowPaint.setTextAlign(Paint.Align.CENTER);
        arrowPaint.setTextSize(30);
        canvas.drawText(text,centerX,centerY*3/2,arrowPaint);
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
        otherLocationX = myLocationX;
        otherLocationY = myLocationY-circle_r;
    }

    public void myDegreeChanged(float myDegreeNew){
        if (Math.abs(myDegreeNew-myDegree)>1) {
            this.myDegree = myDegreeNew;
            postInvalidate();
        }
    }

    public void otherDegreeChanged(float otherDegreeNew){
        if (Math.abs(otherDegreeNew - otherDegree)>1) {
            this.otherDegree = otherDegreeNew;
            postInvalidate();
        }
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
//        getResources().getColor(R.color.deepblue)
        arrowPaint2.setColor(Color.BLUE);
        arrowPaint2.setAntiAlias(true);
        arrowPaint2.setDither(true);

//        外部画圆的画笔
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3);
        circlePaint.setColor(Color.BLUE);

        last_circlePaint = new Paint();
        last_circlePaint.setStyle(Paint.Style.STROKE);
        last_circlePaint.setStrokeWidth(3);
        last_circlePaint.setColor(Color.GRAY);

        arrowPath_Self.arcTo(new RectF(-arrowR, -arrowR, arrowR, arrowR), 0, -180);
        arrowPath_Self.lineTo(0, (-2.4f * arrowR));
        arrowPath_Self.close();

        arrowPath_other.arcTo(new RectF(-arrowR, -arrowR, arrowR, arrowR), 0, -180);
        arrowPath_other.lineTo(0, (-2.2f * arrowR));
        arrowPath_other.close();

//        gestureDetector = new GestureDetector(context,new GestureListener());
    }

    //绘制箭头
    private void drawArrow(Canvas canvas) {
        canvas.save();
        canvas.translate(myLocationX, myLocationY); // 平移画布
        canvas.rotate(myDegree); // 转动画布

        //画外部圆圈
        canvas.drawCircle(0,0,circle_r,circlePaint);
        canvas.drawCircle(0,0,last_circle_r,last_circlePaint);
        //画箭头
        arrowPaint2.setColor(getResources().getColor(R.color.deepblue));
        canvas.drawPath(arrowPath_Self, arrowPaint2);
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
        arrowPaint2.setColor(getResources().getColor(R.color.red));
        canvas.drawPath(arrowPath_other, arrowPaint2);
        canvas.drawCircle(0,0,arrowR,arrowPaint2);
        canvas.drawCircle(0,0,arrowR*0.9f,arrowPaint);
        canvas.restore(); // 恢复画布
    }

    //相对位置方向改变重绘
    public void locationChanged(int sitaNew,int distanceStatus,int tipStatus){
        switch (distanceStatus){
            case NavigateTip.FAR_AWAY:
//                远离
                if (circle_r<DEFAULT_R){
                    circle_r = DEFAULT_R;
                }
                if (circle_r+dis<=MAX_R){
                    circle_r = circle_r+dis;
                }
                circlePaint.setColor(NavigateTip.getColor(distanceStatus));
                break;
            case NavigateTip.NEAR:
                if (circle_r>DEFAULT_R){
                    circle_r = DEFAULT_R;
                }
                if (circle_r-dis>=Min_R){
                    circle_r = circle_r-dis;
                }
                circlePaint.setColor(NavigateTip.getColor(distanceStatus));
                break;
            default:
                circlePaint.setColor(Color.GRAY);
                circle_r = last_circle_r;
        }
        if (isOut(last_circle_r)||isOut(circle_r)){
            last_circle_r = DEFAULT_R - (circle_r - last_circle_r);
            circle_r = DEFAULT_R;
        }

//        double sita = AngleUtil.getAngle(lonA,latA,lonB,latB);
        double sita = sitaNew;
        this.accuracy = accuracy;
        int x = (int) (circle_r * Math.sin(Math.toRadians(sita)));
        int y = (int) (circle_r * Math.cos(Math.toRadians(sita)));
        otherLocationX = myLocationX + x;
        otherLocationY = myLocationY - y;
        Log.e(TAG,"LonA:"+this.lonA+" LatA:"+this.latA+" LonB:"+this.lonB+" LatB:"+this.latB+" x:"+x+" y:"+y);
        Log.e(TAG,"otherLocationX:"+otherLocationX+" otherLocationY:"+otherLocationY);
        Log.e(TAG,"myLocationX:"+myLocationX+" myLocationY:"+myLocationY);
        Log.e(TAG,"sita:"+sita);
        postInvalidate();
    }
    private boolean isOut(int r){
        if (r<Min_R||r>MAX_R){
            return true;
        }
        return false;
    }

    public void setLocation(double lonA, double latA, double lonB, double latB){
        this.lonA = lonA;
        this.latA = latA;
        this.lonB = lonB;
        this.latB = latB;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//
//        switch(event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                lastX = x;
//                lastY = y;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //计算移动的距离
//                int offX = x - lastX;
//                int offY = y - lastY;
//                //调用layout方法来重新放置它的位置
//                layout(getLeft()+offX, getTop()+offY,
//                        getRight()+offX    , getBottom()+offY);
//                break;
//        }
////                gestureDetector.onTouchEvent(event);
//        return true;
//    }

//    /**
//     * 实现手指双击放大缩小   已弃用
//     */
//    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            isViewDouble = isViewDouble?false:true;
//            viewTranslate();
//            return super.onDoubleTap(e);
//        }
//    }

//    private void viewTranslate(){
//        int width;
//        int height;
//        if (isViewDouble){
//            width = viewWidth*2;
//            height = viewHeight*2;
//        }else {
//            width = viewWidth/2;
//            height = viewHeight/2;
//        }
//        ConstraintLayout.LayoutParams focusItemParams = new ConstraintLayout.LayoutParams(width,height);
//        this.setLayoutParams(focusItemParams);//focusView为你需要设置位置的VIEW
//    }
}
