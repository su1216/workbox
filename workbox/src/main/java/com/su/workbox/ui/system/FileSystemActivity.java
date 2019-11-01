package com.su.workbox.ui.system;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.entity.FileSystem;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.Collections;
import java.util.List;

public class FileSystemActivity extends BaseAppCompatActivity {

    private static final String TAG = FileSystemActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(new FileSystemAdapter(getFileSystemList()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("文件系统");
    }

    private List<FileSystem> getFileSystemList() {
        List<FileSystem> list = IOUtil.getFileSystemList();
        IOUtil.fillFileSystemType(list);
        Collections.sort(list, (o1, o2) -> o1.getMountedOn().compareTo(o2.getMountedOn()));
        return list;
    }

    private class FileSystemAdapter extends BaseRecyclerAdapter<FileSystem> {

        FileSystemAdapter(List<FileSystem> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_file_system;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            FileSystem fileSystem = getData().get(position);
            TextView mountedOnView =  holder.getView(R.id.mounted_on);
            TextView fileSystemView =  holder.getView(R.id.file_system);
            TextView usedInfoView =  holder.getView(R.id.used_info);
            mountedOnView.setText(fileSystem.getMountedOn());
            fileSystemView.setText(fileSystem.getFileSystem() + " (" + fileSystem.getFileSystemType() + ")");
            usedInfoView.setText(fileSystem.getUsed() + " / " + fileSystem.getSize() + "  " + fileSystem.getUse());
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}