package com.su.workbox.ui.app.record;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class LifecycleRecordModel extends AndroidViewModel {

    private final LifecycleRecordDao mRecordDao;
    private MediatorLiveData<List<LifecycleRecord>> mRecordList;
    private LiveData<List<LifecycleRecord>> mRecordListSource;
    private Observer<List<LifecycleRecord>> mOnRecordListChanged;
    private MediatorLiveData<List<LifecycleRecord.Summary>> mRecordCount;
    private LiveData<List<LifecycleRecord.Summary>> mRecordCountSource;
    private Observer<List<LifecycleRecord.Summary>> mOnRecordCountChanged;
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

        mRecordCount = new MediatorLiveData<>();
        mRecordCount.setValue(null);
        mRecordCountSource = mRecordDao.getAllHistoryRecordCount("");
        mOnRecordCountChanged = count -> {
            if (database.getDatabaseCreated().getValue() != null) {
                mRecordCount.postValue(count);
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

    public MediatorLiveData<List<LifecycleRecord.Summary>> getRecordCount(String keyword) {
        mRecordCount.removeSource(mRecordCountSource);
        mRecordCountSource = mRecordDao.getAllHistoryRecordCount(keyword);
        mRecordCount.addSource(mRecordCountSource, mOnRecordCountChanged);
        return mRecordCount;
    }

    public void deleteAllHistoryRecords() {
        dataSource.deleteAllHistoryRecords();
    }
}
