package com.mv.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.mv.Activity.AttendanceActivity;
import com.mv.R;

/**
 * Created by user on 7/23/2018.
 */

//Receive the Alarm for checkin time every day
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intentt = new Intent(context, AttendanceActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentt, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.app_logo)
                .setTicker("Hearty365")
                .setContentTitle("Check In")
                .setContentText("You have to Check-In in 10 minuets.")
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, b.build());
        }

//        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
    }
}
