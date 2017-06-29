package com.manuelmaly.hn.server;

import android.content.Context;

import com.manuelmaly.hn.model.HNPost;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Callback;

public class GetHNUserTokenHTTPCommand extends BaseHTTPCommand<HNPost> {

  public GetHNUserTokenHTTPCommand(String url, HashMap<String, String> queryParams, RequestType type, boolean notifyFinishedBroadcast,
      String notificationBroadcastIntentID, Context applicationContext, Map<String, String> body) {
    super(url, queryParams, type, notifyFinishedBroadcast, notificationBroadcastIntentID, applicationContext, 60000, 60000,
        body);
  }

  @Override
  protected void modifyHttpClient(DefaultHttpClient client) {
    super.modifyHttpClient(client);
    HttpClientParams.setRedirecting(client.getParams(), false);
  }

  @Override
  protected HttpUriRequest setRequestData(HttpUriRequest request) {

    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
    Map<String, String> body = getBody();
    if (body != null) {
      for (String key : body.keySet()) {
        params.add(new BasicNameValuePair(key, body.get(key)));
      }
    }

    try {
      ((HttpPost) request).setEntity((new UrlEncodedFormEntity(params, "UTF-8")));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return request;
  }

  @Override
  protected ResponseHandler<HNPost> getResponseHandler(HttpClient client) {
    return new GetHNUserTokenResponseHandler(this, client);
  }

  @Override
  protected Callback<List<HNPost>> getRetrofitResponseHandler() {
    return null;
  }

  @Override
  public void responseListHandlingFinished(List<HNPost> parsedResponse, int responseHttpStatus) {
    ;
  }
}
