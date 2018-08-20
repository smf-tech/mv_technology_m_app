package com.mv.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Attendance;
import com.mv.Model.HolidayListModel;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class AttendanceActivity extends AppCompatActivity implements View.OnClickListener, OnDateSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityAttendanceBinding binding;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private PreferenceHelper preferenceHelper;
    private List<Attendance> attendanceList = new ArrayList<Attendance>();
    private SimpleDateFormat formatter;
    private ArrayList<CalendarDay> dates = new ArrayList<>();
    private ArrayList<CalendarDay> leaveDates = new ArrayList<>();
    private GPSTracker gps;
    private int checkInClickable = 0, checkOutClickable = 0;
    private List<HolidayListModel> holidayListModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attendance);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
        holidayListModels = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllHolidayList();
        leaveDates.clear();
        for (int i = 0; i < holidayListModels.size(); i++) {
            try {
                leaveDates.add(CalendarDay.from(formatter.parse(holidayListModels.get(i).getHoliday_Date__c())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, leaveDates, getResources().getDrawable(R.drawable.circle_background_red)));
        if (Utills.isConnected(AttendanceActivity.this)) {
            // Send offline attendance to server
            Attendance temp = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getUnSynchAttendance();
            if (temp != null) {
                Intent intent = new Intent(AttendanceActivity.this, SendAttendance.class);
                startService(intent);
                //use local BD as server is not Updated
                attendanceList = AppDatabase.getAppDatabase(AttendanceActivity.this).userDao().getAllAttendance();
                for (Attendance attendance : attendanceList) {
                    try {
                        dates.add(CalendarDay.from(formatter.parse(attendance.getDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                setButtonView();
                binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, dates, getResources().getDrawable(R.drawable.circle_background)));
            }else{
                setButtonView();
                getAttendanceData();
            }
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
            binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, dates, getResources().getDrawable(R.drawable.circle_background)));
        }
    }

    private String getCurrentDate() {
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        return formattedDate;
    }

    public void checkInClick() {
        if (checkInClickable == 1) {
            Utills.showToast("Already Check In", AttendanceActivity.this);
            return;
        } else if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
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
        attendance.setCheckInLng("" + gps.getLongitude());
        attendance.setCheckInLat("" + gps.getLatitude());
        attendance.setStatus("Approved");
        attendance.setUser(User.getCurrentUser(getApplicationContext()).getMvUser().getId());
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat time1 = new SimpleDateFormat("kk.mm");
        long date = System.currentTimeMillis();
        String timeString1 = time1.format(date);
        attendance.setCheckInTime("" + timeString1);
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        sendAttendance(attendance, true, isPresent);
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
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(object.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/saveAttendance", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                if (response != null && response.isSuccess()) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        if (object.has("Status")) {
                                            if (object.getString("Status").equalsIgnoreCase("done")) {
                                                JSONObject object1 = object.getJSONObject("Records");
                                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                                if (!isPresent) {
                                                    Attendance attendance1 = gson.fromJson(object1.toString(), Attendance.class);
                                                    attendanceList.add(attendance1);
                                                    dates.add(CalendarDay.from(formatter.parse(attendance1.getDate())));
                                                    binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, dates, getResources().getDrawable(R.drawable.circle_background)));
                                                }
                                                if (isCheckedIn)
                                                    Utills.showToast("Checked In Successfully...", AttendanceActivity.this);
                                                else
                                                    Utills.showToast("Checked Out Successfully...", AttendanceActivity.this);
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
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        } else {
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
            binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, dates, getResources().getDrawable(R.drawable.circle_background)));

            if (isCheckedIn)
                Utills.showToast("Offline Checked In Successfully...", AttendanceActivity.this);
            else
                Utills.showToast("Offline Checked Out Successfully...", AttendanceActivity.this);
            setButtonView();
        }
    }

    public void checkOutClick() {
        if (checkOutClickable == 1) {
            Utills.showToast("Already Check Out", AttendanceActivity.this);
            return;
        } else if (checkOutClickable == 2) {
            Utills.showToast("Please First Check In and then try to Check Out...", AttendanceActivity.this);
            return;
        } else if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
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
        attendance.setCheckOutLng("" + gps.getLongitude());
        attendance.setCheckOutLat("" + gps.getLatitude());
        attendance.setStatus("Approved");
        attendance.setUser(User.getCurrentUser(getApplicationContext()).getMvUser().getId());
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat time1 = new SimpleDateFormat("kk.mm");
        long date = System.currentTimeMillis();
        String timeString1 = time1.format(date);
        attendance.setCheckOutTime("" + timeString1);
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        sendAttendance(attendance, false, isPresent);
    }

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
                        if (str != null && str.length() > 0) {
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
                            binding.calendarView.addDecorator(new EventDecorator(AttendanceActivity.this, dates, getResources().getDrawable(R.drawable.circle_background)));
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
        if (dates != null && dates.contains(day)) {
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

        binding.calendarView.addDecorators(
                //new MySelectorDecorator(context),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );
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

    private void setActionbar(String Title) {

        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = Title;
        if (Title != null && Title.contains("\n"))
            str = Title.replace("\n", " ");
        toolbar_title.setText(str);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
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
        } else if (leaveDates.contains(date)) {
            buildDialog(R.style.DialogTheme, leaveDates.indexOf(date), 1);
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
        StringBuffer buffer = new StringBuffer();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (dialogType == 0) {
            Attendance attendance = attendanceList.get(position);

            buffer.append("Check In Time : " + attendance.getCheckInTime());
            if (attendance.getCheckOutTime() != null) {
                if (!attendance.getCheckOutTime().equals("null")) {
                    buffer.append("\n\n");
                    buffer.append("Check Out Time : " + attendance.getCheckOutTime());
                }
            }
            buffer.append("\n\n");
            buffer.append("Status : " + attendance.getStatus());
            builder.setTitle("Date : " + attendance.getDate());
        } else if (dialogType == 1) {
            HolidayListModel holidayListModel = holidayListModels.get(position);
            builder.setTitle("Date : " + holidayListModel.getHoliday_Date__c());
            buffer.append("" + holidayListModel.getName());
        }


        builder.setMessage(buffer.toString());
        builder.setNegativeButton(getString(R.string.ok), null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = animationSource;
        dialog.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
