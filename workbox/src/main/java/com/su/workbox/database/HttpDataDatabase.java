package com.su.workbox.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;

import com.su.workbox.ui.app.activity.IntentData;
import com.su.workbox.ui.app.activity.IntentDataDao;
import com.su.workbox.ui.app.record.LifecycleRecord;
import com.su.workbox.ui.app.record.LifecycleRecordDao;
import com.su.workbox.ui.usage.DataUsageRecord;
import com.su.workbox.ui.mock.RequestResponseRecord;
import com.su.workbox.ui.log.crash.CrashLogRecord;
import com.su.workbox.ui.log.crash.CrashLogRecordDao;
import com.su.workbox.ui.mock.RequestResponseRecordDao;
import com.su.workbox.ui.usage.DataUsageRecordDao;

@Database(entities = {DataUsageRecord.class,
        RequestResponseRecord.class,
        LifecycleRecord.class,
        IntentData.class,
        CrashLogRecord.class}, version = 7)
public abstract class HttpDataDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "workbox.db";
    private static HttpDataDatabase sInstance;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public abstract DataUsageRecordDao dataUsageRecordDao();
    public abstract RequestResponseRecordDao requestResponseRecordDao();
    public abstract LifecycleRecordDao activityRecordDao();
    public abstract IntentDataDao intentDataDao();
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
