package com.mv.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.LeavesModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLeaveDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    String status;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    ArrayAdapter spinnerAdapter;
    User mUser;
    ArrayList<String> typeOfleaves=new ArrayList<>();
    LeavesModel leavesModel;
    Context context;
    String leaveId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_detail);
        binding.setActivity(this);
        context = this;
        preferenceHelper = new PreferenceHelper(this);
        binding.inputHrFormDate.setOnClickListener(this);
        binding.inputHrToDate.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.btnApprove.setOnClickListener(this);
        binding.btnReject.setOnClickListener(this);

        // setActionbar(getString(R.string.team_user_approval));
        initViews();
        if(getIntent().getParcelableExtra(Constants.Leave)!=null)
        { leavesModel=getIntent().getParcelableExtra(Constants.Leave);
           leaveId= leavesModel.getId();
           binding.spTypeOfLeaves.setSelection(typeOfleaves.indexOf(leavesModel.getTypeOfLeaves()));
           binding.inputHrFormDate.setText(leavesModel.getFromDate());
            binding.inputHrToDate.setText(leavesModel.getToDate());
            binding.etReason.setText(leavesModel.getReason());
            if(preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve))
            {
                if(!leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
                    binding.leaveRemark.setVisibility(View.VISIBLE);
                    if(leavesModel.getComment()!=null&&!leavesModel.getComment().equals(""))
                        binding.leaveRemark.setText("Remark : " +leavesModel.getComment());
                    else
                        binding.leaveRemark.setText("Remark : Approved " );
                }
                else
                {
                    binding.leaveRemark.setVisibility(View.GONE);
                }
                binding.btnSubmit.setVisibility(View.GONE);
                binding.btnApprove.setVisibility(View.VISIBLE);
                binding.btnReject.setVisibility(View.VISIBLE);
                binding.inputHrFormDate.setEnabled(false);
                binding.inputHrToDate.setEnabled(false);
                binding.spTypeOfLeaves.setEnabled(false);
                binding.etReason.setEnabled(false);
            }
            else
            {
                binding.btnSubmit.setVisibility(View.VISIBLE);
                binding.btnApprove.setVisibility(View.GONE);
                binding.btnReject.setVisibility(View.GONE);
                if(leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
                    binding.leaveRemark.setVisibility(View.GONE);
                    binding.inputHrFormDate.setEnabled(true);
                    binding.inputHrToDate.setEnabled(true);
                    binding.spTypeOfLeaves.setEnabled(true);
                    binding.etReason.setEnabled(true);
                    binding.btnSubmit.setVisibility(View.VISIBLE);

                }
                else
                {
                    binding.leaveRemark.setVisibility(View.VISIBLE);
                    if(leavesModel.getComment()!=null&&!leavesModel.getComment().equals(""))
                    binding.leaveRemark.setText("Remark : " +leavesModel.getComment());
                    else
                        binding.leaveRemark.setText("Remark : Approved " );
                    binding.inputHrFormDate.setEnabled(false);
                    binding.inputHrToDate.setEnabled(false);
                    binding.spTypeOfLeaves.setEnabled(false);
                    binding.etReason.setEnabled(false);
                    binding.btnSubmit.setVisibility(View.GONE);

                }
            }
        }
        else
        {
            leaveId=null;
        }


    }

    private void initViews() {


        typeOfleaves.add("CL/SL");
        typeOfleaves.add("Paid");
        typeOfleaves.add("Unpaid");
        typeOfleaves.add("Comp Off");
        typeOfleaves.add("Half Day");
        setActionbar(getString(R.string.leave_detail));
        spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, typeOfleaves);
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

            case R.id.btn_submit:
                sendHRServer();
                break;

            case R.id.btn_approve:
                if (leavesModel.getStatus() != null && leavesModel.getStatus().equalsIgnoreCase("Approved")) {
                    Utills.showToast("Leave Already Approved", context);
                } else {
                    comment = "";
                    status = "Approved";

                    sendApprovedData();
                }
                break;

            case R.id.btn_reject:
                if (leavesModel.getStatus() != null && leavesModel.getStatus().equalsIgnoreCase("Rejected")
                        && leavesModel.getComment() != null
                        && leavesModel.getComment().length() > 0) {
                    Utills.showToast("Leave Already Rejected", context);
                } else {
                    showDialog();
                }
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
            if (fromDate.before(toDate)||fromDate.equals(toDate))
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
                if(leaveId!=null)
                jsonObject.put("Id", leaveId);

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


    private void sendApprovedData() {
        if (Utills.isConnected(this)) {

                Utills.showProgressDialog(this, getString(R.string.leave_approoval), getString(R.string.progress_please_wait));
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.UpdateLeaveStatus+"?leaveId=" +leavesModel.getId() + "&status=" +status+ "&approvedUserId=" +User.getCurrentUser(getApplicationContext()).getMvUser().getId()+ "&comment=" +comment;

                apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast(getString(R.string.submitted_successfully), context);
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), context);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), context);
                    }
                });

        } else {
            Utills.showToast(getString(R.string.error_no_internet), context);
        }
    }

    public void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.comments));
        alertDialog.setMessage(getString(R.string.enter_comment));

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        status = "Rejected";
                        comment = input.getText().toString();
                        if (!comment.isEmpty()) {
                            sendApprovedData();
                        } else {
                            Utills.showToast("Please Enter Comment", context);
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }
}
