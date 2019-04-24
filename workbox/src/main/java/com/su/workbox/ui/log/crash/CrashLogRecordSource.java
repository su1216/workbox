package com.su.workbox.ui.log.crash;

import android.support.annotation.NonNull;

import com.su.workbox.utils.AppExecutors;

public class CrashLogRecordSource {

    private static volatile CrashLogRecordSource sInstance;

    private CrashLogRecordDao mCrashLogRecordDao;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();

    // Prevent direct instantiation.
    private CrashLogRecordSource(@NonNull CrashLogRecordDao crashLogRecordDao) {
        this.mCrashLogRecordDao = crashLogRecordDao;
    }

    public static CrashLogRecordSource getInstance(@NonNull CrashLogRecordDao crashLogRecordDao) {
        if (sInstance == null) {
            synchronized (CrashLogRecordSource.class) {
                if (sInstance == null) {
                    sInstance = new CrashLogRecordSource(crashLogRecordDao);
                }
            }
        }
        return sInstance;
    }

    public void insertCrashLogRecords(CrashLogRecord crashLogRecord) {
        Runnable insertRunnable = () -> mCrashLogRecordDao.insertLog(crashLogRecord);
        mAppExecutors.diskIO().execute(insertRunnable);
    }

    public void deleteCrashLogRecords() {
        Runnable deleteRunnable = () -> mCrashLogRecordDao.deleteLogs();
        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    public void deleteCrashLogRecord(long id) {
        Runnable deleteRunnable = () -> mCrashLogRecordDao.deleteLog(id);
        mAppExecutors.diskIO().execute(deleteRunnable);
    }
}
