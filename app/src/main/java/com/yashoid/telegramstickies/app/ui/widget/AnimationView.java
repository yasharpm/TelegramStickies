package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.airbnb.lottie.LottieAnimationView;

public class AnimationView extends LottieAnimationView {

    private float mIntrinsicWidth = 0;
    private float mIntrinsicHeight = 0;

    public AnimationView(Context context) {
        super(context);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIntrinsicSize(float width, float height) {
        mIntrinsicWidth = width;
        mIntrinsicHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthMeasureSpec == 0 && heightMeasureSpec == 0) {
            setMeasuredDimension((int) mIntrinsicWidth, (int) mIntrinsicHeight);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
