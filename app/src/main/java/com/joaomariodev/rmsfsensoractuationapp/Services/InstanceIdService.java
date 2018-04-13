package com.joaomariodev.rmsfsensoractuationapp.Services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;

import org.json.JSONObject;

public class InstanceIdService extends FirebaseInstanceIdService {

    public InstanceIdService() {
        super();
    }

    private static void sendTokenToServer(Context context, String tokenToSend){
        CloudApi.postStoreNewPushToken(tokenToSend, App.prefs.getStoredFCMtoken(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("UpdateToken", "Success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("UpdateToken", error.toString());
            }
        });
    }


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Context context = getApplicationContext();
        sendTokenToServer(context, refreshedToken);
    }


}
