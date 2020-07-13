package com.su.workbox.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import com.su.workbox.R;

public class ListenerFragment extends Fragment {

    private FragmentLifecycleListener mListener;

    public FragmentLifecycleListener getFragmentLifecycleListener() {
        return mListener;
    }

    public void setFragmentLifecycleListener(FragmentLifecycleListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener.onAttach(this, context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mListener.onAttach(this, activity);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener.onCreate(this, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListener.onCreateView(this, inflater, container, savedInstanceState);
        Space space = new Space(getContext());
        space.setId(R.id.workbox_space);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(0, 0);
        space.setLayoutParams(lp);
        return space;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListener.onViewCreated(this, view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.onPause(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener.onStop(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener.onDestroyView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener.onDestroy(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDetach(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mListener.onSaveInstanceState(this, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mListener.onViewStateRestored(this, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mListener.onHiddenChanged(this, hidden);
    }
}
