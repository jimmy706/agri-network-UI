package com.agrinetwork.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.entities.FirebaseMessageTypes;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MessagingService extends FirebaseMessagingService {
    private final static String CHANNEL_ID = "firebase_notification";
    private final static String CHANNEL_NAME = "firebase_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        publicNotification(remoteMessage);

    }

    private void publicNotification(RemoteMessage remoteMessage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }

        Map<String, String> payload = remoteMessage.getData();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(payload.get("title"))
                .setContentText(payload.get("message"))
                .setSmallIcon(R.drawable.ic_bell)
                .setAutoCancel(true);

        String notificationType = payload.get("type");
        if(notificationType.equals(FirebaseMessageTypes.FRIEND_REQUEST.getLabel())){
            Intent intent = new Intent(this, UserWallActivity.class);
            intent.putExtra("userId", payload.get("fromUser"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.setContentIntent(pendingIntent);
        }

        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(R.color.green_200);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        notificationManager.createNotificationChannel(notificationChannel);
    }
}