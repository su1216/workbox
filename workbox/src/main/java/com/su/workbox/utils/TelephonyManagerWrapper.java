package com.su.workbox.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TelephonyManagerWrapper {
    public static final String TAG = TelephonyManagerWrapper.class.getSimpleName();

    private TelephonyManager mManager;

    public TelephonyManagerWrapper(@NonNull TelephonyManager manager) {
        mManager = manager;
    }

    public int getPhoneCount() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mManager.getPhoneCount();
        }
        return 1;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getLine1Number(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getLine1Number", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getLine1Number();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getImei(int slotIndex) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getImei", int.class);
            return (String) method.invoke(mManager, slotIndex);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getDeviceId();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getMeid(int slotIndex) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getMeid", int.class);
            return (String) method.invoke(mManager, slotIndex);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getDeviceId();
    }

    public int getPhoneType(int subId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && !mManager.isVoiceCapable()) {
            return TelephonyManager.PHONE_TYPE_NONE;
         }
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getCurrentPhoneType", int.class);
            return (Integer) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getPhoneType();
    }

    public String getNetworkCountryIso(int phoneId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getNetworkCountryIso", int.class);
            return (String) method.invoke(mManager, phoneId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getNetworkCountryIso();
    }

    public String getNetworkOperatorName(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getNetworkOperatorName", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getNetworkOperatorName();
    }

    public String getNetworkTypeName() {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getNetworkTypeName", int.class);
            return (String) method.invoke(mManager, mManager.getNetworkType());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return "unknown";
    }

    private int getSimState(int slotIndex) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getSimState", int.class);
            return (Integer) method.invoke(mManager, slotIndex);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getSimState();
    }

    public boolean isSimCardPresent(int slotIndex) {
        int simCardState = getSimState(slotIndex);
        switch (simCardState) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_CARD_IO_ERROR:
            case TelephonyManager.SIM_STATE_CARD_RESTRICTED:
                return false;
            default:
                return true;
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getSimSerialNumber(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getSimSerialNumber", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getSimSerialNumber();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getSubscriberId(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getSubscriberId", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getSubscriberId();
    }

    @SuppressLint("MissingPermission")
    public String getDeviceSoftwareVersion(int slotIndex) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getDeviceSoftwareVersion", int.class);
            return (String) method.invoke(mManager, slotIndex);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getDeviceSoftwareVersion();
    }

    public String field2String(String fieldName, String value) {
        return fieldName + ": " + value;
    }
}
