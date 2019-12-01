package com.su.workbox.ui.app;

import android.annotation.TargetApi;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ExpandableListView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.CertificateDetail;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SignatureUtil;
import com.su.workbox.widget.ToastBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CertificateDetailActivity extends BaseAppCompatActivity implements ExpandableListView.OnChildClickListener {
    public static final String TAG = CertificateDetailActivity.class.getSimpleName();
    private ExpandableListView mListView;
    private List<List<Pair<String, CharSequence>>> mDataList = new ArrayList<>();
    private InfoAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_app_info_list);
        mListView = findViewById(R.id.expandable_list);
        makeCertificateInfo();
        mAdapter = new InfoAdapter(this, mDataList);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(this);
        expandAll(mDataList.size());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("证书信息");
    }

    private void makeCertificateInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SigningInfo signingInfo = SignatureUtil.getSignatureInfo28(this, getPackageName());
            makeCertificateInfo28(signingInfo);
        } else {
            Signature[] signatures = SignatureUtil.getSignatures(this, getPackageName());
            makeCertificateInfo(signatures);
        }
    }

    private void makeCertificateInfo(Signature[] signatures) {
        for (Signature signature : signatures) {
            makeInfoFromSignature(signature);
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private void makeCertificateInfo28(SigningInfo signingInfo) {
        List<Pair<String, CharSequence>> group0 = new ArrayList<>();
        group0.add(new Pair<>("MultipleSigners", String.valueOf(signingInfo.hasMultipleSigners())));
        group0.add(new Pair<>("PastSigningCertificates", String.valueOf(signingInfo.hasPastSigningCertificates())));
        mDataList.add(group0);
        if (!signingInfo.hasMultipleSigners()) {
            for (Signature signature : signingInfo.getSigningCertificateHistory()) {
                makeInfoFromSignature(signature);
            }
        }
    }

    private void makeInfoFromSignature(Signature signature) {
        List<Pair<String, CharSequence>> group = new ArrayList<>();
        CertificateDetail certificateDetail = new CertificateDetail(signature);
        add(group, "签名算法", certificateDetail.getSignAlgorithm());
        add(group, "签名MD5", certificateDetail.getMd5Digest());
        add(group, "签名SHA1", certificateDetail.getSha1Digest());
        add(group, "签名SHA256", certificateDetail.getSha256Digest());
        add(group, "公钥算法", certificateDetail.getPublicKeyAlgorithm());
        add(group, "公钥MD5", certificateDetail.getPublicKeyMd5());
        add(group, "公钥SHA1", certificateDetail.getPublicKeySha1());
        add(group, "公钥SHA256", certificateDetail.getPublicKeySha256());
        add(group, "有效期始", certificateDetail.getStartDate().toString());
        add(group, "有效期至", certificateDetail.getEndDate().toString());
        BigInteger serialNumber = certificateDetail.getSerialNumber();
        add(group, "序列号", "0x" + serialNumber.toString(16) + " / " + serialNumber.toString());
        add(group, "发行者", certificateDetail.getIssuerFirstAndLastName());
        add(group, "发行者单位", certificateDetail.getIssuerOrganizationalUnit());
        add(group, "发行者组织", certificateDetail.getIssuerOrganization());
        add(group, "发行者城市", certificateDetail.getIssuerCityOrLocality());
        add(group, "发行者省份", certificateDetail.getIssuerStateOrProvince());
        add(group, "发行者国家", certificateDetail.getIssuerCountryCode());
        add(group, "所有者", certificateDetail.getSubjectFirstAndLastName());
        add(group, "所有者单位", certificateDetail.getSubjectOrganizationalUnit());
        add(group, "所有者组织", certificateDetail.getSubjectOrganization());
        add(group, "所有者城市", certificateDetail.getSubjectCityOrLocality());
        add(group, "所有者省份", certificateDetail.getSubjectStateOrProvince());
        add(group, "所有者国家", certificateDetail.getSubjectCountryCode());
        mDataList.add(group);
    }

    private static void add(List<Pair<String, CharSequence>> group, String first, CharSequence second) {
        if (TextUtils.isEmpty(second)) {
            return;
        }
        group.add(new Pair<>(first, second));
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Pair<String, CharSequence> pair = mDataList.get(groupPosition).get(childPosition);
        AppHelper.copyToClipboard(this, pair.first, pair.second.toString());
        new ToastBuilder("已将" + pair.first + "复制到粘贴板中").show();
        return true;
    }

    private void expandAll(int groupCount) {
        for (int i = 0; i < groupCount; i++) {
            mListView.expandGroup(i);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
