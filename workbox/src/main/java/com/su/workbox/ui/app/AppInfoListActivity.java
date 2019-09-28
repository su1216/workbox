package com.su.workbox.ui.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.PidInfo;
import com.su.workbox.ui.data.DataActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 应用信息
 */
public class AppInfoListActivity extends DataActivity implements ExpandableListView.OnChildClickListener {
    private static final String TAG = AppInfoListActivity.class.getSimpleName();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ExpandableListView mListView;
    private List<List<Pair<String, CharSequence>>> mDataList = new ArrayList<>();
    private AppInfoAdapter mAdapter;
    private File mExportedApkFile;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, AppInfoListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_app_info_list);
        mExportedApkFile = new File(mExportedBaseDir, mVersionName + "-" + GeneralInfoHelper.getAppName() + ".apk");
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
        mDataList.add(makeGroup4Info());
        mAdapter.notifyDataSetChanged();
    }

    private List<Pair<String, CharSequence>> makeGroup1Info() {
        List<Pair<String, CharSequence>> group = new ArrayList<>();
        group.add(new Pair<>("应用名称", GeneralInfoHelper.getAppName()));
        group.add(new Pair<>("版本名称", GeneralInfoHelper.getVersionName()));
        group.add(new Pair<>("版本号", String.valueOf(GeneralInfoHelper.getVersionCode())));
        group.add(new Pair<>("应用包名", GeneralInfoHelper.getPackageName()));

        int compileSdkVersion = GeneralInfoHelper.getCompileSdkVersion();
        int minSdkVersion = GeneralInfoHelper.getMinSdkVersion();
        int targetSdkVersion = GeneralInfoHelper.getTargetSdkVersion();
        if (compileSdkVersion > 0) {
            group.add(new Pair<>("compileSdkVersion", compileSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(compileSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(compileSdkVersion) + ")"));
        }
        if (minSdkVersion > 0) {
            group.add(new Pair<>("minSdkVersion", minSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(minSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(minSdkVersion) + ")"));
        }
        if (targetSdkVersion > 0) {
            group.add(new Pair<>("targetSdkVersion", targetSdkVersion + " (Android " + SystemInfoHelper.getSystemVersionCode(targetSdkVersion) + ", " + SystemInfoHelper.getSystemVersionName(targetSdkVersion) + ")"));
        }
        group.add(new Pair<>("debuggable", String.valueOf(GeneralInfoHelper.isDebuggable())));
        group.add(new Pair<>("Application", String.valueOf(GeneralInfoHelper.getApplicationClassName())));
        return group;
    }

    private List<Pair<String, CharSequence>> makeGroup2Info() {
        List<Pair<String, CharSequence>> group = new ArrayList<>();
        PidInfo pidInfo = IOUtil.getProcessInfo(GeneralInfoHelper.getProcessId());
        if (pidInfo == null) {
            getProcessInfoWithReflect(group);
        } else {
            getProcessInfoWithCommandPs(group, pidInfo);
        }
        return group;
    }

    private void getProcessInfoWithReflect(List<Pair<String, CharSequence>> group) {
        String processName = GeneralInfoHelper.getProcessName();
        int pid = GeneralInfoHelper.getProcessId();
        group.add(new Pair<>("ProcessName", processName));
        group.add(new Pair<>("Pid", String.valueOf(pid)));
        int ppid = -1;
        try {
            Method method = android.os.Process.class.getMethod("myPpid");
            method.setAccessible(true);
            ppid = (int) method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }

        group.add(new Pair<>("PPid", String.valueOf(ppid)));
        int uid = GeneralInfoHelper.getUid();
        String formatUid = "";
        try {
            StringBuilder sb = new StringBuilder();
            //android 4.4 没有UserHandle#formatUid(int) 方法
            Method method = UserHandle.class.getMethod("formatUid", StringBuilder.class, int.class);
            method.setAccessible(true);
            method.invoke(null, sb, uid);
            formatUid = sb.toString();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        if (TextUtils.isEmpty(formatUid)) {
            group.add(new Pair<>("Uid", String.valueOf(uid)));
        } else {
            group.add(new Pair<>("Uid", formatUid + " / " + uid));
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            StringBuilder sb = new StringBuilder();
            int length = 0;
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo service : runningServices) {
                if (service.uid == uid) {
                    if (pid == service.pid) {
                        length = service.process.length() + 3 + String.valueOf(service.pid).length();
                        sb.insert(0, "\n");
                        sb.insert(0, service.pid);
                        sb.insert(0, " / ");
                        sb.insert(0, service.process);
                    } else {
                        sb.append(service.process);
                        sb.append(" / ");
                        sb.append(service.pid);
                        sb.append("\n");
                    }
                }
            }
            if (sb.length() == 0) {
                return;
            }
            sb.deleteCharAt(sb.length() - 1);
            SpannableString ss = new SpannableString(sb.toString());
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.workbox_color_primary)), 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            group.add(new Pair<>("User's Process(es)", ss));
        }
    }

    private void getProcessInfoWithCommandPs(List<Pair<String, CharSequence>> group, @NonNull PidInfo pidInfo) {
        List<PidInfo> list = pidInfo.getUserPidInfo();
        group.add(new Pair<>("ProcessName", pidInfo.getName()));
        group.add(new Pair<>("Pid", String.valueOf(pidInfo.getPid())));
        group.add(new Pair<>("PPid", String.valueOf(pidInfo.getPpid())));
        group.add(new Pair<>("Uid", pidInfo.getFormatUid() + " / " + pidInfo.getUid()));
        if (!justShowSelfProcess(list)) {
            StringBuilder sb = new StringBuilder();
            int start = 0;
            int length = 0;
            for (PidInfo info : list) {
                if (TextUtils.equals(info.getName(), "ps")) {
                    continue;
                }
                if (info.getPid() == pidInfo.getPid()) {
                    start = sb.length();
                    length = info.getName().length() + 3 + String.valueOf(info.getPid()).length();
                }
                sb.append(info.getName());
                sb.append(" / ");
                sb.append(info.getPid());
                sb.append("\n");
            }
            if (sb.length() == 0) {
                return;
            }
            sb.deleteCharAt(sb.length() - 1);
            SpannableString ss = new SpannableString(sb.toString());
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.workbox_color_primary)), start, start + length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            group.add(new Pair<>("User's Process(es)", ss));
        }
    }

    private boolean justShowSelfProcess(List<PidInfo> list) {
        if (list.size() < 2) {
            return true;
        }
        if (list.size() == 2) {
            for (PidInfo info : list) {
                if (TextUtils.equals(info.getName(), "ps")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Pair<String, CharSequence>> makeGroup3Info() {
        List<Pair<String, CharSequence>> group = new ArrayList<>();
        group.add(new Pair<>("应用安装时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getInstallTime()))));
        group.add(new Pair<>("应用最近更新时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getUpdateTime()))));
        group.add(new Pair<>("本次应用启动时间", SIMPLE_DATE_FORMAT.format(new Date(GeneralInfoHelper.getLaunchTime()))));
        return group;
    }

    private List<Pair<String, CharSequence>> makeGroup4Info() {
        DecimalFormat df = new DecimalFormat("#,###");
        List<Pair<String, CharSequence>> group = new ArrayList<>();
        String apkFilePath = GeneralInfoHelper.getSourceDir();
        File apkFile = new File(apkFilePath);
        String sizeContent = Formatter.formatShortFileSize(this, apkFile.length()) + " (" + df.format(apkFile.length()) + "B)";
        group.add(new Pair<>("Apk大小", sizeContent));
        String md5 = IOUtil.getFileMd5(apkFilePath);
        if (!TextUtils.isEmpty(md5)) {
            group.add(new Pair<>("Apk MD5", md5));
        }
        String sha1 = IOUtil.getFileSha1(apkFilePath);
        if (!TextUtils.isEmpty(sha1)) {
            group.add(new Pair<>("Apk SHA1", sha1));
        }
        String sha256 = IOUtil.getFileSha256(apkFilePath);
        if (!TextUtils.isEmpty(sha256)) {
            group.add(new Pair<>("Apk SHA256", sha256));
        }
        group.add(new Pair<>("Apk路径", apkFilePath));
        group.add(new Pair<>("Native路径", GeneralInfoHelper.getNativeLibraryDir()));
        group.add(new Pair<>("应用私有数据路径", GeneralInfoHelper.getDataDir()));
        return group;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("应用信息");
    }

    @Override
    protected void export() {
        String[] splitSourceDirs = GeneralInfoHelper.getSplitSourceDirs();
        if (splitSourceDirs != null && splitSourceDirs.length > 0) {
            runOnUiThread(() -> new ToastBuilder("请关闭instant run后重新编译安装应用").setDuration(Toast.LENGTH_LONG).show());
            return;
        }

        File dir = mExportedApkFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        IOUtil.copyFile(new File(GeneralInfoHelper.getSourceDir()), mExportedApkFile);
        runOnUiThread(() -> new ToastBuilder("已将apk导出到" + mExportedApkFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Pair<String, CharSequence> pair = mDataList.get(groupPosition).get(childPosition);
        AppHelper.copyToClipboard(this, pair.first, pair.second.toString());
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
            Pair<String, CharSequence> pair = mDataList.get(groupPosition).get(childPosition);
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
