package com.mv.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.soundcloud.android.crop.Crop;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.GPSTracker;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.Widgets.MyEditTextView;
import com.mv.databinding.ActivityRegistrationBinding;

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

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private ActivityRegistrationBinding binding;
    private PreferenceHelper preferenceHelper;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private Boolean isAdd;
    private GPSTracker gps;

    private EditText edit_text_midle_name;
    private EditText edit_text_last_name;
    private EditText edit_text_name;
    private EditText edit_text_mobile_number;
    private EditText edit_text_school_code;
    private EditText edit_text_email;

    private Spinner spinner_project;
    private Spinner spinner_organization;
    private Spinner spinner_district;
    private Spinner spinner_taluka;
    private Spinner spinner_cluster;
    private Spinner spinner_village;
    private Spinner spinner_school_name;

    private int mSelectOrganization = 0;
    private int mSelectRole = 0;
    private int mSelectState = 0;
    private int mSelectDistrict = 0;
    private int mSelectTaluka = 0;
    private int mSelectCluster = 0;
    private int mSelectVillage = 0;
    private int mSelectSchoolName = 0;

    private String SelectedLat = "", SelectedLon = "";
    private String mGenderSelect = "";
    private String value = "", valueProject = "";

    private ArrayAdapter<String> state_adapter, district_adapter, taluka_adapter, cluster_adapter,
            village_adapter, school_adapter, role_adapter, organization_adapter, project_adapter;
    private ArrayList<String> mListProject, mListOrganization, mListState, mListDistrict,
            mListTaluka, mListCluster, mListVillage, mListSchoolName;
    private List<String> mListRoleName, mListRoleId, mListRoleJuridiction;

    private TextView txt_district, txt_taluka, txt_cluster, txt_village, txt_school;
    private RelativeLayout rel_district, rel_taluka, rel_cluster, rel_villgae, rel_school_name;

    private boolean isBackPress = false;
    private boolean[] mSelection = null;
    private boolean isMultipleTalukaSet = false, isProjectSet = false, isOrganizationSet = false,
            isStateSet = false, isDistrictSet = false, isTalukaSet = false, isClusterSet = false,
            isVillageSet = false, isSchoolSet = false, isRollSet = false;

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
        if (!isBackPress) {
            Utills.showProgressDialog(this, "Loading Projects", getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetProjectDataUrl + "?org=" + mListOrganization.get(mSelectOrganization);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListProject.clear();
                            mListProject.add("Select");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                mListProject.add(object.getString("Name"));
                            }

                            project_adapter.notifyDataSetChanged();

                            if (!isAdd && !isProjectSet) {
                                isProjectSet = true;
                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListProject.size(); i++) {
                                        if (mListProject.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getProject_Name__c())) {
                                            spinner_project.setSelection(i);
                                            break;
                                        }
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.MV_RoleUrl + mListOrganization.get(mSelectOrganization) + "'";

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
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

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListRoleName.size(); i++) {
                                        if (mListRoleName.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getRoll())) {
                                            binding.spinnerRole.setSelection(i);
                                            break;
                                        }
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, "Loading Organization", getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetOrganizationUrl;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListOrganization.clear();
                            mListOrganization.add("Select");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListOrganization.add(jsonArray.getString(i));
                            }

                            organization_adapter.notifyDataSetChanged();

                            if (!isAdd && !isOrganizationSet) {
                                isOrganizationSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListOrganization.size(); i++) {
                                        if (mListOrganization.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getOrganisation())) {
                                            spinner_organization.setSelection(i);
                                            break;
                                        }
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
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListState.clear();
                            mListState.add("Select");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListState.add(jsonArray.getString(i));
                            }

                            state_adapter.notifyDataSetChanged();

                            if (!isAdd && !isStateSet) {
                                isStateSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListState.size(); i++) {
                                        if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getState())) {
                                            binding.spinnerState.setSelection(i);
                                            break;
                                        }
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mListState.get(mSelectState)).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListDistrict.clear();
                            mListDistrict.add("Select");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }

                            district_adapter.notifyDataSetChanged();

                            if (!isAdd && !isDistrictSet) {
                                isDistrictSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListDistrict.size(); i++) {
                                        if (mListDistrict.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getDistrict())) {
                                            binding.spinnerDistrict.setSelection(i);
                                            break;
                                        }
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
        binding.birthDate.setOnClickListener(this);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.gender_group);
        radioGroup.setOnCheckedChangeListener((radioGroup1, checkedId) -> {
            if (checkedId == R.id.gender_male) {
                mGenderSelect = "Male";
            } else if (checkedId == R.id.gender_female) {
                mGenderSelect = "Female";
            } else if (checkedId == R.id.gender_other) {
                mGenderSelect = "Other";
            }
        });

        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        edit_text_midle_name = (MyEditTextView) findViewById(R.id.edit_text_midle_name);
        edit_text_last_name = (EditText) findViewById(R.id.edit_text_last_name);
        edit_text_mobile_number = (EditText) findViewById(R.id.edit_text_mobile_number);
        edit_text_email = (EditText) findViewById(R.id.edit_text_email);
        edit_text_school_code = (EditText) findViewById(R.id.edit_text_school_code);

        if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
            edit_text_mobile_number.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getPhone());
            binding.birthDate.setText(User.getCurrentUser(RegistrationActivity.this).getMvUser().getBirth_Day__c());
        }

        binding.editMultiselectTaluka.setOnClickListener(this);
        binding.editMultiselectProject.setOnClickListener(this);
        binding.btnRefreshLocation.setOnClickListener(this);

        if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null &&
                User.getCurrentUser(RegistrationActivity.this).getMvUser().getOrganisation().equals("SMF")) {
            binding.llWork.setVisibility(View.VISIBLE);
            binding.inputAddress.setVisibility(View.VISIBLE);

            String s = User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lat()
                    + " , " + User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lng();
            binding.editTextRefresh.setText(s);

            SelectedLon = User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lng();
            SelectedLat = User.getCurrentUser(RegistrationActivity.this).getMvUser().getAttendance_Loc_Lat();
            binding.editTextAddress.setText(ConvertToAddress(Double.parseDouble(SelectedLon), Double.parseDouble(SelectedLat)));
        } else {
            binding.llWork.setVisibility(View.GONE);
            binding.inputAddress.setVisibility(View.GONE);
        }

        Button btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        Spinner spinner_role = (Spinner) findViewById(R.id.spinner_role);
        spinner_role.setOnItemSelectedListener(this);
        Spinner spinner_state = (Spinner) findViewById(R.id.spinner_state);
        spinner_state.setOnItemSelectedListener(this);

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
        spinner_district.setOnItemSelectedListener(this);
        spinner_taluka.setOnItemSelectedListener(this);
        spinner_cluster.setOnItemSelectedListener(this);
        spinner_village.setOnItemSelectedListener(this);
        spinner_school_name.setOnItemSelectedListener(this);

        mListState = new ArrayList<>();
        mListRoleName = new ArrayList<>();
        mListRoleId = new ArrayList<>();
        mListRoleJuridiction = new ArrayList<>();
        mListDistrict = new ArrayList<>();
        mListTaluka = new ArrayList<>();
        mListCluster = new ArrayList<>();
        mListVillage = new ArrayList<>();
        mListSchoolName = new ArrayList<>();
        mListOrganization = new ArrayList<>();
        mListProject = new ArrayList<>();

        mListState.add("Select");
        mListDistrict.add("Select");
        mListTaluka.add("Select");
        mListCluster.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");
        mListOrganization.add("Select");
        mListRoleName.add("Select");
        mListProject.add("Select");

        project_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListProject);
        project_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_project.setAdapter(project_adapter);

        organization_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListOrganization);
        organization_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_organization.setAdapter(organization_adapter);

        role_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListRoleName);
        role_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_role.setAdapter(role_adapter);

        txt_district = (TextView) findViewById(R.id.txt_district);
        txt_taluka = (TextView) findViewById(R.id.txt_taluka);
        txt_cluster = (TextView) findViewById(R.id.txt_cluster);
        txt_village = (TextView) findViewById(R.id.txt_village);
        txt_school = (TextView) findViewById(R.id.txt_school);

        state_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListState);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(state_adapter);

        district_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_district.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_taluka.setAdapter(taluka_adapter);

        cluster_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListCluster);
        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_cluster.setAdapter(cluster_adapter);

        village_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListVillage);
        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_village.setAdapter(village_adapter);

        school_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListSchoolName);
        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_school_name.setAdapter(school_adapter);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_EDIT)) {
                setActionbar(getString(R.string.update_profile));
                isAdd = false;

                if (User.getCurrentUser(this).getMvUser() != null) {
                    binding.editTextMidleName.setText(User.getCurrentUser(this).getMvUser().getMiddleName());
                    binding.editTextLastName.setText(User.getCurrentUser(this).getMvUser().getLastName());
                    binding.editTextMobileNumber.setText(User.getCurrentUser(this).getMvUser().getPhone());
                    binding.editTextName.setText(User.getCurrentUser(this).getMvUser().getName());
                    binding.editMultiselectTaluka.setText(User.getCurrentUser(this).getMvUser().getMultipleTaluka());
                    binding.editMultiselectProject.setText(User.getCurrentUser(this).getMvUser().getMulti_project__c());
                    binding.editTextAddress.setText(User.getCurrentUser(this).getMvUser().getUser_Address__c());
                }

                if (User.getCurrentUser(this).getMvUser() != null &&
                        User.getCurrentUser(this).getMvUser().getGender() != null &&
                        !TextUtils.isEmpty(User.getCurrentUser(this).getMvUser().getGender())) {

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

                if (User.getCurrentUser(this).getMvUser() != null &&
                        User.getCurrentUser(this).getMvUser().getImageId() != null &&
                        !(User.getCurrentUser(this).getMvUser().getImageId().equalsIgnoreCase("null"))) {

                    Glide.with(this)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                                    + "/services/data/v36.0/sobjects/Attachment/"
                                    + User.getCurrentUser(this).getMvUser().getImageId() + "/Body"))
                            .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                            .into(binding.addImage);
                }

                if (User.getCurrentUser(this).getMvUser() != null) {
                    if (!(TextUtils.isEmpty(User.getCurrentUser(this).getMvUser().getEmail()) ||
                            User.getCurrentUser(this).getMvUser().getEmail().equalsIgnoreCase("null"))) {
                        binding.editTextEmail.setText(User.getCurrentUser(this).getMvUser().getEmail());
                    }
                }
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
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    /*
     * set actionbar to activity
     * */
    private void setActionbar(String Title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
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
                showMultiSelectDialog(mListTaluka);
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
                binding.editTextRefresh.setText(String.format("%s , %s", gps.getLatitude(), gps.getLongitude()));
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
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
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

    /*
     * send all data to server
     * */
    private void sendData() {
        if (isValidate()) {
            if (!isBackPress) {
                Utills.showProgressDialog(this);
            }

            ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

            User user = new User();
            user.getMvUser().setName(edit_text_name.getText().toString().trim());
            user.getMvUser().setEmail(edit_text_email.getText().toString().trim());
            user.getMvUser().setPhone(edit_text_mobile_number.getText().toString().trim());
            user.getMvUser().setSchool_Code(edit_text_school_code.getText().toString().trim());

            if (mListRoleName.size() > mSelectRole) {
                user.getMvUser().setRoll(mListRoleName.get(mSelectRole));
            }
            if (mListCluster.size() > mSelectCluster) {
                user.getMvUser().setCluster(mListCluster.get(mSelectCluster));
            }
            if (mListDistrict.size() > mSelectDistrict) {
                user.getMvUser().setDistrict(mListDistrict.get(mSelectDistrict));
            }
            if (mListTaluka.size() > mSelectTaluka) {
                user.getMvUser().setTaluka(mListTaluka.get(mSelectTaluka));
            }
            if (mListVillage.size() > mSelectVillage) {
                user.getMvUser().setVillage(mListVillage.get(mSelectVillage));
            }

            user.getMvUser().setSchool_Name(mListSchoolName.get(mSelectSchoolName));
            user.getMvUser().setMultipleTaluka(value);

            JSONObject jsonObject2 = new JSONObject();
            try {
                jsonObject2.put("Id",
                        User.getCurrentUser(RegistrationActivity.this).getMvUser() != null ?
                                User.getCurrentUser(RegistrationActivity.this).getMvUser().getId() : "");
                jsonObject2.put("Name", edit_text_name.getText().toString().trim());
                jsonObject2.put("User_Email__c", edit_text_email.getText().toString().trim());
                jsonObject2.put("User_Mobile_No__c", edit_text_mobile_number.getText().toString().trim());
                jsonObject2.put("MV_Role__c", mListRoleId.get(mSelectRole));
                jsonObject2.put("Role_Name__c", mListRoleName.get(mSelectRole));
                jsonObject2.put("Last_Name__c", edit_text_last_name.getText().toString().trim());
                jsonObject2.put("Middle_Name__c", edit_text_midle_name.getText().toString().trim());

                if (binding.birthDate.getText().toString().trim().length() > 0) {
                    jsonObject2.put("Birth_Day__c", binding.birthDate.getText().toString().trim());
                }

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

                    String taluka;
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
                jsonObject2.put("Multi_project__c", binding.editMultiselectProject.getText().toString());
                jsonObject2.put("User_Address__c", binding.editTextAddress.getText().toString());
                jsonObject2.put("Role_Organization__c", binding.spinnerOrganization.getSelectedItem().toString());

                JSONObject jsonObjectAttachment = new JSONObject();
                if (FinalUri != null) {
                    try {
                        InputStream iStream;
                        iStream = getContentResolver().openInputStream(FinalUri);
                        String img_str = null;
                        if (iStream != null) {
                            img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        }

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

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("user", jsonObject2);
                jsonObject1.put("attachments", jsonObjectAttachment);

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("listVisitsData", jsonArray);

                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.MTRegisterUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                String data = response.body().string();
                                if (data.length() > 0) {
                                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                    User user = gson.fromJson(data, User.class);

                                    if (user.getDuplicateMobileNo() != null &&
                                            user.getDuplicateMobileNo().equalsIgnoreCase("true")) {
                                        showDuplicatePopUp();
                                        return;
                                    }

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
                        } catch (Exception e) {
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

    @SuppressWarnings("deprecation")
    private void showDuplicatePopUp() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alreadyPresent));
        alertDialog.setMessage(getString(R.string.alreadyPresentDetail));

        alertDialog.setButton(getString(R.string.ok), (dialog, which) -> {
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
        } else if (edit_text_email.getText().toString().trim().length() != 0 &&
                !android.util.Patterns.EMAIL_ADDRESS.matcher(edit_text_email.getText().toString().trim()).matches()) {
            msg = getString(R.string.Please_Enter_valid_email_address);
        } else if (mGenderSelect.length() == 0) {
            msg = getString(R.string.Please_select_Gender);
        } else if (binding.editTextRefresh.getText().toString().equals("") &&
                binding.spinnerOrganization.getSelectedItem().equals("SMF")) {
            msg = getString(R.string.please_refresh_location);
        } else if (mSelectState == 0) {
            msg = getString(R.string.Please_select_State);
        } else if (binding.editMultiselectProject.getText().toString().equals("")) {
            msg = getString(R.string.Please_select_project);
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("District"))) {
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
            } else if (mSelectCluster == 0) {
                msg = getString(R.string.Please_select_taluka);
            } else if (mSelectVillage == 0) {
                msg = getString(R.string.Please_select_village);
            }
        } else if ((mListRoleJuridiction.get(mSelectRole).equalsIgnoreCase("School"))) {
            if (mSelectDistrict == 0) {
                msg = getString(R.string.Please_select_district);
            } else if (mSelectTaluka == 0) {
                msg = getString(R.string.Please_select_taluka);
            } else if (mSelectCluster == 0) {
                msg = getString(R.string.Please_select_taluka);
            } else if (mSelectVillage == 0) {
                msg = getString(R.string.Please_select_village);
            } else if (mSelectSchoolName == 0) {
                msg = getString(R.string.Please_select_school);
            }
        }

        if (TextUtils.isEmpty(msg)) {
            return true;
        }

        Utills.showToast(msg, this);
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.i("Dialog", "Back pressed");
        if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
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
                        spinner_cluster.setVisibility(View.VISIBLE);
                        txt_cluster.setVisibility(View.VISIBLE);
                        rel_cluster.setVisibility(View.VISIBLE);
                        spinner_village.setVisibility(View.VISIBLE);
                        txt_village.setVisibility(View.VISIBLE);
                        value = "";
                        rel_villgae.setVisibility(View.VISIBLE);
                        spinner_school_name.setVisibility(View.VISIBLE);
                        txt_school.setVisibility(View.VISIBLE);
                        rel_school_name.setVisibility(View.VISIBLE);

                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("Village")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.GONE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        txt_taluka.setVisibility(View.VISIBLE);
                        rel_taluka.setVisibility(View.VISIBLE);
                        spinner_cluster.setVisibility(View.VISIBLE);
                        txt_cluster.setVisibility(View.VISIBLE);
                        rel_cluster.setVisibility(View.VISIBLE);
                        spinner_village.setVisibility(View.VISIBLE);
                        txt_village.setVisibility(View.VISIBLE);
                        rel_villgae.setVisibility(View.VISIBLE);
                        spinner_school_name.setVisibility(View.GONE);
                        rel_school_name.setVisibility(View.GONE);
                        value = "";
                        txt_school.setVisibility(View.GONE);

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

                    } else if (mListRoleJuridiction.get(i).equalsIgnoreCase("MultipleTaluka")) {
                        spinner_district.setVisibility(View.VISIBLE);
                        txt_district.setVisibility(View.VISIBLE);
                        rel_district.setVisibility(View.VISIBLE);
                        spinner_taluka.setVisibility(View.VISIBLE);
                        binding.inputMultiselectTaluka.setVisibility(View.VISIBLE);

                        if (!isAdd && !isMultipleTalukaSet) {
                            isMultipleTalukaSet = true;
                            value = User.getCurrentUser(RegistrationActivity.this).getMvUser() != null ?
                                    User.getCurrentUser(RegistrationActivity.this).getMvUser().getMultipleTaluka() : "";
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
                    getCluster();
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
                break;
        }
    }

    /*
     * API to get all Clusters From server
     * */
    private void getCluster() {
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getCluster(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict),
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

                            cluster_adapter.notifyDataSetChanged();
                            if (!isAdd && !isClusterSet) {
                                isClusterSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListCluster.size(); i++) {
                                        if (mListCluster.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getCluster())) {
                                            binding.spinnerCluster.setSelection(i);
                                            break;
                                        }
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
     * API to get all Talukas From server
     * */
    private void getTaluka() {
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getTaluka(mListState.get(mSelectState),
                mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {

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

                            taluka_adapter.notifyDataSetChanged();
                            if (!isAdd && !isTalukaSet) {
                                isTalukaSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListTaluka.size(); i++) {
                                        if (mListTaluka.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getTaluka())) {
                                            binding.spinnerTaluka.setSelection(i);
                                            break;
                                        }
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict),
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

                            village_adapter.notifyDataSetChanged();
                            if (!isAdd && !isVillageSet) {
                                isVillageSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListVillage.size(); i++) {
                                        if (mListVillage.get(i).trim().equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getVillage())) {
                                            binding.spinnerVillage.setSelection(i);
                                            break;
                                        }
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
        if (!isBackPress) {
            Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getSchool(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict),
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

                            school_adapter.notifyDataSetChanged();
                            if (!isAdd && !isSchoolSet) {
                                isSchoolSet = true;

                                if (User.getCurrentUser(RegistrationActivity.this).getMvUser() != null) {
                                    for (int i = 0; i < mListSchoolName.size(); i++) {
                                        if (mListSchoolName.get(i).equalsIgnoreCase(User.getCurrentUser(
                                                RegistrationActivity.this).getMvUser().getSchool_Name())) {
                                            binding.spinnerSchoolName.setSelection(i);
                                            break;
                                        }
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
        String[] items = {getString(R.string.text_gallary), getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    choosePhotoFromGallery();
                    break;

                case 1:
                    takePhotoFromCamera();
                    break;
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
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture.jpg";
            File imageFile = new File(imageFilePath);
            outputUri = FileProvider.getUriForFile(getApplicationContext(),
                    getPackageName() + ".fileprovider", imageFile);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(takePictureIntent, Constants.CHOOSE_IMAGE_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        } catch (SecurityException se) {
            String errorMessage = "App do not have permission to take a photo, please allow it.";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Intent to open gallery
     * */
    private void choosePhotoFromGallery() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);
        } catch (ActivityNotFoundException e) {
            String errorMessage = "Problem in taking photo from gallery, please use camera to take photo.";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
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
        if (!Utills.isMediaPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.MEDIA_PERMISSION_REQUEST);
            }
        } else {
            showPictureDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MEDIA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPictureDialog();
                }
                break;
        }
    }

    public void showDateDialog(Context context, final EditText editText) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            String date = year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth);
            editText.setText(date);
        }, mYear, mMonth, mDay);

        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    private void showMultiSelectDialog(ArrayList<String> arrayList) {
        if (arrayList.contains("Select")) {
            arrayList.remove(arrayList.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[arrayList.size()]);
        mSelection = new boolean[(items.length)];

        Arrays.fill(mSelection, false);
        if (value.length() != 0) {
            String[] talukas = value.split(",");

            for (String taluka : talukas) {
                if (arrayList.contains(taluka.trim())) {
                    mSelection[arrayList.indexOf(taluka.trim())] = true;
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle(getString(R.string.taluka))
                .setMultiChoiceItems(items, mSelection, (dialog1, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        value = buildSelectedItemString(items);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(RegistrationActivity.this.getString(R.string.ok), (dialog13, id) -> binding.editMultiselectTaluka.setText(value))
                .setNegativeButton(getString(R.string.cancel), (dialog12, id) -> {
                })
                .create();

        dialog.show();
    }

    private void showMultiselectDialogProject(ArrayList<String> arrayList) {
        if (arrayList.contains("Select")) {
            arrayList.remove(arrayList.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[arrayList.size()]);
        mSelection = new boolean[(items.length)];
        Arrays.fill(mSelection, false);

        if (valueProject.length() != 0) {
            String[] talukas = valueProject.split(";");
            for (String taluka : talukas) {
                if (arrayList.contains(taluka.trim())) {
                    mSelection[arrayList.indexOf(taluka.trim())] = true;
                }
            }
        }

        // arraylist to keep the selected items
        AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle(getString(R.string.project))
                .setMultiChoiceItems(items, mSelection, (dialog1, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        valueProject = buildSelectedItemStringProject(items);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(RegistrationActivity.this.getString(R.string.ok), (dialog12, id) -> binding.editMultiselectProject.setText(valueProject))
                .setNegativeButton(getString(R.string.cancel), (dialog13, id) -> {
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