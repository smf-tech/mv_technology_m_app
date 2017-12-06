package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLoactionSelectionActityBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;

public class LocationSelectionActity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private ActivityLoactionSelectionActityBinding binding;
    int position;
    private String locationType;
    private int  mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;

    private ArrayAdapter<String> district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, state_adapter, organization_adapter;
    private PreferenceHelper preferenceHelper;
    private RelativeLayout mToolBar;
    private Spinner selectedSpinner;
    String msg = "";
    private int locationState;
    public static String selectedState,selectedDisrict,selectedTaluka,selectedCluster,selectedVillage,selectedSchool;


    ArrayList<Task> taskList = new ArrayList<>();

    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loaction_selection_actity);
        binding.setActivity(this);
        if (getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
                position=getIntent().getExtras().getInt(Constants.POSITION);
                locationType=getIntent().getExtras().getString(Constants.LOCATION);
            taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
        }

        initViews();


    }




    private void initViews() {
        setActionbar("Select Location");
        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);

        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.btnSubmit.setOnClickListener(this);


        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();
        mListCluster = new ArrayList<String>();
        mListVillage = new ArrayList<String>();
        mListSchoolName = new ArrayList<String>();

        mStateList = new ArrayList<String>();
        mListDistrict.add(User.getCurrentUser(context).getDistrict());
        mListTaluka.add("Select");
        mListCluster.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");
        if (Utills.isConnected(this))
            getDistrict();
        else {

            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(User.getCurrentUser(context).getState());
            mListDistrict.add(0, "Select");
        }


        mStateList = new ArrayList<String>(Arrays.asList(getColumnIdex((User.getCurrentUser(getApplicationContext()).getState()).split(","))));
        mStateList.add(0, "Select");
        state_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mStateList);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(state_adapter);


        district_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTaluka.setAdapter(taluka_adapter);

        cluster_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListCluster);
        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCluster.setAdapter(cluster_adapter);

        village_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListVillage);
        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVillage.setAdapter(village_adapter);

        school_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListSchoolName);
        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSchoolName.setAdapter(school_adapter);

        if (locationType.equals("State")) {
            locationState=1;
            binding.spinnerDistrict.setVisibility(View.GONE);
            binding.tvDistrict.setVisibility(View.GONE);

            binding.spinnerTaluka.setVisibility(View.GONE);
            binding.tvTaluka.setVisibility(View.GONE);

            binding.spinnerCluster.setVisibility(View.GONE);
            binding.tvCluster.setVisibility(View.GONE);

            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);
            selectedSpinner=binding.spinnerState;

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
        } else if (locationType.equals("District")) {
            locationState=2;
            binding.spinnerTaluka.setVisibility(View.GONE);
            binding.tvTaluka.setVisibility(View.GONE);

            binding.spinnerCluster.setVisibility(View.GONE);
            binding.tvCluster.setVisibility(View.GONE);

            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);
            selectedSpinner=binding.spinnerDistrict;

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
        } else if (locationType.equals("Taluka")) {
            locationState=3;
            binding.spinnerCluster.setVisibility(View.GONE);
            binding.tvCluster.setVisibility(View.GONE);

            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner=binding.spinnerTaluka;
        } else if (locationType.equals("Cluster")) {
            locationState=4;
            binding.spinnerVillage.setVisibility(View.GONE);
            binding.tvVillage.setVisibility(View.GONE);

            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner=binding.spinnerCluster;
        } else if (locationType.equals("Village")) {
            locationState=5;
            binding.spinnerSchoolName.setVisibility(View.GONE);
            binding.tvSchool.setVisibility(View.GONE);
            selectedSpinner=binding.spinnerVillage;
            /*if (task.getTask_Response__c() != null)
                holder.spinnerResponse.setSelection(myList.indexOf(task.getTask_Response__c().trim()));*/
        } else if (locationType.equals("School")) {
            locationState=6;
            selectedSpinner=binding.spinnerSchoolName;
        }


    }

    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getApplicationContext()).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getStateName";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mStateList.clear();
                    mStateList.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mStateList.add(jsonArray.getString(i));
                    }
                    state_adapter.notifyDataSetChanged();

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

    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

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

                sendLocation();

                // sendData();
                break;

        }
    }

    private void sendLocation() {

        msg = "";
        taskList.get(position).setTask_Response__c(selectedSpinner.getSelectedItem().toString());
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




    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;

                if (mSelectState != 0) {
                    if (Utills.isConnected(this))
                        getDistrict();
                    else {

                        mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(User.getCurrentUser(context).getState());
                        mListDistrict.add(0, "Select");
                        district_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListDistrict);
                        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerDistrict.setAdapter(district_adapter);

                    }

                    //    mListDistrict.clear();

                }
                else
                {
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                }
                mListTaluka.clear();
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
         ;
                mListTaluka.add("Select");
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                district_adapter.notifyDataSetChanged();
                taluka_adapter.notifyDataSetChanged();
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    selectedDisrict=mListDistrict.get(mSelectDistrict);
                    if (Utills.isConnected(this))
                    getTaluka();
                    else
                    {


                        mListTaluka.clear();

                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(User.getCurrentUser(context).getState(), mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        taluka_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListTaluka);
                        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerTaluka.setAdapter(taluka_adapter);



                    }

                }
                else
                {
                    mListTaluka.clear();
                    mListTaluka.add("Select");

                }

                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                taluka_adapter.notifyDataSetChanged();
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka= mListTaluka.get(mSelectTaluka);
                    if (Utills.isConnected(this))
                        getCluster();
                    else {

                        mListCluster.clear();
                        mListCluster.add("Select");
                        mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListCluster.add(0, "Select");
                        cluster_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListCluster);
                        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCluster.setAdapter(cluster_adapter);

                    }
                }
                else
                {
                    mListCluster.clear();
                    mListCluster.add("Select");
                }
                //  mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                //  mListTaluka.add("Select");
                // mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");

                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster=mListCluster.get(mSelectCluster);
                    if (Utills.isConnected(this))
                        getVillage();
                    else {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                        mListVillage.add(0, "Select");
                        village_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListVillage);
                        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerVillage.setAdapter(village_adapter);

                    }
                    //   getVillage();
                }
                else
                {
                    mListVillage.clear();
                    mListVillage.add("Select");
                }

                //  mListVillage.clear();
                mListSchoolName.clear();

                //  mListVillage.add("Select");
                mListSchoolName.add("Select");
                //  village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage= mListVillage.get(mSelectVillage);
                    if (Utills.isConnected(this))
                        getSchool();
                    else {
                        mListSchoolName.clear();
                        mListSchoolName.add("Select");
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                        mListSchoolName.add(0, "Select");
                        school_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListSchoolName);
                        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerSchoolName.setAdapter(school_adapter);

                    }
                }
               /* mListSchoolName.clear();
                mListSchoolName.add("Select");*/
                //   school_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                selectedSchool= mListSchoolName.get(mSelectSchoolName);
                break;
        }
    }

    private void getCluster() {
        Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getCluster(User.getCurrentUser(LocationSelectionActity.this).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
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
                    cluster_adapter.notifyDataSetChanged();
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
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(User.getCurrentUser(getApplicationContext()).getState(), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
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
                    taluka_adapter.notifyDataSetChanged();
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
        apiService.getVillage(User.getCurrentUser(LocationSelectionActity.this).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {
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
                    village_adapter.notifyDataSetChanged();
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

        apiService.getSchool(User.getCurrentUser(LocationSelectionActity.this).getState(),  mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {
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
                    school_adapter.notifyDataSetChanged();
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
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(User.getCurrentUser(getApplicationContext()).getState()).enqueue(new Callback<ResponseBody>() {
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
                    district_adapter.notifyDataSetChanged();

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
}
