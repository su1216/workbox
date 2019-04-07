package com.su.workbox.ui.mock;

import android.support.annotation.NonNull;

import com.su.workbox.database.table.RequestResponseRecord;
import com.su.workbox.utils.AppExecutors;

public class RequestResponseRecordSource {

    private static volatile RequestResponseRecordSource INSTANCE;

    private RequestResponseRecordDao mRequestResponseRecordDao;

    private AppExecutors mAppExecutors = AppExecutors.getInstance();

    // Prevent direct instantiation.
    private RequestResponseRecordSource(@NonNull RequestResponseRecordDao requestResponseRecordDao) {
        this.mRequestResponseRecordDao = requestResponseRecordDao;
    }

    public static RequestResponseRecordSource getInstance(@NonNull RequestResponseRecordDao requestResponseRecordDao) {
        if (INSTANCE == null) {
            synchronized (RequestResponseRecordSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RequestResponseRecordSource(requestResponseRecordDao);
                }
            }
        }
        return INSTANCE;
    }

    public RequestResponseRecord getRequestResponseRecordByEssentialFactors(@NonNull String url, String method, String contentType, String requestHeaders, String requestBody, int auto) {
        return mRequestResponseRecordDao.getRequestResponseRecordByEssentialFactors(url, method, contentType, requestHeaders, requestBody, auto);
    }

    public RequestResponseRecord getRequestResponseRecordInUseByEssentialFactors(@NonNull String url, String method, String contentType, String requestHeaders, String requestBody, int auto, int inUse) {
        return mRequestResponseRecordDao.getRequestResponseRecordInUseByEssentialFactors(url, method, contentType, requestHeaders, requestBody, auto, inUse);
    }

    public RequestResponseRecord getRequestResponseRecordInUseByEssentialFactors(@NonNull String url, String method, String contentType, String requestHeaders, String requestBody, int inUse) {
        return mRequestResponseRecordDao.getRequestResponseRecordInUseByEssentialFactors(url, method, contentType, requestHeaders, requestBody, inUse);
    }

    public long saveRequestResponseRecord(@NonNull final RequestResponseRecord requestResponseRecord) {
        return mRequestResponseRecordDao.insertRequestResponseRecord(requestResponseRecord);
    }

    public void deleteRequestResponseRecordsByHost(String host) {
        Runnable deleteRunnable = () -> mRequestResponseRecordDao.deleteRequestResponseRecordsByHost(host);
        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    public int deleteRequestResponseRecord(long id) {
        return mRequestResponseRecordDao.deleteRequestResponseRecord(id);
    }

    public int updateRequestResponseRecord(@NonNull final RequestResponseRecord requestResponseRecord) {
        return mRequestResponseRecordDao.updateRequestResponseRecord(requestResponseRecord);
    }
}
