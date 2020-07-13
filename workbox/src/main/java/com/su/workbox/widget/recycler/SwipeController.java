package com.su.workbox.widget.recycler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import com.su.workbox.utils.UiHelper;

public class SwipeController extends ItemTouchHelper.Callback {

    private static final String DELETE = "DELETE";
    private final Paint.FontMetricsInt mFontMetricsInt;
    private OnSwipeListener mOnSwipeListener;
    private final ColorDrawable mBackground;
    private Paint mDeletePaint;
    private int mTextPadding;
    private float mTextWidth;

    public SwipeController(@NonNull OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
        mBackground = new ColorDrawable(Color.RED);
        mDeletePaint = new Paint();
        mDeletePaint.setColor(Color.WHITE);
        mDeletePaint.setTextSize(UiHelper.dp2px(18));
        mDeletePaint.setFakeBoldText(true);
        mTextWidth = mDeletePaint.measureText(DELETE);
        mFontMetricsInt = mDeletePaint.getFontMetricsInt();
        mTextPadding = UiHelper.dp2px(12);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
        mOnSwipeListener.onDelete(viewHolder);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        if (dX > 0) { // to the right
            mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
        } else if (dX < 0) { // to the left
            mBackground.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            mBackground.setBounds(0, 0, 0, 0);
        }
        mBackground.draw(c);
        c.drawText(DELETE, itemView.getRight() - mTextWidth - mTextPadding, itemView.getTop() + UiHelper.getTextBaseline(itemView.getHeight(), mFontMetricsInt),
                   mDeletePaint);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public interface OnSwipeListener {
        void onDelete(@NonNull RecyclerView.ViewHolder viewHolder);

        void onUndo();
    }
}
