package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.shell.ShellUtil;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {

    public static final String TAG = FileActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private File mExportedBaseDir = new File(Workbox.getWorkboxSdcardDir(), GeneralInfoHelper.getPackageName());
    private AppExecutors mAppExecutors = AppExecutors.getInstance();
    private RecyclerViewAdapter mAdapter;
    private File mFile;
    private String mRoot;

    public static void startActivity(Context context, String root, String filepath) {
        Intent intent = new Intent(context, FileActivity.class);
        intent.putExtra("filepath", filepath);
        intent.putExtra("root", root);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        String filepath = intent.getStringExtra("filepath");
        mRoot = intent.getStringExtra("root");
        mFile = new File(filepath);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecyclerViewAdapter(makeDataList(mFile));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mFile.getName());
    }

    private List<Pair<String, String>> makeDataList(@NonNull File file) {
        String filepath = file.getAbsolutePath();
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("path", filepath));
        list.add(new Pair<>("size", IOUtil.formatFileSize(file.length())));
        list.add(new Pair<>("last-modify", ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()))));
        int authority = 0;
        if (file.canExecute()) {
            authority += 1;
        }
        if (file.canWrite()) {
            authority += 2;
        }
        if (file.canRead()) {
            authority += 4;
        }
        list.add(new Pair<>("permissions", String.format(Locale.getDefault(),
                "permissions: %d    r: %b    w: %b    x: %b",
                authority, file.canRead(), file.canWrite(), file.canExecute())));
        String fileType = IOUtil.getFileType(filepath);
        if (!TextUtils.isEmpty(fileType)) {
            list.add(new Pair<>("type", fileType));
        }
        String md5 = ShellUtil.getFileMd5(filepath);
        if (!TextUtils.isEmpty(md5)) {
            list.add(new Pair<>("md5", md5));
        }
        String sha1 = ShellUtil.getFileSha1(filepath);
        if (!TextUtils.isEmpty(sha1)) {
            list.add(new Pair<>("sha1", sha1));
        }
        String sha256 = ShellUtil.getFileSha256(filepath);
        if (!TextUtils.isEmpty(sha256)) {
            list.add(new Pair<>("sha256", sha256));
        }
        return list;
    }

    @Override
    public void onItemClick(View view, int position) {
        Pair<String, String> pair = mAdapter.getData().get(position);
        AppHelper.copyToClipboard(this, pair.first, pair.second);
        new ToastBuilder("已将" + pair.first + "复制到粘贴板中").show();
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<Pair<String, String>> {

        private RecyclerViewAdapter(List<Pair<String, String>> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_file_info;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            Pair<String, String> pair = getData().get(position);
            TextView keyView = holder.getView(R.id.key);
            TextView valueView = holder.getView(R.id.value);
            keyView.setText(pair.first);
            valueView.setText(pair.second);
        }
    }

    public void processExport(@NonNull MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "导出中...");
        mAppExecutors.diskIO().execute(() -> {
            IOUtil.export(FileActivity.this, mExportedBaseDir, mRoot, mFile.getAbsolutePath());
            DIALOG_FRAGMENT.dismissAllowingStateLoss();
        });
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_export_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
