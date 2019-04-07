package com.su.workbox.utils;

public abstract class CancelableRunnable implements Runnable {

    private volatile boolean cancel;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
