package com.su.workbox.net;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by su on 17-4-11.
 */

class OkHttpRequest<T> extends NetRequest<T> {

    private static final String TAG = OkHttpRequest.class.getSimpleName();
    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BODY;
    private static final MediaType CONTENT_TYPE = MediaType.get("application/x-www-form-urlencoded");
    private static final Gson gson = new Gson();

    private static OkHttpClient sClient;
    private Call mCall;
    private final FormBody.Builder mFormBodyBuilder = new FormBody.Builder();

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(LOG_LEVEL);
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = client.newBuilder()
                .protocols(Util.immutableListOf(Protocol.HTTP_1_1, Protocol.HTTP_2))//just for http1.1
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS);
        builder.addNetworkInterceptor(logging);
        sClient = builder.build();
    }

    public OkHttpRequest(String url, TypeToken<T> typeReference, Callback<T> sydCallback) {
        this(url, "POST", typeReference, sydCallback);
    }

    public OkHttpRequest(String url, String method, TypeToken<T> typeReference, Callback<T> sydCallback) {
        super(url, method, typeReference, sydCallback);
    }

    private Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        Set<Map.Entry<String, String>> entrySet = mHeaderMap.entrySet();
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private RequestBody createFormBody() {
        Set<Map.Entry<String, Object>> entrySet = mFormBodyMap.entrySet();
        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object value = entry.getValue();
            if (value instanceof String) {
                mFormBodyBuilder.add(entry.getKey(), (String) value);
            } else {
                mFormBodyBuilder.add(entry.getKey(), gson.toJson(value));
            }
        }
        return mFormBodyBuilder.build();
    }

    private RequestBody createBody(@Nullable MediaType type) {
        if (!mMultipartMap.isEmpty()) {
            return createMultipartBody(type);
        }
        Map<String, Object> jsonObject = new HashMap<>();
        Set<Map.Entry<String, Object>> entrySet = mFormBodyMap.entrySet();
        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object value = entry.getValue();
            jsonObject.put(entry.getKey(), value);
        }
        String jsonString = gson.toJson(jsonObject);
        return RequestBody.create(type, jsonString);
    }

    @Override
    public String getMediaType() {
        if (TextUtils.isEmpty(mMediaType)) {
            MediaType mediaType = mFormBodyBuilder.build().contentType();
            return mediaType.toString();
        } else {
            return mMediaType;
        }
    }

    private RequestBody createMultipartBody(@Nullable MediaType type) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (type != null) {
            builder.setType(type);
        }
        Set<Map.Entry<String, Object>> entrySet = mFormBodyMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value instanceof String) {
                builder.addFormDataPart(entry.getKey(), (String) value);
            } else {
                builder.addFormDataPart(entry.getKey(), gson.toJson(value));
            }
        }

        Set<Map.Entry<String, MultipartFile>> fileSet = mMultipartMap.entrySet();
        for (Map.Entry<String, MultipartFile> entry : fileSet) {
            MultipartFile multipartFile = entry.getValue();
            File file = multipartFile.getFile();
            if (file != null && file.exists() && file.isFile()) {
                builder.addFormDataPart(multipartFile.getName(), multipartFile.getFileName(), RequestBody.create(MediaType.parse(multipartFile.getMimeType()), file));
            } else {
                new ToastBuilder("文件选择错误： " + multipartFile).show();
            }
        }
        return builder.build();
    }

    private Request buildRequest() {
        Request.Builder builder = new Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(mUrl)
                .tag(mTag)
                .headers(createHeaders());

        if (HttpMethod.requiresRequestBody(mMethod)) {
            MediaType mediaType = MediaType.parse(mMediaType);
            if (TextUtils.isEmpty(mMediaType) || CONTENT_TYPE.equals(mediaType)) {
                checkParamsIsNull(mFormBodyMap);
                builder.method(mMethod, createFormBody());
            } else {
                builder.method(mMethod, createBody(mediaType));
            }
        } else {
            builder.method(mMethod, null);
        }
        return builder.build();
    }

    @Override
    public void enqueue() {
        try {
            Request request = buildRequest();
            mCall = sClient.newCall(request);
            mCall.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException ioException) {
                    OkHttpRequest.this.onFailure(ioException);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    OkHttpRequest.this.onResponse(response);
                }
            });
        } catch (RuntimeException e) {
            showRequestErrorToast();
            Log.d(TAG, "url: " + mUrl + " method: " + mMethod, e);
            OkHttpRequest.this.onFailure(new IOException("build request failed!"));
        }
    }

    private void showRequestErrorToast() {
        sHandler.post(() -> {
            String content = "请求错误: " + mUrl + "\n请求方式: " + mMethod + "\nheader: " + mHeaderMap;
            new ToastBuilder(content).show();
        });
    }

    @Override
    public void execute() {
        try {
            Request request = buildRequest();
            mCall = sClient.newCall(request);
            Response response = mCall.execute();
            onResponse(response);
        } catch (IOException e) {
            Log.d(TAG, "url: " + mUrl, e);
            OkHttpRequest.this.onFailure(e);
        } catch (RuntimeException e) {
            showRequestErrorToast();
            Log.d(TAG, "url: " + mUrl + " method: " + mMethod, e);
            OkHttpRequest.this.onFailure(new IOException("build request failed!"));
        }
    }

    private void onResponse(@NonNull Response response) throws IOException {
        Log.d(TAG, "response: " + response);
        try {
            T result = parseNetworkResponse(response.body().string());
            onResponse(new NetResponse<>(result, response.request().url().toString(), response.code(), makeErrorResponseMessage(response)));
        } catch (ParseException e) {
            Log.w(TAG, e);
            onResponse(new NetResponse<>(null, false, response.request().url().toString(), response.code(), makeErrorResponseMessage(response)));
        }
    }

    static void cancel(Object tag) {
        for (Call call : sClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
                Log.d(TAG, "a queued call canceled: " + call.request().url());
            }
        }
        for (Call call : sClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
                Log.d(TAG, "a running call canceled: " + call.request().url());
            }
        }
    }

    @Override
    public boolean isCanceled() {
        if (mCall != null) {
            return mCall.isCanceled();
        }
        return false;
    }

    private String makeErrorResponseMessage(Response response) {
        if (GeneralInfoHelper.getContext() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("url: " + mUrl + "\n");
            if (response.code() > 0) {
                sb.append("statusCode: " + response.code() + "\n");
            }
            sb.append("message: " + response.message() + "\n");
            sb.append("headers: " + response.headers() + "\n");
            return sb.toString();
        }
        return "";
    }
}
