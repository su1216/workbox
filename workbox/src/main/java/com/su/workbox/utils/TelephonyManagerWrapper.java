package com.su.workbox.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.NonNull;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TelephonyManagerWrapper {
    public static final String TAG = TelephonyManagerWrapper.class.getSimpleName();
    private static String sCanNotGet = GeneralInfoHelper.getContext().getResources().getString(R.string.workbox_can_not_get);

    private TelephonyManager mManager;

    public TelephonyManagerWrapper(@NonNull TelephonyManager manager) {
        mManager = manager;
    }

    public int getPhoneCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return sCanNotGet;
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return sCanNotGet;
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && !mManager.isVoiceCapable()) {
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

    public String getNetworkOperator(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getNetworkOperator", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getNetworkOperator();
    }

    public String getSimOperator(int subId) {
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getSimOperator", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getSimOperator();
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
        String iccid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            iccid = getSimSerialNumber22(subId);
        }
        if (TextUtils.isEmpty(iccid) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return sCanNotGet;
        } else if (!TextUtils.isEmpty(iccid)) {
            return iccid;
        }
        Class<TelephonyManager> managerClass = TelephonyManager.class;
        try {
            Method method = managerClass.getMethod("getSimSerialNumber", int.class);
            return (String) method.invoke(mManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return mManager.getSimSerialNumber();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String getSimSerialNumber22(int subId) {
        SubscriptionManager manager = SubscriptionManager.from(GeneralInfoHelper.getContext());
        SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = manager.getActiveSubscriptionInfoForSimSlotIndex(subId);
        if (activeSubscriptionInfoForSimSlotIndex != null) {
            return activeSubscriptionInfoForSimSlotIndex.getIccId();
        }
        return null;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getSubscriberId(int subId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return sCanNotGet;
        }
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
