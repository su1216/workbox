package com.su.workbox.ui.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {

    private static final float HUE_WIDTH_RATE = 1.0f / 12;
    private static final float HUE_HEIGHT_RATE = 5.0f / 6;
    private static final float DIVIDER_WIDTH_RATE = 1.0f / 16;
    private int[] mColors = new int[]{
            0xFFFF0000,// red
            0xFFFF00FF,// magenta
            0xFF0000FF,// blue
            0xFF00FFFF,// cyan
            0xFF00FF00,// green
            0xFFFFFF00,// yellow
            0xFFFF0000,// red
    };

    /**
     * 色调H
     * 用角度度量，取值范围为0°～360°，从红色开始按逆时针方向计算，红色为0°，绿色为120°,蓝色为240°。它们的补色是：黄色为60°，青色为180°,品红为300°
     */
    private float mHue = 360.0f;
    /**
     * 饱和度S表示颜色接近光谱色的程度。一种颜色，可以看成是某种光谱色与白色混合的结果。
     * 其中光谱色所占的比例愈大，颜色接近光谱色的程度就愈高，颜色的饱和度也就愈高。饱和度高，颜色则深而艳。
     * 光谱色的白光成分为0，饱和度达到最高。通常取值范围为0%～100%，值越大，颜色越饱和。
     */
    private float mSaturation = 1.0f;
    /**
     * 明度表示颜色明亮的程度，对于光源色，明度值与发光体的光亮度有关；对于物体色，此值和物体的透射比或反射比有关。通常取值范围为0%（黑）到100%（白）。
     */
    private float mValue = 1.0f;
    private int mAlpha = 255;

    private Paint mHuePaint;
    private Paint mSaturationPaint;
    private Paint mValuePaint;
    private RectF mHueRectF;
    private RectF mSatValRectF;
    private RectF mAlphaRectF;

    private Point mDownPoint;
    private OnColorChangedListener mOnColorChangedListener;
    private LinearGradient mValShader;
    private LinearGradient mSatShader;
    private LinearGradient mAlphaShader;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHuePaint.setStyle(Paint.Style.FILL);
        mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSaturationPaint.setStyle(Paint.Style.FILL);
        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setStyle(Paint.Style.FILL);
        //使得ComposeShader生效
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            mHueRectF = new RectF(w * (1 - HUE_WIDTH_RATE), 0, w, h * HUE_HEIGHT_RATE);
            mSatValRectF = new RectF(0, 0, w * (1 - HUE_WIDTH_RATE - DIVIDER_WIDTH_RATE), h * HUE_HEIGHT_RATE);
            mAlphaRectF = new RectF(0, h * (HUE_HEIGHT_RATE + DIVIDER_WIDTH_RATE), w, h);

            Shader colorShader = new LinearGradient(w * (1 - HUE_WIDTH_RATE / 2), 0, w * (1 - HUE_WIDTH_RATE / 2), h * HUE_HEIGHT_RATE, mColors, null,
                                                    Shader.TileMode.CLAMP);
            mHuePaint.setShader(colorShader);
            mValShader = new LinearGradient(mSatValRectF.left, mSatValRectF.top, mSatValRectF.left, mSatValRectF.bottom, 0xFFFFFFFF, 0xFF000000,
                                            Shader.TileMode.CLAMP);
            onHueChanged();
            onSatValChanged();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean invalidate = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownPoint = new Point((int) event.getX(), (int) event.getY());
                invalidate = updateHsvAndAlpha(event);
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate = updateHsvAndAlpha(event);
                break;
            case MotionEvent.ACTION_UP:
                mDownPoint = null;
                invalidate = updateHsvAndAlpha(event);
                break;
            default:
                break;
        }

        if (invalidate) {
            if (mOnColorChangedListener != null) {
                mOnColorChangedListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{mHue, mSaturation, mValue}));
            }
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean updateHsvAndAlpha(MotionEvent event) {
        if (mDownPoint == null) {
            return false;
        }

        boolean invalidate = false;
        int startX = mDownPoint.x;
        int startY = mDownPoint.y;
        if (mHueRectF.contains(startX, startY)) {
            hue(event.getY());
            invalidate = true;
        } else if (mSatValRectF.contains(startX, startY)) {
            satVal(event.getX(), event.getY());
            invalidate = true;
        } else if (mAlphaRectF != null && mAlphaRectF.contains(startX, startY)) {
            alpha(event.getX());
            invalidate = true;
        }

        return invalidate;
    }

    private void hue(float y) {
        float height = mHueRectF.height();
        if (y < mHueRectF.top) {
            y = 0.0f;
        } else if (y > mHueRectF.bottom) {
            y = height;
        } else {
            y = y - mHueRectF.top;
        }
        float hue = 360.0f - y * 360.0f / height;
        if (mHue != hue) {
            mHue = hue;
            onHueChanged();
            onSatValChanged();
        }
    }

    private void satVal(float x, float y) {
        float[] result = new float[2];
        float width = mSatValRectF.width();
        float height = mSatValRectF.height();
        if (x < mSatValRectF.left) {
            x = 0.0f;
        } else if (x > mSatValRectF.right) {
            x = width;
        } else {
            x = x - mSatValRectF.left;
        }

        if (y < mSatValRectF.top) {
            y = 0.0f;
        } else if (y > mSatValRectF.bottom) {
            y = height;
        } else {
            y = y - mSatValRectF.top;
        }

        result[0] = 1.f / width * x;
        result[1] = 1.f - (1.f / height * y);
        if (mSaturation != result[0] || mValue != result[1]) {
            mSaturation = result[0];
            mValue = result[1];
            onSatValChanged();
        }
    }

    private void alpha(float x) {
        float width = mAlphaRectF.width();
        if (x < mAlphaRectF.left) {
            x = 0;
        } else if (x > mAlphaRectF.right) {
            x = width;
        } else {
            x = x - mAlphaRectF.left;
        }
        mAlpha = (int) (0xFF - x * 0xFF / width);
    }

    private void onHueChanged() {
        int rgb = Color.HSVToColor(new float[]{mHue, 1.0f, 1.0f});
        mSatShader = new LinearGradient(mSatValRectF.left, mSatValRectF.top, mSatValRectF.right, mSatValRectF.top, 0xFFFFFFFF, rgb, Shader.TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(mValShader, mSatShader, PorterDuff.Mode.MULTIPLY);
        mSaturationPaint.setShader(composeShader);
    }

    private void onSatValChanged() {
        float[] hsv = new float[]{mHue, mSaturation, mValue};
        int color = Color.HSVToColor(hsv);
        int alphaColor = Color.HSVToColor(0, hsv);
        mAlphaShader = new LinearGradient(mAlphaRectF.left, mAlphaRectF.top, mAlphaRectF.right, mAlphaRectF.top, color, alphaColor, Shader.TileMode.CLAMP);
        mValuePaint.setShader(mAlphaShader);
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mHue = hsv[0];
        mSaturation = hsv[1];
        mValue = hsv[2];
        mAlpha = (color >> 24) & 0xFF;
        //view未初始化完成
        if (mSatValRectF == null || mAlphaRectF == null) {
            return;
        }
        onHueChanged();
        onSatValChanged();
        invalidate();
    }

    public int getColor() {
        return Color.HSVToColor(mAlpha, new float[]{mHue, mSaturation, mValue});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(mSatValRectF, mSaturationPaint);
        canvas.drawRect(mHueRectF, mHuePaint);
        canvas.drawRect(mAlphaRectF, mValuePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    public void setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        mOnColorChangedListener = onColorChangedListener;
    }

    public interface OnColorChangedListener {
        void onColorChanged(int newColor);
    }
}
