package com.su.sample;

import com.su.annotations.NoteWebView;

/**
 * Created by su on 18-1-3.
 */

public class Url {

    @NoteWebView(description = "stackoverflow",
            requestHeaders = {"h1=1", "h2=测试"},
            parameters = {"p1=测试", "p2=32"},
            needLogin = false)
    public static final String BAIDU = "https://stackoverflow.com";

    @NoteWebView(description = "github",
            title = "这里可以更换",
            needLogin = false)
    public static final String GITHUB = "https://github.com";
}
