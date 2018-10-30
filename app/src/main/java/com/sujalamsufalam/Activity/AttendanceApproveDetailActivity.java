package com.sujalamsufalam.Activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sujalamsufalam.Model.AttendanceApproval;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.databinding.ActivityAttendanceApproveDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceApproveDetailActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityAttendanceApproveDetailBinding binding;
    private AttendanceApproval attendanceApproval;
    private PreferenceHelper preferenceHelper;
    private Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attendance_approve_detail);
        binding.setActivity(this);
        mContext = this;
        initViews();
    }

    private void initViews() {
        setActionbar(getString(R.string.attendance));
        attendanceApproval = (AttendanceApproval) getIntent().getSerializableExtra(Constants.Attendance);
        preferenceHelper = new PreferenceHelper(this);
        binding.name.setText(attendanceApproval.getUser_Name__c());
        binding.userRole.setText(attendanceApproval.getUser_Role__c());
        binding.attendanceDate.setText(attendanceApproval.getAttendanceDateC());
        binding.checkinTime.setText(attendanceApproval.getCheckInTimeC());
        binding.checkoutTime.setText(attendanceApproval.getCheckOutTimeC());
        binding.status.setText(attendanceApproval.getStatusC());
        binding.checkinDifference.setText(attendanceApproval.getCheck_In_Location_Difference__c());
        binding.checkoutDifference.setText(attendanceApproval.getCheck_Out_Location_Difference__c());

        if(attendanceApproval.getStatusC().equals("Pending")){
            binding.buttLay.setVisibility(View.VISIBLE);
           /* binding.btnApprove.setVisibility(View.VISIBLE);
            binding.btnReject.setVisibility(View.VISIBLE);*/
            binding.btnApprove.setOnClickListener(this);
            binding.btnReject.setOnClickListener(this);
        }
        if(attendanceApproval.getStatusC().equals("Rejected")){
            binding.approveRemarks.setVisibility(View.VISIBLE);
            binding.editApproveRemarks.setText(attendanceApproval.getReason());
            binding.editApproveRemarks.setEnabled(false);
        }

    }
    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);

        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reject:
                binding.approveRemarks.setVisibility(View.VISIBLE);
                if(binding.editApproveRemarks.getText().toString().trim().length()>0){
                    updateAttendance("Rejected");
                }else{
                    Utills.showToast(getString(R.string.attendance_rejection_reason), getApplicationContext());
                }
                //updateAttendance("Rejected");
                break;

            case R.id.btn_approve:
                updateAttendance("Approved");
                break;

            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }
    private void updateAttendance(String status) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                AttendanceApproval attendance_Approval = attendanceApproval;
                attendance_Approval.setStatusC(status);
                attendance_Approval.setAttendanceDateC(binding.attendanceDate.getText().toString().trim());
                attendance_Approval.setId(attendanceApproval.getId());
                attendance_Approval.setApprover_User__c(User.getCurrentUser(mContext).getMvUser().getId());
                attendance_Approval.setFinal_Status__c(status);
                if(status.equals("Rejected")){
                    attendance_Approval.setReason( binding.editApproveRemarks.getText().toString().trim());
                }

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(attendance_Approval);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/UpdateAttendanceApproval", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        Utills.showToast("Status of attendance updated successfully.", AttendanceApproveDetailActivity.this);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
