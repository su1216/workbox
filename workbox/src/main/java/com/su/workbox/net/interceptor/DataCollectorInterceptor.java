package com.su.workbox.net.interceptor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.su.workbox.AppHelper;
import com.su.workbox.ui.mock.RequestResponseRecord;
import com.su.workbox.ui.mock.MockUtil;
import com.su.workbox.ui.mock.RequestResponseRecordDao;
import com.su.workbox.utils.GeneralInfoHelper;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * Created by su on 2018/5/30.
 */

public class DataCollectorInterceptor implements Interceptor {

    private static final String TAG = DataCollectorInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final File COLLECTOR_DIR = new File(GeneralInfoHelper.getContext().getExternalCacheDir(), "collector");
    private static boolean sDebug = false;

    static {
        if (!COLLECTOR_DIR.exists()) {
            COLLECTOR_DIR.mkdirs();
        }
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        RequestResponseRecord entity = new RequestResponseRecord();
        requestCollection(entity, request);
        Response response = chain.proceed(request);

        synchronized (RequestResponseRecordDao.LOCK) {
            //如果已经收集过并且存在于数据库中，则不再收集
            if (MockUtil.getRequestResponseRecordByEssentialFactors(entity) != null) {
                return response;
            }

            try {
                responseCollection(entity, response);
                MockUtil.saveResponseEntity(entity);
            } catch (JSONException e) {
                Log.w(TAG, "data error: " + e.getMessage());
            }
        }
        return response;
    }

    private void requestCollection(RequestResponseRecord entity, Request request) throws IOException {
        String fullUrl = request.url().toString();
        int paramsStartIndex = fullUrl.indexOf("?");
        String url;
        if (paramsStartIndex >= 0) {
            url = fullUrl.substring(0, paramsStartIndex);
        } else {
            url = fullUrl;
        }
        String method = request.method();
        JSONObject headersJson = getHeaders(request.headers());
        String headers;
        if (headersJson == null || headersJson.isEmpty()) {
            headers = "";
        } else {
            headers = JSON.toJSONString(headersJson, true);
        }
        Uri uri = Uri.parse(fullUrl);
        Set<String> queryKeySet = new HashSet<>(uri.getQueryParameterNames());
        //调整query字段顺序
        List<String> queryKeyList = new ArrayList<>(queryKeySet);
        Collections.sort(queryKeyList);
        Uri.Builder builder = uri.buildUpon();
        builder.clearQuery();
        for (String key : queryKeyList) {
            builder.appendQueryParameter(key, uri.getQueryParameter(key));
        }
        String newUrl = builder.build().toString();
        String host = AppHelper.getHostFromUrl(newUrl);
        String path = uri.getPath();
        RequestBody requestBody = request.body();
        String bodyString = "";
        String contentType = "";
        if (requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            if (mediaType != null) {
                contentType = mediaType.toString();
            }

            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            if (isPlaintext(buffer)) {
                JSONObject jsonObject;
                String content = buffer.readString(UTF8);
                try {
                    jsonObject = JSON.parseObject(content);
                    MockUtil.removeKeysFromJson(jsonObject);
                    bodyString = JSON.toJSONString(jsonObject,
                            SerializerFeature.DisableCircularReferenceDetect,
                            SerializerFeature.PrettyFormat);
                } catch (JSONException e) {
                    String cleanUpBody = MockUtil.removeKeysFromString(content);
                    bodyString = MockUtil.sortStringBody(cleanUpBody);
                }
            }
        }

        if (sDebug) {
            Log.d(TAG, "url: " + url);
            Log.d(TAG, "host: " + host);
            Log.d(TAG, "path: " + path);
            Log.d(TAG, "method: " + method);
            Log.d(TAG, "contentType: " + contentType);
            Log.d(TAG, "requestHeaders: " + headersJson);
            Log.d(TAG, "requestBody: " + bodyString);
        }
        entity.setUrl(url);
        entity.setHost(host);
        entity.setPath(path);
        entity.setMethod(method);
        entity.setContentType(contentType);
        entity.setRequestHeaders(headers);
        entity.setRequestBody(bodyString);
        entity.setAuto(true);
    }

    private static void responseCollection(RequestResponseRecord entity, Response response) throws IOException {
        Headers headers = response.headers();
        JSONObject responseHeaders = getHeaders(response.headers());
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
            GzipSource gzippedResponseBody = null;
            try {
                gzippedResponseBody = new GzipSource(buffer.clone());
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            } finally {
                if (gzippedResponseBody != null) {
                    gzippedResponseBody.close();
                }
            }
        }
        Charset charset = null;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }
        if (charset == null) {
            charset = UTF8;
        }

        String responseBodyString = null;
        if (sDebug) {
            Log.d(TAG, "responseHeaders: " + responseHeaders);
            entity.setResponseHeaders(JSON.toJSONString(responseHeaders, true));
        }
        if (isPlaintext(buffer)) {
            if (contentLength != 0) {
                responseBodyString = buffer.clone().readString(charset);
            }
            if (sDebug) {
                Log.d(TAG, "responseBody: " + responseBodyString);
            }
            if (responseBodyString == null) {
                entity.setResponseBody(null);
            } else {
                String bodyContent;
                try {
                    bodyContent = toJSONString(responseBodyString);
                } catch (JSONException e) {
                    bodyContent = responseBodyString;
                }
                entity.setResponseBody(bodyContent);
            }
        } else {
            if (sDebug) {
                Log.w(TAG, "responseBody: not plain text!");
            }
        }
    }

    private static String toJSONString(@NonNull String input) {
        return JSON.toJSONString(JSON.parse(input),
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat);
    }

    private static JSONObject getHeaders(Headers headers) {
        if (headers == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String name = headers.name(i);
            jsonObject.put(name, headers.get(name));
        }
        return jsonObject;
    }

    /**
     * @see okhttp3.logging.HttpLoggingInterceptor
     * */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    public static void debug(boolean debug) {
        sDebug = debug;
    }
}
