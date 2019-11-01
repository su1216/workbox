package com.su.workbox.ui.system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.SystemInfo;
import com.su.workbox.ui.PermissionRequiredActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.utils.SensorUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.TelephonyManagerWrapper;
import com.su.workbox.utils.ThreadUtil;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by mahao on 17-5-27.
 * 调试功能列表 - 手机信息
 */
public class DeviceInfoActivity extends PermissionRequiredActivity {

    private static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private static final int REQUEST_PHONE_CODE = 1;
    private static final int REQUEST_IDS_CODE = 2;
    private static final int REQUEST_NETWORK_CODE = 3;
    private static final String KEY_SCREEN = "screen";
    private static final String KEY_SYSTEM = "system";
    private static final String KEY_NETWORK = "network";
    private static final String KEY_HARDWARE = "hardware";
    private static final String KEY_IDS = "ids";
    private static final String KEY_FEATURES = "features";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SENSOR = "sensor";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_VM = "vm";
    private static final String KEY_INPUT_METHOD = "input_method";
    private static final String KEY_BUILD = "android.os.build";
    private static final String KEY_ENVIRONMENT_PATH = "environment_path";
    private static final String KEY_PROPERTIES = "system_properties";
    private static final String KEY_SHELL_SET = "shell_set";
    private List<SystemInfo> mData = new ArrayList<>();

    private InfoAdapter mAdapter;
    private String mBatteryInfo;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                String statusString;
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                            statusString = getString(R.string.workbox_plugged_usb);
                        } else {
                            statusString = getString(R.string.workbox_plugged_ac);
                        }
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "放电中";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "未充电";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "满电";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    default:
                        statusString = "未知";
                        break;
                }
                int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                String healthString;
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = "良好";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = "过热";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = "低电";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = "电压过高";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = "未知错误";
                        break;
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        healthString = "低温";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    default:
                        healthString = "未知";
                        break;
                }
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                float voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                if (voltage > 1000) {
                    voltage = voltage / 1000.0f;
                }
                float temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f;
                String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                mBatteryInfo = getString(R.string.workbox_status) + ": " + statusString + "\n"
                        + getString(R.string.workbox_type) + ": " + technology + "\n"
                        + getString(R.string.workbox_health) + ": " + healthString + "\n"
                        + getString(R.string.workbox_remaining_power) + ": " + level * 100 / scale + "%\n"
                        + getString(R.string.workbox_voltage) + ": " + voltage + " V\n"
                        + getString(R.string.workbox_temperature) + ": " + temperature + " °C\n"
                        + getString(R.string.workbox_capacity) + ": " + ReflectUtil.getBatteryCapacity(context) + " mA";
            }
        }
    };
    private String mIp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_system_info);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        initData();
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemSpaceDecoration(4, 30));
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置控件显示的顺序
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //设置控件显示间隔时间
        controller.setDelay(0.3f);
        recyclerView.setLayoutAnimation(controller);
        mAdapter = new InfoAdapter(this, mData);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("设备信息");
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppExecutors.getInstance().networkIO().execute(this::getPublicIp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryInfoReceiver);
    }

    private void initData() {
        mData.add(new SystemInfo(KEY_SCREEN, "屏幕"));
        mData.add(new SystemInfo(KEY_SYSTEM, "系统"));
        mData.add(new SystemInfo(KEY_NETWORK, "网络"));
        mData.add(new SystemInfo(KEY_HARDWARE, "硬件"));
        mData.add(new SystemInfo(KEY_IDS, "ID"));
        mData.add(new SystemInfo(KEY_FEATURES, "Feature"));
        if (AppHelper.isPhone(this)) {
            mData.add(new SystemInfo(KEY_PHONE, "电话"));
        }
        if (SensorUtil.hasUsefulSensors()) {
            mData.add(new SystemInfo(KEY_SENSOR, "传感器"));
        }
        mData.add(new SystemInfo(KEY_BATTERY, "电池"));
        mData.add(new SystemInfo(KEY_VM, "虚拟机"));
        mData.add(new SystemInfo(KEY_INPUT_METHOD, "输入法"));
        mData.add(new SystemInfo(KEY_BUILD, "Build"));
        mData.add(new SystemInfo(KEY_ENVIRONMENT_PATH, "PATH"));
        mData.add(new SystemInfo(KEY_PROPERTIES, "系统属性"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mData.add(new SystemInfo(KEY_SHELL_SET, "Shell变量"));
        } else {
            mData.add(new SystemInfo(KEY_SHELL_SET, "环境变量"));
        }
    }

    private void getPublicIp() {
        if (!NetworkUtil.isNetworkAvailable()) {
            Log.w(TAG, "network is not available.");
            return;
        }
        BufferedReader in = null;
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            mIp = in.readLine();
            Log.d(TAG, "ip: " + mIp);
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.closeQuietly(in);
        }
    }

    @Override
    public AlertDialog makeHintDialog(String permission, int requestCode) {
        if (REQUEST_PHONE_CODE == requestCode) {
            return makePhonePermissionHintDialog();
        } else if (REQUEST_IDS_CODE == requestCode) {
            return makeIdsPermissionHintDialog();
        } else {
            return makeNetworkPermissionHintDialog();
        }
    }

    private AlertDialog makePhonePermissionHintDialog() {
        return makePermissionHintDialog("授予访问设备电话权限将可查看更多电话相关信息", KEY_PHONE);
    }

    private AlertDialog makeIdsPermissionHintDialog() {
        return makePermissionHintDialog("授予访问设备电话权限将可查设备序列号", KEY_IDS);
    }

    private AlertDialog makeNetworkPermissionHintDialog() {
        return makePermissionHintDialog("授予定位权限将可查Wifi SSID", KEY_NETWORK);
    }

    private AlertDialog makePermissionHintDialog(String hint, String key) {
        return new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage(hint)
                .setPositiveButton(R.string.workbox_set_permission, (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null))))
                .setNegativeButton(R.string.workbox_cancel, (dialog, which) -> {
                    for (SystemInfo systemInfo : mAdapter.getData()) {
                        if (TextUtils.equals(systemInfo.getKey(), key)) {
                            mAdapter.showInfoDialog(systemInfo);
                            return;
                        }
                    }
                })
                .show();
    }

    private static class InfoAdapter extends BaseRecyclerAdapter<SystemInfo> {

        private DeviceInfoActivity mActivity;
        private PackageManager mPackageManager;
        private Resources mResources;

        private InfoAdapter(DeviceInfoActivity activity, List<SystemInfo> data) {
            super(data);
            mActivity = activity;
            mResources = activity.getResources();
            mPackageManager = activity.getPackageManager();
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_system_info;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, final int position, int itemType) {
            ((TextView) holder.itemView).setText(getData().get(position).getTitle());
            holder.itemView.setOnClickListener(v -> {
                        SystemInfo systemInfo = getData().get(holder.getAdapterPosition());
                        String key = systemInfo.getKey();
                        if (KEY_PHONE.equals(key)) {
                            if (PackageManager.PERMISSION_GRANTED != mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName())) {
                                mActivity.permissionRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE_CODE);
                                return;
                            }
                        } else if (KEY_IDS.equals(key)) {
                            if (PackageManager.PERMISSION_GRANTED != mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName())) {
                                mActivity.permissionRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_IDS_CODE);
                                return;
                            }
                        } else if (KEY_NETWORK.equals(key)) {
                            if (PackageManager.PERMISSION_GRANTED != mPackageManager.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mActivity.getPackageName())) {
                                mActivity.permissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_NETWORK_CODE);
                                return;
                            }
                        }
                        showInfoDialog(systemInfo);
                    }
            );
        }

        private void showInfoDialog(SystemInfo systemInfo) {
            new AlertDialog.Builder(mActivity)
                    .setNegativeButton(R.string.workbox_close, null)
                    .setPositiveButton(R.string.workbox_share, (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, systemInfo.getDesc());
                        intent.setType("text/plain");
                        mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.workbox_share_to)));
                    })
                    .setTitle(systemInfo.getTitle())
                    .setMessage(getMsg(systemInfo.getKey()))
                    .show();
        }

        private CharSequence getMsg(@NonNull String key) {
            switch (key) {
                case KEY_SCREEN:
                    return getScreenInfo();
                case KEY_SYSTEM:
                    return getSystemInfo();
                case KEY_NETWORK:
                    return getNetworkInfo();
                case KEY_HARDWARE:
                    return getHardwareInfo();
                case KEY_IDS:
                    return getPhoneId();
                case KEY_FEATURES:
                    return getFeatureList();
                case KEY_PHONE:
                    return getTelephonyInfo();
                case KEY_SENSOR:
                    return getSensorInfo();
                case KEY_BATTERY:
                    return getBatteryInfo();
                case KEY_VM:
                    return getVmInfo();
                case KEY_INPUT_METHOD:
                    return getInstalledInputMethod();
                case KEY_BUILD:
                    return getBuildInfo();
                case KEY_ENVIRONMENT_PATH:
                    return getEnvironmentPathInfo();
                case KEY_PROPERTIES:
                    return getSystemPropertiesInfo();
                case KEY_SHELL_SET:
                    return getVariablesInfo();
                default:
                    throw new IllegalArgumentException("can not find any info about key: " + key);
            }
        }

        @SuppressLint("HardwareIds")
        private String getPhoneId() {
            boolean isHasPermission = PackageManager.PERMISSION_GRANTED == mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName());
            String desc = "Android ID: " + GeneralInfoHelper.getAndroidId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                desc += "\n\n" + "设备序列号: " + mResources.getString(R.string.workbox_can_not_get);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                desc += "\n\n" + "设备序列号: " + (isHasPermission ? Build.getSerial() : "未授权");
            } else {
                desc += "\n\n" + "设备序列号: " + Build.SERIAL;
            }
            desc += "\n\n" + "FINGERPRINT: " + Build.FINGERPRINT;
            return desc;
        }

        private String getHardwareInfo() {
            String desc = "手机: " + (AppHelper.isPhone(mActivity) ? "是" : "否");
            desc += "\n\n" + "型号: " + Build.MODEL;
            desc += "\n\n" + "制造商: " + Build.MANUFACTURER;
            desc += "\n\n" + "主板: " + Build.BOARD;
            desc += "\n\n" + "设备: " + Build.DEVICE;
            desc += "\n\n" + "产品: " + Build.PRODUCT;
            desc += "\n\n" + "CPU 核数: " + Runtime.getRuntime().availableProcessors();
            desc += "\n\n" + "CPU 位数: " + SystemInfoHelper.getCpuBit();
            desc += "\n\n" + "CPU 型号: " + SystemInfoHelper.getCpuName();
            desc += "\n\n" + "ABIs: " + TextUtils.join(", ", Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Build.SUPPORTED_ABIS : new String[]{Build.CPU_ABI, Build.CPU_ABI2});
            desc += "\n\n" + "存储: 共 "
                    + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalExternalMemorySize())
                    + ", 可用 " + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableExternalMemorySize());
            desc += "\n\n" + "内存: 共 "
                    + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalMemorySize(mActivity))
                    + ", 可用 " + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableMemory(mActivity));
            return desc;
        }

        private String getNetworkInfo() {
            int connectedType = NetworkUtil.getConnectedType(mActivity);
            String networkTypeName = NetworkUtil.getNetworkTypeName();
            if (connectedType == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
                int telephonyNetworkType = tm.getNetworkType();
                int networkClass = NetworkUtil.getNetworkClass(telephonyNetworkType);
                String networkClassName = NetworkUtil.getNetworkClassName(networkClass);
                String telephonyNetworkTypeName = NetworkUtil.getTelephonyNetworkTypeName(telephonyNetworkType);
                networkTypeName += " " + networkClassName;
                if (!TextUtils.isEmpty(telephonyNetworkTypeName)) {
                    networkTypeName += " (" + telephonyNetworkTypeName + ")";
                }
            }
            String desc = mActivity.getString(R.string.workbox_type) + ": " + networkTypeName;
            if (connectedType == ConnectivityManager.TYPE_WIFI) {
                WifiManager mgr = (WifiManager) mActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
                if (mgr != null) {
                    WifiInfo wifiInfo = mgr.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    desc += "\n\n" + "Wifi SSID: " + ssid;
                }
            }
            desc += "\n\n" + "IPv4: " + NetworkUtil.getIpv4Address();
            desc += "\n\n" + "IPv6: " + NetworkUtil.getIpv6Address();
            desc += "\n\n" + "Mac地址: " + NetworkUtil.getMacAddress();
            if (!TextUtils.isEmpty(mActivity.mIp)) {
                desc += "\n\n公网IP: " + mActivity.mIp;
            }
            return desc;
        }

        private String getSystemInfo() {
            long time = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            Locale locale = Locale.getDefault();
            String desc = "Android " + Build.VERSION.RELEASE + " / " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + " / " + "API " + Build.VERSION.SDK_INT;
            desc += "\n\n" + "系统类型: " + Build.TYPE;
            desc += "\n\n" + "基带版本: " + Build.getRadioVersion();
            desc += "\n\n" + "Linux 内核版本: " + System.getProperty("os.version");
            desc += "\n\n" + "Http User Agent: " + System.getProperty("http.agent");
            desc += "\n\n" + "开机时间: " + ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
            desc += "\n\n" + "当前语言: " + locale;
            return desc;
        }

        private String getScreenInfo() {
            Resources resources = mActivity.getResources();
            Configuration config = resources.getConfiguration();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            int widthPixels = GeneralInfoHelper.getScreenWidth();
            int heightPixels = GeneralInfoHelper.getScreenHeight();
            String desc = "分辨率: " + widthPixels + " x " + heightPixels + " px"
                    + " / " + UiHelper.px2dp(widthPixels) + " x " + UiHelper.px2dp(heightPixels) + " dp";
            desc += "\n\nsmallestWidth: " + config.smallestScreenWidthDp + " dp";
            desc += "\n\n状态栏高度: " + GeneralInfoHelper.getStatusBarHeight() + " px";
            int navigationBarHeight = UiHelper.getNavigationBarHeight(mActivity);
            if (navigationBarHeight > 0) {
                desc += "\n\n导航栏高度: " + navigationBarHeight + " px";
            }

            Point point = UiHelper.getRealScreenSize(mActivity);
            desc += "\n\n" + "density: " + metrics.density;
            desc += "\n\n" + "scaledDensity: " + metrics.scaledDensity;
            float width = point.x / metrics.xdpi;
            float height = point.y / metrics.ydpi;
            desc += "\n\n" + "ppi: " + Math.round(Math.sqrt(widthPixels * widthPixels + heightPixels * heightPixels) / Math.sqrt(width * width + height * height));
            desc += "\n\n" + "密度: " + metrics.densityDpi + "dp" + " / " + SystemInfoHelper.getDpiInfo(metrics.densityDpi) + " / " + metrics.density + "x";
            desc += "\n\n" + "精确密度: " + metrics.xdpi + " x " + metrics.ydpi + " dp";
            double screenDiagonalSize = UiHelper.getScreenDiagonalSize(metrics, point);
            DecimalFormat format = new DecimalFormat("0.0");
            desc += "\n\n" + "屏幕尺寸: " + format.format(width) + "''" + " x " + format.format(height) + "''" + " / " + format.format(screenDiagonalSize) + "英寸";
            return desc;
        }

        private String getFeatureList() {
            String openGlEsName = "OpenGL ES";
            //array -> list -> sort -> display
            FeatureInfo[] featureInfoArray = mPackageManager.getSystemAvailableFeatures();
            List<Pair<String, String>> list = new ArrayList<>(featureInfoArray.length);
            for (FeatureInfo featureInfo : featureInfoArray) {
                String name = featureInfo.name;
                if (TextUtils.isEmpty(name)) {
                    list.add(new Pair<>(openGlEsName, featureInfo.getGlEsVersion()));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && featureInfo.version > 0) {
                        list.add(new Pair<>(featureInfo.name, String.valueOf(featureInfo.version)));
                    } else {
                        list.add(new Pair<>(featureInfo.name, ""));
                    }
                }
            }

            Collections.sort(list, (o1, o2) -> {
                if (TextUtils.equals(o1.first, o2.first)) {
                    return 0;
                }
                return o1.first.compareTo(o2.first);
            });

            StringBuilder stringBuilder = new StringBuilder();
            for (Pair<String, String> pair : list) {
                String name = pair.first;
                stringBuilder.append(name);
                if (TextUtils.equals(openGlEsName, name) || !TextUtils.isEmpty(pair.second)) {
                    stringBuilder.append(": " + pair.second);
                }
                stringBuilder.append("\n");
            }
            if (!list.isEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            return stringBuilder.toString();
        }

        private String getTelephonyInfo() {
            TelephonyManager manager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
            TelephonyManagerWrapper wrapper = new TelephonyManagerWrapper(manager);
            int count = wrapper.getPhoneCount();

            StringBuilder builder = new StringBuilder();
            builder.append("多卡: " + (count > 1 ? mResources.getString(R.string.workbox_yes) : mResources.getString(R.string.workbox_no)) + "\n");
            builder.append("当前网络: " + wrapper.getNetworkTypeName() + "\n\n");
            for (int i = 0; i < count; i++) {
                if (!wrapper.isSimCardPresent(i)) {
                    continue;
                }

                String indexHolder = String.valueOf(i + 1);
                builder.append("卡" + indexHolder + "\n");
                int phoneType = wrapper.getPhoneType(i);
                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA || phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                        builder.append(wrapper.field2String("网络制式", "CDMA") + "\n");
                        String meid = wrapper.getMeid(i);
                        builder.append(wrapper.field2String("MEID", meid) + "\n");
                    } else {
                        builder.append(wrapper.field2String("网络制式", "GSM") + "\n");
                        String imei = wrapper.getImei(i);
                        builder.append(wrapper.field2String("IMEI", imei) + "\n");
                    }
                }
                String subscriberId = wrapper.getSubscriberId(i);
                builder.append(wrapper.field2String("IMSI", subscriberId) + "\n");
                String sv = wrapper.getDeviceSoftwareVersion(i);
                builder.append(wrapper.field2String("SV", sv) + "\n");
                String phoneNumber = wrapper.getLine1Number(i);
                builder.append(wrapper.field2String("电话号码", phoneNumber) + "\n");
                String simSerialNumber = wrapper.getSimSerialNumber(i);
                builder.append(wrapper.field2String("ICCID", simSerialNumber) + "\n");

                String networkOperator = wrapper.getNetworkOperator(i);
                if (!TextUtils.isEmpty(networkOperator)) {
                    String mcc = networkOperator.substring(0, 3);
                    String mnc = networkOperator.substring(3);
                    builder.append(wrapper.field2String("MCC", mcc) + "\n");
                    builder.append(wrapper.field2String("MNC", mnc) + "\n");
                }
                String networkCountryIso = wrapper.getNetworkCountryIso(i).toUpperCase();
                builder.append(wrapper.field2String("国家代码", networkCountryIso) + "\n");

                String operatorName = wrapper.getNetworkOperatorName(i);
                builder.append(wrapper.field2String("运营商名称", operatorName) + "\n\n");
            }
            builder.deleteCharAt(builder.length() - 2);
            return builder.toString();
        }

        private String getSensorInfo() {
            Set<Integer> recordedType = new HashSet<>();
            List<Sensor> sensors = SensorUtil.getAllSensors();
            StringBuilder builder = new StringBuilder();
            for (Sensor sensor : sensors) {
                int type = sensor.getType();
                if (SensorUtil.isUsefulSensor(type) && !recordedType.contains(type)) {
                    recordedType.add(type);
                    builder.append(SensorUtil.getReadableType(sensor.getType()));
                    builder.append("\n");
                    builder.append("名称: ");
                    builder.append(sensor.getName());
                    builder.append("\n");
                    builder.append("功耗: ");
                    builder.append(sensor.getPower());
                    builder.append("\n");
                    builder.append("供应商: ");
                    builder.append(sensor.getVendor());
                    builder.append("\n");
                    builder.append("版本: ");
                    builder.append(sensor.getVersion());
                    builder.append("\n");
                    builder.append("\n");
                }
            }
            return builder.toString();
        }

        private String getVmInfo() {
            final String vmVersion = System.getProperty("java.vm.version");
            String vm;
            if (TextUtils.isEmpty(vmVersion)) {
                vm = "unknown";
            } else if (vmVersion.startsWith("2")) {
                vm = "ART";
            } else {
                vm = "Dalvik";
            }
            String desc = "虚拟机: " + vm;
            desc += "\n" + "虚拟机版本: " + vmVersion;
            desc += "\n" + "Java Home: " + System.getProperty("java.home");
            desc += "\n" + "Java Class Path: " + System.getProperty("java.class.path");
            desc += "\n\n" + "Java Boot Class Path:";
            String[] paths = System.getProperty("java.boot.class.path").split(":");
            for (String path : paths) {
                desc += "\n" + path;
            }
            return desc;
        }

        private String getBatteryInfo() {
            return mActivity.mBatteryInfo;
        }

        private String getInstalledInputMethod() {
            String defaultInputMethodId = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            InputMethodManager inputMgr = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> inputMethodList = inputMgr.getEnabledInputMethodList();
            StringBuilder builder = new StringBuilder();
            PackageManager pm = mActivity.getPackageManager();
            for (InputMethodInfo method : inputMethodList) {
                builder.append(method.loadLabel(pm));
                if (TextUtils.equals(method.getId(), defaultInputMethodId)) {
                    builder.append("(");
                    builder.append("current");
                    builder.append(")");
                }
                builder.append("\n");
                builder.append("package: ");
                builder.append(method.getPackageName());
                builder.append("\n");
                builder.append("\n");
            }
            return builder.toString();
        }

        private CharSequence getBuildInfo() {
            Class<Build> clazz = Build.class;
            Field[] fields = clazz.getDeclaredFields();
            List<FieldWrapper> fieldList = new ArrayList<>();
            for (Field field : fields) {
                Deprecated annotation = field.getAnnotation(Deprecated.class);
                fieldList.add(new FieldWrapper(field, annotation != null));
            }
            Collections.sort(fieldList, mFieldComparator);
            Log.d(TAG, "fieldList: " + fieldList);
            return makeBuildString(fieldList);
        }

        private Spannable makeBuildString(List<FieldWrapper> fieldList) {
            List<Pair<Integer, Integer>> spanIndexList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (FieldWrapper wrapper : fieldList) {
                Field field = wrapper.field;
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    Object value = field.get(null);
                    String stringValue;
                    if (value == null) {
                        stringValue = "null";
                    } else if (value.getClass().isArray()) {
                        int length = Array.getLength(value);
                        Object[] objects = new Object[length];
                        for (int i = 0; i < length; i++) {
                            objects[i] = Array.get(value, i);
                        }
                        stringValue = Arrays.toString(objects);
                    } else {
                        stringValue = value.toString();
                    }

                    if (wrapper.deprecated) {
                        int start = sb.length();
                        int end = start + fieldName.length();
                        spanIndexList.add(new Pair<>(start, end));
                    }

                    sb.append(fieldName);
                    sb.append(": ");
                    sb.append(stringValue);
                    sb.append("\n");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            Spannable spannable = new SpannableString(sb.toString());
            for (Pair<Integer, Integer> pair : spanIndexList) {
                spannable.setSpan(new StrikethroughSpan(),
                        pair.first,
                        pair.second,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            return spannable;
        }

        private Comparator<FieldWrapper> mFieldComparator = (o1, o2) -> o1.field.getName().compareTo(o2.field.getName());

        private static class FieldWrapper {
            Field field;
            boolean deprecated;

            public FieldWrapper(@NonNull Field field, boolean deprecated) {
                this.field = field;
                this.deprecated = deprecated;
            }
        }

        private String getEnvironmentPathInfo() {
            List<String> pathList = IOUtil.environmentPathList();
            return TextUtils.join("\n", pathList);
        }

        private String getSystemPropertiesInfo() {
            List<String> list = new ArrayList<>();
            Properties properties = System.getProperties();
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String key = (String) propertyNames.nextElement();
                list.add(key + ": " + properties.getProperty(key));
            }
            Collections.sort(list);
            return TextUtils.join("\n", list);
        }

        private String getVariablesInfo() {
            List<String> list;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                list = getShellVariables();
            } else {
                list = getSystemEnvList();
            }
            Collections.sort(list);
            return TextUtils.join("\n", list);
        }

        private List<String> getShellVariables() {
            List<String> list = IOUtil.getShellVariables();
            List<String> variableList = new ArrayList<>();
            for (String variable : list) {
                variableList.add(variable.replaceFirst("=", ": "));
            }
            Collections.sort(variableList);
            return variableList;
        }

        private List<String> getSystemEnvList() {
            List<String> list = new ArrayList<>();
            Map<String, String> map = System.getenv();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                list.add(entry.getKey() + ": " + entry.getValue());
            }
            return list;
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}