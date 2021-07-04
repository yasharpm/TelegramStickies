package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RoundedCornersImageView extends SquareImageView {

    private static final float CORNER_RADIUS = 8;

    private final RectF mRect = new RectF();
    private final Path mPath = new Path();

    private float mCornerRadius;

    public RoundedCornersImageView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public RoundedCornersImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public RoundedCornersImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setScaleType(ScaleType.CENTER_CROP);

        mCornerRadius = getResources().getDisplayMetrics().density * CORNER_RADIUS;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRect.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());

        mPath.reset();
        mPath.addRoundRect(mRect, new float[] { mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius }, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mPath);
        super.onDraw(canvas);
        canvas.restore();
    }

}
