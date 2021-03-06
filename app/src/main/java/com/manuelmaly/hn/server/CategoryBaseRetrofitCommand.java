package com.manuelmaly.hn.server;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.manuelmaly.hn.model.CategoryListModel;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Generic base for HTTP calls via {@link HttpClient}, ideally to be started in
 * a background thread. When the call has finished, listeners are notified via
 * an intent sent to {@link LocalBroadcastManager}, i.e. they must first
 * register (intent name is configurable via notificationBroadcastIntentID).
 * Response and errors are also sent via the intent.
 * 
 * @author manuelmaly
 * 
 * @param <T>
 *            class of response
 */
public abstract class CategoryBaseRetrofitCommand<T extends Serializable> implements IAPICommand<T> {

  private final Map<String, String> mBody;

  private String mNotificationBroadcastIntentID;
    private String mUrl;
    private String mURLQueryParams;
    private RequestType mType;
    private int mActualStatusCode;
    private Context mApplicationContext;
    private int mErrorCode;
    private T mResponse;
    private List<T> mListResponse;
    private int mCurrentpage;
    private Object mTag;
    private int mSocketTimeoutMS;
    private int mHttpTimeoutMS;
    private boolean mNotifyFinishedBroadcast;
    HttpRequestBase mRequest;
    private CookieStore mCookieStore;

    public CategoryBaseRetrofitCommand(final String url, final HashMap<String, String> params, RequestType type,
                                       boolean notifyFinishedBroadcast, String notificationBroadcastIntentID, Context applicationContext,
                                       int socketTimeoutMS, int httpTimeoutMS, Map<String, String> body) {
        mUrl = url;
        mCurrentpage = 0;
        mBody = body;

        if (params != null) {
            StringBuilder sb = new StringBuilder();
            for (String param : params.keySet()) {
                if (sb.length() > 0)
                    sb.append("&");
                sb.append(Uri.encode(param)).append("=").append(Uri.encode(params.get(param)));
            }
            mURLQueryParams = sb.toString();
        }

        mType = type;
        mNotificationBroadcastIntentID = notificationBroadcastIntentID == null ? DEFAULT_BROADCAST_INTENT_ID
                : notificationBroadcastIntentID;
        mApplicationContext = applicationContext;
        mSocketTimeoutMS = socketTimeoutMS;
        mHttpTimeoutMS = httpTimeoutMS;
        mNotifyFinishedBroadcast = notifyFinishedBroadcast;
    }

    public CategoryBaseRetrofitCommand(final String url, final int currentPage, final HashMap<String, String> params, RequestType type,
                                       boolean notifyFinishedBroadcast, String notificationBroadcastIntentID, Context applicationContext,
                                       int socketTimeoutMS, int httpTimeoutMS, Map<String, String> body) {
        mUrl = url;
        mCurrentpage = currentPage;
        mBody = body;

        if (params != null) {
            StringBuilder sb = new StringBuilder();
            for (String param : params.keySet()) {
                if (sb.length() > 0)
                    sb.append("&");
                sb.append(Uri.encode(param)).append("=").append(Uri.encode(params.get(param)));
            }
            mURLQueryParams = sb.toString();
        }

        mType = type;
        mNotificationBroadcastIntentID = notificationBroadcastIntentID == null ? DEFAULT_BROADCAST_INTENT_ID
            : notificationBroadcastIntentID;
        mApplicationContext = applicationContext;
        mSocketTimeoutMS = socketTimeoutMS;
        mHttpTimeoutMS = httpTimeoutMS;
        mNotifyFinishedBroadcast = notifyFinishedBroadcast;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    @Override
    public void run() {
        try {
            mErrorCode = ERROR_UNKNOWN;
            // Check if Device is currently offline:
            if (cancelBecauseDeviceOffline()) {
                onFinished();
                return;
            }

//            // Start request, handle response in separate handler:
//            DefaultHttpClient httpclient = new DefaultHttpClient(getHttpParams());
//            if (mCookieStore == null)
//                mCookieStore = new BasicCookieStore();
//            httpclient.setCookieStore(mCookieStore);
//            modifyHttpClient(httpclient);
//            mRequest = createRequest();

            // Khởi tạo Retrofit để gán API ENDPOINT (Domain URL) cho Retrofit 2.0
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(mUrl)
                    // Sử dụng GSON cho việc parse và maps JSON data tới Object
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Khởi tạo các cuộc gọi cho Retrofit 2.0
            CategoryService hnFeedService = retrofit.create(CategoryService.class);

            Call<List<CategoryListModel.CategoryData>> call = hnFeedService.listAllCategory();
            // Cuộc gọi bất đồng bọ (chạy dưới background)
            //call.enqueue(getRetrofitResponseHandler());

            Response<List<CategoryListModel.CategoryData>> newPostResponse = call.execute();

            // Here call newPostResponse.code() to get response code
            int statusCode = newPostResponse.code();
            if(statusCode == 200) {
                List<CategoryListModel.CategoryData> newPost = newPostResponse.body();
                responseListHandlingFinished((List<T>) newPost, statusCode);
            }
            else if(statusCode == 401) {

            }

            //httpclient.execute(setRequestData(mRequest), getResponseHandler(httpclient));
        } catch (Exception e) {
            setErrorCode(ERROR_GENERIC_COMMUNICATION_ERROR);
            onFinished();
        }
    }

    public interface CategoryService {
        @GET("/api/categories")
        Call<List<CategoryListModel.CategoryData>> listAllCategory();
    }

    /**
     * Override this to make changes to the HTTP client before it executes the
     * request.
     *
     * @param client
     */
    protected void modifyHttpClient(DefaultHttpClient client) {
        // Override this if you need it.
    }

    /**
     * Notify all registered observers
     */
    protected void onFinished() {
        if (!mNotifyFinishedBroadcast)
            return;

        Intent broadcastIntent = new Intent(mNotificationBroadcastIntentID);
        broadcastIntent.putExtra(BROADCAST_INTENT_EXTRA_ERROR, mErrorCode);
        broadcastIntent.putExtra(BROADCAST_INTENT_EXTRA_RESPONSE, mResponse);
        LocalBroadcastManager.getInstance(mApplicationContext).sendBroadcast(broadcastIntent);
    }

    /**
     * Returns TRUE if OFFLINE.
     * 
     * @return boolean true if offline, or false if online.
     */
    protected boolean cancelBecauseDeviceOffline() {
        if (mApplicationContext != null && !ConnectivityUtils.isDeviceOnline(mApplicationContext)) {
            setErrorCode(ERROR_DEVICE_OFFLINE);
            return true;
        }
        return false;
    }

    public void cancel() {
        if (mRequest != null)
            mRequest.abort();
    }

    /**
     * Create a request object according to the request type set.
     *
     * @return HttpRequestBase request object.
     */
    protected HttpRequestBase createRequest() {
        switch (mType) {
            case GET:
                return new HttpGet(getUrlWithParams());
            case PUT:
                return new HttpPut(getUrlWithParams());
            case DELETE:
                return new HttpDelete(getUrlWithParams());
            default:
                return new HttpPost(getUrlWithParams());
        }
    }

    protected String getUrlWithParams() {
        return mUrl + (mURLQueryParams != null && !mURLQueryParams.equals("") ? "?" + mURLQueryParams : "");
    }

    public void responseHandlingFinished(T parsedResponse, int responseHttpStatus) {
        mActualStatusCode = responseHttpStatus;
        mResponse = parsedResponse;
        if (mActualStatusCode < 200 || mActualStatusCode >= 400)
            setErrorCode(ERROR_SERVER_RETURNED_ERROR);
        else if (mResponse == null)
            setErrorCode(ERROR_RESPONSE_PARSE_ERROR);
        else
            setErrorCode(ERROR_NONE);
        onFinished();
    }

    @Override
    public void responseListHandlingFinished(List<T> parsedResponse, int responseHttpStatus) {
        mActualStatusCode = responseHttpStatus;
        mListResponse = parsedResponse;
        if (mActualStatusCode < 200 || mActualStatusCode >= 400)
            setErrorCode(ERROR_SERVER_RETURNED_ERROR);
        else if (mListResponse == null)
            setErrorCode(ERROR_RESPONSE_PARSE_ERROR);
        else
            setErrorCode(ERROR_NONE);
        onFinished();
    }

    @Override
    public T getResponseContent() {
        return mResponse;
    }

    @Override
    public List<T> getListResponseContent() {
        return mListResponse;
    }

    @Override
    public int getCurrentpage() {
        return mCurrentpage;
    }

    protected void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    public Map<String, String> getBody() {
      return mBody;
    }

    @Override
    public int getErrorCode() {
        return mErrorCode;
    }

    public int getActualStatusCode() {
        return mActualStatusCode;
    }

    private HttpParams getHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, mHttpTimeoutMS);
        HttpConnectionParams.setSoTimeout(httpParameters, mSocketTimeoutMS);
        return httpParameters;
    }

    /**
     * Update the given request before it is sent over the wire.
     *
     * @param request
     */
    abstract protected HttpUriRequest setRequestData(HttpUriRequest request);

    abstract protected ResponseHandler<T> getResponseHandler(HttpClient client);

    abstract protected Callback<List<T>> getRetrofitResponseHandler();

    public void setCookieStore(CookieStore cookieStore) {
        mCookieStore = cookieStore;
    }

}
