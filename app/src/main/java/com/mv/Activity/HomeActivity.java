package com.mv.Activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kobakei.ratethisapp.RateThisApp;
import com.mv.ActivityMenu.CommunityHomeFragment;
import com.mv.ActivityMenu.GroupsFragment;
import com.mv.ActivityMenu.IndicatorListFragmet;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.ActivityMenu.TeamManagementFragment;
import com.mv.ActivityMenu.ThetSavandFragment;
import com.mv.ActivityMenu.TrainingCalender;
import com.mv.Adapter.HomeAdapter;
import com.mv.Model.HomeModel;
import com.mv.Model.LocationModel;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener, ForceUpdateChecker.OnUpdateNeededListener, NavigationView.OnNavigationItemSelectedListener {


    private ImageView img_back, img_list, img_logout, img_lang;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private AlertDialog alertDialogApproved;
    private ActivityHome1Binding binding;
    private PreferenceHelper preferenceHelper;

    private AlertDialog alertLocationDialog = null;
    //  private ViewPagerAdapter adapter;
    //   private TabLayout tabLayout;
    //  private ViewPager viewPager;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    Date date;
    int LocatonFlag;
    HomeAdapter mAdapter;
    ArrayList<HomeModel> menulist;
    ActionBar actionBar;
    RecyclerView recyclerView;
    ArrayList<String> menuListName;
    ImageView iv_home_animate, iv_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home1);
        binding.setActivity(this);
        preferenceHelper = new PreferenceHelper(this);
        alertDialogApproved = new AlertDialog.Builder(this).create();
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        ///setActionbar(getString(R.string.app_name));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        TextView versionName = (TextView) headerLayout.findViewById(R.id.versionName);
        versionName.setText("Version is : " + getAppVersion());
        //    tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //  viewPager = (ViewPager) findViewById(R.id.pager);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        date = new Date(System.currentTimeMillis());


        if (User.getCurrentUser(HomeActivity.this).getRolePermssion().getIsLocationTrackingAllow__c().equalsIgnoreCase("true")) {
            if (User.getCurrentUser(HomeActivity.this).getMvUser().getIsApproved() != null && User.getCurrentUser(HomeActivity.this).getMvUser().getIsApproved().equalsIgnoreCase("true")) {


                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // LocationPopup();
                    LocationGPSDialog();
                    LocatonFlag = 0;

                } else {
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        // Utills.scheduleJob(getApplicationContext());
                        getAddress();

                       /* SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                        try {
                            Date CURRENTDATE = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

                            long APICALLDATE = preferenceHelper.getLong(PreferenceHelper.APICALLTIME);
                            long different = CURRENTDATE.getTime() - APICALLDATE;
                            long hrs = (int) ((different / (1000 * 60 * 60)));

                            // getAddress();
                            if (hrs >= 5) {
                                getAddress();
                            }*//*else {
                           // Utills.scheduleJob(getApplicationContext());
                          Utills.showToast("less than 5",HomeActivity.this);
                        }*//*


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
*/

                    } else {
                        if (LocatonFlag == 0) {
                            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                getAddress();
                            }
                        }
                    }
                }
            }
        }
        initViews();
        if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {
            if (Utills.isConnected(this))
                getUserData();
        }


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationTrackingAllow__c() != null
                && User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationTrackingAllow__c().equals("true")) {
            if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("true")) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //LocationPopup();
                    LocationGPSDialog();
                    LocatonFlag = 0;

                } else {
                    if (alertLocationDialog != null && alertLocationDialog.isShowing())
                        alertLocationDialog.dismiss();
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        // Utills.scheduleJob(getApplicationContext());
                        getAddress();

                       /* SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                        try {
                            Date CURRENTDATE = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

                            long APICALLDATE = preferenceHelper.getLong(PreferenceHelper.APICALLTIME);
                            long different = CURRENTDATE.getTime() - APICALLDATE;
                            long hrs = (int) ((different / (1000 * 60 * 60)));

                            // getAddress();
                            if (hrs >= 5) {
                                getAddress();
                            }*//*else {
                           // Utills.scheduleJob(getApplicationContext());
                          Utills.showToast("less than 5",HomeActivity.this);
                        }*//*


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
*/

                    } else {
                        if (LocatonFlag == 0) {
                            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                getAddress();
                            }
                        }
                    }
                }
            }
        }
        if (User.getCurrentUser(this).getMvUser().getUserMobileAppVersion() != null &&
                User.getCurrentUser(this).getMvUser().getUserMobileAppVersion().equalsIgnoreCase(getAppVersion()) &&
                User.getCurrentUser(this).getMvUser().getPhoneId() != null &&
                User.getCurrentUser(this).getMvUser().getPhoneId().equalsIgnoreCase(Utills.getDeviceId(HomeActivity.this))
                ) {

        } else {
            if (Utills.isConnected(this)) {
                User.getCurrentUser(this).getMvUser().setPhoneId(Utills.getDeviceId(HomeActivity.this));
                User.getCurrentUser(this).getMvUser().setUserMobileAppVersion(getAppVersion());
                sendData();
            } else {
                Utills.showToast(getString(R.string.error_no_internet), this);
            }
        }

        Intent intent = new Intent(this, LocationService.class);
        // add infos for the service which file to download and where to store
        intent.putExtra(Constants.State, User.getCurrentUser(getApplicationContext()).getMvUser().getState());
        intent.putExtra(Constants.DISTRICT, User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
        startService(intent);
    }

    private void sendData() {

        JSONObject jsonObject1 = new JSONObject();
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(User.getCurrentUser(this).getMvUser());
            JSONObject jsonObject2 = new JSONObject(json);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObjectAttachment = new JSONObject();
            jsonObject1.put("user", jsonObject2);
            jsonObject1.put("attachments", jsonObjectAttachment);
            jsonArray.put(jsonObject1);
            jsonObject.put("listVisitsData", jsonArray);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.MTRegisterUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {

                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data != null && data.length() > 0) {
                                  /*  JSONObject object = new JSONObject(data);
                                    JSONArray array = object.getJSONArray("Records");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object1 = array.getJSONObject(i);
                                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                        try {
                                            if (!isAdd) {
                                                AppDatabase.getAppDatabase(RegistrationActivity.this).userDao().clearTableCommunity();
                                            }
                                            preferenceHelper.insertString(PreferenceHelper.UserData, object1.toString());
                                            preferenceHelper.insertString(PreferenceHelper.UserRole, user.getMvUser().getRoll());
                                            Utills.showToast("Registration Successful...", RegistrationActivity.this);
                                            User.clearUser();
                                            setResult(RESULT_OK);
                                            finish();
                                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
*/
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                preferenceHelper.insertString(PreferenceHelper.UserData, data);
                                User.clearUser();
                            }
                        }
                            /*JSONObject response1 = new JSONObject(response.body().string());
                            if (response1.getBoolean("success")) {
                                // user.setId(response1.getString("id"));
                            } else {
                                Utills.showToast(response1.getString("Message"), RegistrationActivity.this);
                            }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utills.showToast(getString(R.string.error_something_went_wrong), HomeActivity.this);
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), HomeActivity.this);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getAppVersion() {
        String result = "";

        try {
            result = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TAG", e.getMessage());
        }

        return result;
    }

    private void initViews() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_home_animate = (ImageView) findViewById(R.id.iv_home_animate);
        List<String> allTab = new ArrayList<>();
        menulist = new ArrayList<>();

        if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {
            if (!User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameNoteApproved().equals(""))
                allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameNoteApproved().split(";")));
            showApprovedDilaog();
        } else {
            if (!User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameApproved().equals(""))
                allTab = Arrays.asList(getColumnIdex(User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameApproved().split(";")));

        }
        menuListName = new ArrayList<>();
        menuListName.add(Constants.Thet_Sanvad);
        menuListName.add(Constants.Broadcast);
        menuListName.add(Constants.My_Community);
        menuListName.add(Constants.Programme_Management);
        menuListName.add(Constants.Training_Content);
        menuListName.add(Constants.Team_Management);
        menuListName.add(Constants.My_Reports);
        menuListName.add(Constants.My_Calendar);
        menuListName.add(Constants.HR_MODULE);
        menuListName.add(Constants.Account_Section);
        menuListName.add(Constants.Asset_management);
        menuListName.add(Constants.Attendance);
        //for loop for adding accessible tab
        for (int i = 0; i < allTab.size(); i++) {
            if (checkList(allTab, i, true).getDestination() != null)
                menulist.add(checkList(allTab, i, true));
        }
//for loop for adding non accessible tab
        for (int i = 0; i < menuListName.size(); i++) {
            if (!allTab.contains(menuListName.get(i))) {
                if (checkList(menuListName, i, false).getDestination() != null)
                    menulist.add(checkList(menuListName, i, false));
            }
        }

        mAdapter = new HomeAdapter(menulist, HomeActivity.this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        Animation textAnimation = (AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink));
        iv_logo.startAnimation(textAnimation);


        iv_home_animate.setBackgroundResource(R.drawable.home_progress);

        AnimationDrawable rocketAnimation = (AnimationDrawable) iv_home_animate.getBackground();
        rocketAnimation = (AnimationDrawable) iv_home_animate.getBackground();
        rocketAnimation.start();

        mLayoutManager.setAutoMeasureEnabled(true);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        recyclerView.setLayoutManager(mLayoutManager);
        //binding.recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }


    }

    public HomeModel checkList(List<String> allTab, int i, Boolean isAccessible) {
        HomeModel homeModel = new HomeModel();
        homeModel.setAccessible(isAccessible);
        if (allTab.get(i).equals(Constants.Thet_Sanvad)) {
            homeModel.setMenuName(getString(R.string.thet_savnd));
            homeModel.setMenuIcon(R.drawable.ic_thet_sanvad);
            homeModel.setDestination(ThetSavandFragment.class);
        } else if (allTab.get(i).equals(Constants.Broadcast)) {
            homeModel.setMenuName(getString(R.string.broadcast));
            homeModel.setMenuIcon(R.drawable.ic_broadcast);
            homeModel.setDestination(CommunityHomeFragment.class);
        } else if (allTab.get(i).equals(Constants.My_Community)) {
            homeModel.setMenuName(getString(R.string.community));
            homeModel.setMenuIcon(R.drawable.ic_community);
            homeModel.setDestination(GroupsFragment.class);
        } else if (allTab.get(i).equals(Constants.Programme_Management)) {
            homeModel.setMenuName(getString(R.string.programme_management));
            homeModel.setMenuIcon(R.drawable.ic_program_mangement);
            homeModel.setDestination(ProgrammeManagmentFragment.class);
        } else if (allTab.get(i).equals(Constants.Training_Content)) {
            homeModel.setMenuName(getString(R.string.training_content));
            homeModel.setMenuIcon(R.drawable.ic_traing_content);
            homeModel.setDestination(ExpandableListActivity.class);
        } else if (allTab.get(i).equals(Constants.Team_Management)) {
            homeModel.setMenuName(getString(R.string.team_management));
            homeModel.setMenuIcon(R.drawable.ic_team_management);
            homeModel.setDestination(TeamManagementFragment.class);
        } else if (allTab.get(i).equals(Constants.My_Reports)) {
            homeModel.setMenuName(getString(R.string.indicator));
            homeModel.setMenuIcon(R.drawable.ic_reports);
            homeModel.setDestination(IndicatorListFragmet.class);
        } else if (allTab.get(i).equals(Constants.My_Calendar)) {
            homeModel.setMenuName(getString(R.string.training_calendar));
            homeModel.setMenuIcon(R.drawable.ic_calender);
            homeModel.setDestination(TrainingCalender.class);
        } else if (allTab.get(i).equals(Constants.Asset_management)) {
            homeModel.setMenuName(getString(R.string.asset_management));
            homeModel.setMenuIcon(R.drawable.ic_asset);
            homeModel.setDestination(AssetAllocatedListActivity.class);
        } else if (allTab.get(i).equals(Constants.HR_MODULE)) {
            homeModel.setMenuName(getString(R.string.leave));
            homeModel.setMenuIcon(R.drawable.ic_hr);
            homeModel.setDestination(LeaveApprovalActivity.class);
        } else if (allTab.get(i).equals(Constants.Account_Section)) {
            homeModel.setMenuName(getString(R.string.account_section));
            homeModel.setMenuIcon(R.drawable.ic_account);
            homeModel.setDestination(AccountSectionActivity.class);
        } else if (allTab.get(i).equals(Constants.Attendance)) {
            homeModel.setMenuName(getString(R.string.attendance));
            homeModel.setMenuIcon(R.drawable.ic_about_us);
            homeModel.setDestination(AttendanceActivity.class);
        }
        return homeModel;
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

                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertLocationDialog != null)
            alertLocationDialog.dismiss();

        if (alertDialogApproved != null)
            alertDialogApproved.dismiss();

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
            case R.id.action_share:
                ShareApp();
                return true;
            case R.id.action_rate:
                RateThisApp.showRateDialog(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                return true;
            case R.id.action_add_school:
                Intent openClass = new Intent(HomeActivity.this, AddSchoolActivity.class);
                startActivity(openClass);
                //  startActivity(openClass);

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

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ISROLECHANGE && resultCode == RESULT_OK) {

            if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {
            } else {
                if (alertDialogApproved != null && alertDialogApproved.isShowing())
                    alertDialogApproved.dismiss();

            }
            initViews();

        }
    }

    private void showNotificationDialog() {

        final String[] items = {"On", "Off"};
        final ArrayList seletedItems = new ArrayList();
        int checkedItem = 0;
        if (preferenceHelper.getBoolean(PreferenceHelper.NOTIFICATION)) {
            checkedItem = 0;
        } else {
            checkedItem = 1;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.notification))
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
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

        final String[] items = {"English", "मराठी", "हिंदी "};
        final ArrayList seletedItems = new ArrayList();

        int checkId = 0;
        if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_MARATHI)) {
            checkId = 1;
        } else if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_HINDI)) {
            checkId = 2;
        }


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_lang))
                .setSingleChoiceItems(items, checkId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
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

                    }

                }).create();
        dialog.setCancelable(true);
        dialog.show();
    }

    private void showLogoutPopUp() {

        if (AppDatabase.getAppDatabase(HomeActivity.this).userDao().getOfflineTaskCount(Constants.TASK_ANSWER, "true") == 0) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertDialog.setTitle(getString(R.string.app_name));

            // Setting Dialog Message
            alertDialog.setMessage(getString(R.string.logout_string));

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.logomulya);

            // Setting CANCEL Button
            alertDialog.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
                }
            });
            // Setting OK Button
            alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    sendLogOutRequest();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle(getString(R.string.cannotLogout));
            alertDialog.setMessage(getString(R.string.submitAllForms));

            alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
        }


    }

    private void showUpdateDataPopup() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.update_data_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                List<LocationModel> districts = AppDatabase.getAppDatabase(HomeActivity.this).userDao().getDistinctDistrict();
                AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearLocation();
                for (int i = 0; i < districts.size(); i++) {
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    // add infos for the service which file to download and where to store
                    intent.putExtra(Constants.State, districts.get(i).getState());
                    intent.putExtra(Constants.DISTRICT, districts.get(i).getDistrict());
                    startService(intent);
                }
                if (Utills.isConnected(HomeActivity.this))
                    getUserData();
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
                    + Constants.DoLogout_url + User.getCurrentUser(this).getMvUser().getId();

            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    preferenceHelper.clearPrefrences();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTableCommunity();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTableCotent();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearProcessTable();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearTaskContainer();
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearLocation();
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
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

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
        String message = "";
        if (alertDialogApproved.isShowing())
            alertDialogApproved.dismiss();

        // Setting Dialog Title
        alertDialogApproved.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        if (User.getCurrentUser(getApplicationContext()).getMvUser().getApproval_role() != null) {
            if (!User.getCurrentUser(getApplicationContext()).getMvUser().getApprover_Comment__c().equals(""))
                message = getString(R.string.approve_profile) + "\n" + User.getCurrentUser(getApplicationContext()).getMvUser().getApprover_Comment__c();
            else
                message = getString(R.string.approve_profile) + "\n" + User.getCurrentUser(getApplicationContext()).getMvUser().getApproval_role() + " " + getString(R.string.approve_profile2);

        } else {
            message = getString(R.string.approve_profile);
        }
        alertDialogApproved.setMessage(message);

        // Setting Icon to Dialog
        alertDialogApproved.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button

        // Setting OK Button
        alertDialogApproved.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialogApproved.dismiss();
                //initViews();
             /*   finish();
                sendLogOutRequest();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);*/
            }
        });

        // Showing Alert Message
        alertDialogApproved.show();
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
                + Constants.GetUserData_url + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response.isSuccess()) {
                        String data = response.body().string();
                        preferenceHelper.insertString(PreferenceHelper.UserData, data);
                        User.clearUser();
                        if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null && User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {
                        } else {
                            if (alertDialogApproved != null && alertDialogApproved.isShowing())
                                alertDialogApproved.dismiss();

                        }
                        initViews();

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void ShareApp() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Mulyavardhan 2.0");
            String shareurl = "\nLet me recommend you this application\n\n";
            shareurl = shareurl + "https://play.google.com/store/apps/details?id=com.mv&hl=en \n\n";
            i.putExtra(Intent.EXTRA_TEXT, shareurl);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAddress() {
        mFusedLocationClient.getLastLocation()

                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {

                            return;
                        }

                        mLastLocation = location;
                        GetMapParameters(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
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
                        Log.e("fail", "unable to connect");
                    }
                });


    }


    private void LocationGPSDialog() {
        if (alertLocationDialog == null) {
            alertLocationDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertLocationDialog.setTitle(getString(R.string.gps_settings));

            // Setting Dialog Message
            alertLocationDialog.setMessage(getString(R.string.no_gps));

            // Setting Icon to Dialog
            alertLocationDialog.setIcon(R.drawable.logomulya);

            // Setting CANCEL Button
       /* alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });*/
            // Setting OK Button
            alertLocationDialog.setButton(getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
            });


            // Showing Alert Message
            alertLocationDialog.show();
            alertLocationDialog.setCancelable(false);
        }

    }

    private void GetMapParameters(String latitude, String longitude) {
        if (!Utills.isConnected(this))
            return;
        try {

            preferenceHelper = new PreferenceHelper(getApplicationContext());

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("lat", latitude);
            jsonObject.put("lon", longitude);
            jsonObject.put("id", User.getCurrentUser(this).getMvUser().getId());
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.MapParametersUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data != null && data.length() > 0) {
                                JSONObject jsonObject = new JSONObject(data);
                                String statusofmap = jsonObject.getString("status");
                                String message = jsonObject.getString("msg");

                                if (statusofmap.equals("Success")) {




/*
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
                                    Date APICALLDATE = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

                                    preferenceHelper.insetLong(PreferenceHelper.APICALLTIME,APICALLDATE.getTime());
*/


                                } else {
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    //   Toast.makeText(getApplicationContext(), R.string.error_something_went_wrong, Toast.LENGTH_LONG).show();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void CallUSDialog() {
        final String[] items = {getString(R.string.call_on_hangout), getString(R.string.call_on_landline)};

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name));
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                dialogInterface.dismiss();
                switch (position) {
                    case 0:
                        Uri uri = Uri.parse(User.getCurrentUser(getApplicationContext()).getAppConfig().getHangout_URL__c()); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                    case 1:
                        Intent dial = new Intent();
                        dial.setAction("android.intent.action.DIAL");
                        try {
                            dial.setData(Uri.parse("tel:" + User.getCurrentUser(getApplicationContext()).getAppConfig().getContact_No__c()));
                            startActivity(dial);
                        } catch (Exception e) {
                            Log.e("Calling", "" + e.getMessage());
                        }
                }

            }
        });

        dialog.show();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_lang) {
            showDialog();


        } else if (id == R.id.action_profile) {
            Intent intent;
            intent = new Intent(this, RegistrationActivity.class);
            intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
            startActivityForResult(intent, Constants.ISROLECHANGE);
        } else if (id == R.id.action_logout) {
            showLogoutPopUp();
        } else if (id == R.id.action_notification) {
            showNotificationDialog();
        } else if (id == R.id.action_share) {
            ShareApp();
        } else if (id == R.id.action_rate) {
            RateThisApp.showRateDialog(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);

        } else if (id == R.id.action_add_school) {
            String role = User.getCurrentUser(getApplicationContext()).getMvUser().getRoll();
            if ((User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationAllow__c().equals("true"))) {
                Intent openClass = new Intent(HomeActivity.this, AddSchoolActivity.class);
                startActivity(openClass);
            } else {
                Utills.showToast("You don't have access to add location", HomeActivity.this);
            }

        } else if (id == R.id.action_callus) {
            CallUSDialog();

        } else if (id == R.id.action_update_user_data) {

            showUpdateDataPopup();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}

