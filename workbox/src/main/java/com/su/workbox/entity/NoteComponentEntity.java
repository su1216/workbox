package com.su.workbox.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by su on 17-5-27.
 */

public class NoteComponentEntity implements Parcelable {
    private String description;
    private String className;
    private String action;
    private String buildType;
    private String type;
    private int flags;
    private ArrayList<Parameter> parameters = new ArrayList<>();

    public NoteComponentEntity() {}

    public NoteComponentEntity(NoteComponentEntity src) {
        description = src.description;
        className = src.className;
        action = src.action;
        flags = src.flags;
        buildType = src.buildType;
        type = src.type;
        for (Parameter p : src.parameters) {
            parameters.add(p.clone());
        }
    }

    public NoteComponentEntity(Parcel in) {
        description = in.readString();
        className = in.readString();
        action = in.readString();
        buildType = in.readString();
        type = in.readString();
        flags = in.readInt();
        parameters = in.createTypedArrayList(Parameter.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(className);
        dest.writeString(action);
        dest.writeString(buildType);
        dest.writeString(type);
        dest.writeInt(this.flags);
        dest.writeTypedList(parameters);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NoteComponentEntity> CREATOR = new Creator<NoteComponentEntity>() {
        @Override
        public NoteComponentEntity createFromParcel(Parcel in) {
            return new NoteComponentEntity(in);
        }

        @Override
        public NoteComponentEntity[] newArray(int size) {
            return new NoteComponentEntity[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

    private static Class<?> getArrayClass(String className) throws ClassNotFoundException {
        String name;
        if (boolean.class.getName().equals(className)) {
            name = "[Z";
        } else if (byte.class.getName().equals(className)) {
            name = "[B";
        } else if (char.class.getName().equals(className)) {
            name = "[C";
        } else if (double.class.getName().equals(className)) {
            name = "[D";
        } else if (float.class.getName().equals(className)) {
            name = "[F";
        } else if (int.class.getName().equals(className)) {
            name = "[I";
        } else if (long.class.getName().equals(className)) {
            name = "[J";
        } else if (short.class.getName().equals(className)) {
            name = "[S";
        } else {
            // must be an object non-array class
            name = "[L" + className + ";";
        }
        return Class.forName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteComponentEntity that = (NoteComponentEntity) o;
        return flags == that.flags &&
                Objects.equals(description, that.description) &&
                Objects.equals(className, that.className) &&
                Objects.equals(action, that.action) &&
                Objects.equals(buildType, that.buildType) &&
                Objects.equals(type, that.type) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, className, action, buildType, type, flags, parameters);
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteComponentEntity{" +
                "description='" + description + '\'' +
                ", className='" + className + '\'' +
                ", action='" + action + '\'' +
                ", buildType='" + buildType + '\'' +
                ", type='" + type + '\'' +
                ", flags=" + flags +
                ", parameters=" + parameters +
                '}';
    }
}
