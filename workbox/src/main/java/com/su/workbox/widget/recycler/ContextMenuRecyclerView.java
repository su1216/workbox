package com.su.workbox.widget.recycler;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewParent;

public class ContextMenuRecyclerView extends RecyclerView {

    private RecyclerViewContextMenuInfo mContextMenuInfo;

    public ContextMenuRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ContextMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContextMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        View itemRootView = getItemRootView(originalView);
        final int longPressPosition = getChildAdapterPosition(itemRootView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId, originalView);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    private View getItemRootView(View child) {
        ViewParent parent =  child.getParent();
        while (!(parent instanceof RecyclerView)) {
            child = (View) parent;
            parent = parent.getParent();
        }
        return child;
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        final public int position;
        final public long id;
        final public View originalView;

        public RecyclerViewContextMenuInfo(int position, long id, View originalView) {
            this.position = position;
            this.id = id;
            this.originalView = originalView;
        }
    }
}
