package com.yashoid.telegramstickies.app.ui.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.yashoid.telegramstickies.app.R;

public class BottomSheet extends ViewGroup {

    public static final int MODE_FULL_SCREEN = 0;
    public static final int MODE_WRAPPED = 1;

    private static final float CORNER_RADIUS = 24;
    private static final float HANDLE_WIDTH = 56;
    private static final float HANDLE_HEIGHT = 4;
    private static final float HANDLE_TOP_MARGIN = 12;
    private static final float SHEET_ELEVATION = 6;
    private static final float SHEET_TOP_MARGIN = 24;
    private static final float HANDLE_TOUCH_AREA = 48;
    private static final float OPENED_SHEET_BEHIND_OPACITY = 0.56f;

    private static final long ANIMATION_DURATION = 333;

    public interface OnOpenCloseListener {

        void onOpened();

        void onClosed();

    }

    private OnOpenCloseListener mOnOpenCloseListener = null;

    private float mElevation;

    private int mHandleWidth;
    private int mHandleHeight;
    private int mHandleTopMargin;
    private int mSheetTopMargin;
    private int mHandleTouchArea;
    private int mBehindColor;

    private int mMode = MODE_FULL_SCREEN;

    private View mSheet;
    private View mHandle;

    private float mProgress = 0;

    private GestureDetector mGestureDetector;
    private boolean mTouchedOutside = false;
    private boolean mTouchAccepted = false;

    private ValueAnimator mAnimator = null;

    public BottomSheet(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        mElevation = density * SHEET_ELEVATION;

        mHandleWidth = (int) (density * HANDLE_WIDTH);
        mHandleHeight = (int) (density * HANDLE_HEIGHT);
        mHandleTopMargin = (int) (density * HANDLE_TOP_MARGIN);
        mSheetTopMargin = (int) (density * SHEET_TOP_MARGIN);
        mHandleTouchArea = (int) (density * HANDLE_TOUCH_AREA);
        mBehindColor = ContextCompat.getColor(context, R.color.sheet_behind);

        mSheet = new View(context);
        float sheetCornerRadius = density * CORNER_RADIUS;
        RoundRectShape sheetBackgroundShape = new RoundRectShape(new float[] { sheetCornerRadius, sheetCornerRadius, sheetCornerRadius, sheetCornerRadius, 0, 0, 0, 0 }, null, null);
        Drawable sheetBackground = new ShapeDrawable(sheetBackgroundShape);
        sheetBackground.setColorFilter(ContextCompat.getColor(context, R.color.default_background), PorterDuff.Mode.SRC_IN);
        mSheet.setBackground(sheetBackground);
        addView(mSheet);

        mHandle = new View(context);
        float handleCornerRadius = mHandleHeight / 2f;
        RoundRectShape handleShape = new RoundRectShape(new float[] { handleCornerRadius, handleCornerRadius, handleCornerRadius, handleCornerRadius, handleCornerRadius, handleCornerRadius, handleCornerRadius, handleCornerRadius }, null, null);
        Drawable handleBackground = new ShapeDrawable(handleShape);
        handleBackground.setColorFilter(ContextCompat.getColor(context, R.color.sheet_handle), PorterDuff.Mode.SRC_IN);
        mHandle.setBackground(handleBackground);
        addView(mHandle);
        mHandle.measure(
                MeasureSpec.makeMeasureSpec(mHandleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHandleHeight, MeasureSpec.EXACTLY)
        );

        mGestureDetector = new GestureDetector(context, mOnGestureListener);
    }

    public void setMode(int mode) {
        mMode = mode;
        requestLayout();
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        ViewCompat.setElevation(child, mElevation);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int sheetTotalHeight = calcSheetTotalHeight();
        int childHeight = sheetTotalHeight - mHandleTouchArea;

        int sheetTop = getHeight() - (int) (mProgress * sheetTotalHeight);

        mSheet.measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(sheetTotalHeight, MeasureSpec.EXACTLY)
        );
        mSheet.layout(0, sheetTop, getWidth(), sheetTop + sheetTotalHeight);

        int handleTop = sheetTop + mHandleTopMargin;
        int handleLeft = (getWidth() - mHandleWidth) / 2;
        mHandle.layout(handleLeft, handleTop, handleLeft + mHandleWidth, handleTop + mHandleHeight);

        for (int i = 2; i < getChildCount(); i++) {
            View child = getChildAt(i);

            child.measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
            );
            child.layout(0, sheetTop + mHandleTouchArea, getWidth(), sheetTop + sheetTotalHeight);
        }
    }

    private int calcSheetTotalHeight() {
        switch (mMode) {
            case MODE_FULL_SCREEN:
                return getHeight() - mSheetTopMargin;
            case MODE_WRAPPED:
                int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
                int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

                int height = 0;

                for (int i = 2; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.measure(widthSpec, heightSpec);
                    height = Math.max(height, child.getMeasuredHeight());
                }

                return height + mHandleTouchArea;
        }

        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int alpha = ((int) (mProgress * 255 * OPENED_SHEET_BEHIND_OPACITY) << 24) + 0xffffff;

        canvas.drawColor(alpha & mBehindColor);
    }

    public void open() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = new ValueAnimator();
        mAnimator.setFloatValues(mProgress, 1);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            requestLayout();
            invalidate();
        });
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mProgress = 1;
                requestLayout();
                invalidate();

                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onOpened();
                }
            }

            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

        });
        mAnimator.start();
    }

    public void close() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = new ValueAnimator();
        mAnimator.setFloatValues(mProgress, 0);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            requestLayout();
            invalidate();
        });
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mProgress = 0;
                requestLayout();
                invalidate();

                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onClosed();
                }
            }

            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

        });
        mAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);

        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
            if (mTouchAccepted && mAnimator == null) {
                if (mProgress > 0.5f) {
                    open();
                }
                else {
                    close();
                }

                mTouchAccepted = false;
            }
        }

        return result;
    }

    private final GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            mTouchedOutside = e.getY() < mSheet.getTop();
            mTouchAccepted = e.getY() > mSheet.getTop() && e.getY() < mSheet.getTop() + mHandleTouchArea;

            return mTouchedOutside || mTouchAccepted;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mTouchedOutside) {
                close();
                return true;
            }

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mTouchAccepted) {
                return false;
            }

            float movementRatio = distanceY / mSheet.getHeight();

            mProgress += movementRatio;

            mProgress = Math.max(0, Math.min(1, mProgress));

            requestLayout();
            invalidate();

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!mTouchAccepted) {
                return false;
            }

            if (velocityY < 0) {
                open();
            }
            else {
                close();
            }

            return true;
        }

    };

}
