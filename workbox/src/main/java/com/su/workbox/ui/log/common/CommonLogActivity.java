package com.su.workbox.ui.log.common;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonLogActivity extends BaseAppCompatActivity implements LogManager.OnLogChangedListener, View.OnClickListener, SearchView.OnQueryTextListener, BaseAppCompatActivity.OnTitleDoubleClickListener {

    private static final String TAG = CommonLogActivity.class.getSimpleName();
    private SearchableHelper mSearchableHelper = new SearchableHelper(LogRecord.class);
    private LogManager mLogManager;
    private LogAdapter mAdapter;
    private List<LogRecord> mLogList = new ArrayList<>();
    private Set<String> mTagSet = new HashSet<>();
    private RadioGroup mLevelGroup;
    private String mQuery = "";
    private List<String> mTags = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private EditText mTagsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_common_log);
        mAdapter = new LogAdapter(mLogList);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(mAdapter);
        mTagsView = findViewById(R.id.tags);
        findViewById(R.id.select_tags).setOnClickListener(this);
        findViewById(R.id.query).setOnClickListener(this);
        findViewById(R.id.v).setOnClickListener(this);
        findViewById(R.id.d).setOnClickListener(this);
        findViewById(R.id.i).setOnClickListener(this);
        findViewById(R.id.w).setOnClickListener(this);
        findViewById(R.id.e).setOnClickListener(this);
        findViewById(R.id.f).setOnClickListener(this);
        mLevelGroup = findViewById(R.id.level_group);

        mLogManager = LogManager.getInstance();
        mLogManager.start(null, "v", mQuery, false, false, this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("常规日志");
        installOnTitleDoubleClickListener(this);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
    }

    @Override
    public void onTitleDoubleClick(View view) {
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogManager.stop();
        mLogList.clear();
        mTagSet.clear();
    }

    @Override
    public void onAdded(@NonNull LogRecord logRecord) {
        mLogList.add(0, logRecord);
        if (!TextUtils.isEmpty(logRecord.getTag())) {
            mTagSet.add(logRecord.getTag());
        }
        mAdapter.notifyItemInserted(0);
    }

    @Override
    public void onClear() {
        mLogList.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mQuery = s;
        query(false);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (TextUtils.isEmpty(s)) {
            mQuery = s;
            query(false);
        }
        return false;
    }

    public void showTagsDialog() {
        String[] empty = new String[0];
        final String[] items = mTagSet.toArray(empty);
        int length = items.length;
        final boolean[] checkedItems = new boolean[length];
        Arrays.fill(checkedItems, false);
        if (mTags != null && mTags.size() > 0) {
            for (String tag : mTags) {
                for (int i = 0; i < length; i++) {
                    if (TextUtils.equals(items[i], tag)) {
                        checkedItems[i] = true;
                        break;
                    }
                }
            }
        }

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(items, checkedItems, null)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    mTags.clear();
                    SparseBooleanArray array = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                    int size = array.size();
                    if (size == 0) {
                        mTagsView.setText("");
                        return;
                    }

                    int checkedCount = 0;
                    StringBuilder inputBuilder = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        int key = array.keyAt(i);
                        boolean checked = array.get(key);
                        if (checked) {
                            mTags.add(items[key]);
                            inputBuilder.append(items[key]);
                            inputBuilder.append(", ");
                            checkedCount++;
                        }
                    }
                    if (checkedCount > 0) {
                        inputBuilder.delete(inputBuilder.length() - 2, inputBuilder.length());
                    }
                    mTagsView.setText(inputBuilder);
                    query(false);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .create()
                .show();
    }

    private void query(boolean clear) {
        AppHelper.hideSoftInputFromWindow(getWindow());
        mSearchableHelper.clear();
        collectTags();
        String level = getLevelById(mLevelGroup.getCheckedRadioButtonId());
        String[] empty = new String[0];
        mLogManager.restart(mTags.toArray(empty), level, mQuery, false, clear, this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.v
                || id == R.id.d
                || id == R.id.i
                || id == R.id.w
                || id == R.id.e
                || id == R.id.f) {
            query(false);
        } else if (id == R.id.select_tags) {
            showTagsDialog();
        } else if (id == R.id.query) {
            query(false);
        }
    }

    private void collectTags() {
        String content = mTagsView.getText().toString();
        String[] tags = content.split(",\\s*");
        mTags.clear();
        for (String tag : tags) {
            String trim = tag.trim();
            if (!TextUtils.isEmpty(trim)) {
                mTags.add(trim);
            }
        }
    }

    private String getLevelById(int id) {
        String level;
        if (id == R.id.v) {
            level = "v";
        } else if (id == R.id.d) {
            level = "d";
        } else if (id == R.id.i) {
            level = "i";
        } else if (id == R.id.w) {
            level = "w";
        } else if (id == R.id.e) {
            level = "e";
        } else if (id == R.id.f) {
            level = "f";
        } else {
            level = "v";
        }
        return level;
    }

    private static class LogAdapter extends BaseRecyclerAdapter<LogRecord> implements RecyclerItemClickListener.OnItemClickListener {

        private LogAdapter(List<LogRecord> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_log;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            LogRecord logRecord = getData().get(position);
            TextView logView = holder.getView(R.id.log);
            logView.setText(logRecord.getFull());
            logView.setTextColor(getLogColor(logRecord));
        }

        @Override
        public void onItemClick(View view, int position) {
            LogRecord logRecord = getData().get(position);
            AppHelper.copyToClipboard(GeneralInfoHelper.getContext(), "log", logRecord.getFull());
            new ToastBuilder("log已复制到粘贴板").show();
        }

        private int getLogColor(LogRecord logRecord) {
            Resources resources = GeneralInfoHelper.getContext().getResources();
            String level = logRecord.getLevel();
            int color;
            if ("v".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_v);
            } else if ("d".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_d);
            } else if ("i".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_i);
            } else if ("w".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_w);
            } else if ("e".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_e);
            } else if ("f".equalsIgnoreCase(level)) {
                color = resources.getColor(R.color.workbox_logcat_f);
            } else {
                color = resources.getColor(R.color.workbox_logcat_v);
            }
            return color;
        }
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除所有数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> delete())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void delete() {
        mTagSet.clear();
        query(true);
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_mock_detail_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
