package com.su.workbox.utils;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-5-31.
 */
public class ReflectUtil {
    private static final String TAG = ReflectUtil.class.getSimpleName();

    public static Object getFieldValue(@NonNull Class<?> clazz, @Nullable Object object, @NonNull String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "object class: " + clazz.getName(), e);
        }
        return null;
    }

    @NonNull
    public static List<Pair<String, String>> getMatchedFlags(@NonNull Class<?> clazz, @NonNull List<Field> fields, int flags) {
        List<Pair<String, String>> list = new ArrayList<>();
        try {
            for (Field field : fields) {
                if (int.class.equals(field.getType())) {
                    int value = field.getInt(clazz);
                    if ((flags & value) == value) {
                        list.add(new Pair<>(field.getName(), String.valueOf(value)));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "object class: " + clazz.getName(), e);
        }
        return list;
    }

    @NonNull
    public static Pair<String, String> getSingleMatchedFlag(@NonNull Class<?> clazz, @NonNull List<Field> fields, int flag) {
        try {
            for (Field field : fields) {
                if (int.class.equals(field.getType())) {
                    int value = field.getInt(clazz);
                    if (flag == value) {
                        return new Pair<>(field.getName(), String.valueOf(value));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "object class: " + clazz.getName(), e);
        }
        return new Pair<>("未知flag", String.valueOf(flag));
    }

    @NonNull
    public static List<Field> getFieldsWithPrefix(Class<?> clazz, String prefix) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (field.getName().startsWith(prefix)) {
                list.add(field);
            }
        }
        return list;
    }

    //检查class是否有默认构造函数
    public static boolean hasDefaultConstructor(@NonNull Class<?> clazz) {
        try {
            clazz.getConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "clazz: " + clazz, e);
            return false;
        }
    }

    public static boolean isUseOkHttp3() {
        return isUse("okhttp3.Call");
    }

    public static boolean isUseRhino() {
        return isUse("org.mozilla.javascript.Context");
    }

    private static boolean isUse(@NonNull String className) {
        return isUse(className, null);
    }

    private static boolean isUse(@NonNull String className, @Nullable String hint) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            if (TextUtils.isEmpty(hint)) {
                Log.i(TAG, "can not found " + className + ". " + e.getMessage());
            } else {
                Log.i(TAG, hint + e.getMessage());
            }
        }
        return false;
    }

    //使用默认无参构造函数返回一个实例
    @Nullable
    public static <T> T newInstance(@NonNull Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.e(TAG, "clazz: " + clazz, e);
        }
        return null;
    }

    //使用默认无参构造函数返回一个实例
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T[] newArrayInstance(Class<T> clazz, int size) {
        if (!clazz.isArray()) {
            return null;
        }

        return (T[]) Array.newInstance(clazz.getComponentType(), size);
    }

    @NonNull
    public static List<String> getActivityFlags(int flags) {
        List<String> list = new ArrayList<>();
        Field[] fields = Intent.class.getFields();
        try {
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                String name = field.getName();
                if (name.startsWith("FLAG_ACTIVITY")
                        && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers)) {
                    int flag = field.getInt(null);
                    if ((flag & flags) == flag) {
                        list.add(name);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Log.d(TAG, "flags: " + flags, e);
        }
        return list;
    }

    @NonNull
    public static String getFullClassName(@NonNull Class<?> clazz) {
        String name = clazz.getName();
        if (name.endsWith("[I")) {
            return name.substring(0, name.length() - 2) + " (int)";
        } else if (name.endsWith("[J")) {
            return name.substring(0, name.length() - 2) + " (long)";
        } else if (name.endsWith("[S")) {
            return name.substring(0, name.length() - 2) + " (short)";
        } else if (name.endsWith("[F")) {
            return name.substring(0, name.length() - 2) + " (float)";
        } else if (name.endsWith("[D")) {
            return name.substring(0, name.length() - 2) + " (double)";
        } else if (name.endsWith("[C")) {
            return name.substring(0, name.length() - 2) + " (char)";
        } else if (name.endsWith("[B")) {
            return name.substring(0, name.length() - 2) + " (byte)";
        } else if (name.endsWith("[Z")) {
            return name.substring(0, name.length() - 2) + " (boolean)";
        } else {
            return name;
        }
    }

    public static boolean isPrimitiveClass(Class clazz) {
        return clazz.equals(int.class)
                || clazz.equals(short.class)
                || clazz.equals(long.class)
                || clazz.equals(double.class)
                || clazz.equals(float.class)
                || clazz.equals(byte.class)
                || clazz.equals(char.class)
                || clazz.equals(boolean.class)
                || clazz.equals(String.class);
    }

    public static boolean isPrimitiveWrapperClass(Class clazz) {
        return clazz.equals(Integer.class)
                || clazz.equals(Short.class)
                || clazz.equals(Long.class)
                || clazz.equals(Double.class)
                || clazz.equals(Float.class)
                || clazz.equals(Byte.class)
                || clazz.equals(Character.class)
                || clazz.equals(Boolean.class);
    }

    @Nullable
    public static Class<?> forName(@NonNull String className) {
        try {
            switch (className) {
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "short":
                    return short.class;
                case "char":
                    return char.class;
                case "boolean":
                    return boolean.class;
                case "byte":
                    return byte.class;
                default:
                    return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            Log.w("Parameter", e);
        }
        return null;
    }

    @NonNull
    public static Class<?> getArrayClass(String className) throws ClassNotFoundException {
        String name;
        if (boolean.class.getName().equals(className)) {
            name = "[Z";
        } else if (byte.class.getName().equals(className)) {
            name = "[B";
        } else if (char.class.getName().equals(className)) {
            name = "[C";
        } else if (double.class.getName().equals(className)) {
            name = "[D";
        } else if (float.class.getName().equals(className)) {
            name = "[F";
        } else if (int.class.getName().equals(className)) {
            name = "[I";
        } else if (long.class.getName().equals(className)) {
            name = "[J";
        } else if (short.class.getName().equals(className)) {
            name = "[S";
        } else {
            // must be an object non-array class
            name = "[L" + className + ";";
        }
        return Class.forName(name);
    }
}
