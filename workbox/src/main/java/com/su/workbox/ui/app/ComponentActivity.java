package com.su.workbox.ui.app;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.ui.base.BaseAppCompatActivity;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 组件信息 - 四大组件列表 - 四大组件详情
 */
public class ComponentActivity extends BaseAppCompatActivity {

    private static final String TAG = ComponentActivity.class.getSimpleName();
    private String mType;
    private ComponentInfo mComponentInfo;
    private NoteComponentEntity mNoteComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_container);
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        mComponentInfo = intent.getParcelableExtra("info");
        mNoteComponent = intent.getParcelableExtra("note");
        if (mNoteComponent == null) {
            mNoteComponent = new NoteComponentEntity();
        }

        Fragment fragment = ComponentInfoFragment.newInstance(mType, mNoteComponent, mComponentInfo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.root_layout, fragment)
                .commit();
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
