package com.su.workbox.ui.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.utils.UiHelper;

public class RulerView extends View {

    public static final String TAG = RulerView.class.getSimpleName();
    private static final int NONE = 0;
    private static final int HORIZONTAL = 1;
    private static final int VERTICAL = 2;
    private static final int SCALE_LENGTH = UiHelper.dp2px(4);
    private static final int SCALE_GAP = 5;
    public static final int TEXT_SIZE = UiHelper.sp2px(12);
    public static final int TEXT_PADDING = UiHelper.dp2px(8);
    private int mTouchSlop;
    private int mDirection;

    private PointF mInitPoint;
    private PointF mDownPoint = new PointF();
    private PointF mLastPoint = new PointF();
    private PointF mOldPoint = new PointF();
    private PointF mStartPoint = new PointF();
    private int mVerticalDpCount;
    private int mHorizontalDpCount;

    private float mMaxX;
    private float mMaxY;
    private float mMinY;
    private Paint mPaint;
    private Paint mOldPaint;
    private Paint mResultPaint;
    private final Paint.FontMetrics mFontMetrics;
    private final float mTextHeight;

    public RulerView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();

        SharedPreferences sp = SpHelper.getWorkboxSharedPreferences();
        boolean statusBar = sp.getBoolean(SpHelper.COLUMN_MEASURE_STATUS_BAR, false);
        boolean navigationBar = sp.getBoolean(SpHelper.COLUMN_MEASURE_NAVIGATION_BAR, false);
        String colorString = sp.getString(SpHelper.COLUMN_MEASURE_COLOR_STRING, "#B0FF0000");
        String resultColorString = sp.getString(SpHelper.COLUMN_MEASURE_RESULT_COLOR_STRING, "#B00000FF");

        mMaxX = GeneralInfoHelper.getScreenWidth();
        mMaxY = GeneralInfoHelper.getScreenHeight();
        if (!statusBar && navigationBar) {
            mMinY = GeneralInfoHelper.getStatusBarHeight();
        } else if (!statusBar && !navigationBar) {
            mMinY = GeneralInfoHelper.getStatusBarHeight();
            mMaxY -= GeneralInfoHelper.getNavigationBarHeight(); //ok
        }else if (statusBar && !navigationBar) {
            mMaxY -= GeneralInfoHelper.getNavigationBarHeight(); //ok
        }
        mInitPoint = new PointF(GeneralInfoHelper.getAvailableWidth() / 2.0f, (mMinY + mMaxY) / 2.0f);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor(colorString));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(UiHelper.dp2px(1.0f));

        mOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOldPaint.setColor(Color.parseColor(colorString));
        mOldPaint.setStyle(Paint.Style.STROKE);
        mOldPaint.setStrokeWidth(1);
        mOldPaint.setPathEffect(new DashPathEffect(new float[]{UiHelper.dp2px(3.0f), UiHelper.dp2px(3.0f)}, 0));

        mResultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mResultPaint.setColor(Color.parseColor(resultColorString));
        mResultPaint.setStyle(Paint.Style.FILL);
        mResultPaint.setStrokeWidth(UiHelper.dp2px(2.0f));
        mResultPaint.setTextSize(TEXT_SIZE);
        mResultPaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        mFontMetrics = mResultPaint.getFontMetrics();
        mTextHeight = mFontMetrics.descent - mFontMetrics.ascent;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            mVerticalDpCount = UiHelper.px2dp(h) + 1;
            mHorizontalDpCount = UiHelper.px2dp(w) + 1;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPoint = new PointF(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (mDirection != NONE) {
                    if (mInitPoint.x + x - mStartPoint.x < 0) {
                        x = mStartPoint.x - mInitPoint.x;
                    }else if (mInitPoint.x + x - mStartPoint.x > mMaxX) {
                        x = mMaxX + mStartPoint.x - mInitPoint.x;
                    }

                    if (mInitPoint.y + y - mStartPoint.y < mMinY) {
                        y = mMinY + mStartPoint.y - mInitPoint.y;
                    }else if (mInitPoint.y + y - mStartPoint.y > mMaxY) {
                        y = mMaxY + mStartPoint.y - mInitPoint.y;
                    }
                }

                mLastPoint = new PointF(x, y);
                float dx = mLastPoint.x - mDownPoint.x;
                float dy = mLastPoint.y - mDownPoint.y;
                if (mDirection == NONE) {
                    if (Math.abs(dx) > mTouchSlop) {
                        mDirection = HORIZONTAL;
                        mStartPoint.x = mLastPoint.x;
                        mOldPoint.x = mInitPoint.x;
                        if (mInitPoint.y < mMinY) {
                            mInitPoint.y = mMinY;
                        }
                    } else if (Math.abs(dy) > mTouchSlop) {
                        mDirection = VERTICAL;
                        mStartPoint.y = mLastPoint.y;
                        mOldPoint.y = mInitPoint.y;
                        if (mInitPoint.x < 0) {
                            mInitPoint.x = 0;
                        }
                    }
                } else {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mDirection == NONE) {
                    mOldPoint = new PointF(0, mMinY);
                    mInitPoint = new PointF(event.getX(), event.getY());
                } else {
                    if (mDirection == HORIZONTAL) {
                        mOldPoint.x = mInitPoint.x;
                        xLimitation(mOldPoint);
                        mInitPoint.x += event.getX() - mStartPoint.x;
                        xLimitation(mInitPoint);
                    } else if (mDirection == VERTICAL) {
                        mOldPoint.y = mInitPoint.y;
                        yLimitation(mOldPoint);
                        mInitPoint.y += event.getY() - mStartPoint.y;
                        yLimitation(mInitPoint);
                    }
                    mDirection = NONE;
                }
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void xLimitation(PointF pointF) {
        if (pointF.x < 0) {
            pointF.x = 0;
        } else if (pointF.x > mMaxX) {
            pointF.x = mMaxX;
        }
    }

    private void yLimitation(PointF pointF) {
        if (pointF.y < mMinY) {
            pointF.y = mMinY;
        } else if (pointF.y > mMaxY) {
            pointF.y = mMaxY;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        //尺子
        if (mInitPoint.y > 0) {
            canvas.drawLine(0, mInitPoint.y, measuredWidth, mInitPoint.y, mPaint);
        }
        if (mInitPoint.x > 0) {
            canvas.drawLine(mInitPoint.x, mMinY, mInitPoint.x, measuredHeight, mPaint);
        }
        //刻度
        for (int i = UiHelper.px2dp(mMinY); i < mVerticalDpCount; i += SCALE_GAP) {
            canvas.drawLine(mInitPoint.x, UiHelper.dp2px(i), mInitPoint.x + SCALE_LENGTH, UiHelper.dp2px(i), mPaint);
        }
        for (int i = 0; i < mHorizontalDpCount; i += SCALE_GAP) {
            canvas.drawLine(UiHelper.dp2px(i), mInitPoint.y, UiHelper.dp2px(i), mInitPoint.y + SCALE_LENGTH, mPaint);
        }

        if (mDirection == HORIZONTAL) {
            drawHorizontalMove(canvas);
        } else if (mDirection == VERTICAL) {
            drawVerticalMove(canvas);
        }
        // 滑动结束时上次位置虚线绘制
        if (mOldPoint.x > 0) {
            canvas.drawLine(mOldPoint.x, mMinY, mOldPoint.x, measuredHeight, mOldPaint);
        }
        if (mOldPoint.y > mMinY) {
            canvas.drawLine(0, mOldPoint.y, measuredWidth, mOldPoint.y, mOldPaint);
        }
    }

    private void drawHorizontalMove(Canvas canvas) {
        float distance = mLastPoint.x - mStartPoint.x;
        String textDp = UiHelper.px2dp(distance) + "dp";
        String textPx = (distance > 0 ? (int) (distance + 0.5f) : (int) distance) + "px";

        canvas.drawLine(mInitPoint.x + distance, mMinY, mInitPoint.x + distance, getMeasuredHeight(), mPaint);
        canvas.drawLine(mInitPoint.x, mInitPoint.y, mInitPoint.x + distance, mInitPoint.y, mResultPaint);
        mResultPaint.setTextAlign(Paint.Align.CENTER);

        float textX = mInitPoint.x + distance / 2;
        if (textX - mResultPaint.measureText(textPx) / 2 < 0) {
            textX = mResultPaint.measureText(textPx) / 2;
        } else if (textX + mResultPaint.measureText(textPx) / 2 > GeneralInfoHelper.getScreenWidth()) {
            textX = GeneralInfoHelper.getScreenWidth() - mResultPaint.measureText(textPx) / 2;
        }

        float pxY = mInitPoint.y - mFontMetrics.descent;
        float dpY = mInitPoint.y + mTextHeight;
        if (pxY <= -mFontMetrics.ascent) {
            pxY = -mFontMetrics.ascent;
            dpY = pxY + mFontMetrics.descent + mTextHeight;
        } else if (dpY > mMaxY - mFontMetrics.descent) {
            dpY = mMaxY - mFontMetrics.descent;
            pxY = dpY - mTextHeight - mFontMetrics.descent;
        }
        canvas.drawText(textDp, textX, dpY, mResultPaint);
        canvas.drawText(textPx, textX, pxY, mResultPaint);
    }

    private void drawVerticalMove(Canvas canvas) {
        float distance = mLastPoint.y - mStartPoint.y;
        String textDp = UiHelper.px2dp(distance) + "dp";
        String textPx = (distance > 0 ? (int) (distance + 0.5f) : (int) distance) + "px";

        canvas.drawLine(0, mInitPoint.y + distance, getMeasuredWidth(), mInitPoint.y + distance, mPaint);
        canvas.drawLine(mInitPoint.x, mInitPoint.y, mInitPoint.x, mInitPoint.y + distance, mResultPaint);

        float textX;
        mResultPaint.setTextAlign(Paint.Align.LEFT);
        if (mInitPoint.x > GeneralInfoHelper.getScreenWidth() / 2) {
            textX = mInitPoint.x - TEXT_PADDING - mResultPaint.measureText(textPx);
        } else {
            textX = mInitPoint.x + TEXT_PADDING;
        }

        float pxY = mInitPoint.y + distance / 2 - mFontMetrics.descent;
        float dpY = mInitPoint.y + distance / 2 + mTextHeight;
        if (pxY <= -mFontMetrics.ascent) {
            pxY = -mFontMetrics.ascent;
            dpY = pxY + mFontMetrics.descent + mTextHeight;
        } else if (dpY > mMaxY - mFontMetrics.descent) {
            dpY = mMaxY - mFontMetrics.descent;
            pxY = dpY - mTextHeight - mFontMetrics.descent;
        }
        canvas.drawText(textDp, textX, dpY, mResultPaint);
        canvas.drawText(textPx, textX, pxY, mResultPaint);
    }
}
