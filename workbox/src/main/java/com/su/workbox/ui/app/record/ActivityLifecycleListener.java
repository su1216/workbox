package com.su.workbox.ui.app.record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static ActivityLifecycleListener sActivityLifecycleListener;
    private Activity mTopActivity;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private ActivityRecordDao mActivityRecordDao;
    private CurrentActivityView mCurrentActivityView;

    public ActivityLifecycleListener() {
        mActivityRecordDao = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext()).activityRecordDao();
        mCurrentActivityView = CurrentActivityView.getInstance();
    }

    private void save(Activity activity, String event) {
        Class<?> clazz = activity.getClass();
        if (clazz.getName().startsWith(GeneralInfoHelper.LIB_PACKAGE_NAME)) {
            return;
        }
        ActivityRecord record = new ActivityRecord();
        record.setCreateTime(System.currentTimeMillis());
        record.setName(clazz.getName());
        record.setSimpleName(clazz.getSimpleName());
        record.setTaskId(activity.getTaskId());
        record.setEvent(event);
        insertActivityRecord(record);
    }

    private void insertActivityRecord(ActivityRecord record) {
        Runnable deleteRunnable = () -> mActivityRecordDao.insertActivityRecord(record);
        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        save(activity, "created");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        save(activity, "started");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        save(activity, "resumed");
        mTopActivity = activity;
        if (mCurrentActivityView.isShowing()) {
            mCurrentActivityView.updateTopActivity(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        save(activity, "paused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        save(activity, "stopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        save(activity, "saveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
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
