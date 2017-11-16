package com.mv.Activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mv.Adapter.PagerAdapter;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityHome1Binding;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView img_back, img_list, img_logout, img_lang;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityHome1Binding binding;
    private PreferenceHelper preferenceHelper;
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_UKRAINIAN = "mr";
    public static final String LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home1);
        binding.setActivity(this);
        initViews();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, LocationService.class);
        // add infos for the service which file to download and where to store
        startService(intent);
    }

    private void initViews() {
        Intent receivedIntent = getIntent();

        setActionbar(getString(R.string.home));
        preferenceHelper = new PreferenceHelper(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        int toolbarHeight = actionBarHeight;
        height = height - toolbarHeight;
        height = height - dpToPx(80);
        int textWidth = height / 3;
        //  binding.community.getLayoutParams().width = textWidth;
        // binding.content.getLayoutParams().width = textWidth;
        // binding.process.getLayoutParams().width = textWidth;


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.broadcast)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.community)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.programme_management)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.training_content)));
        if (User.getCurrentUser(getApplicationContext()).getRoll().equals("TC"))
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.indicator)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.team_management)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();
        //make sure it's an action and type we can handle
        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            viewPager.setCurrentItem(1);
            if (receivedType.startsWith("text/")) {
                //handle sent text
            } else if (receivedType.startsWith("image/")) {
                //handle sent image
                Constants.shareUri = (Uri) receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
            //content is being shared
        } else {
            //app has been launched directly, not from share list
            Constants.shareUri = null;
        }

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_lang:
                showDialog();
                return true;
            case R.id.action_profile:
                Intent intent;
                intent = new Intent(this, RegistrationActivity.class);
                intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
                startActivityForResult(intent, Constants.ISROLECHANGE);
                return true;
            case R.id.action_logout:
                showLogoutPopUp();
                return true;
            case R.id.action_notification:
                showNotificationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setActionbar(String Title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // actionBar.setTitle(Title);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.toolbar);
            View view = getSupportActionBar().getCustomView();
            toolbar_title = (TextView) view.findViewById(R.id.toolbar_title);
            toolbar_title.setText(Title);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setVisibility(View.GONE);
            img_back.setOnClickListener(this);
            img_logout = (ImageView) view.findViewById(R.id.img_logout);
            img_logout.setVisibility(View.GONE);
            img_logout.setOnClickListener(this);
            img_list = (ImageView) view.findViewById(R.id.img_list);
            img_lang = (ImageView) view.findViewById(R.id.img_lang);
            img_lang.setVisibility(View.GONE);
            img_lang.setOnClickListener(this);
            img_list.setImageResource(R.drawable.ic_account_circle_white_36dp);
            img_list.setVisibility(View.GONE);
            img_list.setOnClickListener(this);
        }
       /* mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.VISIBLE);
        img_logout.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_lang = (ImageView) findViewById(R.id.img_lang);
        img_lang.setVisibility(View.VISIBLE);
        img_lang.setOnClickListener(this);
        img_list.setImageResource(R.drawable.ic_account_circle_white_36dp);
        img_list.setVisibility(View.VISIBLE);
        img_list.setOnClickListener(this);*/

    }

   /* public void onCommunityClick() {
        Intent intent;
        intent = new Intent(HomeActivity.this, GroupsActivity.class);
        startActivity(intent);
    }

    public void onProcessClick() {
       *//* Intent intent;
        intent = new Intent(HomeActivity.this, WebViewActivity.class);
        intent.putExtra(Constants.URL, "http://dev-mulyavardhan.cs57.force.com/");
        intent.putExtra(Constants.TITLE, "New MT Training");
        startActivity(intent);*//*
        Intent intent;
        intent = new Intent(HomeActivity.this, ProgrammeManagmentActivity.class);
        startActivity(intent);
    }

    public void onContentClick() {
        Intent intent;
        String url = Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/" + 1 + "/story_html5.html";
        if (new File(url).exists()) {
            //btn_mv_trainings.setVisibility(View.VISIBLE);
            intent = new Intent(HomeActivity.this, TrainingActivity.class);
            startActivity(intent);
        } else {
            Utills.showToast("You don't have Training files...", this);
        }
    }*/

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
                Toast.makeText(this, getString(R.string.back_string), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
                break;
            case R.id.img_logout:
                showLogoutPopUp();
                break;
            case R.id.img_list:
                Intent intent;
                intent = new Intent(this, RegistrationActivity.class);
                intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
                startActivityForResult(intent, Constants.ISROLECHANGE);
                break;
            case R.id.img_lang:
                showDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ISROLECHANGE && resultCode == RESULT_OK) {
            initViews();
        }
    }

    private void showNotificationDialog() {

        final String[] items = {"On", "Off"};
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.notification))
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        if (lw.getCheckedItemPosition() == 0) {
                            preferenceHelper.insertBoolean(PreferenceHelper.NOTIFICATION, true);
                        } else {
                            preferenceHelper.insertBoolean(PreferenceHelper.NOTIFICATION, false);
                        }
                        dialog.dismiss();

                    }

                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showDialog() {

        final String[] items = {"English", "मराठी"};
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_lang))
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
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

    private void showLogoutPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.logout_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sendLogOutRequest();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void sendLogOutRequest() {
        if (Utills.isConnected(this)) {
            Utills.showProgressDialog(this);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/doLogout/" + User.getCurrentUser(this).getId();

            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    preferenceHelper.clearPrefrences();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTableCommunity();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTableCotent();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearProcessTable();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTaskContainer();
                    User.clearUser();
                    Intent startMain = new Intent(HomeActivity.this, LoginActivity.class);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startMain);
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), HomeActivity.this);
                }
            });
        } else {
            showPopUp();
        }
    }

    private void showPopUp() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
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
