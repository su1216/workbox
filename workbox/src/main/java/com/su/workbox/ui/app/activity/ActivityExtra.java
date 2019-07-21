package com.su.workbox.ui.app.activity;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivityExtra implements Parcelable, Cloneable {

    private String name;
    private String value;
    private String valueClassName;
    private boolean required;

    public ActivityExtra() {}

    protected ActivityExtra(Parcel in) {
        name = in.readString();
        value = in.readString();
        valueClassName = in.readString();
        required = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(valueClassName);
        dest.writeByte((byte) (required ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActivityExtra> CREATOR = new Creator<ActivityExtra>() {
        @Override
        public ActivityExtra createFromParcel(Parcel in) {
            return new ActivityExtra(in);
        }

        @Override
        public ActivityExtra[] newArray(int size) {
            return new ActivityExtra[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueClassName() {
        return valueClassName;
    }

    public void setValueClassName(String valueClassName) {
        this.valueClassName = valueClassName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "ActivityExtra{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", valueClassName='" + valueClassName + '\'' +
                ", required=" + required +
                '}';
    }
}
