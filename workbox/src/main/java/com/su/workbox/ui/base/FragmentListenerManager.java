package com.su.workbox.ui.base;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.lang.reflect.Field;

public class FragmentListenerManager {

    private static final String LISTENER_FRAGMENT = "listener_fragment";
    private ListenerFragment mListenerFragment;

    public void registerFragment(@NonNull Fragment fragment) {
        if (mListenerFragment == null) {
            mListenerFragment = new ListenerFragment();
        }
        mListenerFragment.setFragmentLifecycleListener(FragmentLifecycleListener.getInstance());
        compatibleFragment(fragment);
        fragment.getChildFragmentManager()
                .beginTransaction()
                .add(mListenerFragment, LISTENER_FRAGMENT)
                .commitAllowingStateLoss();
    }

    /**
     * For bug of Fragment in Android
     * https://issuetracker.google.com/issues/36963722
     */
    private void compatibleFragment(@NonNull Fragment fragment) {
        //androidx无需处理
        try {
            Class<?> clazz = Class.forName("androidx.fragment.app.Fragment");
            if (clazz.isAssignableFrom(fragment.getClass())) {
                return;
            }
        } catch (ClassNotFoundException e) {
            //ignore
        }
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(fragment, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setEnableLog(boolean enableLog) {
        FragmentLifecycleListener.setEnableLog(enableLog);
    }
}
