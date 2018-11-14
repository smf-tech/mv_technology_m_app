package com.mv;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mv.Utils.Constants;
import com.mv.Utils.ForceUpdateChecker;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nanostuffs on 02-11-2017.
 */
public class Mulyavardhan extends Application {
    private static final String TAG = "My App Class";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        Map<String, Object> remoteConfigDefaults = new HashMap();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "1.6");
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
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}