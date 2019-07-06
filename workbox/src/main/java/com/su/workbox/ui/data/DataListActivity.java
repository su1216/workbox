package com.su.workbox.ui.data;

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
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.ui.app.AppInfoListActivity;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.ManifestParser;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class DataListActivity extends BaseAppCompatActivity {
    private static final String TAG = DataListActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private static File sExportedSoDirFile;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, DataListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_preference_activity_template);
        String versionName = GeneralInfoHelper.getVersionName();
        File exportedBaseDir = new File(Workbox.getWorkboxSdcardDir(), getPackageName());
        sExportedSoDirFile = new File(exportedBaseDir, versionName + "-native");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ItemListFragment(), "app_data_export").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("数据查看与导出");
    }

    public static class ItemListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private FragmentActivity mActivity;
        private String mDataDirPath;
        private FilenameFilter mDbFilenameFilter = (dir, name) -> name.endsWith(".db");
        private FilenameFilter mSpFilenameFilter = (dir, name) -> name.endsWith(".xml");
        private AppExecutors mAppExecutors = AppExecutors.getInstance();

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

        private void showCleanCacheDialog() {
            UiHelper.showConfirm(mActivity, "清除缓存后，部分功能需要重启应用才可使用，确定要清除应用缓存数据？", (dialog, which) -> mAppExecutors.diskIO().execute(() -> {
                IOUtil.deleteAllCache();
                WorkboxSupplier supplier = WorkboxSupplier.getInstance();
                List<File> files = supplier.getAllCustomCacheDirs();
                if (files == null || files.isEmpty()) {
                    mActivity.runOnUiThread(() -> new ToastBuilder("缓存清除完毕！").show());
                    return;
                }
                for (File file : files) {
                    IOUtil.deleteFiles(file);
                }
                mActivity.runOnUiThread(() -> new ToastBuilder("缓存清除完毕！").show());
            }));
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.workbox_preference_data_export);
            mActivity = getActivity();
            mDataDirPath = mActivity.getApplicationInfo().dataDir;
            Preference apkPreference = findPreference("apk");
            apkPreference.setOnPreferenceClickListener(this);
            Preference soPreference = findPreference("so");
            soPreference.setOnPreferenceClickListener(this);
            File nativeLibraryDir = new File(GeneralInfoHelper.getNativeLibraryDir());
            if (IOUtil.hasFilesInDir(nativeLibraryDir)) {
                soPreference.setEnabled(true);
                soPreference.setSummary("共" + nativeLibraryDir.list().length + "个so文件");
            } else {
                soPreference.setEnabled(false);
                soPreference.setSummary("暂无so文件");
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
                sharedPreferencePreference.setSummary("共" + sharedPreferenceDir.list(mSpFilenameFilter).length + "个SharedPreference文件");
            } else {
                sharedPreferencePreference.setEnabled(false);
                sharedPreferencePreference.setSummary("暂无SharedPreference文件");
            }

            Preference privateDirPreference = findPreference("private_dir");
            privateDirPreference.setSummary(mDataDirPath);
            privateDirPreference.setOnPreferenceClickListener(this);

            findPreference("clean_data").setOnPreferenceClickListener(this);
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
                    AppInfoListActivity.startActivity(mActivity);
                    break;
                case "so":
                    exportSoFile();
                    break;
                case "manifest":
                    ManifestParser parser = new ManifestParser(mActivity);
                    Intent intent = new Intent(mActivity, XmlViewerActivity.class);
                    intent.putExtra("title", "Manifest");
                    intent.putExtra("content", parser.getManifest());
                    startActivity(intent);
                    break;
                case "database":
                    DatabaseListActivity.startActivity(mActivity);
                    break;
                case "shared_preference":
                    startActivity(new Intent(mActivity, SharedPreferenceListActivity.class));
                    break;
                case "private_dir":
                    ExplorerActivity.startActivity(mActivity, mDataDirPath);
                    break;
                case "clean_data":
                    if (!AppHelper.hasPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new ToastBuilder("没有外存写权限").show();
                        return true;
                    }
                    showCleanCacheDialog();
                    return true;
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
