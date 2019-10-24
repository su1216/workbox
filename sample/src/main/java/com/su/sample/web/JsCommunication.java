package com.su.sample.web;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.su.annotations.NoteJsCallAndroid;
import com.su.annotations.Parameter;

/**
 * Created by su on 2015/1/28.
 */
public class JsCommunication {
    private static final String TAG = JsCommunication.class.getSimpleName();

    protected Activity mActivity;

    public JsCommunication(Activity activity) {
        mActivity = activity;
    }

    @NoteJsCallAndroid(description = "关闭当前webview")
    @JavascriptInterface
    public void finish() {
        logCall("finish", "N/A");
        mActivity.finish();
    }

    @NoteJsCallAndroid(description = "打开一个新的webview ",
            parameters = @Parameter(parameterClass = String.class, parameter = "{\n" +
                    "  \"title\": \"test1\",\n" +
                    "  \"webViewUrl\": \"https://www.baidu.com\",\n" +
                    "  \"refresh\": true\n" +
                    "}"))
    @JavascriptInterface
    public void webview(String data) {
        logCall("webview", data);
        if (!TextUtils.isEmpty(data)) {
            JSONObject param = JSON.parseObject(data);
            String title = param.getString("title");
            String url = param.getString("webViewUrl");
            boolean needToRefresh = param.getBooleanValue("refresh");
            Intent intent = new Intent(mActivity, WebViewActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("url", url);
            intent.putExtra("need_to_refresh", needToRefresh);
            mActivity.startActivity(intent);
        }
    }

    @NoteJsCallAndroid(description = "复制内容到粘贴板中",
            parameters = @Parameter(parameterClass = String.class, parameter = "{\n" +
                    "  \"content\": \"找个输入框长按试试\"\n" +
                    "}"))
    @JavascriptInterface
    public void setClipBoard(String data) {
        logCall("setClipBoard", data);
        if (!TextUtils.isEmpty(data)) {
            JSONObject param = JSON.parseObject(data);
            String content = param.getString("content");
            copyToClipboard(mActivity, "来自内部网页", content);
        }
    }

    private static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    @NoteJsCallAndroid(description = "在android中输出log，使用adb命令查看",
            parameters = @Parameter(parameterClass = String.class, parameter = "{\n" +
                    "  \"tag\": \"FROM_JS\",\n" +
                    "  \"level\": \"e\",\n" +
                    "  \"content\": \"在終端中执行adb logcat -v time -s FROM_JS (你的tag)就能看到log了\"\n" +
                    "}"))
    @JavascriptInterface
    public void log(String data) {
        if (!TextUtils.isEmpty(data)) {
            JSONObject param = JSON.parseObject(data);
            String tag = param.getString("tag");
            String level = param.getString("level");
            String content = param.getString("content");
            if (TextUtils.isEmpty(tag)) {
                tag = TAG;
            }
            if (TextUtils.isEmpty(content)) {
                content = "no message!";
            }
            switch (level) {
                case "e":
                    Log.e(tag, content);
                    break;
                case "w":
                    Log.w(tag, content);
                    break;
                case "i":
                    Log.i(tag, content);
                    break;
                case "d":
                    Log.d(tag, content);
                    break;
                case "v":
                    Log.v(tag, content);
                    break;
                default:
                    Log.v(tag, content);
                    break;
            }
        }
    }

    private static void logCall(String functionName, String data) {
        Log.d(TAG, "function: " + functionName + " data: " + data);
    }
}
