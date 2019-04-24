package com.mv;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mv.Utils.Constants;
import com.mv.Utils.ForceUpdateChecker;
import com.mv.Utils.PreferenceHelper;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nanostuffs on 02-11-2017.
 */
public class Mulyavardhan extends Application {
    private static final String TAG = "My App Class";
    private PreferenceHelper preferenceHelper;
    public static final String KEY_FIREBASE_IMAGE_URL = "SS_firebase_image_url";
    //get isDeleteBackendForm value which tells if form have to be delete or not on image upload failure in form submit flow.
    public static final String KEY_FIREBBASE_FORM_DELETE = "SS_firebase_delete_backend_form";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        preferenceHelper = new PreferenceHelper(this);
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        Map<String, Object> remoteConfigDefaults = new HashMap<>();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "1.6");
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_MINIMUM_REQUIRED_VERSION, "1.6");
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_URL, Constants.playStoreLink);

        firebaseRemoteConfig.setDefaults(remoteConfigDefaults);
        firebaseRemoteConfig.fetch(1000 * 60) // fetch every minutes
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "remote config is fetched.");
                        firebaseRemoteConfig.activateFetched();
                    }
                }).addOnFailureListener(exception -> {
            Log.d(TAG, "Fetch failed");
            // Do whatever should be done on failure
        });
        //get image url from firebase
        String firebase_Image_Url = firebaseRemoteConfig.getString(KEY_FIREBASE_IMAGE_URL);
        if(firebase_Image_Url == null || firebase_Image_Url.isEmpty()){
            preferenceHelper.insertString(PreferenceHelper.FirebaseImageUrl, "https://dcwn642pmzpls.cloudfront.net/");
        }else {
            preferenceHelper.insertString(PreferenceHelper.FirebaseImageUrl, firebase_Image_Url);
        }
        //get isDeleteBackendForm value which tells if form have to be delete or not on image upload failure in form submit flow.
        String isDeleteBackendForm = firebaseRemoteConfig.getString(KEY_FIREBBASE_FORM_DELETE);
        if(isDeleteBackendForm == null || isDeleteBackendForm.isEmpty()){
            preferenceHelper.insertString(PreferenceHelper.isDeleteBackendForm, "false");
        }else {
            preferenceHelper.insertString(PreferenceHelper.isDeleteBackendForm, isDeleteBackendForm);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}