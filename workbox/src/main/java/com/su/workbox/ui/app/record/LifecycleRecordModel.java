package com.su.workbox.ui.app.record;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class LifecycleRecordModel extends AndroidViewModel {

    private final LifecycleRecordDao mRecordDao;
    private MediatorLiveData<List<LifecycleRecord>> mRecordList;
    private LiveData<List<LifecycleRecord>> mRecordListSource;
    private Observer<List<LifecycleRecord>> mOnRecordListChanged;
    private LifecycleRecordSource dataSource;

    public LifecycleRecordModel(@NonNull Application application) {
        super(application);
        HttpDataDatabase database = HttpDataDatabase.getInstance(application);
        dataSource = LifecycleRecordSource.getInstance(database.activityRecordDao());
        mRecordDao = database.activityRecordDao();
        mRecordList = new MediatorLiveData<>();
        mRecordList.setValue(null);
        mRecordListSource = mRecordDao.getActivityRecordsByKeyword("");
        mOnRecordListChanged = productEntities -> {
            if (database.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(productEntities);
            }
        };
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
    }

    public MediatorLiveData<List<LifecycleRecord>> getRecordList(String keyword) {
        mRecordList.removeSource(mRecordListSource);
        mRecordListSource = mRecordDao.getActivityRecordsByKeyword(keyword);
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
        return mRecordList;
    }

    public void deleteAllHistoryRecords() {
        dataSource.deleteAllHistoryRecords();
    }
}
