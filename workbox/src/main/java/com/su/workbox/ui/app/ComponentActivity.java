package com.su.workbox.ui.app;

import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.entity.Parameter;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.widget.SimpleOnTabSelectedListener;

import java.util.List;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 组件信息 - 四大组件列表 - 四大组件详情
 */
public class ComponentActivity extends BaseAppCompatActivity {

    private static final String TAG = ComponentActivity.class.getSimpleName();
    private int mTabSize = 1;
    private int mExtrasTabIndex = -1;
    private int mFlagsTabIndex = -1;
    private String mType;
    private ComponentInfo mComponentInfo;
    private NoteComponentEntity mNoteComponent;
    private ComponentName mComponentName;
    private ComponentExtrasFragment mComponentExtrasFragment;
    private ComponentFlagsFragment mComponentFlagsFragment;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private boolean mCanBeLaunched;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_templete_pager);
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        mComponentInfo = intent.getParcelableExtra("info");
        mNoteComponent = intent.getParcelableExtra("note");
        mComponentName = new ComponentName(mComponentInfo.packageName, mComponentInfo.name);
        if (mNoteComponent == null) {
            mNoteComponent = new NoteComponentEntity();
        }

        mTabLayout = findViewById(R.id.tab_layout);
        if (mNoteComponent.getParameters().isEmpty() && !"activity".equalsIgnoreCase(mType)) {
            mTabLayout.setVisibility(View.GONE);
        } else {
            mTabLayout.setVisibility(View.VISIBLE);
        }

        mCanBeLaunched = canBeLaunched();
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle();

        mPager = findViewById(R.id.pager);
        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position);
                if (!showMenu()) {
                    return;
                }
                Menu menu = mToolbar.getMenu();
                MenuItem format = menu.findItem(R.id.format);
                if (position == 1) {
                    format.setVisible(!mNoteComponent.getParameters().isEmpty());
                } else if (mNoteComponent.getParameters().isEmpty()) {
                    format.setVisible(false);
                } else {
                    format.setVisible(false);
                }
            }
        });

        mTabLayout.addTab(makeTab("Info"));
        if (mCanBeLaunched) {
            if (!mNoteComponent.getParameters().isEmpty()) {
                mTabLayout.addTab(makeTab("Extras"));
                mExtrasTabIndex = 1;
                mTabSize += 1;
            }
            if ("activity".equalsIgnoreCase(mType)) {
                mTabLayout.addTab(makeTab("Flags"));
                mFlagsTabIndex = mExtrasTabIndex + 1;
                mTabSize += 1;
            }
        }
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mPager.setAdapter(new InfoPagerAdapter(getSupportFragmentManager()));
    }

    private boolean canBeLaunched() {
        if ("service".equalsIgnoreCase(mType)) {
            String className = mComponentInfo.name;
            try {
                Class<?> clazz = Class.forName(className);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        && TileService.class.isAssignableFrom(clazz)) {
                    return false;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && JobService.class.isAssignableFrom(clazz)) {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e);
            }
        }
        return true;
    }

    private class InfoPagerAdapter extends FragmentPagerAdapter {
        InfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return ComponentInfoFragment.newInstance(mType, mNoteComponent, mComponentInfo);
            } else if (i == mExtrasTabIndex) {
                mComponentExtrasFragment = ComponentExtrasFragment.newInstance(mNoteComponent);
                return mComponentExtrasFragment;
            } else {
                mComponentFlagsFragment = ComponentFlagsFragment.newInstance(mNoteComponent);
                return mComponentFlagsFragment;
            }
        }

        @Override
        public int getCount() {
            return mTabSize;
        }
    }

    private void setTitle() {
        switch (mType) {
            case "activity":
                setTitle("Activity详情");
                break;
            case "service":
                setTitle("Service详情");
                break;
            case "receiver":
                setTitle("Receiver详情");
                break;
            case "provider":
                setTitle("Provider详情");
                break;
            default:
                break;
        }
    }

    private boolean showMenu() {
        return ("activity".equalsIgnoreCase(mType) || "service".equalsIgnoreCase(mType)) && mCanBeLaunched;
    }

    @MenuRes
    @Override
    public int menuRes() {
        if (showMenu()) {
            return R.menu.workbox_activity_parameters_menu;
        }
        return 0;
    }

    //格式化当前EditText中的参数
    public void format(@NonNull MenuItem item) {
        mComponentExtrasFragment.format();
        if (mPager.getCurrentItem() != 1) {
            mPager.setCurrentItem(1);
        }
    }

    public void go(@NonNull MenuItem item) {
        List<Parameter> parameters = mNoteComponent.getParameters();
        Intent intent;
        if (parameters.isEmpty()) {
            intent = new Intent();
        } else {
            if (mComponentExtrasFragment.checkRequired()) {
                intent = mComponentExtrasFragment.makeIntent();
            } else {
                if (mPager.getCurrentItem() != 1) {
                    mPager.setCurrentItem(1);
                }
                return;
            }
        }
        intent.setComponent(mComponentName);
        //flags
        if (mComponentFlagsFragment != null) {
            int flags = mComponentFlagsFragment.getFlags();
            intent.setFlags(flags);
        }
        //start
        if ("activity".equalsIgnoreCase(mType)) {
            startActivity(intent);
        } else if ("service".equalsIgnoreCase(mType)) {
            startService(intent);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
