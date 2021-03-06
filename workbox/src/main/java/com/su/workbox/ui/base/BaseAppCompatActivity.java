package com.su.workbox.ui.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.su.workbox.R;
import com.su.workbox.ui.WebViewActivity;

/**
 * Created by su on 18-1-2.
 */
@SuppressLint("Registered")
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    public void setTitle(@Nullable String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleRes) {
        setTitle(getString(titleRes));
    }

    protected abstract String getTag();

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = findViewById(R.id.id_toolbar);
        if (mToolbar != null) {
            int menuRes = menuRes();
            if (menuRes != 0) {
                mToolbar.inflateMenu(menuRes);
            }
            mToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
            mToolbar.setNavigationOnClickListener(v -> popStackIfNeeded(BaseAppCompatActivity.this));
        }
    }

    @MenuRes
    public int menuRes() {
        return 0;
    }

    private static void popStackIfNeeded(BaseAppCompatActivity activity) {
        int count = activity.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (activity instanceof WebViewActivity) {
                activity.finish();
            } else {
                activity.onBackPressed();
            }
        } else {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mToolbar != null) {
                mToolbar.showOverflowMenu();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    protected void installOnTitleDoubleClickListener(@NonNull OnTitleDoubleClickListener listener) {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureListener());
        mToolbar.setOnTouchListener((v, event) -> {
            boolean intercept = gestureDetector.onTouchEvent(event);
            if (intercept) {
                listener.onTitleDoubleClick(v);
            }
            return intercept;
        });
    }

    private static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }

    public interface OnTitleDoubleClickListener {
        void onTitleDoubleClick(View view);
    }
}
