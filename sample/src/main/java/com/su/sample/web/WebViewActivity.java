package com.su.sample.web;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.su.workbox.Workbox;
import com.su.sample.BaseAppCompatActivity;
import com.su.sample.R;

/**
 * Created by su on 2018/11/21.
 */
public class WebViewActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    private String mTitle;
    private String mUrl;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView mWebView;
    private View mLoadingErrorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_webview);

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        if (savedInstanceState == null) {
            mUrl = intent.getStringExtra("url");
            String host = Workbox.getWebViewHost();
            if (!TextUtils.isEmpty(host)) {
                mUrl = Workbox.urlMapping(mUrl, host);
            }
        } else {
            mUrl = savedInstanceState.getString("url");
        }
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mWebView = findViewById(R.id.web_view);

        mLoadingErrorLayout = findViewById(R.id.load_error_layout);
        mLoadingErrorLayout.setOnClickListener(this);

        initWebViewSettings();
        mWebView.addJavascriptInterface(new JsCommunication(this), "JsCommunication");
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.stopLoading();
                view.clearView();
                mSwipeRefreshLayout.setRefreshing(false);
                mLoadingErrorLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mUrl = url;
                Log.d(TAG, "newUrl: " + mUrl);
                mSwipeRefreshLayout.setRefreshing(true);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    toastHttpError(request, errorResponse);
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            private void toastHttpError(WebResourceRequest request, WebResourceResponse errorResponse) {
                Toast.makeText(WebViewActivity.this, "HTTP error: " + errorResponse.getStatusCode() + "\n url: " + request.getUrl(), Toast.LENGTH_LONG).show();
            }
        });

        mLoadingErrorLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            mWebView.loadUrl(mUrl);
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToolbar.setTitle(mTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mUrl);
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    private void initWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.load_error_layout) {
            refresh();
        }
    }

    public void refresh() {
        mLoadingErrorLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mWebView.reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ViewGroup) mWebView.getParent()).removeAllViews();
        mWebView.destroy();
    }
}
