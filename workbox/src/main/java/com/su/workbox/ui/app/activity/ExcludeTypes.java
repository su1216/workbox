package com.su.workbox.ui.app.activity;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ExcludeTypes {

    private static final List<Class<?>> DEFAULT_EXCLUDE_TYPES = new ArrayList<>();
    private static boolean sIncludeSubType = true;

    static {
        DEFAULT_EXCLUDE_TYPES.add(Bitmap.class);
    }

    public static void addExcludeType(Class<?> clazz) {
        if (!DEFAULT_EXCLUDE_TYPES.contains(clazz)) {
            DEFAULT_EXCLUDE_TYPES.add(clazz);
        }
    }

    public static void includeSubType(boolean include) {
        sIncludeSubType = include;
    }

    public static boolean exclude(@NonNull Class<?> clazz) {
        boolean exclude = false;
        for (Class<?> c : DEFAULT_EXCLUDE_TYPES) {
            if (sIncludeSubType && c.isAssignableFrom(clazz)) {
                exclude = true;
            } else if (clazz == c) {
                exclude = true;
            }
        }
        return exclude;
    }
}
