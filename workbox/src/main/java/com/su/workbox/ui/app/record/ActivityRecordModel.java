package com.su.workbox.ui.app.record;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class ActivityRecordModel extends AndroidViewModel {

    private final ActivityRecordDao mRecordDao;
    private MediatorLiveData<List<ActivityRecord>> mRecordList;
    private LiveData<List<ActivityRecord>> mRecordListSource;
    private Observer<List<ActivityRecord>> mOnRecordListChanged;
    private ActivityRecordSource dataSource;

    public ActivityRecordModel(@NonNull Application application) {
        super(application);
        HttpDataDatabase database = HttpDataDatabase.getInstance(application);
        dataSource = ActivityRecordSource.getInstance(database.activityRecordDao());
        mRecordDao = database.activityRecordDao();
        mRecordList = new MediatorLiveData<>();
        mRecordList.setValue(null);
        mRecordListSource = mRecordDao.getActivityRecordsByTask("");
        mOnRecordListChanged = productEntities -> {
            if (database.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(productEntities);
            }
        };
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
    }

    public MediatorLiveData<List<ActivityRecord>> getRecordList(String taskId) {
        mRecordList.removeSource(mRecordListSource);
        mRecordListSource = mRecordDao.getActivityRecordsByTask(taskId);
//        mRecordListSource = mRecordDao.getAllActivityRecords();
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
        return mRecordList;
    }

    public void deleteAllActivityRecords() {
        dataSource.deleteAllActivityRecords();
    }
}
