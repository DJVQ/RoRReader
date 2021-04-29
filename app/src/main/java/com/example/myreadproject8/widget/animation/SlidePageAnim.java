package com.example.myreadproject8.widget.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by newbiechen on 17-7-24.
 */

public class SlidePageAnim extends HorizonPageAnim {
    private Rect mSrcRect, mDestRect,mNextSrcRect,mNextDestRect;

    public SlidePageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
    }

    @Override
    public void drawStatic(Canvas canvas) {
        if (isCancel){
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        }else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void drawMove(Canvas canvas) {
        int dis = 0;
        switch (mDirection){
            case NEXT:
                //左半边的剩余区域
                dis = (int) (mScreenWidth - mStartX + mTouchX);
                if (dis > mScreenWidth){
                    dis = mScreenWidth;
                }
                //计算bitmap截取的区域->|
                mSrcRect.left = mScreenWidth - dis;
                //计算bitmap在canvas显示的区域|<-
                mDestRect.right = dis;
                //计算下一页截取的区域|<-
                mNextSrcRect.right = mScreenWidth - dis;
                //计算下一页在canvas显示的区域->|
                mNextDestRect.left = dis;

                canvas.drawBitmap(mNextBitmap,mNextSrcRect,mNextDestRect,null);
                canvas.drawBitmap(mCurBitmap,mSrcRect,mDestRect,null);
                break;
            default:
                dis = (int) (mTouchX - mStartX);
                if (dis < 0){
                    dis = 0;
                    mStartX = mTouchX;
                }
                //计算下一页从从左往右截取的位置
                mSrcRect.left =  mScreenWidth - dis;//->|
                //计算下一页在canvas显示的区域
                mDestRect.right = dis;//|<-

                //计算当前页面从右往左截取的位置
                mNextSrcRect.right = mScreenWidth - dis;//
                //计算当前页面在canvas中显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mCurBitmap,mNextSrcRect,mNextDestRect,null);
                canvas.drawBitmap(mNextBitmap,mSrcRect,mDestRect,null);
                break;
        }
    }

    @Override
    public void startAnim() {
        super.startAnim();
        int dx = 0;//左右页面的分界点
        switch (mDirection){
            case NEXT:
                if (isCancel){
                    int dis = (int)((mScreenWidth - mStartX) + mTouchX);//左边页面右边界
                    if (dis > mScreenWidth){//最多往右滑到到屏幕边缘页面
                        dis = mScreenWidth;
                    }
                    dx = mScreenWidth - dis;//屏幕右边界距离右边页面左边界的距离，由于最终要向右所以取正
                }else{
                    dx = (int) -(mTouchX + (mScreenWidth - mStartX));//屏幕左边界距离左边页面右边界的距离，由于最终要向左所以取负
                }
                break;
            default:
                if (isCancel){
                    dx = (int)-Math.abs(mTouchX - mStartX);//屏幕左边界距离左边页面右边界的距离，由于最终要向左所以取负
                }else{
                    dx = (int) (mScreenWidth - (mTouchX - mStartX));//屏幕右边界距离右边页面左边界的距离，由于最终要向右所以取正
                }
                break;
        }
        //滑动速度（dx绝对值越大，越快）
        int duration =  (animationSpeed * Math.abs(dx)) / mScreenWidth;
        mScroller.startScroll((int) mTouchX, 0, dx, 0, duration);
    }
}
