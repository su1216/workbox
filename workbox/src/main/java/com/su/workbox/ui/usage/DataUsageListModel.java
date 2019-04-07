package com.su.workbox.ui.usage;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.database.table.DataUsageRecord;

import java.util.List;

public class DataUsageListModel extends AndroidViewModel {

    private final HttpDataDatabase mDatabase;
    private MediatorLiveData<List<DataUsageRecord>> mRecordList;
    private MediatorLiveData<DataUsageRecord.Summary> mSummary;
    private DataUsageRecordDao mDataUsageRecordDao;
    private LiveData<List<DataUsageRecord>> mRecordListSource;
    private LiveData<DataUsageRecord.Summary> mSummarySource;
    private Observer<List<DataUsageRecord>> mOnRecordListChanged;
    private Observer<DataUsageRecord.Summary> mOnSummaryChanged;

    public DataUsageListModel(@NonNull Application application) {
        super(application);
        mDatabase = HttpDataDatabase.getInstance(application);
        mDataUsageRecordDao = mDatabase.dataUsageRecordDao();
        mRecordList = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mRecordList.setValue(null);
        mRecordListSource = mDataUsageRecordDao.getDataUsageRecords("");
        mOnRecordListChanged = productEntities -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(productEntities);
            }
        };
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);

        mSummary = new MediatorLiveData<>();
        mSummary.setValue(null);
        mSummarySource = mDataUsageRecordDao.getDataUsageRecordSummary("");
        mOnSummaryChanged = recordSummary -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mSummary.postValue(recordSummary);
            }
        };
        mSummary.addSource(mSummarySource, mOnSummaryChanged);
    }

    public MutableLiveData<List<DataUsageRecord>> getRecordList(String query) {
        mRecordList.removeSource(mRecordListSource);
        mRecordListSource = mDataUsageRecordDao.getDataUsageRecords(query);
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
        return mRecordList;
    }

    public MutableLiveData<DataUsageRecord.Summary> getSummary(String query) {
        mSummary.removeSource(mSummarySource);
        mSummarySource = mDataUsageRecordDao.getDataUsageRecordSummary(query);
        mSummary.addSource(mSummarySource, mOnSummaryChanged);
        return mSummary;
    }
}
