package com.su.workbox.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.entity.Module;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by su on 19-11-9.
 */
public class WorkboxPanel implements View.OnClickListener {

    public static final List<String> DEFAULT_PANEL_MODULES = Arrays.asList(Workbox.MODULE_CRASH_LOG,
            Workbox.MODULE_APP_INFO,
            Workbox.MODULE_DEVICE_INFO,
            Workbox.MODULE_LAUNCHER,
            Workbox.MODULE_MAIN);

    public static final List<Module> MODULE_LIST = new ArrayList<>();

    static {
        Context context = GeneralInfoHelper.getContext();
        Module dataExportModule = new Module();
        dataExportModule.setId(Workbox.MODULE_DATA_EXPORT);
        dataExportModule.setName("数据导出");
        dataExportModule.setOnClickListener(v -> startActivity(Workbox.MODULE_DATA_EXPORT, context));
        MODULE_LIST.add(dataExportModule);

        Module permissionsModule = new Module();
        permissionsModule.setId(Workbox.MODULE_PERMISSIONS);
        permissionsModule.setName("权限列表");
        permissionsModule.setOnClickListener(v -> startActivity(Workbox.MODULE_PERMISSIONS, context));
        MODULE_LIST.add(permissionsModule);

        Module activitiesModule = new Module();
        activitiesModule.setId(Workbox.MODULE_LAUNCHER);
        activitiesModule.setName("任意门");
        activitiesModule.setOnClickListener(v -> startActivity(Workbox.MODULE_LAUNCHER, context));
        MODULE_LIST.add(activitiesModule);

        Module mockDataModule = new Module();
        mockDataModule.setId(Workbox.MODULE_MOCK_DATA);
        mockDataModule.setName("数据模拟");
        mockDataModule.setOnClickListener(v -> startActivity(Workbox.MODULE_MOCK_DATA, context));
        MODULE_LIST.add(mockDataModule);

        Module jsInterfaceModule = new Module();
        jsInterfaceModule.setId(Workbox.MODULE_JS_INTERFACES);
        jsInterfaceModule.setName("前端调试");
        jsInterfaceModule.setOnClickListener(v -> startActivity(Workbox.MODULE_JS_INTERFACES, context));
        MODULE_LIST.add(jsInterfaceModule);

        Module appInfoModule = new Module();
        appInfoModule.setId(Workbox.MODULE_APP_INFO);
        appInfoModule.setName("应用信息");
        appInfoModule.setOnClickListener(v -> startActivity(Workbox.MODULE_APP_INFO, context));
        MODULE_LIST.add(appInfoModule);

        Module deviceInfoModule = new Module();
        deviceInfoModule.setId(Workbox.MODULE_DEVICE_INFO);
        deviceInfoModule.setName("设备信息");
        deviceInfoModule.setOnClickListener(v -> startActivity(Workbox.MODULE_DEVICE_INFO, context));
        MODULE_LIST.add(deviceInfoModule);

        Module databasesModule = new Module();
        databasesModule.setId(Workbox.MODULE_DATABASES);
        databasesModule.setName("数据库");
        databasesModule.setOnClickListener(v -> startActivity(Workbox.MODULE_DATABASES, context));
        MODULE_LIST.add(databasesModule);

        Module lifecycleModule = new Module();
        lifecycleModule.setId(Workbox.MODULE_LIFECYCLE);
        lifecycleModule.setName("生命周期");
        lifecycleModule.setOnClickListener(v -> startActivity(Workbox.MODULE_LIFECYCLE, context));
        MODULE_LIST.add(lifecycleModule);

        Module crashLogModule = new Module();
        crashLogModule.setId(Workbox.MODULE_CRASH_LOG);
        crashLogModule.setName("崩溃日志");
        crashLogModule.setOnClickListener(v -> startActivity(Workbox.MODULE_CRASH_LOG, context));
        MODULE_LIST.add(crashLogModule);

        Module mainModule = new Module();
        mainModule.setId(Workbox.MODULE_MAIN);
        mainModule.setName("功能列表");
        mainModule.setOnClickListener(v -> startActivity(Workbox.MODULE_MAIN, context));
        MODULE_LIST.add(mainModule);

        intModulesState();
    }

    private ViewGroup mRootView;
    private final WindowManager mWindowManager;
    private final ModuleAdapter mModuleAdapter;
    private List<Module> mModuleList = new ArrayList<>();
    private boolean mShown;
    private FloatEntry mFloatEntry;

    public static List<String> getEnableModuleList() {
        List<String> panelList = SpHelper.getPanelList();
        List<String> enabledList;
        if (panelList.isEmpty()) {
            enabledList = WorkboxPanel.DEFAULT_PANEL_MODULES;
        } else {
            enabledList = panelList;
        }
        return enabledList;
    }

    static void intModulesState() {
        List<String> enabledList = getEnableModuleList();
        for (String id : enabledList) {
            for (Module module : MODULE_LIST) {
                if (TextUtils.equals(id, module.getId())) {
                    module.setEnable(true);
                    break;
                }
            }
        }
    }

    private static void startActivity(@NonNull String module, @NonNull Context context) {
        Intent intent = Workbox.getWorkboxModuleIntent(module, context);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    WorkboxPanel(FloatEntry floatEntry) {
        mFloatEntry = floatEntry;
        Context context = GeneralInfoHelper.getContext();
        mRootView = new FrameLayout(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        RecyclerView moduleRecyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.workbox_template_recycler_view, mRootView, false);
        moduleRecyclerView.setBackgroundColor(Color.TRANSPARENT);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        moduleRecyclerView.setLayoutManager(layoutManager);
        mModuleAdapter = new ModuleAdapter(floatEntry, this, mModuleList);
        moduleRecyclerView.setAdapter(mModuleAdapter);
        int margin = UiHelper.dp2px(8);
        moduleRecyclerView.addItemDecoration(new GridItemSpaceDecoration(4, margin));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mRootView.addView(moduleRecyclerView, lp);
        mRootView.setOnClickListener(this);
        mRootView.setFocusableInTouchMode(true);
        mRootView.requestFocus();
        mRootView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hide();
                mFloatEntry.onPanelOutsideClick();
            }
            return true;
        });
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.gravity = Gravity.CENTER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.windowAnimations = R.style.WorkboxFadeAnim;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.3f;
        return layoutParams;
    }

    public void show() {
        mShown = true;
        mWindowManager.addView(mRootView, createLayoutParams());
    }

    public void show(List<Module> moduleList) {
        mShown = true;
        mModuleList.clear();
        mModuleList.addAll(moduleList);
        mModuleAdapter.notifyDataSetChanged();
        mWindowManager.addView(mRootView, createLayoutParams());
    }

    void hide() {
        mShown = false;
        if (mRootView.isAttachedToWindow()) {
            mWindowManager.removeViewImmediate(mRootView);
        }
    }

    boolean isShown() {
        return mShown;
    }

    @Override
    public void onClick(View v) {
        hide();
        mFloatEntry.onPanelOutsideClick();
    }

    private static class ModuleAdapter extends BaseRecyclerAdapter<Module> {

        private FloatEntry mFloatEntry;
        private WorkboxPanel mPanel;

        ModuleAdapter(FloatEntry floatEntry, WorkboxPanel panel, List<Module> data) {
            super(data);
            mFloatEntry = floatEntry;
            mPanel = panel;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_system_info;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            final Module module = getData().get(position);
            TextView moduleView = (TextView) holder.itemView;
            moduleView.setText(module.getName());
            moduleView.setOnClickListener(v -> {
                module.getOnClickListener().onClick(v);
                mPanel.hide();
                mFloatEntry.onPanelClick();
            });
        }
    }
}
