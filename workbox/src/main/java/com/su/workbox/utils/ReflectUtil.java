package com.su.workbox.utils;

import android.content.Context;
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
        if (isPrimitiveClass(clazz) || isPrimitiveWrapperClass(clazz)) {
            return false;
        }
        try {
            clazz.getConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "no default constructor clazz: " + clazz);
            return false;
        }
    }

    public static int getBatteryCapacity(Context context) {
        Object powerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            powerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            batteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(powerProfile);

        } catch (Exception e) {
            Log.w(TAG, e);
        }

        return (int) batteryCapacity;
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
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T newInstance(@NonNull Class<T> clazz) {
        if (clazz == Integer.class) {
            return (T) Integer.valueOf(0);
        } else if (clazz == Boolean.class) {
            return (T) Boolean.valueOf(false);
        } else if (clazz == Character.class) {
            return (T) Character.valueOf('a');
        } else if (clazz == Byte.class) {
            return (T) Byte.valueOf((byte) 48);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(0);
        } else if (clazz == Float.class) {
            return (T) Float.valueOf(0);
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(0);
        } else if (clazz == Short.class) {
            return (T) Short.valueOf((short) 0);
        } else if (clazz == String.class) {
            return (T) "string";
        }

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
        String prefix = name.substring(0, name.length() - 2);
        if (name.endsWith("[I")) {
            return prefix + " (int)";
        } else if (name.endsWith("[J")) {
            return prefix + " (long)";
        } else if (name.endsWith("[S")) {
            return prefix + " (short)";
        } else if (name.endsWith("[F")) {
            return prefix + " (float)";
        } else if (name.endsWith("[D")) {
            return prefix + " (double)";
        } else if (name.endsWith("[C")) {
            return prefix + " (char)";
        } else if (name.endsWith("[B")) {
            return prefix + " (byte)";
        } else if (name.endsWith("[Z")) {
            return prefix + " (boolean)";
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

    public static Class<?> forName(@NonNull String className) throws ClassNotFoundException {
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
