package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityRegistrationBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    private Spinner spinner_role, spinner_state, spinner_district, spinner_taluka, spinner_cluster, spinner_village, spinner_school_name;
    private int mSelectRole = 0, mSelectState = 0, mSelectDistrict = 0, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private ArrayList<String> mListState, mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName;
    private List<String> mListRoleName, mListCode, mListRoleId;
    private TextView txt_taluka, txt_cluster, txt_village, txt_school;
    private TextInputLayout input_school_code;
    private ArrayAdapter<String> state_adapter, district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, role_adapter, organization_adapter;
    private PreferenceHelper preferenceHelper;
    private User user;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private String imageFilePath;
    private Boolean isAdd;
    private boolean isStateSet = false, isDistrictSet = false, isTalukaSet = false, isClusterSet = false, isVillageSet = false, isSchoolSet = false, isRollSet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration);
        binding.setActivity(this);
        initViews();
        if (Utills.isConnected(this)) {
            getState();
            getRole();
        } else {
            showPopUp();
        }
    }

    private void getRole() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/data/v36.0/query/?q=select+Id,Name+from+MV_Role__c";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray jsonArray = obj.getJSONArray("records");
                    mListRoleName.clear();
                    mListRoleId.clear();
                    mListRoleName.add("Select");
                    mListRoleId.add("");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        mListRoleName.add(jsonObject.getString("Name"));
                        mListRoleId.add(jsonObject.getString("Id"));
                    }
                    role_adapter.notifyDataSetChanged();
                    if (!isAdd && !isRollSet) {
                        isRollSet = true;
                        for (int i = 0; i < mListRoleName.size(); i++) {
                            if (mListRoleName.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getRoll())) {
                                binding.spinnerRole.setSelection(i);
                                break;
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
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getStateName";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListState.clear();
                    mListState.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListState.add(jsonArray.getString(i));
                    }
                    state_adapter.notifyDataSetChanged();
                    if (!isAdd && !isStateSet) {
                        isStateSet = true;
                        for (int i = 0; i < mListState.size(); i++) {
                            if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getState())) {
                                binding.spinnerState.setSelection(i);
                                break;
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

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getDistrict_Name__c?StateName=" + mListState.get(mSelectState);
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
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
                    if (!isAdd && !isDistrictSet) {
                        isDistrictSet = true;
                        for (int i = 0; i < mListDistrict.size(); i++) {
                            if (mListDistrict.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getDistrict())) {
                                binding.spinnerDistrict.setSelection(i);
                                break;
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

    private void initViews() {
        setActionbar(getString(R.string.Registration));
        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);

        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        edit_text_midle_name = (EditText) findViewById(R.id.edit_text_midle_name);
        edit_text_last_name = (EditText) findViewById(R.id.edit_text_last_name);
        edit_text_mobile_number = (EditText) findViewById(R.id.edit_text_mobile_number);
        edit_text_mobile_number.setText(User.getCurrentUser(RegistrationActivity.this).getPhone());

        edit_text_email = (EditText) findViewById(R.id.edit_text_email);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        spinner_role = (Spinner) findViewById(R.id.spinner_role);
        spinner_state = (Spinner) findViewById(R.id.spinner_state);
        spinner_district = (Spinner) findViewById(R.id.spinner_district);
        spinner_taluka = (Spinner) findViewById(R.id.spinner_taluka);
        spinner_cluster = (Spinner) findViewById(R.id.spinner_cluster);
        spinner_village = (Spinner) findViewById(R.id.spinner_village);
        spinner_school_name = (Spinner) findViewById(R.id.spinner_school_name);
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
        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();
        mListCluster = new ArrayList<String>();
        mListVillage = new ArrayList<String>();
        mListSchoolName = new ArrayList<String>();
        mListCode = new ArrayList<String>();

        mListState.add("Select");
        mListDistrict.add("Select");
        mListTaluka.add("Select");
        mListCluster.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");
        mListCode.add("Select");


        input_school_code = (TextInputLayout) findViewById(R.id.input_school_code);
        edit_text_school_code = (EditText) findViewById(R.id.edit_text_school_code);

        role_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListRoleName);
        role_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_role.setAdapter(role_adapter);


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
                isAdd = false;
                binding.editTextMidleName.setText(User.getCurrentUser(this).getMiddleName());
                binding.editTextLastName.setText(User.getCurrentUser(this).getLastName());
                binding.editTextMobileNumber.setText(User.getCurrentUser(this).getPhone());
                binding.editTextName.setText(User.getCurrentUser(this).getName());

                if (User.getCurrentUser(this).getRoll().equalsIgnoreCase("HM")
                        && User.getCurrentUser(this).getRoll().equalsIgnoreCase("CRC") && User.getCurrentUser(this).getRoll().equalsIgnoreCase("DEO") && User.getCurrentUser(this).getRoll().equalsIgnoreCase("BEO") && User.getCurrentUser(this).getRoll().equalsIgnoreCase("Teacher")) {
                    binding.roleOrganisation.check(R.id.gov);
                } else {
                    binding.roleOrganisation.check(R.id.smf);
                }
                if (User.getCurrentUser(this).getImageId() != null && !(User.getCurrentUser(this).getImageId().equalsIgnoreCase("null"))) {
                    Glide.with(this)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + User.getCurrentUser(this).getImageId() + "/Body"))
                            .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                            .into(binding.addImage);
                }
                if (!(TextUtils.isEmpty(User.getCurrentUser(this).getEmail()) || User.getCurrentUser(this).getEmail().equalsIgnoreCase("null")))
                    binding.editTextEmail.setText(User.getCurrentUser(this).getEmail());
            } else {
                isAdd = true;
            }
        }

    }

    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
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

                sendData();


                break;

        }
    }

    private void sendData() {
        if (isValidate()) {

            Utills.showProgressDialog(this);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            user = new User();
            user.setName(edit_text_name.getText().toString().trim());
            user.setEmail(edit_text_email.getText().toString().trim());
            user.setPhone(edit_text_mobile_number.getText().toString().trim());
            user.setRoll(mListRoleName.get(mSelectRole));
            user.setCluster(mListCluster.get(mSelectCluster));
            user.setDistrict(mListDistrict.get(mSelectDistrict));
            user.setTaluka(mListTaluka.get(mSelectTaluka));
            user.setVillage(mListVillage.get(mSelectVillage));
            user.setSchool_Code(edit_text_school_code.getText().toString().trim());
            user.setSchool_Name(mListSchoolName.get(mSelectSchoolName));
           /* Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(user);*/
           /* {
                "name":"pravin12",
                    "":"test1",  "":"pune","":"pravin.joshi@nanostuffs","":"123","":"123456789","":"developer",
                    "":"new technology",
                    "":"4545","":"haveli","":"dhankawadi"

            }*/
            JSONObject jsonObject1 = new JSONObject();
            JSONObject jsonObject2 = new JSONObject();
            try {

                jsonObject2.put("Id", User.getCurrentUser(RegistrationActivity.this).getId());
                jsonObject2.put("Name", edit_text_name.getText().toString().trim());
                jsonObject2.put("User_Email__c", edit_text_email.getText().toString().trim());
                jsonObject2.put("User_Mobile_No__c", edit_text_mobile_number.getText().toString().trim());
                jsonObject2.put("MV_Role__c", mListRoleId.get(mSelectRole));
                jsonObject2.put("User_State__c", mListState.get(mSelectState));
                jsonObject2.put("Last_Name__c", edit_text_last_name.getText().toString().trim());
                jsonObject2.put("Middle_Name__c", edit_text_midle_name.getText().toString().trim());
                jsonObject2.put("User_Cluster__c", mListCluster.get(mSelectCluster));
                jsonObject2.put("User_District__c", mListDistrict.get(mSelectDistrict));
                jsonObject2.put("User_Taluka__c", mListTaluka.get(mSelectTaluka));
                jsonObject2.put("User_Village__c", mListVillage.get(mSelectVillage));
                jsonObject2.put("User_SchoolID__c", edit_text_school_code.getText().toString().trim());
                jsonObject2.put("UserSchoolName__c", mListSchoolName.get(mSelectSchoolName));

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
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/MTRegister", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            String data = response.body().string();
                            JSONObject object = new JSONObject(data);
                            JSONArray array = object.getJSONArray("Records");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object1 = array.getJSONObject(i);
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                try {

                                    preferenceHelper.insertString(PreferenceHelper.UserData, object1.toString());
                                    preferenceHelper.insertString(PreferenceHelper.UserRole, user.getRoll());
                                    Utills.showToast("Registration Successful...", RegistrationActivity.this);
                                    User.clearUser();
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                            /*JSONObject response1 = new JSONObject(response.body().string());
                            if (response1.getBoolean("success")) {
                                // user.setId(response1.getString("id"));


                            } else {
                                Utills.showToast(response1.getString("Message"), RegistrationActivity.this);
                            }*/
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

    private boolean isValidate() {
        String msg = "";

        if (edit_text_name.getText().toString().trim().length() == 0) {
            msg = getString(R.string.Please_Enter_Name);
        } else if (edit_text_mobile_number.getText().toString().trim().length() == 0) {
            msg =getString(R.string.Please_Enter_Mobile_Number);
        } else if (edit_text_mobile_number.getText().toString().trim().length() != 10) {
            msg =getString(R.string.mobile_numer_enter);
        } else if (mSelectRole == 0) {
            msg =getString(R.string.Please_select_Role);
        } else if (edit_text_email.getText().toString().trim().length() != 0 && !android.util.Patterns.EMAIL_ADDRESS.matcher(edit_text_email.getText().toString().trim()).matches()) {
            msg =getString(R.string.Please_Enter_valid_email_address);
        } else if (mSelectState == 0) {
            msg =getString(R.string.Please_select_State);
        } else if (mSelectDistrict == 0) {
            msg =getString(R.string.Please_select_district);
        } else if ((mListRoleName.get(mSelectRole).equalsIgnoreCase("HM") || mListRoleName.get(mSelectRole).equalsIgnoreCase("CRC")
                || mListRoleName.get(mSelectRole).equalsIgnoreCase("BEO") || mListRoleName.get(mSelectRole).equalsIgnoreCase("BEO")
                || mListRoleName.get(mSelectRole).equalsIgnoreCase("MT") || mListRoleName.get(mSelectRole).equalsIgnoreCase("TC"))
                && (mSelectTaluka == 0)) {
            msg =getString(R.string.Please_select_taluka);
        } else if ((mListRoleName.get(mSelectRole).equalsIgnoreCase("HM") || mListRoleName.get(mSelectRole).equalsIgnoreCase("CRC")) && (mSelectCluster == 0)) {
            msg =getString(R.string.Please_select_custer);
        } else if (mListRoleName.get(mSelectRole).equalsIgnoreCase("HM") && (mSelectVillage == 0)) {
            msg =getString(R.string.Please_select_village);
        } else if (mListRoleName.get(mSelectRole).equalsIgnoreCase("HM") && (mSelectSchoolName == 0)) {
            msg =getString(R.string.Please_select_school);
        }
        if (TextUtils.isEmpty(msg))
            return true;
        Utills.showToast(msg, this);
        return false;
    }


    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_role:
                mSelectRole = i;
                if (mListRoleName.get(i).equalsIgnoreCase("HM")) {
                    spinner_taluka.setVisibility(View.VISIBLE);
                    txt_taluka.setVisibility(View.VISIBLE);
                    spinner_cluster.setVisibility(View.VISIBLE);
                    txt_cluster.setVisibility(View.VISIBLE);
                    spinner_village.setVisibility(View.VISIBLE);
                    txt_village.setVisibility(View.VISIBLE);
                    spinner_school_name.setVisibility(View.VISIBLE);
                    txt_school.setVisibility(View.VISIBLE);
                    input_school_code.setVisibility(View.VISIBLE);
                }
                if (mListRoleName.get(i).equalsIgnoreCase("CRC")) {
                    spinner_taluka.setVisibility(View.VISIBLE);
                    txt_taluka.setVisibility(View.VISIBLE);
                    spinner_cluster.setVisibility(View.VISIBLE);
                    txt_cluster.setVisibility(View.VISIBLE);
                    spinner_village.setVisibility(View.GONE);
                    txt_village.setVisibility(View.GONE);
                    spinner_school_name.setVisibility(View.GONE);
                    txt_school.setVisibility(View.GONE);
                    input_school_code.setVisibility(View.GONE);
                } else if (mListRoleName.get(i).equalsIgnoreCase("BEO")) {
                    spinner_taluka.setVisibility(View.VISIBLE);
                    txt_taluka.setVisibility(View.VISIBLE);
                    spinner_cluster.setVisibility(View.GONE);
                    txt_cluster.setVisibility(View.GONE);
                    spinner_village.setVisibility(View.GONE);
                    txt_village.setVisibility(View.GONE);
                    spinner_school_name.setVisibility(View.GONE);
                    txt_school.setVisibility(View.GONE);
                    input_school_code.setVisibility(View.GONE);
                } else if (mListRoleName.get(i).equalsIgnoreCase("DEO")) {
                    spinner_taluka.setVisibility(View.GONE);
                    txt_taluka.setVisibility(View.GONE);
                    spinner_cluster.setVisibility(View.GONE);
                    txt_cluster.setVisibility(View.GONE);
                    spinner_village.setVisibility(View.GONE);
                    txt_village.setVisibility(View.GONE);
                    spinner_school_name.setVisibility(View.GONE);
                    txt_school.setVisibility(View.GONE);
                    input_school_code.setVisibility(View.GONE);
                } else if (mListRoleName.get(i).equalsIgnoreCase("MT")) {
                    spinner_taluka.setVisibility(View.VISIBLE);
                    txt_taluka.setVisibility(View.VISIBLE);
                    spinner_cluster.setVisibility(View.GONE);
                    txt_cluster.setVisibility(View.GONE);
                    spinner_village.setVisibility(View.GONE);
                    txt_village.setVisibility(View.GONE);
                    spinner_school_name.setVisibility(View.GONE);
                    txt_school.setVisibility(View.GONE);
                    input_school_code.setVisibility(View.GONE);
                } else if (mListRoleName.get(i).equalsIgnoreCase("TC")) {
                    spinner_taluka.setVisibility(View.VISIBLE);
                    txt_taluka.setVisibility(View.VISIBLE);
                    spinner_cluster.setVisibility(View.GONE);
                    txt_cluster.setVisibility(View.GONE);
                    spinner_village.setVisibility(View.GONE);
                    txt_village.setVisibility(View.GONE);
                    spinner_school_name.setVisibility(View.GONE);
                    txt_school.setVisibility(View.GONE);
                    input_school_code.setVisibility(View.GONE);
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
                mListTaluka.add("Select");
                mListDistrict.add("Select");
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
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
                mListSchoolName.add("Select");
                school_adapter.notifyDataSetChanged();
                edit_text_school_code.setText("");
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                if (mSelectSchoolName != 0)
                    edit_text_school_code.setText(mListCode.get(i));
                else
                    edit_text_school_code.setText("");
                break;
        }
    }

    private void getCluster() {
        Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getCluster_Name__c?StateName=Maharashtra&district=" + mListDistrict.get(mSelectDistrict) + "&taluka=" + mListTaluka.get(mSelectTaluka);
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
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
                    if (!isAdd && !isClusterSet) {
                        isClusterSet = true;
                        for (int i = 0; i < mListCluster.size(); i++) {
                            if (mListCluster.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getCluster())) {
                                binding.spinnerCluster.setSelection(i);
                                break;
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

    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getTaluka_Name__c?DistrictName=" + mListDistrict.get(mSelectDistrict) + "&StateName=Maharashtra";


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
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
                    if (!isAdd && !isTalukaSet) {
                        isTalukaSet = true;
                        for (int i = 0; i < mListTaluka.size(); i++) {
                            if (mListTaluka.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getTaluka())) {
                                binding.spinnerTaluka.setSelection(i);
                                break;
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

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getVillage_Name__c?StateName=Maharashtra&district=" + mListDistrict.get(mSelectDistrict) + "&taluka=" + mListTaluka.get(mSelectTaluka) + "&Cluster=" + mListCluster.get(mSelectCluster);


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
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
                    if (!isAdd && !isVillageSet) {
                        isVillageSet = true;
                        for (int i = 0; i < mListVillage.size(); i++) {
                            if (mListVillage.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getVillage())) {
                                binding.spinnerVillage.setSelection(i);
                                break;
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

    private void getSchool() {
        Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getSchoolName?StateName=Maharashtra&district=" + mListDistrict.get(mSelectDistrict) + "&taluka=" + mListTaluka.get(mSelectTaluka) + "&Cluster=" + mListCluster.get(mSelectCluster) + "&Village=" + mListVillage.get(mSelectVillage);


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListSchoolName.clear();
                    mListCode.clear();
                    mListSchoolName.add("Select");
                    mListCode.add("");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonObject = jsonArr.getJSONObject(i);
                        mListSchoolName.add(jsonObject.getString("school_name"));
                        mListCode.add(jsonObject.getString("school_code"));
                    }
                    school_adapter.notifyDataSetChanged();
                    if (!isAdd && !isSchoolSet) {
                        isSchoolSet = true;
                        for (int i = 0; i < mListSchoolName.size(); i++) {
                            if (mListSchoolName.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getSchool_Name())) {
                                binding.spinnerSchoolName.setSelection(i);
                                break;
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

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
}
