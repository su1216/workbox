package com.su.workbox.entity;

import android.support.annotation.NonNull;

public class FileSystem {

    private String fileSystem;
    private String size;
    private String used;
    private String avail;
    private String use;
    private String mountedOn;
    private String fileSystemType;

    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getAvail() {
        return avail;
    }

    public void setAvail(String avail) {
        this.avail = avail;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getMountedOn() {
        return mountedOn;
    }

    public void setMountedOn(String mountedOn) {
        this.mountedOn = mountedOn;
    }

    public String getFileSystemType() {
        return fileSystemType;
    }

    public void setFileSystemType(String fileSystemType) {
        this.fileSystemType = fileSystemType;
    }

    public static FileSystem fromShellLine(@NonNull String line) {
        String[] info = line.split("\\s+");
        FileSystem fileSystem = new FileSystem();
        fileSystem.fileSystem = info[0];
        fileSystem.size = info[1];
        fileSystem.used = info[2];
        fileSystem.avail = info[3];
        fileSystem.use = info[4];
        fileSystem.mountedOn = info[5];
        return fileSystem;
    }

    @Override
    public String toString() {
        return "FileSystem{" +
                "fileSystem='" + fileSystem + '\'' +
                ", size='" + size + '\'' +
                ", used='" + used + '\'' +
                ", avail='" + avail + '\'' +
                ", use='" + use + '\'' +
                ", mountedOn='" + mountedOn + '\'' +
                ", fileSystemType='" + fileSystemType + '\'' +
                '}';
    }
}
