package com.su.workbox.ui.app.activity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "intent_data", indices = {@Index(value = {"componentPackageName", "componentClassName"}, unique = true)})
public class IntentData implements Parcelable, Cloneable {

    private static final Gson gson = new Gson();

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
    private List<IntentExtra> extraList = new ArrayList<>();
    @Ignore
    private List<String> categoryList = new ArrayList<>();

    public IntentData() {}

    protected IntentData(Parcel in) {
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
        extraList = in.createTypedArrayList(IntentExtra.CREATOR);
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

    public static final Creator<IntentData> CREATOR = new Creator<IntentData>() {
        @Override
        public IntentData createFromParcel(Parcel in) {
            return new IntentData(in);
        }

        @Override
        public IntentData[] newArray(int size) {
            return new IntentData[size];
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

    public List<IntentExtra> getExtraList() {
        return extraList;
    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    public void setExtraList(List<IntentExtra> extraList) {
        this.extraList = extraList;
    }

    public void setCategoryList(List<String> categoryList) {
        this.categoryList = categoryList;
    }

    void initExtrasAndCategories() {
        if (!TextUtils.isEmpty(extras)) {
            extraList = gson.fromJson(extras, new TypeToken<List<IntentExtra>>(){}.getType());
        }
        if (!TextUtils.isEmpty(categories)) {
            categoryList = gson.fromJson(categories, new TypeToken<List<String>>(){}.getType());
        }
    }

    @Override
    public IntentData clone() {
        IntentData o = null;
        try {
            o = (IntentData) super.clone();
            o.extraList = new ArrayList<>();
            for (IntentExtra extra : extraList) {
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
        return "IntentData{" +
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
