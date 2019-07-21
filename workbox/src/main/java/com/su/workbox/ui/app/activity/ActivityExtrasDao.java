package com.su.workbox.ui.app.activity;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ActivityExtrasDao {

    @Query("SELECT * FROM activity_extras WHERE componentPackageName = :componentPackageName AND componentClassName = :componentClassName")
    List<ActivityExtras> getActivityExtras(String componentPackageName, String componentClassName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertActivityExtras(ActivityExtras activityExtras);

    @Update
    int updateActivityExtras(ActivityExtras activityExtras);

    @Query("DELETE FROM activity_extras")
    int deleteAllActivityExtras();

    @Query("DELETE FROM activity_extras WHERE _id = :id")
    int deleteActivityExtrasById(long id);

    @Query("SELECT count(1) as total FROM activity_extras")
    LiveData<Integer> total();
}
