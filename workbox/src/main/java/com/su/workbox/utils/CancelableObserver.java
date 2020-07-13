package com.su.workbox.utils;

import androidx.lifecycle.Observer;

public abstract class CancelableObserver<T> implements Observer<T> {
    private volatile boolean cancel;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
