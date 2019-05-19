package com.su.workbox.ui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.su.workbox.R;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.widget.TouchProxy;

public class ScreenColorViewManager implements View.OnTouchListener {

    public static final String TAG = ScreenColorViewManager.class.getSimpleName();
    static final int PIX_INTERVAL = 16;
    static final int PICK_AREA_SIZE = PIX_INTERVAL * 2;
    static final int HALF_PICK_AREA_SIZE = PICK_AREA_SIZE / 2;
    static final int PICK_VIEW_SIZE = PICK_AREA_SIZE * PIX_INTERVAL;
    static final int HALF_PICK_VIEW_SIZE = PICK_VIEW_SIZE / 2;
    @SuppressLint("StaticFieldLeak")
    private static ScreenColorViewManager sController;
    private final TouchProxy mTouchProxy;
    private ViewGroup mRootView;
    private WindowManager.LayoutParams mLayoutParams;
    private Point mLeftTopPoint = new Point();
    private WindowManager mWindowManager;
    private Handler mHandler;
    private ScreenColorPicker mPicker;
    private ScreenColorPickerView mPickerView;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mScreenWidth = GeneralInfoHelper.getScreenWidth();
    private int mScreenHeight = GeneralInfoHelper.getScreenHeight();

    private ScreenColorViewManager() {
        Context context = GeneralInfoHelper.getContext();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createLayoutParams();
        mTouchProxy = new TouchProxy(new TouchProxy.SimpleOnTouchEventListener() {
            @Override
            public void onMove(int x, int y, int dx, int dy) {
                if (mBitmapWidth == 0) {
                    initBitmapSize();
                }
                if (mBitmapWidth == 0) {
                    return;
                }
                Log.d(TAG, "point1: " + mLeftTopPoint);
                checkPoint(dx, dy);
                Log.d(TAG, "point2: " + mLeftTopPoint);

                mLayoutParams.x += dx;
                mLayoutParams.y += dy;
                checkBounds(mLayoutParams);
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);

                showInfo(mLeftTopPoint);
            }

            @Override
            public void onDown(int x, int y) {
                captureInfo(100L);
            }
        });
    }

    public static ScreenColorViewManager getInstance() {
        if (sController == null) {
            sController = new ScreenColorViewManager();
        }
        return sController;
    }

    public void init(Intent intent) {
        mHandler = new Handler(Looper.myLooper());
        Context context = GeneralInfoHelper.getContext();
        mRootView = new FrameLayout(context);

        View view = LayoutInflater.from(context).inflate(R.layout.workbox_wm_color_picker, mRootView, false);
        mRootView.addView(view);
        mRootView.setOnTouchListener(this);
        setupPickerView(view);
        mWindowManager.addView(mRootView, mLayoutParams);

        mPicker = ScreenColorPicker.getInstance();
        mPicker.prepare(Activity.RESULT_OK, intent);
    }

    private void initBitmapSize() {
        if (mBitmapWidth == 0) {
            mBitmapWidth = mPicker.getBitmapWidth();
            mBitmapHeight = mPicker.getBitmapHeight();
        }
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
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void setupPickerView(View view) {
        mPickerView = view.findViewById(R.id.picker_view);
        ViewGroup.LayoutParams params = mPickerView.getLayoutParams();
        //大小必须是2的倍数
        params.width = PICK_VIEW_SIZE;
        params.height = PICK_VIEW_SIZE;
        mPickerView.setLayoutParams(params);
    }

    //圆圈实际显示范围
    private void checkBounds(WindowManager.LayoutParams layoutParams) {
        if (mLeftTopPoint.x < HALF_PICK_VIEW_SIZE) {
            layoutParams.x = 0;
        } else if (mLeftTopPoint.x > mScreenWidth - HALF_PICK_VIEW_SIZE || layoutParams.x > mScreenWidth - HALF_PICK_VIEW_SIZE) {
            layoutParams.x = mScreenWidth - PICK_VIEW_SIZE;
        }
        if (mLeftTopPoint.y < HALF_PICK_VIEW_SIZE) {
            layoutParams.y = 0;
        } else if (mLeftTopPoint.y > mScreenHeight - HALF_PICK_VIEW_SIZE || layoutParams.y > mScreenHeight - HALF_PICK_VIEW_SIZE) {
            layoutParams.y = mScreenHeight - PICK_VIEW_SIZE;
        }
        Log.d(TAG, "checkBounds: " + layoutParams.x + " - " + layoutParams.y);
    }

    //聚焦范围
    private void checkPoint(int dx, int dy) {
        Log.d(TAG, "dx: " + dx + " - dy: " + dy);
        mLeftTopPoint.x += dx;
        mLeftTopPoint.y += dy;

        if (mLeftTopPoint.x < -HALF_PICK_AREA_SIZE) {
            mLeftTopPoint.x = -HALF_PICK_AREA_SIZE;
        } else if (mLeftTopPoint.x > mBitmapWidth - HALF_PICK_AREA_SIZE - 1) {
            mLeftTopPoint.x = mBitmapWidth - HALF_PICK_AREA_SIZE - 1;
        }

        if (mLeftTopPoint.y < -HALF_PICK_AREA_SIZE) {
            mLeftTopPoint.y = -HALF_PICK_AREA_SIZE;
        } else if (mLeftTopPoint.y > mBitmapHeight - HALF_PICK_AREA_SIZE - 1) {
            mLeftTopPoint.y = mBitmapHeight - HALF_PICK_AREA_SIZE - 1;
        }
    }

    private void showInfo(Point leftTopPoint) {
        Bitmap bitmap = mPicker.getFocusedBitmap(leftTopPoint);
        if (bitmap == null) {
            return;
        }

        int xCenter = bitmap.getWidth() / 2;
        int yCenter = bitmap.getHeight() / 2;
        int colorInt = getPixel(bitmap, xCenter, yCenter);
        mPickerView.setBitmap(bitmap, colorInt, leftTopPoint.x + HALF_PICK_AREA_SIZE, leftTopPoint.y + HALF_PICK_AREA_SIZE);
    }

    public void performDestroy() {
        mWindowManager.removeViewImmediate(mRootView);
        mPicker.release();
        mHandler = null;
        mRootView = null;
        sController = null;
    }

    public static boolean isInitialized() {
        return sController != null;
    }

    private void captureInfo(long delay) {
        mRootView.setVisibility(View.GONE);
        mHandler.postDelayed(() -> {
            mPicker.recording();
            mRootView.setVisibility(View.VISIBLE);
        }, delay);

    }

    public static int getPixel(Bitmap bitmap, int x, int y) {
        if (bitmap == null) {
            return -1;
        }
        if (x < 0 || x > bitmap.getWidth()) {
            return -1;
        }
        if (y < 0 || y > bitmap.getHeight()) {
            return -1;
        }
        return bitmap.getPixel(x, y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mTouchProxy.onTouchEvent(v, event);
    }
}
