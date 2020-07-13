package com.su.workbox.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.app.record.LifecycleRecord;
import com.su.workbox.ui.app.record.LifecycleRecordDao;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;

public class FragmentLifecycleListener implements FragmentLifecycleCallbacks {

    private static boolean sEnableLog = false;
    @SuppressLint("StaticFieldLeak")
    private static FragmentLifecycleListener sFragmentLifecycleListener = new FragmentLifecycleListener();
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private LifecycleRecordDao mLifecycleRecordDao;

    public static FragmentLifecycleListener getInstance() {
        return sFragmentLifecycleListener;
    }

    private FragmentLifecycleListener() {
        mLifecycleRecordDao = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext()).activityRecordDao();
    }

    private void save(Fragment child, String event) {
        Fragment fragment = child.getParentFragment();
        Class<?> clazz = fragment.getClass();
        if (clazz.getName().startsWith(GeneralInfoHelper.LIB_PACKAGE_NAME)) {
            return;
        }

        LifecycleRecord record = new LifecycleRecord();
        record.setType(LifecycleRecord.FRAGMENT);
        record.setCreateTime(System.currentTimeMillis());
        record.setName(clazz.getName());
        record.setSimpleName(clazz.getSimpleName());
        Activity activity = fragment.getActivity();
        if (activity != null) {
            record.setTaskId(activity.getTaskId());
        }
        record.setFragmentTag(fragment.getTag());
        record.setFragmentId(fragment.getId());
        if (fragment.getParentFragment() != null) {
            record.setParentFragment(fragment.getParentFragment().getClass().getName());
        }
        record.setEvent(event);
        insertFragmentRecord(record);

        if (sEnableLog) {
            Log.d(clazz.getSimpleName(), event);
        }
    }

    private void insertFragmentRecord(LifecycleRecord record) {
        Runnable runnable = () -> mLifecycleRecordDao.insertFragmentRecord(record);
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void onAttach(Fragment fragment, Context context) {
        save(fragment, "attach");
    }

    @Override
    public void onAttach(Fragment fragment, Activity activity) {
        save(fragment, "attach");
    }

    @Override
    public void onCreate(Fragment fragment, @Nullable Bundle savedInstanceState) {
        save(fragment, "create");
    }

    @Override
    public void onCreateView(Fragment fragment, @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        save(fragment, "createView");
    }

    @Override
    public void onViewCreated(Fragment fragment, @NonNull View view, @Nullable Bundle savedInstanceState) {
        save(fragment, "viewCreated");
    }

    @Override
    public void onStart(Fragment fragment) {
        save(fragment, "start");
    }

    @Override
    public void onResume(Fragment fragment) {
        save(fragment, "resume");
    }

    @Override
    public void onPause(Fragment fragment) {
        save(fragment, "pause");
    }

    @Override
    public void onStop(Fragment fragment) {
        save(fragment, "stop");
    }

    @Override
    public void onDestroyView(Fragment fragment) {
        save(fragment, "destroyView");
    }

    @Override
    public void onDestroy(Fragment fragment) {
        save(fragment, "destroy");
    }

    @Override
    public void onDetach(Fragment fragment) {
        save(fragment, "detach");
    }

    @Override
    public void onSaveInstanceState(Fragment fragment, @NonNull Bundle outState) {
        save(fragment, "saveInstanceState");
    }

    @Override
    public void onHiddenChanged(Fragment fragment, boolean hidden) {
        save(fragment, "hiddenChanged");
    }

    @Override
    public void onViewStateRestored(Fragment fragment, @Nullable Bundle savedInstanceState) {
        save(fragment, "viewStateRestored");
    }

    public static void setEnableLog(boolean enableLog) {
        sEnableLog = enableLog;
    }
}
