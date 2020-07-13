package com.su.workbox.ui.log.crash;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

import java.util.List;

public class CrashLogRecordModel extends AndroidViewModel {

    private LiveData<List<CrashLogRecord>> mRecordListSource;
    private final Observer<List<CrashLogRecord>> mOnRecordListChanged;
    private CrashLogRecordSource mDataSource;
    private CrashLogRecordDao mCrashLogRecordDao;
    private MediatorLiveData<List<CrashLogRecord>> mRecordList;

    private CrashLogRecordModel(@NonNull Application application) {
        super(application);
        HttpDataDatabase database = HttpDataDatabase.getInstance(application);
        mCrashLogRecordDao = database.crashLogRecordDao();
        mDataSource = CrashLogRecordSource.getInstance(mCrashLogRecordDao);
        mRecordList = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mRecordList.setValue(null);
        mRecordListSource = mCrashLogRecordDao.getLogs();
        mOnRecordListChanged = productEntities -> {
            if (database.getDatabaseCreated().getValue() != null) {
                mRecordList.postValue(productEntities);
            }
        };
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
    }

    public MutableLiveData<List<CrashLogRecord>> getRecordList() {
        mRecordList.removeSource(mRecordListSource);
        mRecordListSource = mCrashLogRecordDao.getLogs();
        mRecordList.addSource(mRecordListSource, mOnRecordListChanged);
        return mRecordList;
    }

    public void deleteAll() {
        mDataSource.deleteCrashLogRecords();
    }

    public void delete(long id) {
        mDataSource.deleteCrashLogRecord(id);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        public Factory(@NonNull Application application) {
            mApplication = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new CrashLogRecordModel(mApplication);
        }
    }
}
