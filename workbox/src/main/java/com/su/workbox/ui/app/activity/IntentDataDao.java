package com.su.workbox.ui.app.activity;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface IntentDataDao {

    @Query("SELECT * FROM intent_data WHERE componentPackageName = :componentPackageName AND componentClassName = :componentClassName")
    IntentData getActivityExtras(String componentPackageName, String componentClassName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertActivityExtras(IntentData intentData);

    @Update
    int updateActivityExtras(IntentData intentData);

    @Query("DELETE FROM intent_data")
    int deleteAllActivityExtras();

    @Query("DELETE FROM intent_data WHERE _id = :id")
    int deleteActivityExtrasById(long id);

    @Query("SELECT count(1) as total FROM intent_data")
    LiveData<Integer> total();
}
