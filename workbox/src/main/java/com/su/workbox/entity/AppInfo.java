package com.su.workbox.entity;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.su.workbox.component.annotation.Searchable;

public class AppInfo implements Comparable<AppInfo> {

    private String versionName;
    private long versionCode;
    private Drawable iconDrawable;
    @Searchable
    private String appName;
    private String packageName;
    private int flags;
    private Intent launchIntent;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Intent getLaunchIntent() {
        return launchIntent;
    }

    public void setLaunchIntent(Intent launchIntent) {
        this.launchIntent = launchIntent;
    }

    @Override
    public int compareTo(AppInfo o) {
        return appName.compareTo(o.appName);
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", iconDrawable=" + iconDrawable +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", flags=" + flags +
                ", launchIntent=" + launchIntent +
                '}';
    }
}
