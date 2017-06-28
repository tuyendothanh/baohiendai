package com.manuelmaly.hn;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import com.manuelmaly.hn.task.HNFeedTaskLoadMore;
import com.manuelmaly.hn.task.HNFeedTaskMainFeed;
import com.manuelmaly.hn.task.HNVoteTask;
import com.manuelmaly.hn.task.ITaskFinishedHandler;
import com.manuelmaly.hn.util.FileUtil;
import com.manuelmaly.hn.util.FontHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EFragment(R.layout.main_list_fragment)
public class MainListFragment extends BaseListFragment implements
        ITaskFinishedHandler<HNFeed>{

    @ViewById(R.id.main_list)
    RecyclerView mPostsList;

    //@ViewById(R.id.main_root)
    //LinearLayout mRootView;
    LinearLayoutManager mLayoutManager;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int visibleThreshold = 5;
    private int previousTotal = 0;

    @ViewById(R.id.main_swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SystemService
    LayoutInflater mInflater;

    TextView mEmptyListPlaceholder;
    HNFeed mFeed;
    ArticleAdapter mPostsListAdapter;
    Set<HNPost> mUpvotedPosts;
    Set<Integer> mAlreadyRead;

    String mCurrentFontSize = null;
    int mFontSizeTitle;
    int mFontSizeDetails;
    int mTitleColor;
    int mTitleReadColor;
    //int mCurrentPage;

    private static final int TASKCODE_LOAD_FEED = 10;
    private static final int TASKCODE_LOAD_MORE_POSTS = 20;
    private static final int TASKCODE_VOTE = 100;

    private static final String LIST_STATE = "listState";
    private static final String ALREADY_READ_ARTICLES_KEY = "HN_ALREADY_READ";
    private Parcelable mListState = null;

    boolean mShouldShowRefreshing = false;

    ActionBar mActionbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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
        //inflater.inflate(R.layout.main_list_fragment, null, false);
        return inflater.inflate(R.layout.main_list_fragment, null);
    }

    @AfterViews
    public void init() {
        //mCurrentPage = 0;
        mFeed = new HNFeed(new ArrayList<HNPost>(), null, "");
        //mPostsListAdapter = new PostsAdapter();
        mUpvotedPosts = new HashSet<HNPost>();

        mLayoutManager = new GridLayoutManager(getActivity(),1);
        //mLayoutManager = new LinearLayoutManager(getActivity());
        //mEmptyListPlaceholder = getEmptyTextView(mRootView);
        //mPostsList.setEmptyView(mEmptyListPlaceholder);
        //mPostsList.setAdapter(mPostsListAdapter);
        mPostsListAdapter = new ArticleAdapter(getActivity().getApplicationContext(), mFeed);
        mPostsListAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position,View shareView) {

                mPostsListAdapter.setReadState(position,true);
                if (Settings.getHtmlViewer(getActivity()).equals(
                        getString(R.string.pref_htmlviewer_browser))) {
                    openURLInBrowser(
                            getArticleViewURL(mFeed.getPosts().get(position)),
                            getActivity());
                } else {
                    openPostInApp(mFeed.getPosts().get(position), null,
                            getActivity());
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
                        HNFeedTaskLoadMore.start(getActivity(),
                                MainListFragment.this, mFeed,
                                TASKCODE_LOAD_MORE_POSTS);
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
                mFeed.getPosts().clear();
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
            mLayoutManager.onRestoreInstanceState(mListState);
        }
        mListState = null;

        // User may have toggled pull-down refresh, so toggle the SwipeRefreshLayout.
        toggleSwipeRefreshLayout();
    }

    private void toggleSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(Settings.isPullDownRefresh(getActivity()));
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
                Toast.makeText(getActivity(),
                        getString(R.string.error_unable_to_retrieve_feed),
                        Toast.LENGTH_SHORT).show();
            }
        } else
        if (taskCode == TASKCODE_LOAD_MORE_POSTS) {
            if (!code.equals(TaskResultCode.Success) || result == null || result.getPosts() == null || result.getPosts().size() == 0) {
                Toast.makeText(getActivity(),
                        getString(R.string.error_unable_to_load_more),
                        Toast.LENGTH_SHORT).show();
                mFeed.setLoadedMore(true); // reached the end.
            }

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

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
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
        Editor editor = getActivity().getSharedPreferences(ALREADY_READ_ARTICLES_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putLong(title, now);
        editor.commit();

        mAlreadyRead.add(title.hashCode());
    }

    private void showFeed(HNFeed feed) {
        mFeed.getPosts().clear();
        //mFeed.addPosts(feed.getPosts());
        mFeed.setHNFeed(feed.getPosts(),
                feed.getNextPageURL(),
                feed.getNextPage(),
                feed.getUserAcquiredFor(),
                feed.isLoadedMore());
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
            progress = new ProgressDialog(getActivity());
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
        HNFeedTaskMainFeed.startOrReattach(this, this, TASKCODE_LOAD_FEED, mFeed.getPosts().size());
    }

    private boolean refreshFontSizes() {
        final String fontSize = Settings.getFontSize(getActivity());
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

    private void vote(String voteURL, HNPost post) {
        HNVoteTask.start(voteURL, getActivity(),
                new VoteTaskFinishedHandler(), TASKCODE_VOTE, post);
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
        mListState = mLayoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE, mListState);
    }

    class VoteTaskFinishedHandler implements ITaskFinishedHandler<Boolean> {
        @Override
        public void onTaskFinished(
                int taskCode,
                com.manuelmaly.hn.task.ITaskFinishedHandler.TaskResultCode code,
                Boolean result, Object tag) {
            if (taskCode == TASKCODE_VOTE) {
                if (result != null && result.booleanValue()) {
                    Toast.makeText(getActivity(), R.string.vote_success,
                            Toast.LENGTH_SHORT).show();
                    HNPost post = (HNPost) tag;
                    if (post != null) {
                        mUpvotedPosts.add(post);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.vote_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    class PostsAdapter extends BaseAdapter {

        private static final int VIEWTYPE_POST = 0;
        private static final int VIEWTYPE_LOADMORE = 1;
        private static final String HACKERNEWS_URLDOMAIN = "news.ycombinator.com";

        @Override
        public int getCount() {
            int posts = mFeed.getPosts().size();
            if (posts == 0) {
                return 0;
            } else {
                return posts + (mFeed.isLoadedMore() ? 0 : 1);
            }
        }

        @Override
        public HNPost getItem(int position) {
            if (getItemViewType(position) == VIEWTYPE_POST) {
                return mFeed.getPosts().get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            // Item ID not needed here:
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mFeed.getPosts().size()) {
                return VIEWTYPE_POST;
            } else {
                return VIEWTYPE_LOADMORE;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEWTYPE_POST:
                    if (convertView == null) {
                        convertView = mInflater.inflate(R.layout.main_list_item,
                                null);
                        PostViewHolder holder = new PostViewHolder();
                        holder.titleView = (TextView) convertView
                                .findViewById(R.id.main_list_item_title);
                        holder.urlView = (TextView) convertView
                                .findViewById(R.id.main_list_item_url);
                        holder.textContainer = (LinearLayout) convertView
                                .findViewById(R.id.main_list_item_textcontainer);
                        holder.commentsButton = (Button) convertView
                                .findViewById(R.id.main_list_item_comments_button);
                        holder.commentsButton.setTypeface(FontHelper.getComfortaa(
                                getActivity(), false));
                        holder.pointsView = (TextView) convertView
                                .findViewById(R.id.main_list_item_points);
                        holder.pointsView.setTypeface(FontHelper.getComfortaa(
                                getActivity(), true));
                        convertView.setTag(holder);
                    }

                    final HNPost item = getItem(position);
                    PostViewHolder holder = (PostViewHolder) convertView.getTag();
                    holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                            mFontSizeTitle);
                    holder.titleView.setText(item.getTitle());
                    holder.titleView.setTextColor(isRead(item) ? mTitleReadColor
                            : mTitleColor);
                    holder.urlView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                            mFontSizeDetails);
                    holder.urlView.setText(item.getURLDomain());
                    holder.pointsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                            mFontSizeDetails);
                    if (item.getPoints() != Integer.toString(BaseHTMLParser.UNDEFINED)) {
                        holder.pointsView.setText(item.getPoints() + "");
                    } else {
                        holder.pointsView.setText("-");
                    }

                    holder.commentsButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                            mFontSizeTitle);
                    if (item.getCommentsCount() != Integer.toString(BaseHTMLParser.UNDEFINED)) {
                        holder.commentsButton.setVisibility(View.VISIBLE);
                        holder.commentsButton.setText(item.getCommentsCount() + "");
                    } else {
                        holder.commentsButton.setVisibility(View.INVISIBLE);
                    }
                    holder.commentsButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startCommentActivity(position);
                        }
                    });
                    holder.textContainer.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            markAsRead(item);
                            if(getItem(position).getURLDomain().equals(HACKERNEWS_URLDOMAIN)){
                                startCommentActivity(position);
                            }
                            else  if (Settings.getHtmlViewer(getActivity()).equals(
                                    getString(R.string.pref_htmlviewer_browser))) {
                                openURLInBrowser(
                                        getArticleViewURL(getItem(position)),
                                        getActivity());
                            } else {
                                openPostInApp(getItem(position), null,
                                        getActivity());
                            }
                        }
                    });
                    holder.textContainer
                            .setOnLongClickListener(new OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    final HNPost post = getItem(position);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            getActivity());
                                    LongPressMenuListAdapter adapter = new LongPressMenuListAdapter(
                                            post);
                                    builder.setAdapter(adapter, adapter).show();
                                    return true;
                                }
                            });
                    break;

                case VIEWTYPE_LOADMORE:
                    // I don't use the preloaded convertView here because it's
                    // only one cell
                    convertView = mInflater.inflate(
                            R.layout.main_list_item_loadmore, null);
                    final TextView textView = (TextView) convertView
                            .findViewById(R.id.main_list_item_loadmore_text);
                    textView.setTypeface(FontHelper.getComfortaa(getActivity(),
                            true));
                    final ImageView imageView = (ImageView) convertView
                            .findViewById(R.id.main_list_item_loadmore_loadingimage);
                    if (HNFeedTaskLoadMore.isRunning(getActivity(),
                            TASKCODE_LOAD_MORE_POSTS)) {
                        textView.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        convertView.setClickable(false);
                    }

                    final View convertViewFinal = convertView;
                    convertView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            textView.setVisibility(View.INVISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            convertViewFinal.setClickable(false);
                            HNFeedTaskLoadMore.start(getActivity(),
                                    MainListFragment.this, mFeed,
                                    TASKCODE_LOAD_MORE_POSTS);
                            setShowRefreshing(true);
                        }
                    });
                    break;
                default:
                    break;
            }

            return convertView;
        }

        private boolean isRead(HNPost post) {
            return mAlreadyRead.contains(post.getTitle().hashCode());
        }

        private void startCommentActivity(int position){
            Intent i = new Intent(getActivity(),
                    CommentsActivity_.class);
            i.putExtra(CommentsActivity.EXTRA_HNPOST,
                    getItem(position));
            startActivity(i);
        }
    }

    private class LongPressMenuListAdapter implements ListAdapter,
            DialogInterface.OnClickListener {

        HNPost mPost;
        boolean mIsLoggedIn;
        boolean mUpVotingEnabled;
        ArrayList<CharSequence> mItems;

        public LongPressMenuListAdapter(HNPost post) {
            mPost = post;
            mIsLoggedIn = Settings.isUserLoggedIn(getActivity());
            mUpVotingEnabled = !mIsLoggedIn
                    || (mPost.getUpvoteURL(Settings
                    .getUserName(getActivity())) != null && !mUpvotedPosts
                    .contains(mPost));

            mItems = new ArrayList<CharSequence>();
            if (mUpVotingEnabled) {
                mItems.add(getString(R.string.upvote));
            } else {
                mItems.add(getString(R.string.already_upvoted));
            }
            mItems.addAll(Arrays.asList(
                    getString(R.string.pref_htmlprovider_original_url),
                    getString(R.string.pref_htmlprovider_viewtext),
                    getString(R.string.pref_htmlprovider_google),
                    getString(R.string.pref_htmlprovider_instapaper),
                    getString(R.string.external_browser),
                    getString(R.string.share_article_url)));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public CharSequence getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) mInflater.inflate(
                    android.R.layout.simple_list_item_1, null);
            view.setText(getItem(position));
            if (!mUpVotingEnabled && position == 0) {
                view.setTextColor(getResources().getColor(
                        android.R.color.darker_gray));
            }
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            if (!mUpVotingEnabled && position == 4) {
                return false;
            }
            return true;
        }

        @Override
        public void onClick(DialogInterface dialog, int item) {
            switch (item) {
                case 0:
                    if (!mIsLoggedIn) {
                        Toast.makeText(getActivity(), R.string.please_log_in,
                                Toast.LENGTH_LONG).show();
                    } else
                    if (mUpVotingEnabled) {
                        vote(mPost.getUpvoteURL(Settings
                                .getUserName(getActivity())), mPost);
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    openPostInApp(mPost, getItem(item).toString(),
                            getActivity());
                    markAsRead(mPost);
                    break;
                case 5:
                    openURLInBrowser(getArticleViewURL(mPost), getActivity());
                    markAsRead(mPost);
                    break;
                case 6:
                    shareUrl(mPost, getActivity());
                    break;
                default:
                    break;
            }
        }

    }

    private String getArticleViewURL(HNPost post) {
        return ArticleReaderActivity.getArticleViewURL(post,
                Settings.getHtmlProvider(getActivity()), getActivity());
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
        if (!Settings.isPullDownRefresh(getActivity())) {
            mShouldShowRefreshing = showRefreshing;
            getActivity().supportInvalidateOptionsMenu();
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
