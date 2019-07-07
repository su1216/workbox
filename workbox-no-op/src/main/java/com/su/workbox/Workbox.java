package com.su.workbox;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import okhttp3.Interceptor;

/**
 * Created by su on 18-1-2.
 */

public class Workbox {

    public static final String MODULE_DATA_EXPORT = "data_export";
    public static final String MODULE_PERMISSIONS = "permissions";
    public static final String MODULE_ACTIVITIES = "activities";
    public static final String MODULE_MOCK_DATA = "mock_data";
    public static final String MODULE_JS_INTERFACES = "js_interfaces";
    public static final String MODULE_APP_INFO = "app_info";
    public static final String MODULE_DATABASES = "databases";
    public static final String MODULE_RULER = "ruler";
    public static final String MODULE_LIFECYCLE = "lifecycle";

    private Workbox() {}

    public static void init(Application app) {}

    public static void init(Application app, @NonNull String className) {}

    @NonNull
    public static String getHost() {
        return "";
    }

    public static Interceptor getMockInterceptor() {
        return null;
    }

    public static Interceptor getDataCollectorInterceptor() {
        return null;
    }

    public static Interceptor getDataUsageInterceptorInterceptor() {
        return null;
    }

    public static Interceptor getHostInterceptor() {
        return null;
    }

    public static Thread.UncaughtExceptionHandler newLogUncaughtExceptionHandler(boolean killProcess) {
        return null;
    }

    @Nullable
    public static Intent getWorkboxMainIntent() {
        return null;
    }

    public static void registerFragment(@NonNull Fragment fragment) {}

    public static void enableFragmentLifecycleLog(boolean enableLog) {}

    public static void startActivity(@NonNull String module, @NonNull Context context) {}

    @NonNull
    public static String getWebViewHost() {
        return "";
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }
}
