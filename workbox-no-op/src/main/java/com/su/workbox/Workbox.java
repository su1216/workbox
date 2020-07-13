package com.su.workbox;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import okhttp3.Interceptor;

/**
 * Created by su on 18-1-2.
 */

public class Workbox {

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

    @Nullable
    public static Intent getWorkboxModuleIntent(@NonNull String module, @NonNull Context context) {
        return null;
    }

    public static void startActivity(@NonNull String module, @NonNull Context context) {}

    @NonNull
    public static String getWebViewHost() {
        return "";
    }

    public static void enableCrashLogEntry(boolean enable) {}

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }

    public static void addIntentExcludeType(Class<?> clazz) {}

    public static void intentIncludeSubType(boolean include) {}
}
