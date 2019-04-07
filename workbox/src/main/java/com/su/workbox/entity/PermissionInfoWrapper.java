package com.su.workbox.entity;

import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;

/**
 * Created by mahao on 17-6-29.
 */

public class PermissionInfoWrapper extends PermissionInfo {

    private boolean isHasPermission;
    private boolean isDangerous;

    public PermissionInfoWrapper(PermissionInfo orig, boolean isHasPermission) {
        super(orig);
        this.isHasPermission = isHasPermission;
        this.isDangerous = orig.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
    }

    public boolean isHasPermission() {
        return isHasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        isHasPermission = hasPermission;
    }

    public boolean isDangerous() {
        return isDangerous;
    }

    public void setDangerous(boolean dangerous) {
        isDangerous = dangerous;
    }

    @NonNull
    @Override
    public String toString() {
        return "PermissionInfoWrapper{" +
                "isHasPermission=" + isHasPermission +
                ", isDangerous=" + isDangerous +
                '}';
    }
}
