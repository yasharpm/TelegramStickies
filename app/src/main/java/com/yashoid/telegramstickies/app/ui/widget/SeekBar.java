package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yashoid.telegramstickies.app.R;

public class SeekBar extends View {

    private static final float BUTTON_RADIUS = 7;
    private static final float LINE_WIDTH = 3;
    private static final float HEIGHT = 48;

    public interface OnSeekChangedListener {

        void onSeekChanged(SeekBar v, int value);

    }

    private final Paint mFilledPaint = new Paint();
    private final Paint mFilledLinePaint = new Paint();
    private final Paint mEmptyLinePaint = new Paint();

    private OnSeekChangedListener mListener = null;

    private float mButtonRadius;
    private float mLineWidth;
    private int mHeight;

    private int mMin = 0;
    private int mMax = 100;

    private float mProgress = 0;

    public SeekBar(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        float density = getResources().getDisplayMetrics().density;

        mButtonRadius = density * BUTTON_RADIUS;
        mLineWidth = density * LINE_WIDTH;
        mHeight = (int) (density * HEIGHT);

        mFilledPaint.setStyle(Paint.Style.FILL);
        mFilledPaint.setColor(ContextCompat.getColor(context, R.color.red));
        mFilledPaint.setAntiAlias(true);

        mFilledLinePaint.setStyle(Paint.Style.STROKE);
        mFilledLinePaint.setColor(mFilledPaint.getColor());
        mFilledLinePaint.setStrokeWidth(mLineWidth);
        mFilledLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mEmptyLinePaint.setStyle(Paint.Style.STROKE);
        mEmptyLinePaint.setColor(ContextCompat.getColor(context, R.color.gray));
        mEmptyLinePaint.setStrokeWidth(mLineWidth);
        mEmptyLinePaint.setStrokeCap(Paint.Cap.ROUND);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBar, defStyleAttr, 0);

        if (a.hasValue(R.styleable.SeekBar_min)) {
            mMin = a.getInt(R.styleable.SeekBar_min, 0);
        }

        if (a.hasValue(R.styleable.SeekBar_max)) {
            mMax = a.getInt(R.styleable.SeekBar_max, 0);
        }

        if (a.hasValue(R.styleable.SeekBar_value)) {
            int value = a.getInt(R.styleable.SeekBar_value, 0);

            mProgress = (float) (value - mMin) / (mMax - mMin);
        }

        a.recycle();
    }

    public void setOnSeekChangedListener(OnSeekChangedListener listener) {
        mListener = listener;
    }

    public void setMin(int min) {
        mMin = min;

        updateValue();
    }

    public void setMax(int max) {
        mMax = max;

        updateValue();
    }

    public void setValue(int value) {
        mProgress = (float) (value - mMin) / (mMax - mMin);
        mProgress = Math.max(0, Math.min(1, mProgress));

        invalidate();
    }

    public int getValue() {
        return (int) ((mMax - mMin) * mProgress + mMin);
    }

    private void updateValue() {
        int value = getValue();

        if (mListener != null) {
            mListener.onSeekChanged(this, value);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        canvas.drawLine(mButtonRadius, height / 2f, width - mButtonRadius, height / 2f, mEmptyLinePaint);

        float position = mButtonRadius + (width - 2 * mButtonRadius) * mProgress;

        canvas.drawLine(mButtonRadius, height / 2f, position, height / 2f, mFilledLinePaint);
        canvas.drawCircle(position, height / 2f, mButtonRadius, mFilledPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mButtonRadius;

        mProgress = x / (getWidth() - 2 * mButtonRadius);
        mProgress = Math.max(0, Math.min(1, mProgress));

        updateValue();
        invalidate();

        return true;
    }

}
