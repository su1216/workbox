package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.su.workbox.R;

public class IntentFragment extends IntentBaseInfoFragment implements View.OnClickListener {

    private TextView mPackageNameView;
    private TextView mClassNameView;
    private SwitchCompat mAutoSwitch;
    private View mActionLayout;
    private TextView mActionView;
    private View mDataLayout;
    private TextView mDataView;
    private View mTypeLayout;
    private TextView mTypeView;

    public IntentFragment() {
        type = TYPE_BASE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workbox_fragment_activity_base_info, container, false);
        mPackageNameView = view.findViewById(R.id.package_name);
        mClassNameView = view.findViewById(R.id.class_name);
        mAutoSwitch = view.findViewById(R.id.auto);
        mActionLayout = view.findViewById(R.id.action_layout);
        mActionView = view.findViewById(R.id.action);
        mDataLayout = view.findViewById(R.id.data_layout);
        mDataView = view.findViewById(R.id.data);
        mTypeLayout = view.findViewById(R.id.type_layout);
        mTypeView = view.findViewById(R.id.type);

        mActionLayout.setOnClickListener(this);
        mDataLayout.setOnClickListener(this);
        mTypeLayout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
    }

    @Override
    protected void initViews() {
        mPackageNameView.setText(mCloneExtras.getComponentPackageName());
        mClassNameView.setText(mCloneExtras.getComponentClassName());
        mAutoSwitch.setChecked(mCloneExtras.isAuto());
        mActionView.setText(mCloneExtras.getAction());
        mDataView.setText(mCloneExtras.getData());
        mTypeView.setText(mCloneExtras.getType());
    }

    private void textDialog(String title, String content, String action, TextView textView) {
        EditText inputView = new EditText(mActivity);
        inputView.setText(content);
        new AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String newContent = inputView.getText().toString();
                    action(newContent, action, textView);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void action(String content, @NonNull String action, TextView textView) {
        switch (action) {
            case "updateAction":
                mCloneExtras.setAction(content);
                textView.setText(content);
                break;
            case "updateData":
                mCloneExtras.setData(content);
                textView.setText(content);
                break;
            case "updateType":
                mCloneExtras.setType(content);
                textView.setText(content);
                break;
            default:
                break;
        }
    }

    public void collectIntentData(Intent intent) {
        ComponentName componentName = new ComponentName(mCloneExtras.getComponentPackageName(), mCloneExtras.getComponentClassName());
        intent.setComponent(componentName);
        intent.setAction(mCloneExtras.getAction());
        String data = mCloneExtras.getData();
        String type = mCloneExtras.getType();
        if (TextUtils.isEmpty(data)) {
            intent.setType(type);
        } else {
            intent.setDataAndType(Uri.parse(data), type);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_layout) {
            textDialog("action", mCloneExtras.getAction(), "updateAction", mActionView);
        } else if (id == R.id.data_layout) {
            textDialog("data", mCloneExtras.getData(), "updateData", mDataView);
        } else if (id == R.id.type_layout) {
            textDialog("type", mCloneExtras.getType(), "updateType", mTypeView);
        }
    }

    public static IntentFragment newInstance(ActivityExtras activityExtras) {
        Bundle args = new Bundle();
        args.putParcelable("activityExtras", activityExtras);
        IntentFragment fragment = new IntentFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
