package com.su.workbox.ui.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.su.workbox.ui.base.BaseFragment;

import java.util.List;

public abstract class IntentBaseInfoFragment extends BaseFragment {

    static final int TYPE_BASE = 0;
    static final int TYPE_EXTRAS = 1;
    static final int TYPE_CATEGORIES = 2;
    static final int TYPE_FLAGS = 3;

    protected Activity mActivity;
    protected ActivityExtras mActivityExtras;
    protected ActivityExtras mCloneExtras;

    protected int type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            mActivityExtras = arguments.getParcelable("activityExtras");
            mCloneExtras = mActivityExtras.clone();
        }
    }

    @Nullable
    public ActivityExtra findActivityExtraByName(@NonNull String extraName) {
        if (mActivityExtras == null) {
            return null;
        }
        List<ActivityExtra> activityExtras = mActivityExtras.getExtraList();
        if (activityExtras == null) {
            return null;
        }
        for (ActivityExtra extra : activityExtras) {
            if (TextUtils.equals(extraName, extra.getName())) {
                return extra;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    boolean checkRequired() {
        return true;
    }

    public ActivityExtras getCloneExtras() {
        return mCloneExtras;
    }

    public abstract void collectIntentData(Intent intent);

    public final void resetIntentData() {
        mCloneExtras = mActivityExtras.clone();
        initViews();
    }

    protected abstract void initViews();
}
