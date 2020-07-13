package com.su.workbox.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.su.workbox.AppHelper;
import com.su.workbox.R;

import java.lang.reflect.Field;

/**
 * Created by su on 15-11-18.
 */
public final class UiHelper {
    private static final String TAG = UiHelper.class.getSimpleName();

    private UiHelper() {}

    public static AlertDialog showConfirm(Context context, String tip, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.workbox_confirm, listener)
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    public static AlertDialog showConfirm(Context context, String tip) {
        return new AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.workbox_confirm, null)
                .show();
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                actionBarHeight += dp2px(8);
            }
        }
        return actionBarHeight;
    }

    public static int dp2px(float dpValue) {
        DisplayMetrics displayMetrics = GeneralInfoHelper.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, displayMetrics);
    }

    public static int sp2px(float spValue) {
        DisplayMetrics displayMetrics = GeneralInfoHelper.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics);
    }

    public static int px2dp(float pxValue) {
        final float scale = GeneralInfoHelper.getContext().getResources().getDisplayMetrics().density;
        return pxValue > 0 ? (int) (pxValue / scale + 0.5f) : -(int) (-pxValue / scale + 0.5f);
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //获取魅族smartbar高度
    public static int getSmartBarHeight(Context context) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object obj = clazz.newInstance();
            Field field = clazz.getField("mz_action_button_min_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }
        return 0;
    }

    //https://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = new Point(GeneralInfoHelper.getScreenWidth(), GeneralInfoHelper.getScreenHeight());

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static int getNavigationBarHeight(Context context) {
        int height = getNavigationBarSize(context).y;
        if (height == 0 && AppHelper.isFlyme()) {
            height = getSmartBarHeight(context);
        }
        return height;
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getTextBaseline(int viewHeight, Paint.FontMetricsInt fontMetricsInt) {
        return (viewHeight - (fontMetricsInt.descent - fontMetricsInt.ascent)) / 2 - fontMetricsInt.ascent;
    }

    public static String getThemeName(int theme) {
        try {
            return GeneralInfoHelper.getContext().getResources().getResourceEntryName(theme);
        } catch (Resources.NotFoundException e) {
            return "unknown";
        }
    }
}
