package com.su.workbox.ui.base;

import android.support.v4.app.Fragment;

import java.lang.reflect.Field;

public class FragmentListenerManager {

    static final String LISTENER_FRAGMENT = "listener_fragment";
    private ListenerFragment mListenerFragment;

    public void registerFragment(Fragment fragment) {
        if (mListenerFragment == null) {
            mListenerFragment = new ListenerFragment();
        }
        mListenerFragment.setFragmentLifecycleListener(FragmentLifecycleListener.getInstance());
        // 由于Fragment的bug，必须将mChildFragmentManager的accessible设为true
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
    private void compatibleFragment(Fragment fragment) {
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
}
