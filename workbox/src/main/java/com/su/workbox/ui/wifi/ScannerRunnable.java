package com.su.workbox.ui.wifi;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.NetworkUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ScannerRunnable implements Runnable {

    public static final String TAG = ScannerRunnable.class.getSimpleName();
    private String mIp;
    private String mApIp = NetworkUtil.getIpv4Address();
    private String mSelf;
    private Handler mHandler;

    public ScannerRunnable(@NonNull Handler handler, @NonNull String self, @NonNull String ip) {
        this.mHandler = handler;
        this.mSelf = self;
        this.mIp = ip;
    }

    private static boolean doPing(String ip) {
        Log.d(TAG, "doPing ip: " + ip);
        BufferedReader bufferedReader = null;
        Runtime runtime = Runtime.getRuntime();
        boolean result = false;
        try {
            String command = "ping -c 1 " + ip;
            bufferedReader = new BufferedReader(new InputStreamReader(runtime.exec(command).getInputStream()), 512);

            int i = 0;
            do {
                if (bufferedReader.ready()) {
                    break;
                }
                Thread.sleep(50);
                i++;
            } while (i <= 60);
            if (i <= 60) {
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    if (readLine.contains(" 1 received")) {
                        Log.i(TAG, "online ip: " + ip);
                        result = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "ip: " + ip, e);
        } catch (InterruptedException e) {
            Log.e(TAG, "ip: " + ip, e);
        } finally {
            IOUtil.closeQuietly(bufferedReader);
        }
        return result;
    }

    @Override
    public void run() {
        boolean result;
        if (TextUtils.equals(mSelf, mIp) || TextUtils.equals(mApIp, mIp)) {
            result = true;
        } else {
            result = doPing(mIp);
        }
        if (result) {
            try {
                InetAddress[] ias = InetAddress.getAllByName(mIp);
                if (ias.length > 0) {
                    String hostName = ias[0].getHostName();
                    LanDevice device = makeDevice(hostName);
                    sendMsg(device);
                }
            } catch (UnknownHostException e) {
                Log.w(TAG, e);
            }
            return;
        }
        try {
            InetAddress address = InetAddress.getByName(mIp);
            boolean reachable = address.isReachable(1000);
            String hostName = address.getHostName();
            Log.d(TAG, "reachable ip: " + mIp);
            if (reachable) {
                Log.i(TAG, "reachable online ip: " + mIp);
                LanDevice device = makeDevice(hostName);
                sendMsg(device);
            }
        } catch (IOException e) {
            Log.d(TAG, "ip: " + mIp, e);
        }
    }

    private LanDevice makeDevice(String hostName) {
        LanDevice device = new LanDevice();
        device.setIp(mIp);
        device.setHostName(hostName);
        if (TextUtils.equals(mSelf, mIp)) {
            device.setSelf(true);
        }
        return device;
    }

    private void sendMsg(LanDevice device) {
        Message message = Message.obtain(mHandler);
        message.what = LanDeviceListActivity.LanDeviceHandler.MSG_ADD;
        message.obj = device;
        mHandler.sendMessage(message);
    }
}
