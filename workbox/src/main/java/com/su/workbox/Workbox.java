package com.su.workbox;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.net.interceptor.DataCollectorInterceptor;
import com.su.workbox.net.interceptor.DataUsageInterceptor;
import com.su.workbox.net.interceptor.HostInterceptor;
import com.su.workbox.net.interceptor.MockInterceptor;
import com.su.workbox.ui.HostsActivity;
import com.su.workbox.ui.JsInterfaceListActivity;
import com.su.workbox.ui.app.AppInfoListActivity;
import com.su.workbox.ui.app.ComponentListActivity;
import com.su.workbox.ui.app.DataExportActivity;
import com.su.workbox.ui.app.DatabaseListActivity;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.ui.main.FloatEntry;
import com.su.workbox.ui.main.WorkboxMainActivity;
import com.su.workbox.ui.mock.MockGroupHostActivity;
import com.su.workbox.ui.ui.RulerActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;

import java.io.File;
import java.util.Map;

/**
 * Created by su on 18-1-2.
 */

public class Workbox {

    private static final String TAG = Workbox.class.getSimpleName();
    private static File sWorkboxSdcardDir = new File(Environment.getExternalStorageDirectory(), "workbox");

    private Workbox() {}

    public static void init(Application app, @NonNull String className) {
        long now = System.currentTimeMillis();
        SpHelper.initSharedPreferences(app);
        GeneralInfoHelper.init(app);
        // events will be dispatched with a delay after a last activity passed through them.
        // This delay is long enough to guarantee that ProcessLifecycleOwner won't send any events if activities are destroyed and recreated due to a configuration change
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
        if (TextUtils.isEmpty(className)) {
            throw new IllegalArgumentException("requestSupplier must not be null.");
        }
//        FloatEntry.getInstance();
        WorkboxSupplier.newInstance(className);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "elapse: " + (System.currentTimeMillis() - now));
        }
    }

    public static Object getMockInterceptor() {
        return new MockInterceptor();
    }

    public static Object getDataCollectorInterceptor() {
        return new DataCollectorInterceptor();
    }

    public static Object getDataUsageInterceptorInterceptor() {
        return new DataUsageInterceptor();
    }

    public static Object getHostInterceptor() {
        return new HostInterceptor();
    }

    public static File getWorkboxSdcardDir() {
        return sWorkboxSdcardDir;
    }

    @NonNull
    public static String getHost() {
        return SpHelper.getWorkboxSharedPreferences().getString(SpHelper.COLUMN_HOST, "");
    }

    @NonNull
    public static String getWebViewHost() {
        return SpHelper.getWorkboxSharedPreferences().getString(SpHelper.COLUMN_WEB_VIEW_HOST, "");
    }

    public static Intent getWorkboxMainIntent() {
        return new Intent(GeneralInfoHelper.getContext(), WorkboxMainActivity.class);
    }

    public static void startDataExportActivity(@NonNull Context context) {
        DataExportActivity.startActivity(context);
    }

    public static void startPermissionsActivity(@NonNull Context context) {
        PermissionListActivity.startActivity(context);
    }

    public static void startActivitiesActivity(@NonNull Context context) {
        ComponentListActivity.startActivity(context, "activity");
    }

    public static void startMockDataActivity(@NonNull Context context) {
        MockGroupHostActivity.startActivity(context, "数据模拟接口列表");
    }

    public static void startJsInterfacesActivity(@NonNull Context context) {
        JsInterfaceListActivity.startActivity(context);
    }

    public static void startAppInfoActivity(@NonNull Context context) {
        AppInfoListActivity.startActivity(context);
    }

    public static void startDatabaseListActivity(@NonNull Context context) {
        DatabaseListActivity.startActivity(context);
    }

    public static void startHostsActivity(@NonNull Context context, int type) {
        HostsActivity.startActivity(context, type);
    }

    public static void startRulerActivity(@NonNull Context context) {
        RulerActivity.startActivity(context);
    }

    @Nullable
    public static byte[] toPostData(@Nullable String content) {
        return WorkboxSupplier.getInstance().toPostData(content);
    }

    @Nullable
    public static String toCookies(@NonNull String host) {
        return WorkboxSupplier.getInstance().toCookies(host);
    }

    @NonNull
    public static Map<String, Object> jsObjectList(Activity activity) {
        return WorkboxSupplier.getInstance().jsObjectList(activity);
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return WorkboxSupplier.getInstance().urlMapping(url, newHost);
    }

    public static boolean isLogin() {
        return WorkboxSupplier.getInstance().isLogin();
    }
}
