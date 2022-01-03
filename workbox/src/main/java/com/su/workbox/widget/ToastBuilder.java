package com.su.workbox.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.UiHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by su on 2019-4-28.
 */
public class ToastBuilder {
    private static final String TAG = ToastBuilder.class.getSimpleName();

    private Context mContext = GeneralInfoHelper.getContext();
    private CharSequence mText;
    private int mGravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    private int mDuration = Toast.LENGTH_SHORT;
    private TextView mTextView;
    private Toast mToast;

    public ToastBuilder(int resId) {
        mText = mContext.getString(resId);
    }

    public ToastBuilder(CharSequence text) {
        mText = text;
    }

    public ToastBuilder setText(CharSequence text) {
        mText = text;
        return this;
    }

    public ToastBuilder setText(int resId) {
        setText(mContext.getString(resId));
        return this;
    }

    public ToastBuilder setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    public ToastBuilder setGravity(int gravity) {
        this.mGravity = gravity;
        return this;
    }

    public Toast create() {
        mToast = new Toast(mContext);
        mToast.setDuration(mDuration);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.workbox_toast_view, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);//设置Toast可以布局到系统状态栏的下面
        mTextView = view.findViewById(R.id.text);
        mTextView.setText(mText);

        int topMargin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topMargin = UiHelper.getStatusBarHeight(mContext);
        } else {
            topMargin = 0;
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mTextView.getLayoutParams();
        lp.width = GeneralInfoHelper.getScreenWidth();
        lp.topMargin = topMargin;
        mTextView.setMinHeight(UiHelper.getActionBarHeight(GeneralInfoHelper.getContext()));
        mTextView.setLayoutParams(lp);
        mToast.setView(view);
        mToast.setGravity(mGravity, 0, 0);
        // 如果是7.0-7.1对Toast进行hook
        //https://cloud.tencent.com/developer/article/1034225
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            HookToastUtil.hook(mToast);
        }
        //9.0+ 刘海屏适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Method getWindowParams = Toast.class.getMethod("getWindowParams");
                WindowManager.LayoutParams toastLp = (WindowManager.LayoutParams) getWindowParams.invoke(mToast);
                toastLp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.w(TAG, e);
            }
        }
        return mToast;
    }

    public Toast show() {
        Toast toast = create();
        toast.show();
        return toast;
    }

    public Toast show(CharSequence text) {
        Toast toast = create();
        mTextView.setText(text);
        toast.show();
        return toast;
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private static class HookToastUtil {
        private static Field sField_TN;
        private static Field sField_TN_Handler;

        static {
            try {
                sField_TN = Toast.class.getDeclaredField("mTN");
                sField_TN.setAccessible(true);
                sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
                sField_TN_Handler.setAccessible(true);
            } catch (Exception e) {
                //ignore
            }
        }

        private static void hook(Toast toast) {
            try {
                Object tn = sField_TN.get(toast);
                Handler preHandler = (Handler) sField_TN_Handler.get(tn);
                sField_TN_Handler.set(tn, new SafelyHandlerWrapper(preHandler));
            } catch (Exception e) {
                //ignore
            }
        }
    }

    private static class SafelyHandlerWrapper extends Handler {

        private Handler impl;

        public SafelyHandlerWrapper(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                super.dispatchMessage(msg);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            impl.handleMessage(msg);//需要委托给原Handler执行
        }
    }
}
