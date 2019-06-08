package com.su.workbox.ui.app.record;

import android.support.annotation.NonNull;

import com.su.workbox.utils.AppExecutors;

public class LifecycleRecordSource {

    private static volatile LifecycleRecordSource sInstance;

    private LifecycleRecordDao mLifecycleRecordDao;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();

    // Prevent direct instantiation.
    private LifecycleRecordSource(@NonNull LifecycleRecordDao lifecycleRecordDao) {
        this.mLifecycleRecordDao = lifecycleRecordDao;
    }

    public static LifecycleRecordSource getInstance(@NonNull LifecycleRecordDao lifecycleRecordDao) {
        if (sInstance == null) {
            synchronized (LifecycleRecordSource.class) {
                if (sInstance == null) {
                    sInstance = new LifecycleRecordSource(lifecycleRecordDao);
                }
            }
        }
        return sInstance;
    }

    public void deleteAllHistoryRecords() {
        Runnable deleteRunnable = () -> mLifecycleRecordDao.deleteAllHistoryRecords();
        mAppExecutors.diskIO().execute(deleteRunnable);
    }
}
