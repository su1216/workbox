package com.su.workbox.ui.wifi;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by su on 2019/9/14.
 */
public class LanDeviceListActivity extends BaseAppCompatActivity {

    public static final String TAG = LanDeviceListActivity.class.getSimpleName();
    private ExecutorService mExecutorService;
    private WifiManager mWifiManager;
    private List<LanDevice> mLanDeviceList = new ArrayList<>();
    private LanDevice mMeLanDevice;
    private Map<String, String> mMacMap = new HashMap<>();
    private NetworkChangeReceiver mReceiver;
    private Handler mHandler;
    private TextView mDeviceView;
    private TextView mDeviceIpView;
    private TextView mDeviceMacView;
    private TextView mSsidView;
    private TextView mStrengthView;
    private TextView mSpeedView;
    private TextView mFrequencyView;
    private TextView mRouterView;
    private TextView mRouterIpView;
    private TextView mRouterMacView;
    private TextView mDnsView;
    private TextView mMaskView;
    private String mIp;
    private String mRouterIp;
    private RecyclerView mRecyclerView;
    private LanDeviceAdapter mAdapter;
    private boolean mIsWifiConnected;

    static class LanDeviceHandler extends Handler {
        static final int MSG_ADD = 0;
        private final WeakReference<LanDeviceListActivity> mActivity;

        LanDeviceHandler(LanDeviceListActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LanDeviceListActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_ADD:
                        LanDevice device = (LanDevice) msg.obj;
                        if (device.isSelf()) {
                            activity.mMeLanDevice = device;
                        } else {
                            activity.mLanDeviceList.add(device);
                            activity.mAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class NetworkChangeReceiver extends BroadcastReceiver {
        private LanDeviceListActivity mActivity;

        public NetworkChangeReceiver(LanDeviceListActivity activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (parcelableExtra != null) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    mActivity.mIsWifiConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    Log.i(TAG, "isConnected: " + mActivity.mIsWifiConnected);
                    if (mActivity.mIsWifiConnected) {
                        mActivity.refresh();
                    } else {
                        if (mActivity.mExecutorService != null) {
                            mActivity.mExecutorService.shutdownNow();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_lan_device);
        mHandler = new LanDeviceHandler(this);
        mReceiver = new NetworkChangeReceiver(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initViews();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    private void initViews() {
        mDeviceView = findViewById(R.id.device);
        mDeviceIpView = findViewById(R.id.device_ip);
        mDeviceMacView = findViewById(R.id.device_mac);
        mSsidView = findViewById(R.id.ssid);
        mStrengthView = findViewById(R.id.strength);
        mSpeedView = findViewById(R.id.speed);
        mFrequencyView = findViewById(R.id.frequency);
        mRouterView = findViewById(R.id.router);
        mRouterIpView = findViewById(R.id.router_ip);
        mRouterMacView = findViewById(R.id.router_mac);
        mDnsView = findViewById(R.id.dns);
        mMaskView = findViewById(R.id.mask);
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new LanDeviceAdapter(mLanDeviceList);
        mRecyclerView.setAdapter(mAdapter);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            findViewById(R.id.dns).setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mFrequencyView.setVisibility(View.GONE);
        }
    }

    private void refresh() {
        getRouterIp();
        AppExecutors.getInstance().networkIO().execute(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getDnsServers();
            }

            WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            int ipAddress = connectionInfo.getIpAddress();
            mIp = Formatter.formatIpAddress(ipAddress);
            String prefix = mIp.substring(0, mIp.lastIndexOf(".") + 1);
            Log.d(TAG, "prefix: " + prefix);
            runOnUiThread(() -> {
                mDeviceIpView.setText(mIp);
                mSsidView.setText(connectionInfo.getSSID().replaceAll("^\"|\"$", ""));
                mStrengthView.setText(connectionInfo.getRssi() + " dBm");
                mSpeedView.setText(connectionInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mFrequencyView.setText(connectionInfo.getFrequency() + " " + WifiInfo.FREQUENCY_UNITS);
                }
            });
            mLanDeviceList.clear();
            mMacMap.clear();
            try {
                mExecutorService = Executors.newFixedThreadPool(16);
                for (int i = 0; i < 256; i++) {
                    String ip = prefix + i;
                    mExecutorService.execute(new ScannerRunnable(mHandler, mIp, ip));
                }
                mExecutorService.shutdown();
                mExecutorService.awaitTermination(5, TimeUnit.MINUTES);

                if (mIsWifiConnected && !isFinishing()) {
                    readArp();
                    fillDeviceMac();
                    runOnUiThread(() -> {
                        mDeviceMacView.setText(mMacMap.get(mIp));
                        mRouterMacView.setText(mMacMap.get(mRouterIp));
                    });
                } else {
                    mLanDeviceList.clear();
                    mMacMap.clear();
                    runOnUiThread(() -> mAdapter.notifyDataSetChanged());
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Lan Devices");
    }

    private void fillDeviceMac() {
        for (LanDevice device : mLanDeviceList) {
            device.setMac(mMacMap.get(device.getIp()));
            Log.d(TAG, "device: " + device);
        }
        if (mMeLanDevice != null) {
            mMeLanDevice.setMac(mMacMap.get(mIp));
        }
        Log.d(TAG, "device: " + mMeLanDevice);
    }

    private void getRouterIp() {
        final DhcpInfo dhcp = mWifiManager.getDhcpInfo();
        mRouterIp = Formatter.formatIpAddress(dhcp.gateway);
        final String mask = Formatter.formatIpAddress(dhcp.netmask);
        mRouterIpView.setText(mRouterIp);
        mMaskView.setText("Mask: " + mask);
        Log.d(TAG, "route address: " + mRouterIp);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getDnsServers() {
        List<InetAddress> servers = new ArrayList<>();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivityManager == null ? null : new Network[]{connectivityManager.getActiveNetwork()};
        if (networks == null) {
            return;
        }
        int length = networks.length;
        for (int i = 0; i < length; ++i) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(networks[i]);
            if (linkProperties != null) {
                servers.addAll(linkProperties.getDnsServers());
            }
        }

        for (InetAddress server : servers) {
            runOnUiThread(() -> mDnsView.setText("DNS: " + server.getHostAddress()));
            Log.d(TAG, "DNS server: " + server.getHostName() + " (" + server.getHostAddress() + ")");
        }
    }

    //read mac from arp
    private void readArp() {
        String arp = AppHelper.shellExec("/bin/sh", "-c", "cat /proc/net/arp | sort | sed '$ d'");
        Scanner scanner = new Scanner(arp);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (TextUtils.isEmpty(line)) {
                continue;
            }
            String[] results = line.split("\\s+");
            if (results.length >= 4 && !TextUtils.equals("00:00:00:00:00:00", results[3])) {
                mMacMap.put(results[0], results[3]);
                LanDevice device = new LanDevice();
                device.setIp(results[0]);
                device.setMac(results[3]);
                if (!contains(device.getIp())) {
                    mLanDeviceList.add(device);
                    runOnUiThread(() -> mAdapter.notifyItemInserted(mLanDeviceList.size() - 1));
                }
            } else if (!TextUtils.equals("00:00:00:00:00:00", results[3])) {
                Log.d(TAG, "wrong result: " + line);
            }
        }
        if (TextUtils.isEmpty(mMacMap.get(mIp))) {
            mMacMap.put(mIp, NetworkUtil.getMacAddress());
        }
        Collections.sort(mLanDeviceList, (o1, o2) -> o1.getIp().compareTo(o2.getIp()));
        runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        scanner.close();
    }

    private boolean contains(String ip) {
        for (LanDevice device : mLanDeviceList) {
            if (TextUtils.equals(device.getIp(), ip)) {
                return true;
            }
        }
        return false;
    }

    private static class LanDeviceAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

        private List<LanDevice> mLanDeviceList;

        LanDeviceAdapter(@NonNull List<LanDevice> lanDeviceList) {
            this.mLanDeviceList = lanDeviceList;
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == BaseRecyclerAdapter.ITEM_TYPE_NORMAL) {
                LanDevice lanDevice = mLanDeviceList.get(position - 1);
                TextView ipView = holder.getView(R.id.ip);
                TextView macView = holder.getView(R.id.mac);
                TextView deviceView = holder.getView(R.id.device);
                ipView.setText(lanDevice.getIp());
                macView.setText(lanDevice.getMac());
                String hostName = lanDevice.getHostName();
                if (hostName == null) {
                    hostName = "";
                }
                deviceView.setText(hostName.replaceFirst("\\.lan$", ""));
            }
        }

        private int getLayoutId(int itemType) {
            if (itemType == BaseRecyclerAdapter.ITEM_TYPE_HEADER) {
                return R.layout.workbox_item_lan_device_title;
            } else {
                return R.layout.workbox_item_lan_device;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return BaseRecyclerAdapter.ITEM_TYPE_HEADER;
            }
            return BaseRecyclerAdapter.ITEM_TYPE_NORMAL;
        }

        @Override
        public int getItemCount() {
            return mLanDeviceList.size() + 1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
