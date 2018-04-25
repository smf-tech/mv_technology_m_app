package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.ExpandableApprovalListAdapter;
import com.mv.Model.LeavesModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LeaveApprovalActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLeaveApprovalBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    ArrayList<String> idList;
        private PreferenceHelper preferenceHelper;
    private ArrayList<String> headerList;
    HashMap<String, ArrayList<LeavesModel>> childList;
    ArrayList<LeavesModel> leaveList = new ArrayList<>();
    String proceesId, Processname;
    Activity mContext;

    TextView textNoData;
    private ExpandableApprovalListAdapter adapter;
    String url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_approval);
        binding.setProcesslist(this);



        headerList=new ArrayList<>();
        childList=new HashMap<>();
        headerList.add(getString(R.string.pending));
        headerList.add(getString(R.string.reject));
        headerList.add(getString(R.string.approve));

/*        proceesId = getIntent().getExtras().getString(Constants.PROCESS_ID);
        Processname = getIntent().getExtras().getString(Constants.PROCESS_NAME);*/
        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {


        preferenceHelper = new PreferenceHelper(this);
        //storing process Id to preference to use later
        if(preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetApproveLeave+ "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.GONE);
        }
        else {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetAllMyLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.VISIBLE);
        }

        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        textNoData = (TextView) findViewById(R.id.textNoData);
        setActionbar(getString(R.string.leave));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
/*        binding.rvProcess.setLayoutManager(mLayoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());*/
        adapter = new ExpandableApprovalListAdapter(mContext,headerList,childList );
        binding.rvProcess.setAdapter(adapter);


    }
    public void deleteLeave(final String id) {
        Utills.showProgressDialog(mContext, "Loading ",mContext. getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/deleteLeave?leaveId=" + id+ "&userId=" +User.getCurrentUser(mContext).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

                            ArrayList <LeavesModel> pendingList=new ArrayList<>();
                            ArrayList <LeavesModel> approveList=new ArrayList<>();
                            ArrayList <LeavesModel> rejectList=new ArrayList<>();
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject data=jsonArray.getJSONObject(i);

                                LeavesModel leavesModel=new LeavesModel();
                                leavesModel.setId(data.getString("Id"));
                                leavesModel.setFromDate(data.getString("From__c"));
                                leavesModel.setToDate(data.getString("To__c"));
                                leavesModel.setReason(data.getString("Reason__c"));
                                leavesModel.setTypeOfLeaves(data.getString("Leave_Type__c"));
                                leavesModel.setStatus(data.getString("Status__c"));

                                leavesModel.setRequested_User__c(data.getString("Requested_User__c"));
                                if(data.has("Requested_User_Name__c"))
                                    leavesModel.setRequested_User_Name__c(data.getString("Requested_User_Name__c"));
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusApprove))
                                    approveList.add(leavesModel);
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusPending))
                                    pendingList.add(leavesModel);
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusRejected))
                                    rejectList.add(leavesModel);
                            }
                            childList.put(getString(R.string.pending),pendingList);
                            childList.put(getString(R.string.reject),rejectList);
                            childList.put(getString(R.string.approve),approveList);
                            adapter = new ExpandableApprovalListAdapter(mContext,headerList,childList );
                            binding.rvProcess.setAdapter(adapter);


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
        if (Utills.isConnected(this))
            getAllProcess();
        else
            Utills.showToast(getString(R.string.error_no_internet),mContext);

    }



    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
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
        }
    }

    public void onAddClick() {

        Intent openClass = new Intent(mContext, LeaveDetailActivity.class);
        startActivity(openClass);
       overridePendingTransition(R.anim.right_in, R.anim.left_out);

    }

    @Override
    public void onBackPressed() {
        // Utills.openActivity(mContext, HomeActivity.class);
        finish();


    }

    public void getAllProcess() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
             try {
                 if (response.body() != null) {
                     String str = response.body().string();
                     if (str != null && str.length() > 0) {
                         JSONArray jsonArray = new JSONArray(str);

                         ArrayList <LeavesModel> pendingList=new ArrayList<>();
                         ArrayList <LeavesModel> approveList=new ArrayList<>();
                         ArrayList <LeavesModel> rejectList=new ArrayList<>();
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject data=jsonArray.getJSONObject(i);

                                LeavesModel leavesModel=new LeavesModel();
                                leavesModel.setId(data.getString("Id"));
                                leavesModel.setFromDate(data.getString("From__c"));
                                leavesModel.setToDate(data.getString("To__c"));
                                leavesModel.setReason(data.getString("Reason__c"));
                                leavesModel.setTypeOfLeaves(data.getString("Leave_Type__c"));
                                leavesModel.setStatus(data.getString("Status__c"));

                                leavesModel.setRequested_User__c(data.getString("Requested_User__c"));
                                if(data.has("Requested_User_Name__c"))
                                leavesModel.setRequested_User_Name__c(data.getString("Requested_User_Name__c"));
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusApprove))
                                    approveList.add(leavesModel);
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusPending))
                                    pendingList.add(leavesModel);
                                if(leavesModel.getStatus().equals(Constants.LeaveStatusRejected))
                                    rejectList.add(leavesModel);
                            }
                            childList.put(getString(R.string.pending),pendingList);
                            childList.put(getString(R.string.reject),rejectList);
                            childList.put(getString(R.string.approve),approveList);
                         adapter = new ExpandableApprovalListAdapter(mContext,headerList,childList );
                         binding.rvProcess.setAdapter(adapter);


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

}
