package com.su.workbox.net;

import android.support.annotation.NonNull;

/**
 * Created by su on 17-4-11.
 */

public class NetResponse<T> {
    private T result;
    private int code;
    private String message;
    private String url;
    private boolean parseSuccessful = true;

    NetResponse(T result, String url, int code, String message) {
        this.result = result;
        this.url = url;
        this.code = code;
        this.message = message;
    }

    NetResponse(T result, boolean parseSuccessful, String url, int code, String message) {
        this.result = result;
        this.url = url;
        this.code = code;
        this.parseSuccessful = parseSuccessful;
        this.message = message;
    }

    public boolean isParseSuccessful() {
        return parseSuccessful;
    }

    public int getCode() {
        return code;
    }

    public T getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was successfully received,
     * understood, and accepted.
     */
    boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * Returns the HTTP status message or null if it is unknown.
     */
    public String getMessage() {
        return message;
    }

    @NonNull
    @Override
    public String toString() {
        return "NetResponse{" +
                "result=" + result +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", url='" + url + '\'' +
                ", parseSuccessful=" + parseSuccessful +
                '}';
    }
}
