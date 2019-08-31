package com.su.workbox.ui.app.activity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.su.workbox.utils.ReflectUtil;

public class ActivityExtra implements Parcelable, Cloneable {

    private String name;
    private String value;
    private String valueClassName;
    private String arrayClassName;
    private String listClassName;
    private boolean required;

    public ActivityExtra() {}

    protected ActivityExtra(Parcel in) {
        name = in.readString();
        value = in.readString();
        valueClassName = in.readString();
        arrayClassName = in.readString();
        listClassName = in.readString();
        required = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(valueClassName);
        dest.writeString(arrayClassName);
        dest.writeString(listClassName);
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

    public String getArrayClassName() {
        return arrayClassName;
    }

    public void setArrayClassName(String arrayClassName) {
        this.arrayClassName = arrayClassName;
    }

    public String getListClassName() {
        return listClassName;
    }

    public void setListClassName(String listClassName) {
        this.listClassName = listClassName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Nullable
    public Class<?> getClass(@NonNull String className) {
        try {
            if (className.endsWith("[]")) {
                return ReflectUtil.getArrayClass(className.substring(0, className.length() - 2));
            } else {
                return ReflectUtil.forName(className);
            }
        } catch (ClassNotFoundException e) {
            Log.w("extra", e);
        }
        return null;
    }

    @Override
    protected ActivityExtra clone() {
        ActivityExtra o = null;
        try {
            o = (ActivityExtra) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @Override
    public String toString() {
        return "ActivityExtra{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", valueClassName='" + valueClassName + '\'' +
                ", arrayClassName='" + arrayClassName + '\'' +
                ", listClassName='" + listClassName + '\'' +
                ", required=" + required +
                '}';
    }
}
