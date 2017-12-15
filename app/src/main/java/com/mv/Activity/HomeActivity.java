package com.mv.Activity;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kobakei.ratethisapp.RateThisApp;
import com.mv.Fragment.CommunityHomeFragment;
import com.mv.Fragment.GroupsFragment;
import com.mv.Fragment.IndicatorListFragmet;
import com.mv.Fragment.ProgrammeManagmentFragment;
import com.mv.Fragment.TeamManagementFragment;
import com.mv.Fragment.TrainingCalender;
import com.mv.Fragment.TrainingFragment;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.ForceUpdateChecker;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityHome1Binding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener, ForceUpdateChecker.OnUpdateNeededListener {


    private ImageView img_back, img_list, img_logout, img_lang;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityHome1Binding binding;

    private PreferenceHelper preferenceHelper;
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_UKRAINIAN = "mr";
    public static final String LANGUAGE = "language";
    ViewPagerAdapter adapter;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home1);
        binding.setActivity(this);
        preferenceHelper = new PreferenceHelper(this);
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(User.getCurrentUser(getApplicationContext()).getRoll().equals("TC")){
            Utills.scheduleJob(getApplicationContext());
        }
                if (User.getCurrentUser(getApplicationContext()).getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getIsApproved().equalsIgnoreCase("false")) {
            if (Utills.isConnected(this)) {
                getUserData();

            }
            else
                initViews();
        } else
            initViews();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
       // getAddress();

        Intent intent = new Intent(this, LocationService.class);
        // add infos for the service which file to download and where to store
        startService(intent);
    }

    private void initViews() {
        Intent receivedIntent = getIntent();

        setActionbar(getString(R.string.app_name));

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
        /*tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.broadcast)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.community)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.programme_management)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.training_content)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.team_management)));
        if (User.getCurrentUser(getApplicationContext()).getRoll().equals("TC"))
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.indicator)));
*/
        //      tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager);


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

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        List<String> allTab = new ArrayList<>();
        adapter.clearFrag();

        if (User.getCurrentUser(getApplicationContext()).getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getIsApproved().equalsIgnoreCase("false")) {
            allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getTabNameNoteApproved().split(";")));
            ;
            if (allTab.contains("Broadcast"))
                adapter.addFrag(new CommunityHomeFragment(), getString(R.string.broadcast));
            if (allTab.contains("My Community"))
                adapter.addFrag(new GroupsFragment(), getString(R.string.community));
            if (allTab.contains("Programme Management"))
                adapter.addFrag(new ProgrammeManagmentFragment(), getString(R.string.programme_management));
            if (allTab.contains("Training Content"))
                adapter.addFrag(new TrainingFragment(), getString(R.string.training_content));
            if (allTab.contains("Team Management"))
                adapter.addFrag(new TeamManagementFragment(), getString(R.string.team_management));
            if (allTab.contains("My Reports"))
                adapter.addFrag(new IndicatorListFragmet(), getString(R.string.indicator));
            if(allTab.contains("My Calendar"))
                adapter.addFrag(new TrainingCalender(), getString(R.string.training_calendar));
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);

            showApprovedDilaog();
        } else {

            allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getTabNameApproved().split(";")));
            ;
            if (allTab.contains("Broadcast"))
                adapter.addFrag(new CommunityHomeFragment(), getString(R.string.broadcast));
            if (allTab.contains("My Community"))
                adapter.addFrag(new GroupsFragment(), getString(R.string.community));
            if (allTab.contains("Programme Management"))
                adapter.addFrag(new ProgrammeManagmentFragment(), getString(R.string.programme_management));
            if (allTab.contains("Training Content"))
                adapter.addFrag(new TrainingFragment(), getString(R.string.training_content));
            if (allTab.contains("Team Management"))
                adapter.addFrag(new TeamManagementFragment(), getString(R.string.team_management));
            if (allTab.contains("My Reports"))
                adapter.addFrag(new IndicatorListFragmet(), getString(R.string.indicator));
            if(allTab.contains("My Calendar"))
                adapter.addFrag(new TrainingCalender(), getString(R.string.training_calendar));
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        }


    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        public void clearFrag() {
            mFragmentList.clear();
            mFragmentTitleList.clear();
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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
            case  R.id.action_share:
                ShareApp();
                return true;
            case  R.id.action_rate:

                RateThisApp.showRateDialog(HomeActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
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

            //    if (User.getCurrentUser(getApplicationContext()).getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getIsApproved().equalsIgnoreCase("false")) {
            if (Utills.isConnected(this))
                getUserData();
            else
                initViews();
    /*        }
            else
                initViews();*/

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

    private void showApprovedDilaog() {
        String message="";
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        if(User.getCurrentUser(getApplicationContext()).getApproval_role()!=null){
          message =   "You are not approved yet." +  "\n " +"Your"+ " " +User.getCurrentUser(getApplicationContext()).getApproval_role() + " "+ "will approve you.";
        }else {
            message = "You are not approved yet." ;
        }
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button

        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                //initViews();
             /*   finish();
                sendLogOutRequest();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);*/
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

    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }


    private void getUserData() {

        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getUserData?userId=" + User.getCurrentUser(getApplicationContext()).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    String data = response.body().string();
                    preferenceHelper.insertString(PreferenceHelper.UserData, data);
                    User.clearUser();
                    initViews();

                } catch (IOException e) {
                    e.printStackTrace();
                }
             /*   try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListState.clear();
                    mListState.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListState.add(jsonArray.getString(i));
                    }
                    state_adapter.notifyDataSetChanged();
                    if (!isAdd && !isStateSet) {
                        isStateSet = true;
                        for (int i = 0; i < mListState.size(); i++) {
                            if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getApprovedUserData())) {
                                binding.spinnerState.setSelection(i);
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void ShareApp(){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Mulyavardhan 2.0");
            String shareurl = "\nLet me recommend you this application\n\n";
            shareurl = shareurl + "https://play.google.com/store/apps/details?id=com.mv&hl=en \n\n";
            i.putExtra(Intent.EXTRA_TEXT, shareurl);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void getAddress() {
        mFusedLocationClient.getLastLocation()

                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.e("location","null");
                            return;
                        }

                        mLastLocation = location;
                        Log.e("lat", String.valueOf(mLastLocation.getLatitude()));
                        Log.e("long", String.valueOf(mLastLocation.getLongitude()));
                        Toast.makeText(getApplicationContext(),"latitude" +location.getLatitude() +"longitude" +location.getLongitude(),Toast.LENGTH_SHORT).show();
                        if (!Geocoder.isPresent()) {
                            return;
                        }

                        // If the user pressed the fetch address button before we had the location,
                        // this will be set to true indicating that we should kick off the intent
                        // service after fetching the location.
                      /*  if (mAddressRequested) {
                            startIntentService();
                        }*/
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       // Log.w(TAG, "getLastLocation:onFailure", e);
                        Log.e("fail","unable to connect");
                    }
                });


    }
}
