package com.su.workbox.ui.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferenceListActivity extends DataActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = SharedPreferenceListActivity.class.getSimpleName();
    private SharedPreferenceAdapter mAdapter;
    private FilenameFilter mSpFilenameFilter = (dir, name) -> name.endsWith(".xml");
    private static File sExportedSharedPreferenceDirFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_page_list_view);
        sExportedSharedPreferenceDirFile = new File(mExportedBaseDir, mVersionName + "-" + SpHelper.SHARED_PREFERENCE_BASE_DIRNAME);
        mAdapter = new SharedPreferenceAdapter(this);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("SharedPreference文件列表");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<File> list = new ArrayList<>(SpHelper.getAllSharedPreferenceFiles(this));
        mAdapter.updateData(list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = (File) mAdapter.getItem(position);
        Intent sharedPreferenceIntent = new Intent(this, SharedPreferenceDetailActivity.class);
        sharedPreferenceIntent.putExtra("name", IOUtil.getFileNameWithoutExtension(file));
        startActivity(sharedPreferenceIntent);
    }

    @Override
    protected void export() {
        File sharedPreferencesDir = new File(mDataDirPath, SpHelper.SHARED_PREFERENCE_BASE_DIRNAME);
        if (!sExportedSharedPreferenceDirFile.exists()) {
            sExportedSharedPreferenceDirFile.mkdirs();
        }
        File[] sharedPreferences = sharedPreferencesDir.listFiles(mSpFilenameFilter);
        for (File sharedPreference : sharedPreferences) {
            IOUtil.copyFile(sharedPreference, new File(sExportedSharedPreferenceDirFile, sharedPreference.getName()));
        }
        runOnUiThread(() -> new ToastBuilder("已将SharedPreference文件导出到" + sExportedSharedPreferenceDirFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
    }

    private static class SharedPreferenceAdapter extends BaseAdapter {

        private List<File> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        SharedPreferenceAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.workbox_item_sp, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            File item = mList.get(position);
            holder.fileView.setText(item.getName());
            holder.detailView.setText(IOUtil.getFileBrief(item));
            return convertView;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        void updateData(@NonNull List<File> list) {
            mList = new ArrayList<>(list);
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        private TextView fileView;
        private TextView detailView;

        ViewHolder(View convertView) {
            fileView = convertView.findViewById(R.id.file);
            detailView = convertView.findViewById(R.id.detail);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
