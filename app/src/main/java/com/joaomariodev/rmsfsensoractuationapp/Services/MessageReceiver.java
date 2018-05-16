package com.joaomariodev.rmsfsensoractuationapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;
import com.joaomariodev.rmsfsensoractuationapp.Controller.MainActivity;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MessageReceiver extends FirebaseMessagingService{
    private static final int REQUEST_CODE = 1;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("Push Service", "Received message of type " + remoteMessage.getData().get("type"));

        String type = remoteMessage.getData().get("type");
        switch (type){
            case "appInvalidation":
                //Here send to invalidateAppWarning
                invalidateAppWarning(remoteMessage.getData().get("appID"));
                break;
            case "refreshData":
                //Here send to updateDeviceData
                updateDeviceData(remoteMessage.getData().get("deviceID"),
                        remoteMessage.getData().get("appID"), remoteMessage.getData().get("status"));
                break;

            case "appOrDeviceUpdated":
                if(App.Companion.isMainActivityVisible()){
                    //Send broadcast
                    Intent refresh = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(refresh);
                }
                break;
            default:
                Log.d("Push Service", "Receiving not expected message type: " + type);
                break;
        }
    }

    private void invalidateAppWarning(String appID){
        //Here create a Warning via broadcast if the user is in the app
        //Otherwise create a warning via notification

        //Check if app is running
        if(App.Companion.isMainActivityVisible()){
            //Send broadcast
            Intent inval = new Intent(ConstantsO.INSTANCE.getBROADCAST_INVAL_APP());
            inval.putExtra("appID", appID);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(inval);
        }
        else {
            //Send notification
            Intent i = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                    i, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

            @SuppressWarnings("deprecation")
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentText("App is invalid")
                    .setContentTitle("Please remove " + appID )
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_info_black_24dp)
                    .setColor(Color.BLUE)
                    .setLargeIcon(logo)
                    .setAutoCancel(true)
                    .build();

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                int uniqueId = 0;
                for (int index = 0; index < appID.length(); index++){
                    uniqueId += ((int) appID.charAt(index));
                }
                manager.notify(uniqueId,notification);
            }
        }
    }

    private void showHazardNotifications(String title, String msg) {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        @SuppressWarnings("deprecation")
        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(msg)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.alarm)
                .setColor(Color.RED)
                .setLargeIcon(logo)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            int uniqueId = 0;
            for (int index = 0; index < title.length(); index++){
                uniqueId += ((int) title.charAt(index));
            }
            manager.notify(uniqueId,notification);
        }
    }

    private void updateDeviceData (String deviceID, String appID, String status){

        Log.d("Push_updateDeviceData", "Device: " + deviceID + " App: " + appID + " Status: " + status);
        if(App.Companion.isMainActivityVisible() && Objects.equals(UserDataService.INSTANCE.getSelectedDeviceID(), deviceID) &&
                Objects.equals(UserDataService.INSTANCE.getSelectedAppID(), appID)){

            Intent update = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_FRAGMENTS());

            try {
                JSONObject device = new JSONObject(status);
                JSONObject temp = device.getJSONObject("temp");
                JSONObject gas = device.getJSONObject("gas");
                JSONObject alarm = device.getJSONObject("alert");
                JSONObject water = device.getJSONObject("water");

                update.putExtra("gas_threshold", gas.getString("threshold"));
                update.putExtra("temp_threshold", temp.getString("threshold"));
                update.putExtra("water_operational",water.getString("operational"));
                update.putExtra("alarm_operational",alarm.getString("operational"));
                update.putExtra("gas_status", gas.getString("status"));
                update.putExtra("temp_status", temp.getString("status"));
                update.putExtra("water_status", water.getString("status"));
                update.putExtra("alarm_status", alarm.getString("status"));
                update.putExtra("last_seen", device.getString("lastseen"));
                update.putExtra("initialized", true);

                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(update);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONObject device = new JSONObject(status);
            JSONObject temp = device.getJSONObject("temp");
            JSONObject gas = device.getJSONObject("gas");

            Double gs = Double.parseDouble(gas.getString("status"));
            Double gt = Double.parseDouble(gas.getString("threshold"));

            Double ts = Double.parseDouble(temp.getString("status"));
            Double tt = Double.parseDouble(temp.getString("threshold"));

            if(gs > gt || ts > tt){
                //Send notification
                showHazardNotifications("On " + deviceID + " at " + appID, "Sensor levels are too high");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
