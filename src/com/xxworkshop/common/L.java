/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 11:46 AM.
 */

package com.xxworkshop.common;

import android.util.Log;

public final class L {
    public final static int VERBOSE = Log.VERBOSE;
    public final static int DEBUG = Log.DEBUG;
    public final static int INFO = Log.INFO;
    public final static int WARN = Log.WARN;
    public final static int ERROR = Log.ERROR;
    public final static int ASSERT = Log.ASSERT;

    public final static String Tag = "xxworkshop";
    public static int LogLevel = DEBUG;

    public final static void log(String message) {
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

    public final static void log(int message) {
        log(String.valueOf(message));
    }

    public final static void log(float message) {
        log(String.valueOf(message));
    }

    public final static void log(Object message) {
        log(message.toString());
    }

    public final static void logSplitter() {
        log("====================");
    }
}
