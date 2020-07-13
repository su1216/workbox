package com.su.workbox.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.util.List;
import java.util.regex.Pattern;

public class HostsActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, View.OnClickListener {

    private static final String TAG = HostsActivity.class.getSimpleName();
    public static final int TYPE_HOST = 0;
    public static final int TYPE_WEB_VIEW_HOST = 1;
    private Pattern mIpPattern = Pattern.compile("^https?://" +
                                                "(?:1\\d\\d|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                                                "(?:1\\d\\d|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                                                "(?:1\\d\\d|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                                                "(?:1\\d\\d|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)" +
                                                "(?::\\d{1,5})?$");
    private Pattern mDomainPattern = Pattern.compile("^\n" +
                                                    "^https?://" +
                                                    "([a-z0-9\\-._~%]+" + //Named or IPv4 host. vivo 5.0 不支持命名分组
                                                    "|\\[[a-z0-9\\-._~%!$&'()*+,;=:]+\\])"); //IPv6+ host
    private List<Pair<String, String>> mHosts; //name host
    private String mHost;
    private RecyclerViewAdapter mAdapter;
    private EditText mInputView;
    private int mType;

    public static void startActivity(@NonNull Context context, int type) {
        Intent intent = new Intent(context, HostsActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_dialog_hosts);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mType = getIntent().getIntExtra("type", TYPE_HOST);
        WorkboxSupplier supplier = WorkboxSupplier.getInstance();
        if (mType == TYPE_HOST) {
            mHost = Workbox.getHost();
            mHosts = supplier.allHosts();
        } else {
            mHost = Workbox.getWebViewHost();
            mHosts = supplier.allWebViewHosts();
        }
        mHosts.add(0, new Pair<>("恢复默认", ""));
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        int padding = UiHelper.dp2px(16);
        lp.width = GeneralInfoHelper.getScreenWidth() - padding * 2;
        int height = GeneralInfoHelper.getAvailableHeight() - padding * 4;
        int titleHeight = UiHelper.dp2px(16) + UiHelper.sp2px(20);
        int itemHeight = UiHelper.dp2px(24) + UiHelper.sp2px(28);
        int used = mHosts.size() * (itemHeight + 1) + 1 + titleHeight + UiHelper.dp2px(32) + UiHelper.sp2px(16);
        lp.height = Math.min(used + (int) (itemHeight * 1.5) /*对话框阴影留出部分空间*/, height);
        getWindow().setAttributes(lp);

        mInputView = findViewById(R.id.input);
        mInputView.setSelection(mInputView.getText().length());
        findViewById(R.id.confirm).setOnClickListener(this);
        recyclerView.addItemDecoration(new PreferenceItemDecoration(this, 0, 0));
        mAdapter = new RecyclerViewAdapter(mHosts);
        initHost();
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
    }

    private void initHost() {
        int size = mHosts.size();
        if (size == 0) {
            mInputView.setText(mHost);
            return;
        }
        for (int i = 0; i < size; i++) {
            Pair<String, String> pair = mHosts.get(i);
            if (TextUtils.equals(pair.second, mHost)) {
                mAdapter.mLastCheckedPos = i;
                return;
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        List<Pair<String, String>> hosts = mAdapter.getData();
        if (position >= hosts.size()) {
            return;
        }
        Pair<String, String> pair = hosts.get(position);
        selectHost(pair.second);
    }

    //点击确定按钮时，只考虑输入框内的情况
    @Override
    public void onClick(View v) {
        String input = mInputView.getText().toString().trim();
        if (!mIpPattern.matcher(input).find() || !mDomainPattern.matcher(input).find()) {
            new ToastBuilder("输入不合法").show();
            return;
        }

        selectHost(input);
    }

    private void selectHost(@NonNull String host) {
        String key = mType == TYPE_HOST ? SpHelper.COLUMN_HOST : SpHelper.COLUMN_WEB_VIEW_HOST;
        SpHelper.getWorkboxSharedPreferences()
                .edit()
                .putString(key, host)
                .apply();

        Intent intent = new Intent();
        intent.putExtra("value", host);
        setResult(RESULT_OK, intent);
        finish();
    }

    //判断当前是否有选中的选项
    private boolean selected() {
        return mAdapter.mLastCheckedPos >= 0 && mAdapter.mLastCheckedPos < mHosts.size();
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<Pair<String, String>> {
        private int mLastCheckedPos = -1;

        private RecyclerViewAdapter(@NonNull List<Pair<String, String>> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_host;
        }

        @Override
        protected void bindData(@NonNull final BaseRecyclerAdapter.BaseViewHolder holder, final int position, int itemType) {
            String host = getData().get(position).second;
            TextView hostView = holder.getView(R.id.host);
            hostView.setText(host);
            hostView.setVisibility(TextUtils.isEmpty(host)? View.GONE : View.VISIBLE);
            ((TextView) holder.getView(R.id.name)).setText(getData().get(position).first);
            RadioButton radioButton = holder.getView(R.id.radio);
            radioButton.setChecked(mLastCheckedPos == position);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
