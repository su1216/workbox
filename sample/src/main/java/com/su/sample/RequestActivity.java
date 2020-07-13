package com.su.sample;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 18-1-3.
 */

public class RequestActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_main);
        TextView textView = findViewById(R.id.text);
        textView.setText("开启数据模拟或者停机维护后，此页面数据返回结果则变为相应模拟数据");
        textView.setText(R.string.app_name);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("网络请求测试");
    }

    private void test() {
        Request.Builder builder = new Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url("http://www.mxnzp.com/api/address/search?type=1&value=" + encodeString("北京"));
        Request request = builder.build();
        OkHttpClient client = RequestHelper.getClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(RequestActivity.this, "网络错误", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    Object object;
                    try {
                        object = JSON.toJSONString(JSON.parse(result), true);
                    } catch (JSONException e) {
                        object = result;
                    }
                    String text = object == null ? "" : object.toString();
                    runOnUiThread(() -> {
                        mSwipeRefreshLayout.setRefreshing(false);
                        ((TextView) findViewById(R.id.content)).setText(text);
                    });
                } else {
                    Toast.makeText(RequestActivity.this, "请求错误", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private static String encodeString(String str) {
        if (str == null) {
            return null;
        }
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //ignore
        }
        return str;
    }

    private RequestBody createFormBody(Map<String, Object> map) {
        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object value = entry.getValue();
            if (value instanceof String) {
                builder.add(entry.getKey(), (String) value);
            } else {
                builder.add(entry.getKey(), JSON.toJSONString(value));
            }
        }
        return builder.build();
    }

    @Override
    public void onRefresh() {
        test();
    }
}
