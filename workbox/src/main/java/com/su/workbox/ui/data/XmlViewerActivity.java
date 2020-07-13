package com.su.workbox.ui.data;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;

public class XmlViewerActivity extends DataActivity {

    public static final String TAG = XmlViewerActivity.class.getSimpleName();
    private File mExportedManifestFile;
    private String mManifestContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_text_viewer);
        mExportedManifestFile = new File(mExportedBaseDir, mVersionName + "-manifest.xml");
        Intent intent = getIntent();
        mManifestContent = intent.getStringExtra("content");
        WebView webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webView.loadData(mManifestContent, "text/xml", "utf-8");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra("title"));
    }

    @Override
    protected void export() {
        File dir = mExportedManifestFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        IOUtil.writeFile(mExportedManifestFile.getAbsolutePath(), mManifestContent);
        runOnUiThread(() -> new ToastBuilder("已将apk导出到" + mExportedManifestFile.getAbsolutePath()).setDuration(Toast.LENGTH_LONG).show());

    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
