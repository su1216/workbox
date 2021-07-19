package com.su.workbox.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();
    protected Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    public void setTitle(Toolbar toolbar) {}

    public static <T extends BaseFragment> T newInstance(Class<T> clazz, Bundle bundle) {
        T fragment = null;
        try {
            fragment = clazz.newInstance();
            fragment.setArguments(bundle);
        } catch (IllegalAccessException | java.lang.InstantiationException e) {
            Log.w(TAG, e);
        }
        return fragment;
    }
}
