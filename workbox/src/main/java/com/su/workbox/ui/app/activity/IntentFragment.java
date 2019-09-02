package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Paint;
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
    private boolean mUseComponent = true;
    private SwitchCompat mAutoSwitch;
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
        mDataLayout = view.findViewById(R.id.data_layout);
        mDataView = view.findViewById(R.id.data);
        mTypeLayout = view.findViewById(R.id.type_layout);
        mTypeView = view.findViewById(R.id.type);

        mPackageNameView.setOnClickListener(this);
        mClassNameView.setOnClickListener(this);
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

    public void collectIntentData(Intent intent, IntentData intentData) {
        if (mUseComponent) {
            ComponentName componentName = new ComponentName(mCloneExtras.getComponentPackageName(), mCloneExtras.getComponentClassName());
            intent.setComponent(componentName);
            intentData.setComponentPackageName(mCloneExtras.getComponentPackageName());
            intentData.setComponentClassName(mCloneExtras.getComponentClassName());
        }
        String data = mCloneExtras.getData();
        String type = mCloneExtras.getType();
        intentData.setType(type);
        intentData.setData(data);
        if (TextUtils.isEmpty(data)) {
            intent.setType(type);
        } else {
            intent.setDataAndType(Uri.parse(data), type);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.data_layout) {
            textDialog("data", mCloneExtras.getData(), "updateData", mDataView);
        } else if (id == R.id.type_layout) {
            textDialog("type", mCloneExtras.getType(), "updateType", mTypeView);
        } else if (id == R.id.package_name) {
            mUseComponent = !mUseComponent;
            useComponent();
        } else if (id == R.id.class_name) {
            mUseComponent = !mUseComponent;
            useComponent();
        }
    }

    private void useComponent() {
        Paint packageNamePaint = mPackageNameView.getPaint();
        Paint classNamePaint = mClassNameView.getPaint();
        if (mUseComponent) {
            packageNamePaint.setFlags(packageNamePaint.getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            classNamePaint.setFlags(packageNamePaint.getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            packageNamePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            classNamePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        mPackageNameView.setText(mPackageNameView.getText());
        mClassNameView.setText(mClassNameView.getText());
    }

    public static IntentFragment newInstance(IntentData intentData) {
        Bundle args = new Bundle();
        args.putParcelable("intentData", intentData);
        IntentFragment fragment = new IntentFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
