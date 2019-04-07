package com.su.workbox.ui.usage;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.database.table.DataUsageRecord;
import com.su.workbox.utils.ThreadUtil;

import java.util.Date;

/**
 * 流量监控记录 - 流量监控记录详情 - response
 * */
public class RecordResponseDetailFragment extends Fragment {

    private Resources mResources;
    private DataUsageRecord mDataUsageRecord;

    static RecordResponseDetailFragment newInstance(@NonNull DataUsageRecord record) {
        RecordResponseDetailFragment fragment = new RecordResponseDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("record", record);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        Bundle bundle = getArguments();
        mDataUsageRecord = bundle.getParcelable("record");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workbox_fragment_record_response_detail, container, false);
        TextView responseSizeView = view.findViewById(R.id.response_size);
        TextView responseCodeView = view.findViewById(R.id.response_code);
        TextView responseTimeView = view.findViewById(R.id.response_time);
        TextView durationView = view.findViewById(R.id.duration);
        View headersLayout = view.findViewById(R.id.headers_layout);
        TextView headerTitleView = view.findViewById(R.id.header_title);
        TextView headersView = view.findViewById(R.id.headers);
        TextView bodyTitleView = view.findViewById(R.id.body_title);
        TextView bodyView = view.findViewById(R.id.body);

        responseSizeView.setText(AppHelper.formatSize(mDataUsageRecord.getResponseLength()));
        int code = mDataUsageRecord.getCode();
        if (code >= 200 && code < 300) {
            responseCodeView.setTextColor(mResources.getColor(R.color.workbox_second_text));
        } else {
            responseCodeView.setTextColor(mResources.getColor(R.color.workbox_error_hint));
        }
        responseCodeView.setText(code > 0 ? String.valueOf(code) : "连接失败");
        responseTimeView.setText(ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(mDataUsageRecord.getResponseTime())));
        durationView.setText(mDataUsageRecord.getDuration() + "ms");
        if (TextUtils.isEmpty(mDataUsageRecord.getResponseHeaders())) {
            headersLayout.setVisibility(View.GONE);
        } else {
            headerTitleView.setText("Headers (" + AppHelper.formatSize(mDataUsageRecord.getResponseHeaderLength()) + "): ");
            headersView.setText(mDataUsageRecord.getResponseHeaders());
            headersLayout.setVisibility(View.VISIBLE);
        }
        bodyTitleView.setText("Body (" + AppHelper.formatSize(mDataUsageRecord.getResponseBodyLength()) + "): ");
        bodyView.setText(mDataUsageRecord.getResponseBody());
        return view;
    }
}
