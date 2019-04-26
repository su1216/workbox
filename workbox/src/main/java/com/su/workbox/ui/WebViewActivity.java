package com.su.workbox.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.BuildConfig;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.entity.NoteWebViewEntity;
import com.su.workbox.entity.SimpleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 2018/1/10.
 */
public class WebViewActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    private String mTitle;
    private String mUrl;
    private boolean mSharable;
    private boolean mClearable;

    private NoteWebViewEntity mEntity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView mWebView;
    private View mLoadingErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_webview);
        Intent intent = getIntent();
        mSharable = intent.getBooleanExtra("sharable", false);
        mClearable = intent.getBooleanExtra("clearable", false);
        mTitle = intent.getStringExtra("title");
        mEntity = intent.getParcelableExtra("entity");
        if (savedInstanceState == null) {
            mUrl = intent.getStringExtra("url");
        } else {
            mUrl = savedInstanceState.getString("url");
        }
        if (mEntity != null) {
            mUrl = mEntity.getUrl();
            mTitle = mEntity.getTitle();
        }

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.workbox_swipe_color_1, R.color.workbox_swipe_color_2,
                R.color.workbox_swipe_color_3, R.color.workbox_swipe_color_4);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        initWebView();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
        Menu menu = mToolbar.getMenu();
        menu.findItem(R.id.share).setVisible(mSharable);
        menu.findItem(R.id.clean_up).setVisible(mClearable);
    }

    private void initWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView = findViewById(R.id.web_view);

        if (!verifyUrl(mUrl)) {
            String toast = "跳转链接错误！";
            toast += mUrl;
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
            finish();
        }

        mLoadingErrorLayout = findViewById(R.id.load_error_layout);
        mLoadingErrorLayout.setOnClickListener(this);

        initWebViewSettings();
        Map<String, Object> jsObjectMap = Workbox.jsObjectList(this);
        for (Map.Entry<String, Object> entry : jsObjectMap.entrySet()) {
            mWebView.addJavascriptInterface(entry.getValue(), entry.getKey());
        }
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //当前url与原始url一致时，同视为原始加载页面
                //优先使用配置中的title
                String webViewUrl = view.getUrl().replaceAll("/$", "");
                String url = mUrl.replaceAll("/$", "");
                if (TextUtils.equals(webViewUrl, url)) {
                    if (TextUtils.isEmpty(mTitle)) {
                        setTitle(title);
                    } else {
                        setTitle(mTitle);
                    }
                } else {
                    if (!TextUtils.isEmpty(title)) {
                        setTitle(title);
                    }
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
                Log.w(TAG, "failingUrl: " + failingUrl + " errorCode: " + errorCode + " description: " + description);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!verifyUrl(url)) {
                    Log.w(TAG, "url错误: " + url);
                    Toast.makeText(WebViewActivity.this, "url错误: " + url, Toast.LENGTH_LONG).show();
                    return true;
                }
                mUrl = url;
                addCookie(url);
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
            loadUrl();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    private void loadUrl() {
        addCookie(mUrl);
        if (mEntity == null) {
            mWebView.loadUrl(mUrl);
            return;
        }
        String method = mEntity.getMethod();
        if (TextUtils.equals("POST", method)) {
            postUrl();
        } else {
            getUrl();
        }
        toastMockData();
    }

    public void addCookie(String url) {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        String cookie = null;
        if (host != null) {
            cookie = Workbox.toCookies(host);
        }
        cookieManager.setCookie(url, cookie);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url: " + url + " cookie: " + cookieManager.getCookie(url));
        }
        CookieSyncManager.getInstance().sync();
    }

    private void postUrl() {
        mWebView.postUrl(mUrl, Workbox.toPostData(mEntity.getPostContent()));
    }

    private void getUrl() {
        List<SimpleParameter> requestHeaders = mEntity.getRequestHeaders();
        Map<String, String> requestHeadersMap = new HashMap<>();
        for (SimpleParameter parameter : requestHeaders) {
            String key = parameter.getKey();
            if (requestHeadersMap.containsKey(key)) {
                Log.e(TAG, "duplicate key found: " + key);
            }
            requestHeadersMap.put(key, parameter.getValue());
        }

        StringBuilder stringBuilder = new StringBuilder();
        List<SimpleParameter> parameters = mEntity.getParameters();
        for (SimpleParameter parameter : parameters) {
            stringBuilder.append(parameter.getKey());
            stringBuilder.append("=");
            stringBuilder.append(AppHelper.encodeUrlString(parameter.getValue()));
            stringBuilder.append("&");
        }
        if (!parameters.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            if (mUrl.contains("?")) {
                mUrl += "&" + stringBuilder;
            } else {
                mUrl += "?" + stringBuilder;
            }
        }
        mWebView.loadUrl(mUrl, requestHeadersMap);
    }

    private void toastMockData() {
        if (mEntity == null) {
            return;
        }
        String method = mEntity.getMethod();
        String title = mEntity.getTitle();
        String description = mEntity.getDescription();
        String content;
        if (TextUtils.equals("POST", method)) {
            content = mEntity.getPostContent();
        } else {
            StringBuilder headerBuilder = new StringBuilder();
            List<SimpleParameter> requestHeaders = mEntity.getRequestHeaders();
            for (SimpleParameter parameter : requestHeaders) {
                String key = parameter.getKey();
                headerBuilder.append(key);
                headerBuilder.append(": ");
                headerBuilder.append(parameter.getValue());
                headerBuilder.append("\n");
            }
            if (requestHeaders.size() > 0) {
                headerBuilder.deleteCharAt(headerBuilder.length() - 1);
                headerBuilder.insert(0, "headers: \n");
            }

            if (TextUtils.isEmpty(headerBuilder)) {
                content = mUrl;
            } else {
                content = mUrl + "\n" + headerBuilder;
            }
        }
        String toast = title + "\n" +
                "description: " + description + "\n" +
                "method: " + method + "\n" +
                content;
        Uri uri = Uri.parse(mUrl);
        String host = uri.getHost();
        String cookie = null;
        if (host != null) {
            cookie = Workbox.toCookies(host);
        }
        if (!TextUtils.isEmpty(cookie)) {
            toast += "\ncookie: " + cookie;
        }
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
    }

    private boolean verifyUrl(String url) {
        return URLUtil.isNetworkUrl(url) || URLUtil.isAssetUrl(url);
    }

    private void initWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
    }

    public void clearWebViewCache() {
        mWebView.clearCache(true);
        deleteDatabase("webview.db");
        deleteDatabase("webviewCache.db");
        Toast.makeText(this, "webview缓存已清除", Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mLoadingErrorLayout.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(true);
            mWebView.goBack();
            mUrl = mWebView.getUrl();
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mUrl);
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_webview_menu;
    }

    public void share(MenuItem menuItem) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mWebView.getOriginalUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "分享当前地址"));
    }

    public void cleanUp(MenuItem menuItem) {
        clearWebViewCache();
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

    public void loadJs(final String jsMethod, final String... params) {
        if (mWebView == null) {
            Log.d(TAG, "webView = null");
            return;
        }

        mWebView.post(() -> {
            String url;
            if (params == null || params.length == 0) {
                url = "javascript:" + jsMethod + "()";
            } else {
                StringBuilder sb = new StringBuilder();
                for (String p : params) {
                    sb.append("'" + p + "',");
                }
                sb.deleteCharAt(sb.length() - 1);
                url = "javascript:" + jsMethod + "(" + sb + ")";
            }
            mWebView.loadUrl(url);
            Log.d(TAG, "loadJs: " + url);
        });
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
