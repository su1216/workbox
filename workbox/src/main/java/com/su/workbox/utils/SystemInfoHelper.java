package com.su.workbox.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by mahao on 17-5-31.
 */

public class SystemInfoHelper {

    private static final String TAG = SystemInfoHelper.class.getSimpleName();
    private static final int ERROR = -1;

    private static SparseArray<String> sSystemVersionName;
    private static SparseArray<String> sSystemVersionCode;

    public static String getNetworkType(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr == null) {
            return "disconnect";
        }

        NetworkInfo info = mgr.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return "disconnect";
        }

        String name = info.getTypeName();
        if ("WIFI".equalsIgnoreCase(name)) {
            return "Wifi";
        }
        if ("MOBILE".equalsIgnoreCase(name)) {
            if (TextUtils.isEmpty(Proxy.getDefaultHost())) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (NetworkUtil.is3g4g(NetworkUtil.getNetworkClass(tm.getNetworkType()))) {
                    return "Mobile 3g/4g";
                }
                return "Mobile 2.5g";
            }
            return "Wap";
        }
        return "unknown";
    }

    public static String getCpuBit() {
        String bits;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bits = TextUtils.join(", ", Build.SUPPORTED_ABIS).contains("64") ? "64-Bit" : "32-Bit";
        } else {
            bits = "32-Bit";
        }
        return bits;
    }

    public static String getDpiInfo(int densityDpi) {
        if (densityDpi <= 120) {
            return "ldpi";
        }
        if (densityDpi <= 160) {
            return "mdpi";
        }
        if (densityDpi <= 240) {
            return "hdpi";
        }
        if (densityDpi <= 320) {
            return "xhdpi";
        }
        if (densityDpi <= 480) {
            return "xxhdpi";
        }
        if (densityDpi <= 640) {
            return "xxxhdpi";
        }
        return "too big dpi";
    }

    public static String getCpuName() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Hardware")) {
                    return line.split(":")[1];
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.close(br);
        }
        return "";
    }

    public static String getSystemVersionCode(int sdk) {
        if (sSystemVersionCode == null) {
            sSystemVersionCode = new SparseArray<>();
            sSystemVersionCode.put(14, "4.0");
            sSystemVersionCode.put(15, "4.0.3");
            sSystemVersionCode.put(16, "4.1");
            sSystemVersionCode.put(17, "4.2");
            sSystemVersionCode.put(18, "4.3");
            sSystemVersionCode.put(19, "4.4");
            sSystemVersionCode.put(20, "4.4");
            sSystemVersionCode.put(21, "5.0");
            sSystemVersionCode.put(22, "5.1");
            sSystemVersionCode.put(23, "6.0");
            sSystemVersionCode.put(24, "7.0");
            sSystemVersionCode.put(25, "7.1");
            sSystemVersionCode.put(26, "8.0.0");
            sSystemVersionCode.put(27, "8.1");
            sSystemVersionCode.put(28, "9.0");
        }
        return sSystemVersionCode.get(sdk);
    }

    public static String getSystemVersionName(int sdk) {
        if (sSystemVersionName == null) {
            sSystemVersionName = new SparseArray<>();
            sSystemVersionName.put(1, "Alpha");
            sSystemVersionName.put(2, "Beta");
            sSystemVersionName.put(3, "Cupcake");
            sSystemVersionName.put(4, "Donut");
            sSystemVersionName.put(5, "Eclair");
            sSystemVersionName.put(6, "Eclair");
            sSystemVersionName.put(7, "Eclair MR1");
            sSystemVersionName.put(8, "Froyo");
            sSystemVersionName.put(9, "Gingerbread");
            sSystemVersionName.put(10, "Gingerbread MR1");
            sSystemVersionName.put(11, "Honeycomb");
            sSystemVersionName.put(12, "Honeycomb MR1");
            sSystemVersionName.put(13, "Honeycomb MR2");
            sSystemVersionName.put(14, "Ice Cream Sandwich");
            sSystemVersionName.put(15, "Ice Cream Sandwich MR1");
            sSystemVersionName.put(16, "Jelly Bean");
            sSystemVersionName.put(17, "Jelly Bean MR1");
            sSystemVersionName.put(18, "Jelly Bean MR2");
            sSystemVersionName.put(19, "KitKat");
            sSystemVersionName.put(20, "KitKat for watches");
            sSystemVersionName.put(21, "Lollipop");
            sSystemVersionName.put(22, "Lollipop MR1");
            sSystemVersionName.put(23, "Marshmallow");
            sSystemVersionName.put(24, "Nougat");
            sSystemVersionName.put(25, "Nougat");
            sSystemVersionName.put(26, "Oreo");
            sSystemVersionName.put(27, "Oreo");
            sSystemVersionName.put(28, "Pie");
            sSystemVersionName.put(29, "Q");
        }
        return sSystemVersionName.get(sdk);
    }

    /**
     * SDCARD是否可用
     */
    private static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SDCARD剩余存储空间
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            return stat.getAvailableBytes();
        } else {
            return ERROR;
        }
    }

    /**
     * 获取SDCARD总的存储空间
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            return stat.getTotalBytes();
        } else {
            return ERROR;
        }
    }

    /**
     * 获取系统总内存
     *
     * @return 总内存大单位为B。
     */
    public static long getTotalMemorySize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static String formatFileSize(long size) {
        return Formatter.formatFileSize(GeneralInfoHelper.getContext(), size);
    }
}
