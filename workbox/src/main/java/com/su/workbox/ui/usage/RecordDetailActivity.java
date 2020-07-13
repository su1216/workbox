package com.su.workbox.ui.usage;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.widget.SimpleOnTabSelectedListener;


/**
 * 流量监控记录 - 流量监控记录详情
 * */
public class RecordDetailActivity extends BaseAppCompatActivity {

    public static final String TAG = RecordDetailActivity.class.getSimpleName();
    private DataUsageRecord mDataUsageRecord;
    private ViewPager mPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_templete_pager);
        Intent intent = getIntent();
        mDataUsageRecord = intent.getParcelableExtra("record");
        mPager = findViewById(R.id.pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.addTab(makeTab("Request"));
        mTabLayout.addTab(makeTab("Response"));
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mPager.setAdapter(new RecordPagerAdapter(getSupportFragmentManager()));
        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("详情");
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    private class RecordPagerAdapter extends FragmentPagerAdapter {
        RecordPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return RecordRequestDetailFragment.newInstance(mDataUsageRecord);
            } else {
                return RecordResponseDetailFragment.newInstance(mDataUsageRecord);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
