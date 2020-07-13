package com.su.workbox.entity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mahao on 17-6-29.
 */

public class PermissionInfoWrapper extends PermissionInfo {

    private static final int FLAG_REMOVED = 1 << 1;
    private boolean isHasPermission;
    private boolean isDangerous;
    private boolean isCustom;

    public static boolean isDangerous(int level) {
        return (level & PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS;
    }

    /**
     * @see android.content.pm.PermissionInfo
     */
    @NonNull
    public static String protectionToString(int level) {
        String protLevel = "????";
        switch (level & PermissionInfo.PROTECTION_MASK_BASE) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                protLevel = "dangerous";
                break;
            case PermissionInfo.PROTECTION_NORMAL:
                protLevel = "normal";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE:
                protLevel = "signature";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                protLevel = "signatureOrSystem";
                break;
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_PRIVILEGED) != 0) {
            protLevel += "|privileged";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
            protLevel += "|development";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_APPOP) != 0) {
            protLevel += "|appop";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_PRE23) != 0) {
            protLevel += "|pre23";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_INSTALLER) != 0) {
            protLevel += "|installer";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_VERIFIER) != 0) {
            protLevel += "|verifier";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_PREINSTALLED) != 0) {
            protLevel += "|preinstalled";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_SETUP) != 0) {
            protLevel += "|setup";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_INSTANT) != 0) {
            protLevel += "|instant";
        }
        if ((level & PermissionInfo.PROTECTION_FLAG_RUNTIME_ONLY) != 0) {
            protLevel += "|runtime";
        }
        return protLevel;
    }

    public static void rearrangeData(@NonNull List<PermissionInfoWrapper> list) {
        Collections.sort(list, (o1, o2) -> {
            // custom, dangerous, grant, group name, name
            if (o1.isCustom() && !o2.isCustom()) {
                return -1;
            } else if (!o1.isCustom() && o2.isCustom()) {
                return 1;
            }

            if (o1.isDangerous() && !o2.isDangerous()) {
                return -1;
            } else if (!o1.isDangerous() && o2.isDangerous()) {
                return 1;
            }

            if (o1.isHasPermission() && !o2.isHasPermission()) {
                return 1;
            } else if (!o1.isHasPermission() && o2.isHasPermission()) {
                return -1;
            }

            if (!TextUtils.equals(o1.group, o2.group)) {
                if (TextUtils.isEmpty(o1.group)) {
                    return 1;
                } else if (TextUtils.isEmpty(o2.group)) {
                    return -1;
                }
                return o1.group.compareToIgnoreCase(o2.group);
            }

            return o1.name.compareToIgnoreCase(o2.name);
        });
    }

    public static List<PermissionInfoWrapper> getPermissionsByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        List<PermissionInfoWrapper> permissionList = new ArrayList<>();
        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        if (packageInfo.requestedPermissions != null) {
            for (String permission : packageInfo.requestedPermissions) {
                try {
                    PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
                    if ((permissionInfo.flags & PermissionInfo.FLAG_INSTALLED) == 0
                            || (permissionInfo.flags & FLAG_REMOVED) != 0) {
                        continue;
                    }
                    boolean isHasPermission = PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, packageName);
                    permissionList.add(new PermissionInfoWrapper(permissionInfo, isHasPermission));
                } catch (PackageManager.NameNotFoundException e) {
                    //ignore
                }
            }
        }

        if (packageInfo.permissions != null) {
            for (PermissionInfo permissionInfo : packageInfo.permissions) {
                boolean isHasPermission = PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionInfo.name, packageName);
                permissionList.add(new PermissionInfoWrapper(permissionInfo, isHasPermission, true));
            }
        }
        return permissionList;
    }

    public PermissionInfoWrapper(PermissionInfo orig, boolean isHasPermission) {
        this(orig, isHasPermission, false);
    }

    public PermissionInfoWrapper(PermissionInfo orig, boolean isHasPermission, boolean isCustom) {
        super(orig);
        this.isHasPermission = isHasPermission;
        this.isCustom = isCustom;
        this.isDangerous = isDangerous(orig.protectionLevel);
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

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    @Override
    public String toString() {
        return "PermissionInfoWrapper{" +
                "isHasPermission=" + isHasPermission +
                ", isDangerous=" + isDangerous +
                ", isCustom=" + isCustom +
                '}';
    }
}
