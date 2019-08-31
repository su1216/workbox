package com.su.workbox.ui.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.base.SimpleActivityLifecycleCallbacks;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;

public class ActivityExtrasCollector extends SimpleActivityLifecycleCallbacks {

    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private ActivityExtrasDao mActivityExtrasDao;

    public ActivityExtrasCollector() {
        HttpDataDatabase dataDatabase = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        mActivityExtrasDao = dataDatabase.activityExtrasDao();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        //不收集workbox信息
        Class<?> clazz = activity.getClass();
        if (clazz.getName().startsWith(GeneralInfoHelper.LIB_PACKAGE_NAME)) {
            return;
        }
        Intent intent = activity.getIntent();
        ComponentName componentName = activity.getComponentName();
        String componentPackageName = componentName.getPackageName();
        String componentClassName = componentName.getClassName();
        mAppExecutors.diskIO().execute(() -> {
            ActivityExtras record = mActivityExtrasDao.getActivityExtras(componentPackageName, componentClassName);
            ActivityExtras activityExtras = ActivityExtras.intent2ActivityExtras(activity, intent);
            if (record != null) {
                return;
            }
            mActivityExtrasDao.insertActivityExtras(activityExtras);
        });
    }
}
