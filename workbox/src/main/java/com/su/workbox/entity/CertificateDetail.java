package com.su.workbox.entity;

import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.util.Log;

import com.su.workbox.utils.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

/**
 * Created by su on 19-12-1.
 */
public class CertificateDetail {

    public static final String TAG = CertificateDetail.class.getSimpleName();

    private X509Certificate mCertificate;
    private Signature mSignature;
    private int mVersion;
    private String mSignAlgorithm;
    private String mMd5Digest;
    private String mSha1Digest;
    private String mSha256Digest;
    private String mPublicKeyAlgorithm;
    private String mPublicKeyMd5;
    private String mPublicKeySha1;
    private String mPublicKeySha256;
    private Date mStartDate;
    private Date mEndDate;
    private BigInteger mSerialNumber;
    private String mIssuerName;
    private String mIssuerFirstAndLastName;
    private String mIssuerOrganizationalUnit;
    private String mIssuerOrganization;
    private String mIssuerCityOrLocality;
    private String mIssuerStateOrProvince;
    private String mIssuerCountryCode;
    private String mSubjectName;
    private String mSubjectFirstAndLastName;
    private String mSubjectOrganizationalUnit;
    private String mSubjectOrganization;
    private String mSubjectCityOrLocality;
    private String mSubjectStateOrProvince;
    private String mSubjectCountryCode;

    public CertificateDetail(@NonNull Signature signature) {
        mSignature = signature;
        InputStream input = new ByteArrayInputStream(signature.toByteArray());
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            mCertificate = (X509Certificate) certificateFactory.generateCertificate(input);
            init();
        } catch (CertificateException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.closeQuietly(input);
        }
    }

    private void init() {
        try {
            mVersion = mCertificate.getVersion();
            byte[] certificateEncodedBytes = mCertificate.getEncoded();
            mSignAlgorithm = mCertificate.getSigAlgName();
            mMd5Digest = algorithmDigest(certificateEncodedBytes, "Md5");
            mSha1Digest = algorithmDigest(certificateEncodedBytes, "Sha1");
            mSha256Digest = algorithmDigest(certificateEncodedBytes, "Sha256");
            mPublicKeyAlgorithm = mCertificate.getPublicKey().getAlgorithm();
            byte[] publicKeyEncodedBytes = mCertificate.getPublicKey().getEncoded();
            mPublicKeyMd5 = algorithmDigest(publicKeyEncodedBytes, "Md5");
            mPublicKeySha1 = algorithmDigest(publicKeyEncodedBytes, "Sha1");
            mPublicKeySha256 = algorithmDigest(publicKeyEncodedBytes, "Sha256");
            mStartDate = mCertificate.getNotBefore();
            mEndDate = mCertificate.getNotAfter();
            mSerialNumber = mCertificate.getSerialNumber();
            X500Principal issuerX500Principal = mCertificate.getIssuerX500Principal();
            mIssuerName = issuerX500Principal.getName();
            mIssuerFirstAndLastName = getFirstAndLastName(mIssuerName);
            mIssuerOrganization = getOrganization(mIssuerName);
            mIssuerOrganizationalUnit = getOrganizationalUnit(mIssuerName);
            mIssuerCityOrLocality = getCityOrLocality(mIssuerName);
            mIssuerStateOrProvince = getStateOrProvince(mIssuerName);
            mIssuerCountryCode = getCountryCode(mIssuerName);
            mSubjectName = mCertificate.getSubjectX500Principal().getName();
            mSubjectFirstAndLastName = getFirstAndLastName(mSubjectName);
            mSubjectOrganization = getOrganization(mSubjectName);
            mSubjectOrganizationalUnit = getOrganizationalUnit(mSubjectName);
            mSubjectCityOrLocality = getCityOrLocality(mSubjectName);
            mSubjectStateOrProvince = getStateOrProvince(mSubjectName);
            mSubjectCountryCode = getCountryCode(mSubjectName);
        } catch (CertificateEncodingException e) {
            Log.w(TAG, e);
        }
    }

    private String getFirstAndLastName(String name) {
        Pattern pattern = Pattern.compile("CN=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getOrganization(String name) {
        Pattern pattern = Pattern.compile("O=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getOrganizationalUnit(String name) {
        Pattern pattern = Pattern.compile("OU=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getCityOrLocality(String name) {
        Pattern pattern = Pattern.compile("L=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getStateOrProvince(String name) {
        Pattern pattern = Pattern.compile("ST=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getCountryCode(String name) {
        Pattern pattern = Pattern.compile("C=([^,]*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public X509Certificate getCertificate() {
        return mCertificate;
    }

    public Signature getSignature() {
        return mSignature;
    }

    public int getVersion() {
        return mVersion;
    }

    public String getSignAlgorithm() {
        return mSignAlgorithm;
    }

    public String getMd5Digest() {
        return mMd5Digest;
    }

    public String getSha1Digest() {
        return mSha1Digest;
    }

    public String getSha256Digest() {
        return mSha256Digest;
    }

    public String getPublicKeyAlgorithm() {
        return mPublicKeyAlgorithm;
    }

    public String getPublicKeyMd5() {
        return mPublicKeyMd5;
    }

    public String getPublicKeySha1() {
        return mPublicKeySha1;
    }

    public String getPublicKeySha256() {
        return mPublicKeySha256;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public BigInteger getSerialNumber() {
        return mSerialNumber;
    }

    public String getIssuerName() {
        return mIssuerName;
    }

    public String getIssuerFirstAndLastName() {
        return mIssuerFirstAndLastName;
    }

    public String getIssuerOrganization() {
        return mIssuerOrganization;
    }

    public String getIssuerOrganizationalUnit() {
        return mIssuerOrganizationalUnit;
    }

    public String getIssuerCityOrLocality() {
        return mIssuerCityOrLocality;
    }

    public String getIssuerStateOrProvince() {
        return mIssuerStateOrProvince;
    }

    public String getIssuerCountryCode() {
        return mIssuerCountryCode;
    }

    public String getSubjectName() {
        return mSubjectName;
    }

    public String getSubjectFirstAndLastName() {
        return mSubjectFirstAndLastName;
    }

    public String getSubjectOrganization() {
        return mSubjectOrganization;
    }

    public String getSubjectOrganizationalUnit() {
        return mSubjectOrganizationalUnit;
    }

    public String getSubjectCityOrLocality() {
        return mSubjectCityOrLocality;
    }

    public String getSubjectStateOrProvince() {
        return mSubjectStateOrProvince;
    }

    public String getSubjectCountryCode() {
        return mSubjectCountryCode;
    }

    private static String algorithmDigest(byte[] input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(input);
            return getHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static String getHexString(byte[] digest) {
        return new BigInteger(1, digest).toString(16);
    }

    @Override
    public String toString() {
        return "CertificateDetail{" +
                "mCertificate=" + mCertificate +
                ", mSignature=" + mSignature +
                ", mVersion=" + mVersion +
                ", mSignAlgorithm='" + mSignAlgorithm + '\'' +
                ", mMd5Digest='" + mMd5Digest + '\'' +
                ", mSha1Digest='" + mSha1Digest + '\'' +
                ", mSha256Digest='" + mSha256Digest + '\'' +
                ", mPublicKeyAlgorithm='" + mPublicKeyAlgorithm + '\'' +
                ", mPublicKeyMd5='" + mPublicKeyMd5 + '\'' +
                ", mPublicKeySha1='" + mPublicKeySha1 + '\'' +
                ", mPublicKeySha256='" + mPublicKeySha256 + '\'' +
                ", mStartDate=" + mStartDate +
                ", mEndDate=" + mEndDate +
                ", mSerialNumber=" + mSerialNumber +
                ", mIssuerName='" + mIssuerName + '\'' +
                ", mIssuerFirstAndLastName='" + mIssuerFirstAndLastName + '\'' +
                ", mIssuerOrganization='" + mIssuerOrganization + '\'' +
                ", mIssuerOrganizationalUnit='" + mIssuerOrganizationalUnit + '\'' +
                ", mIssuerCityOrLocality='" + mIssuerCityOrLocality + '\'' +
                ", mIssuerStateOrProvince='" + mIssuerStateOrProvince + '\'' +
                ", mIssuerCountryCode='" + mIssuerCountryCode + '\'' +
                ", mSubjectName='" + mSubjectName + '\'' +
                ", mSubjectFirstAndLastName='" + mSubjectFirstAndLastName + '\'' +
                ", mSubjectOrganization='" + mSubjectOrganization + '\'' +
                ", mSubjectOrganizationalUnit='" + mSubjectOrganizationalUnit + '\'' +
                ", mSubjectCityOrLocality='" + mSubjectCityOrLocality + '\'' +
                ", mSubjectStateOrProvince='" + mSubjectStateOrProvince + '\'' +
                ", mSubjectCountryCode='" + mSubjectCountryCode + '\'' +
                '}';
    }
}
