package com.mv.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.CalenderEvent;
import com.mv.Model.EventUser;
import com.mv.Model.Template;
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
import com.mv.databinding.ActivityClalenderFliterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalenderFliterActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private Activity context;
    private ActivityClalenderFliterBinding binding;
    private PreferenceHelper preferenceHelper;
    private CalenderEvent calenderEvent = new CalenderEvent();

    private int mSelectOrganization = 0;
    private int mSelectState = 1;
    private int mSelectDistrict = 1;
    private int mSelectTaluka = 0;
    private int mSelectCluster = 0;
    private int mSelectVillage = 0;

    private List<String> mListOrganization, mListRoleName, mListDistrict, mListTaluka,
            mListCluster, mListVillage, mListSchoolName, mStateList;
    private ArrayList<String> selectedProcessId = new ArrayList<>();
    private ArrayList<String> selectedRole = new ArrayList<>();
    private ArrayList<Template> processList;

    private String processId = "";
    private String id = "";
    private String SelectedEventAttendanceIDs = "";
    public String selectedState = "", selectedDisrict = "", selectedRolename = "", selectedTaluka = "",
            selectedCluster = "", selectedVillage = "", selectedSchool = "", selectedOrganization = "",
            selectedUserId = "", selectedUserName = "", selectedCatagory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_clalender_fliter);
        binding.setActivity(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            extras.getParcelable(Constants.My_Calendar);
        }

        if (getIntent().getParcelableExtra(Constants.My_Calendar) != null) {
            calenderEvent = getIntent().getParcelableExtra(Constants.My_Calendar);
            id = calenderEvent.getId();
            SelectedEventAttendanceIDs = calenderEvent.getPresent_User__c();

            selectedState = calenderEvent.getState__c();
            selectedDisrict = calenderEvent.getDistrict__c();
            selectedTaluka = calenderEvent.getTaluka__c();
            selectedCluster = calenderEvent.getCluster__c();
            selectedVillage = calenderEvent.getVillage__c();
            selectedSchool = calenderEvent.getSchool__c();

            if (calenderEvent.getOrganization__c() != null) {
                selectedOrganization = calenderEvent.getOrganization__c();
            } else {
                selectedOrganization = User.getCurrentUser(getApplicationContext()).getMvUser().getOrganisation();
            }

            if (calenderEvent.getStatus() != null && calenderEvent.getStatus().length() > 0) {
                binding.spinnerStatus.setSelection(Arrays.asList(
                        getResources().getStringArray(R.array.array_of_status)).indexOf(calenderEvent.getStatus()));
            }

            selectedRole = new ArrayList<>(Arrays.asList(getColumnIdex((calenderEvent.getRole__c()).split(","))));
            selectedRolename = calenderEvent.getRole__c();

            binding.spinnerRole.setText(calenderEvent.getRole__c());
            binding.etEventTime.setText(calenderEvent.getEvent_Time__c());
            binding.etEventTitle.setText(calenderEvent.getTitle());
            binding.etEventDate.setText(calenderEvent.getDate());
            binding.etEventEndDate.setText(calenderEvent.getEnd_Date__c());
            binding.etEventEndTime.setText(calenderEvent.getEvent_End_Time__c());
            binding.etEventDiscription.setText(calenderEvent.getDescription());

            if (calenderEvent.getMV_Process__c() != null) {
                selectedProcessId = new ArrayList<>(Arrays.asList(
                        getColumnIdex(("Other," + calenderEvent.getMV_Process__c()).split(","))));
            } else {
                selectedProcessId = new ArrayList<>(Arrays.asList(
                        getColumnIdex(("Other,").split(","))));
            }

            if (User.getCurrentUser(this).getMvUser().getHide_Role_On_Calendar__c()
                    .contains(User.getCurrentUser(this).getMvUser().getRoll())) {
                binding.lyAttendance.setVisibility(View.GONE);
            }

            if (!calenderEvent.getMV_User1__c().equals(User.getCurrentUser(this).getMvUser().getId())) {
                binding.etEventTime.setEnabled(false);
                binding.etEventTitle.setEnabled(false);
                binding.etEventDate.setEnabled(false);
                binding.spinnerCatogory.setEnabled(false);
                binding.etEventEndDate.setEnabled(false);
                binding.etEventEndTime.setEnabled(false);
                binding.etEventDiscription.setEnabled(false);
                binding.spinnerStatus.setEnabled(false);
                binding.lyAttendance.setVisibility(View.INVISIBLE);
                binding.btnSubmit.setVisibility(View.GONE);
            }
        } else {
            id = null;
            selectedState = User.getCurrentUser(getApplicationContext()).getMvUser().getState();
            selectedDisrict = User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict();
            selectedTaluka = User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka();
            selectedCluster = User.getCurrentUser(getApplicationContext()).getMvUser().getCluster();
            selectedVillage = User.getCurrentUser(getApplicationContext()).getMvUser().getVillage();
            selectedSchool = User.getCurrentUser(getApplicationContext()).getMvUser().getSchool_Name();
            selectedOrganization = User.getCurrentUser(getApplicationContext()).getMvUser().getOrganisation();
            selectedRole = new ArrayList<>(Arrays.asList(getColumnIdex("Select".split(","))));
            selectedProcessId = new ArrayList<>(Arrays.asList(getColumnIdex(("Other").split(","))));

            binding.spinnerRole.setText("Select");
            binding.spinnerCatogory.setText("Other");
        }

        binding.txtOrg.setVisibility(View.GONE);
        binding.txtRole.setVisibility(View.GONE);
        binding.layoutOrg.setVisibility(View.GONE);
        binding.layoutRole.setVisibility(View.GONE);
        binding.rlMoreLocation.setVisibility(View.GONE);

        initViews();

        Template process = new Template();
        process.setName("Other");
        process.setMV_Process__c("Other");
        processList = new ArrayList<>();
        processList.add(process);

        if (Utills.isConnected(this)) {
            getCalendeEventsProcess();
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CalenderFliterActivity.this);
        }
    }

    public static String[] getColumnIdex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }

        return value;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        setActionbar();
        Utills.setupUI(findViewById(R.id.layout_main), this);

        preferenceHelper = new PreferenceHelper(this);
        binding.rlMoreLocation.setOnClickListener(this);
        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.spinnerCatogory.setOnClickListener(this);
        binding.etEventEndTime.setOnClickListener(this);
        binding.etEventEndTime.setFocusable(false);
        binding.etEventEndTime.setClickable(true);

        binding.etEventTime.setOnClickListener(this);
        binding.etEventTime.setFocusable(false);
        binding.etEventTime.setClickable(true);

        binding.btnSubmit.setOnClickListener(this);
        binding.spinnerOrganization.setOnItemSelectedListener(this);
        binding.tvEventAddUser.setOnClickListener(this);
        binding.tvEventAttendance.setOnClickListener(this);
        binding.etEventDate.setOnClickListener(this);
        binding.etEventDate.setFocusable(false);
        binding.etEventDate.setClickable(true);

        binding.etEventEndDate.setOnClickListener(this);
        binding.etEventEndDate.setFocusable(false);
        binding.etEventEndDate.setClickable(true);

        binding.spinnerRole.setOnClickListener(this);

        mStateList = new ArrayList<>();
        mStateList.add("Select");

        mListDistrict = new ArrayList<>();
        mListDistrict.add("Select");

        mListTaluka = new ArrayList<>();
        mListTaluka.add("Select");

        mListCluster = new ArrayList<>();
        mListCluster.add("Select");

        mListVillage = new ArrayList<>();
        mListVillage.add("Select");

        mListSchoolName = new ArrayList<>();
        mListSchoolName.add("Select");

        mListOrganization = new ArrayList<>();
        mListOrganization.add("Select");

        mListRoleName = new ArrayList<>();
        mListRoleName.add("Select");

        mStateList.clear();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.removeAll(Collections.singleton(null));

        if (mStateList.size() == 0) {
            if (Utills.isConnected(this)) {
                getState();
            }
        } else {
            mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
            mStateList.add(0, "Select");
            setSpinnerAdapter(mStateList, binding.spinnerState, selectedState);
        }

        if (Utills.isConnected(this)) {
            getOrganization();
        }

        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.add(0, "Select");
        setSpinnerAdapter(mStateList, binding.spinnerState, selectedState);

        switch (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c()) {
            case Constants.DISTRICT:
                binding.spinnerState.setEnabled(false);
                break;

            case Constants.TALUKA:
                binding.spinnerState.setEnabled(false);
                binding.spinnerDistrict.setEnabled(false);
                break;

            case Constants.CLUSTER:
                binding.spinnerState.setEnabled(false);
                binding.spinnerDistrict.setEnabled(false);
                binding.spinnerTaluka.setEnabled(false);
                break;

            case Constants.VILlAGE:
                binding.spinnerState.setEnabled(false);
                binding.spinnerDistrict.setEnabled(false);
                binding.spinnerTaluka.setEnabled(false);
                binding.spinnerCluster.setEnabled(false);
                break;

            case Constants.SCHOOL:
                binding.spinnerState.setEnabled(false);
                binding.spinnerDistrict.setEnabled(false);
                binding.spinnerTaluka.setEnabled(false);
                binding.spinnerCluster.setEnabled(false);
                binding.spinnerVillage.setEnabled(false);
                break;
        }

        setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDisrict);
        setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
        setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
        setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
        setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
        setSpinnerAdapter(mListOrganization, binding.spinnerOrganization, selectedOrganization);
    }

    private void setActionbar() {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Select Location");

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    private void getState() {
        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getState().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mStateList.clear();
                            mStateList.add("Select");

                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }

                            setSpinnerAdapter(mStateList, binding.spinnerState, selectedState);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    @Override
    public void onClick(View view) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.btn_submit:
                String msg = "";
                if (binding.spinnerCatogory.getText().equals("") || binding.spinnerCatogory.getText().equals("Select")) {
                    msg = "Please Select Category";
                } else if (binding.etEventTitle.getText().toString().equals("")) {
                    msg = "Please Enter Title";
                } else if (binding.etEventDiscription.getText().toString().equals("")) {
                    msg = "Please Enter Description";
                } else if (binding.etEventDate.getText().toString().equals("")) {
                    msg = "Please Select Start Date";
                } else if (binding.etEventEndDate.getText().toString().equals("")) {
                    msg = "Please Select End Date";
                } else if (!isDatesAreValid(binding.etEventDate.getText().toString().trim(), binding.etEventEndDate.getText().toString().trim())) {
                    msg = "Please select proper Date";
                }

                if (msg.isEmpty()) {
                    if (id == null) {
                        submitEventDetail();
                    } else {
                        updateEventDetail();
                    }
                } else {
                    Utills.showToast(msg, context);
                }
                break;

            case R.id.spinner_role:
                showrRoleDialog();
                break;

            case R.id.et_event_time:
                TimePickerDialog mTimePicker = new TimePickerDialog(context,
                        (timePicker, selectedHour, selectedMinute) -> binding.etEventTime.setText(
                                updateTime(selectedHour, selectedMinute)), hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;

            case R.id.et_event_date:
                showDateDialog(CalenderFliterActivity.this, 1);
                break;

            case R.id.et_event_end_time:
                TimePickerDialog mEndTimePicker = new TimePickerDialog(context,
                        (timePicker, selectedHour, selectedMinute) -> binding.etEventEndTime.setText(
                                updateTime(selectedHour, selectedMinute)), hour, minute, false);//Yes 24 hour time
                mEndTimePicker.setTitle("Select Time");
                mEndTimePicker.show();
                break;

            case R.id.et_event_end_date:
                showDateDialog(CalenderFliterActivity.this, 2);
                break;

            case R.id.tv_event_add_user:
                getAllFilterUser();
                break;

            case R.id.tv_event_attendance:
                setAttendance();
                break;

            case R.id.rl_more_location:
                if (binding.llLoacationlayout.isShown()) {
                    binding.llLoacationlayout.setVisibility(View.GONE);
                } else {
                    binding.llLoacationlayout.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.spinner_catogory:
                showProcessDialog();
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private boolean isDatesAreValid(String startDate, String endDate) {
        try {
            DateFormat formatter;
            Date fromDate, toDate;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            fromDate = formatter.parse(startDate);
            toDate = formatter.parse(endDate);

            if (fromDate.equals(toDate) || fromDate.before(toDate)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String updateTime(int hours, int mins) {
        String timeSet;
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12) {
            timeSet = "PM";
        } else {
            timeSet = "AM";
        }

        String minutes;
        if (mins < 10) {
            minutes = "0" + mins;
        } else {
            minutes = String.valueOf(mins);
        }

        // Append in a StringBuilder
        return String.valueOf(hours) + ':' + minutes + " " + timeSet;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    selectedState = mStateList.get(mSelectState);
                }

                mListDistrict.clear();
                mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                mListDistrict.removeAll(Collections.singleton(null));

                if (mListDistrict.size() == 0) {
                    if (Utills.isConnected(this)) {
                        getDistrict();
                    } else {
                        mListDistrict = new ArrayList<>();
                        mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                        mListDistrict.removeAll(Collections.singleton(null));
                        mListDistrict.add(0, "Select");
                        setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDisrict);
                    }
                } else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDisrict);
                }

                setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    selectedDisrict = mListDistrict.get(mSelectDistrict);

                    mListTaluka.clear();
                    mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                    mListTaluka.removeAll(Collections.singleton(null));

                    if (mListTaluka.size() == 0) {
                        if (Utills.isConnected(this)) {
                            getTaluka();
                        } else {
                            mListTaluka.clear();
                            mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                            mListTaluka.add(0, "Select");
                            mListTaluka.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                        }
                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        mListTaluka.removeAll(Collections.singleton(null));
                        setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                    }
                } else {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                }
                break;

            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);

                    mListCluster.clear();
                    mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(
                            User.getCurrentUser(context).getMvUser().getState(),
                            mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                    mListCluster.removeAll(Collections.singleton(null));

                    if (mListCluster.size() == 0) {
                        mListCluster.add(0, "Select");
                        if (Utills.isConnected(this)) {
                            getCluster();
                        } else {
                            mListCluster.clear();
                            mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(
                                    User.getCurrentUser(context).getMvUser().getState(),
                                    mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                            mListCluster.add(0, "Select");
                            mListCluster.removeAll(Collections.singleton(null));
                        }
                    } else {
                        mListCluster.add(0, "Select");
                    }
                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                }
                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);

                    mListVillage.clear();
                    mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(
                            User.getCurrentUser(context).getMvUser().getState(),
                            mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                            mListCluster.get(mSelectCluster));
                    mListVillage.removeAll(Collections.singleton(null));

                    if (mListVillage.size() == 0) {
                        if (Utills.isConnected(this)) {
                            getVillage();
                        } else {
                            mListVillage.clear();
                            mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(
                                    User.getCurrentUser(context).getMvUser().getState(),
                                    mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                                    mListCluster.get(mSelectCluster));
                            mListVillage.add(0, "Select");
                            mListVillage.removeAll(Collections.singleton(null));
                        }
                    } else {
                        mListVillage.add(0, "Select");
                    }
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                }
                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);

                    mListSchoolName.clear();
                    mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(
                            User.getCurrentUser(context).getMvUser().getState(),
                            mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                            mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                    mListSchoolName.removeAll(Collections.singleton(null));

                    if (mListSchoolName.size() == 0) {
                        if (Utills.isConnected(this)) {
                            getSchool();
                        } else {
                            mListSchoolName.clear();
                            mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(
                                    User.getCurrentUser(context).getMvUser().getState(),
                                    mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                                    mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                            mListSchoolName.add(0, "Select");
                            mListSchoolName.removeAll(Collections.singleton(null));
                        }
                    } else {
                        mListSchoolName.add(0, "Select");
                    }
                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                }
                break;

            case R.id.spinner_school_name:
                selectedSchool = mListSchoolName.get(i);
                break;

            case R.id.spinner_organization:
                mSelectOrganization = i;
                if (mSelectOrganization != 0) {
                    if (Utills.isConnected(this)) {
                        getRole();
                    }
                } else {
                    mListRoleName.add(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll());
                }
                break;

            case R.id.spinner_catogory:
                selectedCatagory = adapterView.getItemAtPosition(i).toString();
                break;
        }
    }

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
    }

    public void setSpinnerAdapter(List<String> itemList, Spinner spinner, String selectedValue) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void getDistrict() {
        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getDistrict(mStateList.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListDistrict.clear();
                    mListDistrict.add("Select");

                    JSONArray jsonArray = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListDistrict.add(jsonArray.getString(i));
                    }

                    setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDisrict);

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getRole() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/data/v36.0/query/?q=select+Id,Juridictions__c,Name+from+MV_Role__c+where+Organisation__c='"
                + mListOrganization.get(mSelectOrganization) + "'";

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListRoleName.clear();
                            mListRoleName.add("Select");

                            JSONObject obj = new JSONObject(data);
                            JSONArray jsonArray = obj.getJSONArray("records");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                mListRoleName.add(jsonObject.getString("Name"));
                            }
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getOrganization() {
        Utills.showProgressDialog(this, "Loading Organization", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.GetOrganizationUrl;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListOrganization.clear();
                            mListOrganization.add("Select");

                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListOrganization.add(jsonArray.getString(i));
                            }

                            setSpinnerAdapter(mListOrganization, binding.spinnerOrganization, selectedOrganization);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getTaluka() {
        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");

                            JSONArray jsonArr = new JSONArray(response.body().string());
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }

                            setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);

                            Intent intent = new Intent(getApplicationContext(), LocationService.class);
                            intent.putExtra(Constants.State, mStateList.get(mSelectState));
                            intent.putExtra(Constants.DISTRICT, mListDistrict.get(mSelectDistrict));
                            startService(intent);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getCluster() {
        Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getCluster(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),
                mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListCluster.clear();
                            mListCluster.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListCluster.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getVillage(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),
                mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListVillage.clear();
                            mListVillage.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListVillage.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getSchool() {
        Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),
                mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster),
                mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListSchoolName.clear();
                            mListSchoolName.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListSchoolName.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getAllFilterUser() {
        Intent intent = new Intent(CalenderFliterActivity.this, EventUserListActivity.class);
        intent.putExtra("EventID", id);
        startActivityForResult(intent, 1);
    }

    private void setAttendance() {
        Intent intent = new Intent(CalenderFliterActivity.this, EventUserAttendanceActivity.class);
        intent.putExtra("EventID", id);
        intent.putExtra("presentUser", calenderEvent.getPresent_User__c());
        startActivityForResult(intent, 2);
    }

    private void getCalendeEventsProcess() {
        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.GetCalenderEventsProcess
                + "?userId=" + User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            StringBuilder sb = new StringBuilder();
                            String prefix = "";

                            for (int i = 0; i < jsonArray.length(); i++) {
                                Template process = new Template();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                process.setName(jsonObject.getString("Name"));
                                process.setMV_Process__c(jsonObject.getString("Id"));

                                if (selectedProcessId.contains(jsonObject.getString("Id"))) {
                                    sb.append(prefix);
                                    prefix = ",";
                                    sb.append(jsonObject.getString("Name"));
                                }
                                processList.add(process);
                            }

                            if (selectedProcessId.contains("Other")) {
                                sb.append(prefix);
                                sb.append("Other");
                            }

                            binding.spinnerCatogory.setText(sb.toString());
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                ArrayList<EventUser> selectedUser = data.getParcelableArrayListExtra(Constants.PROCESS_ID);
                ArrayList<EventUser> calenderEventUserArrayList = data.getParcelableArrayListExtra(Constants.ALLUSER);
                selectedRolename = data.getStringExtra("Role");
                StringBuilder sb = new StringBuilder();
                StringBuilder sbName = new StringBuilder();
                String prefix = "";

                for (int i = 0; i < selectedUser.size(); i++) {
                    sb.append(prefix);
                    sbName.append(prefix);
                    prefix = ",";
                    sb.append(selectedUser.get(i).getUserID());
                    sbName.append(selectedUser.get(i).getUserName());
                }

                selectedUserId = sb.toString();
                selectedUserName = sbName.toString();

                if (calenderEventUserArrayList.size() == selectedUser.size()) {
                    binding.tvEventAddUser.setText("All Selected");
                } else {
                    binding.tvEventAddUser.setText(selectedUserName);
                }

                Log.e("StringId", selectedUserId);
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                ArrayList<EventUser> eventAttendanceUsers = data.getParcelableArrayListExtra("EventSelectedUsers");
                StringBuilder sb = new StringBuilder();
                String prefix = "";

                for (int i = 0; i < eventAttendanceUsers.size(); i++) {
                    sb.append(prefix);
                    prefix = ",";
                    sb.append(eventAttendanceUsers.get(i).getUserID());
                }

                SelectedEventAttendanceIDs = sb.toString();
                Log.e("StringId", SelectedEventAttendanceIDs);
            }
        }
    }

    private void showrRoleDialog() {
        final List<String> temp = mListRoleName;
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];

        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            mSelection[i] = selectedRole.contains(temp.get(i));
        }

        // arraylist to keep the selected items
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CalenderFliterActivity.this)
                .setTitle("Select ")
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (which < mSelection.length) {
                        mSelection[which] = isChecked;
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(getString(R.string.ok), (dialog12, id) -> {
                    StringBuilder sb = new StringBuilder();
                    String prefix = "";
                    for (int i = 0; i < items.length; i++) {
                        if (mSelection[i]) {
                            sb.append(prefix);
                            prefix = ",";
                            sb.append(temp.get(i));
                        }
                    }

                    selectedRolename = sb.toString();
                    binding.spinnerRole.setText(selectedRolename);
                    selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex((selectedRolename).split(","))));

                    Log.e("StringValue", selectedRolename);

                }).setNegativeButton(getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();
        dialog.show();
    }

    private void showProcessDialog() {
        final String[] items = new String[processList.size()];
        final boolean[] mSelection = new boolean[items.length];

        for (int i = 0; i < processList.size(); i++) {
            items[i] = processList.get(i).getName();
            mSelection[i] = selectedProcessId.contains(processList.get(i).getMV_Process__c());
        }

        // arraylist to keep the selected items
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CalenderFliterActivity.this)
                .setTitle("Select Category")
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (which < mSelection.length) {
                        mSelection[which] = isChecked;
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(getString(R.string.ok), (dialog12, id) -> {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder roleId = new StringBuilder();
                    String prefix = "";

                    for (int i = 0; i < items.length; i++) {
                        if (mSelection[i]) {
                            sb.append(prefix);
                            roleId.append(prefix);
                            prefix = ",";
                            sb.append(processList.get(i).getName());
                            roleId.append(processList.get(i).getMV_Process__c());
                            //now original string is changed
                        }
                    }

                    processId = roleId.toString();
                    selectedProcessId = new ArrayList<>(Arrays.asList(getColumnIdex((processId).split(","))));
                    binding.spinnerCatogory.setText(sb.toString());
                }).setNegativeButton(getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();
        dialog.show();
    }

    private void submitEventDetail() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Id", id);
                jsonObject1.put("Cluster__c", selectedCluster);
                jsonObject1.put("Date__c", binding.etEventDate.getText().toString());
                jsonObject1.put("Description_New__c", binding.etEventDiscription.getText().toString());
                jsonObject1.put("District__c", selectedDisrict);

                if (selectedUserId.equals("")) {
                    jsonObject1.put("Is_Event_for_All_Role__c", true);
                } else {
                    jsonObject1.put("Is_Event_for_All_Role__c", false);
                }

                if (selectedUserId.length() > 0) {
                    selectedUserId = selectedUserId + "," + User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();
                } else {
                    selectedUserId = User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();
                }

                jsonObject1.put("Assigned_User_Ids__c", selectedUserId);
                jsonObject1.put("Role__c", selectedRolename);
                jsonObject1.put("Status__c", binding.spinnerStatus.getSelectedItem().toString());
                jsonObject1.put("School__c", selectedSchool);

                if (processId.length() > 0) {
                    jsonObject1.put("MV_Process__c", processId);
                } else {
                    jsonObject1.put("MV_Process__c", "Other");
                }

                jsonObject1.put("State__c", selectedState);
                jsonObject1.put("Taluka__c", selectedTaluka);
                jsonObject1.put("Village__c", selectedVillage);
                jsonObject1.put("MV_User__c", User.getCurrentUser(context).getMvUser().getId());
                jsonObject1.put("category", selectedCatagory);
                jsonObject1.put("Title__c", binding.etEventTitle.getText().toString());
                jsonObject1.put("Event_Time__c", binding.etEventTime.getText().toString());

                jsonObject1.put("End_Date__c", binding.etEventEndDate.getText().toString());
                jsonObject1.put("Event_End_Time__c", binding.etEventEndTime.getText().toString());

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.InsertEventcalender_Url, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CalenderFliterActivity.this);
        }
    }

    private void updateEventDetail() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Id", id);
                jsonObject1.put("Cluster__c", selectedCluster);
                jsonObject1.put("Date__c", binding.etEventDate.getText().toString());
                jsonObject1.put("Description_New__c", binding.etEventDiscription.getText().toString());
                jsonObject1.put("District__c", selectedDisrict);

                if (selectedUserId.equals("")) {
                    jsonObject1.put("Is_Event_for_All_Role__c", true);
                } else {
                    jsonObject1.put("Is_Event_for_All_Role__c", false);
                }

                if (selectedUserId != null && !selectedUserId.equals("")) {
                    selectedUserId = selectedUserId + "," + User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();
                    jsonObject1.put("Assigned_User_Ids__c", selectedUserId);
                }

                jsonObject1.put("Present_User__c", SelectedEventAttendanceIDs);
                jsonObject1.put("Role__c", selectedRolename);
                jsonObject1.put("Status__c", binding.spinnerStatus.getSelectedItem().toString());
                jsonObject1.put("School__c", selectedSchool);

                if (processId.length() > 0) {
                    jsonObject1.put("MV_Process__c", processId);
                } else {
                    jsonObject1.put("MV_Process__c", "Other");
                }

                jsonObject1.put("State__c", selectedState);
                jsonObject1.put("Taluka__c", selectedTaluka);
                jsonObject1.put("Village__c", selectedVillage);
                jsonObject1.put("MV_User__c", User.getCurrentUser(context).getMvUser().getId());
                jsonObject1.put("category", selectedCatagory);
                jsonObject1.put("Title__c", binding.etEventTitle.getText().toString());
                jsonObject1.put("Event_Time__c", binding.etEventTime.getText().toString());

                jsonObject1.put("End_Date__c", binding.etEventEndDate.getText().toString());
                jsonObject1.put("Event_End_Time__c", binding.etEventEndTime.getText().toString());

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.UpdatetEventcalender_Url, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CalenderFliterActivity.this);
        }
    }

    @SuppressLint("SetTextI18n")
    public void showDateDialog(Context context, int type) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> {
                    if (type == 1) {
                        binding.etEventDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                        binding.etEventEndDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                    } else if (type == 2) {
                        binding.etEventEndDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    @SuppressLint("SimpleDateFormat")
    private static List<Date> getDates(String dateString1, String dateString2) {
        ArrayList<Date> dates = new ArrayList<>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
