package com.su.workbox.ui.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.base.SimpleActivityLifecycleCallbacks;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.utils.SpHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntentDataCollector extends SimpleActivityLifecycleCallbacks {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private final AppExecutors mAppExecutors = AppExecutors.getInstance();
    private final IntentDataDao mIntentDataDao;

    public IntentDataCollector() {
        HttpDataDatabase dataDatabase = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        mIntentDataDao = dataDatabase.intentDataDao();
    }

    @NonNull
    private static IntentData intent2ActivityExtras(Activity activity, Intent intent) {
        ComponentName componentName = activity.getComponentName();
        IntentData intentData = new IntentData();
        intentData.setComponentPackageName(componentName.getPackageName());
        intentData.setComponentClassName(componentName.getClassName());
        intentData.setAction(intent.getAction());
        intentData.setData(intent.getDataString());
        intentData.setType(intent.getType());
        intentData.setFlags(intent.getFlags());
        if (intent.getCategories() != null) {
            intentData.setCategoryList(new ArrayList<>(intent.getCategories()));
            intentData.setCategories(gson.toJson(intentData.getCategoryList()));
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return intentData;
        }

        Set<String> keySet = extras.keySet();
        if (keySet == null || keySet.isEmpty()) {
            return intentData;
        }
        copyFromBundle(intentData, extras);
        intentData.setExtras(gson.toJson(intentData.getExtraList()));
        return intentData;
    }

    private static Class<?> getArrayElementClass(@Nullable Object[] objects) {
        if (objects == null) {
            return null;
        }
        for (Object object: objects) {
            if (object != null) {
                return object.getClass();
            }
        }
        return null;
    }

    static void copyFromBundle(@NonNull IntentData intentData, @NonNull Bundle extras) {
        List<IntentExtra> extraList = intentData.getExtraList();
        extraList.clear();
        Set<String> keySet = extras.keySet();
        for (String key : keySet) {
            Object value = extras.get(key);
            if (value == null) {
                continue;
            }
            IntentExtra intentExtra = new IntentExtra();
            intentExtra.setName(key);
            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                intentExtra.setArrayClassName(valueClass.getName());
                //如果是数组，并且数组中有元素，则取第一个元素类型
                //无法直接使用Parcelable
                int length = Array.getLength(value);
                Object[] objects = new Object[length];
                for (int i = 0; i < length; i ++) {
                    objects[i] = Array.get(value, i);
                }
                Class<?> componentType = getArrayElementClass(objects);
                if (componentType == null) {
                    componentType = valueClass.getComponentType();
                }
                intentExtra.setValueClassName(componentType.getName());
                if (!ExcludeTypes.exclude(componentType)) {
                    intentExtra.setValue(gson.toJson(value));
                }
            } else if (ReflectUtil.isPrimitiveClass(valueClass) || ReflectUtil.isPrimitiveWrapperClass(valueClass)) {
                intentExtra.setValueClassName(valueClass.getName());
                if (value instanceof String) {
                    intentExtra.setValue((String) value);
                } else {
                    intentExtra.setValue(gson.toJson(value));
                }
            } else if (valueClass == ArrayList.class) {
                intentExtra.setListClassName(valueClass.getName());
                ArrayList<?> objects = (ArrayList) value;
                Class<?> componentType;
                if (objects.isEmpty()) {
                    componentType = valueClass.getComponentType();
                } else {
                    componentType = objects.get(0).getClass();
                }
                intentExtra.setValueClassName(componentType.getName());
                if (!ExcludeTypes.exclude(componentType)) {
                    intentExtra.setValue(gson.toJson(value));
                }
            } else {
                intentExtra.setValueClassName(valueClass.getName());
                if (!ExcludeTypes.exclude(valueClass)) {
                    intentExtra.setValue(gson.toJson(value));
                }
            }
            extraList.add(intentExtra);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        if (!SpHelper.getWorkboxSharedPreferences().getBoolean("activity_launcher", true)) {
            return;
        }

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
            IntentData record = mIntentDataDao.getActivityExtras(componentPackageName, componentClassName);
            if (record != null) {
                return;
            }
            IntentData intentData = intent2ActivityExtras(activity, intent);
            mIntentDataDao.insertActivityExtras(intentData);
        });
    }
}
