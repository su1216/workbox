package com.su.workbox.ui.data;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.database.DbInfoProvider;
import com.su.workbox.entity.database.TableColumn;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class TableInfoActivity extends BaseAppCompatActivity {
    public static final String TAG = TableInfoActivity.class.getSimpleName();
    private String mDatabaseName;
    private String mTableName;
    private String mTableSql;
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        mTableSql = intent.getStringExtra("sql");
        mDatabaseName = intent.getStringExtra("database_name");
        mTableName = intent.getStringExtra("table_name");
        mRecyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        mRecyclerView.addItemDecoration(decoration);
        mAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTableName + "结构");
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetMenu();
        View header = makeHeaderView();
        mAdapter.setHeaderView(header);
        loadData();
    }

    private View makeHeaderView() {
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.workbox_header_table, mRecyclerView, false);
        TextView sqlView = header.findViewById(R.id.sql);
        sqlView.setText(mTableSql);
        header.setOnClickListener(v -> {
            AppHelper.copyToClipboard(this, "sql", mTableSql);
            new ToastBuilder("已将" + mTableName + "创建语句复制到粘贴板中").setDuration(Toast.LENGTH_LONG).show();
        });
        return header;
    }

    private void loadData() {
        List<TableColumn> columns = new ArrayList<>();
        DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, mDatabaseName);
        Cursor cursor = dbInfoProvider.getTableInfo(mTableName);
        if (cursor == null || cursor.getCount() == 0) {
            mAdapter.updateData(columns);
            return;
        }
        cursor.moveToFirst();
        do {
            TableColumn column = new TableColumn();
            column.setPk(cursor.getInt(cursor.getColumnIndex("pk")) == 1);
            column.setCid(cursor.getInt(cursor.getColumnIndex("cid")));
            column.setName(cursor.getString(cursor.getColumnIndex("name")));
            column.setType(cursor.getString(cursor.getColumnIndex("type")));
            column.setNotNull(cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1);
            columns.add(column);
        } while (cursor.moveToNext());
        dbInfoProvider.closeDb();
        mAdapter.updateData(columns);
    }

    public void info(@NonNull MenuItem item) {
        DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, mDatabaseName);
        List<Index> indexList = getIndexList(dbInfoProvider);
        for (Index index : indexList) {
            List<IndexInfo> indexInfoList = getIndexInfoList(dbInfoProvider, index);
            index.indexInfoList.addAll(indexInfoList);
        }
        dbInfoProvider.closeDb();

        new AlertDialog.Builder(this)
                .setTitle("索引列表")
                .setMessage(indexInfoString(indexList))
                .setPositiveButton(R.string.workbox_confirm, null)
                .show();
    }

    private String indexInfoString(@NonNull List<Index> indexList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Index index : indexList) {
            stringBuilder.append(index.name);
            stringBuilder.append(" ");
            if (index.unique) {
                stringBuilder.append("unique");
            }
            stringBuilder.append("\n");
            List<IndexInfo> indexInfoList = index.indexInfoList;
            for (IndexInfo indexInfo : indexInfoList) {
                stringBuilder.append(indexInfo.name);
                stringBuilder.append(" ");
                stringBuilder.append("(");
                stringBuilder.append(indexInfo.seqno);
                stringBuilder.append(", ");
                stringBuilder.append(indexInfo.cid);
                stringBuilder.append("), ");
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append("\n");
        }
        if (!indexList.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private List<IndexInfo> getIndexInfoList(@NonNull DbInfoProvider dbInfoProvider, @NonNull Index index) {
        List<IndexInfo> list = new ArrayList<>();
        Cursor cursor = dbInfoProvider.getIndexInfo(index.name);
        if (cursor == null) {
            return list;
        }

        cursor.moveToFirst();
        do {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int cid = cursor.getInt(cursor.getColumnIndex("cid"));
            int seqno = cursor.getInt(cursor.getColumnIndex("seqno"));
            IndexInfo indexInfo = new IndexInfo(name, cid, seqno);
            list.add(indexInfo);
        } while (cursor.moveToNext());
        cursor.close();
        return list;
    }

    private List<Index> getIndexList(@NonNull DbInfoProvider dbInfoProvider) {
        List<Index> indexList = new ArrayList<>();
        Cursor cursor = dbInfoProvider.getTableIndexList(mTableName);
        if (cursor == null) {
            return indexList;
        }
        cursor.moveToFirst();
        do {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int unique = cursor.getInt(cursor.getColumnIndex("unique"));
            indexList.add(new Index(name, unique == 1));
        } while (cursor.moveToNext());
        cursor.close();
        return indexList;
    }

    private void resetMenu() {
        Menu menu = mToolbar.getMenu();
        MenuItem info = menu.findItem(R.id.info);
        DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, mDatabaseName);
        Cursor cursor = dbInfoProvider.getTableIndexList(mTableName);
        info.setVisible(cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_info_menu;
    }

    private class RecyclerViewAdapter extends BaseRecyclerAdapter<TableColumn> {

        RecyclerViewAdapter() {
            super(new ArrayList<>());
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_column;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            TableColumn item = getData().get(position);
            TextView cidView = holder.getView(R.id.cid);
            TextView nameView = holder.getView(R.id.name);
            View pkView = holder.getView(R.id.pk);
            TextView typeView = holder.getView(R.id.type);
            View notnullView = holder.getView(R.id.notnull);
            cidView.setText("cid: " + item.getCid());
            nameView.setText("name: " + item.getName());
            typeView.setText("type: " + item.getType());
            pkView.setVisibility(item.isPk() ? View.VISIBLE : View.GONE);
            notnullView.setVisibility(item.isNotNull() ? View.VISIBLE : View.GONE);
        }
    }

    private static class Index {
        private String name;
        private boolean unique;
        private List<IndexInfo> indexInfoList = new ArrayList<>();

        Index(String name, boolean unique) {
            this.name = name;
            this.unique = unique;
        }

        @NonNull
        @Override
        public String toString() {
            return "Index{" +
                    "name='" + name + '\'' +
                    ", unique=" + unique +
                    '}';
        }
    }

    private static class IndexInfo {
        private String name;
        private int cid;
        private int seqno;

        IndexInfo(String name, int cid, int seqno) {
            this.name = name;
            this.cid = cid;
            this.seqno = seqno;
        }

        @NonNull
        @Override
        public String toString() {
            return "IndexInfo{" +
                    "name='" + name + '\'' +
                    ", cid=" + cid +
                    ", seqno=" + seqno +
                    '}';
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
