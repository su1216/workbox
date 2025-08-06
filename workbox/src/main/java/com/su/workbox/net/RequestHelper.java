package com.su.workbox.net;

import com.google.gson.reflect.TypeToken;

/**
 * Created by su on 17-4-12.
 */

public class RequestHelper {

    public static <T> NetRequest<T> getRequest(String url, TypeToken<T> typeReference, SimpleCallback<T> sydCallback) {
        return new OkHttpRequest<>(url, typeReference, sydCallback);
    }

    public static <T> NetRequest<T> getRequest(String url, String method, TypeToken<T> typeReference, SimpleCallback<T> sydCallback) {
        return new OkHttpRequest<>(url, method, typeReference, sydCallback);
    }

    public static void cancel(Object tag) {
        OkHttpRequest.cancel(tag);
    }
}
