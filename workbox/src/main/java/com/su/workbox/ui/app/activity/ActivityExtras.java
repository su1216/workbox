package com.su.workbox.ui.app.activity;

import android.app.Activity;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.su.workbox.utils.ReflectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity(tableName = "activity_extras", indices = {@Index(value = {"componentPackageName", "componentClassName", "action", "auto"}, unique = true)})
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

    public List<ActivityExtra> getExtraList() {
        return extraList;
    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    @NonNull
    public static ActivityExtras intent2ActivityExtras(Activity activity, Intent intent) {
        ComponentName componentName = activity.getComponentName();
        ActivityExtras activityExtras = new ActivityExtras();
        activityExtras.componentPackageName = componentName.getPackageName();
        activityExtras.componentClassName = componentName.getClassName();
        activityExtras.action = intent.getAction();
        activityExtras.data = intent.getDataString();
        activityExtras.type = intent.getType();
        if (intent.getCategories() != null) {
            activityExtras.categoryList = new ArrayList<>(intent.getCategories());
            activityExtras.categories = JSON.toJSONString(activityExtras.categoryList);
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return activityExtras;
        }

        Set<String> keySet = extras.keySet();
        if (keySet == null || keySet.isEmpty()) {
            return activityExtras;
        }
        copyFromBundle(activityExtras, extras);
        activityExtras.extras = JSON.toJSONString(activityExtras.extraList);
        return activityExtras;
    }

    public static void copyFromBundle(@NonNull ActivityExtras activityExtras, @NonNull Bundle extras) {
        activityExtras.extraList.clear();
        Set<String> keySet = extras.keySet();
        for (String key : keySet) {
            Object value = extras.get(key);
            if (value == null) {
                continue;
            }
            ActivityExtra activityExtra = new ActivityExtra();
            activityExtra.setName(key);
            Class<?> valueClass = value.getClass();
            activityExtra.setValueClassName(valueClass.getName());
            if (ReflectUtil.isPrimitiveClass(valueClass) || ReflectUtil.isPrimitiveWrapperClass(valueClass)) {
                activityExtra.setValue(JSON.toJSONString(value));
            } else if (valueClass.isArray()) {
                Class<?> componentType = valueClass.getComponentType();
                if (ReflectUtil.isPrimitiveClass(componentType) || ReflectUtil.isPrimitiveWrapperClass(componentType)) {
                    activityExtra.setValue(JSON.toJSONString(value));
                }
            }
            activityExtras.extraList.add(activityExtra);
        }
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
                ", auto=" + auto +
                ", extraList=" + extraList +
                '}';
    }
}
