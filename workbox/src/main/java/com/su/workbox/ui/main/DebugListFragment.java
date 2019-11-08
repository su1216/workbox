package com.su.workbox.ui.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.su.workbox.ui.app.FeatureListActivity;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.ui.app.record.ActivityLifecycleListener;
import com.su.workbox.ui.app.record.CurrentActivitySettingActivity;
import com.su.workbox.ui.app.record.CurrentActivityView;
import com.su.workbox.ui.app.record.LifecycleRecordListActivity;
import com.su.workbox.ui.data.DataListActivity;
import com.su.workbox.ui.data.DatabaseListActivity;
import com.su.workbox.ui.log.common.CommonLogActivity;
import com.su.workbox.ui.log.crash.CrashLogActivity;
import com.su.workbox.ui.mock.MockGroupHostActivity;
import com.su.workbox.ui.system.AppListActivity;
import com.su.workbox.ui.system.ConfigQualifiersActivity;
import com.su.workbox.ui.system.DeviceInfoActivity;
import com.su.workbox.ui.system.FileSystemActivity;
import com.su.workbox.ui.ui.GridLineSettingActivity;
import com.su.workbox.ui.ui.GridLineView;
import com.su.workbox.ui.ui.RulerSettingActivity;
import com.su.workbox.ui.ui.ScreenColorViewManager;
import com.su.workbox.ui.usage.RecordListActivity;
import com.su.workbox.ui.wifi.LanDeviceListActivity;
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
    private static final int REQUEST_MEDIA_PROJECTION = 3;
    private CurrentActivityView mCurrentActivityView;
    private SwitchPreferenceCompat mCurrentActivityPreference;
    private Preference mProxyPreference;
    private Preference mLanDevicesPreference;
    private Preference mNotificationPreference;
    private ListPreference mMockPolicyPreference;
    private Preference mHostsPreference;
    private Preference mWebViewHostsPreference;
    private String mHost;
    private String mWebViewHost;
    private FragmentActivity mActivity;
    private String mEntryClassName;
    //ui
    private GridLineView mGridLineView;
    private SwitchPreferenceCompat mGridLinePreference;
    private SwitchPreferenceCompat mColorPickerPreference;

    private NetworkChangeReceiver mReceiver;
    private static class NetworkChangeReceiver extends BroadcastReceiver {
        private DebugListFragment mFragment;

        public NetworkChangeReceiver(DebugListFragment fragment) {
            mFragment = fragment;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (parcelableExtra != null) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isWifiConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    Log.i(TAG, "isConnected: " + isWifiConnected);
                    mFragment.mLanDevicesPreference.setEnabled(isWifiConnected);
                }
            }
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(SpHelper.NAME);
        addPreferencesFromResource(R.xml.workbox_preference_debug_list);
        mActivity = getActivity();
        findPreference("crash_log").setOnPreferenceClickListener(this);
        findPreference("app_log").setOnPreferenceClickListener(this);
        mProxyPreference = findPreference("system_proxy");
        mLanDevicesPreference = findPreference("lan_devices");
        mLanDevicesPreference.setOnPreferenceClickListener(this);
        SwitchPreferenceCompat entryPreference = (SwitchPreferenceCompat) findPreference("debug_entry");
        mEntryClassName = Workbox.class.getPackage().getName() + ".ui.DebugEntryActivity";
        entryPreference.setChecked(isComponentEnabled(mActivity.getPackageManager(), mActivity.getPackageName(), mEntryClassName));
        entryPreference.setOnPreferenceChangeListener(this);
        findPreference("panel_settings").setOnPreferenceClickListener(this);
        mNotificationPreference = findPreference("system_notification");
        mNotificationPreference.setOnPreferenceClickListener(this);
        Preference appInfoPreference = findPreference("app_info");
        appInfoPreference.setOnPreferenceClickListener(this);
        appInfoPreference.setSummary("debuggable: " + GeneralInfoHelper.isDebuggable() + "    "
                + "版本:" + GeneralInfoHelper.getVersionName()
                + "(" + GeneralInfoHelper.getVersionCode() + ")");
        findPreference("app_component_info").setOnPreferenceClickListener(this);
        findPreference("activity_launcher").setOnPreferenceClickListener(this);
        findPreference("data_view_export").setOnPreferenceClickListener(this);

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
        findPreference("permission").setOnPreferenceClickListener(this);
        Preference featurePreference = findPreference("feature");
        featurePreference.setOnPreferenceClickListener(this);
        featurePreference.setVisible(!AppHelper.getRequiredFeatures(mActivity).isEmpty());
        mCurrentActivityView = CurrentActivityView.getInstance();
        mCurrentActivityPreference = (SwitchPreferenceCompat) findPreference("current_activity");
        mCurrentActivityPreference.setChecked(mCurrentActivityView.isShowing());
        mCurrentActivityPreference.setOnPreferenceClickListener(this);
        mCurrentActivityPreference.setOnPreferenceChangeListener(this);
        findPreference("lifecycle_history").setOnPreferenceClickListener(this);
        findPreference("more_phone_info").setOnPreferenceClickListener(this);
        findPreference("config_qualifier").setOnPreferenceClickListener(this);
        findPreference("app_list").setOnPreferenceClickListener(this);
        findPreference("file_system").setOnPreferenceClickListener(this);
        mHostsPreference = findPreference("hosts");
        mWebViewHostsPreference = findPreference("web_view_hosts");
        initNetworkPreferences();
        initUiPreference();

        findPreference("web_view_debug").setOnPreferenceClickListener(this);
        findPreference("js_interface").setOnPreferenceClickListener(this);
        Preference preference = findPreference("js_rhino");
        preference.setVisible(ReflectUtil.isUseRhino());
        preference.setOnPreferenceClickListener(this);
        mReceiver = new NetworkChangeReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mActivity.registerReceiver(mReceiver, intentFilter);
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
        mHost = Workbox.getHost();
        mWebViewHost = Workbox.getWebViewHost();
        initHostPreference(mHostsPreference, mHost, HostsActivity.TYPE_HOST);
        initHostPreference(mWebViewHostsPreference, mWebViewHost, HostsActivity.TYPE_WEB_VIEW_HOST);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(mReceiver);
    }

    private void initHostPreference(@NonNull Preference preference, String currentHost, int hostType) {
        WorkboxSupplier supplier = WorkboxSupplier.getInstance();
        List<Pair<String, String>> hosts;
        if (hostType == HostsActivity.TYPE_HOST) {
            hosts = supplier.allHosts();
        } else {
            hosts = supplier.allWebViewHosts();
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
        } else if (TextUtils.equals(key, "grid_line")) {
            if (!AppHelper.hasSystemWindowPermission(mActivity)) {
                AppHelper.gotoManageOverlayPermission(mActivity);
                mGridLinePreference.setChecked(false);
                return false;
            }
            mGridLineView.toggle();
            return true;
        } else if (TextUtils.equals(key, "data_usage")) {
            DataUsageInterceptor.setRecording((boolean) newValue);
            return true;
        } else if (TextUtils.equals(key, "color_picker")) {
            if (!AppHelper.hasSystemWindowPermission(mActivity)) {
                new ToastBuilder("请授予悬浮窗权限").show();
                AppHelper.gotoManageOverlayPermission(mActivity);
                mColorPickerPreference.setChecked(false);
                return false;
            }

            Boolean state = (Boolean) newValue;
            if (state == null || !state) {
                mColorPickerPreference.setChecked(false);
                ScreenColorViewManager.getInstance().performDestroy();
                return true;
            }
            requestMediaProjection();
            return false;
        } else if (TextUtils.equals(key, "measure")) {
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

    private void initUiPreference() {
        mColorPickerPreference = (SwitchPreferenceCompat) findPreference("color_picker");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mColorPickerPreference.setChecked(ScreenColorViewManager.isInitialized());
            mColorPickerPreference.setOnPreferenceClickListener(this);
            mColorPickerPreference.setOnPreferenceChangeListener(this);
        } else {
            mColorPickerPreference.getParent().removePreference(mColorPickerPreference);
        }

        mGridLineView = GridLineView.getInstance();
        mGridLinePreference = (SwitchPreferenceCompat) findPreference("grid_line");
        mGridLinePreference.setChecked(mGridLineView.isShowing());
        mGridLinePreference.setOnPreferenceClickListener(this);
        mGridLinePreference.setOnPreferenceChangeListener(this);

        findPreference("measure").setOnPreferenceClickListener(this);
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

    private void requestMediaProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_HOST || requestCode == REQUEST_WEB_VIEW_HOST) && resultCode == Activity.RESULT_OK) {
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
        } else if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            ScreenColorViewManager controller = ScreenColorViewManager.getInstance();
            controller.init(data);
            mColorPickerPreference.setChecked(true);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "panel_settings":
                startActivity(new Intent(mActivity, PanelSettingsActivity.class));
                return true;
            case "crash_log":
                startActivity(CrashLogActivity.getLaunchIntent(mActivity));
                return true;
            case "app_log":
                startActivity(new Intent(mActivity, CommonLogActivity.class));
                return true;
            case "lan_devices":
                Intent lanDeviceIntent = new Intent(mActivity, LanDeviceListActivity.class);
                startActivity(lanDeviceIntent);
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
            case "system_notification":
                AppHelper.goNotificationSettings(mActivity);
                return true;
            case "permission":
                startActivity(PermissionListActivity.getLaunchIntent(mActivity));
                return true;
            case "feature":
                startActivity(new Intent(mActivity, FeatureListActivity.class));
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
            case "file_system":
                startActivity(new Intent(mActivity, FileSystemActivity.class));
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
            case "config_qualifier":
                startActivity(new Intent(mActivity, ConfigQualifiersActivity.class));
                return true;
            case "app_list":
                startActivity(new Intent(mActivity, AppListActivity.class));
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
            case "color_picker":
                onPreferenceChange(mColorPickerPreference, !mColorPickerPreference.isChecked());
                return true;
            case "grid_line":
                startActivity(new Intent(mActivity, GridLineSettingActivity.class));
                return true;
            case "measure":
                startActivity(new Intent(mActivity, RulerSettingActivity.class));
                return true;
            default:
                return false;
        }
    }
}
