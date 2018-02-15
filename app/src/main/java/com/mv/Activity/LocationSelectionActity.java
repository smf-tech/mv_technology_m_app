package com.mv.Activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Model.Task;
import com.mv.Model.User;
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

public class LocationSelectionActity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private ActivityLoactionSelectionActityBinding binding;
    int position;
    private String locationType;
    private String taskType;
    private int mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;

    private ArrayAdapter<String> district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, state_adapter, organization_adapter;
    private PreferenceHelper preferenceHelper;
    private RelativeLayout mToolBar;
    private Spinner selectedSpinner;
    private String selectedLocation;
    String msg = "";
    private int locationState;

    public static String selectedState = "", selectedDisrict = "", selectedTaluka = "", selectedCluster = "", selectedVillage = "", selectedSchool = "";


    ArrayList<Task> taskList = new ArrayList<>();

    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loaction_selection_actity);
        binding.setActivity(this);
        preferenceHelper = new PreferenceHelper(this);

        if (getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
            position = getIntent().getExtras().getInt(Constants.POSITION);
           taskType= getIntent().getExtras().getString(Constants.LOCATION_TYPE);
           if(taskType.equals("Task Location"))
            locationType = getIntent().getExtras().getString(Constants.LOCATION);
           else
           {
               locationType=preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL);
           }
            taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
        }
        initViews();
    }


    private void initViews() {
        setActionbar("Select Location");
        Utills.setupUI(findViewById(R.id.layout_main), this);

        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.btnSubmit.setOnClickListener(this);

        mStateList = new ArrayList<String>();
        mStateList.add("Select");
        //mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        mListDistrict = new ArrayList<String>();
        mListDistrict.add("Select");

        mListTaluka = new ArrayList<String>();
        mListTaluka.add("Select");
        mListCluster = new ArrayList<String>();
        mListCluster.add("Select");
        mListVillage = new ArrayList<String>();
        mListVillage.add("Select");

        mListSchoolName = new ArrayList<String>();
        mListSchoolName.add("Select");


      /*  if (Utills.isConnected(this))
            getDistrict();
        else {

            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
            mListDistrict.add(0, "Select");
        }
        */
      /*  state_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStateList);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(state_adapter);*/
        mStateList.clear();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.removeAll(Collections.singleton(null));
        if (mStateList.size() == 0) {
            if (Utills.isConnected(this))
                getState();
        }
        else {
            mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
            mStateList.add(0, "Select");
            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);
            //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        }
        //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        binding.spinnerState.setSelection(mStateList.indexOf(selectedState));
        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);
        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
        setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
        setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
        setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


        if (locationType.equals("State")) {
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
            selectedLocation="State";

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
        } else if (locationType.equals("District")) {
            locationState = 2;
            binding.spinnerTaluka.setVisibility(View.GONE);
            binding.tvTaluka.setVisibility(View.GONE);

            binding.spinnerCluster.setVisibility(View.GONE);
            binding.tvCluster.setVisibility(View.GONE);

            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);
            selectedSpinner = binding.spinnerDistrict;
            selectedLocation="District";
            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
        } else if (locationType.equals("Taluka")) {
            locationState = 3;
            binding.spinnerCluster.setVisibility(View.GONE);
            binding.tvCluster.setVisibility(View.GONE);

            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner = binding.spinnerTaluka;
            selectedLocation="Taluka";
        } else if (locationType.equals("Cluster")) {
            locationState = 4;
            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner = binding.spinnerCluster;
            selectedLocation="Cluster";
        } else if (locationType.equals("Village")) {
            locationState = 5;
            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner = binding.spinnerVillage;
            selectedLocation="Village";
            /*if (task.getTask_Response__c() != null)
                holder.spinnerResponse.setSelection(myList.indexOf(task.getTask_Response__c().trim()));*/
        } else if (locationType.equals("School")) {
            selectedLocation="School";
            locationState = 6;
            selectedSpinner = binding.spinnerSchoolName;
        }


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

                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:


                if(taskType.equals("Task Location"))
                sendLocationTask();
                else
                {
                    sendLocation();
                }
                // sendData();
                break;

        }
    }


    private void sendLocation() {

            if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("State")) {
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                locationState = 1;
            } else if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("District")) {
                locationState = 2;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
            } else if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("Taluka")) {
                locationState = 3;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
            } else if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("Cluster")) {
                locationState = 4;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                taskList.get(3).setTask_Response__c(binding.spinnerCluster.getSelectedItem().toString());

            } else if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("Village")) {
                locationState = 5;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                taskList.get(3).setTask_Response__c(binding.spinnerCluster.getSelectedItem().toString());
                taskList.get(4).setTask_Response__c(binding.spinnerVillage.getSelectedItem().toString());

            } else if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equals("School")) {
                locationState = 6;
                taskList.get(0).setTask_Response__c(binding.spinnerState.getSelectedItem().toString());
                taskList.get(1).setTask_Response__c(binding.spinnerDistrict.getSelectedItem().toString());
                taskList.get(2).setTask_Response__c(binding.spinnerTaluka.getSelectedItem().toString());
                taskList.get(3).setTask_Response__c(binding.spinnerCluster.getSelectedItem().toString());
                taskList.get(4).setTask_Response__c(binding.spinnerVillage.getSelectedItem().toString());
                taskList.get(5).setTask_Response__c(binding.spinnerSchoolName.getSelectedItem().toString());
            }
        msg = "";
      //  taskList.get(position).setTask_Response__c(selectedSpinner.getSelectedItem().toString());
        for (int i = 0; i < locationState; i++) {
            if (taskList.get(i).getTask_Response__c().equals("Select")) {
                msg = "Please Select " + taskList.get(i).getTask_Text__c();
                break;
            }
        }
        if (msg.isEmpty()) {
            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
            Intent openClass = new Intent(context, ProcessDeatailActivity.class);
            // openClass.putExtra(Constants.PROCESS_ID, taskList);
            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
            setResult(RESULT_OK,openClass);
            //  startActivity(openClass);
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
            // openClass.putExtra(Constants.PROCESS_ID, dashaBoardListModel);
            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
            setResult(RESULT_OK, openClass);
            //  startActivity(openClass);
            finish();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            msg="Please select "+selectedLocation;
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
                        if (Utills.isConnected(this))
                            getDistrict();
                        else {
                            mListDistrict = new ArrayList<>();
                            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                            mListDistrict.removeAll(Collections.singleton(null));
                            mListDistrict.add(0, "Select");
                            setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                        }
                    }
                }
                else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                }
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


                //    mListDistrict.clear();

                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    selectedDisrict = mListDistrict.get(mSelectDistrict);
            /*        selectedTaluka="";
                    selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    mListTaluka.clear();
                    mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                    mListTaluka.removeAll(Collections.singleton(null));
                    if (mListTaluka.size() == 0) {
                        if (binding.spinnerTaluka.isShown()) {
                            if (Utills.isConnected(this))
                                getTaluka();
                            else {
                                mListTaluka.clear();
                                mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                                mListTaluka.add(0, "Select");
                                mListTaluka.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                            }
                        }
                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        mListTaluka.removeAll(Collections.singleton(null));
                        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                    }
                } else {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                }

                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                break;


            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);
            /*        selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    if (binding.spinnerCluster.isShown()) {

                        mListCluster.clear();
                        mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListCluster.removeAll(Collections.singleton(null));
                        if (mListCluster.size() == 0) {
                            mListCluster.add(0, "Select");
                            if (Utills.isConnected(this))
                                getCluster();
                            else {

                                mListCluster.clear();
                                mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                                mListCluster.add(0, "Select");
                                mListCluster.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                            }
                        } else {
                            mListCluster.add(0, "Select");
                            setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                        }
                    }
                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                }
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);
/*                    selectedVillage="";
                    selectedSchool="";*/
                    if (binding.spinnerVillage.isShown()) {

                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                        mListVillage.removeAll(Collections.singleton(null));
                        if(mListVillage.size()==0) {
                            if (Utills.isConnected(this))
                                getVillage();
                            else {
                                mListVillage.clear();
                                mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                                mListVillage.add(0, "Select");
                                mListVillage.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                            }
                        }
                        else {
                            mListVillage.add(0, "Select");
                            setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                        }
                    }
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                }

                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);
                    // selectedSchool="";
                    if (binding.spinnerSchoolName.isShown()) {
                        mListSchoolName.clear();
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                        mListSchoolName.removeAll(Collections.singleton(null));
                        if (mListSchoolName.size() == 0) {
                            if (Utills.isConnected(this))
                                getSchool();
                            else {
                                mListSchoolName.clear();
                                mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                                mListSchoolName.add(0, "Select");
                                mListSchoolName.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                            }
                        } else {
                            mListSchoolName.add(0, "Select");
                            setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                        }
                    }
                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                    setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


                }
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                selectedSchool = mListSchoolName.get(mSelectSchoolName);
                break;
        }
    }

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)

            spinner.setSelection(itemList.indexOf(selectedValue));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String abc;

    }


    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mStateList.clear();
                            mStateList.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);

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

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mStateList.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListDistrict.add(jsonArray.getString(i));
                    }
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

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
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListTaluka.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    // add infos for the service which file to download and where to store
                    intent.putExtra(Constants.State,mStateList.get(mSelectState));
                    intent.putExtra(Constants.DISTRICT,mListDistrict.get(mSelectDistrict));
                    startService(intent);
                    // taluka_adapter.notifyDataSetChanged();
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
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getCluster(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListCluster.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
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
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListVillage.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
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
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");

                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListSchoolName.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
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
