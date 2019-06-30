package com.su.workbox.ui.usage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DataUsageRecordDao {

    @Query("SELECT * FROM data_usage WHERE url LIKE  '%' || :query || '%' ORDER BY requestTime DESC")
    LiveData<List<DataUsageRecord>> getDataUsageRecords(String query);

    @Query("SELECT * FROM data_usage WHERE _id = :id")
    LiveData<DataUsageRecord> getDataUsageRecordById(long id);

    @Insert
    long insertDataUsageRecord(DataUsageRecord dataUsageRecord);

    @Update
    int updateDataUsageRecord(DataUsageRecord dataUsageRecord);

    @Query("DELETE FROM data_usage WHERE url LIKE  '%' || :query || '%' ")
    void deleteDataUsageRecords(String query);

    @Query("SELECT count(1) as total, sum(requestLength) as totalRequestLength, sum(responseLength) as totalResponseLength FROM data_usage WHERE url LIKE  '%' || :urlFragment || '%' ")
    LiveData<DataUsageRecord.Summary> getDataUsageRecordSummary(String urlFragment);

    @Query("SELECT url, count(1) as total, sum(requestLength) as groupRequestLength, sum(responseLength) as groupResponseLength FROM data_usage GROUP BY url")
    LiveData<List<DataUsageRecord.Group>> getDataUsageRecordGroupByUrl();
}
