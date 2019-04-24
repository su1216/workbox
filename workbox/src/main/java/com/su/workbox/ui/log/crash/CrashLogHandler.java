package com.su.workbox.ui.log.crash;

import android.os.Process;
import android.util.Log;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.utils.GeneralInfoHelper;

public class CrashLogHandler implements Thread.UncaughtExceptionHandler {

    private final CrashLogRecordSource mCrashLogRecordSource;
    private boolean mKillProcess;

    public CrashLogHandler(boolean killProcess) {
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
        record.setContent(content);
        int index = content.indexOf("\n");
        if (index >= 0) {
            record.setFirstLine(content.substring(0, index));
        }
        mCrashLogRecordSource.insertCrashLogRecords(record);
        if (mKillProcess) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                //ignore
            }
            Process.killProcess(Process.myPid());
        }
    }
}
