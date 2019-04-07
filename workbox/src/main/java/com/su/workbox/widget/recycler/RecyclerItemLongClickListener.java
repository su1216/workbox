package com.su.workbox.widget.recycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by su on 18-1-13.
 */
public class RecyclerItemLongClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemLongClickListener mListener;
    private RecyclerView mRecyclerView;

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    private GestureDetector mGestureDetector;

    public RecyclerItemLongClickListener(Context context, OnItemLongClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    int position = mRecyclerView.getChildAdapterPosition(childView);
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemLongClick(childView, position);
                    }
                }
            }
        });
        mGestureDetector.setIsLongpressEnabled(true);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
        if (mRecyclerView == null) {
            mRecyclerView = view;
        }
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
