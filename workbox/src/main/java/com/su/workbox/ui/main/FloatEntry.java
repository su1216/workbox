package com.su.workbox.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.entity.Module;
import com.su.workbox.ui.base.AppLifecycleListener;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.TouchProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by su on 19-11-9.
 */
public class FloatEntry implements View.OnTouchListener, View.OnClickListener, Observer {

    @SuppressLint("StaticFieldLeak")
    private static FloatEntry sFloatEntry;
    @SuppressLint("StaticFieldLeak")
    private static WorkboxPanel sWorkboxPanel;
    private TouchProxy mTouchProxy;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private ViewGroup mRootView;
    private int mScreenWidth = GeneralInfoHelper.getScreenWidth();
    private int mScreenHeight = GeneralInfoHelper.getScreenHeight();
    private int mViewSize;
    private List<Module> mAllModuleList = new ArrayList<>(WorkboxPanel.MODULE_LIST);
    private List<Module> mModuleList = new ArrayList<>();
    private boolean mPanelDisplay;

    public static FloatEntry getInstance() {
        if (sFloatEntry == null) {
            sFloatEntry = new FloatEntry();
        }
        return sFloatEntry;
    }

    private FloatEntry() {
        AppLifecycleListener.getInstance().addObserver(this);
        //添加自定义模块
        mAllModuleList.addAll(WorkboxSupplier.getInstance().getCustomModules());
        filterModules();
    }

    private void createLayoutParams() {
        Context context = GeneralInfoHelper.getContext();
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.START;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        int viewSize = UiHelper.dp2px(64);
        int height = mScreenHeight - UiHelper.getStatusBarHeight(context) - UiHelper.getNavigationBarHeight(context);
        mLayoutParams.x = mScreenWidth - viewSize;
        mLayoutParams.y = (height - viewSize) / 2;
    }

    private void init() {
        if (mWindowManager != null) {
            return;
        }
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

        mRootView = new FrameLayout(context);
        View view = LayoutInflater.from(context).inflate(R.layout.workbox_wm_float_entry, mRootView, false);
        mViewSize = view.getLayoutParams().width;
        mRootView.addView(view);
        mRootView.setOnTouchListener(this);
        mRootView.setOnClickListener(this);
        mWindowManager.addView(mRootView, mLayoutParams);
        sWorkboxPanel = new WorkboxPanel(this);
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

    public void hide() {
        init();
        if (mPanelDisplay) {
            sWorkboxPanel.hide();
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    public void show() {
        init();
        if (mPanelDisplay) {
            sWorkboxPanel.show();
        } else {
            mRootView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (sWorkboxPanel.isShown()) {
            return;
        }
        filterModules();
        hide();
        mPanelDisplay = true;
        sWorkboxPanel.show(mModuleList);
    }

    private void filterModules() {
        mModuleList.clear();
        List<String> enabledList = WorkboxPanel.getEnableModuleList();
        List<String> invalidList = new ArrayList<>();
        for (String id : enabledList) {
            boolean find = false;
            for (Module module : mAllModuleList) {
                if (TextUtils.equals(id, module.getId())) {
                    mModuleList.add(module);
                    find = true;
                    break;
                }
            }
            if (!find) {
                invalidList.add(id);
            }
        }
        if (!invalidList.isEmpty()) {
            enabledList.removeAll(invalidList);
            SpHelper.setPanelList(enabledList);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mTouchProxy.onTouchEvent(v, event);
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean isForeground = (Boolean) arg;
        if (SpHelper.getWorkboxSharedPreferences().getBoolean(SpHelper.COLUMN_PANEL_ICON, true)
                && AppHelper.hasSystemWindowPermission(GeneralInfoHelper.getContext())) {
            getInstance().init();
        } else {
            return;
        }
        if (isForeground) {
            show();
        } else {
            hide();
        }
    }

    void onPanelClick() {
        mPanelDisplay = false;
        show();
    }

    void onPanelOutsideClick() {
        mPanelDisplay = false;
        show();
    }
}
