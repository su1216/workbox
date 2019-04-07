package com.su.workbox.ui.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

public class RulerSettingActivity extends BaseAppCompatActivity {

    public static final String TAG = RulerSettingActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new MeasureFragment(), "measure").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("测距设置");
    }

    public static class MeasureFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

        private Activity mActivity;
        private Preference mColorPreference;
        private Preference mResultColorPreference;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(SpHelper.NAME);
            addPreferencesFromResource(R.xml.workbox_preference_measure_settings);
            mActivity = getActivity();
            mColorPreference = findPreference(SpHelper.COLUMN_MEASURE_COLOR_STRING);
            mColorPreference.setOnPreferenceClickListener(this);
            mResultColorPreference = findPreference(SpHelper.COLUMN_MEASURE_RESULT_COLOR_STRING);
            mResultColorPreference.setOnPreferenceClickListener(this);

            SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
            String colorString = sp.getString(SpHelper.COLUMN_MEASURE_COLOR_STRING, "#B0FF0000");
            String resultColorString = sp.getString(SpHelper.COLUMN_MEASURE_RESULT_COLOR_STRING, "#B00000FF");
            mColorPreference.setSummary(colorString);
            mResultColorPreference.setSummary(resultColorString);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDividerHeight(-1);
            PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
            getListView().addItemDecoration(decoration);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            switch (key) {
                case SpHelper.COLUMN_MEASURE_COLOR_STRING:
                    showColorPicker(preference, "#B0FF0000");
                    return true;
                case SpHelper.COLUMN_MEASURE_RESULT_COLOR_STRING:
                    showColorPicker(preference, "#B00000FF");
                    return true;
                default:
                    break;
            }
            return false;
        }

        private void showColorPicker(Preference preference, String defaultValue) {
            SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
            String cs = sp.getString(preference.getKey(), defaultValue);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.workbox_dialog_color_picker, null);
            View colorView = view.findViewById(R.id.color);
            colorView.setBackgroundColor(Color.parseColor(cs));
            TextView colorValueView = view.findViewById(R.id.color_value);
            colorValueView.setText(cs);
            ColorPickerView pickerView = view.findViewById(R.id.picker);
            pickerView.setColor(Color.parseColor(cs));
            pickerView.setOnColorChangedListener(color -> {
                colorView.setBackgroundColor(color);
                colorValueView.setText(UiHelper.color2rgbString(color));
            });
            new AlertDialog.Builder(mActivity)
                    .setView(view)
                    .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                        int color = pickerView.getColor();
                        String colorString = UiHelper.color2rgbString(color);
                        sp.edit().putString(preference.getKey(), colorString).apply();
                        preference.setSummary(colorString);
                    })
                    .show();
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
