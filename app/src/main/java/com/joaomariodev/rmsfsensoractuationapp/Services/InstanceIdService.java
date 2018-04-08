package com.joaomariodev.rmsfsensoractuationapp.Services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class InstanceIdService extends FirebaseInstanceIdService {

    public InstanceIdService() {
        super();
    }

    public static void sendTokenToServer(String tokenToSend){
        CloudApi.post("/store", tokenToSend, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("TokenFCM", "Success setting FCM token: "+responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("TokenFCM", "Failed setting FCM token: "+responseBody);
            }
        });
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendTokenToServer(refreshedToken);
    }


}
