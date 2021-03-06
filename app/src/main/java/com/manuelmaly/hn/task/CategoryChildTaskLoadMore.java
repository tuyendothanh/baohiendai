package com.manuelmaly.hn.task;

import android.app.Activity;
import android.content.Context;

import com.manuelmaly.hn.model.HNFeed;

public class CategoryChildTaskLoadMore extends CategoryChildBaseTask {

    private HNFeed mFeedToAttachResultsTo;
    private String mCatId;

    private static CategoryChildTaskLoadMore instance;
    public static final String BROADCAST_INTENT_ID = "CategoryChildLoadMore";

    private static CategoryChildTaskLoadMore getInstance(int taskCode) {
        synchronized (CategoryChildTaskLoadMore.class) {
            if (instance == null)
                instance = new CategoryChildTaskLoadMore(taskCode);
        }
        return instance;
    }

    private CategoryChildTaskLoadMore(int taskCode) {
        super(BROADCAST_INTENT_ID, taskCode);
    }

    @Override
    protected String getFeedURL() {
        return mFeedToAttachResultsTo.getNextPageURL();
    }

    @Override
    protected int getCurrentPage() {
        return mFeedToAttachResultsTo.getNextPage();
    }

    @Override
    protected String getCatId() {
        return mCatId;
    }

    public static void start(Activity activity, ITaskFinishedHandler<HNFeed> finishedHandler,
        HNFeed feedToAttachResultsTo, int taskCode, String catid) {
        CategoryChildTaskLoadMore task = getInstance(taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, HNFeed.class);
        task.setFeedToAttachResultsTo(feedToAttachResultsTo);
        task.setCatIdTo(catid);
        if (task.isRunning())
            task.cancel();
        task.startInBackground();
    }

    public static void stopCurrent(Context applicationContext, int taskCode) {
        getInstance(taskCode).cancel();
    }

    public static boolean isRunning(Context applicationContext, int taskCode) {
        return getInstance(taskCode).isRunning();
    }

    public void setFeedToAttachResultsTo(HNFeed feedToAttachResultsTo) {
        mFeedToAttachResultsTo = feedToAttachResultsTo;
    }

    public void setCatIdTo(String catid) {
        mCatId = catid;
    }

}
