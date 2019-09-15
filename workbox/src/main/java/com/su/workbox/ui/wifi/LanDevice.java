package com.su.workbox.ui.wifi;

public class LanDevice {
    private String ip;
    private String mac;
    private String hostName;
    private boolean self;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return "LanDevice{" +
                "ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                ", hostName='" + hostName + '\'' +
                ", self=" + self +
                '}';
    }
}
