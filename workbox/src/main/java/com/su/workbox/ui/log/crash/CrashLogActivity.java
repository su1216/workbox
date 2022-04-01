package com.su.workbox.ui.log.crash;

import android.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CrashLogActivity extends BaseAppCompatActivity {

    private static final String TAG = CrashLogActivity.class.getSimpleName();
    private CrashLogRecordModel mModel;
    private LogAdapter mAdapter;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, CrashLogActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mAdapter = new LogAdapter(this, new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mAdapter));

        CrashLogRecordModel.Factory factory = new CrashLogRecordModel.Factory(getApplication());
        mModel = new ViewModelProvider(this, factory).get(CrashLogRecordModel.class);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("崩溃日志");
        query();
    }

    private void query() {
        MutableLiveData<List<CrashLogRecord>> recordListData = mModel.getRecordList();
        recordListData.observe(this, crashLogRecords -> {
            if (crashLogRecords == null || crashLogRecords.isEmpty()) {
                mAdapter.refresh(Collections.EMPTY_LIST);
            } else {
                mAdapter.refresh(crashLogRecords);
            }
        });
    }

    private class LogAdapter extends BaseRecyclerAdapter<CrashLogRecord> implements RecyclerItemClickListener.OnItemClickListener {

        private Context mContext;

        LogAdapter(Context context, List<CrashLogRecord> data) {
            super(data);
            mContext = context;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_crash_log;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            CrashLogRecord logRecord = getData().get(position);
            TextView firstLineView =  holder.getView(R.id.firstLine);
            TextView timeView =  holder.getView(R.id.time);
            TextView pidView =  holder.getView(R.id.pid);
            firstLineView.setText(logRecord.getFirstLine());
            timeView.setText(ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(logRecord.getTime())));
            pidView.setText("pid: " + logRecord.getPid());
        }

        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent(mContext, CrashDetailActivity.class);
            intent.putExtra("log", getData().get(position));
            mContext.startActivity(intent);
        }
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除所有数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> deleteAll())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void deleteAll() {
        mModel.deleteAll();
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_crash_logs_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
