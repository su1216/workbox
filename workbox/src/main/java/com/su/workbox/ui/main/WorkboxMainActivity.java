package com.su.workbox.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.UiHelper;

/**
 * Created by su on 18-7-20.
 * 调试功能列表
 */
public class WorkboxMainActivity extends BaseAppCompatActivity {
    private static final String TAG = WorkboxMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new DebugListFragment(), "debug_list")
                .commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("调式功能列表");
    }

    public void info(@NonNull MenuItem item) {
        UiHelper.showConfirm(this, GeneralInfoHelper.getLibName() + ": " + GeneralInfoHelper.getLibVersion());
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_info_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
