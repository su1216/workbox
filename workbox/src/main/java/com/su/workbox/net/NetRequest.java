package com.su.workbox.net;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
    private static final Gson gson = new Gson();

    Object mTag;
    private final TypeToken<T> mType;
    static Handler sHandler = new Handler(GeneralInfoHelper.getContext().getMainLooper());
    String mUrl;
    String mMethod = "POST";
    String mMediaType;
    Map<String, String> mHeaderMap = new HashMap<>();
    Map<String, Object> mFormBodyMap = new HashMap<>();
    Map<String, MultipartFile> mMultipartMap = new HashMap<>();

    private final Callback<T> mCallback;

    NetRequest(String url, String method, TypeToken<T> typeReference, Callback<T> callback) {
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

    @SuppressWarnings("unchecked")
    T parseNetworkResponse(String json) {
        try {
            Type type = mType.getType();
            if (type.equals(String.class)) {
                return (T) json;
            } else {
                return gson.fromJson(json, mType.getType());
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

    protected void onFailure(IOException ioException) {
        if (isCanceled()) {
            mCallback.onCancel();
        } else {
            mCallback.onFailure(ioException);
        }
    }

    protected void onResponse(NetResponse<T> response) {
        try {
            if (response.isSuccessful()) {
                mCallback.onResponse(response);
            } else {
                mCallback.onError(response);
            }
        } catch (IOException e) {
            Log.d(TAG, "url: " + mUrl, e);
        }
    }
}
