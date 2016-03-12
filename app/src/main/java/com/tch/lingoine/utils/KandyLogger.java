package com.tch.lingoine.utils;

import com.genband.kandy.api.utils.IKandyLogger;
import com.genband.kandy.api.utils.KandyLog;
import com.genband.kandy.api.utils.KandyLog.Level;


/**
 * Logger from Kandy
 * Created by bijoy on 3/12/16.
 */
public class KandyLogger implements IKandyLogger {
    @Override
    public void log(int level, String tag, String message) {
        // print logs to Android logcat
        switch (level) {
            case KandyLog.Level.ERROR:
                android.util.Log.e(tag, message);
                break;
            case Level.INFO:
                android.util.Log.i(tag, message);
                break;
            case Level.VERBOSE:
                android.util.Log.v(tag, message);
                break;
            case Level.WARN:
                android.util.Log.w(tag, message);
                break;
            default:
                android.util.Log.d(tag, message);
                break;
        }
    }

    @Override
    public void log(int level, String tag, String message, Throwable throwable) {
        // print logs to Android logcat
        switch (level) {
            case Level.ERROR:
                android.util.Log.e(tag, message, throwable);
                break;
            case Level.INFO:
                android.util.Log.i(tag, message, throwable);
                break;
            case Level.VERBOSE:
                android.util.Log.v(tag, message, throwable);
                break;
            case Level.WARN:
                android.util.Log.w(tag, message, throwable);
                break;
            default:
                android.util.Log.d(tag, message, throwable);
                break;
        }
    }

}