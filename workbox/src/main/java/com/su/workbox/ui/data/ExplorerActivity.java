package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.SwipeController;

import java.io.File;
import java.util.ArrayList;
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
        IOUtil.export(this, mExportedBaseDir, mRoot, fragment.mCurrentPath);
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
            List<File> files = new ArrayList<>();
            Collections.addAll(files, file.listFiles());
            Collections.sort(files);
            mFileAdapter = new FileAdapter(mActivity, recyclerView, files);
            recyclerView.setAdapter(mFileAdapter);
            PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
            recyclerView.addItemDecoration(decoration);
            SwipeController swipeController = new SwipeController(mFileAdapter);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);
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

    private static class FileAdapter extends BaseRecyclerAdapter<File> implements SwipeController.OnSwipeListener {

        private ExplorerActivity mActivity;
        private RecyclerView mRecyclerView;
        private File mRecentlyDeletedItem;
        private int mRecentlyDeletedItemPosition;

        FileAdapter(ExplorerActivity activity, @NonNull RecyclerView recyclerView, List<File> data) {
            super(data);
            mRecyclerView = recyclerView;
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
                holder.itemView.setOnClickListener(v -> FileActivity.startActivity(mActivity, mActivity.mRoot, file.getAbsolutePath()));
                arrowView.setVisibility(View.GONE);
            }
            detailView.setText(IOUtil.getFileBrief(file));
        }

        private void setOnClickListenerForDir(@NonNull File dir, @NonNull View itemView) {
            final String path = dir.getAbsolutePath();
            if (mActivity.mDatabasesDir.equals(dir)) {
                itemView.setOnClickListener(v -> mActivity.startActivity(DatabaseListActivity.getLaunchIntent(mActivity)));
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

        @Override
        public void onDelete(@NonNull RecyclerView.ViewHolder viewHolder) {
            List<File> list = getData();
            int position = viewHolder.getAdapterPosition();
            mRecentlyDeletedItem = list.get(position);
            mRecentlyDeletedItemPosition = position;
            Snackbar.make(mRecyclerView, "已将" + mRecentlyDeletedItem.getName() + "删除", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> onUndo())
                    .addCallback(new Snackbar.Callback() {
                        public void onShown(Snackbar sb) {
                            list.remove(mRecentlyDeletedItem);
                            notifyItemRemoved(mRecentlyDeletedItemPosition);
                        }

                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                IOUtil.deleteFiles(mRecentlyDeletedItem);
                            }
                        }
                    })
                    .show();
        }

        @Override
        public void onUndo() {
            if (mRecentlyDeletedItem == null) {
                return;
            }
            List<File> list = getData();
            list.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
            notifyItemInserted(mRecentlyDeletedItemPosition);
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
