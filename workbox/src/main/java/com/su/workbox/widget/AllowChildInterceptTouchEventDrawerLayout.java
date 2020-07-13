package com.su.workbox.widget;

import android.content.Context;
import android.graphics.Rect;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by su on 17-5-3.
 */

public class AllowChildInterceptTouchEventDrawerLayout extends DrawerLayout {
    private int mInterceptTouchEventChildId;

    public void setInterceptTouchEventChildId(int id) {
        this.mInterceptTouchEventChildId = id;
    }

    public AllowChildInterceptTouchEventDrawerLayout(Context context) {
        super(context);
    }

    public AllowChildInterceptTouchEventDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mInterceptTouchEventChildId > 0) {
            View scroll = findViewById(mInterceptTouchEventChildId);
            if (scroll != null) {
                Rect rect = new Rect();
                scroll.getHitRect(rect);
                if (scroll instanceof HorizontalScrollView) {
                    HorizontalScrollView horizontalScrollView = (HorizontalScrollView) scroll;
                    if (horizontalScrollView.getScrollX() != 0) {
                        if (rect.contains((int) ev.getX(), (int) ev.getY())) {
                            return false;
                        }
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
