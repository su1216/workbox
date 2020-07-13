package com.su.workbox.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PidInfo {
    private int pid;
    private int ppid;
    private int uid;
    private String formatUid;
    private String name;
    private List<PidInfo> userPidInfo = new ArrayList<>();

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPpid() {
        return ppid;
    }

    public void setPpid(int ppid) {
        this.ppid = ppid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFormatUid() {
        return formatUid;
    }

    public void setFormatUid(String formatUid) {
        this.formatUid = formatUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PidInfo> getUserPidInfo() {
        return userPidInfo;
    }

    public void setUserPidInfo(List<PidInfo> userPidInfo) {
        this.userPidInfo = userPidInfo;
    }

    public static PidInfo fromShellLine(@NonNull String line) {
        String[] info = line.split("\\s+");
        PidInfo pidInfo = new PidInfo();
        String uidString = info[0].replaceAll("^\\w+[uias]", "");
        pidInfo.setUid(Integer.parseInt(uidString));
        pidInfo.setFormatUid(info[0]);
        pidInfo.setPid(Integer.parseInt(info[1]));
        pidInfo.setPpid(Integer.parseInt(info[2]));
        pidInfo.setName(info[8]);
        return pidInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PidInfo pidInfo = (PidInfo) o;
        return pid == pidInfo.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public String toString() {
        return "PidInfo{" +
                "pid=" + pid +
                ", ppid=" + ppid +
                ", uid=" + uid +
                ", formatUid='" + formatUid + '\'' +
                ", name='" + name + '\'' +
                ", userPidInfo=" + userPidInfo +
                '}';
    }
}
