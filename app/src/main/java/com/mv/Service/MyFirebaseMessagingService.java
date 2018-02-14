package com.mv.Service;

/**
 * Created by Rohit Gujar on 06-11-2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.SplashScreenActivity;
import com.mv.Model.Community;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by Belal on 5/27/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private PreferenceHelper preferenceHelper;
    private String mId = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            try {
                preferenceHelper = new PreferenceHelper(getApplicationContext());
                mId = remoteMessage.getData().get("Id");
                if (preferenceHelper != null) {
                    if (preferenceHelper.getBoolean(PreferenceHelper.NOTIFICATION)) {
                        sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                    }
                } else {
                    sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Check if message contains a notification payload.


        //Calling method to generate notification

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageTitle, String messageBody) {
        Intent intent = null;
        if (mId == null) {
            intent = new Intent(this, SplashScreenActivity.class);
        } else if (TextUtils.isEmpty(mId)) {
            intent = new Intent(this, SplashScreenActivity.class);
        } else {
            preferenceHelper.insertString(PreferenceHelper.COMMUNITYID, mId);
            List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
            int position = -1;
            Community community = new Community();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equalsIgnoreCase(mId)) {
                    position = i;
                    community = list.get(i);
                    break;
                }
            }
            if (position > 0)
                list.remove(position);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(list);
            Log.e("name-->",community.getName());
            intent = new Intent(getApplicationContext(), CommunityHomeActivity.class);
            intent.putExtra(Constants.TITLE, community.getName());
            intent.putExtra(Constants.LIST, json);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = null;
        defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
