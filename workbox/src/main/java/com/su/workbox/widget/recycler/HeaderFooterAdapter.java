package com.su.workbox.widget.recycler;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by su on 16-7-20.
 */
public abstract class HeaderFooterAdapter extends RecyclerView.Adapter<HeaderFooterAdapter.HeaderFooterViewHolder> {

    public static final int TYPE_INVALID = -10000;
    public static final int TYPE_CHILD = -4;
    public static final int TYPE_GROUP = -3;
    public static final int TYPE_HEADER = -2;
    public static final int TYPE_FOOTER = -1;
    public static final int TYPE_NORMAL = 0;

    private boolean mHasFooter;
    private boolean mHasHeader;

    protected HeaderFooterAdapter(boolean hasHeader, boolean hasFooter) {
        mHasHeader = hasHeader;
        mHasFooter = hasFooter;
    }

    @NonNull
    @Override
    public abstract HeaderFooterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull HeaderFooterViewHolder holder, int position);

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        if (isPositionFooter(position))
            return TYPE_FOOTER;
        return getItemDataType(position);
    }

    public abstract int getItemDataType(int position);

    public static boolean isHeader(int viewType) {
        return viewType == TYPE_HEADER;
    }

    public int countHeader() {
        return mHasHeader ? 1 : 0;
    }

    public int countFooter() {
        return mHasFooter ? 1 : 0;
    }

    public static boolean isFooter(int viewType) {
        return viewType == TYPE_FOOTER;
    }

    private boolean isPositionHeader(int position) {
        return mHasHeader && position == 0;
    }

    private boolean isPositionFooter(int position) {
        return mHasFooter && position == getItemCount() - 1;
    }

    public void removeFooter() {
        mHasFooter = false;
    }

    public void addFooter() {
        mHasFooter = true;
    }

    public static class HeaderFooterViewHolder extends RecyclerView.ViewHolder {
        private int mType;

        public HeaderFooterViewHolder(View itemView, int type) {
            super(itemView);
            mType = type;
        }

        public int getType() {
            return mType;
        }
    }
}
