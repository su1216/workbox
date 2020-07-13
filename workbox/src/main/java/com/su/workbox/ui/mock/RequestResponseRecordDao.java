package com.su.workbox.ui.mock;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RequestResponseRecordDao {

    Object LOCK = new Object();

    @Query("SELECT * FROM request_response")
    LiveData<List<RequestResponseRecord>> getAllRequestResponseRecords();

    @Query("SELECT * FROM request_response" +
            " WHERE host = :host" +
            " AND (path LIKE  '%' || :query || '%' OR requestHeaders LIKE  '%' || :query || '%' OR requestBody LIKE  '%' || :query || '%' OR responseHeaders LIKE  '%' || :query || '%' OR responseBody LIKE  '%' || :query || '%')")
    LiveData<List<RequestResponseRecord>> getRequestResponseRecords(String host, String query);

    @Query("SELECT * FROM request_response WHERE _id = :id")
    RequestResponseRecord getRequestResponseRecordById(long id);

    @Query("SELECT * FROM request_response WHERE url = :url AND method = :method AND contentType = :contentType AND requestHeaders = :requestHeaders AND requestBody = :requestBody AND auto = :auto")
    RequestResponseRecord getRequestResponseRecordByEssentialFactors(String url, String method, String contentType, String requestHeaders, String requestBody, int auto);

    @Query("SELECT * FROM request_response WHERE url = :url AND method = :method AND contentType = :contentType AND requestHeaders = :requestHeaders AND requestBody = :requestBody AND auto = :auto AND inUse = :inUse")
    RequestResponseRecord getRequestResponseRecordInUseByEssentialFactors(String url, String method, String contentType, String requestHeaders, String requestBody, int auto, int inUse);

    @Query("SELECT * FROM request_response WHERE url = :url AND method = :method AND contentType = :contentType AND requestHeaders = :requestHeaders AND requestBody = :requestBody AND inUse = :inUse")
    RequestResponseRecord getRequestResponseRecordInUseByEssentialFactors(String url, String method, String contentType, String requestHeaders, String requestBody, int inUse);

    @Insert
    long insertRequestResponseRecord(RequestResponseRecord requestResponseRecord);

    @Update
    int updateRequestResponseRecord(RequestResponseRecord requestResponseRecord);

    @Query("DELETE FROM request_response WHERE host = :host")
    void deleteRequestResponseRecordsByHost(String host);

    @Query("DELETE FROM request_response WHERE _id = :id")
    int deleteRequestResponseRecord(long id);

    @Query("SELECT host, count(1) as total" +
            " FROM request_response" +
            " GROUP BY host")
    LiveData<List<RequestResponseRecord.Summary>> getRequestResponseRecordSummaryList();
}
