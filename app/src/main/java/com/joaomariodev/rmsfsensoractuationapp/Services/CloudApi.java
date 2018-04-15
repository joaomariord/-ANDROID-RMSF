package com.joaomariodev.rmsfsensoractuationapp.Services;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class JsonObjectRequestAuthenticated extends JsonObjectRequest {
    public JsonObjectRequestAuthenticated(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-auth", App.prefs.getLoginToken());
        return headers;
    }
}


public class CloudApi {

    private static String BASE_URL = "http://rmsf-server.herokuapp.com";

    public static void setBaseUrl(String baseUrl, String port) {
        BASE_URL = baseUrl + ":" + port + "/";
    }

    private static String getUrl(String route){
        return BASE_URL + route;
    }

    //POST /device/thr/gas
    //POST /device/set/alrt
    //POST /device/set/wtr
    //POST /device/thr/temp
    //Auth, appID, set , deviceID
    public static void postConfigs(Configs config,String appID, String deviceID,
                                   String valueToSet,Response.Listener<JSONObject> res,
                                   Response.ErrorListener err) {
        String get_url;
        switch (config){
            case GAS:
                get_url = "device/thr/gas";
                break;
            case ALARM:
                get_url = "device/set/alrt";
                break;
            case TEMPERATURE:
                get_url = "device/thr/temp";
                break;
            case WATERPUMP:
                get_url = "device/set/wtr";
                break;
            default: return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("set", valueToSet);
            body.put("appID", appID);
            body.put("deviceID", deviceID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.POST,
                CloudApi.getUrl(get_url), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /store
    //Auth, token_push, old_token_push
    public static void postStoreNewPushToken(String newPushToken, String oldPushToken,
                                             Response.Listener<JSONObject> res,
                                             Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("token_push", newPushToken);
            body.put("old_token_push", oldPushToken);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.POST,
                CloudApi.getUrl("store"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /register
    //email, password, name
    public static void postRegister(String userName, String email, String password,
                                             Response.Listener<JSONObject> res,
                                             Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("name", userName);
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequest(Request.Method.POST,
                CloudApi.getUrl("register"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /login
    //email, password, token_push
    public static void postLogin(String email, String password, String token_push,
                                    Response.Listener<JSONObject> res,
                                    Response.ErrorListener err){
        final JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
            body.put("token_push", token_push);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequest(Request.Method.POST,
                CloudApi.getUrl("login"), body, res , err ){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if( response.statusCode == 200){ //Retrieve body and x-auth header
                    String x_auth =  response.headers.get("x-auth");
                    Response<JSONObject> res = super.parseNetworkResponse(response);
                    try {
                        res.result.put("x-auth",x_auth);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return  res;
                } else {
                    return super.parseNetworkResponse(response);
                }
            }
        };

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /logout
    //Auth, token_push
    public static void postLogout(String pushToken,
                                    Response.Listener<JSONObject> res,
                                    Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("token_push", pushToken);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.POST,
                CloudApi.getUrl("logout"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /application
    //Auth, appID, appKey
    public static void postApp(String appID, String appKey,
                                  Response.Listener<JSONObject> res,
                                  Response.ErrorListener err ){
        JSONObject body = new JSONObject();
        try {
            body.put("appID", appID);
            body.put("appKey", appKey);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.POST,
                CloudApi.getUrl("application"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //DELETE /application
    //Auth, appID
    public static void deleteApp(String appID,
                               Response.Listener<JSONObject> res,
                               Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("appID", appID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.DELETE,
                CloudApi.getUrl("application"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }
    
    //GET /application (appID query)
    //Auth
    public static void getApp(String appID,
                               Response.Listener<JSONObject> res,
                               Response.ErrorListener err){

        final HashMap<String, String> query = new HashMap<>();
        query.put("appID", appID);

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.GET,
                CloudApi.getUrl("application"), null, res , err ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return query;
            }
        };

        App.prefs.getRequestQueue().add(api_request);
    }

    //POST /device
    //Auth, appID, deviceID
    public static void postDevice(String appID, String deviceID,
                               Response.Listener<JSONObject> res,
                               Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("appID", appID);
            body.put("deviceID", deviceID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.POST,
                CloudApi.getUrl("device"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //DELETE /device
    //Auth, appID, deviceID
    public static void deleteDevice(String appID, String deviceID,
                                  Response.Listener<JSONObject> res,
                                  Response.ErrorListener err){
        JSONObject body = new JSONObject();
        try {
            body.put("appID", appID);
            body.put("deviceID", deviceID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.DELETE,
                CloudApi.getUrl("device"), body, res , err );

        App.prefs.getRequestQueue().add(api_request);
    }

    //GET /device (deviceID query)
    //Auth
    public static void getDevice(String deviceID,
                              Response.Listener<JSONObject> res,
                              Response.ErrorListener err){

        final HashMap<String, String> query = new HashMap<>();
        query.put("deviceID", deviceID);

        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.GET,
                CloudApi.getUrl("device"), null, res , err ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return query;
            }
        };

        App.prefs.getRequestQueue().add(api_request);
    }

    //GET /status
    //Auth
    public static void getStatus(Response.Listener<JSONObject> res,
                                 Response.ErrorListener err){


        JsonRequest api_request = new JsonObjectRequestAuthenticated(Request.Method.GET,
                CloudApi.getUrl("status"), null, res , err );
        App.prefs.getRequestQueue().add(api_request);
    }

    //GET /online Just a test to api on air
    public static void testApi(Response.Listener<JSONObject> res, Response.ErrorListener err){

        JsonRequest api_request = new JsonObjectRequest(Request.Method.GET,
                CloudApi.getUrl("online"), null, res , err );
        App.prefs.getRequestQueue().add(api_request);
    }


    public enum Configs {
        GAS, ALARM, TEMPERATURE, WATERPUMP
    }


}


