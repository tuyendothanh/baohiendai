package com.manuelmaly.hn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelmaly.hn.adapter.ArticleAdapter;
import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;
import com.manuelmaly.hn.parser.BaseHTMLParser;
import com.manuelmaly.hn.server.HNCredentials;
import com.manuelmaly.hn.task.CategoryChildMainTask;
import com.manuelmaly.hn.task.CategoryChildTaskLoadMore;
import com.manuelmaly.hn.task.HNFeedTaskLoadMore;
import com.manuelmaly.hn.task.HNFeedTaskMainFeed;
import com.manuelmaly.hn.task.HNVoteTask;
import com.manuelmaly.hn.task.ITaskFinishedHandler;
import com.manuelmaly.hn.util.FileUtil;
import com.manuelmaly.hn.util.FontHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EActivity(R.layout.category_child_activity)
public class CategoryChildActivity extends BaseListActivity implements
        ITaskFinishedHandler<HNFeed> {

    public static final String EXTRA_CATID = "CATID";

    @ViewById(R.id.main_list)
    RecyclerView mPostsList;

    LinearLayoutManager mLayoutManager;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int visibleThreshold = 5;
    private int previousTotal = 0;

    @ViewById(R.id.main_swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SystemService
    LayoutInflater mInflater;

    HNFeed mFeed;
    ArticleAdapter mPostsListAdapter;
    Set<Integer> mAlreadyRead;

    String mCurrentFontSize = null;
    int mFontSizeTitle;
    int mFontSizeDetails;
    int mTitleColor;
    int mTitleReadColor;

    private static final int TASKCODE_LOAD_FEED = 310;
    private static final int TASKCODE_LOAD_MORE_POSTS = 320;
    private static final int TASKCODE_VOTE = 330;

    private static final String LIST_STATE = "CategoryChildListState";
    private static final String ALREADY_READ_ARTICLES_KEY = "CATEGORYCHILD_ALREADY_READ";
    private Parcelable mListState = null;

    boolean mShouldShowRefreshing = false;

    String mCatId;
    ActionBar mActionbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
/*
        // Make sure that we show the overflow menu icon
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // presumably, not relevant
        }

        TextView tv = (TextView) getSupportActionBar().getCustomView()
                .findViewById(R.id.actionbar_title);
        tv.setTypeface(FontHelper.getComfortaa(this, true));
*/
    }

    @AfterViews
    public void init() {
        String catid = getIntent().getStringExtra( EXTRA_CATID );
        mCatId = (catid != null) ? catid : "";
        //mCurrentPage = 0;
        mFeed = new HNFeed(new ArrayList<HNPost>(), null, "");
        //mPostsListAdapter = new PostsAdapter();

        mLayoutManager = new GridLayoutManager(this,1);
        //mLayoutManager = new LinearLayoutManager(getActivity());
        //mEmptyListPlaceholder = getEmptyTextView(mRootView);
        //mPostsList.setEmptyView(mEmptyListPlaceholder);
        //mPostsList.setAdapter(mPostsListAdapter);
        mPostsListAdapter = new ArticleAdapter(this.getApplicationContext(), mFeed);
        mPostsListAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position,View shareView) {

                mPostsListAdapter.setReadState(position,true);
                if (Settings.getHtmlViewer(CategoryChildActivity.this).equals(
                        getString(R.string.pref_htmlviewer_browser))) {
                    openURLInBrowser(
                            getArticleViewURL(mFeed.getPosts().get(position)),
                            CategoryChildActivity.this);
                } else {
                    openPostInApp(mFeed.getPosts().get(position), null,
                            CategoryChildActivity.this);
                }
            }
        });

        //mPostsList.setHasFixedSize(true);
        mPostsList.setLayoutManager(mLayoutManager);
        mPostsList.setAdapter(mPostsListAdapter);
        mPostsList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {//check for scroll down
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (pastVisiblesItems + visibleThreshold)) {
                        // End has been reached
                        CategoryChildTaskLoadMore.start(CategoryChildActivity.this,
                                CategoryChildActivity.this, mFeed,
                                TASKCODE_LOAD_MORE_POSTS, mCatId);
                        setShowRefreshing(true);

                        loading = true;
                    }
                }
            }
        });

        //mEmptyListPlaceholder.setTypeface(FontHelper.getComfortaa(getActivity(), true));

        mTitleColor = getResources().getColor(R.color.dark_gray_post_title);
        mTitleReadColor = getResources().getColor(R.color.gray_post_title_read);

        toggleSwipeRefreshLayout();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loading = true;
                previousTotal = 0;
//                mFeed.getPosts().clear();
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
                Settings.getUserName(CategoryChildActivity.this)));

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
            mPostsList.getLayoutManager().onRestoreInstanceState(mListState);
        }
        mListState = null;

        // User may have toggled pull-down refresh, so toggle the SwipeRefreshLayout.
        toggleSwipeRefreshLayout();
    }

    private void toggleSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(Settings.isPullDownRefresh(CategoryChildActivity.this));
    }

    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code,
                               HNFeed result, Object tag) {
        if (taskCode == TASKCODE_LOAD_FEED) {
            if (code.equals(TaskResultCode.Success)
                    && mPostsListAdapter != null) {
                //mCurrentPage = result.getPosts().size();
                showFeed(result);
            } else
            if (!code.equals(TaskResultCode.Success)) {
                Toast.makeText(CategoryChildActivity.this,
                        getString(R.string.error_unable_to_retrieve_feed),
                        Toast.LENGTH_SHORT).show();
            }
        } else
        if (taskCode == TASKCODE_LOAD_MORE_POSTS) {
            if (!code.equals(TaskResultCode.Success) || result == null || result.getPosts() == null || result.getPosts().size() == 0) {
                Toast.makeText(CategoryChildActivity.this,
                        getString(R.string.error_unable_to_load_more),
                        Toast.LENGTH_SHORT).show();
                mFeed.setLoadedMore(true); // reached the end.
            }
            loading = true;
            previousTotal = 0;
            mFeed.appendLoadMoreFeed(result);
            //mCurrentPage = mFeed.getPosts().size();
            mFeed.setNextPage(mFeed.getPosts().size());
            mPostsListAdapter.notifyDataSetChanged();
        }

        setShowRefreshing(false);
    }

    @Background
    void loadAlreadyReadCache() {
        if (mAlreadyRead == null) {
            mAlreadyRead = new HashSet<Integer>();
        }

        SharedPreferences sharedPref = getSharedPreferences(
                ALREADY_READ_ARTICLES_KEY, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
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

    @Background
    void markAsRead(HNPost post) {
        Long now = new Date().getTime();
        String title = post.getTitle();
        Editor editor = getSharedPreferences(ALREADY_READ_ARTICLES_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putLong(title, now);
        editor.commit();

        mAlreadyRead.add(title.hashCode());
    }

    private void showFeed(HNFeed feed) {
        loading = true;
        previousTotal = 0;
        mFeed.getPosts().clear();
        //mFeed.addPosts(feed.getPosts());
        mFeed.setHNFeed(feed.getPosts(),
                feed.getNextPageURL(),
                0, // feed.getNextPage()
                feed.getUserAcquiredFor(),
                true); // feed.isLoadedMore()
//        if (mListState != null) {
//            mPostsList.getLayoutManager().onRestoreInstanceState(mListState);
//        }
        mPostsListAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new GetLastHNFeedTask().execute((Void) null);
        long start = System.currentTimeMillis();

        Log.i("",
                "Loading intermediate feed took ms:"
                        + (System.currentTimeMillis() - start));
    }

    class GetLastHNFeedTask extends FileUtil.GetLastHNFeedTask {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(CategoryChildActivity.this);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected void onPostExecute(HNFeed result) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            if (result != null
                    && result.getUserAcquiredFor() != null
                    && result.getUserAcquiredFor().equals(
                    Settings.getUserName(App.getInstance()))) {
                showFeed(result);
            }
        }
    }

    private void startFeedLoading() {
        setShowRefreshing(true);
        CategoryChildMainTask.startOrReattach(this, this, TASKCODE_LOAD_FEED, mCatId, 0);
    }

    private boolean refreshFontSizes() {
        final String fontSize = Settings.getFontSize(this);
        if ((mCurrentFontSize == null) || (!mCurrentFontSize.equals(fontSize))) {
            mCurrentFontSize = fontSize;
            if (fontSize.equals(getString(R.string.pref_fontsize_small))) {
                mFontSizeTitle = 15;
                mFontSizeDetails = 11;
            } else
            if (fontSize.equals(getString(R.string.pref_fontsize_normal))) {
                mFontSizeTitle = 18;
                mFontSizeDetails = 12;
            } else {
                mFontSizeTitle = 22;
                mFontSizeDetails = 15;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mListState = state.getParcelable(LIST_STATE);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = mPostsList.getLayoutManager().onSaveInstanceState();
        if (mListState != null) {
            state.putParcelable(LIST_STATE, mListState);
        }
    }

    private String getArticleViewURL(HNPost post) {
        return ArticleReaderActivity.getArticleViewURL(post,
                Settings.getHtmlProvider(this), this);
    }

    public static void openURLInBrowser(String url, Activity a) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        a.startActivity(browserIntent);
    }

    public static void openPostInApp(HNPost post, String overrideHtmlProvider,
            Activity a) {
        Intent i = new Intent(a, ArticleReaderActivity_.class);
        i.putExtra(ArticleReaderActivity.EXTRA_HNPOST, post);
        if (overrideHtmlProvider != null) {
            i.putExtra(ArticleReaderActivity.EXTRA_HTMLPROVIDER_OVERRIDE,
                    overrideHtmlProvider);
        }
        a.startActivity(i);
    }

    public static void shareUrl(HNPost post, Activity a){
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
      shareIntent.putExtra(Intent.EXTRA_TEXT, post.getURL());
      a.startActivity(Intent.createChooser(shareIntent, a.getString(R.string.share_article_url)));
    }

    private void setShowRefreshing(boolean showRefreshing) {
        if (!Settings.isPullDownRefresh(CategoryChildActivity.this)) {
            mShouldShowRefreshing = showRefreshing;
            supportInvalidateOptionsMenu();
        }

        if (mSwipeRefreshLayout.isEnabled() && (!mSwipeRefreshLayout.isRefreshing() || !showRefreshing)) {
            mSwipeRefreshLayout.setRefreshing(showRefreshing);
        }
    }

    static class PostViewHolder {
        TextView titleView;
        TextView urlView;
        TextView pointsView;
        TextView commentsCountView;
        LinearLayout textContainer;
        Button commentsButton;
    }

}
