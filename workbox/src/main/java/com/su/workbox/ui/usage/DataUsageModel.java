package com.su.workbox.ui.usage;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;

public class DataUsageModel extends AndroidViewModel {

    private DataUsageRecordSource dataSource;

    private DataUsageModel(@NonNull Application application) {
        super(application);
        HttpDataDatabase database = HttpDataDatabase.getInstance(application);
        dataSource = DataUsageRecordSource.getInstance(database.dataUsageRecordDao());
    }

    public void deleteAll(String query) {
        dataSource.deleteAllDataUsageRecords(query);
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
            return (T) new DataUsageModel(mApplication);
        }
    }
}
