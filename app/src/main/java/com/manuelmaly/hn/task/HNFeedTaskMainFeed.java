package com.manuelmaly.hn.task;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.manuelmaly.hn.model.HNFeed;

public class HNFeedTaskMainFeed extends HNFeedTaskBase {
    
    private static HNFeedTaskMainFeed instance;
    public static final String BROADCAST_INTENT_ID = "HNFeedMain";
    
    private static HNFeedTaskMainFeed getInstance(int taskCode) {
        synchronized (HNFeedTaskBase.class) {
            if (instance == null)
                instance = new HNFeedTaskMainFeed(taskCode);
        }
        return instance;
    }
    
    private HNFeedTaskMainFeed(int taskCode) {
        super(BROADCAST_INTENT_ID, taskCode);
    }
    
    @Override
    protected String getFeedURL() {
        return "http://192.168.2.89:3000";
        //return "http://192.168.1.3:3000";
        //return "https://news.ycombinator.com/";
    }
    
    public static void startOrReattach(Activity activity, ITaskFinishedHandler<HNFeed> finishedHandler, int taskCode) {
        HNFeedTaskMainFeed task = getInstance(taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, HNFeed.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void startOrReattach(Fragment activity, ITaskFinishedHandler<HNFeed> finishedHandler, int taskCode) {
        HNFeedTaskMainFeed task = getInstance(taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, HNFeed.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void stopCurrent(Context applicationContext) {
        getInstance(0).cancel();
    }

    public static boolean isRunning(Context applicationContext) {
        return getInstance(0).isRunning();
    }

}
