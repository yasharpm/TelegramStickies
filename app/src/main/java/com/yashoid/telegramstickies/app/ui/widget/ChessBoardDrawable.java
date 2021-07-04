package com.yashoid.telegramstickies.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yashoid.telegramstickies.app.R;

public class ChessBoardDrawable extends Drawable {

    private static final int SQUARE_COUNT = 22;

    private final Paint mWhitePaint;
    private final Paint mBlackPaint;

    public ChessBoardDrawable(Context context) {
        mWhitePaint = new Paint();
        mWhitePaint.setStyle(Paint.Style.FILL);
        mWhitePaint.setColor(ContextCompat.getColor(context, R.color.white));

        mBlackPaint = new Paint();
        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setColor(ContextCompat.getColor(context, R.color.gray));
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        outline.setRect(getBounds());
        outline.setAlpha(1);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        canvas.drawRect(bounds, mWhitePaint);

        float squareSize = (float) bounds.width() / SQUARE_COUNT;

        float left;
        float nextLeft;

        float top = bounds.top;
        float nextTop = top + squareSize;

        for (int i = 0; i < SQUARE_COUNT; i++) {
            left = bounds.left;
            nextLeft = left + squareSize;

            for (int j = 0; j < SQUARE_COUNT; j ++) {
                if ((i + j) % 2 == 0) {
                    canvas.drawRect(left, top, nextLeft, nextTop, mBlackPaint);
                }

                left = nextLeft;
                nextLeft += squareSize;
            }

            top = nextTop;
            nextTop += squareSize;
        }
    }

    @Override public void setAlpha(int alpha) { }

    @Override public void setColorFilter(@Nullable ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

}
