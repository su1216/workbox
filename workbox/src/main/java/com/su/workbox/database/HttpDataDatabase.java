package com.su.workbox.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.su.workbox.ui.app.record.LifecycleRecord;
import com.su.workbox.ui.app.record.LifecycleRecordDao;
import com.su.workbox.ui.usage.DataUsageRecord;
import com.su.workbox.ui.mock.RequestResponseRecord;
import com.su.workbox.ui.log.crash.CrashLogRecord;
import com.su.workbox.ui.log.crash.CrashLogRecordDao;
import com.su.workbox.ui.mock.RequestResponseRecordDao;
import com.su.workbox.ui.usage.DataUsageRecordDao;

@Database(entities = {DataUsageRecord.class, RequestResponseRecord.class, LifecycleRecord.class, CrashLogRecord.class}, version = 5)
public abstract class HttpDataDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "workbox.db";
    private static HttpDataDatabase sInstance;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public abstract DataUsageRecordDao dataUsageRecordDao();
    public abstract RequestResponseRecordDao requestResponseRecordDao();
    public abstract LifecycleRecordDao activityRecordDao();
    public abstract CrashLogRecordDao crashLogRecordDao();

    private static final Object LOCK = new Object();

    public static HttpDataDatabase getInstance(final Context context) {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        HttpDataDatabase.class, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                HttpDataDatabase database = HttpDataDatabase.getInstance(context);
                                database.setDatabaseCreated();
                            }
                        })
                        .build();
                sInstance.updateDatabaseCreated(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
