package com.yashoid.telegramstickies.app.ui.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.yashoid.telegramstickies.app.BitmapLoader;
import com.yashoid.telegramstickies.app.Crop;
import com.yashoid.telegramstickies.app.ui.widget.StrongGestureDetector;

public class ImageEditor extends ViewGroup {

    private StrongGestureDetector mGestureDetector;

    private Uri mImageUri = null;

    private int mSize = 0;

    private Bitmap mBitmap = null;
    private Bitmap mPreviousBitmap = null;

    private float mScale = 1;
    private float mLeft = 0;
    private float mTop = 0;

    private final Matrix mMatrix = new Matrix();
    private final Matrix mReverseMatrix = new Matrix();

    private boolean mErasing = false;
    private float mEraserRadius;
    private float mEraserOpacity;
    private final Path mEraserPath = new Path();
    private final Paint mErasurePaint = new Paint();

    private final float[] mHelperPoint = new float[2];
    private final RectF mHelperRect = new RectF();

    public ImageEditor(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ImageEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ImageEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);

        mGestureDetector = new StrongGestureDetector(mOnGestureListener);

        mErasurePaint.setAntiAlias(true);
        mErasurePaint.setStyle(Paint.Style.FILL);
    }

    public void setImageUri(Uri uri) {
        mImageUri = uri;

        if (mImageUri != null && mSize > 0) {
            ensureBitmap();
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void crop(RectF cropRatios, int cropMode) {
        if (mBitmap == null || (cropMode != Crop.CROP_RECT && cropMode != Crop.CROP_CIRCULAR)) {
            return;
        }

        RectF cropRect = new RectF();
        cropRect.left = cropRatios.left * getWidth();
        cropRect.top = cropRatios.top * getHeight();
        cropRect.right = cropRatios.right * getWidth();
        cropRect.bottom = cropRatios.bottom * getHeight();

        mReverseMatrix.mapRect(cropRect);

        if (cropRect.left >= mBitmap.getWidth() || cropRect.right <= 0 || cropRect.top >= mBitmap.getHeight() || cropRect.bottom <= 0) {
            return;
        }

        if (mPreviousBitmap != null) {
            mPreviousBitmap.recycle();
        }

        mPreviousBitmap = mBitmap;
        mBitmap = null;

        RectF bitmapRect = new RectF(0, 0, mPreviousBitmap.getWidth(), mPreviousBitmap.getHeight());

        float left = cropRect.left;
        float top = cropRect.top;

        cropRect.offset(-left, -top);
        bitmapRect.offset(-left, -top);

        Path cropPath = new Path();

        if (cropMode == Crop.CROP_RECT) {
            cropPath.addRect(cropRect, Path.Direction.CW);
        }
        else {
            cropPath.addOval(cropRect, Path.Direction.CW);
        }

        Path bitmapPath = new Path();
        bitmapPath.addRect(bitmapRect, Path.Direction.CW);

        cropPath.op(bitmapPath, Path.Op.INTERSECT);
        cropRect.intersect(bitmapRect);

        float adjustLeft = cropRect.left;
        float adjustTop = cropRect.top;

        cropRect.offset(-adjustLeft, -adjustTop);
        cropPath.offset(-adjustLeft, -adjustTop);

        left += adjustLeft;
        top += adjustTop;

        Bitmap bitmap = Bitmap.createBitmap((int) cropRect.width(), (int) cropRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.clipPath(cropPath);
        canvas.drawBitmap(mPreviousBitmap, -left, -top, null);

        setBitmap(bitmap);
    }

    public boolean isErasing() {
        return mErasing;
    }

    public void startErasing(float eraserRadius, float eraserOpacity) {
        if (mErasing) {
            return;
        }

        mErasing = true;

        mEraserRadius = eraserRadius;
        mEraserOpacity = eraserOpacity;
        mErasurePaint.setAlpha((int) (255 * (1 - mEraserOpacity)));
    }

    public void cancelErasing() {
        mErasing = false;
        mEraserPath.reset();
        invalidate();
    }

    public void applyErasing() {
        mErasing = false;

        if (mPreviousBitmap != null) {
            mPreviousBitmap.recycle();
        }

        mPreviousBitmap = mBitmap;
        mBitmap = null;

        Bitmap bitmap = Bitmap.createBitmap(mPreviousBitmap.getWidth(), mPreviousBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.save();
        canvas.clipPath(mEraserPath, Region.Op.DIFFERENCE);
        canvas.drawBitmap(mPreviousBitmap, 0, 0, null);
        canvas.restore();

        if (mEraserOpacity < 1) {
            canvas.save();
            canvas.clipPath(mEraserPath);
            canvas.drawBitmap(mPreviousBitmap, 0, 0, mErasurePaint);
            canvas.restore();
        }

        mEraserPath.reset();

        setBitmap(bitmap);
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

        if (mSize > 0 && mImageUri != null) {
            ensureBitmap();
        }
    }

    private void ensureBitmap() {
        BitmapLoader.loadBitmap(getContext(), mImageUri, mSize, bitmap -> {
            if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
                return;
            }

            setBitmap(bitmap);
        });
    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;

        if (mBitmap.getWidth() > mBitmap.getHeight()) {
            mScale = (float) mSize / mBitmap.getWidth();
        }
        else {
            mScale = (float) mSize / mBitmap.getHeight();
        }

        float scaledWidth = mScale * mBitmap.getWidth();
        float scaledHeight = mScale * mBitmap.getHeight();

        mLeft = (mSize - scaledWidth) / 2f;
        mTop = (mSize - scaledHeight) / 2f;

        updateMatrix();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();

        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            child.measure(widthSpec, heightSpec);
            child.layout(0, 0, width, height);
        }
    }

    private void updateMatrix() {
        mMatrix.reset();
        mMatrix.preScale(mScale, mScale, 0, 0);
        mMatrix.postTranslate(mLeft, mTop);

        mMatrix.invert(mReverseMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            canvas.save();
            canvas.setMatrix(mMatrix);

            if (mErasing) {
                canvas.save();
                canvas.clipPath(mEraserPath, Region.Op.DIFFERENCE);
                canvas.drawBitmap(mBitmap, 0, 0, null);
                canvas.restore();

                if (mEraserOpacity < 1) {
                    canvas.save();
                    canvas.clipPath(mEraserPath);
                    canvas.drawBitmap(mBitmap, 0, 0, mErasurePaint);
                    canvas.restore();
                }
            }
            else {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }

            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mErasing) {
            float radius = mReverseMatrix.mapRadius(mEraserRadius);
            mHelperPoint[0] = event.getX();
            mHelperPoint[1] = event.getY();
            mReverseMatrix.mapPoints(mHelperPoint);

            mHelperRect.set(mHelperPoint[0] - radius, mHelperPoint[1] - radius, mHelperPoint[0] + radius, mHelperPoint[1] + radius);
            mEraserPath.addOval(mHelperRect, Path.Direction.CW);

            invalidate();
            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private final StrongGestureDetector.OnGestureListener mOnGestureListener = new StrongGestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent event) {
            return !mErasing;
        }

        @Override
        public void onTranslate(float dx, float dy) {
            mLeft += dx;
            mTop += dy;
            updateMatrix();
            invalidate();
        }

        @Override
        public void onScale(float ds) {
            mScale *= ds;
            updateMatrix();
            invalidate();
        }

        @Override public void onUp() { }
        @Override public void onRotate(float dd) { }

    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }

        if (mPreviousBitmap != null && !mPreviousBitmap.isRecycled()) {
            mPreviousBitmap.recycle();
        }
    }

}
