package com.su.workbox.ui.usage;

import android.app.AlertDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.CancelableObserver;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by su on 19-4-7
 * 流量监控记录
 * */
public class RecordListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = RecordListActivity.class.getSimpleName();
    private SearchableHelper mSearchableHelper = new SearchableHelper(DataUsageRecord.class);
    private RecordAdapter mAdapter;
    private DataUsageModel mModel;
    private DataUsageListModel mListModel;
    private CancelableObserver<List<DataUsageRecord>> mRecordObserver;
    private CancelableObserver<DataUsageRecord.Summary> mSummaryObserver;
    private boolean mSearchable = true;
    private String mQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        mSearchable = intent.getBooleanExtra("searchable", true);
        mQuery = intent.getStringExtra("query");
        if (mQuery == null) {
            mQuery = "";
        }

        mAdapter = new RecordAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));

        DataUsageModel.Factory factory = new DataUsageModel.Factory(getApplication());
        mModel = ViewModelProviders.of(this, factory).get(DataUsageModel.class);
        mListModel = ViewModelProviders.of(this).get(DataUsageListModel.class);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("流量监控记录");
        if (mSearchable) {
            mSearchableHelper.initSearchToolbar(mToolbar, this);
        }
        query(mQuery);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        query(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        query(s);
        return false;
    }

    private void query(String query) {
        mQuery = query;
        mSearchableHelper.clear();

        MutableLiveData<List<DataUsageRecord>> recordListData = mListModel.getRecordList(query);
        if (mRecordObserver != null) {
            mRecordObserver.setCancel(true);
            recordListData.removeObserver(mRecordObserver);
        }
        mRecordObserver = new CancelableObserver<List<DataUsageRecord>>() {
            @Override
            public void onChanged(@Nullable List<DataUsageRecord> dataUsageRecords) {
                if (isCancel()) {
                    return;
                }
                if (dataUsageRecords == null || dataUsageRecords.isEmpty()) {
                    mAdapter.update(Collections.EMPTY_LIST);
                } else {
                    for (DataUsageRecord search : dataUsageRecords) {
                        mSearchableHelper.find(query, search);
                    }
                    mAdapter.update(dataUsageRecords);
                }
            }
        };
        recordListData.observe(this, mRecordObserver);

        MutableLiveData<DataUsageRecord.Summary> summaryData = mListModel.getSummary(query);
        if (mSummaryObserver != null) {
            mSummaryObserver.setCancel(true);
            summaryData.removeObserver(mSummaryObserver);
        }
        mSummaryObserver = new CancelableObserver<DataUsageRecord.Summary>() {
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
        };
        summaryData.observe(this, mSummaryObserver);
    }

    @Override
    public int menuRes() {
        if (mSearchable) {
            return R.menu.workbox_data_usage_menu;
        }
        return super.menuRes();
    }

    public void gather(@NonNull MenuItem item) {
        startActivity(new Intent(this, UrlGroupActivity.class));
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除所有数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> deleteAll())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void deleteAll() {
        mModel.deleteAll(mQuery);
    }

    private class RecordAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> implements RecyclerItemClickListener.OnItemClickListener {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_NORMAL = 1;
        private DataUsageRecord.Summary mSummary = new DataUsageRecord.Summary();
        private List<DataUsageRecord> mList = new ArrayList<>();
        private Context mContext;
        private Resources mResources;

        RecordAdapter(Context context) {
            mContext = context;
            mResources = mContext.getResources();
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            if (viewType == TYPE_HEADER) {
                view = inflater.inflate(R.layout.workbox_header_data_usage_record, parent, false);
            } else {
                view = inflater.inflate(R.layout.workbox_item_usage_record, parent, false);
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
                DataUsageRecord record = mList.get(position - 1);
                TextView urlView = baseViewHolder.getView(R.id.url);
                urlView.setText(record.getUrl());
                ((TextView) baseViewHolder.getView(R.id.method)).setText(record.getMethod());
                setCodeView(baseViewHolder.getView(R.id.code), record.getCode());
                ((TextView) baseViewHolder.getView(R.id.binary)).setText((record.isRequestBinary() || record.isResponseBinary()) ? "binary" : "text");
                ((TextView) baseViewHolder.getView(R.id.duration)).setText(record.getDuration() + "ms");
                SimpleDateFormat simpleDateFormat = ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                ((TextView) baseViewHolder.getView(R.id.response_time)).setText(simpleDateFormat.format(new Date(record.getResponseTime())));
                ((TextView) baseViewHolder.getView(R.id.request_length)).setText(AppHelper.formatSize(record.getRequestLength()));
                ((TextView) baseViewHolder.getView(R.id.response_length)).setText(AppHelper.formatSize(record.getResponseLength()));
                if (mSearchable) {
                    mSearchableHelper.refreshFilterColor(urlView, position - 1, "url");
                }
            }
        }

        private void update(List<DataUsageRecord> list) {
            mList = list;
            notifyDataSetChanged();
        }

        private void updateSummary(DataUsageRecord.Summary summary) {
            mSummary = summary;
            notifyItemChanged(0);
        }

        private void setCodeView(TextView codeView, int code) {
            if (code >= 200 && code < 300) {
                codeView.setTextColor(mResources.getColor(R.color.workbox_first_text));
            } else {
                codeView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
            }
            if (code > 0) {
                codeView.setText("[" + code + "]");
            } else {
                codeView.setText("[连接失败]");
            }
        }

        @Override
        public void onItemClick(View view, int position) {
            if (position == 0) {
                return;
            }
            DataUsageRecord record = mList.get(position - 1);
            Intent intent = new Intent(mContext, RecordDetailActivity.class);
            intent.putExtra("record", record);
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
