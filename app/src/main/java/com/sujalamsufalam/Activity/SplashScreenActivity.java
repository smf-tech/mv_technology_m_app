package com.sujalamsufalam.Activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.sujalamsufalam.BuildConfig;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.Logger;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.databinding.ActivityMainBinding;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceHelper preference;
    private static int SPLASH_TIME_OUT = 2000;
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_MARATHI = "mr";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);
        preference = new PreferenceHelper(this);
        // TODO Remove this code after build
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


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
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

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        }, SPLASH_TIME_OUT);
    }


    private void loginToSalesforce() {
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.loginSalesforce(BuildConfig.LOGIN_URL, BuildConfig.USERNAME, BuildConfig.PASSWORD, BuildConfig.CLIENT_SECRET
                , BuildConfig.CLIENT_ID, Constants.GRANT_TYPE, Constants.RESPONSE_TYPE).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    String access_token = obj.getString("access_token");
                    String instance_url = obj.getString("instance_url");
                    String id = obj.getString("id");
                    String str_id = id.substring(id.lastIndexOf("/") + 1, id.length());
                    Log.e("$$$$$$$$$$", str_id);
                    preference.insertString(PreferenceHelper.AccessToken, access_token);
                    preference.insertString(PreferenceHelper.InstanceUrl, instance_url);
                    preference.insertString(PreferenceHelper.SalesforceUserId, str_id);
                    preference.insertString(PreferenceHelper.SalesforceUsername, BuildConfig.USERNAME);
                    preference.insertString(PreferenceHelper.SalesforcePassword, BuildConfig.PASSWORD);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent;
                            if (User.getCurrentUser(SplashScreenActivity.this).getMvUser().getRoll() == null
                                    || TextUtils.isEmpty(User.getCurrentUser(SplashScreenActivity.this).getMvUser().getRoll()))
                                intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                            else
                                intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                            startActivity(intent);

                        }
                    }, SPLASH_TIME_OUT);

                   /* */
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Logger.doToast(getString(R.string.error_something_went_wrong), SplashScreenActivity.this);
            }
        });
    }


}