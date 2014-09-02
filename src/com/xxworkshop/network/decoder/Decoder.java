package com.xxworkshop.network.decoder;

import java.io.InputStream;

/**
 * Created by brochexu on 8/27/14.
 */
public interface Decoder {
    Object decode(InputStream inputStream);
}
