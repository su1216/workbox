package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件浏览
 */
public class ExplorerActivity extends DataActivity {

    public static final String TAG = ExplorerActivity.class.getSimpleName();
    private File mDatabasesDir;
    private File mSharedPreferenceDir;
    private String mRoot;

    public static void startActivity(@NonNull Context context, @NonNull String root) {
        Intent intent = new Intent(context, ExplorerActivity.class);
        intent.putExtra("root", root);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_fragment);
        mDatabasesDir = new File(mDataDirPath, "databases");
        mSharedPreferenceDir = new File(mDataDirPath, SpHelper.SHARED_PREFERENCE_BASE_DIRNAME);
        mRoot = getIntent().getStringExtra("root");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ExplorerFragment.newInstance(mRoot), TAG)
                .commit();
        MenuItem menuItem = mToolbar.getMenu().findItem(R.id.search);
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                ExplorerFragment fragment = (ExplorerFragment) getSupportFragmentManager().findFragmentByTag(TAG);
                Intent intent = new Intent(ExplorerActivity.this, SearchActivity.class);
                intent.putExtra("root", mRoot);
                intent.putExtra("current", fragment.mCurrentPath);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        });
    }

    @Override
    protected void export() {
        ExplorerFragment fragment = (ExplorerFragment) getSupportFragmentManager().findFragmentByTag(TAG);
        export(fragment.mCurrentPath);
    }

    private void export(String filepath) {
        File file = new File(filepath);
        String fileDirPath;
        if (file.exists() && file.isFile()) {
            fileDirPath = file.getParent();
        } else {
            fileDirPath = filepath;
        }
        int index = fileDirPath.indexOf(mRoot);
        String path = fileDirPath.substring(index + mRoot.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File dir = new File(mExportedBaseDir, path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destFile;
        String msg;
        if (file.isFile()) {
            destFile = new File(dir, file.getName());
            msg = "已将文件" + file.getName() + "导出到" + dir.getAbsolutePath();
        } else {
            destFile = dir;
            msg = "已将目录" + file.getName() + "导出到" + dir.getAbsolutePath();
        }
        IOUtil.copyDirectory(new File(filepath), destFile);
        runOnUiThread(() -> new ToastBuilder(msg).setDuration(Toast.LENGTH_LONG).show());
    }

    private void addFragment(@NonNull String path) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ExplorerFragment.newInstance(path), TAG)
                .addToBackStack(null)
                .commit();
    }

    public static class ExplorerFragment extends BaseFragment {

        private ExplorerActivity mActivity;
        private String mCurrentPath;
        private FileAdapter mFileAdapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = (ExplorerActivity) getActivity();
            if (savedInstanceState == null) {
                Bundle args = getArguments();
                mCurrentPath = args.getString("current");
            } else {
                mCurrentPath = savedInstanceState.getString("current");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.workbox_template_recycler_view, container, false);
            File file = new File(mCurrentPath);
            List<File> files = Arrays.asList(file.listFiles());
            Collections.sort(files);
            mFileAdapter = new FileAdapter(mActivity, files);
            recyclerView.setAdapter(mFileAdapter);
            PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
            recyclerView.addItemDecoration(decoration);
            return recyclerView;
        }

        @Override
        public void onResume() {
            super.onResume();
            mActivity.setTitle(new File(mCurrentPath).getName());
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("current", mCurrentPath);
        }

        public static ExplorerFragment newInstance(@NonNull String current) {
            Bundle args = new Bundle();
            args.putString("current", current);
            ExplorerFragment fragment = new ExplorerFragment();
            fragment.setArguments(args);
            return fragment;
        }
    }

    private static class FileAdapter extends BaseRecyclerAdapter<File> {

        private ExplorerActivity mActivity;

        FileAdapter(ExplorerActivity activity, List<File> data) {
            super(data);
            mActivity = activity;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            File file = getData().get(position);
            View arrowView = holder.getView(R.id.arrow);
            TextView nameView = holder.getView(R.id.name);
            TextView detailView = holder.getView(R.id.detail);
            nameView.setText(file.getName());
            if (file.exists() && file.isDirectory()) {
                setOnClickListenerForDir(file, holder.itemView);
                arrowView.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setOnClickListener(v -> mActivity.export(file.getAbsolutePath()));
                arrowView.setVisibility(View.GONE);
            }
            detailView.setText(IOUtil.getFileBrief(file));
        }

        private void setOnClickListenerForDir(@NonNull File dir, @NonNull View itemView) {
            final String path = dir.getAbsolutePath();
            if (mActivity.mDatabasesDir.equals(dir)) {
                itemView.setOnClickListener(v -> DatabaseListActivity.startActivity(mActivity));
            } else if (mActivity.mSharedPreferenceDir.equals(dir)) {
                itemView.setOnClickListener(v -> mActivity.startActivity(new Intent(mActivity, SharedPreferenceListActivity.class)));
            } else {
                itemView.setOnClickListener(v -> mActivity.addFragment(path));
            }
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_file;
        }

        @Override
        public void updateData(@NonNull List<File> data) {
            Collections.sort(data);
            super.updateData(data);
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_private_data_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
