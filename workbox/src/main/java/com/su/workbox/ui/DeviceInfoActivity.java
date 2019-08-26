package com.su.workbox.ui;

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
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.utils.SensorUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.TelephonyManagerWrapper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mahao on 17-5-27.
 * 调试功能列表 - 手机信息
 */
public class DeviceInfoActivity extends PermissionRequiredActivity {

    private static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private static final int REQUEST_PHONE_CODE = 1;
    private static final int REQUEST_IDS_CODE = 2;
    private static final String KEY_SCREEN = "screen";
    private static final String KEY_SYSTEM = "system";
    private static final String KEY_NETWORK = "network";
    private static final String KEY_HARDWARE = "hardware";
    private static final String KEY_IDS = "ids";
    private static final String KEY_FEATURES = "features";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SENSOR = "sensor";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_INPUT_METHOD = "input_method";
    private List<SystemInfo> mData = new ArrayList<>();

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
                            statusString = "充电中 (USB)";
                        } else {
                            statusString = "充电中 (AC)";
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
                int health =intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
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
                mBatteryInfo = "状态: " + statusString + "\n"
                        + "类型: " + technology + "\n"
                        + "健康: " + healthString + "\n"
                        + "电量: " + level * 100 / scale + "%\n"
                        + "电压: " + voltage + " V\n"
                        + "温度: " + temperature + " °C\n"
                        + "容量: " + ReflectUtil.getBatteryCapacity(context) + " mA";
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_system_info);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        initData();
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemSpaceDecoration(3, 30));
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置控件显示的顺序
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //设置控件显示间隔时间
        controller.setDelay(0.35f);
        recyclerView.setLayoutAnimation(controller);
        MyAdapter adapter = new MyAdapter(this, mData);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("设备信息");
        permissionRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_IDS_CODE);
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
        mData.add(new SystemInfo(KEY_INPUT_METHOD, "输入法"));
    }

    @Override
    public AlertDialog makeHintDialog(String permission, int requestCode) {
        if (REQUEST_IDS_CODE == 1) {
            return makePhonePermissionHintDialog();
        }
        return makeIdsPermissionHintDialog();
    }

    private AlertDialog makePhonePermissionHintDialog() {
        return new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage("授予访问设备电话权限将可查看更多电话相关信息")
                .setPositiveButton(R.string.workbox_set_permission, (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null))))
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private AlertDialog makeIdsPermissionHintDialog() {
        return new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage("授予访问设备电话权限将可查设备序列号")
                .setPositiveButton(R.string.workbox_set_permission, (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null))))
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private static class MyAdapter extends BaseRecyclerAdapter<SystemInfo> {

        private DeviceInfoActivity mActivity;
        private PackageManager mPackageManager;

        private MyAdapter(DeviceInfoActivity activity, List<SystemInfo> data) {
            super(data);
            mActivity = activity;
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
                        if (KEY_PHONE.equals(systemInfo.getKey())) {
                            if (PackageManager.PERMISSION_GRANTED != mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName())) {
                                mActivity.permissionRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE_CODE);
                                return;
                            }
                        } else if (KEY_IDS.equals(systemInfo.getKey())) {
                            if (PackageManager.PERMISSION_GRANTED != mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName())) {
                                mActivity.permissionRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_IDS_CODE);
                            }
                        }
                        new AlertDialog.Builder(mActivity)
                                .setTitle(systemInfo.getTitle())
                                .setMessage(getMsg(systemInfo.getKey()))
                                .setNegativeButton(R.string.workbox_close, null)
                                .setPositiveButton(R.string.workbox_share, (dialog, which) -> {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, getData().get(position).getDesc());
                                    intent.setType("text/plain");
                                    mActivity.startActivity(Intent.createChooser(intent, "分享到"));
                                })
                                .show();
                    }
            );
        }

        private String getMsg(@NonNull String key) {
            switch (key) {
                case KEY_SCREEN:
                    return getScreenInfo();
                case KEY_SYSTEM:
                    return getSystemInfo();
                case KEY_NETWORK:
                    return getNetWorkInfo();
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
                case KEY_INPUT_METHOD:
                    return getInstalledInputMethod();
                default:
                    throw new IllegalArgumentException("can not find any info about key: " + key);
            }
        }

        @SuppressLint("HardwareIds")
        private String getPhoneId() {
            boolean isHasPermission = PackageManager.PERMISSION_GRANTED == mPackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE, mActivity.getPackageName());
            String desc = "Android ID: " + GeneralInfoHelper.getAndroidId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            desc += "\n\n" + "ABIs: " + TextUtils.join(", ", new String[]{Build.CPU_ABI, Build.CPU_ABI2});
            desc += "\n\n" + "存储: 共 "
                    + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalExternalMemorySize())
                    + ", 可用 " + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableExternalMemorySize());
            desc += "\n\n" + "内存: 共 "
                    + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalMemorySize(mActivity))
                    + ", 可用 " + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableMemory(mActivity));
            return desc;
        }

        private String getNetWorkInfo() {
            String networkType = SystemInfoHelper.getNetworkType(mActivity);
            String ssid = "";
            String desc = "网络: " + networkType;
            if ("Wifi".equals(networkType)) {
                WifiManager mgr = (WifiManager) mActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
                if (mgr != null) {
                    WifiInfo wifiInfo = mgr.getConnectionInfo();
                    ssid = wifiInfo.getSSID();
                }
            }
            desc += "\n\n" + "Wifi SSID: " + ssid;
            desc += "\n\n" + "IPv4: " + NetworkUtil.getIpv4Address();
            desc += "\n\n" + "IPv6: " + NetworkUtil.getIpv6Address();
            desc += "\n\n" + "Mac地址: " + NetworkUtil.getMacAddress();
            return desc;
        }

        private String getSystemInfo() {
            String desc = "Android " + Build.VERSION.RELEASE + " / " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + " / " + "API " + Build.VERSION.SDK_INT;
            desc += "\n\n" + "系统类型: " + Build.TYPE;
            desc += "\n\n" + "基带版本: " + Build.getRadioVersion();
            desc += "\n\n" + "Linux 内核版本: " + System.getProperty("os.version");
            desc += "\n\n" + "Http User Agent: " + System.getProperty("http.agent");
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
            builder.append("多卡: " + (count > 1 ? "true" : "false") + "\n");
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
                        String meid = wrapper.getMeid(i);
                        builder.append(wrapper.field2String("网络制式", "CDMA") + "\n");
                        builder.append(wrapper.field2String("meid", meid) + "\n");
                    } else {
                        String imei = wrapper.getImei(i);
                        builder.append(wrapper.field2String("网络制式", "GSM") + "\n");
                        builder.append(wrapper.field2String("imei", imei) + "\n");
                    }
                }
                String subscriberId = wrapper.getSubscriberId(i);
                builder.append(wrapper.field2String("Subscriber Id", subscriberId) + "\n");
                String sv = wrapper.getDeviceSoftwareVersion(i);
                builder.append(wrapper.field2String("sv", sv) + "\n");
                String phoneNumber = wrapper.getLine1Number(i);
                builder.append(wrapper.field2String("电话号码", phoneNumber) + "\n");
                String simSerialNumber = wrapper.getSimSerialNumber(i);
                builder.append(wrapper.field2String("SIM序列号", simSerialNumber) + "\n");

                String networkCountryIso = wrapper.getNetworkCountryIso(i);
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
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
