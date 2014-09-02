package com.xxworkshop.network.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by brochexu on 8/27/14.
 */
public class BitmapDecoder implements Decoder {
    @Override
    public Object decode(InputStream inputStream) {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }
}
