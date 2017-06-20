package com.manuelmaly.hn.server;

import android.content.Context;

import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class StringDownloadCommand extends BaseHTTPCommand<HNPost> {

    public StringDownloadCommand(String url, HashMap<String, String> queryParams, RequestType type, boolean notifyFinishedBroadcast,
        String notificationBroadcastIntentID, Context applicationContext, CookieStore cookieStore) {
        super(url, queryParams, type, notifyFinishedBroadcast, notificationBroadcastIntentID, applicationContext, 60000, 60000,
            null);
        setCookieStore(cookieStore);
    }

    @Override
    protected HttpUriRequest setRequestData(HttpUriRequest request) {
        request.setHeader(ACCEPT_HEADER, JSON_MIME);
        return request;
    }

    @Override
    protected ResponseHandler<HNPost> getResponseHandler(HttpClient client) {
        //return new HTMLResponseHandler(this, client);
        return null;
    }

    @Override
    protected Callback<List<HNPost>> getRetrofitResponseHandler() {
        return new RetrofitResponseHandler<HNPost>(this);
    }
}
