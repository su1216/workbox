package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.PermissionInfoWrapper;
import com.su.workbox.ui.base.PermissionRequiredActivity;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-5-27.
 * 权限列表
 */
public class PermissionListActivity extends PermissionRequiredActivity implements RecyclerItemClickListener.OnItemClickListener {
    private static final String TAG = PermissionListActivity.class.getSimpleName();
    /**
     * from {@link android.content.pm.PermissionInfo}.
     */
    public static final int FLAG_REMOVED = 1 << 1;
    public static final int REQUEST_CODE = 1;

    private String mPackageName;
    private PackageManager mPm;
    private List<PermissionInfoWrapper> mDataList = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, PermissionListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mPm = getPackageManager();
        mPackageName = getPackageName();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecyclerViewAdapter(this, mDataList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("权限列表");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        mDataList.clear();
        try {
            List<PermissionInfoWrapper> permissionList = PermissionInfoWrapper.getPermissionsByPackageName(this, mPackageName);
            mDataList.addAll(permissionList);
        } catch (PackageManager.NameNotFoundException e) {
            new ToastBuilder("包名错误： " + e.getMessage()).setDuration(Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        PermissionInfoWrapper.rearrangeData(mDataList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        PermissionInfoWrapper wrapper = mDataList.get(position);
        if (wrapper.isCustom()) {
            UiHelper.showConfirm(this, "这是App内自定义权限");
            return;
        }
        if (!PermissionInfoWrapper.isDangerous(wrapper.protectionLevel)) {
            return;
        }
        if (wrapper.isHasPermission()) {
            AppHelper.goAppSettings(this, getPackageName());
        } else {
            permissionRequest(wrapper.name, REQUEST_CODE);
        }
    }

    @Override
    public AlertDialog makeHintDialog(String permission, int requestCode) {
        PermissionInfoWrapper wrapper = findPermissionInfoWrapperByPermission(permission);
        return new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage(wrapper.name + "\n" + wrapper.loadDescription(mPm))
                .setPositiveButton(R.string.workbox_set_permission, (dialog, which) -> AppHelper.goAppSettings(this, getPackageName()))
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    @NonNull
    private PermissionInfoWrapper findPermissionInfoWrapperByPermission(@NonNull String permission) {
        for (PermissionInfoWrapper wrapper : mDataList) {
            if (TextUtils.equals(wrapper.name, permission)) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("can not find permission in the app permission list: " + permission);
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<PermissionInfoWrapper> {

        private Resources mResources;
        private PackageManager mPm;

        private RecyclerViewAdapter(@NonNull Context context, @NonNull List<PermissionInfoWrapper> data) {
            super(data);
            mPm = context.getPackageManager();
            mResources = context.getResources();
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_permission;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final PermissionInfoWrapper info = getData().get(position);
            TextView nameTextView = holder.getView(R.id.name);
            if (info.isCustom()) {
                nameTextView.setText(info.name);
                nameTextView.setTextColor(mResources.getColor(R.color.workbox_first_text));
            } else {
                if (info.isHasPermission()) {
                    nameTextView.setText(info.name + " ✔");
                    nameTextView.setTextColor(mResources.getColor(R.color.workbox_first_text));
                } else {
                    nameTextView.setText(info.name + " ✘");
                    nameTextView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
                }
            }

            int level = info.protectionLevel;
            TextView levelView = holder.getView(R.id.level);
            levelView.setText("level: " + PermissionInfoWrapper.protectionToString(level));
            if ((level & PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS) {
                levelView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
            } else {
                levelView.setTextColor(mResources.getColor(R.color.workbox_second_text));
            }
            TextView groupView = holder.getView(R.id.group);
            if (TextUtils.isEmpty(info.group)) {
                groupView.setVisibility(View.GONE);
            } else {
                groupView.setVisibility(View.VISIBLE);
                groupView.setText(info.group);
            }
            TextView descView = holder.getView(R.id.desc);
            CharSequence desc = info.loadDescription(mPm);
            if (TextUtils.isEmpty(desc)) {
                descView.setVisibility(View.GONE);
            } else {
                descView.setVisibility(View.VISIBLE);
                descView.setText(desc);
            }
        }
    }

    @MenuRes
    @Override
    public int menuRes() {
        return R.menu.workbox_setting_menu;
    }

    public void goSetting(@NonNull MenuItem item) {
        AppHelper.goAppSettings(this, getPackageName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workbox_setting_menu, menu);
        return true;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
