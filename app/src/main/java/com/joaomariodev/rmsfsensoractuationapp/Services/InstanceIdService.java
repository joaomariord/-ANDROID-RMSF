package com.joaomariodev.rmsfsensoractuationapp.Services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;

import org.json.JSONException;
import org.json.JSONObject;

public class InstanceIdService extends FirebaseInstanceIdService {

    public InstanceIdService() {
        super();
    }

    private static void sendTokenToServer(Context context, String tokenToSend){
        JSONObject token = null;
        try {
            token = new JSONObject().put("token",tokenToSend);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonRequest storeRequest = new JsonObjectRequest(Request.Method.POST, CloudApi.getUrl("store") , token, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                App.prefs.setFCMtokenStored(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                App.prefs.setFCMtokenStored(false);
            }
        });
        Volley.newRequestQueue(context).add(storeRequest); //This is separate to avoid sharing request queue between service and application
    }

    public static void retryTokenStore(Context context){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(context, refreshedToken);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Context context = getApplicationContext();
        sendTokenToServer(context, refreshedToken);
    }


}
