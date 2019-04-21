package com.su.workbox.ui.app.record;

import android.support.annotation.NonNull;

import com.su.workbox.utils.AppExecutors;

public class ActivityRecordSource {

    private static volatile ActivityRecordSource sInstance;

    private ActivityRecordDao mActivityRecordDao;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();

    // Prevent direct instantiation.
    private ActivityRecordSource(@NonNull ActivityRecordDao activityRecordDao) {
        this.mActivityRecordDao = activityRecordDao;
    }

    public static ActivityRecordSource getInstance(@NonNull ActivityRecordDao activityRecordDao) {
        if (sInstance == null) {
            synchronized (ActivityRecordSource.class) {
                if (sInstance == null) {
                    sInstance = new ActivityRecordSource(activityRecordDao);
                }
            }
        }
        return sInstance;
    }

    public void deleteAllActivityRecords() {
        Runnable deleteRunnable = () -> mActivityRecordDao.deleteAllActivityRecords();
        mAppExecutors.diskIO().execute(deleteRunnable);
    }
}
