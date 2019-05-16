package com.su.workbox;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by su on 18-1-2.
 */
public class WorkboxSupplier {
    private static final String TAG = WorkboxSupplier.class.getSimpleName();
    private static WorkboxSupplier sSupplier;

    public boolean isLogin() {
        return false;
    }

    /**
     * 停机维护时，返回统一的数据
     * */
    @Nullable
    public String downtimeResponse(@NonNull String url) {
        return "";
    }

    /**
     * 返回所有在通过 {@link android.webkit.WebView#addJavascriptInterface(Object, String)} 注入到WebView的Objects<br/>
     * 以便在WebView调试功能中使用这些Objects
     * */
    @NonNull
    public Map<String, Object> jsObjectList(Activity activity) {
        return new HashMap<>();
    }

    /**
     * 所有备选host/ip+端口列表，用于切换地址
     * */
    @NonNull
    public List<Pair<String, String>> allHosts() {
        return new ArrayList<>();
    }

    /**
     * 所有备选host/ip+端口列表，用于切换地址
     * */
    @NonNull
    public List<Pair<String, String>> allWebViewHosts() {
        return new ArrayList<>();
    }

    /**
     * 请求体中需要忽略的字段
     * 在使用mock功能时，被忽略的字段将从请求体中移除，判断两个请求的请求条件是否相同时，被忽略的字段将不再影响判断结果
     * */
    @NonNull
    public List<List<String>> getRequestBodyExcludeKeys() {
        return new ArrayList<>();
    }

    /**
     * 自定义的缓存目录，当执行清除缓存时，下列目录中内容都会被递归清除
     * */
    @Nullable
    public List<File> getAllCustomCacheDirs() {
        return new ArrayList<>();
    }

    /**
     * 将一个url通过改变域名或者通过改变ip与端口的方式映射到另一个url
     * */
    @NonNull
    String urlMapping(@NonNull String url, @NonNull String newHost) {
        String newUrl = url;
        Pattern pattern = Pattern.compile("^(https?://)([^/]+)(.*)");
        Matcher matcher = pattern.matcher(url);
        boolean redirect = false;
        if (matcher.find()) {
            String scheme = matcher.group(1);
            String address = matcher.group(2); //域名+端口
            if (!TextUtils.equals(scheme, newHost) && !TextUtils.equals(address, newHost)) {
                newUrl = (newHost.startsWith("http") ? newHost : "https://" + newHost) + matcher.group(3);
                redirect = true;
            }
        }
        Log.i(TAG, "redirect: " + redirect + " new: " + newUrl + " old: " + url);
        return newUrl;
    }

    /**
     * 当WebView请求为post时，需要app自行转换post数据
     * postData byte: the data will be passed to "POST" request, which must be be "application/x-www-form-urlencoded" encoded.
     * */
    @Nullable
    public byte[] toPostData(@Nullable String content) {
        return null;
    }

    /**
     * 当cookies为null时，WebView将不设置cookies
     * */
    @Nullable
    public String toCookies(@NonNull String host) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void newInstance(@NonNull String className) {
        try {
            Class<? extends WorkboxSupplier> clazz = (Class<? extends WorkboxSupplier>) Class.forName(className);
            if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                sSupplier = clazz.newInstance();
                return;
            } else {
                Log.e(TAG, "class: " + clazz.getName());
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Log.e(TAG, "className: " + className, e);
        }
        throw new IllegalArgumentException("supplier must implements WorkboxSupplier!");
    }

    public static WorkboxSupplier getInstance() {
        return sSupplier;
    }
}
