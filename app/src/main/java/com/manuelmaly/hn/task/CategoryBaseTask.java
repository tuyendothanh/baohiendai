package com.manuelmaly.hn.task;

import android.util.Log;

import com.manuelmaly.hn.App;
import com.manuelmaly.hn.model.CategoryListModel;
import com.manuelmaly.hn.reuse.CancelableRunnable;
import com.manuelmaly.hn.server.CategoryDownloadCommand;
import com.manuelmaly.hn.server.HNCredentials;
import com.manuelmaly.hn.server.IAPICommand;
import com.manuelmaly.hn.server.IAPICommand.RequestType;
import com.manuelmaly.hn.util.Const;
import com.manuelmaly.hn.util.ExceptionUtil;
import com.manuelmaly.hn.util.FileUtil;
import com.manuelmaly.hn.util.Run;

import java.util.HashMap;
import java.util.List;

public abstract class CategoryBaseTask extends BaseTask<CategoryListModel> {

    public CategoryBaseTask(String notificationBroadcastIntentID, int taskCode) {
        super(notificationBroadcastIntentID, taskCode);
    }

    @Override
    public CancelableRunnable getTask() {
        return new HNFeedTaskRunnable();
    }
    
    protected abstract String getFeedURL();

    protected abstract int getCurrentPage();

    class HNFeedTaskRunnable extends CancelableRunnable {

        CategoryDownloadCommand mFeedDownload;

        @Override
        public void run() {
            mFeedDownload = new CategoryDownloadCommand(getFeedURL(), getCurrentPage(), new HashMap<String, String>(), RequestType.GET, false, null,
                App.getInstance(), HNCredentials.getCookieStore(App.getInstance()));

            mFeedDownload.run();

            if (mCancelled)
                mErrorCode = IAPICommand.ERROR_CANCELLED_BY_USER;
            else
                mErrorCode = mFeedDownload.getErrorCode();

            if (!mCancelled && mErrorCode == IAPICommand.ERROR_NONE) {
                //HNFeedParser feedParser = new HNFeedParser();
                try {
                    List<CategoryListModel.CategoryData> categoryDatas = mFeedDownload.getListResponseContent();
                    mResult = new CategoryListModel();
                    mResult.setData(categoryDatas);
//                    mResult = feedParser.parseHNPost(mFeedDownload.getListResponseContent());
//                    mResult.setNextPageURL(getFeedURL());
//                    mResult.setNextPage(getCurrentPage()+mResult.getPosts().size());
                    Run.inBackground(new Runnable() {
                        public void run() {
                            FileUtil.setLastCatelory(mResult);
                        }
                    });
                } catch (Exception e) {
                    mResult = null;
                    ExceptionUtil.sendToGoogleAnalytics(e, Const.GAN_ACTION_PARSING);
                    Log.e("HNFeedTask", "HNFeed Parser Error :(", e);
                }
            }

            if (mResult == null)
                mResult = new CategoryListModel();
        }

        @Override
        public void onCancelled() {
            mFeedDownload.cancel();
        }

    }

}
