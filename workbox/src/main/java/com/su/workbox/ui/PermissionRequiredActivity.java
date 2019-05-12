package com.su.workbox.ui;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

public abstract class PermissionRequiredActivity extends BaseAppCompatActivity {

    @TargetApi(Build.VERSION_CODES.M)
    public void permissionRequest(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            makeHintDialog(permission, requestCode).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) {
            granted = granted && grantResult == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                break;
            }
        }
        if (!granted) {
            makeHintDialog(permissions[0], requestCode);
        }
    }

    public abstract AlertDialog makeHintDialog(String permission, int requestCode);
}
