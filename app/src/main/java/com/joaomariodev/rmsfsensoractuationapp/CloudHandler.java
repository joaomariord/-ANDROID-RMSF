package com.joaomariodev.rmsfsensoractuationapp;

import android.view.View;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by joaom on 20/02/2018.
 */

class CloudHandler {
    private Random die = new Random();

    //Variables to sync from and to cloud
    private Double sensorSmokeReading;
    private Double sensorTempReading;
    private Double sensorSmokeThreshold;
    private Double sensorTempThreshold;

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
        sensorSmokeReading = die.nextDouble();
        sensorTempReading = die.nextDouble();

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
