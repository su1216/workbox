package com.su.workbox.ui.log.crash;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CrashLogRecordDao {

    @Query("SELECT * FROM crash_log ORDER BY time DESC")
    LiveData<List<CrashLogRecord>> getLogs();

    @Query("SELECT * FROM crash_log WHERE _id = :id")
    LiveData<CrashLogRecord> getLogById(long id);

    @Insert
    long insertLog(CrashLogRecord crashLogRecord);

    @Query("DELETE FROM crash_log")
    int deleteLogs();

    @Query("DELETE FROM crash_log WHERE _id = :id")
    int deleteLog(long id);
}
