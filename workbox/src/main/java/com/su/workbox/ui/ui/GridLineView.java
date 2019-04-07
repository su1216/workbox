package com.su.workbox.ui.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;

public class GridLineView extends View {

    private static GridLineView sGridLineView;
    private int mUnit;
    private boolean mStatusBar;

    private boolean mIsShowing;
    private Paint mPaint = new Paint();

    private GridLineView(Context context) {
        super(context);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.parseColor("#40000000"));
        mUnit = UiHelper.dp2px(4);
    }

    public static GridLineView getInstance() {
        if (sGridLineView == null) {
            sGridLineView = new GridLineView(GeneralInfoHelper.getContext());
        }
        return sGridLineView;
    }

    private void resetView() {
        SharedPreferences sharedPreferences = SpHelper.getWorkboxSharedPreferences();
        String colorString = sharedPreferences.getString(SpHelper.COLUMN_GRID_LINE_COLOR_STRING, "#40000000");
        int size = Integer.parseInt(sharedPreferences.getString(SpHelper.COLUMN_GRID_LINE_SIZE, "4"));
        int unit = Integer.parseInt(sharedPreferences.getString(SpHelper.COLUMN_GRID_LINE_UNIT, "0"));
        mPaint.setColor(Color.parseColor(colorString));
        if (unit == 0) {
            mUnit = UiHelper.dp2px(size);
        } else {
            mUnit = size;
        }
        mStatusBar = sharedPreferences.getBoolean(SpHelper.COLUMN_GRID_LINE_STATUS_BAR, false);
    }

    private void show() {
        resetView();
        WindowManager windowManager = (WindowManager) GeneralInfoHelper.getContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (mStatusBar) {
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        params.format = PixelFormat.TRANSLUCENT;
        windowManager.addView(sGridLineView, params);
        mIsShowing = true;
    }

    private void dismiss() {
        WindowManager windowManager = (WindowManager) GeneralInfoHelper.getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeView(sGridLineView);
        mIsShowing = false;
    }

    public void toggle() {
        if (mIsShowing) {
            dismiss();
        } else {
            show();
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int startX = 0;
        while (startX < measuredWidth) {
            canvas.drawLine(startX, 0, startX, measuredHeight, mPaint);
            startX += mUnit;
        }

        int startY = 0;
        while (startY < measuredHeight) {
            canvas.drawLine(0, startY, measuredWidth, startY, mPaint);
            startY += mUnit;
        }
    }
}
