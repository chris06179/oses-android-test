package de.stm.oses.application;

import android.app.Application;

public class OsesApplication extends Application {

    private Logger mLogger;
    private static volatile OsesApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static OsesApplication getInstance() {
        return sInstance;
    }

    public Logger getLogger() {
        if (mLogger == null) {
            mLogger = new Logger(getApplicationContext());
        }
        return mLogger;
    }
}
