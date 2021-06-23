package com.example.doordashapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.doordashapp.R;
import com.example.doordashapp.activities.OrderDetailsOwnerActivity;
import com.example.doordashapp.activities.OrderDetailsUsersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";//required for android 0 and above

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //all notifications will be received here

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notification
        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("NewOrder")) {
            String customerUid = remoteMessage.getData().get("customerUid");
            String ownerUid = remoteMessage.getData().get("ownerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(ownerUid)) {
                //user is signed in and is same user to which notification is sent
                showNotification(orderId, ownerUid, customerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
        if (notificationType.equals("OrderStatusChanged")) {

            String customerUid = remoteMessage.getData().get("customerUid");
            String ownerUid = remoteMessage.getData().get("ownerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(customerUid)) {
                //user is signed in and is same user to which notification is sent
                showNotification(orderId, ownerUid, customerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
    }

    private void showNotification(String orderId, String ownerUid, String customerUid, String notificationTitle,
                                  String notificationMessage, String notificationType) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //id for notification,random
        int notificationID = new Random().nextInt(3000);

        //check if android ver is oreo/O above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }

        //handle notification click,start order activity
        Intent intent = null;
        if (notificationType.equals("NewOrder")) {
            //open orderDetailsOwnerActivity
            intent = new Intent(this, OrderDetailsOwnerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", customerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if (notificationType.equals("OrderStatusChanged")) {
            //open OrderDetailsUsersActivity
            intent = new Intent(this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", ownerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        //sound
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)

    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
