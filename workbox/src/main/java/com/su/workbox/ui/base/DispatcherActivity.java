package com.su.workbox.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.su.workbox.R;

import java.io.Serializable;

public class DispatcherActivity extends BaseAppCompatActivity {

    private static final String TAG = DispatcherActivity.class.getSimpleName();
    private BaseFragment mBaseFragment;

    public static Intent getLaunchIntentWithBaseFragment(@NonNull Context context, @NonNull Class<? extends BaseFragment> baseFragmentClass) {
        return getLaunchIntentWithBaseFragment(context, baseFragmentClass, null);
    }

    public static Intent getLaunchIntentWithBaseFragment(@NonNull Context context, @NonNull Class<? extends BaseFragment> baseFragmentClass, @Nullable Bundle bundle) {
        Intent intent = new Intent(context, DispatcherActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.putExtra("base_fragment", baseFragmentClass);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_fragment);
        Intent intent = getIntent();
        Class<BaseFragment> baseFragmentClass = getBaseFragmentClass(intent);
        if (baseFragmentClass == null) {
            Toast.makeText(this, "no dest fragment.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        intent.putExtra("base_fragment", (Serializable) null);
        intent.putExtra("base_fragment_classname", (String) null);
        Fragment fragment;
        {
            mBaseFragment = BaseFragment.newInstance(baseFragmentClass, intent.getExtras());
            fragment = mBaseFragment;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @SuppressWarnings("unchecked")
    private Class<BaseFragment> getBaseFragmentClass(Intent intent) {
        Class<BaseFragment> baseFragmentClass = (Class<BaseFragment>) intent.getSerializableExtra("base_fragment");
        if (baseFragmentClass != null) {
            return baseFragmentClass;
        }
        String baseFragmentClassname = intent.getStringExtra("base_fragment_classname");
        if (TextUtils.isEmpty(baseFragmentClassname)) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName(baseFragmentClassname);
            if (BaseFragment.class.isAssignableFrom(clazz)) {
                return (Class<BaseFragment>) clazz;
            }
        } catch (ClassNotFoundException e) {
            Log.w(TAG, e);
        }
        return null;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mBaseFragment.setTitle(mToolbar);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
