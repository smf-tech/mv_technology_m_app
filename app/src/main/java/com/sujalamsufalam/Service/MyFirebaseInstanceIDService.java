package com.sujalamsufalam.Service;

/**
 * Created by Rohit Gujar on 06-11-2017.
 */

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sujalamsufalam.Utils.PreferenceHelper;

/**
 * Created by Belal on 5/27/2016.
 */


//Class extending FirebaseInstanceIdService
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        PreferenceHelper preferenceHelper = new PreferenceHelper(getApplicationContext());
        preferenceHelper.insertString(PreferenceHelper.TOKEN, refreshedToken);
        //Displaying token on logcat

        Log.d(TAG, "Refreshed token: " + refreshedToken);

    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}
