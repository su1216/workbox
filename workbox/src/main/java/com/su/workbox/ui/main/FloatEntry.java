package com.su.workbox.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.su.workbox.R;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.TouchProxy;

public class FloatEntry implements View.OnTouchListener, View.OnClickListener {

    @SuppressLint("StaticFieldLeak")
    private static FloatEntry sFloatEntry;
    private final TouchProxy mTouchProxy;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private ViewGroup mRootView;
    private int mScreenWidth = GeneralInfoHelper.getScreenWidth();
    private int mScreenHeight = GeneralInfoHelper.getScreenHeight();
    private int mViewSize;

    public static FloatEntry getInstance() {
        if (sFloatEntry == null) {
            sFloatEntry = new FloatEntry();
        }
        return sFloatEntry;
    }

    private FloatEntry() {
        Context context = GeneralInfoHelper.getContext();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createLayoutParams();
        mTouchProxy = new TouchProxy(new TouchProxy.SimpleOnTouchEventListener() {
            @Override
            public void onMove(int x, int y, int dx, int dy) {
                mLayoutParams.x += dx;
                mLayoutParams.y += dy;
                checkBounds(mLayoutParams);
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
            }
        });
        init();
    }

    private void createLayoutParams() {
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void init() {
        Context context = GeneralInfoHelper.getContext();
        mRootView = new FrameLayout(context);
        View view = LayoutInflater.from(context).inflate(R.layout.workbox_wm_float_entry, mRootView, false);
        mViewSize = view.getLayoutParams().width;
        mRootView.addView(view);
        mRootView.setOnTouchListener(this);
        mRootView.setOnClickListener(this);
        mWindowManager.addView(mRootView, mLayoutParams);
    }

    private void checkBounds(WindowManager.LayoutParams layoutParams) {
        if (layoutParams.x < 0) {
            layoutParams.x = 0;
        } else if (layoutParams.x > mScreenWidth - mViewSize) {
            layoutParams.x = mScreenWidth - mViewSize;
        }
        if (layoutParams.y < 0) {
            layoutParams.y = 0;
        } else if (layoutParams.y > mScreenHeight - mViewSize) {
            layoutParams.y = mScreenHeight - mViewSize;
        }
    }

    public void destroy() {
        mWindowManager.removeViewImmediate(mRootView);
        mRootView = null;
    }

    @Override
    public void onClick(View v) {
        new ToastBuilder("onClick").show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mTouchProxy.onTouchEvent(v, event);
    }
}
