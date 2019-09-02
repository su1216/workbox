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
    static final int TYPE_ACTION = 1;
    static final int TYPE_EXTRAS = 2;
    static final int TYPE_CATEGORIES = 3;
    static final int TYPE_FLAGS = 4;

    protected Activity mActivity;
    protected IntentData mIntentData;
    protected IntentData mCloneExtras;

    protected int type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            mIntentData = arguments.getParcelable("intentData");
            mCloneExtras = mIntentData.clone();
        }
    }

    @Nullable
    public IntentExtra findActivityExtraByName(@NonNull String extraName) {
        if (mIntentData == null) {
            return null;
        }
        List<IntentExtra> extras = mIntentData.getExtraList();
        if (extras == null) {
            return null;
        }
        for (IntentExtra extra : extras) {
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

    public IntentData getCloneExtras() {
        return mCloneExtras;
    }

    public abstract void collectIntentData(Intent intent, IntentData intentData);

    public final void resetIntentData() {
        mCloneExtras = mIntentData.clone();
        initViews();
    }

    protected abstract void initViews();
}
