package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件浏览
 * */
public class ExplorerActivity extends DataActivity {

    public static final String TAG = ExplorerActivity.class.getSimpleName();
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
        mRoot = getIntent().getStringExtra("root");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ExplorerFragment.newInstance(mRoot), TAG)
                .commit();
    }

    @Override
    protected void export() {
        ExplorerFragment fragment = (ExplorerFragment) getSupportFragmentManager().findFragmentByTag(TAG);
        int index = fragment.mCurrentPath.indexOf(mRoot);
        String path = fragment.mCurrentPath.substring(index + mRoot.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File dir = new File(mExportedBaseDir, path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        IOUtil.copyDirectory(new File(fragment.mCurrentPath), dir);
        runOnUiThread(() -> new ToastBuilder("已将应用私有文件导出到" + dir.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
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
                holder.itemView.setOnClickListener(v -> mActivity.addFragment(file.getAbsolutePath()));
                detailView.setText("items: " + file.list().length);
                arrowView.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setOnClickListener(null);
                detailView.setText("size: " + SystemInfoHelper.formatFileSize(file.length()));
                arrowView.setVisibility(View.GONE);
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
    protected String getTag() {
        return TAG;
    }
}
