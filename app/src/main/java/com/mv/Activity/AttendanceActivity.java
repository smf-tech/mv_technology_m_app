package com.mv.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Attendance;
import com.mv.Model.HolidayListModel;
import com.mv.Model.LeavesModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.SendAttendance;
import com.mv.Utils.Constants;
import com.mv.Utils.GPSTracker;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAttendanceBinding;
import com.mv.decorators.EventDecorator;
import com.mv.decorators.HighlightWeekendsDecorator;
import com.mv.decorators.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */
public class AttendanceActivity extends AppCompatActivity implements View.OnClickListener,
        OnDateSelectedListener {

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private ActivityAttendanceBinding binding;
    private PreferenceHelper preferenceHelper;
    private SimpleDateFormat formatter;

    private List<Attendance> attendanceList = new ArrayList<>();
    private List<HolidayListModel> holidayListModels;

    private ArrayList<CalendarDay> dates = new ArrayList<>();
    private ArrayList<CalendarDay> holidayDates = new ArrayList<>();
    private ArrayList<CalendarDay> leavesDates = new ArrayList<>();

    private GPSTracker gps;
    private Location location;

    private int checkInClickable = 0, checkOutClickable = 0;
    long UPDATE_INTERVAL = 600 * 1000;  /* 10 secs */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attendance);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initViews();

        //show holidays in calendar
        holidayListModels = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllHolidayList();
        holidayDates.clear();

        for (int i = 0; i < holidayListModels.size(); i++) {
            try {
                holidayDates.add(CalendarDay.from(formatter.parse(holidayListModels.get(i).getHoliday_Date__c())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                holidayDates, getResources().getDrawable(R.drawable.circle_background_red)));

        //show leaves in calendar
        List<LeavesModel> leavesModelList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getApprovedLeaves("Approved");
        leavesDates.clear();

        for (int i = 0; i < leavesModelList.size(); i++) {
            try {
                if (leavesModelList.get(i).getFromDate().equals(leavesModelList.get(i).getToDate())) {
                    if (leavesModelList.get(i) != null && !leavesModelList.get(i).isHalfDayLeave()) {
                        leavesDates.add(CalendarDay.from(formatter.parse(leavesModelList.get(i).getFromDate())));
                    }
                } else {
                    getDatesBetween(leavesModelList.get(i).getFromDate(), leavesModelList.get(i).getToDate());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                leavesDates, getResources().getDrawable(R.drawable.circle_background_pink)));

        if (Utills.isConnected(AttendanceActivity.this)) {
            // Send offline attendance to server
            Attendance temp = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getUnSynchAttendance();
            if (temp != null) {
                Intent intent = new Intent(AttendanceActivity.this, SendAttendance.class);
                startService(intent);

                //use local DB as server is not Updated
                attendanceList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllAttendance();
                for (Attendance attendance : attendanceList) {
                    try {
                        dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                setButtonView();
                binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                        dates, getResources().getDrawable(R.drawable.circle_background)));
            } else {
                attendanceList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllAttendance();
                for (Attendance attendance : attendanceList) {
                    try {
                        dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                setButtonView();
                binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                        dates, getResources().getDrawable(R.drawable.circle_background)));

                getAttendanceData();
            }
        } else {
            attendanceList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllAttendance();

            // display offline attendance in difference color
            Attendance temp = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getUnSynchAttendance();
            if (temp != null) {
                int i = -1;
                for (Attendance tempp : attendanceList) {
                    i++;
                    if (tempp.getUnique_Id() == temp.getUnique_Id()) {
                        break;
                    }
                }
                attendanceList.remove(i);

                ArrayList<CalendarDay> tempDate = new ArrayList<>();
                try {
                    tempDate.add(CalendarDay.from(formatter.parse(temp.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //difference color
                binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                        tempDate, getResources().getDrawable(R.drawable.circle_background_light_blue)));
            }

            for (Attendance attendance : attendanceList) {
                try {
                    dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            setButtonView();
            binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                    dates, getResources().getDrawable(R.drawable.circle_background)));
        }

        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        this.location = location;
    }

    private void getDatesBetween(String dateString1, String dateString2) {
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = formatter.parse(dateString1);
            date2 = formatter.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            leavesDates.add(CalendarDay.from(cal1));
            cal1.add(Calendar.DATE, 1);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentDate() {
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        return formattedDate;
    }

    @SuppressLint("SimpleDateFormat")
    public void checkInClick() {
        if (location == null) {
            Utills.showToast("Current location is not available, Please try again", AttendanceActivity.this);
            return;
        }

        if (checkInClickable == 1) {
            Utills.showToast("Already Check In", AttendanceActivity.this);
            return;
        } else if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
            return;
        } else if (0.0 == location.getLongitude() && 0.0 == location.getLatitude()) {
            Utills.showToast("Current location is not available, Please try again", AttendanceActivity.this);
            initViews();
            return;
        }

        Boolean isPresent;
        Attendance attendance;
        CalendarDay day = null;

        try {
            day = CalendarDay.from(formatter.parse(getCurrentDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dates.contains(day)) {
            attendance = attendanceList.get(dates.indexOf(day));
            isPresent = true;
        } else {
            isPresent = false;
            attendance = new Attendance();
        }

        attendance.setDate(getCurrentDate());
        attendance.setStatus("");

        double Lat = location.getLatitude();
        double Long = location.getLongitude();
        attendance.setCheckInLng("" + Long);
        attendance.setCheckInLat("" + Lat);
        attendance.setStatus("");
        attendance.setUser(User.getCurrentUser(getApplicationContext()).getMvUser().getId());

        //send relative address of lat long to server
        String address = ConvertToAddress(Lat, Long);
        attendance.setCheckIn_Attendance_Address__c(address);

        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        SimpleDateFormat time1 = new SimpleDateFormat("kk.mm");
        long date = System.currentTimeMillis();
        String timeString1 = time1.format(date);
        attendance.setCheckInTime("" + timeString1);
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));

        if (leavesDates.contains(day)) {
            Utills.showToast("You are on leave", AttendanceActivity.this);
        } else {
            sendAttendance(attendance, true, isPresent);
        }
    }

    private String ConvertToAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String result = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                result = addressList.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e("Error", "Unable connect to Geocoder", e);
            return "";
        } catch (IllegalArgumentException e) {
            Log.e("Error", "Unable connect to Geocoder", e);
            return "";
        }
        return result;
    }

    private void sendAttendance(Attendance attendance, Boolean isCheckedIn, Boolean isPresent) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(attendance);
                JSONObject object = new JSONObject();
                JSONObject jsonObject = new JSONObject(json);
                object.put("att", jsonObject);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(object.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/saveAttendance", gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                if (response.isSuccess()) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        if (object.has("Status")) {
                                            if (object.getString("Status").equalsIgnoreCase("done")) {
                                                JSONObject object1 = object.getJSONObject("Records");
                                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                                if (!isPresent) {
                                                    Attendance attendance1 = gson.fromJson(object1.toString(), Attendance.class);
                                                    attendanceList.add(attendance1);
                                                    dates.add(CalendarDay.from(formatter.parse(attendance1.getDate())));
                                                    binding.calendarView.addDecorator(
                                                            new EventDecorator(AttendanceActivity.this, dates,
                                                                    getResources().getDrawable(R.drawable.circle_background)));
                                                }

                                                if (isCheckedIn) {
                                                    Utills.showToast("Checked In Successfully...", AttendanceActivity.this);
                                                } else {
                                                    Utills.showToast("Checked Out Successfully...", AttendanceActivity.this);
                                                }

                                                setButtonView();
                                            } else {
                                                Utills.showToast(object.getString("Status"), AttendanceActivity.this);
                                            }
                                        }

                                    }
                                }
                            }
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                            // save off line if failed to upload the attendance dueto network issue
                            addOffLineAttendance(isPresent, attendance, isCheckedIn);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                        // save off line if failed to upload the attendance dueto network issue
                        addOffLineAttendance(isPresent, attendance, isCheckedIn);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                // save off line if failed to upload the attendance dueto network issue
                addOffLineAttendance(isPresent, attendance, isCheckedIn);
            }
        } else {
            addOffLineAttendance(isPresent, attendance, isCheckedIn);
        }
    }

    // save offline attendance
    void addOffLineAttendance(Boolean isPresent, Attendance attendance, Boolean isCheckedIn) {
        if (!isPresent) {
            attendanceList.add(attendance);
            try {
                dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        attendance.setSynch("false");
        AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().insertAttendance(attendance);

        dates.clear();
        attendanceList.clear();

        attendanceList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllAttendance();
        for (Attendance attendance1 : attendanceList) {
            try {
                dates.add(CalendarDay.from(formatter.parse(attendance1.getDate())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        setButtonView();
        binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                dates, getResources().getDrawable(R.drawable.circle_background)));

        if (isCheckedIn) {
            Utills.showToast("Offline Checked In Successfully...", AttendanceActivity.this);
        } else {
            Utills.showToast("Offline Checked Out Successfully...", AttendanceActivity.this);
        }

        setButtonView();
    }

    @SuppressLint("SimpleDateFormat")
    public void checkOutClick() {
        if (location == null) {
            Utills.showToast("Current location is not available, Please try again", AttendanceActivity.this);
            return;
        }

        if (checkOutClickable == 1) {
            Utills.showToast("Already Check Out", AttendanceActivity.this);
            return;
        } else if (checkOutClickable == 2) {
            Utills.showToast("Please First Check In and then try to Check Out...", AttendanceActivity.this);
            return;
        } else if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
            return;
        } else if (0.0 == location.getLongitude() && 0.0 == location.getLatitude()) {
            Utills.showToast(getResources().getString(R.string.location_not_found), AttendanceActivity.this);
            initViews();
            return;
        }

        Attendance attendance;
        Boolean isPresent;
        CalendarDay day = null;

        try {
            day = CalendarDay.from(formatter.parse(getCurrentDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dates.contains(day)) {
            isPresent = true;
            attendance = attendanceList.get(dates.indexOf(day));
        } else {
            isPresent = false;
            attendance = new Attendance();
        }

        attendance.setDate(getCurrentDate());
        attendance.setStatus("");

        double Lat = location.getLatitude();
        double Long = location.getLongitude();
        attendance.setCheckOutLng("" + Long);
        attendance.setCheckOutLat("" + Lat);
        attendance.setStatus("Approved");
        attendance.setUser(User.getCurrentUser(getApplicationContext()).getMvUser().getId());

        //send relative address of lat long to server
        String address = ConvertToAddress(Lat, Long);
        attendance.setCheckOut_Attendance_Address__c(address);

        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        SimpleDateFormat time1 = new SimpleDateFormat("kk.mm");
        long date = System.currentTimeMillis();
        String timeString1 = time1.format(date);
        attendance.setCheckOutTime("" + timeString1);

        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        if (leavesDates.contains(day)) {
            Utills.showToast("You are on leave", AttendanceActivity.this);
        } else {
            sendAttendance(attendance, false, isPresent);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void initViews() {
        gps = new GPSTracker(AttendanceActivity.this);
        setActionbar(getString(R.string.attendance));
        setCalendar();
        preferenceHelper = new PreferenceHelper(this);
        formatter = new SimpleDateFormat("yyyy-MM-dd");
    }

    private void getAttendanceData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(AttendanceActivity.this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetAttendanceData + "?userId=" + User.getCurrentUser(AttendanceActivity.this).getMvUser().getId();

        //  String url = "http://www.json-generator.com/api/json/get/bTUvLZXUjm?indent=2";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        attendanceList.clear();

                        String str = response.body().string();
                        if (str.length() > 0) {
                            dates.clear();

                            JSONArray jsonArray = new JSONArray(str);
                            if (jsonArray.length() > 0) {
                                List<Attendance> tempAttendances = Arrays.asList(gson.fromJson(jsonArray.toString(), Attendance[].class));
                                attendanceList.addAll(tempAttendances);
                                AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().deleteAllAttendance();
                                AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().insertAllAttendance(attendanceList);
                            }

                            for (Attendance attendance : attendanceList) {
                                dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
                            }

                            setButtonView();
                            binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this,
                                    dates, getResources().getDrawable(R.drawable.circle_background)));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void setButtonView() {
        CalendarDay day = null;
        try {
            day = CalendarDay.from(formatter.parse(getCurrentDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dates != null && dates.contains(day) && attendanceList.size() > dates.indexOf(day)) {
            Attendance attendance = attendanceList.get(dates.indexOf(day));
            if (attendance.getCheckInTime() == null) {
                binding.chekInLayout.setBackgroundResource(R.drawable.orange_box);
                checkInClickable = 0;
            } else {
                binding.chekInLayout.setBackgroundResource(R.drawable.orange_box_disabled);
                checkInClickable = 1;
            }

            if (attendance.getCheckOutTime() == null) {
                binding.chekOutLayout.setBackgroundResource(R.drawable.orange_box);
                checkOutClickable = 0;
            } else {
                binding.chekOutLayout.setBackgroundResource(R.drawable.orange_box_disabled);
                checkOutClickable = 1;
            }
        } else {
            binding.chekOutLayout.setBackgroundResource(R.drawable.orange_box_disabled);
            checkOutClickable = 2;
        }
    }

    private void setCalendar() {
        binding.calendarView.setOnDateChangedListener(this);
        binding.calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        Calendar instance = Calendar.getInstance();
        binding.calendarView.setSelectedDate(instance.getTime());

        Calendar instance1 = Calendar.getInstance();
        instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);
        binding.calendarView.state().edit()
                .setMinimumDate(instance1.getTime())
                .commit();

        binding.calendarView.addDecorators(new HighlightWeekendsDecorator(), oneDayDecorator);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String title) {
        String str = title;
        if (title != null && title.contains("\n")) {
            str = title.replace("\n", " ");
        }

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    /**
     * Called when a user clicks on a day.
     * There is no logic to prevent multiple calls for the same date and state.
     *
     * @param widget   the view associated with this listener
     * @param date     the date that was selected or unselected
     * @param selected true if the day is now selected, false otherwise
     */
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (dates.contains(date)) {
            buildDialog(R.style.DialogTheme, dates.indexOf(date), 0);
        } else if (holidayDates.contains(date)) {
            buildDialog(R.style.DialogTheme, holidayDates.indexOf(date), 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            }
        }
    }

    private void buildDialog(int animationSource, int position, int dialogType) {
        StringBuilder buffer = new StringBuilder();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (dialogType == 0) {
            Attendance attendance = attendanceList.get(position);
            buffer.append(String.format("Check In Time : %s", attendance.getCheckInTime()));

            if (attendance.getCheckOutTime() != null) {
                if (!attendance.getCheckOutTime().equals("null")) {
                    String checkOutTime = "Check Out Time : " + attendance.getCheckOutTime();
                    buffer.append("\n\n");
                    buffer.append(checkOutTime);
                }
            }

            String status = "Status : " + attendance.getStatus();
            buffer.append("\n\n");
            buffer.append(status);
            builder.setTitle("Date : " + attendance.getDate());

        } else if (dialogType == 1) {
            HolidayListModel holidayListModel = holidayListModels.get(position);
            builder.setTitle("Date : " + holidayListModel.getHoliday_Date__c());

            String modelName = "" + holidayListModel.getName();
            buffer.append(modelName);
        }

        builder.setMessage(buffer.toString());
        builder.setNegativeButton(getString(R.string.ok), null);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = animationSource;
        }

        dialog.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
