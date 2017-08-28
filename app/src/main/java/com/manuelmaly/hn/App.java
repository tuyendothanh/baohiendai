package com.manuelmaly.hn;

import android.app.Application;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.manuelmaly.hn.util.Const;

import org.androidannotations.annotations.EApplication;

@EApplication
public class App extends Application {

    private static App mInstance;

    public static String DOMAIN_URL = "http://123.31.12.248:3000";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        GoogleAnalyticsTracker.getInstance().startNewSession(Const.GAN_ID, 30, this);
    }

    @Override
    public void onTerminate() {
        GoogleAnalyticsTracker.getInstance().stopSession();

        super.onTerminate();
    }

    public static App getInstance() {
        return mInstance;
    }

}
