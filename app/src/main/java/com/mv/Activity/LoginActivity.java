package com.mv.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.SliderAdapter;
import com.mv.BuildConfig;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Logger;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLoginBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LANGUAGE = "language";
    private static int currentPage = 0;
    private static final String[] XMEN = {"slider1", "slider2", "slider3", "slider4"};

    private User user;
    private String data;
    private Runnable Update;
    private Timer swipeTimer = null;
    private AlertDialog dialog = null;

    private ActivityLoginBinding binding;
    private PreferenceHelper preferenceHelper;
    private BroadcastReceiver mIntentReceiver;
    private CountDownTimer yourCountDownTimer;
    private boolean doubleBackToExitPressedOnce = false;

    private ArrayList<String> XMENArray = new ArrayList<>();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = new User();

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setActivity(this);

        initViews();
    }

    public void onLoginClick() {
        if (binding.edtUsername.isShown() && isValidate(binding.edtUsername, 10, getString(R.string.mobile_no))) {
            if (Utills.isConnected(this)) {
                loginToSalesforce();
            } else {
                Utills.showInternetPopUp(this);
            }
        } else if (binding.edtOtp.isShown() && isValidate(binding.edtOtp, 6, getString(R.string.password))) {

            if (user.getMvUser().getPassword() != null) {

                if (user.getMvUser().getPassword().trim().equals(binding.edtOtp.getText().toString().trim())) {
                    preferenceHelper.insertString(PreferenceHelper.UserData, data);
                    yourCountDownTimer.cancel();
                    if (user.getMvUser().getRoll() != null && !(TextUtils.isEmpty(user.getMvUser().getRoll()))) {
                        Utills.showToast(getString(R.string.login_successful), LoginActivity.this);
                        Intent intent;
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    } else {
                        Intent intent;
                        intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                } else {
                    binding.edtOtp.setError(getString(R.string.check_otp));
                }
            } else {
                Utills.showToast(getString(R.string.error_no_internet), LoginActivity.this);
            }
        }
    }

    public void onResendOtpClick() {
        if (Utills.isConnected(this)) {
            binding.tvResendOtp.setVisibility(View.GONE);
            loginToSalesforce();
        } else {
            Utills.showInternetPopUp(this);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void loginToSalesforce() {
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.loginSalesforce(BuildConfig.LOGIN_URL, BuildConfig.USERNAME, BuildConfig.PASSWORD,
                BuildConfig.CLIENT_SECRET, BuildConfig.CLIENT_ID, Constants.GRANT_TYPE,
                Constants.RESPONSE_TYPE).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        data = response.body().string();
                        if (data.length() > 0) {
                            JSONObject obj = new JSONObject(data);
                            String access_token = obj.getString("access_token");
                            String instance_url = obj.getString("instance_url");
                            String id = obj.getString("id");
                            String str_id = id.substring(id.lastIndexOf("/") + 1, id.length());

                            Log.e("$$$$$$$$$$", str_id);
                            preferenceHelper.insertString(PreferenceHelper.AccessToken, access_token);
                            preferenceHelper.insertString(PreferenceHelper.InstanceUrl, instance_url);
                            preferenceHelper.insertString(PreferenceHelper.SalesforceUserId, str_id);
                            preferenceHelper.insertString(PreferenceHelper.SalesforceUsername, BuildConfig.USERNAME);
                            preferenceHelper.insertString(PreferenceHelper.SalesforcePassword, BuildConfig.PASSWORD);

                            if (Utills.isConnected(LoginActivity.this)) {
                                getLoginOTP();
                            } else {
                                Utills.showInternetPopUp(getApplicationContext());
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("$$$$$$$$$$", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Log.e("error", t.getLocalizedMessage());
                Logger.doToast(getString(R.string.error_no_internet), LoginActivity.this);
                binding.tvTimer.setVisibility(View.GONE);
                binding.tvResendOtp.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showDialog() {
        preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_ENGLISH);
        final String[] items = {"English", "मराठी", "हिंदी "};

        int checkId = 0;
        if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_MARATHI)) {
            checkId = 1;
        } else if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_HINDI)) {
            checkId = 2;
        }

        dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_lang))
                .setCancelable(false)
                .setSingleChoiceItems(items, checkId, (dialogInterface, i) -> {
                })
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                            .putBoolean("firstrun", false).apply();

                    ListView lw = ((AlertDialog) dialog).getListView();
                    if (lw.getCheckedItemPosition() == 0) {
                        LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_ENGLISH);
                        preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_ENGLISH);
                    } else if (lw.getCheckedItemPosition() == 1) {
                        LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_MARATHI);
                        preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_MARATHI);
                    } else {
                        LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_HINDI);
                        preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_HINDI);
                    }

                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                }).create();

        dialog.show();
    }

    private void getLoginOTP() {

        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetLoginOTP_url + "?mobileNo=" + binding.edtUsername.getText().toString().trim()
                + "&notificationId=" + preferenceHelper.getString(PreferenceHelper.TOKEN)
                + "&PhoneId=" + Utills.getDeviceId(LoginActivity.this);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        data = response.body().string();
                        if (data.length() > 0) {
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            user = gson.fromJson(data, User.class);
                            Log.e("otp", user.getMvUser().getPassword());

                            if (user.getMvUser().getPhoneId() != null
                                    && user.getMvUser().getPhoneId().equalsIgnoreCase(Utills.getDeviceId(LoginActivity.this))) {
                                preferenceHelper.insertString(PreferenceHelper.UserData, data);
                                Utills.showToast(getString(R.string.login_successful), LoginActivity.this);

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                finish();
                            } else {
                                slideOut(binding.edtOtp, binding.edtUsername, getString(R.string.msg_manual_otp));
                                setTimer();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                binding.tvTimer.setVisibility(View.GONE);
                binding.tvResendOtp.setVisibility(View.VISIBLE);
                Utills.showToast(getString(R.string.error_no_internet), LoginActivity.this);
            }
        });
    }

    private void setTimer() {
        binding.tvTimer.setVisibility(View.VISIBLE);
        yourCountDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                binding.tvTimer.setText(getString(R.string.seconds_remaining) + ": " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                binding.tvTimer.setVisibility(View.GONE);
                binding.tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        if (binding.edtOtp.isShown()) {
            binding.edtOtp.setText("");
            slideOut(binding.edtUsername, binding.edtOtp, getString(R.string.msg_enter_mobile));
            if (yourCountDownTimer != null)
                yourCountDownTimer.cancel();
            binding.tvTimer.setVisibility(View.GONE);
            binding.tvResendOtp.setVisibility(View.GONE);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();

                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Runtime.getRuntime().gc();
                startActivity(startMain);
                System.exit(0);

                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.back_string), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
        if (firstrun && dialog == null) {
            showDialog();
        }

        IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("get_msg");

                //Process the sms format and extract body &amp; phoneNumber
                msg = msg.replace("\n", "");
                String body = msg.substring(msg.lastIndexOf(":") + 1, msg.length());

                Log.d("OTP", body);
                binding.edtOtp.setText(body);
                if (yourCountDownTimer != null)
                    yourCountDownTimer.cancel();
                binding.tvTimer.setVisibility(View.GONE);
                binding.tvResendOtp.setVisibility(View.GONE);

                //Add it to the list or do whatever you wish to
            }
        };
        this.registerReceiver(mIntentReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mIntentReceiver);
    }

    private boolean isValidate(EditText view, int charlimit, String errorMEssage) {
        String msg = "";
        if (view.getText().toString().trim().length() == 0) {
            msg = getString(R.string.please_enter) + " " + errorMEssage;
        } else if (view.getText().toString().trim().length() != charlimit) {
            msg = getString(R.string.please_enter) + " " + charlimit + " " + getString(R.string.digit) + " " + errorMEssage;
        }

        if (TextUtils.isEmpty(msg)) {
            return true;
        }

        if (charlimit == 10) {
            binding.edtUsername.setError(msg);
        } else {
            binding.edtOtp.setError(msg);
        }
        return false;
    }

    public void onForgotClick() {
        Intent intent;
        intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void initViews() {
        setActionbar();
        Utills.setupUI(findViewById(R.id.layout_main), this);

        binding.txtForgotPassword.setText(Html.fromHtml("<u>Forgot your Password ?</u>"));
        binding.tvUser.setFactory(mFactory);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        binding.tvUser.setInAnimation(in);
        binding.tvUser.setOutAnimation(out);

        binding.tvUser.setText(getString(R.string.msg_enter_mobile));
        preferenceHelper = new PreferenceHelper(this);

        initSlider();
    }

    private void initSlider() {
        Collections.addAll(XMENArray, XMEN);

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new SliderAdapter(LoginActivity.this, XMENArray));

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        Update = () -> {
            if (currentPage == XMEN.length) {
                currentPage = 0;
            }
            mPager.setCurrentItem(currentPage++, true);
        };

        if (swipeTimer == null) {
            swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, 2500, 2500);
        }
    }

    private void setActionbar() {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Login");

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();

                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    Runtime.getRuntime().gc();
                    startActivity(startMain);
                    System.exit(0);

                    finish();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getString(R.string.backExit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                break;
        }
    }

    private void slideOut(final View viewSlideIn, final View viewSlideOut, final String msg) {
        Animation animShow = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        viewSlideOut.startAnimation(animShow);

        animShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewSlideOut.setVisibility(View.GONE);
                viewSlideIn.setFocusable(true);

                slideIn(viewSlideIn, viewSlideOut, msg);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void slideIn(final View viewSlideIn, View viewSlideOut, final String msg) {
        Animation animShow = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        viewSlideIn.startAnimation(animShow);

        animShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.tvUser.setText(msg);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewSlideIn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private ViewSwitcher.ViewFactory mFactory = () -> {
        // Create a new TextView
        TextView t = new TextView(getApplicationContext());
        t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
        t.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
        return t;
    };

    @Override
    protected void onStop() {
        super.onStop();

        if (swipeTimer != null) {
            swipeTimer.cancel();
            swipeTimer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (swipeTimer == null) {
            swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, 2500, 2500);
        }
    }
}
