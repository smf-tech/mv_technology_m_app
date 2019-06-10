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
import com.mv.Activity.AccountSectionActivity;
import com.mv.Activity.AssetAllocatedListActivity;
import com.mv.Activity.AttendanceActivity;
import com.mv.Activity.AttendanceApproval2Activity;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.ExpandableListActivity;
import com.mv.Activity.HomeActivity;
import com.mv.Activity.LeaveApprovalActivity;
import com.mv.Activity.ProcessApprovalActivity;
import com.mv.Activity.SalaryListActivity;
import com.mv.Activity.SplashScreenActivity;
import com.mv.Activity.VoucherListActivity;
import com.mv.ActivityMenu.GroupsFragment;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.ActivityMenu.TeamManagementFragment;
import com.mv.ActivityMenu.TrainingCalender;
import com.mv.Model.Community;
import com.mv.Model.Notifications;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.Arrays;
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
                List<String> allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameApproved().split(";")));

                if (preferenceHelper != null) {
                    if (preferenceHelper.getBoolean(PreferenceHelper.NOTIFICATION)) {
                        //sendNotification(remoteMessage.getData().get("Title"), remoteMessage.getData().get("Description"));
                        //  Log.e("Approv",User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved());

                        if ((User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("true"))) {
                            if (allTab.contains(Constants.My_Community)) {
                                //check for the mute Notifications of cammunity
                                boolean isNotify=true;
                                List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
                                for (int i = 0; i < list.size(); i++) {
                                    String title = remoteMessage.getData().get("Title");
                                    if (title != null && title.contains(list.get(i).getName()) &&
                                            list.get(i).getMuteNotification() != null &&
                                            list.get(i).getMuteNotification().equals("Unmute")) {
                                        isNotify = false;
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
                    for (int i = 0; i < list.size(); i++) {
                        String title = remoteMessage.getData().get("Title");
                        if (title != null && title.contains(list.get(i).getName()) &&
                                list.get(i).getMuteNotification() != null &&
                                list.get(i).getMuteNotification().equals("Unmute")) {
                            isNotify = false;
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
        Intent notificationIntent = null;
//        if (mId == null) {
//            notificationIntent = new Intent(this, SplashScreenActivity.class);
//        } else if (TextUtils.isEmpty(mId)) {
//            notificationIntent = new Intent(this, SplashScreenActivity.class);
//        } else if(mId.equals("Attendance")){
//            notificationIntent = new Intent(this, AttendanceActivity.class);
//        }
//        else {
//            preferenceHelper.insertString(PreferenceHelper.COMMUNITYID, mId);
//            List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
//            int position = -1;
//            Community community = new Community();
//            for (int i = 0; i < list.size(); i++) {
//                if (list.get(i).getId().equalsIgnoreCase(mId)) {
//                    position = i;
//                    community = list.get(i);
//                    break;
//                }
//            }
//            if (community != null) {
//                int count;
//                if (community.getCount() == null || TextUtils.isEmpty(community.getCount()))
//                    count = 0;
//                else
//                    count = Integer.parseInt(community.getCount());
//                count = count + 1;
//                community.setCount("" + count);
//                AppDatabase.getAppDatabase(getApplicationContext()).userDao().updateCommunities(community);
//                if (position > 0)
//                    list.remove(position);
//                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//                String json = gson.toJson(list);
//                notificationIntent = new Intent(getApplicationContext(), CommunityHomeActivity.class);
//                notificationIntent.putExtra(Constants.TITLE, community.getName());
//                notificationIntent.putExtra(Constants.LIST, json);
//            }
//        }

        switch (mId){
            case "Attendance":
                notificationIntent = new Intent(this, AttendanceActivity.class);
                break;
            case "Asset":
                notificationIntent = new Intent(this, AssetAllocatedListActivity.class);
                break;
            case "Register":
                notificationIntent = new Intent(this, HomeActivity.class);
                break;
            case "Ho Support":
                notificationIntent = new Intent(this, GroupsFragment.class);
                break;
            case "Process Answer Comment":
                notificationIntent = new Intent(this, ProgrammeManagmentFragment.class);
                break;
            case "Leave":
                notificationIntent = new Intent(this, TeamManagementFragment.class);
                break;
            case "Leave Approval":
                notificationIntent = new Intent(this, TeamManagementFragment.class);
                break;
            case "User Approve":
                notificationIntent = new Intent(this, TeamManagementFragment.class);
                break;
            case "Calendar":
                notificationIntent = new Intent(this, TrainingCalender.class);
                break;
            case "Comment On Post":
                notificationIntent = new Intent(this, ProgrammeManagmentFragment.class);
                break;
            case "Event":
                notificationIntent = new Intent(this, TrainingCalender.class);
                break;
            case "Return Advance":
                notificationIntent = new Intent(this, VoucherListActivity.class);
                break;
            case "Salary Deposite":
                notificationIntent = new Intent(this, SalaryListActivity.class);
                break;
            case "Mulyavardhan Content":
                notificationIntent = new Intent(this, ExpandableListActivity.class);
                break;
            case "Form":
                notificationIntent = new Intent(this, ProgrammeManagmentFragment.class);
                break;
            case "Form Approval":
                notificationIntent = new Intent(this, ProcessApprovalActivity.class);
                break;
            case "Attendance Approval":
                notificationIntent = new Intent(this, AttendanceApproval2Activity.class);
                break;
                default :
                    notificationIntent = new Intent(this, HomeActivity.class);
        }

        if (notificationIntent != null) {
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


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
