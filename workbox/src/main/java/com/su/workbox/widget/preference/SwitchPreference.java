package com.su.workbox.widget.preference;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;

public class SwitchPreference extends SwitchPreferenceCompat {

    private final Listener mListener;

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mListener = new Listener();
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.preference.R.attr.switchPreferenceCompatStyle);
    }

    public SwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {}

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(android.support.v7.preference.R.id.switchWidget);
        switchView.setClickable(true);
        switchView.setFocusable(true);
        syncSwitchView(switchView);
        syncSummaryView(holder);
    }

    private void syncSwitchView(View view) {
        SwitchCompat switchView;
        if (view instanceof SwitchCompat) {
            switchView = (SwitchCompat) view;
            switchView.setOnCheckedChangeListener(null);
        }

        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }

        if (view instanceof SwitchCompat) {
            switchView = (SwitchCompat) view;
            switchView.setTextOn(getSwitchTextOn());
            switchView.setTextOff(getSwitchTextOff());
            switchView.setOnCheckedChangeListener(mListener);
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!SwitchPreference.this.callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
            } else {
                SwitchPreference.this.setChecked(isChecked);
            }
        }
    }
}
