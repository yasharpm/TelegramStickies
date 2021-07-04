package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorView extends View {

    private static final float RADIUS = 54f / 2f;

    private final Paint mPaint = new Paint();

    private int mRadius;

    public ColorView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ColorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ColorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        float density = getResources().getDisplayMetrics().density;

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mRadius = (int) (density * RADIUS);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, mRadius, mPaint);
    }

}
