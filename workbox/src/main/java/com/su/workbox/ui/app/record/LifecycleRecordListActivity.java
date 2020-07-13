package com.su.workbox.ui.app.record;

import android.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
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

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, LifecycleRecordListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mModel = ViewModelProviders.of(this).get(LifecycleRecordModel.class);
        mAdapter = new RecordAdapter();
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
                mAdapter.update(Collections.EMPTY_LIST);
            } else {
                mAdapter.update(records);
            }
        });

        MutableLiveData<List<LifecycleRecord.Summary>> countData = mModel.getRecordCount(taskId.trim());
        countData.observe(this, count -> {
            if (count == null) {
                mAdapter.updateCount(Collections.EMPTY_LIST);
            } else {
                mAdapter.updateCount(count);
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

    private class RecordAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> implements RecyclerItemClickListener.OnItemClickListener {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_NORMAL = 1;
        private List<LifecycleRecord.Summary> mSummaries = new ArrayList<>();
        private List<LifecycleRecord> mList = new ArrayList<>();

        @Override
        public void onItemClick(View view, int position) {
            if (position == 0) {
                return;
            }
            LifecycleRecord record = mList.get(position - 1);
            new ToastBuilder(record.getName()).show();
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            if (viewType == TYPE_HEADER) {
                view = inflater.inflate(R.layout.workbox_header_lifecycle_record, parent, false);
            } else {
                view = inflater.inflate(R.layout.workbox_item_activity_record, parent, false);
            }
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            if (viewType == TYPE_HEADER) {
                TextView countView = holder.getView(R.id.count);
                if (mSummaries.isEmpty()) {
                    countView.setText("无记录");
                } else {
                    int total = 0;
                    StringBuilder sb = new StringBuilder();
                    for (LifecycleRecord.Summary summary : mSummaries) {
                        total += summary.getTotal();
                        sb.append(LifecycleRecord.getTypeString(summary.getType()));
                        sb.append(": ");
                        sb.append(summary.getTotal());
                        sb.append("    ");
                    }
                    if (mSummaries.size() > 1) {
                        sb.insert(0, "    ");
                        sb.insert(0, total);
                        sb.insert(0, "total: ");
                    }
                    sb.delete(sb.length() - 4, sb.length());
                    countView.setText(sb);
                }
            } else {
                LifecycleRecord record = mList.get(position - 1);
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
        }

        private void updateCount(List<LifecycleRecord.Summary> summaries) {
            mSummaries = summaries;
            notifyItemChanged(0);
        }

        private void update(List<LifecycleRecord> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            return TYPE_NORMAL;
        }

        @Override
        public int getItemCount() {
            return mList.size() + 1;
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
