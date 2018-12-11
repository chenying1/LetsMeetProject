package com.letsmeet.letsmeetproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyView extends View {

    private Path arrowPath; //箭头的路径
    private int arrowR = 15; // 箭头半径
    private Paint arrowPaint;
    public Paint arrowPaint2;

    private Path path;  //轨迹
    private List<PointF> pointFS = new ArrayList<>();
    private int cR = 5; // 圆点半径
    private Paint pathPaint; //画轨迹的画笔

    private float curX;
    private float curY;
    private float preX;
    private float preY;

    private float translateX = 0;
    private float translateY = 0;

    private int centerX;
    private int centerY;

    private float degree;      //箭头角度
    private int stepLen = 10;  //步长

    private boolean isStart = false;


    private Timer timer = new Timer();
    private TimerTask task;

    private float scale = 1.0f;

    private GestureDetector mScaleGestureDetector = null;

    private Context context ;

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setPaintColor(int color) {
        arrowPaint2.setColor(color);
        invalidate();
    }

    private void init(){
        path = new Path();
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
        //画轨迹画笔
        pathPaint = new Paint(Paint.DITHER_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.BLACK);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setStrokeWidth(3);
        pathPaint.setAntiAlias(true);

        mScaleGestureDetector = new GestureDetector(context,new GestureListener());
    }

    public void setArrowColor(int color){
        arrowPaint2.setColor(color);
    }

    //onSizeChanged在onDraw之前执行
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w/2;
        centerY = h/2;
        curX = centerX;
        curY = centerY;
        preX = curX;
        preY = curY;
        path.moveTo(curX, curY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                isStart = false;
                break;
        }
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }


    public class GestureListener implements GestureDetector.OnGestureListener{
        private float preScrollX = 0;
        private float preScrollY = 0;
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!MyView.this.isStart){
                preScrollX = e1.getX();
                preScrollY = e1.getY();
                isStart = true;
            }
            translateX += (e2.getX() - preScrollX)/3;
            translateY += (e2.getY() - preScrollY)/3;
            preScrollX = e2.getX();
            preScrollY = e2.getY();
            MyView.this.invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //用户拖拽地图进行平移
        canvas.translate(translateX,translateY);
        //绘制轨迹
        drawPath(canvas);
        //绘制箭头
        drawArrow(canvas);
    }

    public void autoAddStep(){
        //贝塞尔曲线
        curX += (float) (stepLen * Math.sin(Math.toRadians(degree)));
        curY += -(float) (stepLen * Math.cos(Math.toRadians(degree)));
        path.quadTo(preX,preY,curX,curY);
        pointFS.add(new PointF(curX, curY));
        preX = curX;
        preY = curY;
        //重绘
        postInvalidate();
    }

    public void autoAddStep(float curX, float curY){
        //贝塞尔曲线
        setCurX(curX);
        setCurY(curY);
        path.quadTo(preX,preY,curX,curY);
        pointFS.add(new PointF(curX, curY));
        setPreX(curX);
        setCurY(curY);
        //重绘
        postInvalidate();
    }

    public void setPreX(float preX){
        this.preX = preX;
    }

    public void setPreY(float preY) {
        this.preY = preY;
    }

    public void setCurX(float curX){
        this.curX = curX;
    }

    public void setCurY(float curY) {
        this.curY = curY;
    }

    public float getCurX(){
        return curX;
    }

    public float getCurY() {
        return curY;
    }

    public void orientChanged(float degreeNew){
        if (Math.abs(degreeNew-degree)>1) {
            this.degree = degreeNew;
            postInvalidate();
        }
    }



    private void drawArrow(Canvas canvas) {
        canvas.save();
        canvas.translate(curX, curY); // 平移画布
        canvas.rotate(degree); // 转动画布
        //画箭头
        canvas.drawPath(arrowPath, arrowPaint2);
        canvas.drawCircle(0,0,arrowR,arrowPaint2);
        canvas.drawCircle(0,0,arrowR*0.9f,arrowPaint);
        canvas.restore(); // 恢复画布
    }

    private void drawPath(Canvas canvas){
        for (PointF p : pointFS) {
            canvas.drawCircle(p.x, p.y, cR, pathPaint);
        }
    }

    public void bigger(){
        if (scale>=1.5){
            return;
        }
        scale += 0.2;
        setScaleX(scale);
        setScaleY(scale);
        invalidate();
    }

    public void smaller(){
        if (scale<=0.5){
            return;
        }
        scale -= 0.2;
        setScaleX(scale);
        setScaleY(scale);
        invalidate();
    }


}
