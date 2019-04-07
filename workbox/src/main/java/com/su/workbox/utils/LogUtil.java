package com.su.workbox.utils;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();

    public static void logHeaders(@NonNull Cursor cursor) {
        Log.d(TAG, "----- logHeaders size: " + cursor.getCount() + "  -----");
        int columnCount = cursor.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Log.d(TAG, cursor.getColumnName(i));
        }
    }
}
