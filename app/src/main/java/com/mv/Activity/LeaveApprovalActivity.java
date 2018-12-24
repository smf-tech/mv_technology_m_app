package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Adapter.ExpandableApprovalListAdapter;
import com.mv.Adapter.LeaveAdapter;
import com.mv.Model.HolidayListModel;
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
import com.mv.databinding.ActivityLeaveApprovalBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveApprovalActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity mContext;
    private ActivityLeaveApprovalBinding binding;
    private PreferenceHelper preferenceHelper;
    //private ExpandableApprovalListAdapter adapter;

//    private ArrayList<String> headerList;
//    private HashMap<String, ArrayList<LeavesModel>> childList;
    private List<LeavesModel> leaveList = new ArrayList<>();
    private List<LeavesModel> leaveSortedList = new ArrayList<>();
    ArrayList<LeavesModel> pendingList = new ArrayList<>();
    ArrayList<LeavesModel> approveList = new ArrayList<>();
    ArrayList<LeavesModel> rejectList = new ArrayList<>();
    private LeaveAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout sortLayout;
    private Button btn_pending;
    private Button btn_approve;
    private Button btn_reject;
    private String sortString;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_approval);
        binding.setProcesslist(this);
        EditText editTextSort = (EditText) findViewById(R.id.edit_text_email);
        sortLayout = (LinearLayout)findViewById(R.id.sort_layout);
        editTextSort.addTextChangedListener(watch);
        recyclerView = (RecyclerView) findViewById(R.id.rv_process);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

//        childList = new HashMap<>();
//        headerList = new ArrayList<>();
//        headerList.add(getString(R.string.pending));
//        headerList.add(getString(R.string.reject));
//        headerList.add(getString(R.string.approve));

        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);

        //storing process Id to preference to use later
        if (preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetApproveLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.GONE);
            sortLayout.setVisibility(View.VISIBLE);

        } else {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetAllMyLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.VISIBLE);
            sortLayout.setVisibility(View.GONE);
        }

        preferenceHelper.insertString(Constants.PROCESS_ID, "");
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        setActionbar(getString(R.string.leave));
        btn_pending = (Button) findViewById(R.id.btn_pending);
        btn_approve = (Button) findViewById(R.id.btn_approve);
        btn_reject = (Button) findViewById(R.id.btn_reject);
        btn_pending.setOnClickListener(this);
        btn_approve.setOnClickListener(this);
        btn_reject.setOnClickListener(this);
        //by default pending status list will be loaded.
        btn_pending.setBackgroundResource(R.drawable.selected_btn_background);
        btn_approve.setBackgroundResource(R.drawable.light_grey_btn_background);
        btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
        sortString = "Pending";

    //    adapter = new ExpandableApprovalListAdapter(mContext, headerList, childList, "");
    //    binding.rvProcess.setAdapter(adapter);
    }

    private void setActionbar(String title) {
        String str = title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
    }

    public void deleteLeave(final String id) {
        Utills.showProgressDialog(mContext, "Loading ", mContext.getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/deleteLeave?leaveId=" + id
                + "&userId=" + User.getCurrentUser(mContext).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

                            ArrayList<LeavesModel> pendingList = new ArrayList<>();
                            ArrayList<LeavesModel> approveList = new ArrayList<>();
                            ArrayList<LeavesModel> rejectList = new ArrayList<>();
                            leaveList.clear();
                            leaveSortedList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);

                                LeavesModel leavesModel = new LeavesModel();
                                leavesModel.setId(data.getString("Id"));
                                leavesModel.setFromDate(data.getString("From__c"));
                                leavesModel.setToDate(data.getString("To__c"));

                                if (data.has("Reason__c")) {
                                    leavesModel.setReason(data.getString("Reason__c"));
                                }

                                leavesModel.setTypeOfLeaves(data.getString("Leave_Type__c"));
                                leavesModel.setStatus(data.getString("Status__c"));

                                if (data.has("isHalfDay__c")) {
                                    leavesModel.setHalfDayLeave(data.getBoolean("isHalfDay__c"));
                                } else {
                                    leavesModel.setHalfDayLeave(false);
                                }

                                leavesModel.setRequested_User__c(data.getString("Requested_User__c"));

                                if (data.has("Requested_User_Name__c")) {
                                    leavesModel.setRequested_User_Name__c(data.getString("Requested_User_Name__c"));
                                }
                                if (data.has("Comment__c")) {
                                    leavesModel.setComment(data.getString("Comment__c"));
                                }
//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusApprove)) {
//                                    approveList.add(leavesModel);
//                                }
//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
//                                    pendingList.add(leavesModel);
//                                }
//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusRejected)) {
//                                    rejectList.add(leavesModel);
//                                }
                                leaveList.add(leavesModel);
                            }

//                            childList.put(getString(R.string.pending), pendingList);
//                            childList.put(getString(R.string.reject), rejectList);
//                            childList.put(getString(R.string.approve), approveList);
//
//                            adapter = new ExpandableApprovalListAdapter(mContext, headerList, childList, "");
//                            binding.rvProcess.setAdapter(adapter);
                            setRecyclerView(sortString);
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
    protected void onResume() {
        super.onResume();
        leaveList.clear();
        leaveSortedList.clear();
        if (Utills.isConnected(this)) {
            getAllProcess();
        } else {
            Utills.showToast(getString(R.string.error_no_internet), mContext);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_pending:
                sortString = "Pending";
                btn_pending.setBackgroundResource(R.drawable.selected_btn_background);
                btn_approve.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;
            case R.id.btn_approve:
                sortString = "Approved";
                btn_pending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_approve.setBackgroundResource(R.drawable.selected_btn_background);
                btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;
            case R.id.btn_reject:
                sortString = "Rejected";
                btn_pending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_approve.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_reject.setBackgroundResource(R.drawable.selected_btn_background);
                setRecyclerView(sortString);
                break;
        }
    }

    public void onAddClick() {
        ArrayList<HolidayListModel> tem = new ArrayList<>(new ArrayList<>());
        Intent intent = new Intent(mContext, LeaveDetailActivity.class);
        intent.putParcelableArrayListExtra(Constants.PROCESS_ID, tem);
        mContext.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getAllProcess() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);

                                LeavesModel leavesModel = new LeavesModel();
                                leavesModel.setId(data.getString("Id"));
                                leavesModel.setFromDate(data.getString("From__c"));
                                leavesModel.setToDate(data.getString("To__c"));

                                if (data.has("Reason__c")) {
                                    leavesModel.setReason(data.getString("Reason__c"));
                                }
                                leavesModel.setTypeOfLeaves(data.getString("Leave_Type__c"));
                                leavesModel.setStatus(data.getString("Status__c"));

                                if (data.has("Comment__c")) {
                                    leavesModel.setComment(data.getString("Comment__c"));
                                }
                                leavesModel.setRequested_User__c(data.getString("Requested_User__c"));

                                if (data.has("Requested_User_Name__c")) {
                                    leavesModel.setRequested_User_Name__c(data.getString("Requested_User_Name__c"));
                                }

                                if (data.has("isHalfDay__c")) {
                                    leavesModel.setHalfDayLeave(data.getBoolean("isHalfDay__c"));
                                } else {
                                    leavesModel.setHalfDayLeave(false);
                                }

//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusApprove)) {
//                                    approveList.add(leavesModel);
//                                }
//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusPending)) {
//                                    pendingList.add(leavesModel);
//                                }
//                                if (leavesModel.getStatus().equals(Constants.LeaveStatusRejected)) {
//                                    rejectList.add(leavesModel);
//                                }
                                leaveList.add(leavesModel);
                            }

                            AppDatabase.getAppDatabase(LeaveApprovalActivity.this).userDao().deleteAllLeaves();
//                            AppDatabase.getAppDatabase(LeaveApprovalActivity.this).userDao().insertLeaves(pendingList);
//                            AppDatabase.getAppDatabase(LeaveApprovalActivity.this).userDao().insertLeaves(rejectList);
//                            AppDatabase.getAppDatabase(LeaveApprovalActivity.this).userDao().insertLeaves(approveList);
                            AppDatabase.getAppDatabase(LeaveApprovalActivity.this).userDao().insertLeaves(leaveList);

//                            childList.put(getString(R.string.pending), pendingList);
//                            childList.put(getString(R.string.reject), rejectList);
//                            childList.put(getString(R.string.approve), approveList);
//
//                            adapter = new LeaveAdapter(mContext, pendingList);
//                            binding.rvProcess.setAdapter(adapter);
                            setRecyclerView(sortString);
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

    private void setRecyclerView(String Status) {
        leaveSortedList.clear();
        for(int i=0;i<leaveList.size();i++){
            if(leaveList.get(i).getStatus().equals(Status)){
                leaveSortedList.add(leaveList.get(i));
            }
        }
//        if(Status.equalsIgnoreCase("Pending")){
//            adapter = new LeaveAdapter(mContext, pendingList);
//        }else if(Status.equalsIgnoreCase("Approved")){
//            adapter = new LeaveAdapter(mContext, approveList);
//        }else if(Status.equalsIgnoreCase("Rejected")){
//            adapter = new LeaveAdapter(mContext, rejectList);
//        }
        adapter = new LeaveAdapter(mContext, leaveSortedList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve) && leaveSortedList.size()==0) {
            Utills.showToast("No data available.",this);
            sortLayout.setVisibility(View.GONE);
        }
//        else{
//            sortLayout.setVisibility(View.VISIBLE);
//        }
    }

    //added this code for sorting vouchers by username
    private TextWatcher watch = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            // TODO Auto-generated method stub
            setFilter(s.toString());

        }
    };
    private void setFilter(String s) {
        List<LeavesModel> list = new ArrayList<>();
//        attendanceSortedList.clear();
//        for (int i = 0; i < attendanceList.size(); i++) {
//            attendanceSortedList.add(attendanceList.get(i));
//        }
        list.clear();
        for (int i = 0; i < leaveSortedList.size(); i++) {
            if (leaveSortedList.get(i).getRequested_User__c()!=null && leaveSortedList.get(i).getRequested_User_Name__c().toLowerCase().contains(s.toLowerCase())) {
                list.add(leaveSortedList.get(i));
            }
        }

        adapter = new LeaveAdapter(mContext, list);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
