package com.su.workbox.ui.data;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.widget.SimpleBlockedDialogFragment;

import java.io.File;

@SuppressLint("Registered")
public abstract class DataActivity extends BaseAppCompatActivity {

    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    protected String mDataDirPath;
    protected String mVersionName;
    protected File mExportedBaseDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataDirPath = getApplicationInfo().dataDir;
        mVersionName = GeneralInfoHelper.getVersionName();
        mExportedBaseDir = new File(Workbox.getWorkboxSdcardDir(), getPackageName());
    }

    public void processExport(@NonNull MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "导出中...");
        mAppExecutors.diskIO().execute(() -> {
            export();
            DIALOG_FRAGMENT.dismissAllowingStateLoss();
        });
    }

    @WorkerThread
    protected abstract void export();

    @Override
    public int menuRes() {
        return R.menu.workbox_export_menu;
    }
}
