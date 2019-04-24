package com.su.workbox.ui.log.crash;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.ThreadUtil;

import java.util.Date;

public class CrashDetailActivity extends BaseAppCompatActivity {

    private static final String TAG = CrashDetailActivity.class.getSimpleName();
    private CrashLogRecordModel mModel;
    private CrashLogRecord mCrashLogRecord;
    private String mMd5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_crash_detail);
        Intent intent = getIntent();
        mCrashLogRecord = intent.getParcelableExtra("log");
        String apkFilePath = GeneralInfoHelper.getSourceDir();
        mMd5 = AppHelper.shellExec("md5sum " + apkFilePath);
        if (!TextUtils.isEmpty(mMd5)) {
            mMd5 = mMd5.replaceFirst("\\s[\\s\\S]+", "");
        }
        initViews();

        CrashLogRecordModel.Factory factory = new CrashLogRecordModel.Factory(getApplication());
        mModel = ViewModelProviders.of(this, factory).get(CrashLogRecordModel.class);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("崩溃详情");
    }

    private void initViews() {
        TextView timeView = findViewById(R.id.time);
        timeView.setText("time: " + ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(mCrashLogRecord.getTime())));
        TextView md5View = findViewById(R.id.md5);
        md5View.setText("md5: " + mMd5);
        TextView appInfoView = findViewById(R.id.app_info);
        appInfoView.setText(GeneralInfoHelper.infoString());
        TextView logView = findViewById(R.id.log);
        logView.setText(mCrashLogRecord.getContent());
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除此数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> delete())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void delete() {
        mModel.delete(mCrashLogRecord.getId());
        finish();
    }

    public void share(@NonNull MenuItem item) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareLog());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "分享日志"));
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_crash_detail_menu;
    }

    private String shareLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("time: " + ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(mCrashLogRecord.getTime())));
        sb.append("\n");
        sb.append("apk md5: " + mMd5);
        sb.append("\n");
        sb.append("\n");
        sb.append(mCrashLogRecord.getContent());
        sb.append("\n");
        sb.append(GeneralInfoHelper.infoString());
        return sb.toString();
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
