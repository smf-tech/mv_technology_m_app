package com.mv.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.HolidayListModel;
import com.mv.Model.LeaveCountModel;
import com.mv.Model.LeavesModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityLeaveDetailBinding;

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

public class LeaveDetailActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ActivityLeaveDetailBinding binding;
    private PreferenceHelper preferenceHelper;
    String userId, comment;
    String status;
    String halfDayCheck = "false";
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    ArrayAdapter spinnerAdapter;
    User mUser;
    SimpleDateFormat formatter;
    ArrayList<String> typeOfleaves = new ArrayList<>();
    ArrayList<String> category = new ArrayList<>();
    LeavesModel leavesModel;
    Context context;
    String leaveId = "",tabName;
    String selected = "";

    List<HolidayListModel> holidayListModels = new ArrayList<>();
    ArrayList<Date> holidayLiistDate = new ArrayList<>();
    LeaveCountModel leaveCountModel = new LeaveCountModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_detail);
        binding.setActivity(this);
        context = this;
        userId = User.getCurrentUser(context).getMvUser().getId();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        preferenceHelper = new PreferenceHelper(this);
        binding.inputHrFormDate.setOnClickListener(this);
        binding.inputHrToDate.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.btnApprove.setOnClickListener(this);
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        binding.btnReject.setOnClickListener(this);
        binding.spTypeOfCatagory.setOnItemSelectedListener(this);
        binding.spTypeOfLeaves.setOnItemSelectedListener(this);

        // holidayListModels = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);


        // setActionbar(getString(R.string.team_user_approval));
        initViews();


        if (getIntent().getParcelableExtra(Constants.Leave) != null) {
            leavesModel = getIntent().getParcelableExtra(Constants.Leave);
            leaveId = leavesModel.getId();
            if (leavesModel.getRequested_User__c() != null)
                userId = leavesModel.getRequested_User__c();

            if (leavesModel.getTypeOfLeaves().equals("Add Comp Off"))
                binding.spTypeOfCatagory.setSelection(category.indexOf(leavesModel.getTypeOfLeaves()));
            else {
                binding.spTypeOfCatagory.setSelection(category.indexOf(leavesModel.getTypeOfLeaves()));
                binding.spTypeOfLeaves.setSelection(typeOfleaves.indexOf(leavesModel.getTypeOfLeaves()));
            }
            binding.inputHrFormDate.setText(leavesModel.getFromDate());
            binding.inputHrToDate.setText(leavesModel.getToDate());
            binding.etReason.setText(leavesModel.getReason());
            if (leavesModel.getIsHalfDayLeave().equals("true")) {
                binding.detailChk.setChecked(true);
            } else if (leavesModel.getIsHalfDayLeave().equals("false")) {
                binding.detailChk.setChecked(false);
            }
            if (preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
                if (!leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
                    binding.leaveRemark.setVisibility(View.VISIBLE);
                    if (leavesModel.getComment() != null && !leavesModel.getComment().equals(""))
                        binding.leaveRemark.setText("Remark : " + leavesModel.getComment());
                    else
                        binding.leaveRemark.setText("Remark : Approved ");
                } else {
                    binding.leaveRemark.setVisibility(View.GONE);
                }
                binding.btnSubmit.setVisibility(View.GONE);
                binding.btnApprove.setVisibility(View.VISIBLE);
                binding.btnReject.setVisibility(View.VISIBLE);
                binding.detailChk.setEnabled(false);
                binding.inputHrFormDate.setEnabled(false);
                binding.inputHrToDate.setEnabled(false);
                binding.spTypeOfLeaves.setEnabled(false);
                binding.etReason.setEnabled(false);
            } else {
                binding.btnSubmit.setVisibility(View.VISIBLE);
                binding.btnApprove.setVisibility(View.GONE);
                binding.btnReject.setVisibility(View.GONE);
                binding.detailChk.setEnabled(true);
                if (leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
                    binding.leaveRemark.setVisibility(View.GONE);
                    binding.inputHrFormDate.setEnabled(true);
                    binding.inputHrToDate.setEnabled(true);
                    binding.spTypeOfLeaves.setEnabled(true);
                    binding.etReason.setEnabled(true);
                    binding.btnSubmit.setVisibility(View.VISIBLE);

                } else {
                    binding.leaveRemark.setVisibility(View.VISIBLE);
                    if (leavesModel.getComment() != null && !leavesModel.getComment().equals(""))
                        binding.leaveRemark.setText("Remark : " + leavesModel.getComment());
                    else
                        binding.leaveRemark.setText("Remark : Approved ");
                    binding.inputHrFormDate.setEnabled(false);
                    binding.inputHrToDate.setEnabled(false);
                    binding.spTypeOfLeaves.setEnabled(false);
                    binding.etReason.setEnabled(false);
                    binding.btnSubmit.setVisibility(View.GONE);

                }
            }
        } else {
            leaveId = null;
        }
        if (AppDatabase.getAppDatabase(LeaveDetailActivity.this).userDao().getAllHolidayList().size() == 0) {
            getHolidayList();
        } else {
            holidayListModels = AppDatabase.getAppDatabase(LeaveDetailActivity.this).userDao().getAllHolidayList();
            holidayLiistDate.clear();
            for (int i = 0; i < holidayListModels.size(); i++) {
                try {
                    holidayLiistDate.add(formatter.parse(holidayListModels.get(i).getHoliday_Date__c()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        getLeaveBalanceCount();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        category.add("Leaves");
        category.add("Add Comp Off");

        typeOfleaves.add("CL/SL");
        typeOfleaves.add("Paid");
        typeOfleaves.add("Unpaid");
        typeOfleaves.add("Comp Off");

        // typeOfleaves.add("Half Day");
        setActionbar(getString(R.string.leave_detail));
        spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, category);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTypeOfCatagory.setPrompt(getString(R.string.catagory));
        binding.spTypeOfCatagory.setAdapter(spinnerAdapter);

        spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, typeOfleaves);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTypeOfLeaves.setPrompt(getString(R.string.type_of_leaves));
        binding.spTypeOfLeaves.setAdapter(spinnerAdapter);

        binding.detailChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    halfDayCheck = "true";
                    binding.inputHrToDate.setEnabled(false);
                    binding.inputHrToDate.setText(binding.inputHrFormDate.getText().toString());
                } else {
                    halfDayCheck = "false";
                    binding.inputHrToDate.setEnabled(true);
                    binding.inputHrToDate.setText("");
                }
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

            case R.id.input_hr_form_date:
                showDateDialog(context, binding.inputHrFormDate);
                break;

            case R.id.input_hr_to_date:
                showDateDialog(context, binding.inputHrToDate);
                break;

            case R.id.btn_submit:
                sendHRServer(Constants.SendData);
                break;

            case R.id.btn_approve:
                if (leavesModel.getStatus() != null && leavesModel.getStatus().equalsIgnoreCase("Approved")) {
                    Utills.showToast("Leave Already Approved", context);
                } else {


                    comment = "";
                    status = "Approved";

                    sendHRServer(Constants.Approval);
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

            case R.id.img_logout:
                Intent intent = new Intent(context, HolidayListActivity.class);
                context.startActivity(intent);
                break;

        }
    }

    private void sendHRServer(String methodeValue) {
        String msg = "";
        if (binding.spTypeOfLeaves.getSelectedItem().equals("") || binding.spTypeOfLeaves.getSelectedItem().equals("Select")) {
            msg = "Please Select Leave Type";
        } else if (binding.inputHrFormDate.getText().toString().equals("")) {
            msg = "Please Enter From Date";
        } else if (binding.inputHrToDate.getText().toString().equals("")) {
            msg = "Please Select To Date";
        } else if (!isDatesAreValid(binding.inputHrFormDate.getText().toString().trim(), binding.inputHrToDate.getText().toString().trim())) {
            msg = "Please select proper To date";
        } else {
            int dateSize = getDates(binding.inputHrFormDate.getText().toString(), binding.inputHrToDate.getText().toString()).size();

            if (binding.spTypeOfCatagory.getSelectedItem().equals("Leaves")) {
                if (binding.spTypeOfLeaves.getSelectedItem().equals("CL/SL") && leaveCountModel.getAvailable_CL_SL_Leave__c() < dateSize) {
                    msg = " CL/SL Leaves Not Available";

                } else if (binding.spTypeOfLeaves.getSelectedItem().equals("Paid") && leaveCountModel.getAvailable_Paid_Leave__c() < dateSize) {
                    msg = "Paid Leaves Leaves Not Available";

                } else if (binding.spTypeOfLeaves.getSelectedItem().equals("Unpaid") && leaveCountModel.getAvailable_Unpaid_Leave__c() < dateSize) {
                    msg = "Unpaid Leaves  eLeaves Not Available";

                } else if (binding.spTypeOfLeaves.getSelectedItem().equals("Comp Off") && leaveCountModel.getAvailable_Comp_Off_Leave__c() < dateSize) {
                    msg = "Comp Off Leaves Not Available";

                } else if (binding.etReason.getText().toString().equals("")) {
                    msg = "Please Enter Reason Of Leave";
                }
            }
        }


        if (msg.isEmpty()) {
            if (methodeValue.equals(Constants.Approval))
                sendApprovedData();
            else if (methodeValue.equals(Constants.SendData))
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
            if (fromDate.before(toDate) || fromDate.equals(toDate))
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
                if (leaveId != null)
                    jsonObject.put("Id", leaveId);

                jsonObject.put("Leave_Type__c", selected);
                jsonObject.put("Reason__c", binding.etReason.getText().toString());
                jsonObject.put("Requested_User__c", User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                jsonObject.put("Status__c", "Pending");
                jsonObject.put("From__c", binding.inputHrFormDate.getText().toString());
                jsonObject.put("To__c", binding.inputHrToDate.getText().toString());
                jsonObject.put("isHalfDay__c", halfDayCheck);
                jsonObject1.put("leave", jsonObject);
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.SendHRLeavesDataToServer, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();

                        if (binding.spTypeOfCatagory.getSelectedItem().equals("Leaves")) {
                            Utills.showToast(getString(R.string.leave_leave_approoval), LeaveDetailActivity.this);;
                        } else {
                            Utills.showToast(getString(R.string.compoff_leave_approoval), LeaveDetailActivity.this);
                        }
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

    private void getLeaveBalanceCount() {
        Utills.showProgressDialog(context, "Loading Leave Balance", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getTotalLeaveAndBalanace?userId=" + userId;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String data = response.body().string();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    leaveCountModel = gson.fromJson(data, LeaveCountModel.class);
                    binding.unpaidAvailable.setText("" + leaveCountModel.getAvailable_Unpaid_Leave__c());
                    binding.unpaidTotal.setText("" + leaveCountModel.getTotal_Unpaid_Leave__c());
                    binding.paidAvailable.setText("" + leaveCountModel.getAvailable_Paid_Leave__c());
                    binding.paidTotal.setText("" + leaveCountModel.getTotal_Paid_Leave__c());

                    binding.clAvailable.setText("" + leaveCountModel.getAvailable_CL_SL_Leave__c());
                    binding.clTotal.setText("" + leaveCountModel.getTotal_CL_SL_Leave__c());

                    binding.compOffAvailable.setText("" + leaveCountModel.getAvailable_Comp_Off_Leave__c());
                    binding.compOffTotal.setText("" + leaveCountModel.getTotal_Comp_Off_Leave__c());

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
        img_logout.setVisibility(View.VISIBLE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
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
                        if (halfDayCheck.equals("true")) {
                            binding.inputHrToDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                        }
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
                    + Constants.UpdateLeaveStatus + "?leaveId=" + leavesModel.getId() + "&status=" + status + "&approvedUserId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId() + "&comment=" + comment;

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

        alertDialog.setPositiveButton(getString(R.string.ok),
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

    private List<Date> getDates(String dateString1, String dateString2) {
        ArrayList<Date> dates = new ArrayList<Date>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            if (!holidayLiistDate.contains(cal1.getTime()))
                dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }


    private void getHolidayList() {
        if (Utills.isConnected(LeaveDetailActivity.this)) {

            Utills.showProgressDialog(context, "Loading Holidays", getString(R.string.progress_please_wait));
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAllHolidays?userId=" + userId;
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        String data = response.body().string();
                        JSONArray jsonArray = new JSONArray(data);
                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                        AppDatabase.getAppDatabase(LeaveDetailActivity.this).userDao().deleteHolidayList();
                        holidayListModels = Arrays.asList(gson.fromJson(jsonArray.toString(), HolidayListModel[].class));
                        AppDatabase.getAppDatabase(LeaveDetailActivity.this).userDao().insertAllHolidayList(holidayListModels);
                        holidayLiistDate.clear();
                        for (int i = 0; i < holidayListModels.size(); i++) {
                            try {
                                holidayLiistDate.add(formatter.parse(holidayListModels.get(i).getHoliday_Date__c()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {

            case R.id.sp_type_of_catagory:
                if (binding.spTypeOfCatagory.getSelectedItem().equals("Leaves")) {
                    binding.llSpinnerLayout.setVisibility(View.VISIBLE);
                    selected = binding.spTypeOfLeaves.getSelectedItem().toString();

                } else {
                    binding.llSpinnerLayout.setVisibility(View.GONE);
                    selected = binding.spTypeOfCatagory.getSelectedItem().toString();

                }
                break;
            case R.id.sp_type_of_leaves:
                selected = binding.spTypeOfLeaves.getSelectedItem().toString();
                break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
