package com.su.workbox.ui.log.crash;

import android.os.Process;
import android.util.Log;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.utils.GeneralInfoHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CrashLogHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CrashLogHandler.class.getSimpleName();
    private static final int MAX_LINES = 1024;
    private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private final CrashLogRecordSource mCrashLogRecordSource;
    private boolean mKillProcess;

    public CrashLogHandler(boolean killProcess) {
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        HttpDataDatabase database = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        mCrashLogRecordSource = CrashLogRecordSource.getInstance(database.crashLogRecordDao());
        mKillProcess = killProcess;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        CrashLogRecord record = new CrashLogRecord();
        record.setPid(GeneralInfoHelper.getProcessId());
        record.setTime(System.currentTimeMillis());
        String content = Log.getStackTraceString(e);
        record.setContent(truncate(content));
        int index = content.indexOf("\n");
        if (index >= 0) {
            record.setFirstLine(content.substring(0, index));
        }
        mCrashLogRecordSource.insertCrashLogRecords(record);
        if (mDefaultExceptionHandler != null) {
            mDefaultExceptionHandler.uncaughtException(t, e);
        }
        if (mKillProcess) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Log.w(TAG, ex);
            }
            Process.killProcess(Process.myPid());
        }
    }

    private static String truncate(String content) {
        BufferedReader reader = new BufferedReader(new StringReader(content));
        StringBuilder sb = new StringBuilder();
        String str;
        try {
            int count = 0;
            while ((str = reader.readLine()) != null) {
                if (count > MAX_LINES) {
                    break;
                }
                count++;
                sb.append(str);
                sb.append("\n");
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
        return sb.toString();
    }

    public void unregister() {
        Thread.setDefaultUncaughtExceptionHandler(mDefaultExceptionHandler);
    }
}
