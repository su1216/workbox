package com.su.workbox;

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.net.interceptor.DataCollectorInterceptor;
import com.su.workbox.net.interceptor.DataUsageInterceptor;
import com.su.workbox.net.interceptor.HostInterceptor;
import com.su.workbox.net.interceptor.MockInterceptor;
import com.su.workbox.ui.JsInterfaceListActivity;
import com.su.workbox.ui.app.AppInfoListActivity;
import com.su.workbox.ui.app.ComponentListActivity;
import com.su.workbox.ui.app.ComponentListFragment;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.ui.app.activity.ExcludeTypes;
import com.su.workbox.ui.app.activity.IntentDataCollector;
import com.su.workbox.ui.app.record.ActivityLifecycleListener;
import com.su.workbox.ui.app.record.LifecycleRecordListActivity;
import com.su.workbox.ui.base.AppLifecycleListener;
import com.su.workbox.ui.base.FragmentListenerManager;
import com.su.workbox.ui.data.DataListActivity;
import com.su.workbox.ui.data.DatabaseListActivity;
import com.su.workbox.ui.log.crash.CrashLogActivity;
import com.su.workbox.ui.log.crash.CrashLogHandler;
import com.su.workbox.ui.main.FloatEntry;
import com.su.workbox.ui.main.WorkboxMainActivity;
import com.su.workbox.ui.mock.MockGroupHostActivity;
import com.su.workbox.ui.system.DeviceInfoActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;

import java.io.File;

import okhttp3.Interceptor;

/**
 * Created by su on 18-1-2.
 */
public class Workbox {

    private static final String TAG = Workbox.class.getSimpleName();
    public static final String MODULE_DATA_EXPORT = "data_export";
    public static final String MODULE_PERMISSIONS = "permissions";
    public static final String MODULE_LAUNCHER = "launcher";
    public static final String MODULE_MOCK_DATA = "mock_data";
    public static final String MODULE_JS_INTERFACES = "js_interfaces";
    public static final String MODULE_APP_INFO = "app_info";
    public static final String MODULE_DEVICE_INFO = "device_info";
    public static final String MODULE_DATABASES = "databases";
    public static final String MODULE_LIFECYCLE = "lifecycle";
    public static final String MODULE_CRASH_LOG = "crash_log";
    public static final String MODULE_MAIN = "main";
    public static final File WORKBOX_SDCARD_DIR = new File(Environment.getExternalStorageDirectory(), "workbox");

    private Workbox() {}

    public static void init(Application app) {
        init(app, null);
    }

    public static void init(Application app, @Nullable String className) {
        long now = System.currentTimeMillis();
        SpHelper.initSharedPreferences(app);
        GeneralInfoHelper.init(app);
        ActivityLifecycleListener lifecycleListener = new ActivityLifecycleListener();
        ActivityLifecycleListener.setActivityLifecycleListener(lifecycleListener);
        app.registerActivityLifecycleCallbacks(lifecycleListener);
        app.registerActivityLifecycleCallbacks(new IntentDataCollector());
        // events will be dispatched with a delay after a last activity passed through them.
        // This delay is long enough to guarantee that ProcessLifecycleOwner won't send any events if activities are destroyed and recreated due to a configuration change
        ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleListener.getInstance());
        if (TextUtils.isEmpty(className)) {
            WorkboxSupplier.newDefaultInstance();
        } else {
            WorkboxSupplier.newInstance(className);
        }
        initFloatIcon(app);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "elapse: " + (System.currentTimeMillis() - now));
        }
    }

    private static void initFloatIcon(Application app) {
        if (!TextUtils.equals(GeneralInfoHelper.getProcessName(), app.getPackageName())) {
            return;
        }
        if (SpHelper.getWorkboxSharedPreferences().getBoolean(SpHelper.COLUMN_PANEL_ICON, true)) {
            FloatEntry entry = FloatEntry.getInstance();
            if (!AppHelper.hasSystemWindowPermission(app)) {
                AppHelper.gotoManageOverlayPermission(app);
                return;
            }
            entry.show();
        }
    }

    public static Interceptor getMockInterceptor() {
        return new MockInterceptor();
    }

    public static Interceptor getDataCollectorInterceptor() {
        return new DataCollectorInterceptor();
    }

    public static Interceptor getDataUsageInterceptorInterceptor() {
        return new DataUsageInterceptor();
    }

    public static Interceptor getHostInterceptor() {
        return new HostInterceptor();
    }

    @NonNull
    public static String getHost() {
        return SpHelper.getWorkboxSharedPreferences().getString(SpHelper.COLUMN_HOST, "");
    }

    @NonNull
    public static String getWebViewHost() {
        return SpHelper.getWorkboxSharedPreferences().getString(SpHelper.COLUMN_WEB_VIEW_HOST, "");
    }

    public static Thread.UncaughtExceptionHandler newLogUncaughtExceptionHandler(boolean killProcess) {
        return new CrashLogHandler(killProcess);
    }

    public static Intent getWorkboxMainIntent() {
        return new Intent(GeneralInfoHelper.getContext(), WorkboxMainActivity.class);
    }

    public static void registerFragment(@NonNull Fragment fragment) {
        new FragmentListenerManager().registerFragment(fragment);
    }

    public static void enableFragmentLifecycleLog(boolean enableLog) {
        FragmentListenerManager.setEnableLog(enableLog);
    }

    public static void enableActivityLifecycleLog(boolean enableLog) {
        ActivityLifecycleListener.setEnableLog(enableLog);
    }

    @NonNull
    public static Intent getWorkboxModuleIntent(@NonNull String module, @NonNull Context context) {
        switch (module) {
            case MODULE_DATA_EXPORT:
                return DataListActivity.getLaunchIntent(context);
            case MODULE_PERMISSIONS:
                return PermissionListActivity.getLaunchIntent(context);
            case MODULE_LAUNCHER:
                return ComponentListActivity.getLaunchIntent(context, ComponentListFragment.TYPE_LAUNCHER);
            case MODULE_MOCK_DATA:
                return MockGroupHostActivity.getLaunchIntent(context, "数据模拟接口列表");
            case MODULE_JS_INTERFACES:
                return JsInterfaceListActivity.getLaunchIntent(context);
            case MODULE_APP_INFO:
                return AppInfoListActivity.getLaunchIntent(context);
            case MODULE_DEVICE_INFO:
                return DeviceInfoActivity.getLaunchIntent(context);
            case MODULE_DATABASES:
                return DatabaseListActivity.getLaunchIntent(context);
            case MODULE_LIFECYCLE:
                return LifecycleRecordListActivity.getLaunchIntent(context);
            case MODULE_CRASH_LOG:
                return CrashLogActivity.getLaunchIntent(context);
            case MODULE_MAIN:
                return getWorkboxMainIntent();
            default:
                Log.e(TAG, "no intent for module: " + module);
                return getWorkboxMainIntent();
        }
    }

    public static void startActivity(@NonNull String module, @NonNull Context context) {
        Intent intent = getWorkboxModuleIntent(module, context);
        context.startActivity(intent);
    }

    public static void enableCrashLogEntry(@NonNull Context context, boolean enable) {
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, "com.su.workbox.ui.log.crash.entry.CrashLogActivity");
        pm.setComponentEnabledSetting(componentName,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return WorkboxSupplier.getInstance().urlMapping(url, newHost);
    }

    public static void addIntentExcludeType(Class<?> clazz) {
        ExcludeTypes.addExcludeType(clazz);
    }

    public static void intentIncludeSubType(boolean include) {
        ExcludeTypes.includeSubType(include);
    }
}
