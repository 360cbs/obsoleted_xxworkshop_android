/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 11:46 AM.
 */

package com.xxworkshop.common;

import android.util.Log;

public final class XXLog {
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;

    public static String Tag = "xxworkshop";
    public static int LogLevel = DEBUG;

    public static void log(String message) {
        switch (LogLevel) {
            case VERBOSE:
                Log.v(Tag, message);
                break;
            case DEBUG:
                Log.d(Tag, message);
                break;
            case INFO:
                Log.i(Tag, message);
                break;
            case WARN:
                Log.w(Tag, message);
                break;
            case ERROR:
                Log.e(Tag, message);
                break;
            default:
                Log.d(Tag, message);
                break;
        }
    }

    public static void log(int message) {
        log(String.valueOf(message));
    }

    public static void log(float message) {
        log(String.valueOf(message));
    }

    public static void log(Object message) {
        log(message.toString());
    }
}
