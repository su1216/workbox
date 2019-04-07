package com.su.sample;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.su.sample.web.WebViewActivity;

/**
 * Created by su on 2018/1/29.
 */

public class SampleListActivity extends BaseAppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        findViewById(R.id.activity_test).setOnClickListener(this);
        findViewById(R.id.network_test).setOnClickListener(this);
        findViewById(R.id.web_view_test).setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Sample");
        mToolbar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_toolbar:
                if (BuildConfig.DEBUG) {
                    debug();
                }
                break;
            case R.id.network_test:
                startActivity(new Intent(this, RequestActivity.class));
                break;
            case R.id.web_view_test:
                Intent webViewIntent = new Intent(this, WebViewActivity.class);
                webViewIntent.putExtra("url", "https://github.com/explore");
                startActivity(webViewIntent);
                break;
            case R.id.activity_test:
                ObjectParameter p = new ObjectParameter();
                p.setId(0);
                p.setProvince("北京");
                p.setProvinceCode(10);
                ObjectParameter p1 = new ObjectParameter();
                p1.setId(1);
                p1.setProvince("天津");
                p1.setProvinceCode(22);
                ObjectParameter p2 = new ObjectParameter();
                p2.setId(2);
                p2.setProvince("上海");
                p2.setProvinceCode(21);
                Intent intent = new Intent(this, ObjectParameterActivity.class);
                intent.putExtra(ObjectParameterActivity.EXTRA_KEY_PARAMETER_INT, 1024);
                intent.putExtra(ObjectParameterActivity.EXTRA_KEY_PARAMETER_LONG, 2048L);
                intent.putExtra(ObjectParameterActivity.EXTRA_KEY_PARAMETER_OBJECT, p);
                intent.putExtra(ObjectParameterActivity.EXTRA_KEY_PARAMETER_OBJECTS, new ObjectParameter[]{p1, p2});
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void debug() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(getPackageName(), "com.su.workbox.ui.main.WorkboxMainActivity");
        intent.setComponent(componentName);
        startActivity(intent);
    }
}
