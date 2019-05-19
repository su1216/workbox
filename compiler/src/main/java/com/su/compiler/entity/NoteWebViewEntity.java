package com.su.compiler.entity;

import java.util.ArrayList;

/**
 * Created by su on 18-11-14.
 */

public class NoteWebViewEntity {
    private String url;
    private String description;
    private String method;
    private ArrayList<SimpleParameter> requestHeaders;
    private ArrayList<SimpleParameter> parameters = new ArrayList<>();
    private boolean needLogin;
    private String title;
    private String postContent;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
    public String toString() {
        return "NoteWebViewEntity{" +
                "url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", method='" + method + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", parameters=" + parameters +
                ", needLogin=" + needLogin +
                ", title='" + title + '\'' +
                ", postContent='" + postContent + '\'' +
                '}';
    }
}
