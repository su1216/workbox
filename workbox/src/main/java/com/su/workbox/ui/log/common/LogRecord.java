package com.su.workbox.ui.log.common;

import com.su.workbox.component.annotation.Searchable;

public class LogRecord {

    private String date;
    private String pid;
    private String tid;
    private String level;
    private String tag;
    @Searchable
    private String full;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "date='" + date + '\'' +
                ", pid='" + pid + '\'' +
                ", tid='" + tid + '\'' +
                ", level='" + level + '\'' +
                ", tag='" + tag + '\'' +
                ", full='" + full + '\'' +
                '}';
    }
}
