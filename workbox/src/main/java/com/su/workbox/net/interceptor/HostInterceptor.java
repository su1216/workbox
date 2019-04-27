package com.su.workbox.net.interceptor;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.su.workbox.Workbox;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 切换域名时使用。
 * webview中需要手动调用urlMapping函数修改url。
 * 需要在MockInterceptor之前设置，先mock再修改url，否则mock找不到需要mock数据的url
 **/
public class HostInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String host = Workbox.getHost();
        if (TextUtils.isEmpty(host)) {
            return chain.proceed(request);
        }

        String url = request.url().toString();
        String newUrl = Workbox.urlMapping(url, host);
        Request newRequest = request.newBuilder().url(newUrl).build();
        return chain.proceed(newRequest);
    }
}
