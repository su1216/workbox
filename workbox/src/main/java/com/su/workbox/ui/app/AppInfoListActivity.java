package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 应用信息
 */
public class AppInfoListActivity extends BaseAppCompatActivity implements ExpandableListView.OnChildClickListener {
    private static final String TAG = AppInfoListActivity.class.getSimpleName();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ExpandableListView mListView;
    private List<List<Pair<String, String>>> mDataList = new ArrayList<>();
    private AppInfoAdapter mAdapter;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, AppInfoListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_app_info_list);
        mListView = findViewById(R.id.expandable_list);
        mAdapter = new AppInfoAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(this);
        makeAppInfo();
        expandAll(mDataList.size());
    }

    private void expandAll(int groupCount) {
        for (int i = 0; i < groupCount; i++) {
            mListView.expandGroup(i);
        }
    }

    private void makeAppInfo() {
        mDataList.add(makeGroup1Info());
        mDataList.add(makeGroup2Info());
        mDataList.add(makeGroup3Info());
        mAdapter.notifyDataSetChanged();
    }

    private List<Pair<String, String>> makeGroup1Info() {
        List<Pair<String, String>> group1 = new ArrayList<>();
        group1.add(new Pair<>("应用名称", GeneralInfoHelper.getAppName()));
        group1.add(new Pair<>("版本名称", GeneralInfoHelper.getVersionName()));
        group1.add(new Pair<>("版本号", String.valueOf(GeneralInfoHelper.getVersionCode())));
        group1.add(new Pair<>("应用包名", GeneralInfoHelper.getPackageName()));

        int compileSdkVersion = GeneralInfoHelper.getCompileSdkVersion();
        int minSdkVersion = GeneralInfoHelper.getMinSdkVersion();
        int targetSdkVersion = GeneralInfoHelper.getTargetSdkVersion();
        if (compileSdkVersion > 0) {
            group1.add(new Pair<>("compileSdkVersion", compileSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(compileSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(compileSdkVersion) + ")"));
        }
        if (minSdkVersion > 0) {
            group1.add(new Pair<>("minSdkVersion", minSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(minSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(minSdkVersion) + ")"));
        }
        if (targetSdkVersion > 0) {
            group1.add(new Pair<>("targetSdkVersion", targetSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(targetSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(targetSdkVersion) + ")"));
        }
        group1.add(new Pair<>("debuggable", String.valueOf(GeneralInfoHelper.isDebuggable())));
        group1.add(new Pair<>("Uid", String.valueOf(GeneralInfoHelper.getUid())));
        group1.add(new Pair<>("Application类名", String.valueOf(GeneralInfoHelper.getApplicationClassName())));
        return group1;
    }

    private List<Pair<String, String>> makeGroup2Info() {
        List<Pair<String, String>> group2 = new ArrayList<>();
        group2.add(new Pair<>("应用安装时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getInstallTime()))));
        group2.add(new Pair<>("应用最近更新时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getUpdateTime()))));
        group2.add(new Pair<>("本次应用启动时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getLaunchTime()))));
        return group2;
    }

    private List<Pair<String, String>> makeGroup3Info() {
        DecimalFormat df = new DecimalFormat("#,###");
        List<Pair<String, String>> group3 = new ArrayList<>();
        String apkFilePath = GeneralInfoHelper.getSourceDir();
        File apkFile = new File(apkFilePath);
        String sizeContent = Formatter.formatShortFileSize(this, apkFile.length()) + " (" + df.format(apkFile.length()) + "B)";
        group3.add(new Pair<>("Apk大小", sizeContent));
        String md5 = AppHelper.shellExec("md5sum " + apkFilePath);
        if (!TextUtils.isEmpty(md5)) {
            group3.add(new Pair<>("Apk MD5", md5.replaceFirst("\\s.+$", "")));
        }
        String sha1 = AppHelper.shellExec("sha1sum " + apkFilePath);
        if (!TextUtils.isEmpty(sha1)) {
            group3.add(new Pair<>("Apk SHA1", sha1.replaceFirst("\\s.+$", "")));
        }
        String sha256 = AppHelper.shellExec("sha256sum " + apkFilePath);
        if (!TextUtils.isEmpty(sha256)) {
            group3.add(new Pair<>("Apk SHA256", sha256.replaceFirst("\\s.+$", "")));
        }
        group3.add(new Pair<>("Apk路径", apkFilePath));
        group3.add(new Pair<>("Native路径", GeneralInfoHelper.getNativeLibraryDir()));
        group3.add(new Pair<>("应用私有数据路径", GeneralInfoHelper.getDataDir()));
        return group3;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("应用信息");
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Pair<String, String> pair = mDataList.get(groupPosition).get(childPosition);
        AppHelper.copyToClipboard(this, pair.first, pair.second);
        new ToastBuilder("已将" + pair.first + "复制到粘贴板中").show();
        return true;
    }

    private class AppInfoAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private Resources mResources;
        private LayoutInflater mInflater;

        private AppInfoAdapter(Context context) {
            this.mContext = context;
            mResources = mContext.getResources();
            mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            convertView = new FrameLayout(mContext);
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, UiHelper.dp2px(1));
            convertView.setLayoutParams(lp);
            convertView.setBackgroundColor(mResources.getColor(R.color.workbox_disabled_text));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Pair<String, String> pair = mDataList.get(groupPosition).get(childPosition);
            ItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.workbox_item_app_info, parent, false);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }
            viewHolder.keyView.setText(pair.first);
            viewHolder.valueView.setText(pair.second);
            return convertView;
        }

        @Override
        public int getGroupCount() {
            return mDataList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mDataList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mDataList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mDataList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private static class ItemViewHolder {
        private TextView keyView;
        private TextView valueView;

        ItemViewHolder(View view) {
            keyView = view.findViewById(R.id.key);
            valueView = view.findViewById(R.id.value);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
