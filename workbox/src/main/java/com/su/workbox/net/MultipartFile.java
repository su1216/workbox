package com.su.workbox.net;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by su on 17-6-12.
 */

public class MultipartFile {

    private String mName;
    private String mFileName;
    private String mMimeType;
    private File mFile;

    public MultipartFile(String name, String fileName, String mimeType, File file) {
        this.mName = name;
        this.mFileName = fileName;
        this.mMimeType = mimeType;
        this.mFile = file;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        this.mFile = file;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mMimeType) {
        this.mMimeType = mMimeType;
    }

    @NonNull
    @Override
    public String toString() {
        return "MultipartFile{" +
                "mName='" + mName + '\'' +
                ", mFileName='" + mFileName + '\'' +
                ", mMimeType='" + mMimeType + '\'' +
                ", mFile=" + mFile +
                '}';
    }
}
