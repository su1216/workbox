package com.su.workbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.su.workbox.BuildConfig;
import com.su.workbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by su on 17-2-8.
 */

public class GeneralInfoHelper {

    private static final String TAG = GeneralInfoHelper.class.getSimpleName();

    public static final String LIB_PACKAGE_NAME = "com.su.workbox";
    //application context
    private static Context sContext;
    private static String sAndroidId;
    private static int sScreenWidth;
    private static int sScreenHeight;
    private static double sAspectRatio;

    private static int sAvailableWidth;
    private static int sAvailableHeight;

    private static String sVersionName = "";
    private static int sVersionCode;
    private static String sPackageName = "";
    private static String sAppName = "";
    private static String sApplicationLabel = "";
    private static String sProcessName = "";
    private static int sProcessId = -1;
    private static boolean sDebuggable;

    private static int sActionBarHeight;
    private static int sStatusBarHeight;
    private static int sNavigationBarHeight;
    private static int sTargetSdkVersion;
    private static int sMinSdkVersion;
    private static int sCompileSdkVersion;
    private static int sUid;
    private static String sApplicationClassName;
    private static long sInstallTime;
    private static long sUpdateTime;
    private static long sLaunchTime;
    private static String sSourceDir;
    private static String[] sSplitSourceDirs;
    private static String sDeviceProtectedDataDir;
    private static String sNativeLibraryDir;
    private static String sDataDir;
    private static String sLibName;
    private static String sLibVersion;

    private static int sScaledTouchSlop;
    private static int sScaledEdgeSlop;

    private GeneralInfoHelper() {}

    public static void init(Context context) {
        long now = System.currentTimeMillis();
        sContext = context.getApplicationContext();
        Resources resources = sContext.getResources();
        sLibName = resources.getString(R.string.workbox_name);
        initPackageInfo();
        initAndroidId();
        initScreenSize();
        sProcessId = Process.myPid();
        sProcessName = getCurrentProcessName();
        sStatusBarHeight = UiHelper.getStatusBarHeight(sContext);
        sActionBarHeight = UiHelper.getActionBarHeight(sContext);
        sNavigationBarHeight = UiHelper.getNavigationBarHeight(sContext);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        sScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        sScaledEdgeSlop = viewConfiguration.getScaledEdgeSlop();
        SharedPreferences sharedPreferences = SpHelper.getWorkboxSharedPreferences();
        sLaunchTime = sharedPreferences.getLong("launch_time", now);

        ManifestParser parser = new ManifestParser(context);
        int[] sdkVersions = parser.getSdkVersions();
        sCompileSdkVersion = sdkVersions[0];
        sMinSdkVersion = sdkVersions[1];
        sTargetSdkVersion = sdkVersions[2];
    }

    private static void initPackageInfo() {
        if (TextUtils.isEmpty(sVersionName)) {
            try {
                PackageManager pm = sContext.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(sContext.getPackageName(), 0);
                sVersionName = pi.versionName;
                sVersionCode = pi.versionCode;
                sPackageName = pi.packageName;
                sAppName = pi.applicationInfo.loadLabel(pm).toString();
                ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                sLibVersion = BuildConfig.VERSION_NAME;
                sUid = applicationInfo.uid;
                sApplicationClassName = applicationInfo.className;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    sDeviceProtectedDataDir = applicationInfo.deviceProtectedDataDir;
                }

                sApplicationLabel = pm.getApplicationLabel(applicationInfo).toString();
                sDebuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
                sSourceDir = applicationInfo.sourceDir;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sSplitSourceDirs = applicationInfo.splitSourceDirs;
                }
                sNativeLibraryDir = applicationInfo.nativeLibraryDir;
                sDataDir = applicationInfo.dataDir;
                sUpdateTime = new File(sSourceDir).lastModified();
                sInstallTime = pi.firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, e);
            }
        }
    }

    @SuppressLint("HardwareIds")
    private static void initAndroidId() {
        sAndroidId = Settings.Secure.getString(sContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static void initScreenSize() {
        WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        sScreenWidth = Math.min(realSize.x, realSize.y);
        sScreenHeight = Math.max(realSize.x, realSize.y);
        sAspectRatio = BigDecimal.valueOf(getScreenHeight())
                .divide(BigDecimal.valueOf(getScreenWidth()), 2, BigDecimal.ROUND_DOWN)
                .doubleValue();
        Point availableSize = new Point();
        display.getSize(availableSize);
        sAvailableWidth = Math.min(availableSize.x, availableSize.y);
        sAvailableHeight = Math.max(availableSize.x, availableSize.y);
    }

    public static double getAspectRatio() {
        return sAspectRatio;
    }

    public static int getProcessId() {
        return sProcessId;
    }

    @NonNull
    private static String getCurrentProcessName() {
        try {
            return IOUtil.streamToString(new FileInputStream("/proc/self/cmdline")).trim();
        } catch (IOException e) {
            Log.e(TAG, "can't get current process name!", e);
            return "";
        }
    }

    public static Context getContext() {
        return sContext;
    }

    public static String getVersionName() {
        return sVersionName;
    }

    public static int getVersionCode() {
        return sVersionCode;
    }

    public static String getPackageName() {
        return sPackageName;
    }

    public static String getAppName() {
        return sAppName;
    }

    public static String getAndroidId() {
        return sAndroidId;
    }

    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        return sScreenHeight;
    }

    public static int getAvailableWidth() {
        return sAvailableWidth;
    }

    public static int getAvailableHeight() {
        return sAvailableHeight;
    }

    public static int getNavigationBarHeight() {
        return sNavigationBarHeight;
    }

    public static String getApplicationLabel() {
        return sApplicationLabel;
    }

    public static String getProcessName() {
        return sProcessName;
    }

    public static boolean isDebuggable() {
        return sDebuggable;
    }

    public static long getLaunchTime() {
        return sLaunchTime;
    }

    public static int getTargetSdkVersion() {
        return sTargetSdkVersion;
    }

    public static int getMinSdkVersion() {
        return sMinSdkVersion;
    }

    public static int getCompileSdkVersion() {
        return sCompileSdkVersion;
    }

    public static int getUid() {
        return sUid;
    }

    public static String getApplicationClassName() {
        return sApplicationClassName;
    }

    public static long getInstallTime() {
        return sInstallTime;
    }

    public static long getUpdateTime() {
        return sUpdateTime;
    }

    public static String getSourceDir() {
        return sSourceDir;
    }

    public static String[] getSplitSourceDirs() {
        return sSplitSourceDirs;
    }

    public static String getDeviceProtectedDataDir() {
        return sDeviceProtectedDataDir;
    }

    public static String getNativeLibraryDir() {
        return sNativeLibraryDir;
    }

    public static String getDataDir() {
        return sDataDir;
    }

    public static int getStatusBarHeight() {
        return sStatusBarHeight;
    }

    public static int getActionBarHeight() {
        return sActionBarHeight;
    }

    public static String getLibName() {
        return sLibName;
    }

    public static String getLibVersion() {
        return sLibVersion;
    }

    public static int getScaledTouchSlop() {
        return sScaledTouchSlop;
    }

    public static int getScaledEdgeSlop() {
        return sScaledEdgeSlop;
    }

    @NonNull
    public static String infoToString() {
        return "GeneralInfoHelper{" + '\n' +
                infoString() + '\n' +
                '}';
    }

    public static String infoString() {
        return "libName=" + sLibName + '\n' +
                ", libVersion=" + sLibVersion + '\n' +
                ", debuggable=" + sDebuggable + '\n' +
                ", versionName=" + sVersionName + '\n' +
                ", versionCode=" + sVersionCode + '\n' +
                ", processId=" + sProcessId + '\n' +
                ", processName=" + sProcessName + '\n' +
                ", packageName=" + sPackageName + '\n' +
                ", appName=" + sAppName + '\n' +
                ", deviceId=" + sAndroidId + '\n' +
                ", screenWidth=" + sScreenWidth + '\n' +
                ", screenHeight=" + sScreenHeight + '\n' +
                ", aspectRatio=" + sAspectRatio + '\n' +
                ", statusBarHeight=" + sStatusBarHeight + '\n' +
                ", actionBarHeight=" + sActionBarHeight + '\n' +
                ", navigationBarHeight=" + sNavigationBarHeight + '\n' +
                ", availableWidth=" + sAvailableWidth + '\n' +
                ", availableHeight=" + sAvailableHeight + '\n' +
                ", applicationLabel=" + sApplicationLabel + '\n' +
                ", uid=" + sUid + '\n' +
                ", applicationClassName=" + sApplicationClassName + '\n' +
                ", sourceDir=" + sSourceDir + '\n' +
                ", sSplitSourceDirs=" + Arrays.toString(sSplitSourceDirs) + '\n' +
                ", nativeLibraryDir=" + sNativeLibraryDir + '\n' +
                ", dataDir=" + sDataDir + '\n' +
                ", deviceProtectedDataDir=" + sDeviceProtectedDataDir + '\n' +
                ", installTime=" + formatDate(sInstallTime) + '\n' +
                ", updateTime=" + formatDate(sUpdateTime) + '\n' +
                ", launchTime=" + formatDate(sLaunchTime) + '\n' +
                ", targetSdkVersion=" + sTargetSdkVersion + '\n' +
                ", compileSdkVersion=" + sCompileSdkVersion + '\n' +
                ", minSdkVersion=" + sMinSdkVersion + '\n' +
                ", scaledTouchSlop=" + sScaledTouchSlop + '\n' +
                ", scaledEdgeSlop=" + sScaledEdgeSlop;
    }

    private static String formatDate(long ms) {
        return ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(ms));
    }
}
