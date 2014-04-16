/*
 * Copyright (c) 2012, Converger Co.,ltd..
 * All rights reserved.
 */

package com.xxworkshop.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by brochexu on 3/31/14.
 */
public final class XXGraphics {
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
}
