package com.su.workbox.ui.mock;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.database.table.RequestResponseRecord;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.CancelableObserver;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;
import com.su.workbox.widget.recycler.RecyclerItemLongClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockGroupHostActivity extends BaseAppCompatActivity {

    public static final String TAG = MockGroupHostActivity.class.getSimpleName();
    private String mHostListTitle;
    private HostAdapter mAdapter;
    private RequestResponseModel mModel;
    private CancelableObserver<List<RequestResponseRecord.Summary>> mSummaryObserver;

    public static void startActivity(@NonNull Context context, @Nullable CharSequence title) {
        Intent intent = new Intent(context, MockGroupHostActivity.class);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHostListTitle = intent.getStringExtra("title");
        setContentView(R.layout.workbox_template_recycler_list);
        RequestResponseModel.Factory factory = new RequestResponseModel.Factory(getApplication(), "");
        mModel = ViewModelProviders.of(this, factory).get(RequestResponseModel.class);
        mAdapter = new HostAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));
        recyclerView.addOnItemTouchListener(new RecyclerItemLongClickListener(this, mAdapter));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Mock数据分组");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MutableLiveData<List<RequestResponseRecord.Summary>> recordListData = mModel.getSummaryList();
        if (mSummaryObserver != null) {
            mSummaryObserver.setCancel(true);
            recordListData.removeObserver(mSummaryObserver);
        }
        mSummaryObserver = new CancelableObserver<List<RequestResponseRecord.Summary>>() {
            @Override
            public void onChanged(@Nullable List<RequestResponseRecord.Summary> summaryList) {
                if (isCancel()) {
                    return;
                }
                if (summaryList == null || summaryList.isEmpty()) {
                    mAdapter.updateData(Collections.EMPTY_LIST);
                } else if (summaryList.size() == 1) {
                    RequestResponseRecord.Summary summary = summaryList.get(0);
                    startRecordsActivity(summary.getHost(), summary.getHost());
                    finish();
                } else {
                    mAdapter.updateData(summaryList);
                }
            }
        };
        recordListData.observe(this, mSummaryObserver);
    }

    private void startRecordsActivity(@NonNull String host, String title) {
        Intent newFakeIntent = new Intent(this, MockUrlListActivity.class);
        newFakeIntent.putExtra("title", title);
        newFakeIntent.putExtra("host", host);
        startActivity(newFakeIntent);
    }

    private class HostAdapter extends BaseRecyclerAdapter<RequestResponseRecord.Summary> implements RecyclerItemClickListener.OnItemClickListener, RecyclerItemLongClickListener.OnItemLongClickListener {

        private Context mContext;

        HostAdapter(Context context) {
            super(new ArrayList<>());
            mContext = context;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_mock_group_host;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            RequestResponseRecord.Summary summary = getData().get(position);
            TextView hostView = holder.getView(R.id.host);
            TextView countView = holder.getView(R.id.count);
            hostView.setText(summary.getHost());
            countView.setText(String.valueOf(summary.getCount()));
        }

        @Override
        public void onItemClick(View view, int position) {
            RequestResponseRecord.Summary summary = getData().get(position);
            startRecordsActivity(summary.getHost(), mHostListTitle);
        }

        @Override
        public void onItemLongClick(View view, int position) {
            RequestResponseRecord.Summary summary = getData().get(position);
            final String host = summary.getHost();
            new AlertDialog.Builder(mContext)
                    .setMessage("确定要将" + host + "下的所有数据删除吗？")
                    .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                        deleteByHost(host);
                    })
                    .setNegativeButton(R.string.workbox_cancel, null)
                    .show();
        }

        private void deleteByHost(String host) {
            mModel.deleteByHost(host);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
