package com.manuelmaly.hn.task;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.manuelmaly.hn.App;
import com.manuelmaly.hn.model.HNFeed;

public class CategoryChildMainTask extends CategoryChildBaseTask {

    private static CategoryChildMainTask instance;
    public static final String BROADCAST_INTENT_ID = "CategoryChildMain";
    private static int mCurentPage;
    private static String mCatId;

    private static CategoryChildMainTask getInstance(int taskCode) {
        synchronized (HNFeedTaskBase.class) {
            if (instance == null)
                instance = new CategoryChildMainTask(taskCode);
        }
        return instance;
    }

    private CategoryChildMainTask(int taskCode) {
        super(BROADCAST_INTENT_ID, taskCode);
    }
    
    @Override
    protected String getFeedURL() {
        return App.DOMAIN_URL;//"http://192.168.2.89:3000";
        //return "http://192.168.1.11:3000";
        //return "https://news.ycombinator.com/";
    }

    @Override
    protected int getCurrentPage() {
        return mCurentPage;
    }

    @Override
    protected String getCatId() {
        return mCatId;
    }

    public static void startOrReattach(Activity activity, ITaskFinishedHandler<HNFeed> finishedHandler, int taskCode) {
        CategoryChildMainTask task = getInstance(taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, HNFeed.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void startOrReattach(Activity activity, ITaskFinishedHandler<HNFeed> finishedHandler, int taskCode,
                                       String catid, int currentPage) {
        CategoryChildMainTask task = getInstance(taskCode);
        mCurentPage = currentPage;
        mCatId = catid;
        task.setOnFinishedHandler(activity, finishedHandler, HNFeed.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void startOrReattach(Fragment activity, ITaskFinishedHandler<HNFeed> finishedHandler, int taskCode, int currentPage) {
        CategoryChildMainTask task = getInstance(taskCode);
        mCurentPage = currentPage;
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
