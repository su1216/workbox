package com.su.workbox.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.su.workbox.utils.ReflectUtil;

/**
 * Created by su on 17-12-05.
 */

public class Parameter implements Parcelable, Cloneable {

    private String parameter;
    private String parameterName;
    private String parameterClassName;
    private Class<?> parameterClass;
    private boolean parameterRequired;

    public Parameter() {}

    protected Parameter(Parcel in) {
        parameter = in.readString();
        parameterName = in.readString();
        parameterClassName = in.readString();
        parameterClass = (Class<?>) in.readSerializable();
        parameterRequired = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parameter);
        dest.writeString(parameterName);
        dest.writeString(parameterClassName);
        dest.writeSerializable(parameterClass);
        dest.writeByte((byte) (parameterRequired ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Parameter> CREATOR = new Creator<Parameter>() {
        @Override
        public Parameter createFromParcel(Parcel in) {
            return new Parameter(in);
        }

        @Override
        public Parameter[] newArray(int size) {
            return new Parameter[size];
        }
    };

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public boolean isParameterRequired() {
        return parameterRequired;
    }

    public void setParameterRequired(boolean parameterRequired) {
        this.parameterRequired = parameterRequired;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public String getParameterClassName() {
        return parameterClassName;
    }

    public void setParameterClassName(@Nullable String parameterClassName) {
        this.parameterClassName = parameterClassName;
        if (parameterClassName != null) {
            try {
                if (parameterClassName.endsWith("[]")) {
                    parameterClass = ReflectUtil.getArrayClass(parameterClassName.substring(0, parameterClassName.length() - 2));
                } else {
                    parameterClass = ReflectUtil.forName(parameterClassName);
                }
            } catch (ClassNotFoundException e) {
                Log.w("Parameter", e);
            }
        }
    }

    @Override
    public Parameter clone() {
        Parameter o = null;
        try {
            o = (Parameter) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "parameter='" + parameter + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", parameterClassName='" + parameterClassName + '\'' +
                ", parameterClass=" + parameterClass +
                ", parameterRequired=" + parameterRequired +
                '}';
    }
}
