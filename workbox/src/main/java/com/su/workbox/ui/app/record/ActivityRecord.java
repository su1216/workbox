package com.su.workbox.ui.app.record;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "activity_history")
public class ActivityRecord {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "ActivityRecord{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", simpleName='" + simpleName + '\'' +
                ", name='" + name + '\'' +
                ", event='" + event + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
