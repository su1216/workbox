package com.su.workbox.ui.app.record;

import android.app.AlertDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LifecycleRecordListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = LifecycleRecordListActivity.class.getSimpleName();
    private RecordAdapter mAdapter;
    private LifecycleRecordModel mModel;
    private SearchableHelper mSearchableHelper = new SearchableHelper();

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, LifecycleRecordListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mModel = ViewModelProviders.of(this).get(LifecycleRecordModel.class);
        mAdapter = new RecordAdapter(new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));
        query("");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("生命周期历史记录");
        mSearchableHelper.initSearchToolbar(mToolbar, "请输入类名", this);
    }

    private void query(String taskId) {
        mSearchableHelper.clear();
        MutableLiveData<List<LifecycleRecord>> recordListData = mModel.getRecordList(taskId.trim());
        recordListData.observe(this, records -> {
            if (records == null || records.isEmpty()) {
                mAdapter.refresh(Collections.EMPTY_LIST);
            } else {
                mAdapter.refresh(records);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        query(s);
        return false;
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除所有数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> deleteAll())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void deleteAll() {
        mModel.deleteAllHistoryRecords();
    }

    private class RecordAdapter extends BaseRecyclerAdapter<LifecycleRecord> implements RecyclerItemClickListener.OnItemClickListener {

        private RecordAdapter(List<LifecycleRecord> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_activity_record;
        }

        @Override
        public int getItemType(int position) {
            return ITEM_TYPE_NORMAL;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            LifecycleRecord record = getData().get(position);
            TextView simpleNameView = holder.getView(R.id.simple_name);
            TextView eventView = holder.getView(R.id.event);
            TextView timeView = holder.getView(R.id.time);
            TextView taskIdView = holder.getView(R.id.task_id);
            simpleNameView.setText(record.getSimpleName());
            eventView.setText(record.getEvent());
            timeView.setText(ThreadUtil.getSimpleDateFormat("MM-dd HH:mm:ss SSS").format(new Date(record.getCreateTime())));
            taskIdView.setText("taskId: " + record.getTaskId());
            TextView parentView = holder.getView(R.id.parent);
            TextView tagView = holder.getView(R.id.tag);
            String parentFragment = record.getParentFragment();
            String tag = record.getFragmentTag();
            if (TextUtils.isEmpty(parentFragment)) {
                parentView.setVisibility(View.GONE);
            } else {
                parentView.setText(parentFragment);
                parentView.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(tag)) {
                tagView.setVisibility(View.GONE);
            } else {
                tagView.setText(tag);
                tagView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onItemClick(View view, int position) {
            LifecycleRecord record = getData().get(position);
            new ToastBuilder(record.getName()).show();
        }
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
