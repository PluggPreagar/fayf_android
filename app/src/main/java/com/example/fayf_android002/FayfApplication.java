package com.example.fayf_android002;

import android.app.Application;

public class FayfApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set the custom crash handler
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}