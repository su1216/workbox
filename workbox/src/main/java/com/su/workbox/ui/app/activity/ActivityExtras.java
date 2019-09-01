package com.su.workbox.ui.app.activity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "activity_extras", indices = {@Index(value = {"componentPackageName", "componentClassName"}, unique = true)})
public class ActivityExtras implements Parcelable, Cloneable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
    @ColumnInfo(name = "componentPackageName")
    private String componentPackageName;
    @ColumnInfo(name = "componentClassName")
    private String componentClassName;
    @ColumnInfo(name = "action")
    private String action;
    @ColumnInfo(name = "data")
    private String data;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "extras")
    private String extras;
    @ColumnInfo(name = "categories")
    private String categories;
    @ColumnInfo(name = "flags")
    private int flags;
    @ColumnInfo(name = "auto")
    private boolean auto = true;
    @Ignore
    private List<ActivityExtra> extraList = new ArrayList<>();
    @Ignore
    private List<String> categoryList = new ArrayList<>();

    public ActivityExtras() {}

    protected ActivityExtras(Parcel in) {
        id = in.readLong();
        componentPackageName = in.readString();
        componentClassName = in.readString();
        action = in.readString();
        data = in.readString();
        type = in.readString();
        extras = in.readString();
        categories = in.readString();
        flags = in.readInt();
        auto = in.readByte() != 0;
        extraList = in.createTypedArrayList(ActivityExtra.CREATOR);
        categoryList = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(componentPackageName);
        dest.writeString(componentClassName);
        dest.writeString(action);
        dest.writeString(data);
        dest.writeString(type);
        dest.writeString(extras);
        dest.writeString(categories);
        dest.writeInt(flags);
        dest.writeByte((byte) (auto ? 1 : 0));
        dest.writeTypedList(extraList);
        dest.writeStringList(categoryList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActivityExtras> CREATOR = new Creator<ActivityExtras>() {
        @Override
        public ActivityExtras createFromParcel(Parcel in) {
            return new ActivityExtras(in);
        }

        @Override
        public ActivityExtras[] newArray(int size) {
            return new ActivityExtras[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComponentPackageName() {
        return componentPackageName;
    }

    public void setComponentPackageName(String componentPackageName) {
        this.componentPackageName = componentPackageName;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public List<ActivityExtra> getExtraList() {
        return extraList;
    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    public void setExtraList(List<ActivityExtra> extraList) {
        this.extraList = extraList;
    }

    public void setCategoryList(List<String> categoryList) {
        this.categoryList = categoryList;
    }

    void initExtrasAndCategories() {
        if (!TextUtils.isEmpty(extras)) {
            extraList = JSON.parseArray(extras, ActivityExtra.class);
        }
        if (!TextUtils.isEmpty(categories)) {
            categoryList = JSON.parseArray(categories, String.class);
        }
    }

    @Override
    public ActivityExtras clone() {
        ActivityExtras o = null;
        try {
            o = (ActivityExtras) super.clone();
            o.extraList = new ArrayList<>();
            for (ActivityExtra extra : extraList) {
                o.extraList.add(extra.clone());
            }
            o.categoryList = new ArrayList<>(categoryList);
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @Override
    public String toString() {
        return "ActivityExtras{" +
                "id=" + id +
                ", componentPackageName='" + componentPackageName + '\'' +
                ", componentClassName='" + componentClassName + '\'' +
                ", action='" + action + '\'' +
                ", data='" + data + '\'' +
                ", type='" + type + '\'' +
                ", extras='" + extras + '\'' +
                ", categories='" + categories + '\'' +
                ", flags=" + flags +
                ", auto=" + auto +
                ", extraList=" + extraList +
                ", categoryList=" + categoryList +
                '}';
    }
}
