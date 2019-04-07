package com.su.workbox.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.SystemInfo;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.TelephonyManagerWrapper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mahao on 17-5-27.
 * 调试功能列表 - 手机信息
 */
public class DeviceInfoActivity extends BaseAppCompatActivity {

    private static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private List<SystemInfo> mData = new ArrayList<>();
    private Resources mResources;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_system_info);
        mResources = getResources();
        RecyclerView recyclerView = findViewById(R.id.recycler);
        initData();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemSpaceDecoration(3, 30));
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置控件显示的顺序
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //设置控件显示间隔时间
        controller.setDelay(0.5f);
        recyclerView.setLayoutAnimation(controller);
        MyAdapter adapter = new MyAdapter(mData);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("设备信息");
    }

    private class MyAdapter extends BaseRecyclerAdapter<SystemInfo> {

        private MyAdapter(List<SystemInfo> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_system_info;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, final int position, int itemType) {
            ((TextView) holder.itemView).setText(getData().get(position).getTitle());
            holder.itemView.setOnClickListener(v ->
                    new AlertDialog.Builder(DeviceInfoActivity.this)
                            .setTitle(getData().get(position).getTitle())
                            .setMessage(getData().get(position).getDesc())
                            .setNegativeButton(R.string.workbox_close, null)
                            .setPositiveButton(R.string.workbox_share, (dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, getData().get(position).getDesc());
                                intent.setType("text/plain");
                                startActivity(Intent.createChooser(intent, "分享到"));
                            })
                            .show()
            );
        }
    }

    private void initData() {
        mData.add(getScreenInfo());
        mData.add(getSystemInfo());
        mData.add(getNetWorkInfo());
        mData.add(getHardwareInfo());
        mData.add(getPhoneId());
        mData.add(getFeatureList());
        if (PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName())
                && AppHelper.isPhone(this)) {
            mData.add(getTelephonyInfo());
        }
    }

    @SuppressLint("HardwareIds")
    private SystemInfo getPhoneId() {
        boolean isHasPermission = PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName());
        SystemInfo info = new SystemInfo();
        info.setTitle("本机ID");
        String desc = "";
        desc += "Android ID: " + GeneralInfoHelper.getAndroidId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            desc += "\n\n" + "设备序列号: " + (isHasPermission ? Build.getSerial() : "未授权");
        } else {
            desc += "\n\n" + "设备序列号: " + Build.SERIAL;
        }
        desc += "\n\n" + "FINGERPRINT: " + Build.FINGERPRINT;
        info.setDesc(desc);
        return info;
    }

    public SystemInfo getHardwareInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("硬件");
        String desc = "";
        desc += "手机: " + (AppHelper.isPhone(this) ? "是" : "否");
        desc += "\n\n" + "型号: " + Build.MODEL;
        desc += "\n\n" + "制造商: " + Build.MANUFACTURER;
        desc += "\n\n" + "主板: " + Build.BOARD;
        desc += "\n\n" + "设备: " + Build.DEVICE;
        desc += "\n\n" + "产品: " + Build.PRODUCT;
        desc += "\n\n" + "CPU 核数: " + Integer.toString(Runtime.getRuntime().availableProcessors());
        desc += "\n\n" + "CPU 位数: " + SystemInfoHelper.getCpuBit();
        desc += "\n\n" + "CPU 型号: " + SystemInfoHelper.getCpuName();
        desc += "\n\n" + "ABIs: " + TextUtils.join(", ", new String[]{Build.CPU_ABI, Build.CPU_ABI2});
        desc += "\n\n" + "存储: 共"
                + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalExternalMemorySize(), false)
                + "," + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableExternalMemorySize(), false)
                + "可用";
        desc += "\n\n" + "内存: 共"
                + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalMemorySize(), false)
                + "," + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableMemory(this), false)
                + "可用";
        info.setDesc(desc);
        return info;
    }

    private SystemInfo getNetWorkInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("网络相关");
        String desc = "";
        String networkType = SystemInfoHelper.getNetworkType(this);
        String ssid = "";
        desc += "网络: " + networkType;
        if ("Wifi".equals(networkType)) {
            WifiManager mgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (mgr != null) {
                WifiInfo wifiInfo = mgr.getConnectionInfo();
                ssid = wifiInfo.getSSID();
            }
        }
        desc += "\n\n" + "Wifi SSID: " + ssid;
        desc += "\n\n" + "IPv4: " + NetworkUtil.getIpv4Address();
        desc += "\n\n" + "IPv6: " + NetworkUtil.getIpv6Address();
        desc += "\n\n" + "Mac地址: " + NetworkUtil.getMacAddress();
        info.setDesc(desc);
        return info;
    }

    private SystemInfo getSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("系统");
        String desc = "";
        desc += "Android " + Build.VERSION.RELEASE + " / " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + " / " + "API " + Build.VERSION
                .SDK_INT;
        desc += "\n\n" + "系统类型: " + Build.TYPE;
        desc += "\n\n" + "基带版本: " + Build.getRadioVersion();
        desc += "\n\n" + "Linux 内核版本: " + System.getProperty("os.version");
        desc += "\n\n" + "Http User Agent: " + System.getProperty("http.agent");
        info.setDesc(desc);
        return info;
    }

    public SystemInfo getScreenInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("屏幕信息");
        Configuration config = mResources.getConfiguration();
        DisplayMetrics metrics = mResources.getDisplayMetrics();
        int widthPixels = GeneralInfoHelper.getScreenWidth();
        int heightPixels = GeneralInfoHelper.getScreenHeight();
        String desc = "分辨率: " + widthPixels + " x " + heightPixels + " px"
                + " / " + UiHelper.px2dp(widthPixels) + " x " + UiHelper.px2dp(heightPixels) + " dp";
        desc += "\n\nsmallestWidth: " + config.smallestScreenWidthDp + " dp";
        desc += "\n\n状态栏高度: " + GeneralInfoHelper.getStatusBarHeight() + " px";
        int navigationBarHeight = UiHelper.getNavigationBarHeight(this);
        if (navigationBarHeight > 0) {
            desc += "\n\n导航栏高度: " + navigationBarHeight + " px";
        }

        Point point = UiHelper.getRealScreenSize(this);
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
        info.setDesc(desc);
        return info;
    }

    public SystemInfo getFeatureList() {
        String openGlEsName = "OpenGL ES";
        SystemInfo info = new SystemInfo();
        info.setTitle("Feature列表");

        //array -> list -> sort -> display
        FeatureInfo[] featureInfoArray = getPackageManager().getSystemAvailableFeatures();
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
        info.setDesc(stringBuilder.toString());
        return info;
    }

    public SystemInfo getTelephonyInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("电话相关");
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyManagerWrapper wrapper = new TelephonyManagerWrapper(manager);
        int count = wrapper.getPhoneCount();

        StringBuilder builder = new StringBuilder();
        builder.append("多卡: " + (count > 1 ? "true" : "false") + "\n");
        builder.append("当前网络: " + wrapper.getNetworkTypeName() + "\n\n");
        for (int i = 0; i < count; i++) {
            if (!wrapper.isSimCardPresent(i)) {
                continue;
            }

            int slot = i + 1;
            String indexHolder = count > 1 ? " " + slot : "";
            int phoneType = wrapper.getPhoneType(slot);
            if (phoneType == TelephonyManager.PHONE_TYPE_CDMA || phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    String meid = wrapper.getMeid(slot);
                    builder.append(wrapper.field2String("网络制式", indexHolder, "CDMA") + "\n");
                    builder.append(wrapper.field2String("meid", indexHolder, meid) + "\n");
                } else {
                    String imei = wrapper.getImei(slot);
                    builder.append(wrapper.field2String("网络制式", indexHolder, "GSM") + "\n");
                    builder.append(wrapper.field2String("imei", indexHolder, imei) + "\n");
                }
            }
            String subscriberId = wrapper.getSubscriberId(slot);
            builder.append(wrapper.field2String("Subscriber Id", indexHolder, subscriberId) + "\n");
            String sv = wrapper.getDeviceSoftwareVersion(slot);
            builder.append(wrapper.field2String("sv", indexHolder, sv) + "\n");
            String phoneNumber = wrapper.getLine1Number(slot);
            builder.append(wrapper.field2String("电话号码", indexHolder, phoneNumber) + "\n");
            String simSerialNumber = wrapper.getSimSerialNumber(slot);
            builder.append(wrapper.field2String("SIM序列号", indexHolder, simSerialNumber) + "\n");

            String networkCountryIso = wrapper.getNetworkCountryIso(slot);
            builder.append(wrapper.field2String("国家代码", indexHolder, networkCountryIso) + "\n");

            String operatorName = wrapper.getNetworkOperatorName(slot);
            builder.append(wrapper.field2String("运营商名称", indexHolder, operatorName) + "\n\n");
        }
        builder.deleteCharAt(builder.length() - 2);
        info.setDesc(builder.toString());
        return info;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
