package com.xxworkshop.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.xxworkshop.common.F;
import com.xxworkshop.common.Graphics;
import com.xxworkshop.common.L;
import com.xxworkshop.common.formatter.Anchor;
import com.xxworkshop.common.formatter.Rect;
import com.xxworkshop.ui.imagecapture.FocusView;


public class ImageCaptureHelper {
    /**
     * 拖动模式
     */
    private static final int MODE_TRANS = 1;
    /**
     * 缩放模式
     */
    private static final int MODE_SCALE = 2;
    /**
     * 没有模式
     */
    private static final int MODE_NONE = 3;

    private int mMode = MODE_NONE;

    private ImageView imageView = null;
    private FocusView focusView = null;

    private Rect limitRect;
    private int ImageWidth;
    private int ImageHeight;
    private Bitmap bitmap = null;

    /**
     * MSCALE_X	    MSKEW_X		MTRANS_X
     * MSKEW_Y		MSCALE_Y	MTRANS_Y
     * MPERSP_0	    MPERSP_1	MPERSP_2
     */
    private Matrix startMatrix = new Matrix();
    private PointF transStartPoint = new PointF();
    private float scaleStartDistance = 0;
    private PointF scaleCenterPoint = new PointF();

    private ResetHandler resetHandler = new ResetHandler();

    public ImageCaptureHelper(Context context, final FrameLayout rootView) {
        imageView = new ImageView(context);

        rootView.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        focusView = new FocusView(context);
        rootView.addView(focusView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        imageView.setOnTouchListener(new OnTouchListener());

        ViewTreeObserver vto = rootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = rootView.getHeight();
                int width = rootView.getWidth();
                int size = width < height ? width : height;
                Rect rect = new Rect(width / 2, height / 2, size, size);
                focusView.init(rect, Anchor.Center);
                ImageCaptureHelper.this.limitRect = F.convertRect(rect, Anchor.Center);
            }
        });
    }

    public ImageCaptureHelper setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        imageView.setScaleType(ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public ImageCaptureHelper setImageSize(int width, int height) {
        ImageWidth = width;
        ImageHeight = height;
        return this;
    }

    /**
     * 保存剪切的
     *
     * @return 剪切的图像
     */
    public Bitmap capture() {
        if (bitmap != null) {
            checkBounds();
            float[] mvalues = new float[9];
            imageView.getImageMatrix().getValues(mvalues);
            int left = (int) ((limitRect.x - mvalues[2]) / mvalues[0]);
            int top = (int) ((limitRect.y - mvalues[5]) / mvalues[4]);
            int width = (int) (limitRect.w / mvalues[0]);
            int height = (int) (limitRect.h / mvalues[4]);
            float scale = ((float) ImageWidth) / ((float) width);
            Bitmap mBitmap = Graphics.cutZoomImage(bitmap, new Rect(left, top, width, height), scale);
//            Bitmap zBitmap = Graphics.zoomImage(mBitmap, ImageWidth, ImageHeight);
            if (mBitmap != null) {
                return mBitmap;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 边界检查，防止截图区域中出现空白区域
     */
    private void checkBounds() {
        float[] mvalues = new float[9];
        imageView.getImageMatrix().getValues(mvalues);
        float ox = mvalues[2];
        float oy = mvalues[5];
        float ow = (bitmap.getWidth() * mvalues[0]);
        float oh = (bitmap.getHeight() * mvalues[4]);

        float destX, destY, dsw, dsh, destScale;
        boolean flagx = false;
        boolean flagy = false;
        if (ow < limitRect.w) {
            dsw = (float) limitRect.w / bitmap.getWidth();
            flagx = true;
        } else {
            dsw = mvalues[0];
        }
        if (oh < limitRect.h) {
            flagy = true;
            dsh = (float) limitRect.h / bitmap.getHeight();
        } else {
            dsh = mvalues[4];
        }
        if (dsw > dsh) {
            destScale = dsw;
        } else {
            destScale = dsh;
        }
        if (flagx || flagy) {
            if (flagx) {
                destX = limitRect.x;
            } else {
                if (ox > limitRect.x) {
                    destX = limitRect.x;
                } else if (ox + ow < limitRect.x + limitRect.w) {
                    destX = limitRect.x + limitRect.w - ow;
                } else {
                    destX = ox;
                }
            }
            if (flagy) {
                destY = limitRect.y;
            } else {
                if (oy > limitRect.y) {
                    destY = limitRect.y;
                } else if (oy + oh < limitRect.y + limitRect.h) {
                    destY = limitRect.y + limitRect.h - oh;
                } else {
                    destY = oy;
                }
            }
        } else {
            if (ox > limitRect.x) {
                destX = limitRect.x;
            } else if (ox + ow < limitRect.x + limitRect.w) {
                destX = limitRect.x + limitRect.w - ow;
            } else {
                destX = ox;
            }
            if (oy > limitRect.y) {
                destY = limitRect.y;
            } else if (oy + oh < limitRect.y + limitRect.h) {
                destY = limitRect.y + limitRect.h - oh;
            } else {
                destY = oy;
            }
        }
        mvalues[0] = destScale;
        mvalues[2] = destX;
        mvalues[4] = destScale;
        mvalues[5] = destY;
        Matrix cmatrix = new Matrix();
        cmatrix.setValues(mvalues);
        imageView.setImageMatrix(cmatrix);
    }

    private void actionDown(MotionEvent event) {
        resetHandler.cancel();
        imageView.setScaleType(ScaleType.MATRIX);
        startMatrix.set(imageView.getImageMatrix());
        transStartPoint.set(event.getX(), event.getY());
        mMode = MODE_TRANS;
    }

    private void actionPointerDown(MotionEvent event) {
        resetHandler.cancel();
        imageView.setScaleType(ScaleType.MATRIX);
        float opx = event.getX(0) - event.getX(1);
        float opy = event.getY(0) - event.getY(1);
        scaleStartDistance = (float) Math.sqrt(opx * opx + opy * opy);

        if (scaleStartDistance > 10f) {
            startMatrix.set(imageView.getImageMatrix());
            scaleCenterPoint.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
            mMode = MODE_SCALE;
        }
    }

    private void actionMove(MotionEvent event) {
        if (mMode == MODE_TRANS) {
            Matrix cmatrix = new Matrix();
            cmatrix.set(startMatrix);
            float offsetx = event.getX() - transStartPoint.x;
            float offsety = event.getY() - transStartPoint.y;
            cmatrix.postTranslate(offsetx, offsety);
            imageView.setImageMatrix(cmatrix);
        } else if (mMode == MODE_SCALE) {
            float opx = event.getX(0) - event.getX(1);
            float opy = event.getY(0) - event.getY(1);
            float curDistance = (float) Math.sqrt(opx * opx + opy * opy);
            if (curDistance > 10f) {
                Matrix cmatrix = new Matrix();
                cmatrix.set(startMatrix);
                float scale = curDistance / scaleStartDistance;
                cmatrix.postScale(scale, scale, scaleCenterPoint.x, scaleCenterPoint.y);
                imageView.setImageMatrix(cmatrix);
            }
        }
    }

    private class OnTouchListener implements View.OnTouchListener {
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
                    resetHandler.reset();
                    mMode = MODE_NONE;
                    break;
            }
            return true;
        }
    }

    public class ResetHandler extends Handler {
        private float destX = 0;
        private float destY = 0;
        private float destScale = 0;
        private int index = 0;

        public void reset() {
            this.removeMessages(0);

            float[] mvalues = new float[9];
            imageView.getImageMatrix().getValues(mvalues);
            float ox = mvalues[2];
            float oy = mvalues[5];
            float ow = (bitmap.getWidth() * mvalues[0]);
            float oh = (bitmap.getHeight() * mvalues[4]);

            float dsw, dsh;
            boolean flagx = false;
            boolean flagy = false;
            if (ow < limitRect.w) {
                dsw = (float) limitRect.w / bitmap.getWidth();
                flagx = true;
            } else {
                dsw = mvalues[0];
            }
            if (oh < limitRect.h) {
                flagy = true;
                dsh = (float) limitRect.h / bitmap.getHeight();
            } else {
                dsh = mvalues[4];
            }
            if (dsw > dsh) {
                destScale = dsw;
            } else {
                destScale = dsh;
            }
            if (flagx || flagy) {
                if (flagx) {
                    destX = limitRect.x;
                } else {
                    if (ox > limitRect.x) {
                        destX = limitRect.x;
                    } else if (ox + ow < limitRect.x + limitRect.w) {
                        destX = limitRect.x + limitRect.w - ow;
                    } else {
                        destX = ox;
                    }
                }
                if (flagy) {
                    destY = limitRect.y;
                } else {
                    if (oy > limitRect.y) {
                        destY = limitRect.y;
                    } else if (oy + oh < limitRect.y + limitRect.h) {
                        destY = limitRect.y + limitRect.h - oh;
                    } else {
                        destY = oy;
                    }
                }
            } else {
                if (ox > limitRect.x) {
                    destX = limitRect.x;
                } else if (ox + ow < limitRect.x + limitRect.w) {
                    destX = limitRect.x + limitRect.w - ow;
                } else {
                    destX = ox;
                }
                if (oy > limitRect.y) {
                    destY = limitRect.y;
                } else if (oy + oh < limitRect.y + limitRect.h) {
                    destY = limitRect.y + limitRect.h - oh;
                } else {
                    destY = oy;
                }
            }

            startMatrix.set(imageView.getImageMatrix());
            index = 0;
            sendEmptyMessage(0);
        }

        public void cancel() {
            this.removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            if (index <= 8) {
                float[] mvalues = new float[9];
                imageView.getImageMatrix().getValues(mvalues);
                float cx = mvalues[2];
                float cy = mvalues[5];
                float cs = mvalues[0];

                float offx = (destX - cx) / 2;
                float offy = (destY - cy) / 2;
                float offs = (destScale - cs) / 2;
                mvalues[0] += offs;
                mvalues[2] += offx;
                mvalues[4] += offs;
                mvalues[5] += offy;
                Matrix cmatrix = new Matrix();
                cmatrix.setValues(mvalues);
                imageView.setImageMatrix(cmatrix);
                sendEmptyMessageDelayed(0, 50);
            } else if (index == 9) {
                float[] mvalues = new float[9];
                imageView.getImageMatrix().getValues(mvalues);
                mvalues[0] = destScale;
                mvalues[2] = destX;
                mvalues[4] = destScale;
                mvalues[5] = destY;
                Matrix cmatrix = new Matrix();
                cmatrix.setValues(mvalues);
                imageView.setImageMatrix(cmatrix);
            }
            index++;
        }
    }

    private void printMatrix(Matrix matrix) {
        float[] mMatrixValues = new float[9];
        matrix.getValues(mMatrixValues);
        L.log("MSCALE_X = " + mMatrixValues[0] + "; MSKEW_X = " + mMatrixValues[1] + "; MTRANS_X = " + mMatrixValues[2]
                + "; \nMSCALE_Y = " + mMatrixValues[4] + "; MSKEW_Y = " + mMatrixValues[3] + "; MTRANS_Y = " + mMatrixValues[5]
                + "; \nMPERSP_0 = " + mMatrixValues[6] + "; MPERSP_1 = " + mMatrixValues[7] + "; MPERSP_2 = " + mMatrixValues[8]);
    }
}