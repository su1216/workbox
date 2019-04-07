package com.su.workbox.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import com.su.workbox.ui.main.WorkboxMainActivity;

/**
 * Created by mahao on 17-7-19.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    @Override
    public void onClick() {
        startActivityAndCollapse(new Intent(this, WorkboxMainActivity.class));
    }
}
