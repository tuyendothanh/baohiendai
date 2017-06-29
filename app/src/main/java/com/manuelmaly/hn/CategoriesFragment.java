package com.manuelmaly.hn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.manuelmaly.hn.adapter.CategoryAdapter;
import com.manuelmaly.hn.model.CategoryListModel;
import com.manuelmaly.hn.server.HNCredentials;
import com.manuelmaly.hn.task.CategoryMainTask;
import com.manuelmaly.hn.task.ITaskFinishedHandler;
import com.manuelmaly.hn.util.FileUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by TPS on 6/23/2017.
 */
@EFragment(R.layout.categories_fragment)
public class CategoriesFragment extends BaseListFragment implements
        ITaskFinishedHandler<CategoryListModel> {
    public static final String IT_CATEGORY_ID = "CATEGORY_ID";
    public static final String IT_CATEGORY_TITLE = "CATEGORY_TITLE";

    @ViewById(R.id.view_main)
    RecyclerView rvSectionList;
    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

   // List<CategoryListModel.CategoryData> mList;
    CategoryListModel mCategory;
    CategoryAdapter mAdapter;

    Activity mActivity;
    Context mContext;

    private static final int TASKCODE_LOAD_FEED = 200;

    private static final String LIST_STATE = "categoryState";
    private static final String ALREADY_READ_ARTICLES_KEY = "CATEGORY_ALREADY_READ";
    private Parcelable mListState = null;

    boolean mShouldShowRefreshing = false;

    Set<Integer> mAlreadyRead;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.categories_fragment, null);
    }
    @AfterViews
    public void init() {
        mActivity = getActivity();
        mContext = getActivity();
        mCategory = new CategoryListModel();
        mCategory.setData(new ArrayList<CategoryListModel.CategoryData>());
        mAdapter = new CategoryAdapter(mContext,mCategory);
        rvSectionList.setLayoutManager(new GridLayoutManager(mContext, 2));
        rvSectionList.setAdapter(mAdapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startFeedLoading();
            }
        });

        //showContent(createDmy());
        loadAlreadyReadCache();
        loadIntermediateFeedFromStore();
        startFeedLoading();
    }

//    public void showContent(CategoryListModel categoryListModel) {
//        if(swipeRefresh.isRefreshing()) {
//            swipeRefresh.setRefreshing(false);
//        }
//        mList.clear();
//        mList.addAll(categoryListModel.getData());
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private CategoryListModel createDmy() {
//        CategoryListModel categoryListModel = new CategoryListModel();
//
//        ArrayList<CategoryListModel.CategoryData> data = new ArrayList<CategoryListModel.CategoryData>();
//        CategoryListModel.CategoryData categoryData = new CategoryListModel.CategoryData("description", "id", "name", "http://img.f31.vnecdn.net/2017/06/26/TELEMMGLPICT0001330151721largetransNvBQzQNjv4BqwioWl5aH7fAEJ8IWJw2Y5PHkRvugymKLtqq96rVP8-1498434042_140x84.jpeg");
//        data.add(categoryData);
//        CategoryListModel.CategoryData categoryData1 = new CategoryListModel.CategoryData("description", "id", "name", "http://img.f32.vnecdn.net/2017/06/23/Hyundai-Sonata-2014-13-3791-1498210628_140x84.jpg");
//        data.add(categoryData1);
//        categoryListModel.setData(data);
//
//        return categoryListModel;
//    }

    @Override
    public void onResume() {
        super.onResume();

        // We want to reload the feed if a new user logged in
        if (HNCredentials.isInvalidated()) {
            showFeed(new CategoryListModel());
            startFeedLoading();
        }

//        // refresh if font size changed
//        if (refreshFontSizes()) {
//            mAdapter.notifyDataSetChanged();
//        }

        // restore vertical scrolling position if applicable
        if (mListState != null) {
            rvSectionList.getLayoutManager().onRestoreInstanceState(mListState);
        }
        mListState = null;

        // User may have toggled pull-down refresh, so toggle the SwipeRefreshLayout.
        toggleSwipeRefreshLayout();
    }

    private void toggleSwipeRefreshLayout() {
        swipeRefresh.setEnabled(Settings.isPullDownRefresh(getActivity()));
    }

    @Override
    public void onTaskFinished(int taskCode, ITaskFinishedHandler.TaskResultCode code,
                               CategoryListModel result, Object tag) {
        if (taskCode == TASKCODE_LOAD_FEED) {
            if (code.equals(ITaskFinishedHandler.TaskResultCode.Success)
                    && mAdapter != null) {
                showFeed(result);
            } else
            if (!code.equals(ITaskFinishedHandler.TaskResultCode.Success)) {
                Toast.makeText(getActivity(),
                        getString(R.string.error_unable_to_retrieve_feed),
                        Toast.LENGTH_SHORT).show();
            }
        }

        setShowRefreshing(false);
    }

    @Background
    void loadAlreadyReadCache() {
        if (mAlreadyRead == null) {
            mAlreadyRead = new HashSet<Integer>();
        }

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                ALREADY_READ_ARTICLES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Map<String, ?> read = sharedPref.getAll();
        Long now = new Date().getTime();

        for (Map.Entry<String, ?> entry : read.entrySet()) {
            Long readAt = (Long) entry.getValue();
            Long diff = (now - readAt) / (24 * 60 * 60 * 1000);
            if (diff >= 2) {
                editor.remove(entry.getKey());
            } else {
                mAlreadyRead.add(entry.getKey().hashCode());
            }
        }
        editor.commit();
    }

    private void showFeed(CategoryListModel feed) {
        mCategory.setData(feed.getData());
        mAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new CategoriesFragment.GetLastCategoryTask().execute((Void) null);
        long start = System.currentTimeMillis();

        Log.i("",
                "Loading intermediate feed took ms:"
                        + (System.currentTimeMillis() - start));
    }

    class GetLastCategoryTask extends FileUtil.GetLastCategoryTask {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(getActivity());
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected void onPostExecute(CategoryListModel result) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            if (result != null) {
                showFeed(result);
            }
        }
    }

    private void startFeedLoading() {
        setShowRefreshing(true);
        CategoryMainTask.startOrReattach(this, this, TASKCODE_LOAD_FEED, 0);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        if (state != null) {
            mListState = state.getParcelable(LIST_STATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = rvSectionList.getLayoutManager().onSaveInstanceState();
        if (mListState != null) {
            state.putParcelable(LIST_STATE, mListState);
        }
    }

    private void setShowRefreshing(boolean showRefreshing) {
        if (!Settings.isPullDownRefresh(getActivity())) {
            mShouldShowRefreshing = showRefreshing;
            getActivity().supportInvalidateOptionsMenu();
        }

        if (swipeRefresh.isEnabled() && (!swipeRefresh.isRefreshing() || !showRefreshing)) {
            swipeRefresh.setRefreshing(showRefreshing);
        }
    }
}