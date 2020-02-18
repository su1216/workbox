package com.su.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.Workbox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by su on 18-1-2.
 */

public class SampleApplication extends Application {

    public static final String TAG = SampleApplication.class.getSimpleName();
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        if (TextUtils.equals(getCurrentProcessName(), BuildConfig.APPLICATION_ID)) {
            initWorkbox(this);
        }
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(Workbox.newLogUncaughtExceptionHandler(true));
        }
        initSharedPreference();
    }

    private static void initWorkbox(Application application) {
        Workbox.init(application, "com.su.sample.MySupplier");
    }

    private void initSharedPreference() {
        Set<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putInt("int_test", 1)
                .putLong("long_test", 0x80000000)
                .putFloat("float_test", 1.1f)
                .putBoolean("boolean_test", true)
                .putString("string_test", "string...")
                .putStringSet("set_test", set)
                .apply();
    }

    @NonNull
    private static String getCurrentProcessName() {
        try {
            return TextUtils.join("\n", streamToLines(new FileInputStream("/proc/self/cmdline"))).trim();
        } catch (IOException e) {
            Log.e(TAG, "can't get current process name!", e);
            return "";
        }
    }

    @NonNull
    private static List<String> streamToLines(@NonNull InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input), 8192);
        try {
            String line;
            final List<String> buffer = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
            }
            return buffer;
        } finally {
            reader.close();
        }
    }

    public static Context getContext() {
        return sContext;
    }
}
