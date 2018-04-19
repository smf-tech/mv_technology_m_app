package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLeaveDetailBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLeaveDetailBinding binding;
    private PreferenceHelper preferenceHelper;
    String userId, comment;
    String isSave;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    ArrayAdapter spinnerAdapter;
    User mUser;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_detail);
        binding.setActivity(this);
        context = this;
        binding.inputHrFormDate.setOnClickListener(this);
        binding.inputHrToDate.setOnClickListener(this);
        binding.btnHrSubmit.setOnClickListener(this);
        setActionbar(getString(R.string.leave_detail));
        preferenceHelper = new PreferenceHelper(this);
        // setActionbar(getString(R.string.team_user_approval));
        if (Utills.isConnected(this)) {

        }
        initViews();

    }

    private void initViews() {
        spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.array_of_leaves));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTypeOfLeaves.setPrompt(getString(R.string.type_of_leaves));
        binding.spTypeOfLeaves.setAdapter(spinnerAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.input_hr_form_date:
                showDateDialog(context, binding.inputHrFormDate);
                break;

            case R.id.input_hr_to_date:
                showDateDialog(context, binding.inputHrToDate);
                break;

            case R.id.btn_hr_submit:
                sendHRServer();
                break;
        }
    }

    private void sendHRServer() {
        String msg = "";
        if (binding.spTypeOfLeaves.getSelectedItem().equals("") || binding.spTypeOfLeaves.getSelectedItem().equals("Select")) {
            msg = "Please Select Category";
        } else if (binding.inputHrFormDate.getText().toString().equals("")) {
            msg = "Please Enter From Date";
        } else if (binding.inputHrToDate.getText().toString().equals("")) {
            msg = "Please Select To Date";
        } else if (!isDatesAreValid(binding.inputHrFormDate.getText().toString().trim(), binding.inputHrToDate.getText().toString().trim())) {
            msg = "Please select proper To date";
        } else if (binding.etReason.getText().toString().equals("")) {
            msg = "Please Enter Reason Of Leave";
        }
        if (msg.isEmpty()) {
            sendHRLeavesDataToServer();
        } else {
            Utills.showToast(msg, context);
        }
    }

    private boolean isDatesAreValid(String startDate, String endDate) {
        try {
            DateFormat formatter;
            Date fromDate, toDate;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            fromDate = formatter.parse(startDate);
            toDate = formatter.parse(endDate);
            if (fromDate.before(toDate))
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendHRLeavesDataToServer() {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);

                final JSONObject jsonObject = new JSONObject();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject.put("Leave_Type__c", binding.spTypeOfLeaves.getSelectedItem());
                jsonObject.put("Reason__c", binding.etReason.getText().toString());
                jsonObject.put("Requested_User__c", User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                jsonObject.put("Status__c", "Pending");
                jsonObject.put("From__c", binding.inputHrFormDate.getText().toString());
                jsonObject.put("To__c", binding.inputHrToDate.getText().toString());
                jsonObject1.put("leave", jsonObject);
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.SendHRLeavesDataToServer, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        Utills.showToast("Leave Approval sent successfully...", LeaveDetailActivity.this);
                        finish();
                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }
}
