package com.su.workbox.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ThreadUtil {

    private static final String TAG = ThreadUtil.class.getSimpleName();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sSdfMap = new HashMap<>();
    private static Map<String, ThreadLocal<DecimalFormat>> sDecimalFormatMap = new HashMap<>();

    public static SimpleDateFormat getSimpleDateFormat(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sSdfMap.get(pattern);
        if (tl == null) {
            tl = new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    Log.d(TAG, TimeZone.getDefault().getID());
                    Log.d(TAG, Locale.getDefault().getDisplayName());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                    simpleDateFormat.setTimeZone(TimeZone.getDefault());
                    return simpleDateFormat;
                }
            };
            sSdfMap.put(pattern, tl);
        }
        return tl.get();
    }

    public static DecimalFormat getDecimalFormat(final String pattern) {
        ThreadLocal<DecimalFormat> tl = sDecimalFormatMap.get(pattern);
        if (tl == null) {
            tl = new ThreadLocal<DecimalFormat>() {
                @Override
                protected DecimalFormat initialValue() {
                    return new DecimalFormat(pattern);
                }
            };
            sDecimalFormatMap.put(pattern, tl);
        }
        return tl.get();
    }
}
