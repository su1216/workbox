package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.SearchableHelper;

/**
 * Created by su on 17-5-31.
 * 调试功能列表 - 组件信息 - 四大组件列表
 */
public class ComponentListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = ComponentListActivity.class.getSimpleName();
    private SearchableHelper mSearchableHelper = new SearchableHelper();
    private ComponentListFragment mFragment;

    public static Intent getLaunchIntent(@NonNull Context context, @NonNull String type) {
        Intent intent = new Intent(context, ComponentListActivity.class);
        intent.putExtra("type", type);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_fragment);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        mFragment = ComponentListFragment.newInstance(type);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mFragment, TAG)
                .commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle(mFragment.getTitle());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFragment.filter(newText);
        return false;
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
