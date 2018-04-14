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

public class MessageReceiver extends FirebaseMessagingService{
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 6578;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        final String title = remoteMessage.getData().get("title");

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

            default:
                Log.d("Push Service", "Receiving not expected message type: " + type);
                break;
        }

        //Here we can have all sorts of messages:
        /*
        type                    notion
        appInvalidation         Invalidate App
        refreshData             New Data for devices -> Can lead to hazard notification
            ...
         */
        //So according to a parameter we choose how to act on -> type parameter


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
                manager.notify(NOTIFICATION_ID,notification);
            }
        }
    }

    private void showHazardNotifications(String title, String msg) {
        Intent i = new Intent(this, MainActivity.class);

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
            manager.notify(NOTIFICATION_ID,notification);
        }
    }

    private void updateDeviceData (String deviceID, String appID, String status){

        Log.d("Push_updateDeviceData", "Device: " + deviceID + " App: " + appID + " Status: " + status);
    }
}
