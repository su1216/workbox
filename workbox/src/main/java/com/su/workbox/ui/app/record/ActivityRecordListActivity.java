package com.su.workbox.ui.app.record;

import android.app.AlertDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

public class ActivityRecordListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = ActivityRecordListActivity.class.getSimpleName();
    private RecordAdapter mAdapter;
    private ActivityRecordModel mModel;
    private SearchableHelper mSearchableHelper = new SearchableHelper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mModel = ViewModelProviders.of(this).get(ActivityRecordModel.class);
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
        setTitle("Activity历史记录");
        mSearchableHelper.initSearchToolbar(mToolbar, "请输入taskId", this);
    }

    private void query(String taskId) {
        mSearchableHelper.clear();
        MutableLiveData<List<ActivityRecord>> recordListData = mModel.getRecordList(taskId.trim());
        recordListData.observe(this, activityRecords -> {
            if (activityRecords == null || activityRecords.isEmpty()) {
                mAdapter.refresh(Collections.EMPTY_LIST);
            } else {
                mAdapter.refresh(activityRecords);
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
        mModel.deleteAllActivityRecords();
    }

    private class RecordAdapter extends BaseRecyclerAdapter<ActivityRecord> implements RecyclerItemClickListener.OnItemClickListener {

        private RecordAdapter(List<ActivityRecord> data) {
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
            ActivityRecord record = getData().get(position);
            TextView simpleNameView = holder.getView(R.id.simple_name);
            TextView eventView = holder.getView(R.id.event);
            TextView timeView = holder.getView(R.id.time);
            TextView taskIdView = holder.getView(R.id.task_id);
            simpleNameView.setText(record.getSimpleName());
            eventView.setText(record.getEvent());
            timeView.setText(ThreadUtil.getSimpleDateFormat("MM-dd HH:mm:ss SSS").format(new Date(record.getCreateTime())));
            taskIdView.setText("taskId: " + record.getTaskId());
        }

        @Override
        public void onItemClick(View view, int position) {
            ActivityRecord record = getData().get(position);
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
