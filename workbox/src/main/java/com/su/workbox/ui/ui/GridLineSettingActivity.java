package com.su.workbox.ui.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.preference.EditTextPreferenceDialogFragmentCompat;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

public class GridLineSettingActivity extends BaseAppCompatActivity implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    private static final String TAG = GridLineSettingActivity.class.getSimpleName();
    private GridLineFragment mGridLineFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        mGridLineFragment = new GridLineFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, mGridLineFragment, "grid_line").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("网格设置");
    }

    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
        return mGridLineFragment.onPreferenceDisplayDialog(preferenceFragmentCompat, preference);
    }

    public static class GridLineFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,
            Preference.OnPreferenceChangeListener {

        private Activity mActivity;
        private EditTextPreference mLineSizePreference;
        private ListPreference mLineUnitPreference;
        private Preference mLineColorPreference;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(SpHelper.NAME);
            addPreferencesFromResource(R.xml.workbox_preference_grid_line_settings);
            mActivity = getActivity();
            mLineSizePreference = (EditTextPreference) findPreference(SpHelper.COLUMN_GRID_LINE_SIZE);
            mLineSizePreference.setSummary(mLineSizePreference.getText());
            mLineSizePreference.setOnPreferenceChangeListener(this);
            mLineUnitPreference = (ListPreference) findPreference(SpHelper.COLUMN_GRID_LINE_UNIT);
            mLineUnitPreference.setSummary(mLineUnitPreference.getEntries()[Integer.parseInt(mLineUnitPreference.getValue())]);
            mLineUnitPreference.setOnPreferenceChangeListener(this);
            mLineColorPreference = findPreference(SpHelper.COLUMN_GRID_LINE_COLOR_STRING);
            mLineColorPreference.setOnPreferenceClickListener(this);
            SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
            String colorString = sp.getString(SpHelper.COLUMN_GRID_LINE_COLOR_STRING, "");
            mLineColorPreference.setSummary(colorString);
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
                case SpHelper.COLUMN_GRID_LINE_COLOR_STRING:
                    showColorPicker();
                    return true;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, final Object newValue) {
            String key = preference.getKey();
            if (TextUtils.equals(key, SpHelper.COLUMN_GRID_LINE_SIZE)) {
                mLineSizePreference.setSummary((String) newValue);
                mLineSizePreference.setText((String) newValue);
            } else if (TextUtils.equals(key, SpHelper.COLUMN_GRID_LINE_UNIT)) {
                String value = (String) newValue;
                int index = Integer.parseInt(value);
                mLineUnitPreference.setSummary(mLineUnitPreference.getEntries()[index]);
                mLineUnitPreference.setValue(value);
            }
            return false;
        }

        private void showColorPicker() {
            SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
            String cs = sp.getString(SpHelper.COLUMN_GRID_LINE_COLOR_STRING, "#40000000");
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
                        sp.edit().putString(SpHelper.COLUMN_GRID_LINE_COLOR_STRING, colorString).apply();
                        mLineColorPreference.setSummary(colorString);
                    })
                    .show();
        }

        public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
            String key = preference.getKey();
            if (TextUtils.equals(key, SpHelper.COLUMN_GRID_LINE_SIZE)) {
                EditTextPreferenceDialogFragmentCompat f = EditTextPreferenceDialogFragmentCompat.newInstance(preference.getKey(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), "android.support.v14.preference.PreferenceFragment.DIALOG");
                return true;
            }
            return false;
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
