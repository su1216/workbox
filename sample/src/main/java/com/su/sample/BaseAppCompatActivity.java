package com.su.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.su.sample.web.WebViewActivity;

/**
 * Created by su on 18-1-2.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    public void setTitle(@Nullable String title) {
        mToolbar.setTitle(title);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = findViewById(R.id.id_toolbar);
        //抢标页面/转让页面等没有toolbar
        if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(v -> popStackIfNeeded(BaseAppCompatActivity.this));
        }
    }

    private static void popStackIfNeeded(AppCompatActivity activity) {
        int count = activity.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (activity instanceof WebViewActivity) {
                activity.finish();
            } else {
                activity.onBackPressed();
            }
        } else {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
