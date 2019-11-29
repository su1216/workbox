package com.su.workbox.ui.app.record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.base.SimpleActivityLifecycleCallbacks;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;

public class ActivityLifecycleListener extends SimpleActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static ActivityLifecycleListener sActivityLifecycleListener;
    private Activity mTopActivity;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private LifecycleRecordDao mLifecycleRecordDao;
    private CurrentActivityView mCurrentActivityView;

    public ActivityLifecycleListener() {
        HttpDataDatabase dataDatabase = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        mLifecycleRecordDao = dataDatabase.activityRecordDao();
        mCurrentActivityView = CurrentActivityView.getInstance();
    }

    private void save(Activity activity, String event) {
        Class<?> clazz = activity.getClass();
        if (clazz.getName().startsWith(GeneralInfoHelper.LIB_PACKAGE_NAME)) {
            return;
        }
        LifecycleRecord record = new LifecycleRecord();
        record.setType(LifecycleRecord.ACTIVITY);
        record.setCreateTime(System.currentTimeMillis());
        record.setName(clazz.getName());
        record.setSimpleName(clazz.getSimpleName());
        record.setTaskId(activity.getTaskId());
        record.setEvent(event);
        insertActivityRecord(record);
    }

    private void insertActivityRecord(LifecycleRecord record) {
        Runnable runnable = () -> mLifecycleRecordDao.insertActivityRecord(record);
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        save(activity, "created");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        save(activity, "started");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        save(activity, "resumed");
        mTopActivity = activity;
        if (mCurrentActivityView.isShowing()) {
            mCurrentActivityView.updateTopActivity(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        save(activity, "paused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        save(activity, "stopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        save(activity, "saveInstanceState");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        save(activity, "destroyed");
        if (mTopActivity == activity) {
            mTopActivity = null;
            mCurrentActivityView.updateTopActivity(null);
        }
    }

    public static Activity getTopActivity() {
        return sActivityLifecycleListener.mTopActivity;
    }

    public static void setActivityLifecycleListener(ActivityLifecycleListener listener) {
        if (sActivityLifecycleListener == null) {
            sActivityLifecycleListener = listener;
        } else {
            throw new IllegalStateException("ActivityLifecycleListener has already initialized.");
        }
    }
}
