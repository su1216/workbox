package com.su.workbox.widget.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.su.workbox.R;

public class PreferenceItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int mStartPadding;
    private int mEndPadding;

    public PreferenceItemDecoration(@NonNull Context context, int startPadding, int endPadding) {
        this(context, startPadding, endPadding, ContextCompat.getDrawable(context, R.drawable.workbox_recycler_view_linear_divider));
    }

    public PreferenceItemDecoration(@NonNull Context context, int startPadding, int endPadding, @NonNull Drawable drawable) {
        mStartPadding = startPadding;
        mEndPadding = endPadding;
        mDivider = drawable;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int left = parent.getPaddingLeft() + mStartPadding;
        final int right = parent.getWidth() - parent.getPaddingRight() - mEndPadding;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin + Math.round(child.getTranslationY());
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        int nextPosition = position + 1;
        int type = parent.getAdapter().getItemViewType(position);
        int nextType = HeaderFooterAdapter.TYPE_INVALID;
        if (nextPosition < parent.getAdapter().getItemCount()) {
            nextType = parent.getAdapter().getItemViewType(nextPosition);
        }
        if (nextType == HeaderFooterAdapter.TYPE_INVALID
                || type == HeaderFooterAdapter.TYPE_HEADER
                || type == HeaderFooterAdapter.TYPE_GROUP
                || (type == HeaderFooterAdapter.TYPE_CHILD && nextType == HeaderFooterAdapter.TYPE_GROUP)) {
            //ignore
            outRect.set(0, 0, 0, 0);
        } else {
            //1px height
            outRect.set(0, 0, 0, 1);
        }
    }
}
