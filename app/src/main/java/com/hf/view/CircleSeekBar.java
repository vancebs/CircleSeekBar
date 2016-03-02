/******************************************************************************/
/*                                                               Date:03/2014 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2014 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Fan.Hu                                                          */
/*  Email  :   fan.hu@tct.com                                                 */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 03/17/2014|        Fan.Hu        |      wave3-dev       |first version     */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.hf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hf.circleseekbar.R;

public class CircleSeekBar extends ViewGroup {
    private static final int DEFAULT_PROGRESS_COLOR = 0xFF9999FF;
    private static final int DEFAULT_MAX_PROGRESS = 3600;

    protected int mCircleCount = 0;

    protected ImageView mSlider;
    protected int mSliderMargin;
    protected float mRadius = 0;
    protected int mMaxProgress = DEFAULT_MAX_PROGRESS;
    protected int mProgress = 0;
    protected OnProgressListener mListener = null;
    protected RectF mRect = new RectF();
    protected Paint mPaint;

    protected GestureDetector mGestureDetector;
    protected GestureDetector.OnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {
        float mLastX = 0;
        float mLastY = 0;
        double mLastAngle = 0.0d;

        @Override
        public boolean onDown(MotionEvent e) {
            mLastX = mSlider.getTranslationX();
            mLastY = mSlider.getTranslationY();
            mLastAngle = progress2Angle(mProgress, mMaxProgress);
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float cy = (float) getMeasuredHeight() / 2.0f;
            float x = mLastX + e2.getRawX() - e1.getRawX();
            float y = mLastY + e2.getRawY() - e1.getRawY();

            double angle = Math.atan2(x, cy - y);
            setProgress(angle2Progress(angle, mMaxProgress));

            // check whether circle count should be updated
            int lastAngleArea = getAngleArea(mLastAngle);
            int currAngleArea = getAngleArea(angle);
            if (lastAngleArea == 1 && currAngleArea == 2) {
                mCircleCount --;
                if (mListener != null) {
                    mListener.onCircleCountChanged(mCircleCount);
                }
            } else if (lastAngleArea == 2 && currAngleArea == 1) {
                mCircleCount ++;
                if (mListener != null) {
                    mListener.onCircleCountChanged(mCircleCount);
                }
            }
            mLastAngle = angle;
            return true;
        }
    };

    public interface OnProgressListener {
        /**
         * On progress updated
         * @param progress current progress value
         * @param max max progress value
         */
        void onProgress(int progress, int max);

        void onCircleCountChanged(int circleCount);
    }

    public CircleSeekBar(Context context) {
        this(context, null);
    }

    public CircleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar);
        Drawable sliderSrc = a.getDrawable(R.styleable.CircleSeekBar_sliderSrc);
        mSliderMargin = a.getDimensionPixelOffset(R.styleable.CircleSeekBar_sliderMargin, 0);
        int progressColor = a.getColor(R.styleable.CircleSeekBar_progressColor, DEFAULT_PROGRESS_COLOR);
        a.recycle();

        // enable draw
        setWillNotDraw(false);

        // paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(progressColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Gesture
        mGestureDetector = new GestureDetector(context, mGestureDetectorListener);

        // create slider & add into parent
        mSlider = new ImageView(context);
        if (sliderSrc != null) {
            mSlider.setImageDrawable(sliderSrc);
        } else {
            mSlider.setImageResource(R.drawable.sel_timeout_button);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.addViewInLayout(mSlider, 0, lp, true);

        // drag slider
        mSlider.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (view == mSlider) {
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawArc(mRect, -90, (mProgress * 360) / mMaxProgress, true, mPaint);
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        int sliderLeft = (right - left - mSlider.getMeasuredWidth()) / 2;
        int sliderRight = sliderLeft + mSlider.getMeasuredWidth();
        int sliderTop = mSliderMargin;
        int sliderBottom = sliderTop + mSlider.getMeasuredHeight() + mSliderMargin;

        mSlider.layout(sliderLeft, sliderTop, sliderRight, sliderBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);

        // do measure
        measureChild(mSlider, widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthSpec, heightSpec);

        // generate radius of circle
        mSlider.setPivotX(mSlider.getMeasuredWidth() / 2);
        mSlider.setPivotX(mSlider.getMeasuredHeight() / 2);
        mRadius = (getMeasuredHeight() - mSlider.getMeasuredHeight()) / 2 - mSliderMargin;
    }

    protected void setAngle(double angle) {
        double dx = mRadius * Math.sin(angle);
        double dy = mRadius * Math.cos(angle);
        float x = (float) dx;
        float y = (float) (mRadius - dy);
        mSlider.setTranslationX(x);
        mSlider.setTranslationY(y);
    }

    protected double progress2Angle(int progress, int max) {
        return Math.PI * 2.0d * progress / max;
    }

    protected int angle2Progress(double angle, int max) {
        if (Math.abs(angle / Math.PI) >= 0.99) {
            // angle cannot be PI. This is a patch to make it can be PI
            return max / 2;
        } else {
            return ((int) (angle * (double) max / Math.PI / 2.0d) + max) % max;
        }
    }

    public void setProgress(int progress) {
        if (mProgress != progress) {
            mProgress = progress;
            setAngle(progress2Angle(mProgress, mMaxProgress));
            invalidate();

            if (mListener != null) {
                mListener.onProgress(mProgress, mMaxProgress);
            }
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setMaxProgress(int max) {
        if (mMaxProgress != max) {
            mMaxProgress = max;
            setAngle(progress2Angle(mProgress, mMaxProgress));

            if (mListener != null) {
                mListener.onProgress(mProgress, mMaxProgress);
            }
        }
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mListener = listener;
    }

    public int getCircleCount() {
        return mCircleCount;
    }

    public void setCircleCount(int totalProgress) {
        mCircleCount = totalProgress;
    }

    private static int getAngleArea(double angle) {
        final double PI_2 = Math.PI / 2.0d;
        if (angle >= 0 && angle < PI_2) {
            return 1;
        } else if (angle >= -PI_2 && angle < 0) {
            return 2;
        } else if (angle >= -Math.PI && angle < -PI_2) {
            return 3;
        } else if (angle >= PI_2 && angle < Math.PI) {
            return 4;
        }
        return 0;
    }
}