package com.su.workbox.ui.app.activity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
