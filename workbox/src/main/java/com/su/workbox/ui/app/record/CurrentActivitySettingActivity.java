package com.su.workbox.ui.app.record;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;

public class CurrentActivitySettingActivity extends BaseAppCompatActivity {

    public static final String TAG = CurrentActivitySettingActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new CurrentActivityFragment(), "current_activity").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("当前Activity信息展示设置");
    }

    public static class CurrentActivityFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(SpHelper.NAME);
            addPreferencesFromResource(R.xml.workbox_preference_current_activity_settings);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
