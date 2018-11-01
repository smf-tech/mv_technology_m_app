package com.mv.Activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Model.Task;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLoactionSelectionActityBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSelectionActity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private ActivityLoactionSelectionActityBinding binding;
    private int position;
    private int mSelectState = 1;
    private int mSelectDistrict = 1;
    private int mSelectTaluka = 0;
    private int mSelectCluster = 0;
    private int mSelectVillage = 0;
    private int locationState;

    private Activity context;
    private ArrayList<Task> taskList = new ArrayList<>();
    private List<String> mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;

    private PreferenceHelper preferenceHelper;
    private Spinner selectedSpinner;

    private String selectedLocation;
    private String msg = "";
    private String locationType;
    private String taskType;
    public static String selectedState = "", selectedDistrict = "", selectedTaluka = "",
            selectedCluster = "", selectedVillage = "", selectedSchool = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loaction_selection_actity);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        binding.setActivity(this);
        preferenceHelper = new PreferenceHelper(this);

        if (getIntent().getExtras() != null && getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
            position = getIntent().getExtras().getInt(Constants.POSITION);
            taskType = getIntent().getExtras().getString(Constants.LOCATION_TYPE);

            if (taskType != null && taskType.equals("Task Location")) {
                locationType = getIntent().getExtras().getString(Constants.LOCATION);
            } else {
                locationType = preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL);
            }
            taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
        }

        initViews();
    }

    private void initViews() {
        setActionbar();
        Utills.setupUI(findViewById(R.id.layout_main), this);

        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.btnSubmit.setOnClickListener(this);

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

        binding.spinnerState.setSelection(mStateList.indexOf(selectedState));
        setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDistrict);
        setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
        setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
        setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
        setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);

        switch (locationType) {
            case "State":
                locationState = 1;
                binding.spinnerDistrict.setVisibility(View.GONE);
                binding.tvDistrict.setVisibility(View.GONE);

                binding.spinnerTaluka.setVisibility(View.GONE);
                binding.tvTaluka.setVisibility(View.GONE);

                binding.spinnerCluster.setVisibility(View.GONE);
                binding.tvCluster.setVisibility(View.GONE);

                binding.spinnerVillage.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);
                selectedSpinner = binding.spinnerState;
                selectedLocation = "State";

                binding.spinnerSchoolName.setVisibility(View.GONE);
                binding.tvSchool.setVisibility(View.GONE);
                break;

            case "District":
                locationState = 2;
                binding.spinnerTaluka.setVisibility(View.GONE);
                binding.tvTaluka.setVisibility(View.GONE);

                binding.spinnerCluster.setVisibility(View.GONE);
                binding.tvCluster.setVisibility(View.GONE);

                binding.spinnerVillage.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);
                selectedSpinner = binding.spinnerDistrict;
                selectedLocation = "District";

                binding.spinnerSchoolName.setVisibility(View.GONE);
                binding.tvSchool.setVisibility(View.GONE);
                break;

            case "Taluka":
                locationState = 3;
                binding.spinnerCluster.setVisibility(View.GONE);
                binding.tvCluster.setVisibility(View.GONE);

                binding.spinnerVillage.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);

                binding.spinnerSchoolName.setVisibility(View.GONE);
                binding.tvSchool.setVisibility(View.GONE);
                selectedSpinner = binding.spinnerTaluka;
                selectedLocation = "Taluka";
                break;

            case "Cluster":
                locationState = 4;
                binding.spinnerVillage.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);

                binding.spinnerSchoolName.setVisibility(View.GONE);
                binding.tvSchool.setVisibility(View.GONE);
                selectedSpinner = binding.spinnerCluster;
                selectedLocation = "Cluster";
                break;

            case "Village":
                locationState = 5;
                binding.spinnerSchoolName.setVisibility(View.GONE);
                binding.tvSchool.setVisibility(View.GONE);
                selectedSpinner = binding.spinnerVillage;
                selectedLocation = "Village";
                break;

            case "School":
                selectedLocation = "School";
                locationState = 6;
                selectedSpinner = binding.spinnerSchoolName;
                break;
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.btn_submit:
                if (taskType.equals("Task Location")) {
                    sendLocationTask();
                } else {
                    sendLocation();
                }
                break;
        }
    }

    private void sendLocation() {
        switch (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL)) {
            case "State":
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                locationState = 1;
                break;

            case "District":
                locationState = 2;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                break;

            case "Taluka":
                locationState = 3;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                break;

            case "Village":
                locationState = 5;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                taskList.get(4).setTask_Response__c(binding.spinnerVillage.getSelectedItem().toString());
                break;

            case "School":
                locationState = 6;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                taskList.get(4).setTask_Response__c(binding.spinnerVillage.getSelectedItem().toString());
                taskList.get(5).setTask_Response__c(binding.spinnerSchoolName.getSelectedItem().toString());
                break;
        }

        msg = "";
        for (int i = 0; i < locationState; i++) {
            if (taskList.get(i).getTask_Response__c().equals("Select")) {
                if (i == 3) {
                    continue;
                }
                msg = "Please Select " + taskList.get(i).getTask_Text__c();
                break;
            }
        }

        if (msg.isEmpty()) {
            Intent openClass = new Intent(context, ProcessDeatailActivity.class);
            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
            setResult(RESULT_OK, openClass);

            finish();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            Utills.showToast(msg, getApplicationContext());
        }
    }

    private void sendLocationTask() {
        msg = "";
        taskList.get(position).setTask_Response__c(selectedSpinner.getSelectedItem().toString());
        for (int i = 0; i < locationState; i++) {
            if (taskList.get(i).getTask_Response__c().equals("Select")) {
                msg = "Please Select " + taskList.get(i).getTask_Text__c();
                break;
            }
        }

        if (!selectedSpinner.getSelectedItem().toString().equals("Select")) {
            Intent openClass = new Intent(context, ProcessDeatailActivity.class);
            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
            setResult(RESULT_OK, openClass);
            finish();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            msg = "Please select " + selectedLocation;
            Utills.showToast(msg, getApplicationContext());
        }
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
                    if (binding.spinnerDistrict.isShown()) {
                        if (Utills.isConnected(this)) {
                            getDistrict();
                        } else {
                            mListDistrict = new ArrayList<>();
                            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                            mListDistrict.removeAll(Collections.singleton(null));
                            mListDistrict.add(0, "Select");
                            setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDistrict);
                        }
                    }
                } else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDistrict);
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
                    selectedDistrict = mListDistrict.get(mSelectDistrict);
                    mListTaluka.clear();
                    mListTaluka = AppDatabase.getAppDatabase(context).userDao()
                            .getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                    mListTaluka.removeAll(Collections.singleton(null));

                    if (mListTaluka.size() == 0) {
                        if (binding.spinnerTaluka.isShown()) {
                            if (Utills.isConnected(this)) {
                                if (mListDistrict.size() > 0) {
                                    getTaluka();
                                }
                            } else {
                                mListTaluka.clear();
                                mListTaluka = AppDatabase.getAppDatabase(context).userDao()
                                        .getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                                mListTaluka.add(0, "Select");
                                mListTaluka.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                            }
                        }
                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao()
                                .getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        mListTaluka.removeAll(Collections.singleton(null));
                        setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                    }
                } else {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                }

                setSpinnerAdapter(mListTaluka, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                break;

            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);

                    if (binding.spinnerVillage.isShown() && mListDistrict.size() > 0 && mListTaluka.size() > 0) {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao()
                                .getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListVillage.removeAll(Collections.singleton(null));

                        if (mListVillage.size() == 0) {
                            if (Utills.isConnected(this)) {
                                getVillage();
                            } else {
                                mListVillage.clear();
                                mListVillage = AppDatabase.getAppDatabase(context).userDao()
                                        .getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                                mListVillage.add(0, "Select");
                                mListVillage.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                            }
                        } else {
                            mListVillage.add(0, "Select");
                            setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                        }
                    }
                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    setSpinnerAdapter(mListCluster, binding.spinnerCluster, selectedCluster);
                }

                setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);

                    if (binding.spinnerVillage.isShown()) {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao()
                                .getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListVillage.removeAll(Collections.singleton(null));

                        if (mListVillage.size() == 0) {
                            if (Utills.isConnected(this)) {
                                getVillage();
                            } else {
                                mListVillage.clear();
                                mListVillage = AppDatabase.getAppDatabase(context).userDao()
                                        .getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                                mListVillage.add(0, "Select");
                                mListVillage.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                            }
                        } else {
                            mListVillage.add(0, "Select");
                            setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                        }
                    }
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    setSpinnerAdapter(mListVillage, binding.spinnerVillage, selectedVillage);
                }

                setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);

                    if (binding.spinnerSchoolName.isShown() && mListDistrict.size() > 0 &&
                            mListTaluka.size() > 0 && mListVillage.size() > 0) {

                        mListSchoolName.clear();
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao()
                                .getSchoolName(selectedState, mListDistrict.get(mSelectDistrict),
                                        mListTaluka.get(mSelectTaluka), mListVillage.get(mSelectVillage));
                        mListSchoolName.removeAll(Collections.singleton(null));

                        if (mListSchoolName.size() == 0) {
                            if (Utills.isConnected(this)) {
                                getSchool();
                            } else {
                                mListSchoolName.clear();
                                mListSchoolName = AppDatabase.getAppDatabase(context).userDao()
                                        .getSchoolName(selectedState, mListDistrict.get(mSelectDistrict),
                                                mListTaluka.get(mSelectTaluka), mListVillage.get(mSelectVillage));
                                mListSchoolName.add(0, "Select");
                                mListSchoolName.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                            }
                        } else {
                            mListSchoolName.add(0, "Select");
                            setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                        }
                    }
                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                    setSpinnerAdapter(mListSchoolName, binding.spinnerSchoolName, selectedSchool);
                }
                break;

            case R.id.spinner_school_name:
                if (mListSchoolName.size() > 0) {
                    selectedSchool = mListSchoolName.get(i);
				}
                break;
        }
    }

    private void setSpinnerAdapter(List<String> itemList, Spinner spinner, String selectedValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0) {
            spinner.setSelection(itemList.indexOf(selectedValue));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void getState() {
        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getState("structure").enqueue(new Callback<ResponseBody>() {

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

    private void getDistrict() {
        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getDistrict(mStateList.get(mSelectState), "structure").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListDistrict.clear();
                            mListDistrict.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListDistrict.add(jsonArr.getString(i));
                            }

                            setSpinnerAdapter(mListDistrict, binding.spinnerDistrict, selectedDistrict);
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

    private void getTaluka() {
        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),
                "structure").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
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

    public void getCluster() {
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
                mListTaluka.get(mSelectTaluka), "structure").enqueue(new Callback<ResponseBody>() {

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
}