package com.su.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.squareup.leakcanary.LeakCanary;
import com.su.workbox.Workbox;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by su on 18-1-2.
 */

public class SampleApplication extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        if (TextUtils.equals(GeneralInfoHelper.getCurrentProcessName(), BuildConfig.APPLICATION_ID)) {
            initWorkbox(this);
        }
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(Workbox.newLogUncaughtExceptionHandler(true));
        }
        initSharedPreference();
        initLeakCanary();
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {//1
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
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

    public static Context getContext() {
        return sContext;
    }
}
