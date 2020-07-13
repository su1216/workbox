package com.su.workbox.ui.usage;

import androidx.annotation.NonNull;

import com.su.workbox.utils.AppExecutors;

public class DataUsageRecordSource {

    private static volatile DataUsageRecordSource sInstance;

    private DataUsageRecordDao mDataUsageRecordDao;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();

    // Prevent direct instantiation.
    private DataUsageRecordSource(@NonNull DataUsageRecordDao dataUsageRecordDao) {
        this.mDataUsageRecordDao = dataUsageRecordDao;
    }

    public static DataUsageRecordSource getInstance(@NonNull DataUsageRecordDao dataUsageRecordDao) {
        if (sInstance == null) {
            synchronized (DataUsageRecordSource.class) {
                if (sInstance == null) {
                    sInstance = new DataUsageRecordSource(dataUsageRecordDao);
                }
            }
        }
        return sInstance;
    }

    public void deleteAllDataUsageRecords(String query) {
        Runnable deleteRunnable = () -> mDataUsageRecordDao.deleteDataUsageRecords(query);
        mAppExecutors.diskIO().execute(deleteRunnable);
    }
}
