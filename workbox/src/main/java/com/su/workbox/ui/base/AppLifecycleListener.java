package com.su.workbox.ui.base;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

import java.util.Observable;

public class AppLifecycleListener extends Observable implements LifecycleObserver {

    private static AppLifecycleListener sAppLifecycleListener = new AppLifecycleListener();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        notifyObservers(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        notifyObservers(false);
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public static AppLifecycleListener getInstance() {
        return sAppLifecycleListener;
    }
}
