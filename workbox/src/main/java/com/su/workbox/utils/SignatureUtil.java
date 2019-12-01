package com.su.workbox.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.util.Log;

public class SignatureUtil {

    public static final String TAG = SignatureUtil.class.getSimpleName();

    public static Signature[] getSignatures(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return packageInfo.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "packageName: " + packageName, e);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.P)
    public static SigningInfo getSignatureInfo28(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            return packageInfo.signingInfo;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "packageName: " + packageName, e);
        }
        return null;
    }
}
