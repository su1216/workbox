package com.su.workbox.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import com.su.workbox.ui.ui.RulerActivity;

@TargetApi(Build.VERSION_CODES.N)
public class MeasureService extends TileService {
    @Override
    public void onClick() {
        if (RulerActivity.isShowing()) {
            sendBroadcast(new Intent("com.su.workbox.FINISH_RULER"));
        } else {
            Intent intent = new Intent(this, RulerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(intent);
        }
    }
}
