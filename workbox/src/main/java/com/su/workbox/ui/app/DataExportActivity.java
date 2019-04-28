package com.su.workbox.ui.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.ManifestParser;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.File;
import java.io.FilenameFilter;

public class DataExportActivity extends BaseAppCompatActivity {
    private static final String TAG = DataExportActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private static File sExportedApkFile;
    private static File sExportedManifestFile;
    private static File sExportedSoDirFile;
    private static File sExportedDatabaseDirFile;
    private static File sExportedSharedPreferenceDirFile;
    private static File sExportedSharedPrivateDirFile;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, DataExportActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        String versionName = GeneralInfoHelper.getVersionName();
        File exportedBaseDir = new File(Workbox.getWorkboxSdcardDir(), getPackageName());
        sExportedApkFile = new File(exportedBaseDir, versionName + "-" + GeneralInfoHelper.getAppName() + ".apk");
        sExportedManifestFile = new File(exportedBaseDir, versionName + "-manifest.xml");
        sExportedSoDirFile = new File(exportedBaseDir, versionName + "-native");
        sExportedDatabaseDirFile = new File(exportedBaseDir, versionName + "-databases");
        sExportedSharedPreferenceDirFile = new File(exportedBaseDir, versionName + "-" + SpHelper.SHARED_PREFERENCE_BASE_DIRNAME);
        sExportedSharedPrivateDirFile = exportedBaseDir;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ItemListFragment(), "app_data_export").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("数据导出");
    }

    public static class ItemListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private FragmentActivity mActivity;
        private String mDataDirPath;
        private FilenameFilter mDbFilenameFilter = (dir, name) -> name.endsWith(".db");
        private FilenameFilter mSpFilenameFilter = (dir, name) -> name.endsWith(".xml");
        private AppExecutors mAppExecutors = AppExecutors.getInstance();

        private void exportApkFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                File dir = sExportedApkFile.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                IOUtil.copyFile(new File(GeneralInfoHelper.getSourceDir()), sExportedApkFile);
                mActivity.runOnUiThread(() -> new ToastBuilder("已将apk导出到" + sExportedApkFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        private void exportSoFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                File nativeLibraryDir = new File(GeneralInfoHelper.getNativeLibraryDir());
                if (!sExportedSoDirFile.exists()) {
                    sExportedSoDirFile.mkdirs();
                }
                File[] sos = nativeLibraryDir.listFiles();
                for (File so : sos) {
                    IOUtil.copyFile(so, new File(sExportedSoDirFile, so.getName()));
                }
                mActivity.runOnUiThread(() -> new ToastBuilder("已将so导出到" + sExportedSoDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        private void exportManifestFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                File dir = sExportedManifestFile.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                ManifestParser parser = new ManifestParser(mActivity);
                IOUtil.writeFile(sExportedManifestFile.getAbsolutePath(), parser.getManifest());
                mActivity.runOnUiThread(() -> new ToastBuilder("已将apk导出到" + sExportedManifestFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        private void exportDatabaseFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                File databasesDir = new File(mDataDirPath, "databases");
                if (!sExportedDatabaseDirFile.exists()) {
                    sExportedDatabaseDirFile.mkdirs();
                }
                File[] databases = databasesDir.listFiles(mDbFilenameFilter);
                for (File database : databases) {
                    IOUtil.copyFile(database, new File(sExportedDatabaseDirFile, database.getName()));
                }
                mActivity.runOnUiThread(() -> new ToastBuilder("已将数据库文件导出到" + sExportedDatabaseDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        private void exportSharedPreferenceFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                File databasesDir = new File(mDataDirPath, "databases");
                if (!sExportedSharedPreferenceDirFile.exists()) {
                    sExportedSharedPreferenceDirFile.mkdirs();
                }
                File[] sharedPreferences = databasesDir.listFiles(mSpFilenameFilter);
                for (File sharedPreference : sharedPreferences) {
                    IOUtil.copyFile(sharedPreference, new File(sExportedSharedPreferenceDirFile, sharedPreference.getName()));
                }
                mActivity.runOnUiThread(() -> new ToastBuilder("已将SharedPreference文件导出到" + sExportedSharedPreferenceDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        private void exportPrivateDirFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            mAppExecutors.diskIO().execute(() -> {
                if (!sExportedSharedPrivateDirFile.exists()) {
                    sExportedSharedPrivateDirFile.mkdirs();
                }
                IOUtil.copyDirectory(new File(mDataDirPath), sExportedSharedPrivateDirFile);
                mActivity.runOnUiThread(() -> new ToastBuilder("已将应用私有文件导出到" + sExportedSharedPrivateDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.workbox_preference_data_export);
            mActivity = getActivity();
            mDataDirPath = mActivity.getApplicationInfo().dataDir;
            Preference apkPreference = findPreference("apk");
            apkPreference.setOnPreferenceClickListener(this);
            String[] splitSourceDirs = GeneralInfoHelper.getSplitSourceDirs();
            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                apkPreference.setSummary("请关闭instant run后重新编译安装应用");
                apkPreference.setEnabled(false);
            } else {
                apkPreference.setSummary("可将您现在使用中的App导出到sdcard中");
            }
            Preference soPreference = findPreference("so");
            soPreference.setOnPreferenceClickListener(this);
            File nativeLibraryDir = new File(GeneralInfoHelper.getNativeLibraryDir());
            if (IOUtil.hasFilesInDir(nativeLibraryDir)) {
                soPreference.setEnabled(true);
                soPreference.setSummary("共" + nativeLibraryDir.list().length + "个So文件");
            } else {
                soPreference.setEnabled(false);
                soPreference.setSummary("暂无So文件");
            }
            findPreference("manifest").setOnPreferenceClickListener(this);

            Preference databasePreference = findPreference("database");
            databasePreference.setOnPreferenceClickListener(this);
            File databasesDir = new File(mDataDirPath, "databases");
            if (IOUtil.hasFilesInDir(databasesDir, mDbFilenameFilter)) {
                databasePreference.setEnabled(true);
                databasePreference.setSummary("共" + databasesDir.list(mDbFilenameFilter).length + "个数据库文件");
            } else {
                databasePreference.setEnabled(false);
                databasePreference.setSummary("暂无数据库文件");
            }

            File sharedPreferenceDir = new File(mDataDirPath, SpHelper.SHARED_PREFERENCE_BASE_DIRNAME);
            Preference sharedPreferencePreference = findPreference("shared_preference");
            sharedPreferencePreference.setOnPreferenceClickListener(this);
            if (IOUtil.hasFilesInDir(sharedPreferenceDir, mSpFilenameFilter)) {
                sharedPreferencePreference.setEnabled(true);
                sharedPreferencePreference.setSummary("共" + sharedPreferenceDir.list(mSpFilenameFilter).length + "个数据库文件");
            } else {
                sharedPreferencePreference.setEnabled(false);
                sharedPreferencePreference.setSummary("暂无SharedPreference文件");
            }

            Preference privateDirPreference = findPreference("private_dir");
            privateDirPreference.setSummary(mDataDirPath);
            privateDirPreference.setOnPreferenceClickListener(this);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDividerHeight(-1);
            PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
            getListView().addItemDecoration(decoration);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!AppHelper.hasPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mActivity.runOnUiThread(() -> new ToastBuilder("没有外存写入权限").show());
                PermissionListActivity.startActivity(mActivity);
                return true;
            }
            switch (preference.getKey()) {
                case "apk":
                    exportApkFile();
                    break;
                case "so":
                    exportSoFile();
                    break;
                case "manifest":
                    exportManifestFile();
                    break;
                case "database":
                    exportDatabaseFile();
                    break;
                case "shared_preference":
                    exportSharedPreferenceFile();
                    break;
                case "private_dir":
                    exportPrivateDirFile();
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public String getTag() {
        return TAG;
    }
}
