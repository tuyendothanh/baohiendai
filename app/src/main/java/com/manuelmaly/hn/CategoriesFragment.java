package com.manuelmaly.hn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.manuelmaly.hn.R;
import com.manuelmaly.hn.adapter.CategoryAdapter;
import com.manuelmaly.hn.model.CategoryListModel;
import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;
import com.manuelmaly.hn.server.HNCredentials;
import com.manuelmaly.hn.task.HNFeedTaskMainFeed;
import com.manuelmaly.hn.task.ITaskFinishedHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by TPS on 6/23/2017.
 */
@EFragment
public class CategoriesFragment extends Fragment {
    public static final String IT_CATEGORY_ID = "CATEGORY_ID";
    public static final String IT_CATEGORY_TITLE = "CATEGORY_TITLE";

    @ViewById(R.id.view_main)
    RecyclerView rvSectionList;
    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    List<CategoryListModel.CategoryData> mList;
    CategoryAdapter mAdapter;

    Activity mActivity;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_list_fragment, null);
    }
    @AfterViews
    public void init() {
        mActivity = getActivity();
        mContext = getActivity();
        mList = new ArrayList<>();
        mAdapter = new CategoryAdapter(mContext,mList);
        rvSectionList.setLayoutManager(new GridLayoutManager(mContext, 2));
        rvSectionList.setAdapter(mAdapter);

        toggleSwipeRefreshLayout();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startFeedLoading();
            }
        });

        loadAlreadyReadCache();
        loadIntermediateFeedFromStore();
        startFeedLoading();
    }


    @Override
    public void onResume() {
        super.onResume();

        boolean registeredUserChanged = mFeed.getUserAcquiredFor() != null
                && (!mFeed.getUserAcquiredFor().equals(
                Settings.getUserName(getActivity())));

        // We want to reload the feed if a new user logged in
        if (HNCredentials.isInvalidated() || registeredUserChanged) {
            showFeed(new HNFeed(new ArrayList<HNPost>(), null, ""));
            startFeedLoading();
        }

        // refresh if font size changed
        if (refreshFontSizes()) {
            mPostsListAdapter.notifyDataSetChanged();
        }

        // restore vertical scrolling position if applicable
        if (mListState != null) {
            mPostsList.onRestoreInstanceState(mListState);
        }
        mListState = null;

        // User may have toggled pull-down refresh, so toggle the SwipeRefreshLayout.
        toggleSwipeRefreshLayout();
    }

    private void toggleSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(Settings.isPullDownRefresh(getActivity()));
    }

    @Override
    public void onTaskFinished(int taskCode, ITaskFinishedHandler.TaskResultCode code,
                               HNFeed result, Object tag) {
        if (taskCode == TASKCODE_LOAD_FEED) {
            if (code.equals(ITaskFinishedHandler.TaskResultCode.Success)
                    && mPostsListAdapter != null) {
                showFeed(result);
            } else
            if (!code.equals(ITaskFinishedHandler.TaskResultCode.Success)) {
                Toast.makeText(getActivity(),
                        getString(R.string.error_unable_to_retrieve_feed),
                        Toast.LENGTH_SHORT).show();
            }
        } else
        if (taskCode == TASKCODE_LOAD_MORE_POSTS) {
            if (!code.equals(ITaskFinishedHandler.TaskResultCode.Success) || result == null || result.getPosts() == null || result.getPosts().size() == 0) {
                Toast.makeText(getActivity(),
                        getString(R.string.error_unable_to_load_more),
                        Toast.LENGTH_SHORT).show();
                mFeed.setLoadedMore(true); // reached the end.
            }

            mFeed.appendLoadMoreFeed(result);
            mPostsListAdapter.notifyDataSetChanged();
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

    private void startFeedLoading() {
        setShowRefreshing(true);
        HNFeedTaskMainFeed.startOrReattach(this, this, TASKCODE_LOAD_FEED);
    }

    public void showContent(CategoryListModel categoryListModel) {
        if(swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
        mList.clear();
        mList.addAll(categoryListModel.getData());
        mAdapter.notifyDataSetChanged();
    }
}