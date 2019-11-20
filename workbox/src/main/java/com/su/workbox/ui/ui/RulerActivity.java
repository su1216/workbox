package com.su.workbox.ui.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;

public class RulerActivity extends BaseAppCompatActivity {

    public static final String TAG = RulerActivity.class.getSimpleName();
    private static boolean sShowing;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, RulerActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
        boolean statusBar = sp.getBoolean(SpHelper.COLUMN_MEASURE_STATUS_BAR, false);
        boolean navigationBar = sp.getBoolean(SpHelper.COLUMN_MEASURE_NAVIGATION_BAR, false);

        View decorView = window.getDecorView();
        if (statusBar) {
            UiHelper.setStatusBarColor(window, Color.TRANSPARENT);
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | uiOptions);
        }
        if (navigationBar) {
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | uiOptions);
        }
        RulerView rulerView = new RulerView(this);
        setContentView(rulerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sShowing = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sShowing = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    public static boolean isShowing() {
        return sShowing;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
