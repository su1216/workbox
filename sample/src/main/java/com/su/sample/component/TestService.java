package com.su.sample.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.su.annotations.NoteComponent;
import com.su.annotations.Parameter;

@NoteComponent(description = "Service测试",
        type = "service",
        parameters = {@Parameter(parameterName = "cmd", parameterClass = String.class)})
public class TestService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendBroadcast(new Intent("test"), "com.su.sample.test");
        String cmd = intent.getStringExtra("cmd");
        Log.w("TestService", cmd);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
