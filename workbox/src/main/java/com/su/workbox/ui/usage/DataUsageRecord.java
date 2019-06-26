package com.su.workbox.ui.usage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.su.workbox.component.annotation.Searchable;

@Entity(tableName = "data_usage")
public final class DataUsageRecord implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;
    @Searchable
    @ColumnInfo(name = "url")
    private String url;
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
    @ColumnInfo(name = "requestBinary")
    private boolean requestBinary;
    @ColumnInfo(name = "responseBinary")
    private boolean responseBinary;
    @ColumnInfo(name = "requestTime")
    private long requestTime = 0;
    @ColumnInfo(name = "urlLength")
    private long urlLength = 0;
    @ColumnInfo(name = "requestLength")
    private long requestLength = 0;
    @ColumnInfo(name = "requestHeaderLength")
    private long requestHeaderLength = 0;
    @ColumnInfo(name = "requestBodyLength")
    private long requestBodyLength = 0;
    @ColumnInfo(name = "multipartRequestBody")
    private boolean multipartRequestBody;
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

    public DataUsageRecord() {}

    protected DataUsageRecord(Parcel in) {
        id = in.readLong();
        url = in.readString();
        contentType = in.readString();
        method = in.readString();
        requestHeaders = in.readString();
        requestBody = in.readString();
        responseHeaders = in.readString();
        responseBody = in.readString();
        requestBinary = in.readByte() != 0;
        responseBinary = in.readByte() != 0;
        requestTime = in.readLong();
        urlLength = in.readLong();
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
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(url);
        dest.writeString(contentType);
        dest.writeString(method);
        dest.writeString(requestHeaders);
        dest.writeString(requestBody);
        dest.writeString(responseHeaders);
        dest.writeString(responseBody);
        dest.writeByte((byte) (requestBinary ? 1 : 0));
        dest.writeByte((byte) (responseBinary ? 1 : 0));
        dest.writeLong(requestTime);
        dest.writeLong(urlLength);
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
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DataUsageRecord> CREATOR = new Creator<DataUsageRecord>() {
        @Override
        public DataUsageRecord createFromParcel(Parcel in) {
            return new DataUsageRecord(in);
        }

        @Override
        public DataUsageRecord[] newArray(int size) {
            return new DataUsageRecord[size];
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

    public boolean isMultipartRequestBody() {
        return multipartRequestBody;
    }

    public void setMultipartRequestBody(boolean multipartRequestBody) {
        this.multipartRequestBody = multipartRequestBody;
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

    public boolean isRequestBinary() {
        return requestBinary;
    }

    public void setRequestBinary(boolean requestBinary) {
        this.requestBinary = requestBinary;
    }

    public boolean isResponseBinary() {
        return responseBinary;
    }

    public void setResponseBinary(boolean responseBinary) {
        this.responseBinary = responseBinary;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getUrlLength() {
        return urlLength;
    }

    public void setUrlLength(long urlLength) {
        this.urlLength = urlLength;
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

    public static class Summary {
        @ColumnInfo(name = "total")
        private int count;
        @ColumnInfo(name = "totalRequestLength")
        private long totalRequestLength;
        @ColumnInfo(name = "totalResponseLength")
        private long totalResponseLength;

        public int getCount() {
            return count;
        }

        public long getTotalRequestLength() {
            return totalRequestLength;
        }

        public long getTotalResponseLength() {
            return totalResponseLength;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setTotalRequestLength(long totalRequestLength) {
            this.totalRequestLength = totalRequestLength;
        }

        public void setTotalResponseLength(long totalResponseLength) {
            this.totalResponseLength = totalResponseLength;
        }

        @NonNull
        @Override
        public String toString() {
            return "Summary{" +
                    "count=" + count +
                    ", totalRequestLength=" + totalRequestLength +
                    ", totalResponseLength=" + totalResponseLength +
                    '}';
        }
    }

    public static class Group {
        @ColumnInfo(name = "url")
        private String url;
        @ColumnInfo(name = "total")
        private int total;
        @ColumnInfo(name = "groupRequestLength")
        private long groupRequestLength;
        @ColumnInfo(name = "groupResponseLength")
        private long groupResponseLength;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public long getGroupRequestLength() {
            return groupRequestLength;
        }

        public void setGroupRequestLength(long groupRequestLength) {
            this.groupRequestLength = groupRequestLength;
        }

        public long getGroupResponseLength() {
            return groupResponseLength;
        }

        public void setGroupResponseLength(long groupResponseLength) {
            this.groupResponseLength = groupResponseLength;
        }

        @Override
        public String toString() {
            return "Group{" +
                    "url='" + url + '\'' +
                    ", total=" + total +
                    ", groupRequestLength=" + groupRequestLength +
                    ", groupResponseLength=" + groupResponseLength +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DataUsageRecord{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", contentType='" + contentType + '\'' +
                ", method='" + method + '\'' +
                ", requestHeaders='" + requestHeaders + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", requestBinary=" + requestBinary +
                ", responseBinary=" + responseBinary +
                ", requestTime=" + requestTime +
                ", urlLength=" + urlLength +
                ", requestLength=" + requestLength +
                ", requestHeaderLength=" + requestHeaderLength +
                ", requestBodyLength=" + requestBodyLength +
                ", multipartRequestBody=" + multipartRequestBody +
                ", hasRequestBody=" + hasRequestBody +
                ", responseTime=" + responseTime +
                ", responseLength=" + responseLength +
                ", responseHeaderLength=" + responseHeaderLength +
                ", responseBodyLength=" + responseBodyLength +
                ", hasResponseBody=" + hasResponseBody +
                ", duration=" + duration +
                ", code=" + code +
                '}';
    }
}
