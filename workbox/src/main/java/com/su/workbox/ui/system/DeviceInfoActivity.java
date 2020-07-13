package com.su.workbox.ui.system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.SystemInfo;
import com.su.workbox.shell.ShellUtil;
import com.su.workbox.ui.base.PermissionRequiredActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.TelephonyManagerWrapper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by mahao on 17-5-27.
 * 调试功能列表 - 手机信息
 */
public class DeviceInfoActivity extends PermissionRequiredActivity {

    private static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private static final int REQUEST_PHONE_CODE = 1;
    private static final int REQUEST_IDS_CODE = 2;
    private static final int REQUEST_NETWORK_CODE = 3;
    private static final String KEY_NETWORK = "network";
    private static final String KEY_HARDWARE = "hardware";
    private static final String KEY_IDS = "ids";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ENVIRONMENT_PATH = "environment_path";
    private static final String KEY_PROPERTIES = "system_properties";
    private static final String KEY_SHELL_SET = "shell_set";
    private List<SystemInfo> mData = new ArrayList<>();

    private InfoAdapter mAdapter;
    private String mIp;

    public static Intent getLaunchIntent(@NonNull Context context) {
        return new Intent(context, DeviceInfoActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        initData();
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

    private void initData() {
        mData.add(new SystemInfo(KEY_NETWORK, "网络"));
        mData.add(new SystemInfo(KEY_HARDWARE, "硬件"));
        mData.add(new SystemInfo(KEY_IDS, "ID"));
        if (AppHelper.isPhone(this)) {
            mData.add(new SystemInfo(KEY_PHONE, "电话"));
        }
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
                case KEY_NETWORK:
                    return getNetworkInfo();
                case KEY_HARDWARE:
                    return getHardwareInfo();
                case KEY_IDS:
                    return getPhoneId();
                case KEY_PHONE:
                    return getTelephonyInfo();
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

        private String getEnvironmentPathInfo() {
            List<String> pathList = ShellUtil.environmentPathList();
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
            List<String> list = ShellUtil.getShellVariables();
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
