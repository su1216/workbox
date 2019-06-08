package com.su.workbox.ui.app.record;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Entity(tableName = "lifecycle_history")
public class LifecycleRecord {

    @IntDef({ACTIVITY, FRAGMENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    public static final int ACTIVITY = 0;
    public static final int FRAGMENT = 1;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
    @ColumnInfo(name = "type")
    private int type;
    @ColumnInfo(name = "taskId")
    private int taskId;
    @ColumnInfo(name = "simpleName")
    private String simpleName;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "event")
    private String event;
    @ColumnInfo(name = "createTime")
    private long createTime;
    @ColumnInfo(name = "fragmentId")
    private int fragmentId;
    @ColumnInfo(name = "fragmentTag")
    private String fragmentTag;
    @ColumnInfo(name = "parentFragment")
    private String parentFragment;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    public void setFragmentTag(String fragmentTag) {
        this.fragmentTag = fragmentTag;
    }

    public String getParentFragment() {
        return parentFragment;
    }

    public void setParentFragment(String parentFragment) {
        this.parentFragment = parentFragment;
    }

    @Override
    public String toString() {
        return "LifecycleRecord{" +
                "id=" + id +
                ", type=" + type +
                ", taskId=" + taskId +
                ", simpleName='" + simpleName + '\'' +
                ", name='" + name + '\'' +
                ", event='" + event + '\'' +
                ", createTime=" + createTime +
                ", fragmentId=" + fragmentId +
                ", fragmentTag='" + fragmentTag + '\'' +
                ", parentFragment='" + parentFragment + '\'' +
                '}';
    }
}
