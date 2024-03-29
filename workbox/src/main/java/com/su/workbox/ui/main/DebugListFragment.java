package com.su.workbox.ui.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.net.interceptor.DataUsageInterceptor;
import com.su.workbox.ui.HostsActivity;
import com.su.workbox.ui.JsInterfaceListActivity;
import com.su.workbox.ui.JsListActivity;
import com.su.workbox.ui.WebViewListActivity;
import com.su.workbox.ui.app.AppComponentActivity;
import com.su.workbox.ui.app.AppInfoListActivity;
import com.su.workbox.ui.app.ComponentListActivity;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.ui.app.git.GitActivity;
import com.su.workbox.ui.app.lib.LibActivity;
import com.su.workbox.ui.app.record.ActivityLifecycleListener;
import com.su.workbox.ui.app.record.CurrentActivitySettingActivity;
import com.su.workbox.ui.app.record.CurrentActivityView;
import com.su.workbox.ui.app.record.LifecycleRecordListActivity;
import com.su.workbox.ui.base.DispatcherActivity;
import com.su.workbox.ui.data.DataListActivity;
import com.su.workbox.ui.data.DatabaseListActivity;
import com.su.workbox.ui.log.common.CommonLogActivity;
import com.su.workbox.ui.log.crash.CrashLogActivity;
import com.su.workbox.ui.mock.MockGroupHostActivity;
import com.su.workbox.ui.system.DeviceInfoActivity;
import com.su.workbox.ui.system.ShellFragment;
import com.su.workbox.ui.usage.RecordListActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 17-4-17.
 */

public class DebugListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String TAG = DebugListFragment.class.getSimpleName();
    private static final int REQUEST_HOST = 1;
    private static final int REQUEST_WEB_VIEW_HOST = 2;
    private static final int REQUEST_WEB_SOCKET_HOST = 3;
    private SwitchPreferenceCompat mPanelIconPreference;
    private CurrentActivityView mCurrentActivityView;
    private SwitchPreferenceCompat mCurrentActivityPreference;
    private Preference mProxyPreference;
    private Preference mNotificationPreference;
    private ListPreference mMockPolicyPreference;
    private Preference mHostsPreference;
    private Preference mWebViewHostsPreference;
    private Preference mWebSocketHostsPreference;
    private String mHost;
    private String mWebViewHost;
    private FragmentActivity mActivity;
    private String mEntryClassName;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(SpHelper.NAME);
        addPreferencesFromResource(R.xml.workbox_preference_debug_list);
        mActivity = getActivity();

        initWorkboxPreferences();
        initLogPreferences();
        initAppPreferences();
        initSystemPreferences();
        initNetworkPreferences();
        initOtherPreferences();
    }

    private void initWorkboxPreferences() {
        SwitchPreferenceCompat entryPreference = findPreference("debug_entry");
        mEntryClassName = Workbox.class.getPackage().getName() + ".ui.DebugEntryActivity";
        entryPreference.setChecked(isComponentEnabled(mActivity.getPackageManager(), mActivity.getPackageName(), mEntryClassName));
        entryPreference.setOnPreferenceChangeListener(this);
        mPanelIconPreference = findPreference("panel_icon");
        mPanelIconPreference.setOnPreferenceClickListener(this);
        mPanelIconPreference.setOnPreferenceChangeListener(this);
        findPreference("lib_info").setOnPreferenceClickListener(this);
    }

    private void initAppPreferences() {
        Preference appInfoPreference = findPreference("app_info");
        appInfoPreference.setOnPreferenceClickListener(this);
        appInfoPreference.setSummary("debuggable: " + GeneralInfoHelper.isDebuggable() + "    "
                + "版本:" + GeneralInfoHelper.getVersionName()
                + "(" + GeneralInfoHelper.getVersionCode() + ")");
        findPreference("git_log").setOnPreferenceClickListener(this);
        findPreference("app_component_info").setOnPreferenceClickListener(this);
        findPreference("activity_launcher").setOnPreferenceClickListener(this);
        findPreference("data_view_export").setOnPreferenceClickListener(this);
        findPreference("permission").setOnPreferenceClickListener(this);
        mCurrentActivityView = CurrentActivityView.getInstance();
        mCurrentActivityPreference = findPreference("current_activity");
        mCurrentActivityPreference.setChecked(mCurrentActivityView.isShowing());
        mCurrentActivityPreference.setOnPreferenceClickListener(this);
        mCurrentActivityPreference.setOnPreferenceChangeListener(this);
        findPreference("lifecycle_history").setOnPreferenceClickListener(this);
        mNotificationPreference = findPreference("system_notification");
        mNotificationPreference.setOnPreferenceClickListener(this);
    }

    private void initLogPreferences() {
        findPreference("crash_log").setOnPreferenceClickListener(this);
        findPreference("app_log").setOnPreferenceClickListener(this);
    }

    private void initSystemPreferences() {
        mProxyPreference = findPreference("system_proxy");
        Preference softwareInfoPreference = findPreference("software_info");
        softwareInfoPreference.setSummary("Android " + Build.VERSION.RELEASE + "    " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + "    "
                + "API " + Build.VERSION.SDK_INT);
        Preference hardwareInfoPreference = findPreference("hardware_info");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = GeneralInfoHelper.getScreenWidth();
        int heightPixels = GeneralInfoHelper.getScreenHeight();
        hardwareInfoPreference.setSummary("分辨率: " + widthPixels + " x " + heightPixels + " px    "
                + "密度: " + SystemInfoHelper.getDpiInfo(metrics.densityDpi) + " / " + metrics.density + "x   "
                + "CPU 位数: " + SystemInfoHelper.getCpuBit());
        findPreference("more_phone_info").setOnPreferenceClickListener(this);
        findPreference("shell").setOnPreferenceClickListener(this);
    }

    private void initNetworkPreferences() {
        boolean okHttp3 = ReflectUtil.isUseOkHttp3();
        SwitchPreferenceCompat dataUsagePreference = (SwitchPreferenceCompat) findPreference("data_usage");
        dataUsagePreference.setOnPreferenceClickListener(this);
        dataUsagePreference.setOnPreferenceChangeListener(this);
        dataUsagePreference.setVisible(okHttp3);

        mMockPolicyPreference = (ListPreference) findPreference(SpHelper.COLUMN_MOCK_POLICY);
        mMockPolicyPreference.setVisible(okHttp3);
        mMockPolicyPreference.setOnPreferenceChangeListener(this);
        initMockPolicy(mMockPolicyPreference.getValue());

        Preference mockDataListPreference = findPreference("mock_data_list");
        mockDataListPreference.setVisible(okHttp3);
        mockDataListPreference.setOnPreferenceClickListener(this);

        mHostsPreference = findPreference("hosts");
        mWebViewHostsPreference = findPreference("web_view_hosts");
        mWebSocketHostsPreference = findPreference("web_socket_hosts");
    }

    private void initOtherPreferences() {
        findPreference("web_view_debug").setOnPreferenceClickListener(this);
        findPreference("js_interface").setOnPreferenceClickListener(this);
        Preference preference = findPreference("js_rhino");
        preference.setVisible(ReflectUtil.isUseRhino());
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDividerHeight(-1);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
        getListView().addItemDecoration(decoration);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mHost = Workbox.getHost();
        mWebViewHost = Workbox.getWebViewHost();
        initHostPreference(mHostsPreference, mHost, HostsActivity.TYPE_HOST);
        initHostPreference(mWebViewHostsPreference, mWebViewHost, HostsActivity.TYPE_WEB_VIEW_HOST);
        initHostPreference(mWebSocketHostsPreference, mWebViewHost, HostsActivity.TYPE_WEB_SOCKET_HOST);
        setNotificationSummary();
        if (!NetworkUtil.isNetworkAvailable()) {
            mProxyPreference.setSummary("无网络连接");
            return;
        }
        String[] proxySetting = NetworkUtil.getSystemProxy();
        if (TextUtils.isEmpty(proxySetting[0])) {
            mProxyPreference.setSummary("无代理");
        } else {
            mProxyPreference.setSummary(proxySetting[0] + ":" + proxySetting[1]);
        }
    }

    private void initHostPreference(@NonNull Preference preference, String currentHost, int hostType) {
        WorkboxSupplier supplier = WorkboxSupplier.getInstance();
        List<Pair<String, String>> hosts;
        if (hostType == HostsActivity.TYPE_HOST) {
            hosts = supplier.allHosts();
        } else if (hostType == HostsActivity.TYPE_WEB_VIEW_HOST) {
            hosts = supplier.allWebViewHosts();
        } else {
            hosts = supplier.allWebSocketHosts();
        }
        preference.setVisible(!hosts.isEmpty());
        if (!hosts.isEmpty()) {
            int size = hosts.size();
            Pair<String, String> pair = null;
            for (int i = 0; i < size; i++) {
                Pair<String, String> host = hosts.get(i);
                if (TextUtils.equals(host.second, currentHost)) {
                    pair = host;
                    preference.setSummary(host.first + ": " + currentHost);
                }
            }
            if (pair == null) {
                preference.setSummary(currentHost);
            } else {
                preference.setSummary(pair.first + " (" + pair.second + ")");
            }
        }
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, final Object newValue) {
        String key = preference.getKey();
        if (TextUtils.equals(key, "debug_entry")) {
            boolean enable = (boolean) newValue;
            enableEntry(mActivity, mEntryClassName, enable);
            return true;
        } else if (TextUtils.equals(key, SpHelper.COLUMN_PANEL_ICON)) {
            boolean enable = (boolean) newValue;
            if (enable) {
                if (!AppHelper.hasSystemWindowPermission(mActivity)) {
                    AppHelper.gotoManageOverlayPermission(mActivity);
                    mPanelIconPreference.setChecked(false);
                    return false;
                }
                FloatEntry.getInstance().show();
            } else {
                FloatEntry.getInstance().hide();
            }
            return true;
        } else if (TextUtils.equals(key, "current_activity")) {
            if (!AppHelper.hasSystemWindowPermission(mActivity)) {
                AppHelper.gotoManageOverlayPermission(mActivity);
                mCurrentActivityPreference.setChecked(false);
                return false;
            }
            mCurrentActivityView.toggle();
            if (mCurrentActivityView.isShowing()) {
                mCurrentActivityView.updateTopActivity(ActivityLifecycleListener.getTopActivity());
            }
            return true;
        } else if (TextUtils.equals(key, SpHelper.COLUMN_MOCK_POLICY)) {
            initMockPolicy(newValue.toString());
            return true;
        } else if (TextUtils.equals(key, "data_usage")) {
            DataUsageInterceptor.setRecording((boolean) newValue);
            return true;
        }
        return false;
    }

    private void initMockPolicy(String value) {
        mMockPolicyPreference.setSummary("");
        CharSequence[] values = mMockPolicyPreference.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (TextUtils.equals(values[i], value)) {
                mMockPolicyPreference.setSummary(mMockPolicyPreference.getEntries()[i]);
                break;
            }
        }
    }

    public static void enableEntry(Context context, String className, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, className);
        pm.setComponentEnabledSetting(componentName,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isComponentEnabled(PackageManager pm, String pkgName, String clsName) {
        ComponentName componentName = new ComponentName(pkgName, clsName);
        int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);
        switch (componentEnabledSetting) {
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
                return false;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);
                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null) {
                        Collections.addAll(components, packageInfo.activities);
                    }
                    if (packageInfo.services != null) {
                        Collections.addAll(components, packageInfo.services);
                    }
                    if (packageInfo.providers != null) {
                        Collections.addAll(components, packageInfo.providers);
                    }

                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.equals(clsName)) {
                            return componentInfo.isEnabled();
                        }
                    }
                    // the component is not declared in the AndroidManifest
                    return false;
                } catch (PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    return false;
                }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void setNotificationSummary() {
        boolean enabled = AppHelper.isNotificationEnabled(mActivity);
        if (!enabled) {
            mNotificationPreference.setSummary("disabled");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mNotificationPreference.setSummary("enabled");
            return;
        }

        List<NotificationChannel> notificationChannels = AppHelper.listNotificationChannels(mActivity);
        if (notificationChannels != null && !notificationChannels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (NotificationChannel channel : notificationChannels) {
                sb.append(channel.getName());
                sb.append(": ");
                sb.append(AppHelper.isNotificationChannelEnabled(mActivity, channel.getId()) ? "enabled" : "disabled");
                sb.append("  ");
            }
            sb.delete(sb.length() - 2, sb.length());
            mNotificationPreference.setSummary(sb.toString());
        } else {
            mNotificationPreference.setSummary("应用未创建channel");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && (requestCode == REQUEST_HOST || requestCode == REQUEST_WEB_VIEW_HOST || requestCode == REQUEST_WEB_SOCKET_HOST)) {
            final String value = data.getStringExtra("value");
            if (!TextUtils.equals(mHost, value)) {
                new AlertDialog.Builder(mActivity)
                        .setCancelable(false)
                        .setMessage("是否重启应用？")
                        .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                            AppHelper.restartApp(mActivity);
                        })
                        .setNegativeButton(R.string.workbox_cancel, null)
                        .show();
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "panel_icon":
                startActivity(new Intent(mActivity, PanelSettingsActivity.class));
                return true;
            case "lib_info":
                startActivity(new Intent(mActivity, LibActivity.class));
                return true;
            case "crash_log":
                startActivity(CrashLogActivity.getLaunchIntent(mActivity));
                return true;
            case "app_log":
                startActivity(new Intent(mActivity, CommonLogActivity.class));
                return true;
            case "hosts":
                Intent hostIntent = new Intent(mActivity, HostsActivity.class);
                hostIntent.putExtra("type", HostsActivity.TYPE_HOST);
                startActivityForResult(hostIntent, REQUEST_HOST);
                return true;
            case "web_view_hosts":
                Intent webViewHostIntent = new Intent(mActivity, HostsActivity.class);
                webViewHostIntent.putExtra("type", HostsActivity.TYPE_WEB_VIEW_HOST);
                startActivityForResult(webViewHostIntent, REQUEST_WEB_VIEW_HOST);
                return true;
            case "web_socket_hosts":
                Intent webSocketHostIntent = new Intent(mActivity, HostsActivity.class);
                webSocketHostIntent.putExtra("type", HostsActivity.TYPE_WEB_SOCKET_HOST);
                startActivityForResult(webSocketHostIntent, REQUEST_WEB_SOCKET_HOST);
                return true;
            case "system_notification":
                AppHelper.goNotificationSettings(mActivity);
                return true;
            case "permission":
                startActivity(PermissionListActivity.getLaunchIntent(mActivity));
                return true;
            case "data_view_export":
                startActivity(DataListActivity.getLaunchIntent(mActivity));
                return true;
            case "current_activity":
                startActivity(new Intent(mActivity, CurrentActivitySettingActivity.class));
                return true;
            case "lifecycle_history":
                startActivity(new Intent(mActivity, LifecycleRecordListActivity.class));
                return true;
            case "database":
                startActivity(new Intent(mActivity, DatabaseListActivity.class));
                return true;
            case "app_info":
                startActivity(AppInfoListActivity.getLaunchIntent(mActivity));
                return true;
            case "git_log":
                startActivity(new Intent(mActivity, GitActivity.class));
                return true;
            case "app_component_info":
                startActivity(new Intent(mActivity, AppComponentActivity.class));
                return true;
            case "activity_launcher":
                Intent launchList = new Intent(mActivity, ComponentListActivity.class);
                launchList.putExtra("type", "launcher");
                startActivity(launchList);
                return true;
            case "more_phone_info":
                startActivity(DeviceInfoActivity.getLaunchIntent(mActivity));
                return true;
            case "shell":
                startActivity(DispatcherActivity.getLaunchIntentWithBaseFragment(mActivity, ShellFragment.class));
                return true;
            case "data_usage":
                startActivity(new Intent(mActivity, RecordListActivity.class));
                return true;
            case "mock_data_list":
                mActivity.startActivity(MockGroupHostActivity.getLaunchIntent(mActivity, preference.getTitle()));
                return true;
            case "web_view_debug":
                Intent serverIntent = new Intent(mActivity, WebViewListActivity.class);
                serverIntent.putExtra("type", 0);
                serverIntent.putExtra("title", preference.getTitle());
                startActivity(serverIntent);
                return true;
            case "js_interface":
                startActivity(JsInterfaceListActivity.getLaunchIntent(mActivity));
                return true;
            case "js_rhino":
                if (!AppHelper.hasPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || !AppHelper.hasPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new ToastBuilder("没有外存读写权限").show();
                    startActivity(PermissionListActivity.getLaunchIntent(mActivity));
                    return true;
                }
                startActivity(new Intent(mActivity, JsListActivity.class));
                return true;
            default:
                return false;
        }
    }
}
