package com.yashoid.telegramstickies.app.ui.widget;

import android.graphics.PointF;
import android.view.MotionEvent;

public class StrongGestureDetector {

    public interface OnGestureListener {

        boolean onDown(MotionEvent event);

        void onUp();

        void onTranslate(float dx, float dy);

        void onScale(float ds);

        void onRotate(float dd);

    }

    private final OnGestureListener mOnGestureListener;

    private int mFirstTouchId = -1;
    private int mSecondTouchId = -1;

    private final MotionEvent.PointerCoords mFirstTouch = new MotionEvent.PointerCoords();
    private final MotionEvent.PointerCoords mSecondTouch = new MotionEvent.PointerCoords();

    private final MotionEvent.PointerCoords mHelperCoords1 = new MotionEvent.PointerCoords();
    private final MotionEvent.PointerCoords mHelperCoords2 = new MotionEvent.PointerCoords();

    private final State mHelperState1 = new State();
    private final State mHelperState2 = new State();

    public StrongGestureDetector(OnGestureListener listener) {
        mOnGestureListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mFirstTouchId == -1) {
                    if (mOnGestureListener.onDown(event)) {
                        mFirstTouchId = event.getPointerId(event.getActionIndex());
                        event.getPointerCoords(event.getActionIndex(), mFirstTouch);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else if (mSecondTouchId == -1) {
                    mSecondTouchId = event.getPointerId(event.getActionIndex());
                    event.getPointerCoords(event.getActionIndex(), mSecondTouch);
                    return true;
                }
                else {
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                if (mFirstTouchId != -1) {
                    processMove(event);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
                mFirstTouchId = -1;
                mSecondTouchId = -1;

                mOnGestureListener.onUp();
                return true;
            default:
                int touchId = event.getPointerId(event.getActionIndex());

                if (touchId == mFirstTouchId) {
                    mFirstTouchId = mSecondTouchId;
                    mFirstTouch.copyFrom(mSecondTouch);
                    mSecondTouchId = -1;

                    if (mFirstTouchId == -1) {
                        mOnGestureListener.onUp();
                    }
                    return true;
                }
                else if (touchId == mSecondTouchId) {
                    mSecondTouchId = -1;
                    return true;
                }

                return false;
        }
    }

    private void processMove(MotionEvent event) {
        if (mSecondTouchId == -1) {
            event.getPointerCoords(event.findPointerIndex(mFirstTouchId), mHelperCoords1);

            float dx = mHelperCoords1.x - mFirstTouch.x;
            float dy = mHelperCoords1.y - mFirstTouch.y;

            mFirstTouch.copyFrom(mHelperCoords1);

            mOnGestureListener.onTranslate(dx, dy);
            return;
        }

        measureState(mFirstTouch, mSecondTouch, mHelperState1);

        event.getPointerCoords(event.findPointerIndex(mFirstTouchId), mHelperCoords1);
        event.getPointerCoords(event.findPointerIndex(mSecondTouchId), mHelperCoords2);
        measureState(mHelperCoords1, mHelperCoords2, mHelperState2);

        measureOffset(mHelperState1, mHelperState2, mHelperState1);

        mFirstTouch.copyFrom(mHelperCoords1);
        mSecondTouch.copyFrom(mHelperCoords2);

        if (mHelperState1.x != 0 || mHelperState1.y != 0) {
            mOnGestureListener.onTranslate(mHelperState1.x, mHelperState1.y);
        }

        if (mHelperState1.d != 1) {
            mOnGestureListener.onScale(mHelperState1.d);
        }

        if (mHelperState1.a != 0) {
            mOnGestureListener.onRotate((float) Math.toDegrees(mHelperState1.a));
        }
    }

    private static void measureState(MotionEvent.PointerCoords e1, MotionEvent.PointerCoords e2, State state) {
        state.x = (e1.x + e2.x) / 2f;
        state.y = (e1.y + e2.y) / 2f;
        state.d = PointF.length(e1.x - e2.x, e1.y - e2.y);

        if (e1.x == e2.x) {
            state.a = e1.y < e2.y ? Math.PI / 2d : 3d * Math.PI / 2d;
        }
        else {
            state.a = Math.atan((e1.y - e2.y) / (e1.x - e2.x));

            if (e1.y < e2.y) {
                if (e1.x > e2.x) {
                    state.a += Math.PI;
                }
            }
            else if (e1.x > e2.x) {
                state.a += Math.PI;
            }
            else {
                state.a = 2 * Math.PI + state.a;
            }
        }
    }

    private static void measureOffset(State a, State b, State out) {
        out.x = b.x - a.x;
        out.y = b.y - a.y;
        out.d = a.d == 0 ? 0 : b.d / a.d;
        out.a = b.a - a.a;
    }

    private static class State {

        float x;
        float y;
        float d;
        double a;

    }

}
