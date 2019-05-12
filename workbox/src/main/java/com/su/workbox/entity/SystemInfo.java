package com.su.workbox.entity;

/**
 * Created by mahao on 17-5-31.
 */

public class SystemInfo {

    private String key;
    private String title;
    private String desc;

    public SystemInfo(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
