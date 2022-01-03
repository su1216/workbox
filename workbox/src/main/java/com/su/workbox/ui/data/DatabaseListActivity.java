package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.database.DbInfoProvider;
import com.su.workbox.entity.database.Table;
import com.su.workbox.entity.database.Trigger;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseListActivity extends DataActivity {
    public static final String TAG = DatabaseListActivity.class.getSimpleName();
    private final List<String> mGroupList = new ArrayList<>();
    private final List<Database> mDatabaseList = new ArrayList<>();
    private DatabaseAdapter mAdapter;
    private File mExportedDatabaseDirFile;
    private File mDatabasesDir;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, DatabaseListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mDatabasesDir = new File(mDataDirPath, "databases");
        mExportedDatabaseDirFile = new File(mExportedBaseDir, mVersionName + "-databases");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new DatabaseAdapter(this);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("数据库列表");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mGroupList.clear();
        mDatabaseList.clear();
        String[] dbList = getApplicationContext().databaseList();
        if (dbList == null || dbList.length == 0) {
            return;
        }

        for (String dbName : dbList) {
            if (!dbName.endsWith(".db")) {
                continue;
            }
            mGroupList.add(dbName);
            DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, dbName);
            int version = dbInfoProvider.getDatabaseVersion();
            List<Table> tables = getTables(dbInfoProvider.getAllTablesAndViews());
            List<Trigger> triggers = getTriggerSqlList(dbInfoProvider.getAllTriggers());
            Database database = new Database(mDatabasesDir, dbName, version, tables, triggers);
            mDatabaseList.add(database);
            dbInfoProvider.closeDb();
        }
        mAdapter.updateData(mGroupList, mDatabaseList);
    }

    private List<Table> getTables(Cursor cursor) {
        List<Table> list = new ArrayList<>();
        if (cursor == null || cursor.getCount() == 0) {
            return list;
        }
        cursor.moveToFirst();
        do {
            String tableName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String tableSql = cursor.getString(cursor.getColumnIndexOrThrow("sql"));
            String tableType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            Table table = new Table();
            table.setTableName(tableName);
            table.setTableSql(tableSql);
            table.setType(tableType);
            list.add(table);
        } while (cursor.moveToNext());
        cursor.close();
        Collections.sort(list);
        return list;
    }

    private List<Trigger> getTriggerSqlList(Cursor cursor) {
        List<Trigger> list = new ArrayList<>();
        if (cursor == null || cursor.getCount() == 0) {
            return list;
        }
        cursor.moveToFirst();
        do {
            String sql = cursor.getString(cursor.getColumnIndexOrThrow("sql"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String tblName = cursor.getString(cursor.getColumnIndexOrThrow("tbl_name"));
            Trigger trigger = new Trigger();
            trigger.setName(name);
            trigger.setTblName(tblName);
            trigger.setSql(sql);
            list.add(trigger);
        } while (cursor.moveToNext());
        cursor.close();
        return list;
    }

    @Override
    protected void export() {
        if (!mExportedDatabaseDirFile.exists()) {
            mExportedDatabaseDirFile.mkdirs();
        }
        //需要将db/db-shm/db-wal同时导出
        File[] databases = mDatabasesDir.listFiles();
        for (File database : databases) {
            IOUtil.copyFile(database, new File(mExportedDatabaseDirFile, database.getName()));
        }
        runOnUiThread(() -> new ToastBuilder("已将数据库文件导出到" + mExportedDatabaseDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
    }

    private static class DatabaseAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

        private List<String> mGroupList = new ArrayList<>();
        private List<Database> mDatabaseList = new ArrayList<>();
        private Context mContext;

        DatabaseAdapter(@NonNull Context context) {
            mContext = context;
        }

        private void updateData(@NonNull List<String> groupList, @NonNull List<Database> databaseList) {
            mGroupList = new ArrayList<>(groupList);
            mDatabaseList = new ArrayList<>(databaseList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        public int getLayoutId(int itemType) {
            if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                return R.layout.workbox_item_group_database;
            } else {
                return R.layout.workbox_item_table;
            }
        }

        private void bindGroupData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int groupIndex = getPositions(position)[0];
            final String dbPath = mGroupList.get(groupIndex);
            final Database database = mDatabaseList.get(groupIndex);
            TextView databaseNameView = holder.getView(R.id.database_name);
            View triggersView = holder.getView(R.id.triggers);
            TextView versionView = holder.getView(R.id.version);
            TextView tablesView = holder.getView(R.id.tables);
            databaseNameView.setText(dbPath + " - " + IOUtil.formatFileSize(database.size));
            triggersView.setVisibility(database.triggerCount == 0 ? View.GONE : View.VISIBLE);
            triggersView.setOnClickListener(v -> showAllTriggers(database));
            versionView.setText("version: " + database.version);
            tablesView.setText("tables: " + database.tableCount + "    views: " + database.viewCount);
            holder.getView(R.id.arrow).setSelected(!database.collapse);
            holder.itemView.setOnClickListener(v -> {
                database.collapse = !database.collapse;
                holder.getView(R.id.arrow).setSelected(!database.collapse);
                notifyDataSetChanged();
            });
        }

        private void showAllTriggers(@NonNull Database database) {
            List<Trigger> triggerSqlList = database.triggerSqlList;
            StringBuilder stringBuilder = new StringBuilder();
            for (Trigger trigger : triggerSqlList) {
                stringBuilder.append(trigger.getName());
                stringBuilder.append("\n");
                stringBuilder.append(trigger.getSql());
                stringBuilder.append("\n\n");
            }
            if (!triggerSqlList.isEmpty()) {
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            }

            new AlertDialog.Builder(mContext)
                    .setTitle("triggers")
                    .setMessage(stringBuilder)
                    .setPositiveButton(R.string.workbox_confirm, null)
                    .show();
        }

        private void bindChildData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int[] positions = getPositions(position);
            String dbName = mGroupList.get(positions[0]);
            Table table = mDatabaseList.get(positions[0]).tableList.get(positions[1]);
            TextView nameView = holder.getView(R.id.name);
            TextView typeView = holder.getView(R.id.type);
            nameView.setText(table.getTableName());
            typeView.setText(table.getType());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, TableInfoActivity.class);
                intent.putExtra("sql", table.getTableSql());
                intent.putExtra("database_name", dbName);
                intent.putExtra("table_name", table.getTableName());
                mContext.startActivity(intent);
            });
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                bindGroupData(holder, position);
            } else {
                bindChildData(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int pointer = -1;
            for (Database database : mDatabaseList) {
                pointer++;
                if (pointer == position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_GROUP;
                }
                int childrenSize = database.collapse ? 0 : database.tableList.size();
                pointer += childrenSize;
                if (pointer >= position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_NORMAL;
                }
            }
            throw new IllegalStateException("wrong state");
        }

        private int[] getPositions(int position) {
            int[] positions = new int[2];
            int pointer = -1;
            int groupPosition = -1;
            int childPosition = -1;
            positions[0] = groupPosition;
            positions[1] = childPosition;
            for (Database database : mDatabaseList) {
                pointer++;
                groupPosition++;
                positions[0] = groupPosition;
                int childrenSize = database.collapse ? 0 : database.tableList.size();
                if (pointer + childrenSize >= position) {
                    childPosition = position - pointer - 1;
                    positions[1] = childPosition;
                    return positions;
                }
                pointer += childrenSize;
            }
            return positions;
        }

        @Override
        public int getItemCount() {
            int size = 0;
            for (Database database : mDatabaseList) {
                size++;
                int childrenSize = database.collapse ? 0 : database.tableList.size();
                size += childrenSize;
            }
            return size;
        }
    }

    private static class Database {
        private File mDatabasesDir;
        private long size;
        private String databasePath;
        private int version;
        private List<Table> tableList;
        private int tableCount;
        private int viewCount;
        private int triggerCount;
        private boolean collapse;
        private List<Trigger> triggerSqlList;

        private Database(@NonNull File databasesDir, String databasePath, int version, List<Table> tableList, List<Trigger> triggerSqlList) {
            mDatabasesDir = databasesDir;
            this.databasePath = databasePath;
            this.version = version;
            this.tableList = tableList;
            this.triggerSqlList = triggerSqlList;
            this.size = getDbSize(databasePath);
            if (tableList != null) {
                int tableCount = 0;
                int viewCount = 0;
                for (Table table : tableList) {
                    if ("table".equals(table.getType())) {
                        tableCount++;
                    } else {
                        viewCount++;
                    }
                }
                this.tableCount = tableCount;
                this.viewCount = viewCount;
            }
            if (triggerSqlList != null) {
                triggerCount = triggerSqlList.size();
            }
        }

        //db + sb-shm + db-wal + db-journal
        private long getDbSize(String databasePath) {
            long mainFileSize = new File(mDatabasesDir, databasePath).length();
            long shmFileSize = new File(mDatabasesDir, databasePath + "-shm").length();
            long walFileSize = new File(mDatabasesDir, databasePath + "-wal").length();
            long journalFileSize = new File(mDatabasesDir, databasePath + "-journal").length();
            return mainFileSize + shmFileSize + walFileSize + journalFileSize;
        }

        @Override
        public String toString() {
            return "Database{" +
                    "size=" + size +
                    ", databasePath='" + databasePath + '\'' +
                    ", version=" + version +
                    ", tableList=" + tableList +
                    ", tableCount=" + tableCount +
                    ", viewCount=" + viewCount +
                    ", triggerCount=" + triggerCount +
                    ", collapse=" + collapse +
                    ", triggerSqlList=" + triggerSqlList +
                    '}';
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
