package com.su.workbox.net;

import android.view.Gravity;
import android.widget.Toast;

import com.su.workbox.BuildConfig;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.NetworkUtil;

import java.io.IOException;

/**
 * Created by su on 17-4-11.
 */

public class SimpleCallback<T> implements Callback<T> {

    private static Toast sFailureToast = Toast.makeText(GeneralInfoHelper.getContext(), "请求失败,请检查网络设置", Toast.LENGTH_LONG);
    private static Toast sErrorToast = Toast.makeText(GeneralInfoHelper.getContext(), "请求失败,请重试", Toast.LENGTH_LONG);
    private static Toast sCanceledToast = Toast.makeText(GeneralInfoHelper.getContext(), "请求已取消", Toast.LENGTH_LONG);
    private static Toast sParseErrorToast = Toast.makeText(GeneralInfoHelper.getContext(), "", Toast.LENGTH_LONG);
    private Toast mErrorMessageToast = Toast.makeText(GeneralInfoHelper.getContext(), "", Toast.LENGTH_LONG);
    private String mErrorMessage = "";

    @Override
    public final void onFailure(IOException exception) {
        if (NetworkUtil.isNetworkAvailable()) {
            sErrorToast.show();
        } else {
            sFailureToast.show();
        }
        onServerError();
    }

    @Override
    public final void onCancel() {
        sCanceledToast.show();
        onRequestCancel();
    }

    @Override
    public final void onError(NetResponse<T> response) {
        if (BuildConfig.DEBUG) {
            Toast toast = Toast.makeText(GeneralInfoHelper.getContext(), response.getUrl() + "\n" + response.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            sErrorToast.show();
        }
        onServerError();
    }

    @Override
    public final void onResponse(NetResponse<T> response) {
        if (response.isParseSuccessful()) {
            onResponseSuccessful(response.getResult());
        } else {
            sParseErrorToast.setText("数据解析失败\n" + "url: " + response.getUrl());
            sParseErrorToast.show();
            onServerError();
        }
    }

    public void onResponseSuccessful(T response) {}

    public void onRequestCancel() {}

    public void onServerError() {}

    protected void toastErrorMessage(int gravity) {
        mErrorMessageToast.setGravity(gravity, 0, 0);
        mErrorMessageToast.setText(mErrorMessage);
        mErrorMessageToast.show();
    }

    protected void toastErrorMessage() {
        toastErrorMessage(Gravity.CENTER);
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }
}
