package com.joaomariodev.rmsfsensoractuationapp;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joaom on 20/02/2018.
 */

class CloudHandler {
    private Random die = new Random();

    //Variables to sync from and to cloud
    private Double sensorSmokeReading = Double.valueOf(0);
    private Double sensorTempReading = Double.valueOf(0);
    private Double sensorSmokeThreshold;
    private Double sensorTempThreshold;
    private String raw;

    Double getSensorSmokeReading() {
        return sensorSmokeReading;
    }

    Double getSensorTempReading() {
        return sensorTempReading;
    }

    Double getSensorSmokeThreshold() {
        return sensorSmokeThreshold;
    }

    void setSensorSmokeThreshold(Double sensorSmokeThreshold) {
        this.sensorSmokeThreshold = sensorSmokeThreshold;
    }

    Double getSensorTempThreshold() {
        return sensorTempThreshold;
    }

    void setSensorTempThreshold(Double sensorTempThreshold) {
        this.sensorTempThreshold = sensorTempThreshold;
    }

    //Sync action
    void Action_Sync (View view){
        //Get data from cloud
//
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://jsonplaceholder.typicode.com/posts/1", null, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            sensorSmokeReading=response.getDouble("userId");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("SSS", "onFailure: " + errorResponse);
                    }
                }
        );

//        sensorSmokeReading = die.nextDouble();
        sensorTempReading = die.nextDouble();

        Log.d("SSS", "EHFE");
        Toast.makeText(view.getContext(),"Data updated from cloud", Toast.LENGTH_LONG).show();
    }

    void Action_Sync_Smoke (View view){
        //Send data to cloud

        Toast.makeText(view.getContext(),"Data updated to cloud", Toast.LENGTH_LONG).show();
    }

    void Action_Sync_Temp (View view){
        //Send data to cloud

        Toast.makeText(view.getContext(),"Data updated to cloud", Toast.LENGTH_LONG).show();
    }
}
