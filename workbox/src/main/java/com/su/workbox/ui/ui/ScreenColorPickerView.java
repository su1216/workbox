package com.su.workbox.ui.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.su.workbox.R;
import com.su.workbox.utils.UiHelper;

public class ScreenColorPickerView extends View {

    public static final String TEXT_FOCUS_INFO = "%s   %d,%d";
    private Paint mRingPaint;
    private Paint mBitmapPaint;
    private Paint mFocusPaint;
    private Paint mGridPaint;
    private Paint mGridShadowPaint;
    private TextPaint mTextPaint;
    private Path mClipPath = new Path();
    private Matrix mBitmapMatrix = new Matrix();
    private Rect mGridRect = new Rect();
    private RoundedBitmapDrawable mGridDrawable;
    private Bitmap mCircleBitmap;
    private String mText;
    private int mRingWidth;
    private int mBaseline;

    public ScreenColorPickerView(Context context) {
        super(context);
        init();
    }

    public ScreenColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScreenColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(Color.WHITE);
        mRingPaint.setStyle(Paint.Style.STROKE);

        mFocusPaint = new Paint();
        mFocusPaint.setAntiAlias(true);
        mFocusPaint.setStyle(Paint.Style.STROKE);
        mFocusPaint.setStrokeWidth(3.0f);
        mFocusPaint.setColor(Color.BLACK);

        mBitmapPaint = new Paint();
        mBitmapPaint.setFilterBitmap(false);

        mGridPaint = new Paint();
        //设置线宽。单位为1像素
        mGridPaint.setStrokeWidth(1.0f);
        mGridPaint.setStyle(Paint.Style.STROKE);
        //画笔颜色
        mGridPaint.setColor(-3355444);
        mGridShadowPaint = new Paint(mGridPaint);
        mGridShadowPaint.setColor(-12303292);

        float textSizeSp = 12.0f;
        float ringWidthSp = textSizeSp + 4.0f;
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setTextSize(UiHelper.sp2px(textSizeSp));
        mRingWidth = UiHelper.sp2px(ringWidthSp);
        Paint.FontMetricsInt fontMetricsInt = mTextPaint.getFontMetricsInt();
        mBaseline = (-mRingWidth - (fontMetricsInt.descent - fontMetricsInt.ascent)) / 2 - fontMetricsInt.ascent;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mClipPath.rewind();
        mClipPath.moveTo(0, 0);
        mClipPath.addCircle(getWidth() / 2.0f, getHeight() / 2.0f, getWidth() / 2.0f, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
        drawGrid(canvas);
        drawRing(canvas);
        drawText(canvas);
        drawFocus(canvas);
    }

    private void drawText(Canvas canvas) {
        if (!TextUtils.isEmpty(mText)) {
            float hOffset = (float) (getWidth() * Math.PI * (90 * 1.0 / 360));
            canvas.drawTextOnPath(mText, mClipPath, hOffset, mBaseline, mTextPaint);
            canvas.setDrawFilter(null);
        }
    }

    private void drawGrid(Canvas canvas) {
        if (mGridDrawable == null) {
            Bitmap gridBitmap = createGridBitmap(ScreenColorViewManager.PIX_INTERVAL, canvas);
            mGridDrawable = RoundedBitmapDrawableFactory.create(getResources(), gridBitmap);
            mGridDrawable.setBounds(0, 0, getRight(), getBottom());
            mGridDrawable.setCircular(true);
        }
        mGridDrawable.draw(canvas);
    }

    private Bitmap createGridBitmap(int pixInterval, Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        canvas.getClipBounds(mGridRect);

        Bitmap gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas gridCanvas = new Canvas(gridBitmap);

        if (pixInterval >= 4) {
            int alpha = Math.min(pixInterval * 36, 255);
            mGridPaint.setAlpha(alpha);
            mGridShadowPaint.setAlpha(alpha);
            float value;
            float start;
            float end;
            gridCanvas.save();
            for (int i = 0; i <= getWidth(); i += pixInterval) {
                value = (float) (i - 1);
                start = 0f;
                end = (float) height;
                gridCanvas.drawLine(value, start, value, end, this.mGridPaint);
                value = (float) i;
                gridCanvas.drawLine(value, start, value, end, this.mGridShadowPaint);
            }
            for (int i = 0; i <= getHeight(); i += pixInterval) {
                value = (float) (i - 1);
                start = 0f;
                end = (float) width;
                gridCanvas.drawLine(start, value, end, value, this.mGridPaint);
                value = (float) i;
                gridCanvas.drawLine(start, value, end, value, this.mGridShadowPaint);
            }
            gridCanvas.restore();
        }
        return gridBitmap;
    }

    private void drawFocus(Canvas canvas) {
        float focusWidth = ScreenColorViewManager.PIX_INTERVAL + 4.0f;
        canvas.drawRect(getWidth() / 2.0f - 2.0f,
                getWidth() / 2.0f - 2.0f,
                getWidth() / 2.0f + focusWidth - 2.0f,
                getWidth() / 2.0f + focusWidth - 2.0f,
                mFocusPaint);
    }

    private void drawRing(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mRingPaint.setStrokeWidth(mRingWidth);
        canvas.drawCircle(getWidth() / 2.0f, getWidth() / 2.0f, getWidth() / 2.0f - mRingWidth / 2.0f, mRingPaint);
        mRingPaint.setColor(getResources().getColor(R.color.workbox_first_text));
        mRingPaint.setStrokeWidth(0.5f);
        canvas.drawCircle(getWidth() / 2.0f, getWidth() / 2.0f, getWidth() / 2.0f - 0.5f, mRingPaint);
        canvas.drawCircle(getWidth() / 2.0f, getWidth() / 2.0f, getWidth() / 2.0f - mRingWidth, mRingPaint);
    }

    private void drawBitmap(Canvas canvas) {
        if (mCircleBitmap == null || mCircleBitmap.isRecycled()) {
            return;
        }
        canvas.save();
        canvas.clipPath(mClipPath);
        mBitmapMatrix.reset();
        mBitmapMatrix.postScale(getWidth() / (float) mCircleBitmap.getWidth(), getHeight() / (float) mCircleBitmap.getHeight());
        canvas.drawBitmap(mCircleBitmap, mBitmapMatrix, mBitmapPaint);
        canvas.restore();
    }

    public void setBitmap(Bitmap bitmap, int color, int x, int y) {
        mCircleBitmap = bitmap;
        mText = String.format(TEXT_FOCUS_INFO, parseColorInt(color), x, y);
        mRingPaint.setColor(color);
        if (UiHelper.isDarkColor(color)) {
            mFocusPaint.setColor(Color.WHITE);
            mTextPaint.setColor(Color.WHITE);
        } else {
            mFocusPaint.setColor(Color.BLACK);
            mTextPaint.setColor(Color.BLACK);
        }
        invalidate();
    }

    public static String parseColorInt(@ColorInt int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }
}