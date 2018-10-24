package com.mv.Activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.AttendanceAdapter;
import com.mv.Model.AttendanceApproval;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceApproval2Activity extends AppCompatActivity implements View.OnClickListener {

    private AttendanceAdapter adapter;
    public List<AttendanceApproval> attendanceList = new ArrayList<>();
    public List<AttendanceApproval> attendanceSortedList = new ArrayList<>();
    TextView textNoData;
    private PreferenceHelper preferenceHelper;
    LinearLayout sortLayout;
    String proceesId;
    EditText edittextsort;
    Activity mContext;
    RecyclerView recyclerView;
    Button btn_pending,btn_approve,btn_reject;
    String sortString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_approval2);
        mContext = this;
        initViews();
    }
    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        textNoData = (TextView) findViewById(R.id.textNoData);
        setActionbar( getString(R.string.attendance_approoval));
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        edittextsort = (EditText)findViewById(R.id.edit_text_email);
        sortLayout = (LinearLayout)findViewById(R.id.sort_layout);
        edittextsort.addTextChangedListener(watch);
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
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Utills.isConnected(this))
            getAllProcess();
        else {
            Utills.showToast(getString(R.string.error_no_internet), mContext);
            onBackPressed();
        }
    }
    public void getAllProcess() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAttendanceForApproval?userId="+ User.getCurrentUser(mContext).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, AttendanceApproval[].class)).size()>0) {
                                attendanceList = Arrays.asList(gson.fromJson(str, AttendanceApproval[].class));
                                setRecyclerView(sortString);
                            }
                        }
                    }
                }  catch(IOException e){
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
        attendanceSortedList.clear();
        for(int i=0;i<attendanceList.size();i++){
            if(attendanceList.get(i).getStatusC().equals(Status)){
                attendanceSortedList.add(attendanceList.get(i));
            }
        }
        adapter = new AttendanceAdapter(mContext, attendanceSortedList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(attendanceSortedList.size()==0) {
            Utills.showToast("No data available.",this);
            sortLayout.setVisibility(View.GONE);
        }else{
            sortLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    //added this code for sorting vouchers by username
    TextWatcher watch = new TextWatcher() {

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
        List<AttendanceApproval> list = new ArrayList<>();
//        attendanceSortedList.clear();
//        for (int i = 0; i < attendanceList.size(); i++) {
//            attendanceSortedList.add(attendanceList.get(i));
//        }
        list.clear();
        for (int i = 0; i < attendanceSortedList.size(); i++) {
            if (attendanceSortedList.get(i).getUser_Name__c()!=null && attendanceSortedList.get(i).getUser_Name__c().toLowerCase().contains(s.toLowerCase())) {
                list.add(attendanceSortedList.get(i));
            }
        }

        adapter = new AttendanceAdapter(mContext, list);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        // Utills.openActivity(mContext, HomeActivity.class);
        finish();
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

}