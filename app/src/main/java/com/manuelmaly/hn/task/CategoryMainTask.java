package com.manuelmaly.hn.task;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.manuelmaly.hn.App;
import com.manuelmaly.hn.model.CategoryListModel;

public class CategoryMainTask extends CategoryBaseTask {

    private static CategoryMainTask instance;
    public static final String BROADCAST_INTENT_ID = "CategoryMain";
    private static int mCurentPage;

    private static CategoryMainTask getInstance(int taskCode) {
        synchronized (CategoryBaseTask.class) {
            if (instance == null)
                instance = new CategoryMainTask(taskCode);
        }
        return instance;
    }

    private CategoryMainTask(int taskCode) {
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

    public static void startOrReattach(Activity activity, ITaskFinishedHandler<CategoryListModel> finishedHandler, int taskCode) {
        CategoryMainTask task = getInstance(taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, CategoryListModel.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void startOrReattach(Fragment activity, ITaskFinishedHandler<CategoryListModel> finishedHandler, int taskCode, int currentPage) {
        CategoryMainTask task = getInstance(taskCode);
        mCurentPage = currentPage;
        task.setOnFinishedHandler(activity, finishedHandler, CategoryListModel.class);
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
