package com.example.myreadproject8.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.myreadproject8.R;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ycq on 2021/4/26 0026
 * describe：绘制艾宾浩斯曲线
 */
public class EbbinghausWave extends View {
    private List<WaveConfigData> mWaves;// 数值集合
    // 坐标轴的数值
    private int mCoordinateYCount = 9;
    private String[] mCoordinateXValues ;// 外界传入
    private float[] mCoordinateYValues;// 动态计算
    // 坐标的单位
    private String mXUnit;
    private String mYUnit;
    // 所有曲线中所有数据中的最大值
    private Paint mCoordinatorPaint;
    private Paint mTextPaint;
    private Paint mWrapPaint;
    // 坐标轴上描述性文字的空间大小
    private int mTopUnitHeight;// 顶部Y轴单位高度
    private int mBottomTextHeight;
    private int mLeftTextWidth;
    // 网格尺寸
    private int mGridWidth, mGridHeight;
    private float mAnimProgress;
    public EbbinghausWave(Context context) {
        this(context,null);
    }

    public EbbinghausWave(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EbbinghausWave(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        post(new Runnable() {
            @Override
            public void run() {
                showAnimator();
            }
        });
    }
    private void init() {
        // 初始化数据集合的容器
        mWaves = new ArrayList<>();
        // 坐标系的单位
        mBottomTextHeight = dp2px(40);// X轴底部字体的高度
        mLeftTextWidth = mBottomTextHeight;// Y轴左边字体的宽度
        mTopUnitHeight = dp2px(30);// 顶部Y轴的单位
        // 初始化坐标轴Paint
        mCoordinatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCoordinatorPaint.setColor(Color.LTGRAY);
        // 初始化文本Paint
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setTextSize(sp2px(12));
        // 初始化曲线Paint
        mWrapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mWrapPaint.setPathEffect(new CornerPathEffect(200f));
    }

    /**
     * description: 配置坐标轴信息
     * @param xUnit       X 轴的单位
     * @param yUnit       Y 轴的单位
     * @param coordinateXValues X 坐标轴上的数值
     */
    public void setupCoordinator(String xUnit, String yUnit, String... coordinateXValues) {
        mXUnit = xUnit;
        mYUnit = yUnit;
        mCoordinateXValues = coordinateXValues;

    }

    /**
     * 添加一条曲线, 确保与横坐标的数值对应
     *
     * @param color
     * @param isCoverRegion
     * @param values
     */
    public void addWave(int color, boolean isCoverRegion, float... values) {
        mWaves.add(new WaveConfigData(color, isCoverRegion, values));

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCoordinate(canvas);
        if(mWaves != null)
            drawWrap(canvas);
    }

    private void showAnimator(){
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }



    /**
     * 绘制坐标系
     */
    private void drawCoordinate(Canvas canvas) {
        //y轴
        mCoordinateYValues = new float[mCoordinateYCount];
        for (int i = 0; i < mCoordinateYCount; i++) {
            mCoordinateYValues[i] = i * 12.5f;
        }

        Point start = new Point();//起点
        Point stop = new Point();//终点
        // 1. 绘制横轴线和纵坐标单位
        int xLineCount = mCoordinateYValues.length;
        mGridHeight = (getHeight() - getPaddingTop() - getPaddingBottom() - mBottomTextHeight - mTopUnitHeight) / (xLineCount - 1);
        for (int i = 0; i < xLineCount; i++) {
            start.x = getPaddingLeft() + mLeftTextWidth;
            start.y = getHeight() - getPaddingBottom() - mBottomTextHeight - mGridHeight * i;
            stop.x = getRight() - getPaddingRight();
            stop.y = start.y;
            // 绘制横轴线
            canvas.drawLine(start.x, start.y, stop.x, stop.y, mCoordinatorPaint);
            // 绘制y轴每行下标
            if (i == 0) continue;
            String drawText = String.valueOf( mCoordinateYValues[i]);
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            float offsetY = ((fontMetrics.bottom - fontMetrics.top) / 2 + fontMetrics.bottom) / 2;
            float baseLine = start.y + offsetY;
            float left = getPaddingLeft() + mLeftTextWidth / 2 - mTextPaint.measureText(drawText) / 2;
            canvas.drawText(drawText, left, baseLine, mTextPaint);
             //绘制Y轴单位
            if (i == xLineCount - 1) {
                drawText = mYUnit;
                baseLine = getPaddingTop() + mTopUnitHeight / 2;
                canvas.drawText(drawText, left, baseLine, mTextPaint);
            }
        }
        // 2. 绘制纵轴线和横坐标单位
        int yLineCount = mCoordinateXValues.length;
        mGridWidth = (getWidth() - getPaddingLeft() - getPaddingRight() - mLeftTextWidth) / (yLineCount);
        for (int i = 0; i < yLineCount; i++) {
            start.x = getPaddingTop() + mLeftTextWidth + mGridWidth * i;
            start.y = getPaddingTop() + mTopUnitHeight;
            stop.x = start.x;
            stop.y = getHeight() - mBottomTextHeight - getPaddingBottom();
            // 绘制纵轴线
            canvas.drawLine(start.x, start.y, stop.x, stop.y, mCoordinatorPaint);
            // 绘制横坐标单位
            String drawText = mCoordinateXValues[i];
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            float offsetY = ((fontMetrics.bottom - fontMetrics.top) / 2 + fontMetrics.bottom) / 2;
            float baseLine = getHeight() - getPaddingBottom() - mBottomTextHeight / 2 + offsetY;
            float left = start.x - mTextPaint.measureText(drawText) / 2;
            // 绘制X轴单位
            if (i == 0) {
                drawText = mXUnit;
                left = getPaddingLeft() + mLeftTextWidth / 2 - mTextPaint.measureText(drawText) / 2;
            }
            mTextPaint.setTextSize(20);
            canvas.drawText(drawText, left, baseLine, mTextPaint);
        }
    }



    /**
     * 绘制曲线
     */
    private void drawWrap(Canvas canvas) {

        //裁剪画布
        canvas.clipRect(new RectF(
                mLeftTextWidth,
                getPaddingTop() + mTopUnitHeight,
                (getRight() - getPaddingRight()) * mAnimProgress,
                getHeight() - getPaddingBottom() - mBottomTextHeight)
        );

        float yHeight = mGridHeight * (mCoordinateYCount - 1);//纵坐标高度
        for (WaveConfigData wave : mWaves) {
            Path[] path = new Path[wave.values.length];//路径
            float maxY = mCoordinateYValues[mCoordinateYCount - 1];// Y轴坐标的最大值
            path[0] = new Path();
            path[0].moveTo(mLeftTextWidth,  getPaddingTop() + mTopUnitHeight);

            for (int index = 1; index < wave.values.length; index++) {
                float x = mLeftTextWidth + mGridWidth * index;
                float y = getHeight() - getPaddingBottom() - mBottomTextHeight - yHeight * (wave.values[index] / maxY);
                float x0 = mLeftTextWidth + mGridWidth * (index-1);
                float y0 = getHeight() - getPaddingBottom() - mBottomTextHeight - yHeight * (wave.values[index-1] / maxY);
                if((wave.values[index-1] - wave.values[index]) > 10){
                    mWrapPaint.setColor(getResources().getColor(R.color.md_red_800));
                    path[index-1].cubicTo(x0+0.4f*((x-x0)/mGridWidth),y0,(x+x0)/2,y,x,y);
                }else if((wave.values[index-1] - wave.values[index]) < -10){
                    mWrapPaint.setColor(wave.color);
                    path[index-1].cubicTo((x+x0)/2,y0,x+5,y,x,y);
                }else if((wave.values[index-1] - wave.values[index]) >= -10 && (wave.values[index-1] - wave.values[index]) <= 0){
                    mWrapPaint.setColor(wave.color);
                    path[index-1].cubicTo((x+x0)/2,y0,x+5,y,x,y);
                }else{
                    mWrapPaint.setColor(wave.color);
                    path[index-1].cubicTo(x0+0.4f*((x-x0)/mGridWidth),y0,(x+x0)/2,y,x,y);
                }
                canvas.drawPath(path[index-1], mWrapPaint);
                path[index] = new Path();
                path[index].moveTo(mLeftTextWidth + mGridWidth * index,
                        getHeight() - getPaddingBottom() - mBottomTextHeight
                                - yHeight * (wave.values[index] / maxY));
            }

            if (wave.isCoverRegion) {
                mWrapPaint.setStyle(Paint.Style.FILL);
                path[wave.values.length-1].lineTo(getRight() - getPaddingRight(), getHeight());
                path[wave.values.length].close();
            } else {
                mWrapPaint.setStyle(Paint.Style.STROKE);
                mWrapPaint.setStrokeWidth(10);
            }
            mWrapPaint.setColor(wave.color);
        }
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getResources().getDisplayMetrics());
    }

    public static class WaveConfigData {
        int color;
        boolean isCoverRegion;
        float values[];

        public WaveConfigData(int color, boolean isCoverRegion, float[] values) {
            this.color = color;
            this.isCoverRegion = isCoverRegion;
            this.values = values;
        }
    }
}
