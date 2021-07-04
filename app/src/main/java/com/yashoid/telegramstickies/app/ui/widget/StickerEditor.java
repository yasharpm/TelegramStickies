package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Rules;
import com.yashoid.telegramstickies.app.ui.widget.StrongGestureDetector;

public class StickerEditor extends ViewGroup {

    private static final float SELECTION_STROKE_WIDTH = 1;
    private static final float SELECTION_STROKE_GAP = 4;

    private StrongGestureDetector mGestureDetector;

    private int mSize = 0;

    private View mSelectedChild = null;
    private LayoutParams mSelectedChildParams = null;

    private final Paint mSelectionPaint = new Paint();

    private final float[] mHelperPoint = new float[2];
    private final Matrix mHelperMatrix = new Matrix();
    private final RectF mHelperRect = new RectF();

    public StickerEditor(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public StickerEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public StickerEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);

        mGestureDetector = new StrongGestureDetector(mOnGestureListener);

        float density = getResources().getDisplayMetrics().density;
        float strokeGap = density * SELECTION_STROKE_GAP;

        mSelectionPaint.setStyle(Paint.Style.STROKE);
        mSelectionPaint.setColor(ContextCompat.getColor(context, R.color.black));
        mSelectionPaint.setStrokeWidth(density * SELECTION_STROKE_WIDTH);
        mSelectionPaint.setStrokeCap(Paint.Cap.ROUND);
        mSelectionPaint.setPathEffect(new DashPathEffect(new float[] { strokeGap, strokeGap }, 0));
    }

    public Bitmap getStickerBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(Rules.STICKER_SIZE, Rules.STICKER_SIZE, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        float scale = (float) Rules.STICKER_SIZE / getWidth();

        canvas.scale(scale, scale, 0, 0);

        Drawable background = getBackground();
        setBackground(null);

        draw(canvas);

        setBackground(background);

        return bitmap;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        LayoutParams params = (LayoutParams) child.getLayoutParams();

        child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (child.getMeasuredWidth() == 0 || child.getMeasuredHeight() == 0) {
            removeView(child);
            return;
        }

        params.aspectRatio = (float) child.getMeasuredWidth() / child.getMeasuredHeight();

        if (mSize > 0) {
            setupChild(child);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : Integer.MAX_VALUE;
        int heightSize = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightMeasureSpec) : Integer.MAX_VALUE;
        int size = Math.min(widthSize, heightSize);
        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(sizeMeasureSpec, sizeMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSize = Math.min(w, h);

        if (mSize > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                setupChild(getChildAt(i));
            }
        }
    }

    private void setupChild(View child) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();

        int newWidth;
        int newHeight;

        if (params.aspectRatio > 1) {
            newWidth = mSize;
            newHeight = (int) (mSize / params.aspectRatio);
        }
        else {
            newWidth = (int) (mSize * params.aspectRatio);
            newHeight = mSize;
        }

        child.measure(
                MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
        );
        child.layout(0, 0, newWidth, newHeight);

        if (params.left == -1 && params.top == -1) {
            params.left = (mSize - newWidth) / 2f;
            params.top = (mSize - newHeight) / 2f;
            params.width = newWidth;
            params.height = newHeight;
            params.scale = 1;

            child.setTranslationX(params.left);
            child.setTranslationY(params.top);
        }
        else {
            // TODO We are dropping the case that the editor is being resized. So doesn't really matter.
            params.scale *= (float) newWidth / params.width;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mSelectedChild != null) {
            mHelperRect.set(
                    mSelectionPaint.getStrokeWidth(),
                    mSelectionPaint.getStrokeWidth(),
                    mSelectedChildParams.width - mSelectionPaint.getStrokeWidth(),
                    mSelectedChildParams.height - mSelectionPaint.getStrokeWidth()
            );

            canvas.save();
            canvas.setMatrix(mSelectedChild.getMatrix());
            canvas.drawRect(mHelperRect, mSelectionPaint);
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final StrongGestureDetector.OnGestureListener mOnGestureListener = new StrongGestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent event) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();

                mHelperPoint[0] = event.getX();
                mHelperPoint[1] = event.getY();

                child.getMatrix().invert(mHelperMatrix);

                mHelperMatrix.mapPoints(mHelperPoint);

                if (mHelperPoint[0] > 0 && mHelperPoint[0] < params.width && mHelperPoint[1] > 0 && mHelperPoint[1] < params.height) {
                    mSelectedChild = child;
                    mSelectedChildParams = params;
                    invalidate();
                    return true;
                }
            }

            return false;
        }

        @Override
        public void onUp() {
            mSelectedChild = null;
            mSelectedChildParams = null;
            invalidate();
        }

        @Override
        public void onTranslate(float dx, float dy) {
            mSelectedChildParams.left += dx;
            mSelectedChildParams.top += dy;
            mSelectedChild.setTranslationX(mSelectedChildParams.left);
            mSelectedChild.setTranslationY(mSelectedChildParams.top);

            invalidate();
        }

        @Override
        public void onScale(float ds) {
            mSelectedChildParams.scale *= ds;
            mSelectedChild.setScaleX(mSelectedChildParams.scale);
            mSelectedChild.setScaleY(mSelectedChildParams.scale);

            invalidate();
        }

        @Override
        public void onRotate(float dd) {
            mSelectedChildParams.rotation += dd;

            while (mSelectedChildParams.rotation > 360) {
                mSelectedChildParams.rotation -= 360;
            }

            while (mSelectedChildParams.rotation < 0) {
                mSelectedChildParams.rotation += 360;
            }

            mSelectedChild.setRotation(mSelectedChildParams.rotation);

            invalidate();
        }

    };

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(0, 0);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public float aspectRatio;
        public float baseScale;

        public float left = -1;
        public float top = -1;
        public float scale = 1;
        public float rotation = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }

}
