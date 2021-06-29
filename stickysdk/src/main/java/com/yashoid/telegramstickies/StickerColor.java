package com.yashoid.telegramstickies;

import android.graphics.Color;

import java.util.Arrays;

public class StickerColor {

    private float[] mColor = new float[4];

    public StickerColor(float... color) {
        mColor = color;
    }

    public StickerColor(int color) {
        mColor[0] = Color.red(color) / 255f;
        mColor[1] = Color.green(color) / 255f;
        mColor[2] = Color.blue(color) / 255f;
        mColor[3] = Color.alpha(color) / 255f;
    }

    public float[] rawValue() {
        return mColor;
    }

    public int intValue() {
        return Color.argb(rawToInt(mColor[3]), rawToInt(mColor[0]), rawToInt(mColor[1]), rawToInt(mColor[2]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StickerColor that = (StickerColor) o;

        return Arrays.equals(mColor, that.mColor);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mColor);
    }

    private static int rawToInt(float v) {
        return (int) (v * 255.0f + 0.5f);
    }

}
