package com.su.workbox.ui.app.record;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LifecycleRecordDao {

    @Query("SELECT * FROM lifecycle_history  WHERE type = 0 ORDER BY createTime DESC")
    LiveData<List<LifecycleRecord>> getAllActivityRecords();

    @Query("SELECT * FROM lifecycle_history WHERE type = 1 ORDER BY createTime DESC")
    LiveData<List<LifecycleRecord>> getAllFragmentRecords();

    @Query("SELECT * FROM lifecycle_history ORDER BY createTime DESC")
    LiveData<List<LifecycleRecord>> getAllRecords();

    @Query("SELECT * FROM lifecycle_history where simpleName LIKE  '%' || :keyword || '%' ORDER BY createTime DESC")
    LiveData<List<LifecycleRecord>> getActivityRecordsByKeyword(String keyword);

    @Query("SELECT * FROM lifecycle_history WHERE _id = :id")
    LiveData<LifecycleRecord> getActivityRecordById(long id);

    @Insert
    long insertActivityRecord(LifecycleRecord lifecycleRecord);

    @Insert
    long insertFragmentRecord(LifecycleRecord lifecycleRecord);

    @Query("DELETE FROM lifecycle_history")
    int deleteAllHistoryRecords();
}
