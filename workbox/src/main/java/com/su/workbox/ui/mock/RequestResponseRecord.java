package com.su.workbox.ui.mock;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.component.annotation.Searchable;

@Entity(tableName = "request_response")
public final class RequestResponseRecord implements Parcelable, Cloneable {

    public static final String TYPE_REQUEST_HEADERS = "RequestHeaders";
    public static final String TYPE_REQUEST_QUERY = "RequestQuery";
    public static final String TYPE_REQUEST_BODY = "RequestBody";
    public static final String TYPE_RESPONSE_HEADERS = "ResponseHeaders";
    public static final String TYPE_RESPONSE_BODY = "ResponseBody";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
    @Searchable
    @ColumnInfo(name = "url")
    private String url;
    @ColumnInfo(name = "host")
    private String host;
    @ColumnInfo(name = "path")
    private String path;
    @ColumnInfo(name = "pages")
    private String pages;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "contentType")
    private String contentType;
    @ColumnInfo(name = "method")
    private String method;
    @ColumnInfo(name = "requestHeaders")
    private String requestHeaders;
    @ColumnInfo(name = "requestBody")
    private String requestBody;
    @ColumnInfo(name = "responseHeaders")
    private String responseHeaders;
    @ColumnInfo(name = "responseBody")
    private String responseBody;
    @ColumnInfo(name = "binary")
    private boolean binary;
    @ColumnInfo(name = "requestTime")
    private long requestTime = 0;
    @ColumnInfo(name = "requestLength")
    private long requestLength = 0;
    @ColumnInfo(name = "requestHeaderLength")
    private long requestHeaderLength = 0;
    @ColumnInfo(name = "requestBodyLength")
    private long requestBodyLength = 0;
    @ColumnInfo(name = "hasRequestBody")
    private boolean hasRequestBody;
    @ColumnInfo(name = "responseTime")
    private long responseTime = 0;
    @ColumnInfo(name = "responseLength")
    private long responseLength = 0;
    @ColumnInfo(name = "responseHeaderLength")
    private long responseHeaderLength = 0;
    @ColumnInfo(name = "responseBodyLength")
    private long responseBodyLength = 0;
    @ColumnInfo(name = "hasResponseBody")
    private boolean hasResponseBody;
    @ColumnInfo(name = "duration")
    private long duration = 0;
    @ColumnInfo(name = "code")
    private int code = 0;
    @ColumnInfo(name = "auto")
    private boolean auto;
    @ColumnInfo(name = "inUse")
    private boolean inUse;

    public RequestResponseRecord() {}

    protected RequestResponseRecord(Parcel in) {
        id = in.readLong();
        url = in.readString();
        host = in.readString();
        path = in.readString();
        pages = in.readString();
        description = in.readString();
        contentType = in.readString();
        method = in.readString();
        requestHeaders = in.readString();
        requestBody = in.readString();
        responseHeaders = in.readString();
        responseBody = in.readString();
        binary = in.readByte() != 0;
        requestTime = in.readLong();
        requestLength = in.readLong();
        requestHeaderLength = in.readLong();
        requestBodyLength = in.readLong();
        hasRequestBody = in.readByte() != 0;
        responseTime = in.readLong();
        responseLength = in.readLong();
        responseHeaderLength = in.readLong();
        responseBodyLength = in.readLong();
        hasResponseBody = in.readByte() != 0;
        duration = in.readLong();
        code = in.readInt();
        auto = in.readByte() != 0;
        inUse = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(url);
        dest.writeString(host);
        dest.writeString(path);
        dest.writeString(pages);
        dest.writeString(description);
        dest.writeString(contentType);
        dest.writeString(method);
        dest.writeString(requestHeaders);
        dest.writeString(requestBody);
        dest.writeString(responseHeaders);
        dest.writeString(responseBody);
        dest.writeByte((byte) (binary ? 1 : 0));
        dest.writeLong(requestTime);
        dest.writeLong(requestLength);
        dest.writeLong(requestHeaderLength);
        dest.writeLong(requestBodyLength);
        dest.writeByte((byte) (hasRequestBody ? 1 : 0));
        dest.writeLong(responseTime);
        dest.writeLong(responseLength);
        dest.writeLong(responseHeaderLength);
        dest.writeLong(responseBodyLength);
        dest.writeByte((byte) (hasResponseBody ? 1 : 0));
        dest.writeLong(duration);
        dest.writeInt(code);
        dest.writeByte((byte) (auto ? 1 : 0));
        dest.writeByte((byte) (inUse ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RequestResponseRecord> CREATOR = new Creator<RequestResponseRecord>() {
        @Override
        public RequestResponseRecord createFromParcel(Parcel in) {
            return new RequestResponseRecord(in);
        }

        @Override
        public RequestResponseRecord[] newArray(int size) {
            return new RequestResponseRecord[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
        this.hasRequestBody = !TextUtils.isEmpty(requestBody);
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        this.hasResponseBody = !TextUtils.isEmpty(responseBody);
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getRequestLength() {
        return requestLength;
    }

    public void setRequestLength(long requestLength) {
        this.requestLength = requestLength;
    }

    public long getRequestHeaderLength() {
        return requestHeaderLength;
    }

    public void setRequestHeaderLength(long requestHeaderLength) {
        this.requestHeaderLength = requestHeaderLength;
    }

    public long getRequestBodyLength() {
        return requestBodyLength;
    }

    public void setRequestBodyLength(long requestBodyLength) {
        this.requestBodyLength = requestBodyLength;
    }

    public boolean isHasRequestBody() {
        return hasRequestBody;
    }

    public void setHasRequestBody(boolean hasRequestBody) {
        this.hasRequestBody = hasRequestBody;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(long responseLength) {
        this.responseLength = responseLength;
    }

    public long getResponseHeaderLength() {
        return responseHeaderLength;
    }

    public void setResponseHeaderLength(long responseHeaderLength) {
        this.responseHeaderLength = responseHeaderLength;
    }

    public long getResponseBodyLength() {
        return responseBodyLength;
    }

    public void setResponseBodyLength(long responseBodyLength) {
        this.responseBodyLength = responseBodyLength;
    }

    public boolean isHasResponseBody() {
        return hasResponseBody;
    }

    public void setHasResponseBody(boolean hasResponseBody) {
        this.hasResponseBody = hasResponseBody;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public static class Summary {
        @ColumnInfo(name = "host")
        private String host;
        @ColumnInfo(name = "total")
        private int count;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "Summary{" +
                    "host='" + host + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    @Override
    public RequestResponseRecord clone() {
        RequestResponseRecord o = null;
        try {
            o = (RequestResponseRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @Override
    public String toString() {
        return "RequestResponseRecord{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", pages='" + pages + '\'' +
                ", description='" + description + '\'' +
                ", contentType='" + contentType + '\'' +
                ", method='" + method + '\'' +
                ", requestHeaders='" + requestHeaders + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", binary=" + binary +
                ", requestTime=" + requestTime +
                ", requestLength=" + requestLength +
                ", requestHeaderLength=" + requestHeaderLength +
                ", requestBodyLength=" + requestBodyLength +
                ", hasRequestBody=" + hasRequestBody +
                ", responseTime=" + responseTime +
                ", responseLength=" + responseLength +
                ", responseHeaderLength=" + responseHeaderLength +
                ", responseBodyLength=" + responseBodyLength +
                ", hasResponseBody=" + hasResponseBody +
                ", duration=" + duration +
                ", code=" + code +
                ", auto=" + auto +
                ", inUse=" + inUse +
                '}';
    }
}
