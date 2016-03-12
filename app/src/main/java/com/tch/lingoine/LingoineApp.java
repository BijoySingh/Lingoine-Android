package com.tch.lingoine;

import android.app.Application;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.utils.KandyLog;
import com.tch.lingoine.utils.KandyLogger;

/**
 * Created by bijoy on 3/12/16.
 */
public class LingoineApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Init Kandy SDK
        Kandy.initialize(getApplicationContext(), "DAKe58958a710904df898822f40e7e8c1fb",
            "DAS7af5656897db447796e8460626e79403");
        Kandy.getKandyLog().setLogger(new KandyLogger());
        Kandy.getKandyLog().setLogLevel(KandyLog.Level.VERBOSE);
    }
}
