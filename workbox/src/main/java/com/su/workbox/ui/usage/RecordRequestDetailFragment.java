package com.su.workbox.ui.usage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.ThreadUtil;

import java.util.Date;

public class RecordRequestDetailFragment extends BaseFragment {

    private DataUsageRecord mDataUsageRecord;

    static RecordRequestDetailFragment newInstance(@NonNull DataUsageRecord record) {
        RecordRequestDetailFragment fragment = new RecordRequestDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("record", record);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mDataUsageRecord = bundle.getParcelable("record");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workbox_fragment_record_request_detail, container, false);
        TextView requestSizeView = view.findViewById(R.id.request_size);
        TextView urlTitleView = view.findViewById(R.id.url_title);
        TextView urlView = view.findViewById(R.id.url);
        TextView methodView = view.findViewById(R.id.method);
        TextView contentTypeView = view.findViewById(R.id.content_type);
        TextView requestTimeView = view.findViewById(R.id.request_time);
        View headersLayout = view.findViewById(R.id.headers_layout);
        TextView headerTitleView = view.findViewById(R.id.header_title);
        TextView headersView = view.findViewById(R.id.headers);
        TextView bodyTitleView = view.findViewById(R.id.body_title);
        TextView bodyView = view.findViewById(R.id.body);

        requestSizeView.setText(AppHelper.formatSize(mDataUsageRecord.getRequestLength()));
        urlView.setText(mDataUsageRecord.getUrl());
        methodView.setText(mDataUsageRecord.getMethod());
        contentTypeView.setText(mDataUsageRecord.getContentType());
        requestTimeView.setText(ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(mDataUsageRecord.getRequestTime())));
        urlTitleView.setText("Url (" + AppHelper.formatSize(mDataUsageRecord.getUrlLength()) + "): ");
        if (TextUtils.isEmpty(mDataUsageRecord.getRequestHeaders())) {
            headersLayout.setVisibility(View.GONE);
        } else {
            headerTitleView.setText("Headers (" + AppHelper.formatSize(mDataUsageRecord.getRequestHeaderLength()) + "): ");
            headersView.setText(mDataUsageRecord.getRequestHeaders());
            headersLayout.setVisibility(View.VISIBLE);
        }
        bodyTitleView.setText("Body (" + AppHelper.formatSize(mDataUsageRecord.getRequestBodyLength()) + "): ");
        bodyView.setText(mDataUsageRecord.getRequestBody());
        return view;
    }
}
