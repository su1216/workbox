package com.su.workbox.ui.mock;

import android.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.CancelableObserver;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MockUrlListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = MockUrlListActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private RecordAdapter mAdapter;
    private String mHost;
    private String mTitle;
    private String mQueryText = "";
    private final SearchableHelper mSearchableHelper = new SearchableHelper();
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
        mModel = new ViewModelProvider(this, factory).get(RequestResponseModel.class);
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

    public void processImport(@NonNull MenuItem item) {
        MockUtil.startCollection(this, SimpleBlockedDialogFragment.newInstance());
    }

    public void delete(@NonNull MenuItem item) {
        editMode(mToolbar.getMenu(), true);
        mAdapter.notifyDataSetChanged();
    }

    public void confirm(@NonNull MenuItem item) {
        List<RequestResponseRecord> data = mAdapter.getData();
        int size = data.size();
        List<Pair<Integer, Long>> deleteList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            RequestResponseRecord record = data.get(i);
            if (record.isChecked()) {
                deleteList.add(new Pair<>(i, record.getId()));
            }
        }
        editMode(mToolbar.getMenu(), false);
        if (deleteList.isEmpty()) {
            mAdapter.notifyDataSetChanged();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("确认删除已选中的" + deleteList.size() + "条数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> delete(data, deleteList))
                .setNegativeButton(R.string.workbox_cancel, (dialog, which) ->  {
                    for (int i = 0; i < size; i++) {
                        RequestResponseRecord record = data.get(i);
                        record.setChecked(false);
                    }
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private void delete(List<RequestResponseRecord> data, List<Pair<Integer, Long>> deleteList) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "删除中...");
        AppExecutors.getInstance().diskIO().execute(() -> {
            int deleteSize = deleteList.size();
            for (int i = deleteSize - 1; i >= 0; i--) {
                Pair<Integer, Long> pair = deleteList.get(i);
                MockUtil.deleteById(this, pair.second);
                data.remove((int) pair.first);
                runOnUiThread(() -> mAdapter.notifyItemRemoved(pair.first));
            }
            DIALOG_FRAGMENT.dismissAllowingStateLoss();
            runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        });
    }

    private void editMode(Menu menu, boolean edit) {
        mAdapter.mEdit = edit;
        menu.findItem(R.id.process_import).setVisible(!edit);
        menu.findItem(R.id.search).setVisible(!edit);
        menu.findItem(R.id.delete).setVisible(!edit);
        menu.findItem(R.id.confirm).setVisible(edit);
        menu.findItem(R.id.search).collapseActionView();
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_mock_url_list_menu;
    }

    private class RecordAdapter extends BaseRecyclerAdapter<RequestResponseRecord> {

        private Context mContext;
        private Resources mResources;
        boolean mEdit;

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
            TextView pathView = holder.getView(R.id.path);
            View pathLayout = holder.getView(R.id.path_layout);
            TextView queryView = holder.getView(R.id.query);
            TextView methodView = holder.getView(R.id.method);
            TextView contentTypeTitleView = holder.getView(R.id.content_type_title);
            TextView contentTypeView = holder.getView(R.id.content_type);
            TextView typeView = holder.getView(R.id.type);
            TextView mockableView = holder.getView(R.id.mockable);
            TextView descView = holder.getView(R.id.desc);
            View descLayout = holder.getView(R.id.desc_layout);
            AppCompatCheckBox checkBox = holder.getView(R.id.check_box);

            RequestResponseRecord record = getData().get(position);
            String url = record.getUrl();
            String host = record.getHost();
            String method = record.getMethod();
            String contentType = record.getContentType();
            String description = record.getDescription();
            boolean auto = record.isAuto();
            boolean mockable = record.isMockable();
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

            schemeView.setText(scheme);
            if (TextUtils.equals("http", scheme)) {
                schemeView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
            } else {
                schemeView.setTextColor(mResources.getColor(R.color.workbox_second_text));
            }
            hostView.setText(host);
            typeView.setText(auto ? "auto" : "manual");
            if (mockable) {
                mockableView.setTextColor(mResources.getColor(R.color.workbox_second_text));
            } else {
                mockableView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
            }
            mockableView.setText(String.valueOf(mockable));

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

            if (mEdit) {
                checkBox.setChecked(record.isChecked());
                checkBox.setOnClickListener(v -> record.setChecked(checkBox.isChecked()));
                holder.itemView.setOnClickListener(null);
            } else {
                checkBox.setChecked(inUse);
                checkBox.setOnClickListener(v -> {
                    RequestResponseRecord requestResponseRecord = record.clone();
                    requestResponseRecord.setInUse(checkBox.isChecked());
                    AppExecutors.getInstance().diskIO().execute(() -> mModel.updateRequestResponseRecord(requestResponseRecord));
                });
                holder.itemView.setOnClickListener(v -> onItemClick(position));
            }
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
