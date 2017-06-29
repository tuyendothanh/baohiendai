package com.manuelmaly.hn.server;

import android.content.Context;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import retrofit2.Callback;

public class CategoryDownloadCommand<T extends Serializable> extends CategoryBaseRetrofitCommand<T> {

    public CategoryDownloadCommand(String url, HashMap<String, String> queryParams, RequestType type, boolean notifyFinishedBroadcast,
                                   String notificationBroadcastIntentID, Context applicationContext, CookieStore cookieStore) {
        super(url, queryParams, type, notifyFinishedBroadcast, notificationBroadcastIntentID, applicationContext, 60000, 60000,
                null);
        setCookieStore(cookieStore);
    }

    public CategoryDownloadCommand(String url, int currentPage, HashMap<String, String> queryParams, RequestType type, boolean notifyFinishedBroadcast,
                                   String notificationBroadcastIntentID, Context applicationContext, CookieStore cookieStore) {
        super(url, currentPage, queryParams, type, notifyFinishedBroadcast, notificationBroadcastIntentID, applicationContext, 60000, 60000,
            null);
        setCookieStore(cookieStore);
    }

    @Override
    protected HttpUriRequest setRequestData(HttpUriRequest request) {
        request.setHeader(ACCEPT_HEADER, JSON_MIME);
        return request;
    }

    @Override
    protected ResponseHandler<T> getResponseHandler(HttpClient client) {
        //return new HTMLResponseHandler(this, client);
        return null;
    }

    @Override
    protected Callback<List<T>> getRetrofitResponseHandler() {
        return new RetrofitResponseHandler<T>(this);
    }
}
