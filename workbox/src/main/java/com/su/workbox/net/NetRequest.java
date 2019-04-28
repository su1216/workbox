package com.su.workbox.net;

import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.su.workbox.BuildConfig;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.widget.ToastBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by su on 17-4-10.
 */

public abstract class NetRequest<T> {

    private static final String TAG = NetRequest.class.getSimpleName();

    Object mTag;
    private final TypeReference<T> mType;
    static Handler sHandler = new Handler(GeneralInfoHelper.getContext().getMainLooper());
    String mUrl;
    String mMethod = "POST";
    String mMediaType;
    Map<String, String> mHeaderMap = new HashMap<>();
    Map<String, Object> mFormBodyMap = new HashMap<>();
    Map<String, MultipartFile> mMultipartMap = new HashMap<>();

    private Callback<T> mCallback;

    NetRequest(String url, String method, TypeReference<T> typeReference, Callback<T> callback) {
        if (!"POST".equals(method) && !"GET".equals(method)) {
            throw new IllegalArgumentException("method is wrong: " + method);
        }
        mUrl = url;
        mMethod = method;
        mType = typeReference;
        mCallback = callback;
    }

    public NetRequest<T> setMediaType(String type) {
        mMediaType = type;
        return this;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public NetRequest<T> addHeader(String key, String value) {
        mHeaderMap.put(key, value);
        return this;
    }

    public NetRequest<T> addHeaders(Map<String, String> headers) {
        mHeaderMap.putAll(headers);
        return this;
    }

    public NetRequest<T> addParameter(String key, Object value) {
        mFormBodyMap.put(key, value);
        return this;
    }

    public NetRequest<T> addParameters(Map<String, Object> parameters) {
        mFormBodyMap.putAll(parameters);
        return this;
    }

    public NetRequest<T> setTag(Object tag) {
        mTag = tag;
        return this;
    }

    T parseNetworkResponse(String json) {
        try {
            Type type = mType.getType();
            if (type.equals(String.class)) {
                return (T) json;
            } else {
                return JSON.parseObject(json, mType);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "url: " + mUrl + " \tjson: " + json, e);

            ParseException parseException = new ParseException(e.getMessage());
            parseException.initCause(e);
            throw parseException;
        }
    }

    public abstract void enqueue();

    public abstract void execute();

    public abstract boolean isCanceled();

    static void checkParamsIsNull(Map<String, Object> params) {
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                if (BuildConfig.DEBUG) {
                    sHandler.post(() -> new ToastBuilder(entry.getKey() + " is null").show());
                } else {
                    sHandler.post(() -> new ToastBuilder("请求参数不合法").show());
                }
            }
        }
    }

    class OnFailure implements Runnable {
        private IOException mException;

        OnFailure(IOException e) {
            mException = e;
        }

        @Override
        public void run() {
            if (isCanceled()) {
                mCallback.onCancel();
            } else {
                mCallback.onFailure(mException);
            }
        }
    }

    class OnResponse implements Runnable {
        private NetResponse<T> mResponse;

        OnResponse(NetResponse<T> response) {
            mResponse = response;
        }

        @Override
        public void run() {
            try {
                if (mResponse.isSuccessful()) {
                    mCallback.onResponse(mResponse);
                } else {
                    mCallback.onError(mResponse);
                }
            } catch (IOException e) {
                Log.d(TAG, "url: " + mUrl, e);
            }
        }
    }
}
