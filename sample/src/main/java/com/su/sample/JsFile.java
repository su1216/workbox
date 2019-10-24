package com.su.sample;

import com.su.annotations.NoteFilepath;
import com.su.annotations.NoteJsFunction;
import com.su.annotations.Parameter;

/**
 * Created by su on 18-1-5.
 */

public class JsFile {

    @NoteJsFunction(description = "需要将js文件放置到/sdcard/workbox/js/test.js，且包含一个名为add的函数",
            jsFilepath = @NoteFilepath(filepath = "file:///sdcard/workbox/js/test.js"),
            parameters = {@Parameter(parameterClass = Integer.class, parameter = "3"),
                    @Parameter(parameterClass = Integer.class, parameter = "5")},
            resultClass = Integer.class)
    public static final String JS_FUNCTION_ADD = "add";

    @NoteJsFunction(description = "需要将js文件放置到assets/js/test.js，且包含一个名为minus的函数",
            jsFilepath = @NoteFilepath(filepath = "file:///android_asset/js/test.js"),
            parameters = {@Parameter(parameterClass = Integer.class, parameter = "3"),
                    @Parameter(parameterClass = Integer.class, parameter = "5")},
            resultClass = Integer.class)
    public static final String JS_FUNCTION_MINUS = "minus";
}
