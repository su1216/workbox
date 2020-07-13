package com.su.workbox.widget.recycler;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by su on 16-8-2.
 */
public class GridItemSpaceDecoration extends RecyclerView.ItemDecoration {
    private int mSpacing;
    private int mSpanCount;

    public GridItemSpaceDecoration(int spanCount, int itemOffset) {
        mSpacing = itemOffset / 2;
        mSpanCount = spanCount;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(mSpacing, mSpacing, mSpacing, mSpacing);
        int position = parent.getChildLayoutPosition(view);
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int count = adapter.getItemCount();
        if (isLastLine(position, count)) {
            outRect.bottom = 0;
        }
    }

    private boolean isLastLine(int position, int count) {
        if (count % mSpanCount != 0) {
            return position >= (count / mSpanCount * mSpanCount);
        } else {
            return position >= ((count / mSpanCount - 1) * mSpanCount);
        }
    }
}
