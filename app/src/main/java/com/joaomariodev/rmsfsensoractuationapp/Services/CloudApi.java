package com.joaomariodev.rmsfsensoractuationapp.Services;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by joaom on 20/02/2018. To make simpler http communications
 */

public class CloudApi {
    private static String BASE_URL = "http://rmsf-server.herokuapp.com/";

    public static void setBaseUrl(String baseUrl, String port) {
        BASE_URL = baseUrl + ":" + port + "/";
    }

    static String getUrl(String route){
        return BASE_URL + route;
    }

    public static void get(Response.Listener<JSONObject> res, Response.ErrorListener err) {
        JsonRequest getStatusRequest = new JsonObjectRequest(Request.Method.GET, CloudApi.getUrl("status"), null, res , err );
        App.prefs.getRequestQueue().add(getStatusRequest);

    }

    public static void post(String postRoute,boolean setParam, Response.Listener<JSONObject> res, Response.ErrorListener err) {

        JSONObject params = new JSONObject();
        try {
            params.put("set", setParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonPost(postRoute, params, res, err);
    }

    public static void post(String postRoute,Double setParam, Response.Listener<JSONObject> res, Response.ErrorListener err) {
        JSONObject params = new JSONObject();
        try {
            params.put("set", setParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonPost(postRoute, params, res, err);
    }

    private static void jsonPost(String postRoute, JSONObject params, Response.Listener<JSONObject> res, Response.ErrorListener err){
        JsonRequest postChangesRequest = new JsonObjectRequest(Request.Method.POST, CloudApi.getUrl(postRoute), params, res , err );
        App.prefs.getRequestQueue().add(postChangesRequest);
    }
}


