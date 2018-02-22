package com.joaomariodev.rmsfsensoractuationapp;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by joaom on 20/02/2018.
 */

class CloudApi {
    private static String BASE_URL = "http://jsonplaceholder.typicode.com/";
    private static int PORT = 80;
    private static AsyncHttpClient client = new AsyncHttpClient(PORT);

    static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl + "/";
    }

    static void setPORT(int PORT) {
        CloudApi.PORT = PORT;
    }

    static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}


