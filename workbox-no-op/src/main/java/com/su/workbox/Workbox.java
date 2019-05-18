package com.su.workbox;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.Interceptor;

/**
 * Created by su on 18-1-2.
 */

public class Workbox {

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

    @NonNull
    public static String getWebViewHost() {
        return "";
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }
}
