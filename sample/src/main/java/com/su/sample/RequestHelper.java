package com.su.sample;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.su.workbox.Workbox;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by su on 17-4-12.
 */

public class RequestHelper {

    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BODY;

    private static OkHttpClient sClient;

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(LOG_LEVEL);
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = client.newBuilder()
                .protocols(Util.immutableListOf(Protocol.HTTP_1_1, Protocol.HTTP_2))//just for http1.1
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(Workbox.getHostInterceptor());
            builder.addNetworkInterceptor(logging);
            builder.addInterceptor(Workbox.getMockInterceptor());
            builder.addInterceptor(new ChuckerInterceptor(SampleApplication.getContext()));
            builder.addInterceptor(Workbox.getDataCollectorInterceptor());
            builder.addInterceptor(Workbox.getDataUsageInterceptorInterceptor());
        }
        sClient = builder.build();
    }

    public static OkHttpClient getClient() {
        return sClient;
    }
}
