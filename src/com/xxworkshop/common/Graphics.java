/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 3/31/14 7:42 PM.
 */

package com.xxworkshop.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import com.xxworkshop.common.formatter.Rect;

public final class Graphics {
    public final static Bitmap view2Bitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        view.draw(canvas);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    public final static Bitmap modifyImageAddAtPosition(Bitmap originBitmap, Bitmap newBitmap, float left, float top) {
        Canvas canvas = new Canvas(originBitmap);
        canvas.drawBitmap(newBitmap, left, top, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return originBitmap;
    }

    public static Bitmap cutImage(Bitmap bitmap, Rect rect) {
        if (bitmap != null) {
            int bWidth = bitmap.getWidth();
            int bHeight = bitmap.getHeight();
            if (rect.x >= 0 && rect.y >= 0 && rect.x + rect.w <= bWidth && rect.y + rect.h <= bHeight) {
                try {
                    Bitmap mBitmap = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.w, rect.h);
                    return mBitmap;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Bitmap cutZoomImage(Bitmap bitmap, Rect rect, float scale) {
        if (bitmap != null) {
            int bWidth = bitmap.getWidth();
            int bHeight = bitmap.getHeight();
            if (rect.x >= 0 && rect.y >= 0 && rect.x + rect.w <= bWidth && rect.y + rect.h <= bHeight) {
                try {
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    Bitmap mBitmap = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.w, rect.h, matrix, true);
                    return mBitmap;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Bitmap zoomImage(Bitmap bitmap, float destWidth, float destHeight) {
        if (bitmap != null && destWidth > 0 && destHeight > 0) {
            int oldWidth = bitmap.getWidth();
            int oldHeight = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float widthScale = destWidth / oldWidth;
            float heightScale = destHeight / oldHeight;
            matrix.postScale(widthScale, heightScale);
            Bitmap mBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, true);
            return mBitmap;
        }
        return null;
    }
}
