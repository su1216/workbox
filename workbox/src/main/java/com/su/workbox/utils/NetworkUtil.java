package com.su.workbox.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.su.workbox.BuildConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by su on 14-6-3.
 */
public final class NetworkUtil {

    private static final String TAG = NetworkUtil.class.getSimpleName();

    /**
     * the constants below are all from
     *
     * @see android.telephony.TelephonyManager
     * Unknown network class.
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks.
     */
    public static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks.
     */
    public static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks.
     */
    public static final int NETWORK_CLASS_4_G = 3;

    private NetworkUtil() {
    }

    /*
     * 获取mac地址
     * */
    @NonNull
    public static String getMacAddress() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if ("wlan0".equals(networkInterface.getName())) {
                    byte[] hardwareAddress = networkInterface.getHardwareAddress();
                    if (hardwareAddress == null || hardwareAddress.length == 0) {
                        continue;
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : hardwareAddress) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    return buf.toString();
                }
            }
        } catch (SocketException e) {
            Log.w(TAG, e);
        }
        return "";
    }

    /**
     * 获取系统代理信息
     */
    @NonNull
    public static String[] getSystemProxy() {
        String proxyAddress = System.getProperty("http.proxyHost");
        String portStr = System.getProperty("http.proxyPort");
        String proxyPort = portStr != null ? portStr : "-1";
        return new String[]{proxyAddress, proxyPort};
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) GeneralInfoHelper.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    /*
     * 获取当前联网方式
     * */
    public static int getConnectedType(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return networkInfo.getType();
        }

        return -1;
    }

    /**
     * from level api 28
     * same as TelephonyManager.getNetworkClass(networkType) (hide)<br/>
     * Return general class of network type, such as "3G" or "4G". In cases
     * where classification is contentious, this method is conservative.
     */
    public static int getNetworkClass(int networkType) {
        Class<TelephonyManager> clazz = TelephonyManager.class;
        try {
            Method method = clazz.getDeclaredMethod("getNetworkClass", int.class);
            return (Integer) method.invoke(null, networkType);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "networkType: " + networkType, e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "networkType: " + networkType, e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "networkType: " + networkType, e);
        }
        return NETWORK_CLASS_UNKNOWN;
    }

    public static String getNetworkClassName(int networkClass) {
        switch (networkClass) {
            case NETWORK_CLASS_2_G:
                return "2G";
            case NETWORK_CLASS_3_G:
                return "3G";
            case NETWORK_CLASS_4_G:
                return "4G";
            default:
                return "CLASS UNKNOWN";
        }
    }

    public static String getTelephonyNetworkTypeName(int networkType) {
        Class<TelephonyManager> clazz = TelephonyManager.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getName().startsWith("NETWORK_TYPE_")) {
                    int value = (Integer) field.get(null);
                    if (value == networkType) {
                        return field.getName().substring(13);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "networkType: " + networkType, e);
        }
        return "";
    }

    /*
     * 获取当前联网方式
     * */
    public static String getNetworkTypeName() {
        ConnectivityManager connectivityManager = (ConnectivityManager) GeneralInfoHelper.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return "DISCONNECT";
        }

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return "DISCONNECT";
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return networkInfo.getTypeName();
        }

        return "UNKNOWN";
    }

    public static String getIpv4Address() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "getIpv4Address:" + hostAddress);
                        }
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    public static String getIpv6Address() {
        try {
            String address = null;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "getIpv6Address:" + hostAddress);
                        }
                        int index = hostAddress.indexOf("%");
                        if (index >= 0 && "wlan0".equalsIgnoreCase((hostAddress).substring(index + 1).trim())) {
                            return inetAddress.getHostAddress().substring(0, index).toUpperCase();
                        } else if (index < 0) {
                            address = inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return address;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
}
