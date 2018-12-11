package com.mv.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kobakei.ratethisapp.RateThisApp;
import com.mv.ActivityMenu.CommunityHomeFragment;
import com.mv.ActivityMenu.GroupsFragment;
import com.mv.ActivityMenu.MyReportActivity;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.ActivityMenu.TeamManagementFragment;
import com.mv.ActivityMenu.ThetSavandFragment;
import com.mv.ActivityMenu.TrainingCalender;
import com.mv.Adapter.HomeAdapter;
import com.mv.Model.Attendance;
import com.mv.Model.HolidayListModel;
import com.mv.Model.HomeModel;
import com.mv.Model.LeavesModel;
import com.mv.Model.LocationModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Service.SendAttendance;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener, NavigationView.OnNavigationItemSelectedListener {

    private TextView tvUnreadNotification;
    private AlertDialog alertDialogApproved;
    private AlertDialog alertLocationDialog;
    private PreferenceHelper preferenceHelper;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    private int LocationFlag;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("cycled", "onCreate: A");

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        ActivityHome1Binding binding = DataBindingUtil.setContentView(this, R.layout.activity_home1);
        binding.setActivity(this);

        preferenceHelper = new PreferenceHelper(this);
        alertDialogApproved = new AlertDialog.Builder(this).create();

        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

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
        TextView versionName = headerLayout.findViewById(R.id.versionName);
        versionName.setText(String.format("Version is : %s", getAppVersion()));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView ivBellNotification = (ImageView) findViewById(R.id.iv_bell_notification);
        tvUnreadNotification = (TextView) findViewById(R.id.tv_unread_notification);
        ivBellNotification.setOnClickListener(this);

        if (User.getCurrentUser(HomeActivity.this).getRolePermssion() != null &&
                User.getCurrentUser(HomeActivity.this).getRolePermssion().getIsLocationTrackingAllow__c() != null &&
                User.getCurrentUser(HomeActivity.this).getRolePermssion().getIsLocationTrackingAllow__c().equalsIgnoreCase("true")) {

            if (User.getCurrentUser(HomeActivity.this).getMvUser() != null &&
                    User.getCurrentUser(HomeActivity.this).getMvUser().getIsApproved() != null &&
                    User.getCurrentUser(HomeActivity.this).getMvUser().getIsApproved().equalsIgnoreCase("true")) {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (manager != null) {
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        LocationGPSDialog();
                        LocationFlag = 0;
                    } else {
                        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getAddress();
                        } else {
                            if (LocationFlag == 0) {
                                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    getAddress();
                                }
                            }
                        }
                    }
                }
            }
        }

        initViews();

        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null &&
                User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null &&
                User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {

            if (Utills.isConnected(this)) {
                getUserData();
            }
        }

        long deviceTime = System.currentTimeMillis();
        Log.i("deviceTime", deviceTime + "");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onStart() {
        Log.d("cycled", "onStart:A ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("cycled", "onResume: A");

        if (User.getCurrentUser(getApplicationContext()).getRolePermssion() != null &&
                User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationTrackingAllow__c() != null &&
                User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationTrackingAllow__c().equals("true")) {

            if (User.getCurrentUser(getApplicationContext()).getMvUser() != null &&
                    User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null &&
                    User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("true")) {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (manager != null) {
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        LocationGPSDialog();
                        LocationFlag = 0;
                    } else {
                        if (alertLocationDialog != null && alertLocationDialog.isShowing())
                            alertLocationDialog.dismiss();
                        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getAddress();
                        } else {
                            if (LocationFlag == 0) {
                                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    getAddress();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (User.getCurrentUser(this).getMvUser() != null) {
            if (User.getCurrentUser(this).getMvUser().getUserMobileAppVersion() == null ||
                    !User.getCurrentUser(this).getMvUser().getUserMobileAppVersion().equalsIgnoreCase(getAppVersion()) ||
                    User.getCurrentUser(this).getMvUser().getPhoneId() == null ||
                    !User.getCurrentUser(this).getMvUser().getPhoneId().equalsIgnoreCase(Utills.getDeviceId(HomeActivity.this))) {

                if (Utills.isConnected(this)) {
                    User.getCurrentUser(this).getMvUser().setPhoneId(Utills.getDeviceId(HomeActivity.this));
                    User.getCurrentUser(this).getMvUser().setUserMobileAppVersion(getAppVersion());
                    sendData();
                } else {
                    Utills.showToast(getString(R.string.error_no_internet), this);
                }
            }
        }

        if (AppDatabase.getAppDatabase(HomeActivity.this).userDao().getAllHolidayList().size() == 0) {
            getHolidayList();
        }

        // add infos for the service which file to download and where to store
        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null) {
            Intent intent = new Intent(this, LocationService.class);
            intent.putExtra(Constants.State, User.getCurrentUser(getApplicationContext()).getMvUser().getState());
            intent.putExtra(Constants.DISTRICT, User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
            startService(intent);
        }

        // Send offline attendance to server
        Attendance temp = AppDatabase.getAppDatabase(HomeActivity.this).userDao().getUnSynchAttendance();
        if (Utills.isConnected(HomeActivity.this)) {
            if (temp != null) {
                Intent intentt = new Intent(HomeActivity.this, SendAttendance.class);
                startService(intentt);
            }
            getAllLeaves();
        }

        int count = AppDatabase.getAppDatabase(this).userDao().getUnRearNotificationsCount("unread");
        tvUnreadNotification.setText("" + count);
        if (count > 0) {
            tvUnreadNotification.setVisibility(View.VISIBLE);
        }

        // new push notification is received
        BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.PUSH_NOTIFICATION.equals(intent.getAction())) {
                    int count = AppDatabase.getAppDatabase(HomeActivity.this).userDao().getUnRearNotificationsCount("unread");
                    tvUnreadNotification.setText("" + count);

                    if (count > 0) {
                        tvUnreadNotification.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_NOTIFICATION));
    }

    private void getHolidayList() {
        if (User.getCurrentUser(getApplicationContext()).getMvUser() == null) {
            return;
        }

        if (Utills.isConnected(HomeActivity.this)) {
            Utills.showProgressDialog(HomeActivity.this, "Loading Holidays", getString(R.string.progress_please_wait));
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(HomeActivity.this).create(ServiceRequest.class);

            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAllHolidays?userId="
                    + User.getCurrentUser(getApplicationContext()).getMvUser().getId();

            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data.length() > 0) {
                                JSONArray jsonArray = new JSONArray(data);
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                AppDatabase.getAppDatabase(HomeActivity.this).userDao().deleteHolidayList();

                                List<HolidayListModel> holidayListModels = Arrays.asList(gson.fromJson(jsonArray.toString(), HolidayListModel[].class));
                                AppDatabase.getAppDatabase(HomeActivity.this).userDao().insertAllHolidayList(holidayListModels);
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
                    Utills.hideProgressDialog();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("cycled", "onPause: A");
    }

    @Override
    protected void onStop() {
        Log.d("cycled", "onStop: A");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("cycled", "onDestroy: A");
        if (alertLocationDialog != null) {
            alertLocationDialog.dismiss();
        }

        if (alertDialogApproved != null) {
            alertDialogApproved.dismiss();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("cycled", "onRestart: A");
    }

    private void sendData() {
        JSONObject jsonObject1 = new JSONObject();
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(User.getCurrentUser(this).getMvUser());

            JSONObject jsonObject2 = new JSONObject(json);
            jsonObject1.put("user", jsonObject2);

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObjectAttachment = new JSONObject();
            jsonObject1.put("attachments", jsonObjectAttachment);
            jsonArray.put(jsonObject1);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("listVisitsData", jsonArray);

            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) +
                    Constants.MTRegisterUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data.length() > 0) {
                                preferenceHelper.insertString(PreferenceHelper.UserData, data);
                                User.clearUser();
                            }
                        }
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

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private void initViews() {
        ArrayList<String> menuListName = new ArrayList<>();
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

        List<String> allTabNotApprove = new ArrayList<>();
        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null &&
                !User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameNoteApproved().equals("")) {

            allTabNotApprove = Arrays.asList(getColumnIndex(
                    User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameNoteApproved().split(";")));
        }

        List<String> allTab = new ArrayList<>();
        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null &&
                !User.getCurrentUser(getApplicationContext()).getMvUser().getTabNameApproved().equals("")) {

            allTab = Arrays.asList(getColumnIndex(User.getCurrentUser(
                    getApplicationContext()).getMvUser().getTabNameApproved().split(";")));
        }

        ArrayList<HomeModel> menuList = new ArrayList<>();
        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null &&
                User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() != null &&
                User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {

            showApprovedDialog();

            for (int i = 0; i < allTabNotApprove.size(); i++) {
                if (checkList(allTabNotApprove, i, true).getDestination() != null)
                    menuList.add(checkList(allTabNotApprove, i, true));
            }

            //for loop for adding non accessible tab
            for (int i = 0; i < allTab.size(); i++) {
                if (!allTabNotApprove.contains(allTab.get(i))) {
                    if (checkList(allTab, i, false).getDestination() != null)
                        menuList.add(checkList(allTab, i, false));
                }
            }
        } else {
            for (int i = 0; i < allTab.size(); i++) {
                if (checkList(allTab, i, true).getDestination() != null)
                    menuList.add(checkList(allTab, i, true));
            }

            //for loop for adding non accessible tab
            for (int i = 0; i < allTabNotApprove.size(); i++) {
                if (!allTabNotApprove.contains(allTabNotApprove.get(i))) {
                    if (checkList(allTabNotApprove, i, false).getDestination() != null)
                        menuList.add(checkList(allTabNotApprove, i, false));
                }
            }
        }

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(itemAnimator);

        Animation textAnimation = (AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink));
        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.startAnimation(textAnimation);

        ImageView iv_home_animate = (ImageView) findViewById(R.id.iv_home_animate);
        iv_home_animate.setBackgroundResource(R.drawable.home_progress);
        AnimationDrawable rocketAnimation = (AnimationDrawable) iv_home_animate.getBackground();
        rocketAnimation.start();

        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mLayoutManager.setAutoMeasureEnabled(true);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        layoutManager.setJustifyContent(JustifyContent.CENTER);

        recyclerView.setLayoutManager(mLayoutManager);

        HomeAdapter mAdapter = new HomeAdapter(menuList, HomeActivity.this);
        recyclerView.setAdapter(mAdapter);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
    }

    private HomeModel checkList(List<String> allTab, int i, Boolean isAccessible) {
        HomeModel homeModel = new HomeModel();
        homeModel.setAccessible(isAccessible);

        switch (allTab.get(i)) {
            case Constants.Thet_Sanvad:
                homeModel.setMenuName(getString(R.string.thet_savnd));
                homeModel.setMenuIcon(R.drawable.ic_thet_sanvad);
                homeModel.setDestination(ThetSavandFragment.class);
                break;

            case Constants.Broadcast:
                homeModel.setMenuName(getString(R.string.broadcast));
                homeModel.setMenuIcon(R.drawable.ic_broadcast);
                homeModel.setDestination(CommunityHomeFragment.class);
                break;

            case Constants.My_Community:
                homeModel.setMenuName(getString(R.string.community));
                homeModel.setMenuIcon(R.drawable.ic_community);
                homeModel.setDestination(GroupsFragment.class);
                break;

            case Constants.Programme_Management:
                homeModel.setMenuName(getString(R.string.programme_management));
                homeModel.setMenuIcon(R.drawable.ic_program_mangement);
                homeModel.setDestination(ProgrammeManagmentFragment.class);
                break;

            case Constants.Training_Content:
                homeModel.setMenuName(getString(R.string.training_content));
                homeModel.setMenuIcon(R.drawable.ic_traing_content);
                homeModel.setDestination(ExpandableListActivity.class);
                break;

            case Constants.Team_Management:
                homeModel.setMenuName(getString(R.string.team_management));
                homeModel.setMenuIcon(R.drawable.ic_team_management);
                homeModel.setDestination(TeamManagementFragment.class);
                break;

            case Constants.My_Reports:
                homeModel.setMenuName(getString(R.string.indicator));
                homeModel.setMenuIcon(R.drawable.ic_reports);
                homeModel.setDestination(MyReportActivity.class);
                break;

            case Constants.My_Calendar:
                homeModel.setMenuName(getString(R.string.training_calendar));
                homeModel.setMenuIcon(R.drawable.ic_calender);
                homeModel.setDestination(TrainingCalender.class);
                break;

            case Constants.Asset_management:
                homeModel.setMenuName(getString(R.string.asset_management));
                homeModel.setMenuIcon(R.drawable.ic_asset);
                homeModel.setDestination(AssetAllocatedListActivity.class);
                break;

            case Constants.HR_MODULE:
                homeModel.setMenuName(getString(R.string.leave));
                homeModel.setMenuIcon(R.drawable.ic_hr);
                homeModel.setDestination(LeaveApprovalActivity.class);
                break;

            case Constants.Account_Section:
                homeModel.setMenuName(getString(R.string.account_section));
                homeModel.setMenuIcon(R.drawable.ic_account);
                homeModel.setDestination(AccountSectionActivity.class);
                break;

            case Constants.Attendance:
                homeModel.setMenuName(getString(R.string.attendance));
                homeModel.setMenuIcon(R.drawable.ic_about_us);
                homeModel.setDestination(AttendanceActivity.class);
                break;
        }
        return homeModel;
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        (dialog1, which) -> redirectStore(updateUrl)).setNegativeButton("No, thanks",
                        (dialog12, which) -> {
                        })
                .create();

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
                shareApp();
                return true;

            case R.id.action_rate:
                RateThisApp.showRateDialog(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                return true;

            case R.id.action_add_school:
                Intent openClass = new Intent(HomeActivity.this, AddSchoolActivity.class);
                startActivity(openClass);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_bell_notification:
                Intent intent = new Intent(this, NotificationActivity.class);
                startActivity(intent);
                tvUnreadNotification.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ISROLECHANGE && resultCode == RESULT_OK) {

            if (User.getCurrentUser(getApplicationContext()).getMvUser() != null) {
                if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() == null ||
                        !User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved().equalsIgnoreCase("false")) {

                    if (alertDialogApproved != null && alertDialogApproved.isShowing())
                        alertDialogApproved.dismiss();
                }
            }

            initViews();
        }
    }

    private void showNotificationDialog() {
        final String[] items = {"On", "Off"};
        int checkedItem;

        if (preferenceHelper.getBoolean(PreferenceHelper.NOTIFICATION)) {
            checkedItem = 0;
        } else {
            checkedItem = 1;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.notification))
                .setSingleChoiceItems(items, checkedItem, (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.ok), (dialog1, id) -> {
                    ListView lw = ((AlertDialog) dialog1).getListView();
                    if (lw.getCheckedItemPosition() == 0) {
                        preferenceHelper.insertBoolean(PreferenceHelper.NOTIFICATION, true);
                    } else {
                        preferenceHelper.insertBoolean(PreferenceHelper.NOTIFICATION, false);
                    }
                    dialog1.dismiss();
                }).create();

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showDialog() {
        final String[] items = {"English", "मराठी", "हिंदी "};

        int checkId = 0;
        if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_MARATHI)) {
            checkId = 1;
        } else if (preferenceHelper.getString(Constants.LANGUAGE).equalsIgnoreCase(Constants.LANGUAGE_HINDI)) {
            checkId = 2;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_lang))
                .setSingleChoiceItems(items, checkId, (dialogInterface, i) -> {
                })
                .setPositiveButton(R.string.ok, (dialog1, id) -> {
                    ListView lw = ((AlertDialog) dialog1).getListView();
                    switch (lw.getCheckedItemPosition()) {
                        case 0:
                            LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_ENGLISH);
                            preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_ENGLISH);
                            break;

                        case 1:
                            LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_MARATHI);
                            preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_MARATHI);
                            break;

                        default:
                            LocaleManager.setNewLocale(getApplicationContext(), Constants.LANGUAGE_HINDI);
                            preferenceHelper.insertString(Constants.LANGUAGE, Constants.LANGUAGE_HINDI);
                            break;
                    }

                    dialog1.dismiss();
                    finish();
                    startActivity(getIntent());
                }).create();

        dialog.setCancelable(true);
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private void showLogoutPopUp() {
        if (AppDatabase.getAppDatabase(HomeActivity.this).userDao().getOfflineTaskCount(Constants.TASK_ANSWER, "true") == 0) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertDialog.setTitle(getString(R.string.app_name));

            // Setting Dialog Message
            alertDialog.setMessage(getString(R.string.logout_string));

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.app_logo);

            // Setting CANCEL Button
            alertDialog.setButton2(getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());

            // Setting OK Button
            alertDialog.setButton(getString(R.string.ok), (dialog, which) -> sendLogOutRequest());

            // Showing Alert Message
            alertDialog.show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.cannotLogout));
            alertDialog.setMessage(getString(R.string.submitAllForms));
            alertDialog.setButton(getString(R.string.ok), (dialog, which) -> {
            });
            alertDialog.show();
        }
    }

    @SuppressWarnings("deprecation")
    private void showUpdateDataPopup() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.update_data_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());

        // Setting OK Button
        alertDialog.setButton(getString(R.string.ok), (dialog, which) -> {
            List<LocationModel> districts = AppDatabase.getAppDatabase(HomeActivity.this).userDao().getDistinctDistrict();
            AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearLocation();

            for (int i = 0; i < districts.size(); i++) {
                // add infos for the service which file to download and where to store
                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                intent.putExtra(Constants.State, districts.get(i).getState());
                intent.putExtra(Constants.DISTRICT, districts.get(i).getDistrict());
                startService(intent);
            }

            if (Utills.isConnected(HomeActivity.this)) {
                getUserData();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void sendLogOutRequest() {
        if (Utills.isConnected(this)) {
            Utills.showProgressDialog(this);
            ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            String userId = User.getCurrentUser(this).getMvUser() != null ? User.getCurrentUser(this).getMvUser().getId() : "";
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.DoLogout_url + userId;

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
                    AppDatabase.getAppDatabase(HomeActivity.this).userDao().clearNotification();

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

    @SuppressWarnings("deprecation")
    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @SuppressWarnings("deprecation")
    private void showApprovedDialog() {

        if (alertDialogApproved.isShowing()) {
            alertDialogApproved.dismiss();
        }

        // Setting Dialog Title
        alertDialogApproved.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        String message = getString(R.string.approve_profile);
        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null) {
            if (User.getCurrentUser(getApplicationContext()).getMvUser().getApproval_role() != null) {
                String ss =User.getCurrentUser(getApplicationContext()).getMvUser().getApprover_Comment__c();
                String sst =User.getCurrentUser(getApplicationContext()).getMvUser().getApproval_role();
                if (!User.getCurrentUser(getApplicationContext()).getMvUser().getApprover_Comment__c().equals("")) {
                    message = getString(R.string.approve_profile) + "\nRemark:" + User.getCurrentUser(getApplicationContext()).getMvUser().getApprover_Comment__c();
                } else {
                    message = getString(R.string.approve_profile) + "\n" + User.getCurrentUser(getApplicationContext()).getMvUser().getApproval_role() + " " + getString(R.string.approve_profile2);
                }
            }
        }
        alertDialogApproved.setMessage(message);

        // Setting Icon to Dialog
        alertDialogApproved.setIcon(R.drawable.app_logo);

        // Setting OK Button
        alertDialogApproved.setButton(getString(android.R.string.ok), (dialog, which) -> alertDialogApproved.dismiss());

        try {
            // Showing Alert Message
            alertDialogApproved.show();
        } catch (Exception e) {
            Log.e("Error in showing dialog", e.getMessage());
        }
    }

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
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private static String[] getColumnIndex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;
    }

    private void getUserData() {
        if (User.getCurrentUser(getApplicationContext()).getMvUser() == null) {
            return;
        }

        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserData_url + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.isSuccess()) {
                        String data = response.body().string();
                        preferenceHelper.insertString(PreferenceHelper.UserData, data);
                        User.clearUser();

                        if (User.getCurrentUser(getApplicationContext()).getMvUser() != null) {
                            if (User.getCurrentUser(getApplicationContext()).getMvUser().getIsApproved() == null ||
                                    !User.getCurrentUser(getApplicationContext()).getMvUser()
                                            .getIsApproved().equalsIgnoreCase("false")) {

                                if (alertDialogApproved != null && alertDialogApproved.isShowing())
                                    alertDialogApproved.dismiss();
                            }
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

    private void shareApp() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Mulyavardhan 2.0");
            String shareUrl = "\nLet me recommend you this application\n\nhttps://play.google.com/store/apps/details?id=com.mv&hl=en \n\n";
            i.putExtra(Intent.EXTRA_TEXT, shareUrl);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
                break;
        }
    }

    private void getAddress() {
        if (!Utills.isLocationPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.LOCATION_PERMISSION_REQUEST);
            }
        } else {
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        return;
                    }

                    mLastLocation = location;
                    getMapParameters(String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude()));

                    if (!Geocoder.isPresent()) {
                        return;
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e("fail", "unable to connect");
                });
    }

    @SuppressWarnings("deprecation")
    private void LocationGPSDialog() {
        if (alertLocationDialog == null) {
            alertLocationDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertLocationDialog.setTitle(getString(R.string.gps_settings));

            // Setting Dialog Message
            alertLocationDialog.setMessage(getString(R.string.no_gps));

            // Setting Icon to Dialog
            alertLocationDialog.setIcon(R.drawable.app_logo);

            // Setting OK Button
            alertLocationDialog.setButton(getString(R.string.gps_settings), (dialog, which) -> {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            });

            // Showing Alert Message
            alertLocationDialog.show();
            alertLocationDialog.setCancelable(false);
        }
    }

    private void getMapParameters(String latitude, String longitude) {
        if (!Utills.isConnected(this)) {
            return;
        }

        try {
            preferenceHelper = new PreferenceHelper(getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lat", latitude);
            jsonObject.put("lon", longitude);
            jsonObject.put("id", User.getCurrentUser(this).getMvUser() != null ?
                    User.getCurrentUser(this).getMvUser().getId() : "");

            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

            ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.MapParametersUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data.length() > 0) {
                                JSONObject jsonObject = new JSONObject(data);
                                String statusOfMap = jsonObject.getString("status");
                                String message = jsonObject.getString("msg");
                                Log.d("onResponse", statusOfMap + "-" + message);
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
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callUsDialog() {
        final String[] items = {getString(R.string.call_on_hangout), getString(R.string.call_on_landline)};

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.app_name));
        dialog.setItems(items, (dialogInterface, position) -> {
            dialogInterface.dismiss();

            switch (position) {
                case 0:
                    // missing 'http://' will cause crashed
                    Uri uri = Uri.parse(User.getCurrentUser(getApplicationContext()).getAppConfig().getHangout_URL__c());
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
        });

        dialog.show();
    }

    //Set the Alarm for checkin time repiting every day
//    private void setCheckInAlarm() {
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
//        intent.setAction(Constants.ACTION_ALARM_RECEIVER);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this,
//                1001, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 10); //
//        calendar.set(Calendar.MINUTE, 15);
//        calendar.set(Calendar.SECOND, 0);
//
//        if (manager != null) {
//            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        }
//        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
//    }

    private void getAllLeaves() {
        if (User.getCurrentUser(getApplicationContext()).getMvUser() == null) {
            return;
        }

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) +
                Constants.GetAllMyLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);
                            ArrayList<LeavesModel> leavesList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);

                                LeavesModel leavesModel = new LeavesModel();
                                leavesModel.setId(data.getString("Id"));
                                leavesModel.setFromDate(data.getString("From__c"));
                                leavesModel.setToDate(data.getString("To__c"));
                                leavesModel.setReason(data.getString("Reason__c"));
                                leavesModel.setTypeOfLeaves(data.getString("Leave_Type__c"));
                                leavesModel.setStatus(data.getString("Status__c"));

                                if (data.has("Comment__c")) {
                                    leavesModel.setComment(data.getString("Comment__c"));
                                }

                                leavesModel.setRequested_User__c(data.getString("Requested_User__c"));

                                if (data.has("Requested_User_Name__c")) {
                                    leavesModel.setRequested_User_Name__c(data.getString("Requested_User_Name__c"));
                                }

                                if (data.has("isHalfDay__c")) {
                                    leavesModel.setHalfDayLeave(data.getBoolean("isHalfDay__c"));
                                } else {
                                    leavesModel.setHalfDayLeave(false);
                                }

                                leavesList.add(leavesModel);
                            }

                            AppDatabase.getAppDatabase(HomeActivity.this).userDao().deleteAllLeaves();
                            AppDatabase.getAppDatabase(HomeActivity.this).userDao().insertLeaves(leavesList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_lang:
                showDialog();
                break;

            case R.id.action_profile:
                Intent intent;
                intent = new Intent(this, RegistrationActivity.class);
                intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
                startActivityForResult(intent, Constants.ISROLECHANGE);
                break;

            case R.id.action_logout:
                showLogoutPopUp();
                break;

            case R.id.action_notification:
                showNotificationDialog();
                break;

            case R.id.action_share:
                shareApp();
                break;

            case R.id.action_rate:
                RateThisApp.showRateDialog(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                break;

            case R.id.action_add_school:
                if (User.getCurrentUser(getApplicationContext()).getRolePermssion() != null &&
                        User.getCurrentUser(getApplicationContext()).getRolePermssion().getIsLocationAllow__c().equals("true")) {
                    Intent openClass = new Intent(HomeActivity.this, AddSchoolActivity.class);
                    startActivity(openClass);
                } else {
                    Utills.showToast("You don't have access to add location", HomeActivity.this);
                }
                break;

            case R.id.action_callus:
                callUsDialog();
                break;

            case R.id.action_update_user_data:
                showUpdateDataPopup();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}