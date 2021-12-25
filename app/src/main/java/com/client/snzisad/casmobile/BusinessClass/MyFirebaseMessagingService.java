package com.client.snzisad.casmobile.BusinessClass;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.client.snzisad.casmobile.*;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by snzisad on 9/7/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size() > 0){
            pushNotification(remoteMessage.getData().get("id"), remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData().get("type"));
        }
    }

    private void pushNotification(String id, String title, String body, String type) {

        DataHandler.productID = Integer.parseInt(id);

        //default intent, product list activity
        Intent intent = new Intent(getApplicationContext(), ProductActivity.class);

        if(type.equals("1")){
            //if type = 1, go to chat activity
            intent = new Intent(getApplicationContext(), ChatActivity.class);
        }
        else if(type.equals("2")){
            //if type = 2, goto productdesctiption activity
            intent = new Intent(getApplicationContext(), ProductDetailsActivity.class);
        }
        else{
            //clear previous data
            DataHandler.productID = 0;
        }

        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("pid", id);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(body);

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        notificationBuilder.setAutoCancel(true);

        //notification sound
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        r.play();

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = this.getString(R.string.default_notification_channel_id);
            NotificationChannel channel = new NotificationChannel(channelId,   title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(body);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }

        notificationManager.notify(0, notificationBuilder.build());

    }


}
