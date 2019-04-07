package com.su.workbox.net;

import com.alibaba.fastjson.TypeReference;

/**
 * Created by su on 17-4-12.
 */

public class RequestHelper {

    public static <T> NetRequest<T> getRequest(String url, TypeReference<T> typeReference, SimpleCallback<T> sydCallback) {
        return new OkHttpRequest<>(url, typeReference, sydCallback);
    }

    public static <T> NetRequest<T> getRequest(String url, String method, TypeReference<T> typeReference, SimpleCallback<T> sydCallback) {
        return new OkHttpRequest<>(url, method, typeReference, sydCallback);
    }

    public static void cancel(Object tag) {
        OkHttpRequest.cancel(tag);
    }
}
