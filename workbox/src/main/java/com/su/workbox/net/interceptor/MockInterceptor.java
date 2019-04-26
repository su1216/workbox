package com.su.workbox.net.interceptor;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.mock.RequestResponseRecord;
import com.su.workbox.ui.mock.MockUtil;
import com.su.workbox.ui.mock.RequestResponseRecordSource;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;

/**
 * Created by su on 17-6-14.
 */

public class MockInterceptor implements Interceptor {

    private static final String TAG = MockInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Pattern PATTERN_BODY = Pattern.compile("([^=&?]+)=([^=&]*)");
    private static boolean sDebug = false;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (isDowntime()) {
            Response downtimeResponse = getDowntimeResponse(request);
            if (downtimeResponse != null) {
                return downtimeResponse;
            }
        }
        Response fakeResponse = fakeResponse(request);
        if (fakeResponse != null) {
            return fakeResponse;
        }
        return chain.proceed(request);
    }

    private Response fakeResponse(Request request) {
        String url = request.url().toString();
        SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
        String policy = sp.getString(SpHelper.COLUMN_MOCK_POLICY, "none");
        Log.d(TAG, "policy: " + policy);
        if (TextUtils.equals(policy, "none")) {
            return null;
        }
        boolean all = TextUtils.equals(policy, "all");
        RequestResponseRecord entity = prepareRequestData(url, request);
        Buffer buffer = null;
        try {
            //手动数据优先自动收集的数据
            HttpDataDatabase database = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
            RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
            RequestResponseRecord record;
            if (all) {
                record = dataSource.getRequestResponseRecordInUseByEssentialFactors(entity.getUrl(),
                        entity.getMethod(),
                        entity.getContentType(),
                        entity.getRequestHeaders(),
                        entity.getRequestBody(),
                        1);
            } else {
                record = dataSource.getRequestResponseRecordInUseByEssentialFactors(entity.getUrl(),
                        entity.getMethod(),
                        entity.getContentType(),
                        entity.getRequestHeaders(),
                        entity.getRequestBody(),
                        0,
                        1);
            }

            if (record != null) {
                Log.d(TAG, "got mock result!");
                String response = record.getResponseBody();
                if (response == null) {
                    response = "";
                }
                String responseHeaders = record.getResponseHeaders();
                Headers.Builder responseHeadersBuilder = new Headers.Builder();
                if (!TextUtils.isEmpty(responseHeaders)) {
                    Map<String, String> map = JSON.parseObject(responseHeaders, new TypeReference<Map<String, String>>() {});
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        responseHeadersBuilder.add(entry.getKey(), entry.getValue());
                    }
                }
                delay();

                buffer = new Buffer().writeUtf8(response);
                return new Response.Builder()
                        .protocol(Protocol.HTTP_2)
                        .code(200)
                        .message("FAKE")
                        .request(request)
                        .headers(responseHeadersBuilder.build())
                        .body(new RealResponseBody(response, buffer.size(), buffer))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e);
        } finally {
            if (buffer != null) {
                buffer.close();
            }
        }
        return null;
    }

    /**
     * 准备数据，将数据转换成指定格式
     * okhttp会在content type后面添加 {@code ; charset=utf-8} 字符
     *
     * @see okhttp3.RequestBody#create(MediaType, String)
     */
    private static RequestResponseRecord prepareRequestData(String url, Request request) {
        RequestResponseRecord entity = new RequestResponseRecord();
        Uri uri = Uri.parse(url);
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
        String method = request.method();
        Headers headers = request.headers();
        Headers headerWithoutContentType = headers.newBuilder().removeAll("Content-Type").build();
        Map<String, String> headersMap = new HashMap<>();
        Set<String> headerNames = headerWithoutContentType.names();
        for (String name : headerNames) {
            headersMap.put(name, headerWithoutContentType.get(name));
        }
        String newHeaders;
        if (headersMap.isEmpty()) {
            newHeaders = "";
        } else {
            newHeaders = JSON.toJSONString(headersMap, true);
        }

        RequestBody requestBody = request.body();
        String contentType = "";
        String newRequestBody = "";
        if (requestBody != null) {
            if (requestBody.contentType() != null) {
                contentType = requestBody.contentType().toString();
            }
            try {
                long contentLength = requestBody.contentLength();
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                if (contentLength != 0) {
                    String content = buffer.clone().readString(UTF8);
                    if (!TextUtils.isEmpty(contentType) && contentType.contains("application/json")) {
                        newRequestBody = parseApplicationJsonContent(content);
                    } else {
                        newRequestBody = parseBody(content);
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }

        entity.setUrl(newUrl);
        entity.setMethod(method);
        entity.setContentType(contentType);
        entity.setRequestHeaders(newHeaders);
        entity.setRequestBody(newRequestBody);

        if (sDebug) {
            Log.d(TAG, "newUrl: " + newUrl);
            Log.d(TAG, "method: " + method);
            Log.d(TAG, "contentType: " + contentType);
            Log.d(TAG, "headers: " + newHeaders);
            Log.d(TAG, "requestBody: " + newRequestBody);
        }
        return entity;
    }

    //模拟请求网络耗时
    private static void delay() {
        try {
            Thread.sleep(500L + (long) (2000 * Math.random()));
        } catch (InterruptedException e) {
            Log.w(TAG, e);
        }
    }

    private static String parseApplicationJsonContent(String content) {
        JSONObject jsonObject = JSON.parseObject(content);
        MockUtil.removeKeysFromJson(jsonObject);
        return JSON.toJSONString(jsonObject, true);
    }

    private static String parseBody(@NonNull String bodyString) {
        String cleanUpBody = MockUtil.removeKeysFromString(bodyString);
        return MockUtil.sortStringBody(cleanUpBody);
    }

    private Response getDowntimeResponse(Request request) {
        WorkboxSupplier supplier = WorkboxSupplier.getInstance();
        Buffer buffer = null;
        try {
            String body = supplier.downtimeResponse(request.url().toString());
            if (body == null) {
                body = "";
            }
            buffer = new Buffer().writeUtf8(body);
            Headers.Builder builder = new Headers.Builder()
                    .add("content-type", "application/json; charset=UTF-8")
                    .add("Content-Length", String.valueOf(buffer.size()));
            Headers headers = builder.build();
            return new Response.Builder()
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    .message("FAKE")
                    .request(request)
                    .headers(headers)
                    .body(new RealResponseBody(body, buffer.size(), buffer))
                    .build();
        } catch (RuntimeException e) {
            Log.w(TAG, e);
        } finally {
            if (buffer != null) {
                buffer.close();
            }
        }
        return null;
    }

    private static boolean isDowntime() {
        SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
        return sp.getBoolean(SpHelper.COLUMN_DEBUG_DOWNTIME, false);
    }

    public static void debug(boolean debug) {
        MockInterceptor.sDebug = debug;
    }
}
