package com.manuelmaly.hn.server;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles HTML response of a {@link HttpClient}.
 * @author manuelmaly
 */
public class RetrofitResponseHandler<T> implements Callback<List<T>> {

    private IAPICommand<T> mCommand;

    public RetrofitResponseHandler(IAPICommand<T> command) {
        mCommand = command;
    }
    
//    public String handleResponse(HttpResponse response)
//            throws ClientProtocolException, IOException {
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        response.getEntity().writeTo(out);
//        final StatusLine statusLine = response.getStatusLine();
//        final T responseString = (T) out.toString();
//        out.close();
//        int statusCode = statusLine.getStatusCode();
//
//        mCommand.responseHandlingFinished(responseString, statusCode);
//        return null;
//    }

    @Override
    public void onResponse(Call<List<T>> call, Response<List<T>> response) {
        mCommand.responseListHandlingFinished(response.body(), response.code());
    }

    @Override
    public void onFailure(Call<List<T>> call, Throwable t) {
        ;
    }
}
