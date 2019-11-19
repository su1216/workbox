package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文件浏览
 */
public class FilesInAppExplorerActivity extends BaseAppCompatActivity {

    public static final String TAG = FilesInAppExplorerActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private String mCurrentDirs;
    private List<String> mAllEntries = new ArrayList<>();
    private List<String> mAllFileEntries = new ArrayList<>();
    private List<String> mAllDirs = new ArrayList<>();
    private List<String> mTopLevelEntries = new ArrayList<>();
    private FileAdapter mAdapter;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, FilesInAppExplorerActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new FileAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(mAdapter);
        prepareData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setCurrentDir("/");
    }

    private void setCurrentDir(String dir) {
        mCurrentDirs = dir;
        if (mCurrentDirs.startsWith("/")) {
            mCurrentDirs = dir;
        } else {
            mCurrentDirs = "/" + dir;
        }
        if (!TextUtils.equals("/", mCurrentDirs)) {
            mCurrentDirs = mCurrentDirs.replaceFirst("/$", "");
        }
        setTitle(mCurrentDirs);
    }

    private void prepareData() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "解析中...");
        mAppExecutors.diskIO().execute(() -> {
            ZipFile zipFile = null;
            try {
                Set<String> dirSet = new HashSet<>();
                zipFile = new ZipFile(GeneralInfoHelper.getSourceDir());
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {
                    String entryName = enumeration.nextElement().getName();
                    mAllFileEntries.add(entryName);
                    dirSet.addAll(getEntryParents(entryName));
                    File file = new File(entryName);
                    if (file.getParent() == null) {
                        mTopLevelEntries.add(entryName);
                    }
                }
                mAllDirs.addAll(dirSet);
                mAllEntries.addAll(mAllFileEntries);
                mAllEntries.addAll(mAllDirs);
                Collections.sort(mAllEntries);

                for (String filename : mAllDirs) {
                    File file = new File(filename);
                    if (file.getParent() == null) {
                        mTopLevelEntries.add(filename);
                    }
                }
                Collections.sort(mTopLevelEntries);
            } catch (IOException e) {
                Log.w(TAG, e);
            } finally {
                runOnUiThread(() -> {
                    mAdapter.updateData(mTopLevelEntries);
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                });
                IOUtil.closeQuietly(zipFile);
            }
        });
    }

    private List<String> getNextLevelEntries(String parentName) {
        if (TextUtils.equals("/", parentName)) {
            return mTopLevelEntries;
        }
        //entry最前面没有/
        if (parentName.startsWith("/")) {
            parentName = parentName.substring(1);
        }

        List<String> entries = new ArrayList<>();
        for (String entryName : mAllEntries) {
            if (entryName.startsWith(parentName) && !TextUtils.equals(entryName, parentName)) {
                String left = entryName.substring(parentName.length());
                if (!left.contains("/") || left.indexOf("/") == left.length() - 1) {
                    entries.add(entryName);
                }
            }
        }
        return entries;
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.equals("/", mCurrentDirs)) {
            super.onBackPressed();
        } else {
            mCurrentDirs = mCurrentDirs.replaceFirst("(.*/).*", "$1");
            setCurrentDir(mCurrentDirs);
            List<String> data = getNextLevelEntries(mCurrentDirs);
            mAdapter.updateData(data);
        }
    }

    private static List<String> getEntryParents(String entryName) {
        List<String> list = new ArrayList<>();
        char[] cs = entryName.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == 47) {
                list.add(entryName.substring(0, i + 1));
            }
        }
        return list;
    }

    private static class FileAdapter extends BaseRecyclerAdapter<String> {

        private FilesInAppExplorerActivity mActivity;

        FileAdapter(FilesInAppExplorerActivity activity, List<String> data) {
            super(data);
            mActivity = activity;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            String filepath = getData().get(position);
            View arrowView = holder.getView(R.id.arrow);
            TextView nameView = holder.getView(R.id.name);
            File file = new File(filepath);
            nameView.setText(file.getName());
            if (filepath.endsWith("/")) {
                holder.itemView.setOnClickListener(v -> {
                    mActivity.setCurrentDir(filepath);
                    List<String> data = mActivity.getNextLevelEntries(filepath);
                    updateData(data);
                });
                arrowView.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setOnClickListener(null);
                arrowView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_app_file;
        }

        @Override
        public void updateData(@NonNull List<String> data) {
            Collections.sort(data);
            super.updateData(data);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
