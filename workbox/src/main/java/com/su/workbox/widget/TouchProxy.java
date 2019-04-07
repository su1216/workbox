package com.su.workbox.widget;

import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.su.workbox.utils.GeneralInfoHelper;

public class TouchProxy {

    private static final int SCALED_TOUCH_SLOP = GeneralInfoHelper.getScaledTouchSlop();
    private OnTouchEventListener mEventListener;
    private TouchState mState = TouchState.STATE_STOP;
    private int mLastX;
    private int mLastY;
    private int mStartX;
    private int mStartY;
    private GestureDetector mGestureDetector = new GestureDetector(GeneralInfoHelper.getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    });

    public TouchProxy(@NonNull OnTouchEventListener eventListener) {
        mEventListener = eventListener;
    }

    public boolean onTouchEvent(@NonNull View v, @NonNull MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            mState = TouchState.STATE_STOP;
            v.performClick();
            return true;
        }

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                mLastY = y;
                mLastX = x;
                mEventListener.onDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mStartX) < SCALED_TOUCH_SLOP && Math.abs(y - mStartY) < SCALED_TOUCH_SLOP) {
                    if (mState == TouchState.STATE_STOP) {
                        break;
                    }
                } else if (mState != TouchState.STATE_MOVE) {
                    mState = TouchState.STATE_MOVE;
                    int smoothXDelta = 0;
                    int smoothYDelta = 0;
                    if (x - mStartX >= SCALED_TOUCH_SLOP) {
                        smoothXDelta = SCALED_TOUCH_SLOP;
                        smoothYDelta = y - mLastY;
                    } else if (mStartX - x >= SCALED_TOUCH_SLOP) {
                        smoothXDelta = -SCALED_TOUCH_SLOP;
                        smoothYDelta = y - mLastY;
                    }
                    if (y - mStartY >= SCALED_TOUCH_SLOP) {
                        smoothYDelta = SCALED_TOUCH_SLOP;
                        smoothXDelta = x - mLastX;
                    } else if (mStartY - y >= SCALED_TOUCH_SLOP) {
                        smoothYDelta = -SCALED_TOUCH_SLOP;
                        smoothXDelta = x - mLastX;
                    }
                    mEventListener.onMove(mLastX, mLastY, x - mLastX - smoothXDelta, y - mLastY - smoothYDelta);
                } else {
                    mEventListener.onMove(mLastX, mLastY, x - mLastX, y - mLastY);
                }

                mLastX = x;
                mLastY = y;
                mState = TouchState.STATE_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                mEventListener.onUp(x, y);
                mState = TouchState.STATE_STOP;
                break;
            default:
                break;
        }
        return true;
    }

    public enum TouchState {
        STATE_MOVE,
        STATE_STOP
    }

    public interface OnTouchEventListener {
        void onMove(int x, int y, int dx, int dy);

        void onUp(int x, int y);

        void onDown(int x, int y);
    }

    public static class SimpleOnTouchEventListener implements OnTouchEventListener {
        public void onMove(int x, int y, int dx, int dy) {}

        public void onUp(int x, int y) {}

        public void onDown(int x, int y) {}
    }
}
