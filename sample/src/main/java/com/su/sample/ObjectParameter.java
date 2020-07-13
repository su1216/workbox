package com.su.sample;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Created by su on 2018/1/25.
 */

public class ObjectParameter implements Parcelable {

    private String province;
    private int provinceCode;
    private int id;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "ObjectParameter{" +
                "province='" + province + '\'' +
                ", provinceCode=" + provinceCode +
                ", id=" + id +
                '}';
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.province);
        dest.writeInt(this.provinceCode);
        dest.writeInt(this.id);
    }

    public ObjectParameter() {}

    protected ObjectParameter(Parcel in) {
        this.province = in.readString();
        this.provinceCode = in.readInt();
        this.id = in.readInt();
    }

    public static final Parcelable.Creator<ObjectParameter> CREATOR = new Parcelable.Creator<ObjectParameter>() {
        @Override
        public ObjectParameter createFromParcel(Parcel source) {return new ObjectParameter(source);}

        @Override
        public ObjectParameter[] newArray(int size) {return new ObjectParameter[size];}
    };
}
