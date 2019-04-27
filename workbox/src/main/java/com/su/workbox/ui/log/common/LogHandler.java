package com.su.workbox.ui.log.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.utils.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogHandler extends Handler {

    public static final String TAG = LogHandler.class.getSimpleName();
    private static final Pattern PATTERN_TAG = Pattern.compile("^(\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\d+)\\s+(\\d+)\\s+(\\w)\\s+([^\\s:]+).*");
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;

    private volatile boolean isRunning = true;
    private String[] mTags;
    private String mLevel;
    private String mQuery;
    private boolean mUseRegex;
    private boolean mClear;
    private Pattern mPattern;
    private Handler mHandler;

    LogHandler(Looper looper, Handler handler, String[] tags, String level, String query, boolean useRegex, boolean clear) {
        super(looper);
        mHandler = handler;
        mTags = tags;
        mLevel = level;
        mQuery = query;
        mUseRegex = useRegex;
        mClear = clear;
        if (mUseRegex && !TextUtils.isEmpty(query)) {
            mPattern = Pattern.compile(query);
        }
    }

    void start() {
        sendEmptyMessage(MSG_START);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START:
                isRunning = true;
                process();
                break;
            case MSG_STOP:
                isRunning = false;
                break;
            default:
                break;
        }
    }

    private void process() {
        InputStream is;
        InputStreamReader reader;
        BufferedReader br = null;
        try {
            if (mClear) {
                Runtime.getRuntime().exec("logcat -c");
                mClear = false;
            }
            Process process = Runtime.getRuntime().exec(makeCommand());
            is = process.getInputStream();
            reader = new InputStreamReader(is);
            br = new BufferedReader(reader);

            String log;
            while ((log = br.readLine()) != null && isRunning) {
                if (!filterLog(log)) {
                    continue;
                }
                Message message = Message.obtain();
                message.obj = parseLogRecord(log);
                mHandler.sendMessage(message);
            }
        } catch (IOException e) {
            Log.e(TAG, "fail to capture logs.", e);
        } finally {
            IOUtil.closeQuietly(br);
        }
    }

    private LogRecord parseLogRecord(String log) {
        LogRecord logRecord = new LogRecord();
        Matcher matcher = PATTERN_TAG.matcher(log);
        if (matcher.find()) {
            logRecord.setDate(matcher.group(1));
            logRecord.setPid(matcher.group(2));
            logRecord.setTid(matcher.group(3));
            logRecord.setLevel(matcher.group(4));
            logRecord.setTag(matcher.group(5));
        }
        logRecord.setFull(log);
        return logRecord;
    }

    private boolean filterLog(String log) {
        if (TextUtils.isEmpty(mQuery)) {
            return true;
        }
        if (mUseRegex) {
            Matcher matcher = mPattern.matcher(log);
            return matcher.find();
        } else {
            return log.contains(mQuery);
        }
    }

    private String makeCommand() {
        String command;
        if (isTagEmpty() && TextUtils.isEmpty(mLevel)) {
            command = "logcat -v threadtime";
        } else if (!isTagEmpty() && TextUtils.isEmpty(mLevel)) {
            command = "logcat -v threadtime -s";
            for (String tag : mTags) {
                command += " " + tag;
            }
        } else if (isTagEmpty() && !TextUtils.isEmpty(mLevel)) {
            command = "logcat -v threadtime -s *:" + mLevel;
        } else {
            command = "logcat -v threadtime -s";
            for (String tag : mTags) {
                command += " " + tag + ":" + mLevel;
            }
        }
        return command;
    }

    private boolean isTagEmpty() {
        return mTags == null || mTags.length == 0;
    }

    void stop() {
        isRunning = false;
    }
}
