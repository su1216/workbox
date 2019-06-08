package com.su.workbox.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface FragmentLifecycleCallbacks {

    void onAttach(Fragment fragment, Context context);

    void onAttach(Fragment fragment, Activity activity);

    void onCreate(Fragment fragment, @Nullable Bundle savedInstanceState);

    void onCreateView(Fragment fragment, @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    void onViewCreated(Fragment fragment, @NonNull View view, @Nullable Bundle savedInstanceState);

    void onStart(Fragment fragment);

    void onResume(Fragment fragment);

    void onPause(Fragment fragment);

    void onStop(Fragment fragment);

    void onDestroyView(Fragment fragment);

    void onDestroy(Fragment fragment);

    void onDetach(Fragment fragment);

    void onSaveInstanceState(Fragment fragment, @NonNull Bundle outState);

    void onHiddenChanged(Fragment fragment, boolean hidden);

    void onViewStateRestored(Fragment fragment, @Nullable Bundle savedInstanceState);
}
