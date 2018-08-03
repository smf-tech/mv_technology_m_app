package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Adavance;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAdavanceNewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class AdavanceNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityAdavanceNewBinding binding;
    private int mProjectSelect = 0;
    private List<String> projectList = new ArrayList<>();
    private Adavance mAdavance;
    private boolean isAdd;
    private PreferenceHelper preferenceHelper;
    private ArrayAdapter<String> project_adapter;
    private Voucher mVoucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_adavance_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
    }

    private void initViews() {
        mVoucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
        preferenceHelper = new PreferenceHelper(this);
        projectList = Arrays.asList(getResources().getStringArray(R.array.array_of_project));
        projectList = new ArrayList<String>();
        projectList.add("Select");
      /*  if (Utills.isConnected(this))
            getProject();*/
        project_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, projectList);
        project_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProject.setAdapter(project_adapter);
        setActionbar(getString(R.string.adavance_new));
        binding.txtDate.setText(getCurrentDate());
        binding.txtDate.setOnClickListener(this);
        binding.spinnerProject.setOnItemSelectedListener(this);
        if (getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;
        } else {
            isAdd = false;
            mAdavance = (Adavance) getIntent().getSerializableExtra(Constants.ADAVANCE);
            binding.txtDate.setText(mAdavance.getDate());
            binding.editTextCount.setText(mAdavance.getAmount());
            binding.editTextDescription.setText(mAdavance.getDecription());
            binding.editApproveRemarks.setText(mAdavance.getRemark__c());
            mProjectSelect = projectList.indexOf(mAdavance.getProject());
            binding.spinnerProject.setSelection(mProjectSelect);
            binding.editApproveAmt.setText(mAdavance.getApproved_Amount__c());

            binding.txtDate.setEnabled(false);
            binding.editTextCount.setEnabled(false);
            binding.editTextDescription.setEnabled(false);
            binding.btnSubmit.setVisibility(View.GONE);
            binding.btnApprove.setOnClickListener(this);
            binding.btnReject.setOnClickListener(this);

            if (mAdavance.getApproved_Amount__c() != null) {
                binding.approveAmt.setVisibility(View.VISIBLE);
                binding.editApproveAmt.setText(mAdavance.getApproved_Amount__c());

            }
            if (mAdavance.getRemark__c() != null) {
                binding.approveRemarks.setVisibility(View.VISIBLE);
                binding.editApproveRemarks.setText(mAdavance.getRemark__c());
            }

            if (mAdavance.getStatus().equalsIgnoreCase("Pending")) {
                binding.editApproveAmt.setText(mAdavance.getAmount());
                binding.approveRemarks.setVisibility(View.VISIBLE);
                binding.linearly.setVisibility(View.VISIBLE);
                binding.editApproveRemarks.setEnabled(true);
                binding.editApproveAmt.setEnabled(true);
            } else {
                binding.linearly.setVisibility(View.GONE);
                binding.editApproveAmt.setEnabled(false);
                binding.editApproveRemarks.setEnabled(false);
            }
            if (!Constants.AccountTeamCode.equals("TeamManagement")) {
                binding.approveRemarks.setVisibility(View.GONE);
                binding.linearly.setVisibility(View.GONE);
                binding.btnSubmit.setVisibility(View.GONE);
                if (mAdavance.getStatus().equalsIgnoreCase("Pending")) {
                    binding.approveAmt.setVisibility(View.GONE);
                    binding.editTextCount.setEnabled(true);
                    binding.editTextDescription.setEnabled(true);
                    binding.btnSubmit.setVisibility(View.VISIBLE);
                } else {
                    binding.editTextCount.setEnabled(false);
                    binding.editTextDescription.setEnabled(false);
                    if(mAdavance.getApproved_Amount__c()!=null){
                        binding.approveAmt.setVisibility(View.VISIBLE);
                        binding.editApproveAmt.setText(mAdavance.getApproved_Amount__c());
                    }
                    if(mAdavance.getRemark__c()!=null){
                        binding.approveRemarks.setVisibility(View.VISIBLE);
                        binding.editApproveRemarks.setText(mAdavance.getRemark__c());
                    }
                }
            }
        }
    }

    public String getCurrentDate() {
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        return formattedDate;
    }

    private void getProject() {

        Utills.showProgressDialog(this, "Loading Projects", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetProjectDataUrl;
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
                            projectList.clear();

                            projectList.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                projectList.add(object.getString("Project_Name__c"));
                            }
                            project_adapter.notifyDataSetChanged();
                            if (!isAdd) {
                                mProjectSelect = projectList.lastIndexOf(mAdavance.getProject());
                                binding.spinnerProject.setSelection(mProjectSelect);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.txtDate:
                //showDateDialog();
                break;
            case R.id.btn_reject:
                if(!binding.editApproveAmt.getText().toString().equals("") && Double.parseDouble(binding.editApproveAmt.getText().toString())>0){
                    Toast.makeText(this,"Please clear approve amount to reject.",Toast.LENGTH_LONG).show();
                }else
                    changeAdvance("Rejected");
                break;
            case R.id.btn_approve:
                //check if approve amount is greater than requested amount
                if(binding.editApproveAmt.getText().toString().trim().length()>0) {
                    if(Integer.parseInt(binding.editApproveAmt.getText().toString().trim())>Integer.parseInt(binding.editTextCount.getText().toString().trim())){
                        Utills.showToast( getString(R.string.valid_advance), this);
                    }else{
                        changeAdvance("Approved");
                    }
                }else{
                    Utills.showToast("Please enter proper Advance to be approve.", this);
                }

                break;
        }
    }

    private void changeAdvance(String status) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                Adavance adavance = mAdavance;
                adavance.setStatus(status);
               // allow manager to edit approvable amount
              //  Add approvedamt filed in advance class
                adavance.setApproved_Amount__c(binding.editApproveAmt.getText().toString().trim());
                adavance.setRemark__c(binding.editApproveRemarks.getText().toString().trim());

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(adavance);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertAdavance", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        Utills.showToast("Status of expense changed successfully", AdavanceNewActivity.this);
                                        finish();
                                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }

    private void addAdavance(final Adavance adavance) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(adavance);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertAdavance", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                if (response != null && response.isSuccess()) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        JSONArray array = object.getJSONArray("Records");
                                        if (array.length() != 0) {
                                            adavance.setId(array.getJSONObject(0).getString("Id"));
                                            adavance.setStatus("Pending");
                                        }
                                        AppDatabase.getAppDatabase(AdavanceNewActivity.this).userDao().insertAdavance(adavance);
                                        Utills.showToast("Adavance Added successfully", AdavanceNewActivity.this);
                                        finish();
                                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }


    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        binding.txtDate.setText(getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    public void onSubmitClick() {
        if (isValid()) {
            Adavance adavance = new Adavance();
            if (!isAdd) {
                adavance = mAdavance;
            }
            adavance.setProject(projectList.get(mProjectSelect));
            adavance.setProject(projectList.get(mProjectSelect));
            adavance.setDate(binding.txtDate.getText().toString().trim());
            adavance.setDecription(binding.editTextDescription.getText().toString().trim());
            adavance.setAmount(binding.editTextCount.getText().toString().trim());
            adavance.setUser(User.getCurrentUser(this).getMvUser().getId());
            adavance.setVoucherId("" + mVoucher.getId());
            addAdavance(adavance);
        }
    }


    private boolean isValid() {
        String str = "";
        if (binding.editTextCount.getText().toString().trim().length() == 0) {
            str = "Please enter Amount";
        } else if (binding.txtDate.getText().toString().trim().length() == 4) {
            str = "Please select the date";
        }
        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnerProject:
                mProjectSelect = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
