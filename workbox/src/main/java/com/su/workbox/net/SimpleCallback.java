package com.su.workbox.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.su.workbox.BuildConfig;
import com.su.workbox.utils.NetworkUtil;
import com.su.workbox.widget.ToastBuilder;

import java.io.IOException;

/**
 * Created by su on 17-4-11.
 */

public class SimpleCallback<T> implements Callback<T> {

    private static Toast sFailureToast;
    private static Toast sErrorToast;
    private static Toast sCanceledToast;
    private static Toast sParseErrorToast;
    private static Toast mErrorMessageToast;
    private final String mErrorMessage = "";
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private Handler mHandler;
    private boolean mShowToast = true;

    static {
        MAIN_HANDLER.post(() -> {
            sFailureToast = new ToastBuilder("请求失败,请检查网络设置").create();
            sErrorToast = new ToastBuilder("请求失败,请重试").create();
            sCanceledToast = new ToastBuilder("请求已取消").create();
            sParseErrorToast = new ToastBuilder("").create();
            mErrorMessageToast = new ToastBuilder("").create();
        });
    }

    public SimpleCallback() {
        mHandler = MAIN_HANDLER;
    }

    public SimpleCallback<T> setHandler(Handler handler) {
        mHandler = handler;
        return this;
    }

    public SimpleCallback<T> showErrorToast(boolean showToast) {
        mShowToast = showToast;
        return this;
    }

    @Override
    public final void onFailure(IOException exception) {
        if (mHandler != null) {
            mHandler.post(this::onServerError);
        } else {
            onServerError();
        }
        if (mShowToast) {
            MAIN_HANDLER.post(() -> {
                if (NetworkUtil.isNetworkAvailable()) {
                    sErrorToast.show();
                } else {
                    sFailureToast.show();
                }
            });
        }
    }

    @Override
    public final void onCancel() {
        if (mShowToast) {
            MAIN_HANDLER.post(() -> sCanceledToast.show());
        }
        if (mHandler != null) {
            mHandler.post(this::onRequestCancel);
        } else {
            onRequestCancel();
        }
    }

    @Override
    public final void onError(NetResponse<T> response) {
        if (mShowToast) {
            MAIN_HANDLER.post(() -> {
                if (BuildConfig.DEBUG) {
                    Toast toast = new ToastBuilder(response.getUrl() + "\n" + response.getMessage()).create();
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    sErrorToast.show();
                }
            });
        }
        if (mHandler != null) {
            mHandler.post(this::onServerError);
        } else {
            onServerError();
        }
    }

    @Override
    public final void onResponse(NetResponse<T> response) {
        if (mHandler != null) {
            mHandler.post(() -> onPreResponse(response));
        } else {
            onPreResponse(response);
        }
    }

    private void onPreResponse(NetResponse<T> response) {
        if (response.isParseSuccessful()) {
            T result = response.getResult();
            Log.d("response", "result: " + result);
            onResponseSuccessful(result);
        } else {
            if (mShowToast) {
                MAIN_HANDLER.post(() -> {
                    sParseErrorToast.setText("数据解析失败\n" + "url: " + response.getUrl());
                    sParseErrorToast.show();
                });
            }
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
