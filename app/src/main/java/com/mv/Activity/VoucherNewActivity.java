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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.mv.databinding.ActivityVoucherNewBinding;

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
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class VoucherNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityVoucherNewBinding binding;
    private int mProjectSelect = 0;
    private List<String> projectList = new ArrayList<>();
    private Voucher mVoucher;
    private boolean isAdd;

    private PreferenceHelper preferenceHelper;
    private ArrayAdapter<String> project_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
    }

    private void initViews() {
        projectList = Arrays.asList(getResources().getStringArray(R.array.array_of_project));
        projectList = new ArrayList<String>();
        projectList.add("Select");
        setActionbar(getString(R.string.voucher_new));
        binding.txtDate.setOnClickListener(this);
        binding.txtDateFrom.setOnClickListener(this);
        binding.txtDateTo.setOnClickListener(this);
        binding.spinnerProject.setOnItemSelectedListener(this);
        preferenceHelper = new PreferenceHelper(this);
        binding.txtDate.setText(getCurrentDate());
        project_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, projectList);
        project_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProject.setAdapter(project_adapter);
        if (Utills.isConnected(this))
            getProject();
        if (getIntent().getExtras()!=null && getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;

        } else {
            isAdd = false;
            mVoucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
            binding.txtDate.setText(mVoucher.getDate());
            binding.txtDateFrom.setText(mVoucher.getFromDate());
            binding.editTextPlace.setText(mVoucher.getPlace());
            binding.txtDateTo.setText(mVoucher.getToDate());
            binding.editTextCount.setText(mVoucher.getNoOfPeople());
            binding.editTextDescription.setText(mVoucher.getDecription());
        }
        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            binding.btnSubmit.setVisibility(View.GONE);
            binding.txtDate.setEnabled(false);
            binding.txtDateFrom.setEnabled(false);
            binding.editTextPlace.setEnabled(false);
            binding.txtDateTo.setEnabled(false);
            binding.editTextCount.setEnabled(false);
            binding.editTextDescription.setEnabled(false);
            binding.spinnerProject.setEnabled(false);
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
        //updated project fetch api. getting projects of selected org.
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetProjectDataUrl+"?org="+User.getCurrentUser(VoucherNewActivity.this).getMvUser().getOrganisation();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {

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
                                mProjectSelect = projectList.lastIndexOf(mVoucher.getProject());
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
                showDateDialog(binding.txtDate);
                break;
            case R.id.txtDateFrom:
                showDateDialog(binding.txtDateFrom);
                break;
            case R.id.txtDateTo:
                showDateDialog(binding.txtDateTo);
                break;
        }
    }

    private void showDateDialog(TextView textView) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        textView.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
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
            Voucher voucher = new Voucher();
            if (!isAdd) {
                voucher.setUniqueId(mVoucher.getUniqueId());
                voucher.setId(mVoucher.getId());
            }
            voucher.setProject(projectList.get(mProjectSelect));
            voucher.setDate(binding.txtDate.getText().toString().trim());
            voucher.setPlace(binding.editTextPlace.getText().toString().trim());
            voucher.setFromDate(binding.txtDateFrom.getText().toString().trim());
            voucher.setToDate(binding.txtDateTo.getText().toString().trim());
            voucher.setDecription(binding.editTextDescription.getText().toString().trim());
            voucher.setNoOfPeople(binding.editTextCount.getText().toString().trim());
            voucher.setUser(User.getCurrentUser(this).getMvUser().getId());
            addVoucher(voucher);
        }
    }

    private void addVoucher(final Voucher voucher) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(voucher);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertVoucher", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        JSONArray array = object.getJSONArray("Records");
                                        if (array.length() != 0) {
                                            voucher.setId(array.getJSONObject(0).getString("Id"));
                                        }
                                        AppDatabase.getAppDatabase(VoucherNewActivity.this).userDao().insertVoucher(voucher);
                                        Utills.showToast("Voucher Added successfully", VoucherNewActivity.this);
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

    private boolean isValid() {

        String str = "";
        if (mProjectSelect == 0) {
            str = "Please select Project";
        } else if (binding.editTextPlace.getText().toString().trim().length() == 0) {
            str = "Please enter Place";
        } else if (binding.txtDate.getText().toString().trim().length() == 4) {
            str = "Please select the date";
        } else if (binding.txtDateFrom.getText().toString().trim().length() == 0) {
            str = "Please select the From date";
        } else if (binding.txtDateTo.getText().toString().trim().length() == 0) {
            str = "Please select the To date";
        } else if (!isDatesAreValid(binding.txtDateFrom.getText().toString().trim(), binding.txtDateTo.getText().toString().trim())) {
            str = "Please select proper To date";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description ";
        } else if (binding.editTextCount.getText().toString().trim().length() == 0) {
            str = "Please enter No Of People Travelled";
        }
        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
    }

    private boolean isDatesAreValid(String startDate, String endDate) {
        try {
            DateFormat formatter;
            Date fromDate, toDate;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            fromDate = formatter.parse(startDate);
            toDate = formatter.parse(endDate);
            if (fromDate.before(toDate) || fromDate.equals(toDate))
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
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
