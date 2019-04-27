package com.su.workbox.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

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

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public static double getScreenDiagonalSize(DisplayMetrics displayMetrics, Point point) {
        return Math.sqrt(Math.pow(point.x / displayMetrics.xdpi, 2.0d) + Math.pow(point.y / displayMetrics.ydpi, 2.0d));
    }

    public static void setStatusBarColor(Window window, int color) {
        setStatusBarColor(window, color, true);
    }

    public static void setStatusBarColor(Window window, int color, boolean isAddView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0及以上，不设置透明状态栏，设置会有半透明阴影
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (isAddView) {
                View statusBarView = new View(window.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(window.getContext()));
                statusBarView.setLayoutParams(params);
                statusBarView.setBackgroundColor(color);
                ViewGroup decorView = (ViewGroup) window.getDecorView();
                decorView.addView(statusBarView);
            }
            ViewGroup contentView = window.findViewById(android.R.id.content);
            View rootView = contentView.getChildAt(0);
            if (rootView instanceof ViewGroup) {
                rootView.setFitsSystemWindows(true);
            }
        }
    }

    /**
     * 设置颜色透明度
     *
     * @param alpha 目标透明度 eg:0xAA000000
     */
    public static int resetColorAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | alpha;
    }

    public static String color2rgbString(int color) {
        return String.format("#%06X", color);
    }

    @ColorInt
    public static int getComplementaryColor(@ColorInt int originColor) {
        return Color.rgb(255 - Color.red(originColor), 255 - Color.green(originColor), 255 - Color.blue(originColor));
    }

    public static boolean isDarkColor(@ColorInt int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        return hsl[2] <= 0.5f;
    }

    public static void setNavigationBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
    }

    public static int getTextBaseline(int viewHeight, Paint.FontMetricsInt fontMetricsInt) {
        return (viewHeight - (fontMetricsInt.descent - fontMetricsInt.ascent)) / 2 - fontMetricsInt.ascent;
    }
}
