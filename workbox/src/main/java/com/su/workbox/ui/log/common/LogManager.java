package com.su.workbox.ui.log.common;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class LogManager {

    public static final String TAG = LogManager.class.getSimpleName();
    private static final int MSG_ADD = 0;
    private static final int MSG_CLEAR = 1;
    private OnLogChangedListener mOnLogChangedListener;

    private LogHandler mLogHandler;
    private Handler mHandler;

    static class LogManagerHandler extends Handler {
        private final WeakReference<LogManager> mLogManager;

        LogManagerHandler(LogManager manager) {
            mLogManager = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LogManager manager = mLogManager.get();
            switch (msg.what) {
                case MSG_ADD:
                    manager.mOnLogChangedListener.onAdded((LogRecord) msg.obj);
                    break;
                case MSG_CLEAR:
                    manager.mOnLogChangedListener.onClear();
                    break;
                default:
                    break;
            }
        }
    }

    private LogManager() {
        mHandler = new LogManagerHandler(this);
    }

    public static LogManager getInstance() {
        return new LogManager();
    }

    public void start(String[] tags, String level, String query, boolean useRegex, boolean clear, @NonNull OnLogChangedListener listener) {
        if (mLogHandler != null) {
            mLogHandler.stop();
        }
        mOnLogChangedListener = listener;
        HandlerThread thread = new HandlerThread("LogManager");
        thread.start();
        mLogHandler = new LogHandler(thread.getLooper(), mHandler, tags, level, query, useRegex, clear);
        mLogHandler.start();
    }

    public void restart(String[] tags, String level, String query, boolean useRegex, boolean clear, @NonNull OnLogChangedListener listener) {
        stop();
        start(tags, level, query, useRegex, clear, listener);
    }

    public void stop() {
        mOnLogChangedListener.onClear();
        if (mLogHandler != null) {
            mLogHandler.stop();
            mLogHandler = null;
        }
    }

    interface OnLogChangedListener {
        void onAdded(@NonNull LogRecord log);

        void onClear();
    }
}
