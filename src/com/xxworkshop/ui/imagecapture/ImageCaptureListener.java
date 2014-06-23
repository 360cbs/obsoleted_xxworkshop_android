/*
 * Copyright (c) 2012, Converger Co.,ltd..
 * All rights reserved.
 */

package com.xxworkshop.ui.imagecapture;

import android.graphics.Bitmap;

/**
 * Created by brochexu on 6/16/14.
 */
public interface ImageCaptureListener {
    public void onCapture(Bitmap bitmap);

    public void onCancel();
}
