package com.mv.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.SplashScreenActivity;
import com.mv.Model.Community;
import com.mv.Model.Notifications;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Belal on 5/27/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private PreferenceHelper preferenceHelper;
    private String mId = "";
    private List<String> allTab = new ArrayList<>();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            try {
                preferenceHelper = new PreferenceHelper(getApplicationContext());
                mId = remoteMessage.getData().get("Id");
                allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameApproved().split(";")));

                if (preferenceHelper != null) {
                    if (preferenceHelper.getBoolean(PreferenceHelper.NOTIFICATION)) {
                        //sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                        //  Log.e("Approv",User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved());

                        if ((User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("true"))) {
                            if (allTab.contains(Constants.My_Community)) {
                                //check for the mute Notifications of cammunity
                                boolean isNotify=true;
                                List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
                                for(int i=0;i<list.size();i++){
                                    if(remoteMessage.getData().get("Title").contains(list.get(i).getName())&& list.get(i).getMuteNotification()!=null &&
                                            list.get(i).getMuteNotification().equals("Unmute")){
                                        isNotify=false;
                                        break;
                                    }
                                }
                                if(isNotify)
                                    sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                                Notifications data = new Notifications();
                                data.setId(remoteMessage.getData().get("Id"));
                                data.setTitle(remoteMessage.getData().get("Title"));
                                data.setDescription(remoteMessage.getData().get("Description"));
                                data.setStatus("unread");
                                AppDatabase.getAppDatabase(this).userDao().insertNotification(data);

                                // notify for new notification.
                                Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
                                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                            }
                        }
                    }
                } else {
                    //check for the mute Notifications of cammunity
                    boolean isNotify=true;
                    List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
                    for(int i=0;i<list.size();i++){
                        if(remoteMessage.getData().get("Title").contains(list.get(i).getName()) && list.get(i).getMuteNotification()!=null &&
                                list.get(i).getMuteNotification().equals("Unmute")){
                            isNotify=false;
                            break;
                        }
                    }
                    if(isNotify)
                        sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                    Notifications data = new Notifications();
                    data.setId(remoteMessage.getData().get("Id"));
                    data.setTitle(remoteMessage.getData().get("Title"));
                    data.setDescription(remoteMessage.getData().get("Description"));
                    data.setStatus("unread");
                    AppDatabase.getAppDatabase(this).userDao().insertNotification(data);

                    // notify for new notification.
                    Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
            if (community != null) {
                int count;
                if (community.getCount() == null || TextUtils.isEmpty(community.getCount()))
                    count = 0;
                else
                    count = Integer.parseInt(community.getCount());
                count = count + 1;
                community.setCount("" + count);
                AppDatabase.getAppDatabase(getApplicationContext()).userDao().updateCommunities(community);
                if (position > 0)
                    list.remove(position);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(list);
                intent = new Intent(getApplicationContext(), CommunityHomeActivity.class);
                intent.putExtra(Constants.TITLE, community.getName());
                intent.putExtra(Constants.LIST, json);
            }
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
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

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }
}
