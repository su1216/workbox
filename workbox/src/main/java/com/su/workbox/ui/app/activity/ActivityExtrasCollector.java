package com.su.workbox.ui.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.base.SimpleActivityLifecycleCallbacks;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.ReflectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ActivityExtrasCollector extends SimpleActivityLifecycleCallbacks {

    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private ActivityExtrasDao mActivityExtrasDao;

    public ActivityExtrasCollector() {
        HttpDataDatabase dataDatabase = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        mActivityExtrasDao = dataDatabase.activityExtrasDao();
    }

    @NonNull
    private static ActivityExtras intent2ActivityExtras(Activity activity, Intent intent) {
        ComponentName componentName = activity.getComponentName();
        ActivityExtras activityExtras = new ActivityExtras();
        activityExtras.setComponentPackageName(componentName.getPackageName());
        activityExtras.setComponentClassName(componentName.getClassName());
        activityExtras.setAction(intent.getAction());
        activityExtras.setData(intent.getDataString());
        activityExtras.setType(intent.getType());
        activityExtras.setFlags(intent.getFlags());
        if (intent.getCategories() != null) {
            activityExtras.setCategoryList(new ArrayList<>(intent.getCategories()));
            activityExtras.setCategories(JSON.toJSONString(activityExtras.getCategoryList()));
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return activityExtras;
        }

        Set<String> keySet = extras.keySet();
        if (keySet == null || keySet.isEmpty()) {
            return activityExtras;
        }
        copyFromBundle(activityExtras, extras);
        activityExtras.setExtras(JSON.toJSONString(activityExtras.getExtraList()));
        return activityExtras;
    }

    static void copyFromBundle(@NonNull ActivityExtras activityExtras, @NonNull Bundle extras) {
        List<ActivityExtra> extraList = activityExtras.getExtraList();
        extraList.clear();
        Set<String> keySet = extras.keySet();
        for (String key : keySet) {
            Object value = extras.get(key);
            if (value == null) {
                continue;
            }
            ActivityExtra activityExtra = new ActivityExtra();
            activityExtra.setName(key);
            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                activityExtra.setArrayClassName(valueClass.getName());
                //如果是数组，并且数组中有元素，则取第一个元素类型
                //无法直接使用Parcelable
                Object[] objects = (Object[]) value;
                Class<?> componentType;
                if (objects.length > 0) {
                    componentType = objects[0].getClass();
                } else {
                    componentType = valueClass.getComponentType();
                }
                activityExtra.setValueClassName(componentType.getName());
            } else if (ReflectUtil.isPrimitiveClass(valueClass) || ReflectUtil.isPrimitiveWrapperClass(valueClass)) {
                activityExtra.setValueClassName(valueClass.getName());
            } else if (valueClass == ArrayList.class) {
                activityExtra.setValueClassName(valueClass.getName());
                ArrayList<?> objects = (ArrayList) value;
                Class<?> componentType;
                if (objects.isEmpty()) {
                    componentType = valueClass.getComponentType();
                } else {
                    componentType = objects.get(0).getClass();
                }
                activityExtra.setListClassName(componentType.getName());
            } else {
                activityExtra.setValueClassName(valueClass.getName());
            }
            activityExtra.setValue(JSON.toJSONString(value));
            extraList.add(activityExtra);
        }
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
            ActivityExtras activityExtras = intent2ActivityExtras(activity, intent);
            if (record != null) {
                return;
            }
            mActivityExtrasDao.insertActivityExtras(activityExtras);
        });
    }
}
