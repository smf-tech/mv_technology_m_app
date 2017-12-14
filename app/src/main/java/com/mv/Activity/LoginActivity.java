package com.mv.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.SliderAdapter;
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
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLoginBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title, timer, resend;
    private RelativeLayout mToolBar;
    private PreferenceHelper preferenceHelper;
    private BroadcastReceiver mIntentReceiver;
    User user = new User();
    String msg = "";
    CountDownTimer yourCountDownTimer;
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] XMEN = {R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e};
    private ArrayList<Integer> XMENArray = new ArrayList<Integer>();
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_UKRAINIAN = "mr";
    public static final String LANGUAGE = "language";
    final Handler handler = new Handler();
    Timer swipeTimer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setActivity(this);
        initViews();
     /*   DownloadFile downloadFile = new DownloadFile(this);
        downloadFile.startDownload("http://mobileyougokidinformationdesk.com//denver123//videos//test0.zip","DownLoad1.zip");
*/    }

    public void onLoginClick() {
        // binding.tvUser.setText("(Enter the OTP below in case if we fail to detect the SMS automatically)");
        // slideOut(binding.edtOtp,binding.edtUsername);
        if (binding.edtUsername.isShown() && isValidate(binding.edtUsername, 10, getString(R.string.mobile_no))) {
            if (Utills.isConnected(this)) {
                slideOut(binding.edtOtp, binding.edtUsername, getString(R.string.msg_manual_otp));
                loginToSalesforce();
            } else {
                Utills.showInternetPopUp(this);
            }
        } else if (binding.edtOtp.isShown() && isValidate(binding.edtOtp, 6, "OTP")) {
            if (user.getPassword().trim().equals(binding.edtOtp.getText().toString().trim())) {
                yourCountDownTimer.cancel();
                if (user.getRoll() != null) {
                    Utills.showToast("Login Successful...", LoginActivity.this);
                    Intent intent;
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    finish();
                    preferenceHelper.insertString(PreferenceHelper.UserRole, user.getRoll());
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

        }
        //  getUserData();


       /* */
    }

    public void onResendOtpClick() {
        if (Utills.isConnected(this)) {
            binding.tvResendOtp.setVisibility(View.GONE);
            getLoginOTP();
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

        apiService.loginSalesforce(Constants.LOGIN_URL, Constants.USERNAME, Constants.PASSWORD, Constants.CLIENT_SECRET
                , Constants.CLIENT_ID, Constants.GRANT_TYPE, Constants.RESPONSE_TYPE).enqueue(new Callback<ResponseBody>() {
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
                    preferenceHelper.insertString(PreferenceHelper.AccessToken, access_token);
                    preferenceHelper.insertString(PreferenceHelper.InstanceUrl, instance_url);
                    preferenceHelper.insertString(PreferenceHelper.SalesforceUserId, str_id);
                    preferenceHelper.insertString(PreferenceHelper.SalesforceUsername, Constants.USERNAME);
                    preferenceHelper.insertString(PreferenceHelper.SalesforcePassword, Constants.PASSWORD);
                    if (Utills.isConnected(LoginActivity.this))
                        getLoginOTP();
                    else {
                        Utills.showInternetPopUp(getApplicationContext());
                    }

                   /* */
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Logger.doToast(getString(R.string.error_something_went_wrong), LoginActivity.this);
            }
        });
    }

    private void showDialog() {

        final String[] items = {"English", "मराठी"};


// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_lang))
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                      /*  Intent intent;
                            intent = new Intent(LoginActivity.this, LoginActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);*/
                        ListView lw = ((AlertDialog) dialog).getListView();

                        if (lw.getCheckedItemPosition() == 0) {
                            LocaleManager.setNewLocale(getApplicationContext(), LANGUAGE_ENGLISH);
                            preferenceHelper.insertString(LANGUAGE, LANGUAGE_ENGLISH);
                        } else {
                            LocaleManager.setNewLocale(getApplicationContext(), LANGUAGE_UKRAINIAN);
                            preferenceHelper.insertString(LANGUAGE, LANGUAGE_UKRAINIAN);
                        }
                        dialog.dismiss();
                        finish();
                        startActivity(getIntent());


                    }

                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void getLoginOTP() {
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getLoginOTP?mobileNo=" + binding.edtUsername.getText().toString().trim()
                + "&notificationId=" + preferenceHelper.getString(PreferenceHelper.TOKEN);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    String data = response.body().string();
                    preferenceHelper.insertString(PreferenceHelper.UserData, data);
                    user = gson.fromJson(data, User.class);
                    setTimer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), LoginActivity.this);
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

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (binding.edtOtp.isShown()) {

            slideOut(binding.edtUsername, binding.edtOtp, getString(R.string.msg_enter_mobile));
            if(yourCountDownTimer!=null)
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

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
        if (firstrun) {

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstrun", false)
                    .commit();
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

                String pNumber = msg.substring(0, msg.lastIndexOf(":"));
                Log.d("OTP", body);
                binding.edtOtp.setText(body);
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
        msg = "";
        if (view.getText().toString().trim().length() == 0) {
            msg = getString(R.string.please_enter) + "" + errorMEssage;
        } else if (view.getText().toString().trim().length() != charlimit) {
            msg = getString(R.string.please_enter) + " " + charlimit + getString(R.string.digit) + errorMEssage;
        }
        if (TextUtils.isEmpty(msg))
            return true;
        if (charlimit == 10)
            binding.edtUsername.setError(msg);
        else
            binding.edtOtp.setError(msg);
        //   Utills.showToast(msg, this);
        return false;
    }

    public void onForgotClick() {
        Intent intent;
        intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void initViews() {
        setActionbar("Login");
        Utills.setupUI(findViewById(R.id.layout_main), this);
        binding.txtForgotPassword.setText(Html.fromHtml("<u>Forgot your Password ?</u>"));
        binding.tvUser.setFactory(mFactory);
        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);
        binding.tvUser.setInAnimation(in);
        binding.tvUser.setOutAnimation(out);
        binding.tvUser.setText(getString(R.string.msg_enter_mobile));
        preferenceHelper = new PreferenceHelper(this);
        initSlider();
    }

    private void initSlider() {
        for (int i = 0; i < XMEN.length; i++)
            XMENArray.add(XMEN[i]);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new SliderAdapter(LoginActivity.this, XMENArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager

        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == XMEN.length) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };

        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2500, 2500);
    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
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

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
                break;
        }
    }

  /*  @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
*/

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

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(getApplicationContext());
            t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
            t.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
            return t;
        }
    };


}
