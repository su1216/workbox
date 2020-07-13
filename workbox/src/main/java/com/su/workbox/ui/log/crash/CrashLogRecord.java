package com.su.workbox.ui.log.crash;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.su.workbox.component.annotation.Searchable;

@Entity(tableName = "crash_log")
public class CrashLogRecord implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
    @Searchable
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "firstLine")
    private String firstLine;
    @ColumnInfo(name = "time")
    private long time;
    @ColumnInfo(name = "pid")
    private int pid;

    public CrashLogRecord() {}

    protected CrashLogRecord(Parcel in) {
        id = in.readLong();
        content = in.readString();
        firstLine = in.readString();
        time = in.readLong();
        pid = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeString(firstLine);
        dest.writeLong(time);
        dest.writeInt(pid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CrashLogRecord> CREATOR = new Creator<CrashLogRecord>() {
        @Override
        public CrashLogRecord createFromParcel(Parcel in) {
            return new CrashLogRecord(in);
        }

        @Override
        public CrashLogRecord[] newArray(int size) {
            return new CrashLogRecord[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", firstLine='" + firstLine + '\'' +
                ", time=" + time +
                ", pid=" + pid +
                '}';
    }
}
