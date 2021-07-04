package com.yashoid.telegramstickies.app.ui.imageeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yashoid.telegramstickies.app.Crop;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.ui.widget.StrongGestureDetector;

public class CropView extends View implements Crop {

    private static final float CROP_ALPHA = 0.35f;
    private static final float BORDER_WIDTH = 2;
    private static final float BORDER_TOUCH_DISTANCE = 18;
    private static final float DEFAULT_CLOSURE_RATIO = 0.2f;

    private static final int LEFT = 0;
    private static final int TOP = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;

    private final Paint mCropPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private final RectF mCropRect = new RectF();
    private final Path mCropPath = new Path();

    private StrongGestureDetector mGestureDetector;

    private int mCropMode = CROP_RECT;

    private float mBorderTouchDistance;
    private float mBorderTouchRatio;

    private float mLeftBorder = DEFAULT_CLOSURE_RATIO;
    private float mTopBorder = DEFAULT_CLOSURE_RATIO;
    private float mRightBorder = 1 - DEFAULT_CLOSURE_RATIO;
    private float mBottomBorder = 1 - DEFAULT_CLOSURE_RATIO;

    private int mSelectedBorder = -1;

    public CropView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mGestureDetector = new StrongGestureDetector(mOnGestureListener);

        float density = getResources().getDisplayMetrics().density;

        int color = ContextCompat.getColor(context, R.color.black);

        int alpha = (((int) (CROP_ALPHA * 255)) << 24) | 0xffffff;

        mCropPaint.setStyle(Paint.Style.FILL);
        mCropPaint.setColor(alpha & color);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(color);
        mBorderPaint.setStrokeWidth(density * BORDER_WIDTH);

        mBorderTouchDistance = density * BORDER_TOUCH_DISTANCE;
    }

    public void setCropMode(int cropMode) {
        mCropMode = cropMode;

        updateCropPath();
    }

    public int getCropMode() {
        return mCropMode;
    }

    public void getCropRatios(RectF rect) {
        rect.set(mLeftBorder, mTopBorder, mRightBorder, mBottomBorder);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w == 0 || h == 0) {
            return;
        }

        updateCropPath();

        mBorderTouchRatio = mBorderTouchDistance / Math.min(w, h);
    }

    private void updateCropPath() {
        updateCropRect();

        mCropPath.reset();

        switch (mCropMode) {
            case CROP_RECT:
                mCropPath.addRect(mCropRect, Path.Direction.CW);
                break;
            case CROP_CIRCULAR:
                mCropPath.addOval(mCropRect, Path.Direction.CW);
                break;
        }

        invalidate();
    }

    private void updateCropRect() {
        float width = getWidth();
        float height = getHeight();

        mCropRect.left = mLeftBorder * width;
        mCropRect.top = mTopBorder * height;
        mCropRect.right = mRightBorder * width;
        mCropRect.bottom = mBottomBorder * height;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.clipPath(mCropPath, Region.Op.DIFFERENCE);
        canvas.drawPaint(mCropPaint);
        canvas.restore();

        float width = getWidth();
        float height = getHeight();

        canvas.drawLine(mCropRect.left, 0, mCropRect.left, height, mBorderPaint);
        canvas.drawLine(mCropRect.right, 0, mCropRect.right, height, mBorderPaint);
        canvas.drawLine(0, mCropRect.top, width, mCropRect.top, mBorderPaint);
        canvas.drawLine(0, mCropRect.bottom, width, mCropRect.bottom, mBorderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final StrongGestureDetector.OnGestureListener mOnGestureListener = new StrongGestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent event) {
            float distance = Math.abs(event.getX() - mCropRect.left);

            if (distance < mBorderTouchDistance) {
                mSelectedBorder = LEFT;
            }

            distance = Math.abs(event.getX() - mCropRect.right);

            if (distance < mBorderTouchDistance) {
                mSelectedBorder = RIGHT;
            }

            distance = Math.abs(event.getY() - mCropRect.top);

            if (distance < mBorderTouchDistance) {
                mSelectedBorder = TOP;
            }

            distance = Math.abs(event.getY() - mCropRect.bottom);

            if (distance < mBorderTouchDistance) {
                mSelectedBorder = BOTTOM;
            }

            return mSelectedBorder != -1;
        }

        @Override
        public void onUp() {
            mSelectedBorder = -1;
        }

        @Override
        public void onTranslate(float dx, float dy) {
            float width = getWidth();
            float height = getHeight();

            switch (mSelectedBorder) {
                case LEFT:
                    mLeftBorder += dx / width;

                    if (mLeftBorder + mBorderTouchRatio >= mRightBorder) {
                        mLeftBorder = mRightBorder - mBorderTouchRatio;
                    }
                    else if (mLeftBorder < 0) {
                        mLeftBorder = 0;
                    }
                    break;
                case RIGHT:
                    mRightBorder += dx / width;

                    if (mRightBorder - mBorderTouchRatio <= mLeftBorder) {
                        mRightBorder = mLeftBorder + mBorderTouchRatio;
                    }
                    else if (mRightBorder > 1) {
                        mRightBorder = 1;
                    }
                    break;
                case TOP:
                    mTopBorder += dy / height;

                    if (mTopBorder + mBorderTouchRatio >= mBottomBorder) {
                        mTopBorder = mBottomBorder - mBorderTouchRatio;
                    }
                    else if (mTopBorder < 0) {
                        mTopBorder = 0;
                    }
                    break;
                case BOTTOM:
                    mBottomBorder += dy / height;

                    if (mBottomBorder - mBorderTouchRatio < mTopBorder) {
                        mBottomBorder = mTopBorder + mBorderTouchRatio;
                    }
                    else if (mBottomBorder > 1) {
                        mBottomBorder = 1;
                    }
                    break;
            }

            updateCropPath();
        }

        @Override public void onScale(float ds) { }
        @Override public void onRotate(float dd) { }

    };

}
