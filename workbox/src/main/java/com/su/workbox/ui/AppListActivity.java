package com.su.workbox.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.entity.AppInfo;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.SimpleOnTabSelectedListener;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = AppListActivity.class.getSimpleName();
    public static final int TYPE_INSTALLED = 0;
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_ALL = 2;
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private static final String[] TITLES = {"Installed", "System", "All"};
    private TabLayout mTabLayout;
    private ViewPager mPager;
    private List<AppInfo> mAppInfoList = new ArrayList<>();
    private AppPagerAdapter mAppPagerAdapter;
    private SearchableHelper mSearchableHelper = new SearchableHelper();
    private SearchableHelper mInstalledSearchableHelper = new SearchableHelper(AppInfo.class);
    private SearchableHelper mSystemSearchableHelper = new SearchableHelper(AppInfo.class);
    private SearchableHelper mAllSearchableHelper = new SearchableHelper(AppInfo.class);
    private String mQueryText = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_templete_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.addTab(makeTab("Installed"));
        mTabLayout.addTab(makeTab("System"));
        mTabLayout.addTab(makeTab("All"));
        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position);
                AppListFragment fragment = mAppPagerAdapter.fragmentList.get(position);
                fragment.filter(mQueryText);
            }
        });
        mAppPagerAdapter = new AppPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAppPagerAdapter);
        View contentLayout = findViewById(R.id.content_layout);
        View progressBar = findViewById(R.id.progress_bar);
        contentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mAppExecutors.networkIO().execute(() -> {
            initAppInfoList();
            runOnUiThread(() -> {
                contentLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("应用列表");
        mSearchableHelper.initSearchToolbar(mToolbar, this);
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    @NonNull
    private List<AppInfo> getAllAppInfoList(String keyword) {
        mAllSearchableHelper.clear();
        List<AppInfo> list = new ArrayList<>();
        for (AppInfo appInfo : mAppInfoList) {
            if (mAllSearchableHelper.find(keyword, appInfo)) {
                list.add(appInfo);
            }
        }
        return list;
    }

    @NonNull
    private List<AppInfo> getSystemAppInfoList(String keyword) {
        mSystemSearchableHelper.clear();
        List<AppInfo> list = new ArrayList<>();
        for (AppInfo appInfo : mAppInfoList) {
            if ((ApplicationInfo.FLAG_SYSTEM & appInfo.getFlags()) == ApplicationInfo.FLAG_SYSTEM
                    && mSystemSearchableHelper.find(keyword, appInfo)) {
                list.add(appInfo);
            }
        }
        return list;
    }

    @NonNull
    private List<AppInfo> getInstalledAppInfoList(String keyword) {
        mInstalledSearchableHelper.clear();
        List<AppInfo> list = new ArrayList<>();
        for (AppInfo appInfo : mAppInfoList) {
            if ((ApplicationInfo.FLAG_SYSTEM & appInfo.getFlags()) == 0
                    && mInstalledSearchableHelper.find(keyword, appInfo)) {
                list.add(appInfo);
            }
        }
        return list;
    }

    private void initAppInfoList() {
        if (!mAppInfoList.isEmpty()) {
            return;
        }
        PackageManager pm = getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo packageInfo = packageInfoList.get(i);
            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
            if (packageInfo.applicationInfo.loadIcon(pm) == null) {
                continue;
            }
            appInfo.setIconDrawable(packageInfo.applicationInfo.loadIcon(pm));
            appInfo.setPackageName(packageInfo.packageName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appInfo.setVersionCode(packageInfo.getLongVersionCode());
            } else {
                appInfo.setVersionCode(packageInfo.versionCode);
            }
            appInfo.setVersionName(packageInfo.versionName);
            appInfo.setFlags(packageInfo.applicationInfo.flags);
            appInfo.setLaunchIntent(pm.getLaunchIntentForPackage(packageInfo.packageName));
            mAppInfoList.add(appInfo);
        }
        Collections.sort(mAppInfoList);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mQueryText = s;
        AppListFragment fragment = mAppPagerAdapter.fragmentList.get(mPager.getCurrentItem());
        fragment.filter(mQueryText);
        return false;
    }

    private static class AppPagerAdapter extends FragmentPagerAdapter {

        private List<AppListFragment> fragmentList = new ArrayList<>();

        private AppPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList.add(AppListFragment.newInstance(TYPE_INSTALLED));
            fragmentList.add(AppListFragment.newInstance(TYPE_SYSTEM));
            fragmentList.add(AppListFragment.newInstance(TYPE_ALL));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }

    public static class AppListFragment extends Fragment {

        private AppListActivity mActivity;
        private RecyclerView mRecyclerView;
        private AppAdapter mAppAdapter;
        private int mType;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Bundle bundle = getArguments();
            mType = bundle.getInt("type");
            mActivity = (AppListActivity) getActivity();
            mAppAdapter = new AppAdapter(mActivity, mType, Collections.emptyList());
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.workbox_template_recycler_view, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            mRecyclerView = view.findViewById(R.id.recycler_view);
            mRecyclerView.setAdapter(mAppAdapter);
            filter("");
        }

        public void filter(String keyword) {
            List<AppInfo> list;
            switch (mType) {
                case TYPE_INSTALLED:
                    list = new ArrayList<>(mActivity.getInstalledAppInfoList(keyword));
                    break;
                case TYPE_SYSTEM:
                    list = new ArrayList<>(mActivity.getSystemAppInfoList(keyword));
                    break;
                case TYPE_ALL:
                    list = new ArrayList<>(mActivity.getAllAppInfoList(keyword));
                    break;
                default:
                    list = new ArrayList<>();
                    break;
            }
            mAppAdapter.updateData(list);
        }

        int getType() {
            return mType;
        }

        static AppListFragment newInstance(int type) {
            AppListFragment fragment = new AppListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            fragment.setArguments(bundle);
            return fragment;
        }
    }

    private static class AppAdapter extends BaseRecyclerAdapter<AppInfo> {

        private AppListActivity mAppListActivity;
        private int mType;

        private AppAdapter(AppListActivity activity, int type, List<AppInfo> data) {
            super(data);
            mAppListActivity = activity;
            mType = type;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_app;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            AppInfo appInfo = getData().get(position);
            ImageView iconView = holder.getView(R.id.icon);
            TextView nameView = holder.getView(R.id.name);
            TextView packageNameView = holder.getView(R.id.packageName);
            TextView versionView = holder.getView(R.id.version);
            TextView systemView = holder.getView(R.id.system);
            iconView.setImageDrawable(appInfo.getIconDrawable());
            nameView.setText(appInfo.getAppName());
            switch (mType) {
                case TYPE_INSTALLED:
                    mAppListActivity.mInstalledSearchableHelper.refreshFilterColor(nameView, position, "appName");
                    break;
                case TYPE_SYSTEM:
                    mAppListActivity.mSystemSearchableHelper.refreshFilterColor(nameView, position, "appName");
                    break;
                case TYPE_ALL:
                    mAppListActivity.mAllSearchableHelper.refreshFilterColor(nameView, position, "appName");
                    break;
                default:
                    break;
            }
            packageNameView.setText(appInfo.getPackageName());
            versionView.setText(appInfo.getVersionName() + " / " + appInfo.getVersionCode());
            systemView.setText((ApplicationInfo.FLAG_SYSTEM & appInfo.getFlags()) == ApplicationInfo.FLAG_SYSTEM ? "system" : "");
            Intent launchIntent = appInfo.getLaunchIntent();
            if (launchIntent == null) {
                holder.itemView.setEnabled(false);
                holder.itemView.setOnClickListener(null);
            } else {
                holder.itemView.setEnabled(true);
                holder.itemView.setOnClickListener(v -> GeneralInfoHelper.getContext().startActivity(launchIntent));
            }
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_search_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
