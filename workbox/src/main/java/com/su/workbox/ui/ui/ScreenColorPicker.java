package com.su.workbox.ui.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;

import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenColorPicker implements DisplayManager.DisplayListener, ImageReader.OnImageAvailableListener {

    public static final String TAG = ScreenColorPicker.class.getSimpleName();
    private static ScreenColorPicker sInstance;

    private DisplayManager mDisplayManager;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private int mRotation;

    private Surface mSurface;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Bitmap mTempBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private volatile boolean mRecording;

    private ScreenColorPicker() {
        Context context = GeneralInfoHelper.getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        Log.d(TAG, "displayId: " + mDisplay.getDisplayId());
        mRotation = mDisplay.getRotation();
        mDisplayMetrics = new DisplayMetrics();
        mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);

        int width = GeneralInfoHelper.getScreenWidth();
        int height = GeneralInfoHelper.getScreenHeight();
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        mSurface = mImageReader.getSurface();
        mImageReader.setOnImageAvailableListener(this, null);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        if (mBitmapWidth == 0) {
            initBitmapSize();
        }
    }

    public static ScreenColorPicker getInstance() {
        if (sInstance == null) {
            sInstance = new ScreenColorPicker();
        }
        return sInstance;
    }

    public void prepare(int resultCode, Intent resultData) {
        Context context = GeneralInfoHelper.getContext();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager == null) {
            Log.d(TAG, "mediaProjectionManager = null");
            return;
        }

        int width = GeneralInfoHelper.getScreenWidth();
        int height = GeneralInfoHelper.getScreenHeight();
        if (mMediaProjection == null) {
            mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        }
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(context.getPackageName(), width, height, displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mSurface, null, null);
        } else if (mVirtualDisplay.getSurface() == null) {
            mVirtualDisplay.setSurface(mSurface);
        }
    }

    private void initBitmapSize() {
        Log.d(TAG, "initBitmapSize");
        Image image = null;
        try {
            image = mImageReader.acquireLatestImage();
            if (image == null) {
                return;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final Image.Plane plane0 = planes[0];
            int pixelStride = plane0.getPixelStride(); //4
            int rowStride = plane0.getRowStride(); //4352
            int rowPadding = rowStride - pixelStride * width;
            mBitmapWidth = width + rowPadding / pixelStride;
            mBitmapHeight = height;
            image.close();
        } finally {
            mRecording = false;
            IOUtil.close(image);
        }
    }

    public void recording() {
        if (mRecording) {
            return;
        }
        mRecording = true;
        Image image = null;
        try {
            int format = mImageReader.getImageFormat();
            image = mImageReader.acquireLatestImage();
            Log.d(TAG, "format: " + format);
            if (image == null) {
                return;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final Image.Plane plane0 = planes[0];
            final ByteBuffer buffer = plane0.getBuffer();
            int pixelStride = plane0.getPixelStride(); //4
            int rowStride = plane0.getRowStride(); //4352
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = resetTempBitmap(width + rowPadding / pixelStride, height);
            Log.d(TAG, "bitmap width: " + bitmap.getWidth() + " - height: " + bitmap.getHeight());
            bitmap.copyPixelsFromBuffer(buffer);
            image.close();
        } finally {
            mRecording = false;
            IOUtil.close(image);
        }
    }

    private Bitmap resetTempBitmap(int width, int height) {
        if (mTempBitmap != null) {
            mTempBitmap.recycle();
        }
        mTempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return mTempBitmap;
    }

    public Bitmap getFocusedBitmap(Point leftTopPoint) {
//        Log.d(TAG, "leftTopPoint: " + leftTopPoint);
        int width, height;
        int cropLeftTopX, cropLeftTopY;
        int contentLeftTopX, contentLeftTopY;
        width = height = ScreenColorViewManager.PICK_AREA_SIZE;

        if (mTempBitmap == null) {
            return null;
        }

        boolean background = false;
        if (leftTopPoint.x < 0) {
            cropLeftTopX = 0;
            contentLeftTopX = -leftTopPoint.x;
            width += leftTopPoint.x;
            background = true;
        } else if (leftTopPoint.x + width > mTempBitmap.getWidth()) {
            cropLeftTopX = leftTopPoint.x;
            contentLeftTopX = 0;
            width = mTempBitmap.getWidth() - leftTopPoint.x;
            background = true;
        } else {
            cropLeftTopX = leftTopPoint.x;
            contentLeftTopX = 0;
        }
        if (leftTopPoint.y < 0) {
            cropLeftTopY = 0;
            contentLeftTopY = -leftTopPoint.y;
            height += leftTopPoint.y;
            background = true;
        } else if (leftTopPoint.y + height > mTempBitmap.getHeight()) {
            cropLeftTopY = leftTopPoint.y;
            contentLeftTopY = 0;
            height = mTempBitmap.getHeight() - leftTopPoint.y;
            background = true;
        } else {
            cropLeftTopY = leftTopPoint.y;
            contentLeftTopY = 0;
        }

//        Log.d(TAG, "width: " + width + " - height: " + height);
        if (width > 0 && height > 0) {
            Bitmap content = Bitmap.createBitmap(mTempBitmap, cropLeftTopX, cropLeftTopY, width, height);
            if (background) {
                Bitmap blackBitmap = createBlackBitmap(ScreenColorViewManager.PICK_AREA_SIZE, ScreenColorViewManager.PICK_AREA_SIZE);
                Canvas canvas = new Canvas(blackBitmap);
                canvas.drawBitmap(content, contentLeftTopX, contentLeftTopY, null);
                return blackBitmap;
            }
            return content;
        } else {
            return createBlackBitmap(ScreenColorViewManager.PICK_AREA_SIZE, ScreenColorViewManager.PICK_AREA_SIZE);
        }
    }

    private Bitmap createBlackBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.BLACK);
        return bitmap;
    }

    public boolean isRecording() {
        return mRecording;
    }

    public void release() {
        mRecording = false;
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        mSurface = null;

        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        mDisplayManager.unregisterDisplayListener(this);
        sInstance = null;
    }

    @Override
    public void onDisplayAdded(int displayId) {}

    @Override
    public void onDisplayRemoved(int displayId) {}

    @Override
    public void onDisplayChanged(int displayId) {
        Log.d(TAG, "onDisplayChanged: " + displayId);
        if (mDisplay.getDisplayId() != displayId) {
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        displayMetrics.setTo(mDisplayMetrics);
        mDisplay.getRealMetrics(mDisplayMetrics);

        int rotation = mDisplay.getRotation();
        if ((displayMetrics.equals(mDisplayMetrics)) && (mRotation == rotation)) {
            return;
        }
        Log.d(TAG, "real change");
        mRotation = rotation;
        release();
    }

    public int getBitmapWidth() {
        return mBitmapWidth;
    }

    public int getBitmapHeight() {
        return mBitmapHeight;
    }
}
