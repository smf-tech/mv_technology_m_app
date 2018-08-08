package com.mv.Activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ExpandableApprovalListAdapter;
import com.mv.Adapter.ExpandableAttendanceApprovalListAdapter;
import com.mv.Model.Attendance;
import com.mv.Model.AttendanceApproval;
import com.mv.Model.HolidayListModel;
import com.mv.Model.LeavesModel;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAttendanceApprovalBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceApprovalActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAttendanceApprovalBinding binding;
    private PreferenceHelper preferenceHelper;
    private ArrayList<String> headerList;
    HashMap<String, ArrayList<AttendanceApproval>> childList;
    private List<AttendanceApproval> attendanceList = new ArrayList<>();
    String proceesId, Processname,tabName;
    Activity mContext;
    List<HolidayListModel> holidayListModels = new ArrayList<>();
    TextView textNoData;

    private ExpandableAttendanceApprovalListAdapter adapter;
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_approval);
        mContext = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attendance_approval);
        binding.setProcesslist(this);
        headerList = new ArrayList<>();
        childList = new HashMap<>();
        headerList.add(getString(R.string.pending));
        headerList.add(getString(R.string.reject));
        headerList.add(getString(R.string.approve));
        initViews();
    }
    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        //storing process Id to preference to use later
        /*if (preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetApproveLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.GONE);

        } else {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetAllMyLeave + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            binding.fabAddProcess.setVisibility(View.VISIBLE);
        }*/

        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        textNoData = (TextView) findViewById(R.id.textNoData);
        setActionbar(getString(R.string.attendance));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
/*        binding.rvProcess.setLayoutManager(mLayoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());*/
        adapter = new ExpandableAttendanceApprovalListAdapter(mContext, headerList, childList,tabName);
        binding.rvProcess.setAdapter(adapter);
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
        img_back.setOnClickListener((View.OnClickListener) this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
        img_logout.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utills.isConnected(this))
            getAllProcess();
        else
            Utills.showToast(getString(R.string.error_no_internet), mContext);
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

    @Override
    public void onBackPressed() {
        // Utills.openActivity(mContext, HomeActivity.class);
        finish();
    }

    public void getAllProcess() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAttendanceForApproval?userId="+User.getCurrentUser(mContext).getMvUser().getId();

       // http://cs57.salesforce.com/services/apexrest/getAttendanceApprovalForUser?userId=a1J0k000000cMTG

     //   http://cs57.salesforce.com/services/apexrest/getAttendanceForApproval?userRole=a1I0k000000IOjc

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

                            ArrayList<AttendanceApproval> pendingList = new ArrayList<>();
                            ArrayList<AttendanceApproval> approveList = new ArrayList<>();
                            ArrayList<AttendanceApproval> rejectList = new ArrayList<>();

                            if (Arrays.asList(gson.fromJson(str, AttendanceApproval[].class)) != null) {
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteAllVoucher();
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().insertVoucher();
                                attendanceList = Arrays.asList(gson.fromJson(str, AttendanceApproval[].class));
                                setRecyclerView();

                               /* for (int i = 0; i < attendanceList.size(); i++) {

                                    AttendanceApproval attendance_approval = attendanceList.get(i);

                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusApprove))
                                        approveList.add(attendance_approval);
                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusPending))
                                        pendingList.add(attendance_approval);
                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusRejected))
                                        rejectList.add(attendance_approval);
                                }

                                childList.put(getString(R.string.pending), pendingList);
                                childList.put(getString(R.string.reject), rejectList);
                                childList.put(getString(R.string.approve), approveList);
                                adapter = new ExpandableAttendanceApprovalListAdapter(mContext, headerList, childList, tabName);
                                binding.rvProcess.setAdapter(adapter);*/

                            }
                        }
                    }
                    } catch(JSONException e){
                        e.printStackTrace();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }
    private void setRecyclerView() {
      //  attendanceList = AppDatabase.getAppDatabase(this).userDao().getAllAttendanceApproval();
       /* adapter = new AdavanceAdapter(this, mList);
        binding.rvAdavance.setAdapter(adapter);
        binding.rvAdavance.setHasFixedSize(true);
        binding.rvAdavance.setLayoutManager(new LinearLayoutManager(this));
*/
        ArrayList<AttendanceApproval> pendingList = new ArrayList<>();
        ArrayList<AttendanceApproval> approveList = new ArrayList<>();
        ArrayList<AttendanceApproval> rejectList = new ArrayList<>();

        for (AttendanceApproval attendance_approval : attendanceList) {

            if (attendance_approval.getStatusC().equals(Constants.LeaveStatusApprove))
                approveList.add(attendance_approval);
            if (attendance_approval.getStatusC().equals(Constants.LeaveStatusPending))
                pendingList.add(attendance_approval);
            if (attendance_approval.getStatusC().equals(Constants.LeaveStatusRejected))
                rejectList.add(attendance_approval);
        }
        childList.put(getString(R.string.pending), pendingList);
        childList.put(getString(R.string.reject), rejectList);
        childList.put(getString(R.string.approve), approveList);
        adapter = new ExpandableAttendanceApprovalListAdapter(mContext, headerList, childList, tabName);
        binding.rvProcess.setAdapter(adapter);
    }
}