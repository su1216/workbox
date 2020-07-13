package com.su.workbox.ui.usage;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.CancelableObserver;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UrlGroupActivity extends BaseAppCompatActivity {

    public static final String TAG = UrlGroupActivity.class.getSimpleName();
    private RecordAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mAdapter = new RecordAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));

        DataUsageGroupModel groupModel = ViewModelProviders.of(this).get(DataUsageGroupModel.class);
        MutableLiveData<DataUsageRecord.Summary> summaryData = groupModel.getSummary();
        summaryData.observe(this, new CancelableObserver<DataUsageRecord.Summary>() {
            @Override
            public void onChanged(@Nullable DataUsageRecord.Summary summary) {
                if (isCancel()) {
                    return;
                }
                if (summary == null) {
                    mAdapter.updateSummary(new DataUsageRecord.Summary());
                } else {
                    mAdapter.updateSummary(summary);
                }
            }
        });

        MutableLiveData<List<DataUsageRecord.Group>> groupList = groupModel.getGroupList();
        groupList.observe(this, new CancelableObserver<List<DataUsageRecord.Group>>() {
            @Override
            public void onChanged(@Nullable List<DataUsageRecord.Group> summary) {
                if (isCancel()) {
                    return;
                }
                if (summary == null) {
                    mAdapter.update(Collections.EMPTY_LIST);
                } else {
                    mAdapter.update(summary);
                }
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("流量监控汇总");
    }

    private static class RecordAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> implements RecyclerItemClickListener.OnItemClickListener {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_NORMAL = 1;
        private DataUsageRecord.Summary mSummary = new DataUsageRecord.Summary();
        private List<DataUsageRecord.Group> mList = new ArrayList<>();
        private Context mContext;

        RecordAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            if (viewType == TYPE_HEADER) {
                view = inflater.inflate(R.layout.workbox_header_data_usage_record, parent, false);
            } else {
                view = inflater.inflate(R.layout.workbox_item_url_group_usage_record, parent, false);
            }
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder baseViewHolder, int position) {
            int viewType = getItemViewType(position);
            if (viewType == TYPE_HEADER) {
                ((TextView) baseViewHolder.getView(R.id.count)).setText(String.valueOf(mSummary.getCount()));
                ((TextView) baseViewHolder.getView(R.id.total_request)).setText(AppHelper.formatSize(mSummary.getTotalRequestLength()));
                ((TextView) baseViewHolder.getView(R.id.total_response)).setText(AppHelper.formatSize(mSummary.getTotalResponseLength()));
            } else {
                DataUsageRecord.Group groupRecord = mList.get(position - 1);
                TextView urlView = baseViewHolder.getView(R.id.url);
                urlView.setText(groupRecord.getUrl());
                ((TextView) baseViewHolder.getView(R.id.request_length)).setText(AppHelper.formatSize(groupRecord.getGroupRequestLength()));
                ((TextView) baseViewHolder.getView(R.id.response_length)).setText(AppHelper.formatSize(groupRecord.getGroupResponseLength()));
                ((TextView) baseViewHolder.getView(R.id.count)).setText(String.valueOf(groupRecord.getTotal()));
            }
        }

        private void update(List<DataUsageRecord.Group> list) {
            mList = list;
            notifyDataSetChanged();
        }

        private void updateSummary(DataUsageRecord.Summary summary) {
            mSummary = summary;
            notifyItemChanged(0);
        }

        @Override
        public void onItemClick(View view, int position) {
            if (position == 0) {
                return;
            }
            DataUsageRecord.Group groupRecord = mList.get(position - 1);
            Intent intent = new Intent(mContext, RecordListActivity.class);
            intent.putExtra("query", groupRecord.getUrl());
            intent.putExtra("searchable", false);
            mContext.startActivity(intent);
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
    protected String getTag() {
        return TAG;
    }
}
