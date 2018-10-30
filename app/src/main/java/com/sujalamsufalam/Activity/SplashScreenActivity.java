package com.sujalamsufalam.Activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.databinding.ActivityMainBinding;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        // TODO Remove this code after build
        PreferenceHelper preference = new PreferenceHelper(this);
        if (preference.getBoolean(PreferenceHelper.FIRSTTIME_V_2_7)) {
            preference.clearPrefrences(PreferenceHelper.UserData);
            preference.insertBoolean(PreferenceHelper.FIRSTTIME_V_2_7, false);
        }

        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video");
        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image");
        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download");
        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip");
        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip");
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(() -> {
            Intent intent;
            if (User.getCurrentUser(SplashScreenActivity.this).getMvUser() != null) {
                if (User.getCurrentUser(SplashScreenActivity.this).getMvUser().getRoll() == null
                        || TextUtils.isEmpty(User.getCurrentUser(SplashScreenActivity.this).getMvUser().getRoll())) {
                    intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    User.clearUser();
                } else {
                    if (User.getCurrentUser(SplashScreenActivity.this).getMvUser().getGender() == null
                            || TextUtils.isEmpty(User.getCurrentUser(SplashScreenActivity.this).getMvUser().getGender())) {
                        intent = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
                        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
                        User.clearUser();
                    } else {
                        intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    }
                }
            } else {
                intent = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
                intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
                User.clearUser();
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }, SPLASH_TIME_OUT);
    }
}