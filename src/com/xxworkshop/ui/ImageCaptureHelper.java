package com.xxworkshop.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.xxworkshop.common.F;
import com.xxworkshop.common.Graphics;
import com.xxworkshop.common.L;
import com.xxworkshop.common.formatter.Anchor;
import com.xxworkshop.common.formatter.Rect;
import com.xxworkshop.ui.imagecapture.FocusView;
import com.xxworkshop.ui.imagecapture.ImageCaptureListener;


public class ImageCaptureHelper implements View.OnTouchListener {
    /**
     * 拖动模式
     */
    private static final int MODE_DRAG = 1;
    /**
     * 缩放模式
     */
    private static final int MODE_ZOOM = 2;
    /**
     * 没有模式
     */
    private static final int MODE_NONE = 3;

    private int mMode = MODE_NONE;

    private ImageView imageView = null;

    private FocusView focusView = null;

    private Rect rect;

    private Bitmap bitmap = null;

    private ImageCaptureListener listener;

    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();

    private PointF mStartPoint = new PointF();
    private PointF mZoomPoint = new PointF();
    private float mOldDist = 1f;

    private float[] mMatrixValues = new float[9];

    private float mMiniScale = 1f;

    private float moveX = 0f;
    private float moveY = 0f;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Math.abs(moveX) > 150 && Math.abs(moveY) > 150) {
                float x = 100f;
                float y = 100f;
                if (moveX < 0) {
                    x = -100f;
                }
                if (y < 0) {
                    y = -100f;
                }
                mMatrix.postTranslate(x, y);
                moveX = moveX > 0 ? moveX - 100f : moveX + 100f;
                moveY = moveY > 0 ? moveY - 100f : moveY + 100f;
            } else {
                if (moveX > 25 || moveY > 25) {
                    mMatrix.postTranslate(moveX / 2, moveY / 2);
                    moveX = moveX / 2;
                    moveY = moveY / 2;
                } else {
                    mMatrix.postTranslate(moveX, moveY);
                    moveX = 0;
                    moveY = 0;
                }
            }
            imageView.setImageMatrix(mMatrix);
            if (moveX != 0 || moveY != 0) {
                handler.sendEmptyMessageDelayed(0, 50);
            } else {
                mMatrix.set(imageView.getImageMatrix());
                mMatrix.getValues(mMatrixValues);
            }
        }
    };

    //	Matrix的Value是一个3x3的矩阵，文档中的Matrix获取数据的方法是void getValues(float[] values)，对于Matrix内字段的顺序
//	并没有很明确的说明，经过测试发现他的顺序是这样的
//	MSCALE_X	MSKEW_X		MTRANS_X
//	MSKEW_Y		MSCALE_Y	MTRANS_Y
//	MPERSP_0	MPERSP_1	MPERSP_2	
    public ImageCaptureHelper(Context context, FrameLayout rootView, ImageCaptureListener listener) {
        imageView = new ImageView(context);
        this.listener = listener;

        rootView.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        focusView = new FocusView(context);
        rootView.addView(focusView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button buttonSubmit = new Button(context);
        buttonSubmit.setText("OK");
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageCaptureHelper.this.listener.onCapture(capture());
            }
        });
        FrameLayout.LayoutParams lpSubmit = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lpSubmit.gravity = Gravity.LEFT | Gravity.BOTTOM;
        lpSubmit.leftMargin = 10;
        lpSubmit.rightMargin = 10;
        rootView.addView(buttonSubmit, lpSubmit);

        Button buttonCancel = new Button(context);
        buttonCancel.setText("Cancel");
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageCaptureHelper.this.listener.onCancel();
            }
        });
        FrameLayout.LayoutParams lpCancel = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lpCancel.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        lpCancel.rightMargin = 10;
        lpCancel.bottomMargin = 10;
        rootView.addView(buttonCancel, lpCancel);


        imageView.setOnTouchListener(this);
    }

    public void setRect(Rect rect, Anchor anchor) {
        focusView.init(rect, anchor);
        this.rect = F.convertRect(rect, anchor);
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        imageView.setScaleType(ScaleType.FIT_CENTER);
        imageView.setImageBitmap(bitmap);
        mMatrix.set(imageView.getImageMatrix());
        mMatrix.getValues(mMatrixValues);
    }

    /**
     * 保存剪切的
     *
     * @return 剪切的图像
     */
    public Bitmap capture() {
        if (bitmap != null) {
//			焦点框内的图片为缩放的图片，起始坐标（相对屏幕）有可能小于零，
//			可以通过0 + (focusView.getFocusLeft() - mMatrixValues[2])获得真实的坐标（图片时从0开始的），
//			但是这个还是缩放的，别忘了除以缩放比例
            int left = (int) ((rect.x - mMatrixValues[2]) / mMatrixValues[0]);
            int top = (int) ((rect.y - mMatrixValues[5]) / mMatrixValues[4]);
            int right = (int) ((rect.x + rect.w - mMatrixValues[2]) / mMatrixValues[0]);
            int bottom = (int) ((rect.y + rect.h - mMatrixValues[5]) / mMatrixValues[4]);
            correctSize(left, top, right, bottom);
            //截图
            //截图时需传递的参数为:原始图片,截图框左/上边线距原始图片左/上边线距离,截图框宽/高
            Bitmap bitmap = Graphics.cutImage(this.bitmap, new Rect(left, top, right - left, bottom - top));
//            Bitmap bitmap = Bitmap.createBitmap(bitmap, left, top, right-left, bottom-top);

            //缩放图片,控制图片边长

            return Graphics.zoomImage(bitmap, 300f, 300f);
        }
        return null;
    }

    /**
     * 修正图片，获得真实的边界（防止焦点框不包含图片外部时出错）
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void correctSize(int left, int top, int right, int bottom) {
        mMatrix.getValues(mMatrixValues);
        int bitmapLeft = (int) mMatrixValues[2];
        int bitmapTop = (int) mMatrixValues[5];
        int bitmapRight = (int) (bitmap.getWidth() * mMatrixValues[0] - mMatrixValues[2]);
        int bitmapBottom = (int) (bitmap.getHeight() * mMatrixValues[4] - mMatrixValues[5]);
        if (bitmapLeft > rect.x) {
            left += (bitmapLeft - rect.x) / mMatrixValues[0];
        }
        if (bitmapTop > rect.y) {
            top += (bitmapTop - rect.y) / mMatrixValues[4];
        }
        if (bitmapRight < (rect.x + rect.w)) {
            right -= ((rect.x + rect.w) - bitmapRight) / mMatrixValues[0];
        }
        if (bitmapBottom < (rect.y + rect.h)) {
            bottom -= ((rect.y + rect.h) - bitmapBottom) / mMatrixValues[4];
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
//		如果在API8以下的版本使用，采用FloatMath.sqrt()会更快，但是在API8和以上版本，Math.sqrt()更快
//		原文：Use java.lang.Math#sqrt instead of android.util.FloatMath#sqrt() since it is faster as of API 8
//		return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                actionPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mMode = MODE_NONE;
                mMatrix.getValues(mMatrixValues);
                L.log("MSCALE_X = " + mMatrixValues[0] + "; MSKEW_X = " + mMatrixValues[1] + "; MTRANS_X = " + mMatrixValues[2]
                        + "; \nMSCALE_Y = " + mMatrixValues[4] + "; MSKEW_Y = " + mMatrixValues[3] + "; MTRANS_Y = " + mMatrixValues[5]
                        + "; \nMPERSP_0 = " + mMatrixValues[6] + "; MPERSP_1 = " + mMatrixValues[7] + "; MPERSP_2 = " + mMatrixValues[8]);
                checkLocation();
                break;
        }
        imageView.setImageMatrix(mMatrix);
        return true;
    }

    private void actionDown(MotionEvent event) {
        imageView.setScaleType(ScaleType.MATRIX);
        mMatrix.set(imageView.getImageMatrix());
        mSavedMatrix.set(mMatrix);
        mStartPoint.set(event.getX(), event.getY());
        mMode = MODE_DRAG;
    }

    private void actionPointerDown(MotionEvent event) {
        mOldDist = spacing(event);
        if (mOldDist > 10f) {
            mSavedMatrix.set(mMatrix);
            midPoint(mZoomPoint, event);
            mMiniScale = (float) rect.w / Math.min(bitmap.getWidth(), bitmap.getHeight());
            mMode = MODE_ZOOM;
        }
    }

    private void actionMove(MotionEvent event) {
        if (mMode == MODE_DRAG) {
            mMatrix.set(mSavedMatrix);
            float transX = event.getX() - mStartPoint.x;
            float transY = event.getY() - mStartPoint.y;
            mMatrix.getValues(mMatrixValues);
            float leftLimit = rect.x - mMatrixValues[2];
            float topLimit = rect.y - mMatrixValues[5];
            float rightLimit = (rect.x + rect.w) - (bitmap.getWidth() * mMatrixValues[0] + mMatrixValues[2]);
            float bottomLimit = (rect.y + rect.h) - (bitmap.getHeight() * mMatrixValues[0] + mMatrixValues[5]);
            if (transX > 0 && transX > leftLimit) {
                transX = leftLimit;
            }
            if (transY > 0 && transY > topLimit) {
                transY = topLimit;
            }
            if (transX < 0 && transX < rightLimit) {
                transX = rightLimit;
            }
            if (transY < 0 && transY < bottomLimit) {
                transY = bottomLimit;
            }
            mMatrix.postTranslate(transX, transY);
        } else if (mMode == MODE_ZOOM) {
            float newDist = spacing(event);
            if (newDist > 10f) {
                mMatrix.set(mSavedMatrix);
                mMatrix.getValues(mMatrixValues);
                float scale = newDist / mOldDist;
                if (mMatrixValues[0] * scale < mMiniScale) {
                    scale = mMiniScale / mMatrixValues[0];
                }
                mMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);
            }
        }
    }

    private void checkLocation() {
        float bRight = mMatrixValues[2] + bitmap.getWidth() * mMatrixValues[0];
        float bBottom = mMatrixValues[5] + bitmap.getHeight() * mMatrixValues[4];
        if (rect.x < mMatrixValues[2]) {
            moveX = rect.x - mMatrixValues[2];
        } else if ((rect.x + rect.w) > bRight) {
            moveX = (rect.x + rect.w) - bRight;
        }
        if (rect.y < mMatrixValues[5]) {
            moveY = rect.y - mMatrixValues[5];
        } else if ((rect.y + rect.h) > bBottom) {
            moveY = (rect.y + rect.h) - bBottom;
        }
        if (moveX != 0 || moveY != 0) {
            handler.sendEmptyMessage(0);
        }
    }
}