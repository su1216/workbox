package com.su.workbox.net.interceptor;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.util.Log;

import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.usage.DataUsageRecord;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class DataUsageInterceptor implements Interceptor {

    private static final String TAG = DataUsageInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static boolean sDebug = false;
    private static volatile boolean sRecording;

    static {
        SharedPreferences sharedPreferences = SpHelper.getWorkboxSharedPreferences();
        sRecording = sharedPreferences.getBoolean("data_usage", false);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!sRecording) {
            return chain.proceed(chain.request());
        }
        Request request = chain.request();
        DataUsageRecord record = new DataUsageRecord();
        requestCollection(record, request);
        //first insert, then update
        insert(record);
        Response response = chain.proceed(request);
        responseCollection(record, response);
        update(record);
        return response;
    }

    private void insert(DataUsageRecord record) {
        HttpDataDatabase database = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        long id = database.dataUsageRecordDao().insertDataUsageRecord(record);
        record.setId(id);
    }

    private void update(DataUsageRecord record) {
        HttpDataDatabase database = HttpDataDatabase.getInstance(GeneralInfoHelper.getContext());
        database.dataUsageRecordDao().updateDataUsageRecord(record);
    }

    private void requestCollection(DataUsageRecord entity, Request request) throws IOException {
        String fullUrl = request.url().toString();
        int paramsStartIndex = fullUrl.indexOf("?");
        String url;
        if (paramsStartIndex >= 0) {
            url = fullUrl.substring(0, paramsStartIndex);
        } else {
            url = fullUrl;
        }
        String method = request.method();
        RequestBody requestBody = request.body();
        String bodyString = "";
        String contentType = "";
        boolean binary = false;
        if (!(requestBody instanceof MultipartBody) && requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            if (mediaType != null) {
                contentType = mediaType.toString();
            }

            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            if (isPlaintext(buffer)) {
                bodyString = buffer.readString(UTF8);
                entity.setRequestBody(bodyString);
            } else {
                binary = true;
                entity.setRequestBody(null);
            }
        }

        if (sDebug) {
            Log.d(TAG, "url: " + url);
            Log.d(TAG, "method: " + method);
        }
        entity.setUrl(url);
        entity.setMethod(method);
        entity.setContentType(contentType);
        entity.setRequestHeaders(getHeaders(request.headers()));
        entity.setUrlLength(readUrlLength(url));
        entity.setRequestHeaderLength(readHeaderLength(request.headers()));
        entity.setRequestBodyLength(readRequestBodyLength(request));
        entity.setRequestLength(entity.getRequestBodyLength() + entity.getRequestHeaderLength() + entity.getUrlLength());
        entity.setRequestBinary(binary);
        entity.setRequestTime(System.currentTimeMillis());
    }

    private void responseCollection(DataUsageRecord entity, Response response) throws IOException {
        entity.setResponseTime(System.currentTimeMillis());
        entity.setDuration(entity.getResponseTime() - entity.getRequestTime());
        entity.setCode(response.code());
        Headers headers = response.headers();
        String responseHeaders = getHeaders(response.headers());
        entity.setResponseHeaders(responseHeaders);
        entity.setResponseHeaderLength(readHeaderLength(headers));

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            entity.setResponseLength(0L);
            return;
        }

        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        if (isBodyGzipped(headers)) {
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
        boolean binary = false;
        if (isPlaintext(buffer)) {
            if (contentLength != 0) {
                responseBodyString = buffer.clone().readString(charset);
            }
            entity.setResponseBody(responseBodyString);
        } else {
            binary = true;
        }
        entity.setResponseBinary(binary);
        readResponseBodyLength(entity, buffer);
    }

    private static String getHeaders(Headers headers) {
        if (headers == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String name = headers.name(i);
            stringBuilder.append(name);
            stringBuilder.append(": ");
            stringBuilder.append(headers.get(name));
            stringBuilder.append("\r\n");
        }
        return stringBuilder.toString();
    }

    /**
     * @see okhttp3.logging.HttpLoggingInterceptor
     */
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

    private long readRequestBodyLength(Request request) {
        long length = 0;
        try {
            RequestBody body = request.body();
            if (body != null) {
                length = body.contentLength();
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
        return length;
    }

    private long readUrlLength(String url) {
        Buffer buffer = new Buffer();
        buffer.writeString(url, UTF8);
        return buffer.size();
    }

    private long readHeaderLength(Headers headers) {
        Buffer headerBuffer = new Buffer();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String name = headers.name(i);
            headerBuffer.writeString(name + ": " + headers.get(name) + "\r\n", UTF8);
        }
        headerBuffer.writeString("\r\n", UTF8);
        return headerBuffer.size();
    }

    private void readResponseBodyLength(DataUsageRecord entity, Buffer buffer) {
        entity.setResponseBodyLength(buffer.size());
        entity.setResponseLength(entity.getResponseBodyLength() + entity.getResponseHeaderLength());
    }

    private boolean isBodyGzipped(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return "gzip".equalsIgnoreCase(contentEncoding);
    }

    public static void setRecording(boolean recording) {
        sRecording = recording;
    }

    public static void debug(boolean debug) {
        sDebug = debug;
    }
}
