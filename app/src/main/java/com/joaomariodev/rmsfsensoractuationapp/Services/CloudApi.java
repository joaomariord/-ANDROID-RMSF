package com.joaomariodev.rmsfsensoractuationapp.Services;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by joaom on 20/02/2018. To make simpler http communications
 */

public class CloudApi {
    private static String BASE_URL = "http://rmsf-server.herokuapp.com";
    private static int PORT = 80;
    private static AsyncHttpClient client = new AsyncHttpClient(PORT);

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl + "/";
    }

    public static void setPORT(int PORT) {
        CloudApi.PORT = PORT;
    }

    public static void get(AsyncHttpResponseHandler responseHandler) {
        String get_route = "status";
        client.get(getAbsoluteUrl(get_route), null, responseHandler);
    }

    public static void post(String postRoute,boolean setParam, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("set", setParam);
        params.setUseJsonStreamer(true);
        client.post(getAbsoluteUrl(postRoute), params , responseHandler);
    }

    public static void post(String postRoute,Double setParam, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("set", setParam);
        params.setUseJsonStreamer(true);
        client.post(getAbsoluteUrl(postRoute), params , responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}


