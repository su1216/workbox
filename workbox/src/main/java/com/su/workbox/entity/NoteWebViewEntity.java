package com.su.workbox.entity;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.su.workbox.component.annotation.Searchable;

import java.util.ArrayList;

/**
 * Created by su on 18-11-13.
 */

public class NoteWebViewEntity implements Comparable<NoteWebViewEntity>, Parcelable {

    @Searchable
    private String url;
    private String title;
    @Searchable
    private String description;
    private String method;
    private ArrayList<SimpleParameter> requestHeaders = new ArrayList<>();
    private ArrayList<SimpleParameter> parameters = new ArrayList<>();
    private String postContent;
    private boolean needLogin;

    public NoteWebViewEntity() {}

    protected NoteWebViewEntity(Parcel in) {
        url = in.readString();
        title = in.readString();
        description = in.readString();
        method = in.readString();
        requestHeaders = in.createTypedArrayList(SimpleParameter.CREATOR);
        parameters = in.createTypedArrayList(SimpleParameter.CREATOR);
        postContent = in.readString();
        needLogin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(method);
        dest.writeTypedList(requestHeaders);
        dest.writeTypedList(parameters);
        dest.writeString(postContent);
        dest.writeByte((byte) (needLogin ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NoteWebViewEntity> CREATOR = new Creator<NoteWebViewEntity>() {
        @Override
        public NoteWebViewEntity createFromParcel(Parcel in) {
            return new NoteWebViewEntity(in);
        }

        @Override
        public NoteWebViewEntity[] newArray(int size) {
            return new NoteWebViewEntity[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<SimpleParameter> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(ArrayList<SimpleParameter> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public boolean isNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ArrayList<SimpleParameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<SimpleParameter> parameters) {
        this.parameters = parameters;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    @Override
    public int compareTo(@NonNull NoteWebViewEntity noteUrl) {
        int len1 = url.length();
        int len2 = noteUrl.url.length();
        int lim = Math.min(len1, len2);
        char[] v1 = url.toCharArray();
        char[] v2 = noteUrl.url.toCharArray();

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteWebViewEntity{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", method='" + method + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", parameters=" + parameters +
                ", postContent='" + postContent + '\'' +
                ", needLogin=" + needLogin +
                '}';
    }
}
