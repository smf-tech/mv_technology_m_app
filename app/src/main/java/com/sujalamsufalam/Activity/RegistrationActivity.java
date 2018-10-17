package com.sujalamsufalam.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.GPSTracker;
import com.sujalamsufalam.Utils.LocaleManager;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.Widgets.MyEditTextView;
import com.sujalamsufalam.databinding.ActivityRegistrationBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private Button btn_submit;
    private ActivityRegistrationBinding binding;
    private EditText edit_text_midle_name, edit_text_last_name, edit_text_name, edit_text_mobile_number, edit_text_school_code, edit_text_email;
    private Spinner spinner_project, spinner_organization, spinner_role, spinner_state, spinner_district, spinner_taluka, spinner_cluster, spinner_village, spinner_school_name;
    private int mSelectProject = 0, mSelectOrganization = 0, mSelectRole = 0, mSelectState = 0, mSelectDistrict = 0, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private ArrayList<String> mListProjectId, mListProject, mListOrganization, mListState, mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName;
    private List<String> mListRoleName, mListCode, mListRoleId, mListRoleJuridiction;
    private TextView txt_district, txt_taluka, txt_cluster, txt_village, txt_school;
    private TextInputLayout input_school_code;
    private ArrayAdapter<String> state_adapter, district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, role_adapter, organization_adapter, project_adapter;
    private PreferenceHelper preferenceHelper;
    private User user;
    String SelectedLat = "", SelectedLon = "";
    private String mGenderSelect = "";
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private String imageFilePath;
    private Boolean isAdd;
    private GPSTracker gps;
    private boolean isMultipleTalukatSet = false, isProjectSet = false, isOrganizationSet = false, isStateSet = false, isDistrictSet = false, isTalukaSet = false, isClusterSet = false, isVillageSet = false, isSchoolSet = false, isRollSet = false;
    private RadioGroup radioGroup;
    private RelativeLayout rel_district, rel_taluka, rel_cluster, rel_villgae, rel_school_name;
    private boolean isBackPress = false;
    private boolean[] mSelection = null;
    private String value = "",valueProject = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration);
        binding.setActivity(this);
        gps = new GPSTracker(RegistrationActivity.this);
        initViews();
        if (Utills.isConnected(this)) {
            getState();
            getOrganization();
            getProject();
        } else {
            showPopUp();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
    }

    /*
    * API to get all Projects From server
    * */
    private void getProject() {
        if (!isBackPress)
            Utills.showProgressDialog(this, "Loading Projects", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        //updated project fetch api. getting projects of selected org.
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetProjectDataUrl+"?org="+mListOrganization.get(mSelectOrganization);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {

                            JSONArray jsonArray = null;
                            jsonArray = new JSONArray(data);
                            mListProject.clear();
                            mListProjectId.clear();
                            mListProject.add("Select");
                            mListProjectId.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
//                                mListProject.add(object.getString("Project_Name__c"));
                                mListProject.add(object.getString("Name"));
                                mListProjectId.add(object.getString("Id"));
                            }
                            project_adapter.notifyDataSetChanged();
                            if (!isAdd && !isProjectSet) {
                                isProjectSet = true;
                                for (int i = 0; i < mListProject.size(); i++) {
                                    if (mListProject.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getProject_Name__c())) {
                                        spinner_project.setSelection(i);
                                        break;

                                    }
                                }
                            }

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

    /*
    * API to get all Roles From server
    * */
    private void getRole() {
        if (!isBackPress)
            Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.MV_RoleUrl + mListOrganization.get(mSelectOrganization) + "'";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {


                            JSONObject obj = new JSONObject(data);
                            JSONArray jsonArray = obj.getJSONArray("records");
                            mListRoleName.clear();
                            mListRoleId.clear();
                            mListRoleJuridiction.clear();
                            mListRoleName.add("Select");
                            mListRoleId.add("");
                            mListRoleJuridiction.add("");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                mListRoleName.add(jsonObject.getString("Name"));
                                mListRoleId.add(jsonObject.getString("Id"));
                                mListRoleJuridiction.add(jsonObject.getString("Juridictions__c"));
                            }
                            role_adapter.notifyDataSetChanged();
                            if (!isAdd && !isRollSet) {
                                isRollSet = true;
                                for (int i = 0; i < mListRoleName.size(); i++) {
                                    if (mListRoleName.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getRoll())) {
                                        binding.spinnerRole.setSelection(i);
                                        break;
                                    }
                                }
                            }

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


    /*
    * API to get all Organization From server
    * */
    private void getOrganization() {
        if (!isBackPress)
            Utills.showProgressDialog(this, "Loading Organization", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetOrganizationUrl;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListOrganization.clear();
                            mListOrganization.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListOrganization.add(jsonArray.getString(i));
                            }
                            organization_adapter.notifyDataSetChanged();
                            if (!isAdd && !isOrganizationSet) {
                                isOrganizationSet = true;
                                for (int i = 0; i < mListOrganization.size(); i++) {
                                    if (mListOrganization.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getOrganisation())) {
                                        spinner_organization.setSelection(i);
                                        break;
                                    }
                                }
                            }
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


    /*
    * check internet connectivity and show pop up for no internet connection
    * */
    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_launcher);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    /*
    * API to get all States From server
    * */
    private void getState() {
        if (!isBackPress)
            Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState("structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListState.clear();
                            mListState.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListState.add(jsonArray.getString(i));
                            }
                            state_adapter.notifyDataSetChanged();
                            if (!isAdd && !isStateSet) {
                                isStateSet = true;
                                for (int i = 0; i < mListState.size(); i++) {
                                    if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getState())) {
                                        binding.spinnerState.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    /*
    * API to get all Districts From server
    * */
    private void getDistrict() {
        if (!isBackPress)
            Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mListState.get(mSelectState),"structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListDistrict.clear();
                            mListDistrict.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }
                            district_adapter.notifyDataSetChanged();
                            if (!isAdd && !isDistrictSet) {
                                isDistrictSet = true;
                                for (int i = 0; i < mListDistrict.size(); i++) {
                                    if (mListDistrict.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getDistrict())) {
                                        binding.spinnerDistrict.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    /*
    * initialize all views
    * */
    private void initViews() {

        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);
        binding.btnRefreshLocation.setOnClickListener(this);

        radioGroup = (RadioGroup) findViewById(R.id.gender_group);
        binding.birthDate.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.gender_male)
                    mGenderSelect = "Male";
                else if (checkedId == R.id.gender_female)
                    mGenderSelect = "Female";
                else if (checkedId == R.id.gender_other)
                    mGenderSelect = "Other";
            }
        });

        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        edit_text_midle_name = (MyEditTextView) findViewById(R.id.edit_text_midle_name);
        edit_text_last_name = (EditText) findViewById(R.id.edit_text_last_name);
        edit_text_mobile_number = (EditText) findViewById(R.id.edit_text_mobile_number);
        edit_text_mobile_number.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getPhone());
        binding.birthDate.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getBirth_Day__c());
        edit_text_email = (EditText) findViewById(R.id.edit_text_email);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        binding.editMultiselectTaluka.setOnClickListener(this);
        binding.editMultiselectProject.setOnClickListener(this);
        if (User.getCurrentUser(RegistrationActivity.this).getMvUser().getOrganisation().equals("SMF")) {
            binding.llWork.setVisibility(View.VISIBLE);
            binding.inputAddress.setVisibility(View.VISIBLE);
            binding.editTextRefresh.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lat() + " , " + User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lng());
            SelectedLon = User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lng();
            SelectedLat = User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lat();
            binding.editTextAddress.setText(ConvertToAddress(Double.parseDouble(SelectedLon), Double.parseDouble(SelectedLat)));
        } else {
            binding.llWork.setVisibility(View.GONE);
            binding.inputAddress.setVisibility(View.GONE);
        }

        binding.btnRefreshLocation.setOnClickListener(this);
        spinner_role = (Spinner) findViewById(R.id.spinner_role);
        spinner_state = (Spinner) findViewById(R.id.spinner_state);
        spinner_district = (Spinner) findViewById(R.id.spinner_district);
        spinner_taluka = (Spinner) findViewById(R.id.spinner_taluka);
        spinner_cluster = (Spinner) findViewById(R.id.spinner_cluster);
        spinner_village = (Spinner) findViewById(R.id.spinner_village);
        spinner_school_name = (Spinner) findViewById(R.id.spinner_school_name);
        spinner_organization = (Spinner) findViewById(R.id.spinner_organization);
        spinner_project = (Spinner) findViewById(R.id.spinner_project);

        rel_district = (RelativeLayout) findViewById(R.id.rel_district);
        rel_taluka = (RelativeLayout) findViewById(R.id.rel_taluka);
        rel_cluster = (RelativeLayout) findViewById(R.id.rel_cluster);
        rel_villgae = (RelativeLayout) findViewById(R.id.rel_villgae);
        rel_school_name = (RelativeLayout) findViewById(R.id.rel_school_name);

        spinner_project.setOnItemSelectedListener(this);
        spinner_organization.setOnItemSelectedListener(this);
        spinner_role.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);
        spinner_district.setOnItemSelectedListener(this);
        spinner_taluka.setOnItemSelectedListener(this);
        spinner_cluster.setOnItemSelectedListener(this);
        spinner_village.setOnItemSelectedListener(this);
        spinner_school_name.setOnItemSelectedListener(this);

        mListState = new ArrayList<String>();
        mListRoleName = new ArrayList<String>();
        mListRoleId = new ArrayList<String>();
        mListRoleJuridiction = new ArrayList<String>();
        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();
        mListCluster = new ArrayList<String>();
        mListVillage = new ArrayList<String>();
        mListSchoolName = new ArrayList<String>();
        mListCode = new ArrayList<String>();
        mListOrganization = new ArrayList<String>();
        mListProject = new ArrayList<String>();
        mListProjectId = new ArrayList<String>();

        mListState.add("Select");
        mListDistrict.add("Select");
        mListTaluka.add("Select");
        mListCluster.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");
        mListCode.add("Select");
        mListOrganization.add("Select");
        mListRoleName.add("Select");
        mListProject.add("Select");
        mListProjectId.add("Select");

        input_school_code = (TextInputLayout) findViewById(R.id.input_school_code);
        edit_text_school_code = (EditText) findViewById(R.id.edit_text_school_code);

        project_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListProject);
        project_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_project.setAdapter(project_adapter);

        organization_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListOrganization);
        organization_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_organization.setAdapter(organization_adapter);

        role_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListRoleName);
        role_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_role.setAdapter(role_adapter);


        txt_district = (TextView) findViewById(R.id.txt_district);
        txt_taluka = (TextView) findViewById(R.id.txt_taluka);
        txt_cluster = (TextView) findViewById(R.id.txt_cluster);
        txt_village = (TextView) findViewById(R.id.txt_village);
        txt_school = (TextView) findViewById(R.id.txt_school);

        state_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListState);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(state_adapter);

        district_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_district.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_taluka.setAdapter(taluka_adapter);

        cluster_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListCluster);
        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_cluster.setAdapter(cluster_adapter);

        village_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListVillage);
        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_village.setAdapter(village_adapter);

        school_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListSchoolName);
        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_school_name.setAdapter(school_adapter);

        if (getIntent() != null) {

            if (getIntent().getStringExtra(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_EDIT)) {
                setActionbar(getString(R.string.update_profile));
                isAdd = false;
                binding.editTextMidleName.setText(User.getCurrentUser(this).getMvUser().getMiddleName());
                binding.editTextLastName.setText(User.getCurrentUser(this).getMvUser().getLastName());
                binding.editTextMobileNumber.setText(User.getCurrentUser(this).getMvUser().getPhone());
                binding.editTextName.setText(User.getCurrentUser(this).getMvUser().getName());
                binding.editMultiselectTaluka.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getMultipleTaluka());
                binding.editMultiselectProject.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getMulti_project__c());
                binding.editTextAddress.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getUser_Address__c());


                if (User.getCurrentUser(this).getMvUser().getGender() != null && !TextUtils.isEmpty(User.getCurrentUser(this).getMvUser().getGender())) {
                    if (User.getCurrentUser(this).getMvUser().getGender().equalsIgnoreCase("Male")) {
                        radioGroup.check(R.id.gender_male);
                        mGenderSelect = "Male";
                    } else if (User.getCurrentUser(this).getMvUser().getGender().equalsIgnoreCase("Female")) {
                        radioGroup.check(R.id.gender_female);
                        mGenderSelect = "Female";
                    } else if (User.getCurrentUser(this).getMvUser().getGender().equalsIgnoreCase("Other")) {
                        radioGroup.check(R.id.gender_other);
                        mGenderSelect = "Other";
                    }
                }

                if (User.getCurrentUser(this).getMvUser().getImageId() != null && !(User.getCurrentUser(this).getMvUser().getImageId().equalsIgnoreCase("null"))) {
                    Glide.with(this)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + User.getCurrentUser(this).getMvUser().getImageId() + "/Body"))
                            .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                            .into(binding.addImage);
                }
                if (!(TextUtils.isEmpty(User.getCurrentUser(this).getMvUser().getEmail()) || User.getCurrentUser(this).getMvUser().getEmail().equalsIgnoreCase("null")))
                    binding.editTextEmail.setText(User.getCurrentUser(this).getMvUser().getEmail());
            } else {
                isAdd = true;
                setActionbar(getString(R.string.Registration));
            }
        }

    }

    /*
    * add required header to glide
    * */
    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    /*
    * set actionbar to activity
    * */
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
                Log.i("Dialog", "Back pressed");
                isBackPress = true;
                Utills.hideProgressDialog();
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.edit_multiselect_taluka:
                showMultiselectDialog(mListTaluka);
                break;
            case R.id.edit_multiselect_project:
                showMultiselectDialogProject(mListProject);
                break;
            case R.id.btn_submit:
                sendData();
                break;
            case R.id.btn_refresh_location:
                binding.btnRefreshLocation.setImageResource(0);//It will remove the image resource so animation will be clearly visible
                binding.btnRefreshLocation.setBackgroundResource(R.drawable.work_location_progress);
                AnimationDrawable rocketAnimation = (AnimationDrawable) binding.btnRefreshLocation.getBackground();
                rocketAnimation.start();

                gps = new GPSTracker(RegistrationActivity.this);
                binding.editTextRefresh.setText(gps.getLatitude() + " , " + gps.getLongitude());
                SelectedLat = String.valueOf(gps.getLatitude());
                SelectedLon = String.valueOf(gps.getLongitude());
                binding.editTextAddress.setText(ConvertToAddress(gps.getLatitude(), gps.getLongitude()));
                break;
            case R.id.birth_date:
                showDateDialog(RegistrationActivity.this, binding.birthDate);
                break;

        }
    }

    // to convert the gps coordinate into address
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
        }catch (IllegalArgumentException e) {
            Log.e("Error", "Unable connect to Geocoder", e);
            return "";
        }
        return result;
    }

    /*
    * send all data to server
    * */
    private void sendData() {
        if (isValidate()) {
            if (!isBackPress)
                Utills.showProgressDialog(this);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            user = new User();
            user.getMvUser().setName(edit_text_name.getText().toString().trim());
            user.getMvUser().setEmail(edit_text_email.getText().toString().trim());
            user.getMvUser().setPhone(edit_text_mobile_number.getText().toString().trim());
            user.getMvUser().setRoll(mListRoleName.get(mSelectRole));
            user.getMvUser().setCluster(mListCluster.get(mSelectCluster));
            user.getMvUser().setDistrict(mListDistrict.get(mSelectDistrict));
            user.getMvUser().setTaluka(mListTaluka.get(mSelectTaluka));
            user.getMvUser().setVillage(mListVillage.get(mSelectVillage));
            user.getMvUser().setSchool_Code(edit_text_school_code.getText().toString().trim());
            user.getMvUser().setSchool_Name(mListSchoolName.get(mSelectSchoolName));
            user.getMvUser().setMultipleTaluka(value);
            JSONObject jsonObject1 = new JSONObject();
            JSONObject jsonObject2 = new JSONObject();
            try {

                jsonObject2.put("Id", User.getCurrentUser(RegistrationActivity.this).getMvUser().getId());
                jsonObject2.put("Name", edit_text_name.getText().toString().trim());
                jsonObject2.put("User_Email__c", edit_text_email.getText().toString().trim());
                jsonObject2.put("User_Mobile_No__c", edit_text_mobile_number.getText().toString().trim());
                jsonObject2.put("MV_Role__c", mListRoleId.get(mSelectRole));
                jsonObject2.put("Role_Name__c", mListRoleName.get(mSelectRole));
                jsonObject2.put("Last_Name__c", edit_text_last_name.getText().toString().trim());
                jsonObject2.put("Middle_Name__c", edit_text_midle_name.getText().toString().trim());
                if (binding.birthDate.getText().toString().trim().length() > 0)
                    jsonObject2.put("Birth_Day__c", binding.birthDate.getText().toString().trim());
                    jsonObject2.put("User_State__c", mListState.get(mSelectState));
                    jsonObject2.put("Gender__c", mGenderSelect);
                    jsonObject2.put("Attendance_Loc__Longitude__s", SelectedLon);
                    jsonObject2.put("Attendance_Loc__Latitude__s", SelectedLat);
                if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("School")) {
                    jsonObject2.put("User_Cluster__c", mListCluster.get(mSelectCluster));
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                    jsonObject2.put("User_Village__c", mListVillage.get(mSelectVillage));
                    jsonObject2.put("UserSchoolName__c", mListSchoolName.get(mSelectSchoolName));
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Village")) {
                    jsonObject2.put("User_Cluster__c", mListCluster.get(mSelectCluster));
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                    jsonObject2.put("User_Village__c", mListVillage.get(mSelectVillage));
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Cluster")) {
                    jsonObject2.put("User_Cluster__c", mListCluster.get(mSelectCluster));
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                    jsonObject2.put("User_Village__c", "Select");
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Taluka")) {
                    jsonObject2.put("User_Cluster__c", "Select");
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                    jsonObject2.put("User_Village__c", "Select");
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("MultipleTaluka")) {
                    jsonObject2.put("User_Cluster__c", "Select");
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    String taluka = "Select";
                    if (value.length() > 0 && value.contains(",")) {
                        String talukas[] = value.split(",");
                        taluka = talukas[0];
                    } else {
                        taluka = value;
                    }
                    jsonObject2.put("User_Taluka__c", taluka);
                    jsonObject2.put("User_Village__c", "Select");
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("District")) {
                    jsonObject2.put("User_Cluster__c", "Select");
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", "Select");
                    jsonObject2.put("User_Village__c", "Select");
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else if (mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("State")) {
                    jsonObject2.put("User_Cluster__c", "Select");
                    jsonObject2.put("User_District__c", "Select");
                    jsonObject2.put("User_Taluka__c", "Select");
                    jsonObject2.put("User_Village__c", "Select");
                    jsonObject2.put("UserSchoolName__c", "Select");
                } else {
                    jsonObject2.put("User_Cluster__c", mListCluster.get(mSelectCluster));
                    jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                    jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                    jsonObject2.put("User_Village__c", mListVillage.get(mSelectVillage));
                    jsonObject2.put("UserSchoolName__c", mListSchoolName.get(mSelectSchoolName));
                }
                jsonObject2.put("User_Multiple_Taluka__c", binding.editMultiselectTaluka.getText().toString());

                jsonObject2.put("Multi_project__c",binding.editMultiselectProject.getText().toString());
                jsonObject2.put("User_Address__c",binding.editTextAddress.getText().toString());
//                if (mSelectProject > 0)
//                    jsonObject2.put("Project__c", mListProjectId.get(mSelectProject));
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObjectAttachment = new JSONObject();
                if (FinalUri != null) {

                    try {
                        InputStream iStream = null;
                        iStream = getContentResolver().openInputStream(FinalUri);
                        String img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);

                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", edit_text_name.getText().toString().trim());
                        jsonObjectAttachment.put("ContentType", "image/png");

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                jsonObject1.put("user", jsonObject2);
                jsonObject1.put("attachments", jsonObjectAttachment);
                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.MTRegisterUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                            if (response.body() != null) {
                                String data = response.body().string();
                                if (data != null && data.length() > 0) {
                                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                    User user = gson.fromJson(data, User.class);
                                    if (user.getDuplicateMobileNo() != null && user.getDuplicateMobileNo().equalsIgnoreCase("true")) {
                                        showDuplicatePopUp();
                                        return;
                                    }
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
                                    if (!isAdd) {
                                        AppDatabase.getAppDatabase(RegistrationActivity.this).userDao().clearTableCommunity();
                                        AppDatabase.getAppDatabase(RegistrationActivity.this).userDao().clearProcessTable();
                                        AppDatabase.getAppDatabase(RegistrationActivity.this).userDao().clearTaskContainer();
                                    }
                                    preferenceHelper.insertString(PreferenceHelper.UserData, data);
                                    User.clearUser();
                                    setResult(RESULT_OK);
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                }
                            }
                            /*JSONObject response1 = new JSONObject(response.body().string());
                            if (response1.getBoolean("success")) {
                                // user.setId(response1.getString("id"));
                            } else {
                                Utills.showToast(response1.getString("Message"), RegistrationActivity.this);
                            }*/
                        } catch (
                                Exception e)

                        {
                            e.printStackTrace();
                            Utills.showToast(getString(R.string.error_something_went_wrong), RegistrationActivity.this);
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), RegistrationActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void showDuplicatePopUp() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle(getString(R.string.alreadyPresent));
        alertDialog.setMessage(getString(R.string.alreadyPresentDetail));

        alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }


    /*
    * check all data is valid or not
    * */
    private boolean isValidate() {
        String msg = "";
        if (edit_text_name.getText().toString().trim().length() == 0) {
            msg = getString(R.string.Please_Enter_Name);
        } else if (edit_text_mobile_number.getText().toString().trim().length() == 0) {
            msg = getString(R.string.Please_Enter_Mobile_Number);
        } else if (edit_text_mobile_number.getText().toString().trim().length() != 10) {
            msg = getString(R.string.mobile_numer_enter);
        } else if (mSelectRole == 0) {
            msg = getString(R.string.Please_select_Role);
        } else if (edit_text_email.getText().toString().trim().length() != 0 && !android.util.Patterns.EMAIL_ADDRESS.matcher(edit_text_email.getText().toString().trim()).matches()) {
            msg = getString(R.string.Please_Enter_valid_email_address);
        } else if (mGenderSelect.length() == 0) {
            msg = getString(R.string.Please_select_Gender);
        } else if (binding.editTextRefresh.getText().toString().equals("") && binding.spinnerOrganization.getSelectedItem().equals("SMF")) {
            msg = getString(R.string.please_refresh_location);
        } else if (mSelectState == 0) {
            msg = getString(R.string.Please_select_State);
        } else if (binding.editMultiselectProject.getText().toString().equals("")) {
            msg = getString(R.string.Please_select_project);
        }else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("District"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Taluka"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            } else if (mSelectTaluka == 0) {
                msg = getString(R.string.Please_select_taluka);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("MultipleTaluka"))) {
            if (binding.editMultiselectTaluka.getText().toString().trim().length() == 0) {
                msg = getString(R.string.Please_select_taluka);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Cluster"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            } else if (mSelectTaluka == 0) {
                msg = getString(R.string.Please_select_taluka);
            } else if (mSelectCluster == 0) {
                msg = getString(R.string.Please_select_taluka);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("Village"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            } else if (mSelectTaluka == 0) {
                msg = getString(R.string.Please_select_taluka);
            } /*else if (mSelectCluster == 0) {
                msg = getString(R.string.Please_select_taluka);
            } */else if (mSelectVillage == 0) {
                msg = getString(R.string.Please_select_village);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("School"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            } else if (mSelectTaluka == 0) {
                msg = getString(R.string.Please_select_taluka);
            } /*else if (mSelectCluster == 0) {
                msg = getString(R.string.Please_select_taluka);
            } */else if (mSelectVillage == 0) {
                msg = getString(R.string.Please_select_village);
            } else if (mSelectSchoolName == 0) {
                msg = getString(R.string.Please_select_school);
            }
        }
        if (TextUtils.isEmpty(msg))
            return true;
        Utills.showToast(msg, this);
        return false;
    }


    @Override
    public void onBackPressed() {
        Log.i("Dialog", "Back pressed");
        if (User.getCurrentUser(RegistrationActivity.this).getMvUser().getRoll() == null
                || TextUtils.isEmpty(User.getCurrentUser(RegistrationActivity.this).getMvUser().getRoll())) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            isBackPress = true;
            Utills.hideProgressDialog();
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_project:
                mSelectProject = i;
                break;
            case R.id.spinner_organization:
                mSelectOrganization = i;
                if (mSelectOrganization != 0) {
                    getProject();
                    getRole();
                }
                mListRoleId.clear();
                mListRoleName.clear();
                mListRoleJuridiction.clear();
                mListRoleName.add("Select");
                mListRoleJuridiction.add("");
                mListRoleId.add("");
                role_adapter.notifyDataSetChanged();
                if (mListOrganization.get(mSelectOrganization).equals("SMF")) {
                    binding.llWork.setVisibility(View.VISIBLE);
                    binding.inputAddress.setVisibility(View.VISIBLE);
                } else {
                    binding.llWork.setVisibility(View.GONE);
                    binding.inputAddress.setVisibility(View.GONE);
                }
                break;
            case R.id.spinner_role:
                mSelectRole = i;
                if (mSelectRole != 0) {
                    if (mListRoleJuridiction.get(i).equalsIgnoreCase("School")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        txt_taluka.setVisibility(View.VISIBLE);
                        rel_taluka.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        spinner_cluster.setVisibility(View.GONE);
                        txt_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.VISIBLE);
                        txt_village.setVisibility(View.VISIBLE);
                        value = "";
                        rel_villgae.setVisibility(View.VISIBLE);
                        spinner_school_name.setVisibility(View.VISIBLE);
                        txt_school.setVisibility(View.VISIBLE);
                        rel_school_name.setVisibility(View.VISIBLE);
                        // input_school_code.setVisibility(View.VISIBLE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("Village")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        txt_taluka.setVisibility(View.VISIBLE);
                        rel_taluka.setVisibility(View.VISIBLE);
                        spinner_cluster.setVisibility(View.GONE);
                        txt_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.VISIBLE);
                        txt_village.setVisibility(View.VISIBLE);
                        rel_villgae.setVisibility(View.VISIBLE);
                        spinner_school_name.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        value = "";
                        txt_school.setVisibility(View.GONE);
                        //  input_school_code.setVisibility(View.GONE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("Cluster")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        txt_taluka.setVisibility(View.VISIBLE);
                        rel_taluka.setVisibility(View.VISIBLE);
                        spinner_cluster.setVisibility(View.VISIBLE);
                        txt_cluster.setVisibility(View.VISIBLE);
                        rel_cluster.setVisibility(View.VISIBLE);
                        spinner_village.setVisibility(View.GONE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        rel_villgae.setVisibility(View.GONE);
                        txt_village.setVisibility(View.GONE);
                        spinner_school_name.setVisibility(View.GONE);
                        value = "";
                        rel_school_name.setVisibility(View.GONE);
                        txt_school.setVisibility(View.GONE);
                        //  input_school_code.setVisibility(View.GONE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("Taluka")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        txt_taluka.setVisibility(View.VISIBLE);
                        rel_taluka.setVisibility(View.VISIBLE);
                        spinner_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        txt_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.GONE);
                        rel_villgae.setVisibility(View.GONE);
                        txt_village.setVisibility(View.GONE);
                        spinner_school_name.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        txt_school.setVisibility(View.GONE);
                        value = "";
                        //  input_school_code.setVisibility(View.GONE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("MultipleTaluka")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.VISIBLE);
                        if (!isAdd && !isMultipleTalukatSet) {
                            isMultipleTalukatSet = true;
                            value = User.getCurrentUser(RegistrationActivity.this).getMvUser().getMultipleTaluka();
                            binding.editMultiselectTaluka.setText(value);
                        }
                        txt_taluka.setVisibility(View.GONE);
                        rel_taluka.setVisibility(View.GONE);
                        spinner_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        txt_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.GONE);
                        rel_villgae.setVisibility(View.GONE);
                        txt_village.setVisibility(View.GONE);
                        spinner_school_name.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        txt_school.setVisibility(View.GONE);
                        //  input_school_code.setVisibility(View.GONE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("District")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.GONE);
                        rel_taluka.setVisibility(View.GONE);
                        txt_taluka.setVisibility(View.GONE);
                        spinner_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        value = "";
                        txt_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.GONE);
                        txt_village.setVisibility(View.GONE);
                        rel_villgae.setVisibility(View.GONE);
                        spinner_school_name.setVisibility(View.GONE);
                        txt_school.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        //   input_school_code.setVisibility(View.GONE);
                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("State")) {
                        spinner_district.setVisibility(View.GONE);
                        rel_district.setVisibility(View.GONE);
                        value = "";
                        txt_district.setVisibility(View.GONE);
                        spinner_taluka.setVisibility(View.GONE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        rel_taluka.setVisibility(View.GONE);
                        txt_taluka.setVisibility(View.GONE);
                        spinner_cluster.setVisibility(View.GONE);
                        rel_cluster.setVisibility(View.GONE);
                        txt_cluster.setVisibility(View.GONE);
                        spinner_village.setVisibility(View.GONE);
                        txt_village.setVisibility(View.GONE);
                        rel_villgae.setVisibility(View.GONE);
                        spinner_school_name.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        txt_school.setVisibility(View.GONE);
                        //  input_school_code.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    getDistrict();
                }
                mListDistrict.clear();
                mListTaluka.clear();
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                value = "";
                binding.editMultiselectTaluka.setText("");
                mListTaluka.add("Select");
                mListDistrict.add("Select");
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                mSelectDistrict = 0;
                mSelectCluster = 0;
                mSelectTaluka = 0;
                mSelectVillage = 0;
                mSelectSchoolName = 0;
                district_adapter.notifyDataSetChanged();
                taluka_adapter.notifyDataSetChanged();
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    getTaluka();
                }
                mListTaluka.clear();
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                mListTaluka.add("Select");
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                value = "";
               // binding.editMultiselectTaluka.setText("");
                mSelectTaluka = 0;
                mSelectCluster = 0;
                mSelectVillage = 0;
                mSelectSchoolName = 0;
                taluka_adapter.notifyDataSetChanged();
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                  //  getCluster();
                    getVillage();
                }
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                mSelectCluster = 0;
                mSelectVillage = 0;
                mSelectSchoolName = 0;
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    getVillage();
                }

                mListVillage.clear();
                mListSchoolName.clear();
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                mSelectVillage = 0;
                mSelectSchoolName = 0;
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    getSchool();
                }
                mListSchoolName.clear();
                mSelectSchoolName = 0;
                mListSchoolName.add("Select");
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
               /* if (mSelectSchoolName != 0)
                    edit_text_school_code.setText(mListCode.get(i));
                else
                    edit_text_school_code.setText("");*/
                break;
        }
    }


    /*
    * API to get all Clusters From server
    * */
 //   private void getCluster() {
//        if (!isBackPress)
//            Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
//        ServiceRequest apiService =
//                ApiClient.getClient().create(ServiceRequest.class);
//        apiService.getCluster(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Utills.hideProgressDialog();
//                try {
//                    if (response.body() != null) {
//                        String data = response.body().string();
//                        if (data != null && data.length() > 0) {
//                            mListCluster.clear();
//                            mListCluster.add("Select");
//                            JSONArray jsonArr = new JSONArray(data);
//                            for (int i = 0; i < jsonArr.length(); i++) {
//                                mListCluster.add(jsonArr.getString(i));
//                            }
//                            cluster_adapter.notifyDataSetChanged();
//                            if (!isAdd && !isClusterSet) {
//                                isClusterSet = true;
//                                for (int i = 0; i < mListCluster.size(); i++) {
//                                    if (mListCluster.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getCluster())) {
//                                        binding.spinnerCluster.setSelection(i);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Utills.hideProgressDialog();
//            }
//        });
//    }

    /*
    * API to get all Talukas From server
    * */
    private void getTaluka() {
        if (!isBackPress)
            Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict),"structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }
                            taluka_adapter.notifyDataSetChanged();
                            if (!isAdd && !isTalukaSet) {
                                isTalukaSet = true;
                                for (int i = 0; i < mListTaluka.size(); i++) {
                                    if (mListTaluka.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getTaluka())) {
                                        binding.spinnerTaluka.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    /*
    * API to get all Villages From server
    * */
    private void getVillage() {
        if (!isBackPress)
            Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getVillage(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), "structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListVillage.clear();
                            mListVillage.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListVillage.add(jsonArr.getString(i));
                            }
                            village_adapter.notifyDataSetChanged();
                            if (!isAdd && !isVillageSet) {
                                isVillageSet = true;
                                for (int i = 0; i < mListVillage.size(); i++) {
                                    if (mListVillage.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getVillage())) {
                                        binding.spinnerVillage.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    /*
    * API to get all Schools From server
    * */
    private void getSchool() {
        if (!isBackPress)
            Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getSchool(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListVillage.get(mSelectVillage), "structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListSchoolName.clear();
                            mListCode.clear();
                            mListSchoolName.add("Select");
                            mListCode.add("");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                       /* JSONObject jsonObject = jsonArr.getJSONObject(i);
                        mListSchoolName.add(jsonObject.getString("school_name"));
                        .add(jsonObject.getString("school_code"));*/
                                mListSchoolName.add(jsonArr.getString(i));
                            }
                            school_adapter.notifyDataSetChanged();
                            if (!isAdd && !isSchoolSet) {
                                isSchoolSet = true;
                                for (int i = 0; i < mListSchoolName.size(); i++) {
                                    if (mListSchoolName.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getMvUser().getSchool_Name())) {
                                        binding.spinnerSchoolName.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /*
    * Show dialog to select image from camera or gallary
    * */
    private void showPictureDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosepicture));
        String[] items = {getString(R.string.text_gallary),
                getString(R.string.text_camera)};

        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        choosePhotoFromGallery();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;

                }
            }
        });
        dialog.show();
    }

    /*
    * Intent to open camera
    * */
    private void takePhotoFromCamera() {

        try {
            //use standard intent to capture an image
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture.jpg";
            File imageFile = new File(imageFilePath);
            outputUri = Uri.fromFile(imageFile); // convert path to Uri
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            startActivityForResult(takePictureIntent, Constants.CHOOSE_IMAGE_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * Intent to open gallery
    * */
    private void choosePhotoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                File imageFile = new File(imageFilePath);
                FinalUri = Uri.fromFile(imageFile);
                Crop.of(outputUri, FinalUri).asSquare().start(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Constants.CHOOSE_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    outputUri = data.getData();
                    String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                    File imageFile = new File(imageFilePath);
                    FinalUri = Uri.fromFile(imageFile);
                    Crop.of(outputUri, FinalUri).asSquare().start(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 100) {
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Glide.with(this)
                    .load(FinalUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.addImage);
        }
    }

    public void onAddImageClick() {
        showPictureDialog();
    }

    public void showDateDialog(Context context, final EditText editText) {


        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        //  taskList.get(Position).setTask_Response__c(getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);
                        // notifyItemChanged(Position);
                        editText.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));

                    }
                }, mYear, mMonth, mDay);

        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }


    private void showMultiselectDialog(ArrayList<String> arrayList) {
        ArrayList<String> list = arrayList;
        if (list.contains("Select")) {
            list.remove(list.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[list.size()]);
        mSelection = new boolean[(items.length)];
        Arrays.fill(mSelection, false);
        if (value.length() != 0) {
            String[] talukas = value.split(",");
            for (int i = 0; i < talukas.length; i++) {
                if (arrayList.contains(talukas[i].trim())) {
                    mSelection[arrayList.indexOf(talukas[i].trim())] = true;
                }
            }
        }

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle(getString(R.string.taluka))
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;
                            value = buildSelectedItemString(items);
                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton(RegistrationActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        binding.editMultiselectTaluka.setText(value);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private void showMultiselectDialogProject(ArrayList<String> arrayList) {
        ArrayList<String> list = arrayList;
        if (list.contains("Select")) {
            list.remove(list.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[list.size()]);
        mSelection = new boolean[(items.length)];
        Arrays.fill(mSelection, false);
        if (valueProject.length() != 0) {
            String[] talukas = valueProject.split(";");
            for (int i = 0; i < talukas.length; i++) {
                if (arrayList.contains(talukas[i].trim())) {
                    mSelection[arrayList.indexOf(talukas[i].trim())] = true;
                }
            }
        }

        // arraylist to keep the selected items
        AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle(getString(R.string.project))
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;
                            valueProject = buildSelectedItemStringProject(items);
                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton(RegistrationActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        binding.editMultiselectProject.setText(valueProject);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }
    private String buildSelectedItemStringProject(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(";");
                }
                foundOne = true;

                sb.append(items[i]);
            }
        }
        return sb.toString();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(",");
                }
                foundOne = true;

                sb.append(items[i]);
            }
        }
        return sb.toString();
    }
}
