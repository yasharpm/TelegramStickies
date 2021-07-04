package com.yashoid.telegramstickies;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StickyThingy {

    private static final String DEFAULT_BODY = "{\"tgs\":1,\"v\":\"5.7.1\",\"fr\":60,\"ip\":0,\"op\":0,\"w\":0,\"h\":0,\"ddd\":0,\"assets\":[],\"layers\":[],\"markers\":[]}";

    private final ParsedSticker mSticker;
    private final Map<StickerColor, StickerColor> mColorReplacements = new HashMap<>();

    private final List<StickyThingy> mLayers = new LinkedList<>();

    private int mDuration;

    private float mWidth;
    private float mHeight;

    private float mLeft = 0;
    private float mTop = 0;

    private float mScaleX = 1;
    private float mScaleY = 1;

    private float mRotation = 0;

    private int mTimeShift = 0;
    private float mDurationScale = 1;

    public StickyThingy() {
        mSticker = null;

        mDuration = 0;
        mWidth = 0;
        mHeight = 0;
    }

    public StickyThingy(String json) throws JSONException {
        mSticker = new ParsedSticker(json);

        mDuration = mSticker.getDuration();
        mWidth = mSticker.getWidth();
        mHeight = mSticker.getHeight();
    }

    public int getRealDuration() {
        return mDuration;
    }

    public int getScaledDuration() {
        return (int) (mDuration * mDurationScale);
    }

    public void shiftTime(int by) {
        mTimeShift += by;
    }

    public int getTimeShift() {
        return mTimeShift;
    }

    public void scaleDuration(int targetDuration) {
        mDurationScale = (float) targetDuration / mDuration;
    }

    public float getWidth() {
        return mWidth * mScaleX;
    }

    public float getHeight() {
        return mHeight * mScaleY;
    }

    public void setWidth(float width) {
        mWidth = width / mScaleX;
    }

    public void setHeight(float height) {
        mHeight = height / mScaleY;
    }

    public void translate(float dx, float dy) {
        mLeft += dx;
        mTop += dy;
    }

    public float getLeft() {
        return mLeft;
    }

    public float getTop() {
        return mTop;
    }

    public void resize(float pivotU, float pivotV, float newWidth, float newHeight) {
        if (mWidth == 0 || mHeight == 0 || newWidth <= 0 || newHeight <= 0) {
            return;
        }

        float targetX = pivotU * getWidth();
        float targetY = pivotV * getHeight();

        mScaleX = newWidth / mWidth;
        mScaleY = newHeight / mHeight;

        float x = getWidth() * pivotU;
        float y = getHeight() * pivotV;

        mLeft += targetX - x;
        mTop += targetY - y;
    }

    public void rotateBy(float degrees) {
        mRotation += degrees;
    }

    public float getRotation() {
        return mRotation;
    }

    public Set<StickerColor> getColors() throws JSONException {
        return mSticker.getColors();
    }

    public void replaceColor(StickerColor what, StickerColor withWhat) {
        mColorReplacements.put(what, withWhat);
    }

    public void resetColorReplacements() {
        mColorReplacements.clear();;
    }

    public void add(StickyThingy stickyThingy) throws JSONException {
        mLayers.add(stickyThingy);
        updateInfo();
    }

    public void remove(int index) throws JSONException {
        mLayers.remove(index);
        updateInfo();
    }

    public void remove(StickyThingy stickyThingy) throws JSONException {
        mLayers.remove(stickyThingy);
        updateInfo();
    }

    public void updateInfo() throws JSONException {
        mDuration = mSticker == null ? 0 : mSticker.getDuration();
        mWidth = mSticker == null ? 0 : mSticker.getWidth();
        mHeight = mSticker == null ? 0 : mSticker.getHeight();

        for (StickyThingy stickyThingy: mLayers) {
            mDuration = Math.max(mDuration, stickyThingy.getTimeShift() + stickyThingy.getRealDuration());
            mWidth = Math.max(mWidth, stickyThingy.mWidth);
            mHeight = Math.max(mHeight, stickyThingy.mHeight);
        }
    }

    public StickyThingy get(int index) {
        return mLayers.get(index);
    }

    public int getChildCount() {
        return mLayers.size();
    }

    public void reorder(int from, int to) {
        StickyThingy temp = mLayers.get(to);
        mLayers.set(to, mLayers.get(from));
        mLayers.set(from, temp);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject(DEFAULT_BODY);

        ParsedSticker.updateBody(json, Math.max(0, getWidth()), Math.max(0, getHeight()), Math.max(0, getTimeShift() + getScaledDuration()));

        double fixRadians = Math.toRadians(mRotation);
        float rotationFixX = getWidth() / 2 - (float) (getWidth() / 2 * Math.cos(fixRadians) - getHeight() / 2 * Math.sin(fixRadians));
        float rotationFixY = getHeight() / 2 - (float) (getWidth() / 2 * Math.sin(fixRadians) + getHeight() / 2 * Math.cos(fixRadians));

        if (mSticker != null) {
            mSticker.apply(json, mTimeShift, mDurationScale, mLeft + rotationFixX, mScaleX, mTop + rotationFixY, mScaleY, mRotation, mColorReplacements);
        }

        for (StickyThingy stickyThingy: mLayers) {
            ParsedSticker.apply(stickyThingy.toJSON(), json, mTimeShift, mDurationScale, mLeft, mScaleX, mTop, mScaleY, mRotation, mColorReplacements);
        }

        return json;
    }

}
