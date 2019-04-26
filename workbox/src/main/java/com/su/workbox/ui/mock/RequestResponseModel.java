package com.su.workbox.ui.mock;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class RequestResponseModel extends AndroidViewModel {

    private MediatorLiveData<List<RequestResponseRecord>> mRecordList;
    private MediatorLiveData<List<RequestResponseRecord.Summary>> mSummaryList;
    private LiveData<List<RequestResponseRecord>> mRecordListSource;
    private RequestResponseRecordDao mRequestResponseRecordDao;
    private RequestResponseRecordSource mDataSource;
    private final HttpDataDatabase mDatabase;
    private final String mHost;
    private Observer<List<RequestResponseRecord>> mOnRecordListChanged;

    private RequestResponseModel(Application application, String host) {
        super(application);
        mDatabase = HttpDataDatabase.getInstance(application);
        HttpDataDatabase database = HttpDataDatabase.getInstance(application);
        mRequestResponseRecordDao = database.requestResponseRecordDao();
        mDataSource = RequestResponseRecordSource.getInstance(mRequestResponseRecordDao);

        mSummaryList = new MediatorLiveData<>();
        mSummaryList.setValue(null);
        mSummaryList.addSource(mRequestResponseRecordDao.getRequestResponseRecordSummaryList(), recordSummary -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mSummaryList.postValue(recordSummary);
            }
        });

        mRecordList = new MediatorLiveData<>();
        mRecordList.setValue(null);

        mHost = host;
        if (TextUtils.isEmpty(host)) {
            mRecordListSource = mRequestResponseRecordDao.getAllRequestResponseRecords();
        } else {
            mRecordListSource = mRequestResponseRecordDao.getRequestResponseRecords(host, "");
        }
        mOnRecordListChanged = records -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(records);
            }
        };
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
    }

    public MediatorLiveData<List<RequestResponseRecord>> getRecordList(String query) {
        mRecordList.removeSource(mRecordListSource);
        mRecordListSource = mRequestResponseRecordDao.getRequestResponseRecords(mHost, query);
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
        return mRecordList;
    }

    public MediatorLiveData<List<RequestResponseRecord.Summary>> getSummaryList() {
        if (mSummaryList == null) {
            mSummaryList = new MediatorLiveData<>();
        }
        return mSummaryList;
    }

    public void deleteByHost(String host) {
        mDataSource.deleteRequestResponseRecordsByHost(host);
    }

    public int updateRequestResponseRecord(@NonNull final RequestResponseRecord requestResponseRecord) {
        return mDataSource.updateRequestResponseRecord(requestResponseRecord);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        @Nullable
        private final String mHost;

        public Factory(@NonNull Application application, @Nullable String host) {
            mApplication = application;
            mHost = host;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new RequestResponseModel(mApplication, mHost);
        }
    }
}
