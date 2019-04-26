package com.su.workbox.ui.mock;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.CancelableObserver;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MockUrlListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = MockUrlListActivity.class.getSimpleName();
    private RecordAdapter mAdapter;
    private String mHost;
    private String mTitle;
    private String mQueryText = "";
    private SearchableHelper mSearchableHelper = new SearchableHelper();
    private RequestResponseModel mModel;
    private CancelableObserver<List<RequestResponseRecord>> mRequestResponseObserver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        mHost = intent.getStringExtra("host");
        RequestResponseModel.Factory factory = new RequestResponseModel.Factory(getApplication(), mHost);
        mModel = ViewModelProviders.of(this, factory).get(RequestResponseModel.class);
        mAdapter = new RecordAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        query(mQueryText);
    }

    private void query(String query) {
        mSearchableHelper.clear();
        MutableLiveData<List<RequestResponseRecord>> recordListData = mModel.getRecordList(query);
        if (mRequestResponseObserver != null) {
            mRequestResponseObserver.setCancel(true);
            recordListData.removeObserver(mRequestResponseObserver);
        }
        mRequestResponseObserver = new CancelableObserver<List<RequestResponseRecord>>() {
            @Override
            public void onChanged(@Nullable List<RequestResponseRecord> records) {
                if (isCancel()) {
                    return;
                }
                if (records == null || records.isEmpty()) {
                    mAdapter.updateData(Collections.EMPTY_LIST);
                } else {
                    for (RequestResponseRecord search : records) {
                        mSearchableHelper.find(query, search);
                    }
                    mAdapter.updateData(records);
                }
            }
        };
        recordListData.observe(this, mRequestResponseObserver);
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_search_menu;
    }

    private class RecordAdapter extends BaseRecyclerAdapter<RequestResponseRecord> {

        private Context mContext;
        private Resources mResources;

        RecordAdapter(Context context) {
            super(new ArrayList<>());
            mContext = context;
            mResources = context.getResources();
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_mock_url;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            View queryLayout = holder.getView(R.id.query_layout);
            TextView schemeView = holder.getView(R.id.scheme);
            TextView hostView = holder.getView(R.id.host);
            TextView typeView = holder.getView(R.id.type);
            TextView pathView = holder.getView(R.id.path);
            View pathLayout = holder.getView(R.id.path_layout);
            TextView queryView = holder.getView(R.id.query);
            TextView requestBodyView = holder.getView(R.id.request_body);
            View requestBodyLayout = holder.getView(R.id.request_body_layout);
            TextView methodView = holder.getView(R.id.method);
            TextView contentTypeTitleView = holder.getView(R.id.content_type_title);
            TextView contentTypeView = holder.getView(R.id.content_type);
            TextView descView = holder.getView(R.id.desc);
            View descLayout = holder.getView(R.id.desc_layout);
            AppCompatCheckBox checkBox = holder.getView(R.id.check_box);

            RequestResponseRecord record = getData().get(position);
            String url = record.getUrl();
            String host = record.getHost();
            String method = record.getMethod();
            String contentType = record.getContentType();
            String parameters = record.getRequestBody();
            String description = record.getDescription();
            boolean auto = record.isAuto();
            boolean inUse = record.isInUse();

            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            Set<String> set = uri.getQueryParameterNames();
            String queryContent = MockUtil.makeQueryContent(uri, " ");
            if (set.isEmpty()) {
                queryLayout.setVisibility(View.GONE);
            } else {
                queryView.setText(queryContent);
                queryLayout.setVisibility(View.VISIBLE);
            }

            String requestBodyContent = MockUtil.makeRequestBodyContent(parameters, " ");
            if (TextUtils.isEmpty(parameters)) {
                requestBodyLayout.setVisibility(View.GONE);
            } else {
                requestBodyView.setText(requestBodyContent);
                requestBodyLayout.setVisibility(View.VISIBLE);
            }
            schemeView.setText(scheme);
            if (TextUtils.equals("http", scheme)) {
                schemeView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
            } else {
                schemeView.setTextColor(mResources.getColor(R.color.workbox_second_text));
            }
            hostView.setText(host);
            typeView.setText(auto ? "auto" : "manual");
            if (TextUtils.isEmpty(uri.getPath())) {
                pathLayout.setVisibility(View.GONE);
            } else {
                pathView.setText(uri.getPath());
                pathLayout.setVisibility(View.VISIBLE);
            }
            methodView.setText(method);
            if (TextUtils.equals(method, "GET") || TextUtils.isEmpty(contentType)) {
                contentTypeTitleView.setVisibility(View.GONE);
                contentTypeView.setVisibility(View.GONE);
            } else {
                contentTypeTitleView.setVisibility(View.VISIBLE);
                contentTypeView.setVisibility(View.VISIBLE);
                contentTypeView.setText(contentType);
            }
            if (TextUtils.isEmpty(description)) {
                descLayout.setVisibility(View.GONE);
            } else {
                descView.setText(description);
                descLayout.setVisibility(View.VISIBLE);
            }

            checkBox.setChecked(inUse);
            checkBox.setOnClickListener(v -> {
                RequestResponseRecord requestResponseRecord = record.clone();
                requestResponseRecord.setInUse(checkBox.isChecked());
                AppExecutors.getInstance().diskIO().execute(() -> mModel.updateRequestResponseRecord(requestResponseRecord));
            });
            holder.itemView.setOnClickListener(v -> onItemClick(position));
        }

        private void onItemClick(int position) {
            RequestResponseRecord record = getData().get(position);
            Intent intent = new Intent(mContext, MockDetailActivity.class);
            intent.putExtra(MockDetailActivity.KEY_ENTITY, record);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mQueryText = s;
        query(mQueryText);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mQueryText = s;
        query(mQueryText);
        return false;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
