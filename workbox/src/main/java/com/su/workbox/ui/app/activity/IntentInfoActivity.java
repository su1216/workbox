package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.su.workbox.R;
import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.widget.SimpleOnTabSelectedListener;

import java.util.ArrayList;

/**
 * Created by su on 19-7-21.
 * 调试功能列表 - 组件信息 - 四大组件列表 - 四大组件详情
 */
public class IntentInfoActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = IntentInfoActivity.class.getSimpleName();
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private ComponentInfo mComponentInfo;
    private IntentData mIntentData;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private IntentDataDao mIntentDataDao;
    private InfoPagerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_templete_pager);
        HttpDataDatabase database = HttpDataDatabase.getInstance(this);
        mIntentDataDao = database.intentDataDao();
        Intent intent = getIntent();
        mComponentInfo = intent.getParcelableExtra("info");
        mTabLayout = findViewById(R.id.tab_layout);
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Activity详情");

        mToolbar.setNavigationOnClickListener(v -> showSaveDialog());
        Menu menu = mToolbar.getMenu();
        MenuItem addMenu = menu.findItem(R.id.add);
        addMenu.setVisible(false);
        findViewById(R.id.reset).setOnClickListener(this);
        findViewById(R.id.launch).setOnClickListener(this);
        mPager = findViewById(R.id.pager);
        mTabLayout.addTab(makeTab("Base"));
        mTabLayout.addTab(makeTab("Extras"));
        mTabLayout.addTab(makeTab("Categories"));
        mTabLayout.addTab(makeTab("Flags"));
        mPager.setOffscreenPageLimit(3);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mAppExecutors.diskIO().execute(() -> {
            mIntentData = mIntentDataDao.getActivityExtras(mComponentInfo.packageName, mComponentInfo.name);
            if (mIntentData == null) {
                mIntentData = new IntentData();
                mIntentData.setComponentPackageName(mComponentInfo.packageName);
                mIntentData.setComponentClassName(mComponentInfo.name);
            }
            mIntentData.initExtrasAndCategories();
            runOnUiThread(() -> {
                mAdapter = new InfoPagerAdapter(getSupportFragmentManager());
                mPager.setAdapter(mAdapter);

                mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        mPager.setCurrentItem(position);
                        IntentBaseInfoFragment fragment = mAdapter.mFragmentList.get(position);
                        int type = fragment.getType();
                        addMenu.setVisible(type == IntentBaseInfoFragment.TYPE_CATEGORIES || type == IntentBaseInfoFragment.TYPE_EXTRAS);
                    }
                });
                findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            });
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.launch) {
            launch();
        } else if (id == R.id.reset) {
            reset();
        }
    }

    private void launch() {
        Intent intent = makeLaunchIntent();
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void reset() {
        SparseArray<IntentBaseInfoFragment> fragmentArray = mAdapter.mFragmentList;
        int size = fragmentArray.size();
        for (int i = 0; i < size ; i++) {
            IntentBaseInfoFragment fragment = fragmentArray.get(fragmentArray.keyAt(i));
            fragment.resetIntentData();
        }
    }

    private Intent makeLaunchIntent() {
        Intent intent = new Intent();
        SparseArray<IntentBaseInfoFragment> fragmentArray = mAdapter.mFragmentList;
        int size = fragmentArray.size();
        for (int i = 0; i < size ; i++) {
            IntentBaseInfoFragment fragment = fragmentArray.get(fragmentArray.keyAt(i));
            if (!fragment.checkRequired()) {
                return null;
            }

            fragment.collectIntentData(intent);
        }
        return intent;
    }

    private void save() {
        Intent intent = makeLaunchIntent();
        if (intent == null) {
            return;
        }

        mAppExecutors.diskIO().execute(() -> {
            mIntentData.setAction(intent.getAction());
            mIntentData.setData(intent.getDataString());
            mIntentData.setType(intent.getType());
            mIntentData.setFlags(intent.getFlags());
            if (intent.getCategories() != null) {
                mIntentData.setCategories(JSON.toJSONString(new ArrayList<>(intent.getCategories())));
            }
            IntentDataCollector.copyFromBundle(mIntentData, intent.getExtras());
            mIntentData.setExtras(JSON.toJSONString(mIntentData.getExtraList()));
            mIntentDataDao.insertActivityExtras(mIntentData);
        });
    }

    private class InfoPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<IntentBaseInfoFragment> mFragmentList = new SparseArray<>();

        InfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            IntentBaseInfoFragment fragment;
            if (position == 0) {
                fragment = IntentFragment.newInstance(mIntentData);
            } else if (position == 1) {
                fragment = IntentExtrasFragment.newInstance(mIntentData);
            } else if (position == 2) {
                fragment = IntentCategoriesFragment.newInstance(mIntentData);
            } else {
                fragment = IntentFlagsFragment.newInstance(mIntentData);
            }
            mFragmentList.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @MenuRes
    @Override
    public int menuRes() {
        return R.menu.workbox_add_menu;
    }

    public void add(@NonNull MenuItem item) {
        IntentBaseInfoFragment fragment = mAdapter.mFragmentList.get(mPager.getCurrentItem());
        int type = fragment.getType();
        if (type == IntentBaseInfoFragment.TYPE_CATEGORIES) {
            IntentCategoriesFragment categoriesFragment = (IntentCategoriesFragment) fragment;
            categoriesFragment.showAddDialog();
        } else if (type == IntentBaseInfoFragment.TYPE_EXTRAS) {
            IntentExtrasFragment extrasFragment = (IntentExtrasFragment) fragment;
            extrasFragment.showAddDialog();
        }
    }

    private void showSaveDialog() {
        new AlertDialog.Builder(this)
                .setMessage("是否保存当前数据？")
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    save();
                    finish();
                })
                .setNegativeButton(R.string.workbox_cancel, (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onBackPressed() {
        showSaveDialog();
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
