package com.su.workbox.ui.app.record;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ActivityRecordDao {

    @Query("SELECT * FROM activity_history ORDER BY createTime DESC")
    LiveData<List<ActivityRecord>> getAllActivityRecords();

    @Query("SELECT * FROM activity_history where taskId LIKE  '%' || :taskId || '%' ORDER BY createTime DESC")
    LiveData<List<ActivityRecord>> getActivityRecordsByTask(String taskId);

    @Query("SELECT * FROM activity_history WHERE _id = :id")
    LiveData<ActivityRecord> getActivityRecordById(long id);

    @Insert
    long insertActivityRecord(ActivityRecord activityRecord);

    @Query("DELETE FROM activity_history")
    int deleteAllActivityRecords();
}
