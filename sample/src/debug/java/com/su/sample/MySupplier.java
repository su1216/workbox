package com.su.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import com.su.sample.web.JsCommunication;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.entity.Module;
import com.su.workbox.utils.GeneralInfoHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 18-1-3.
 */

public class MySupplier extends WorkboxSupplier {

    @Override
    public boolean isLogin() {
        return Math.random() * 3.0d > 1.0d;
    }

    @Nullable
    public String toCookies(@NonNull String host) {
        return "uid=abc";
    }

    @NonNull
    @Override
    public Map<String, Object> jsObjectList(Activity activity) {
        Map<String, Object> map = new HashMap<>();
        map.put("JsCommunication", new JsCommunication(activity));
        return map;
    }

    @NonNull
    @Override
    public List<Pair<String, String>> allHosts() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("test", "https://www.baidu.com"));
        list.add(new Pair<>("release", "https://www.google.com"));
        return list;
    }

    @NonNull
    @Override
    public List<Pair<String, String>> allWebViewHosts() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("test", "https://www.baidu.com"));
        list.add(new Pair<>("release", "https://www.facebook.com"));
        return list;
    }

    @NonNull
    @Override
    public List<List<String>> getRequestBodyExcludeKeys() {
        List<List<String>> keys = new ArrayList<>();
        List<String> key = new ArrayList<>();
        keys.add(key);
        return keys;
    }

    @NonNull
    @Override
    public List<Module> getCustomModules() {
        Context context = GeneralInfoHelper.getContext();
        List<Module> list = new ArrayList<>();
        Module module = new Module();
        module.setName("自定义哦");
        module.setOnClickListener(v -> {
            Intent intent = new Intent(context, LifecycleActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        module.setId("test");
        list.add(module);
        return list;
    }
}
