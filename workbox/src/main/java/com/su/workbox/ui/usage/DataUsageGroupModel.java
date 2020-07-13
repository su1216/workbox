package com.su.workbox.ui.usage;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class DataUsageGroupModel extends AndroidViewModel {

    private final HttpDataDatabase mDatabase;
    private MediatorLiveData<DataUsageRecord.Summary> mSummary;
    private MediatorLiveData<List<DataUsageRecord.Group>> mRecordList;

    public DataUsageGroupModel(@NonNull Application application) {
        super(application);
        mDatabase = HttpDataDatabase.getInstance(application);
        DataUsageRecordDao dataUsageRecordDao = mDatabase.dataUsageRecordDao();
        mRecordList = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mRecordList.setValue(null);
        LiveData<List<DataUsageRecord.Group>> recordListSource = dataUsageRecordDao.getDataUsageRecordGroupByUrl();
        mRecordList.addSource(recordListSource, productEntities -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(productEntities);
            }
        });

        mSummary = new MediatorLiveData<>();
        mSummary.setValue(null);
        LiveData<DataUsageRecord.Summary> summarySource = dataUsageRecordDao.getDataUsageRecordSummary("");
        mSummary.addSource(summarySource, recordSummary -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mSummary.postValue(recordSummary);
            }
        });
    }

    public MutableLiveData<List<DataUsageRecord.Group>> getGroupList() {
        return mRecordList;
    }

    public MutableLiveData<DataUsageRecord.Summary> getSummary() {
        return mSummary;
    }
}
