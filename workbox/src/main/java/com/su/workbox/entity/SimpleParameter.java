package com.su.workbox.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by su on 17-4-28.
 */

public class SimpleParameter implements Parcelable {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.value);
    }

    public SimpleParameter() {}

    public SimpleParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    protected SimpleParameter(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
    }

    public static final Parcelable.Creator<SimpleParameter> CREATOR = new Parcelable.Creator<SimpleParameter>() {
        @Override
        public SimpleParameter createFromParcel(Parcel source) {
            return new SimpleParameter(source);
        }

        @Override
        public SimpleParameter[] newArray(int size) {
            return new SimpleParameter[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "SimpleParameter{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
