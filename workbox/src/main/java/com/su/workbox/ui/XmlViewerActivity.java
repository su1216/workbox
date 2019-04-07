package com.su.workbox.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.su.workbox.R;

public class XmlViewerActivity extends BaseAppCompatActivity {

    public static final String TAG = XmlViewerActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_text_viewer);
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        WebView webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webView.loadData(content, "text/xml", "utf-8");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra("title"));
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
