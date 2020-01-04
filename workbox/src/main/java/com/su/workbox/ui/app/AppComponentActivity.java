package com.su.workbox.ui.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 应用信息
 */
public class AppComponentActivity extends BaseAppCompatActivity {
    private static final String TAG = AppComponentActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new InfoListFragment(), "component_info").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("组件信息");
    }

    public static class InfoListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private FragmentActivity mActivity;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.workbox_preference_app_info);
            mActivity = getActivity();
            findPreference("activity").setOnPreferenceClickListener(this);
            findPreference("service").setOnPreferenceClickListener(this);
            findPreference("receiver").setOnPreferenceClickListener(this);
            findPreference("provider").setOnPreferenceClickListener(this);
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
            mActivity.startActivity(ComponentListActivity.getLaunchIntent(mActivity, key));
            return true;
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
