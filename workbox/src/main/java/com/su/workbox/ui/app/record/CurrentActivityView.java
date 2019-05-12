package com.su.workbox.ui.app.record;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.su.workbox.AppLifecycleListener;
import com.su.workbox.R;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;

import java.util.Observable;
import java.util.Observer;

public class CurrentActivityView extends AppCompatTextView implements Observer {

    private static CurrentActivityView sCurrentActivityView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private Paint mPaint;
    private boolean mIsShowing;
    private Activity mTopActivity;
    private boolean mShowTaskInfo;
    private boolean mShowComponent;
    private int mPaddingStartEnd;

    public static CurrentActivityView getInstance() {
        if (sCurrentActivityView == null) {
            sCurrentActivityView = new CurrentActivityView(GeneralInfoHelper.getContext());
        }
        return sCurrentActivityView;
    }

    public CurrentActivityView(Context context) {
        super(context);
        mPaddingStartEnd = UiHelper.dp2px(4);
        setPadding(mPaddingStartEnd, UiHelper.dp2px(2), mPaddingStartEnd, UiHelper.dp2px(2));
        int color = context.getResources().getColor(R.color.workbox_black_50);
        setBackgroundColor(color);

        int textSize = UiHelper.sp2px(14);
        mPaint = new Paint();
        mPaint.setTextSize(textSize);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setTextColor(Color.WHITE);
        setGravity(Gravity.START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
        }

        mWindowManager = (WindowManager) GeneralInfoHelper.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    private void resetView() {
        SharedPreferences sharedPreferences = SpHelper.getWorkboxSharedPreferences();
        mShowTaskInfo = sharedPreferences.getBoolean(SpHelper.COLUMN_CURRENT_TASK, false);
        mShowComponent = sharedPreferences.getBoolean(SpHelper.COLUMN_CURRENT_COMPONENT, false);
    }

    private void open() {
        resetView();
        mIsShowing = true;
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.START | Gravity.BOTTOM;
        mWindowManager.addView(this, mLayoutParams);
        AppLifecycleListener.getInstance().addObserver(this);
    }

    private void close() {
        mIsShowing = false;
        mWindowManager.removeView(this);
        AppLifecycleListener.getInstance().deleteObserver(this);
    }

    public void toggle() {
        if (mIsShowing) {
            close();
        } else {
            open();
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void updateTopActivity(Activity activity) {
        mTopActivity = activity;
        if (mTopActivity == null) {
            setText(null);
            return;
        }

        String className = mTopActivity.getClass().getName();
        String info = className;
        if (mShowTaskInfo) {
            info += "\nroot: " + mTopActivity.isTaskRoot() + "  taskId: " + mTopActivity.getTaskId();
        }
        if (mShowComponent) {
            ComponentName componentName = mTopActivity.getComponentName();
            String packageName = componentName.getPackageName();
            String componentClassName = componentName.getClassName();
            info += "\npackageName: " + packageName;
            if (TextUtils.equals(className, componentClassName)) {
                info += "\nclassName: " + componentClassName;
            } else {
                int count = info.length();
                info += "\nalias: " + componentClassName;
                int color = getContext().getResources().getColor(R.color.workbox_color_accent);
                ForegroundColorSpan span1 = new ForegroundColorSpan(color);
                ForegroundColorSpan span2 = new ForegroundColorSpan(color);
                SpannableStringBuilder ssb = new SpannableStringBuilder(info);
                ssb.setSpan(span1, 0, className.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ssb.setSpan(span2, count + 1, count + 8 + componentClassName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                setWidth((int) getMaxWidth(info));
                mWindowManager.updateViewLayout(this, mLayoutParams);
                setText(ssb);
                return;
            }
        }

        setWidth((int) getMaxWidth(info));
        mWindowManager.updateViewLayout(this, mLayoutParams);
        setText(info);
    }

    @Override
    public void setWidth(int maxWidth) {
        int max = maxWidth + mPaddingStartEnd * 2;
        if (max > GeneralInfoHelper.getScreenWidth()) {
            mLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        } else {
            mLayoutParams.width = max;
        }
    }

    private float getMaxWidth(String info) {
        String[] ss = info.split("\n");
        float maxLength = 0;
        for (String s : ss) {
            float length = mPaint.measureText(s);
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean isForeground = (Boolean) arg;
        if (isForeground) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }
}
