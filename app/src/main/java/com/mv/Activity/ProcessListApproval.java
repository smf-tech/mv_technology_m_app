package com.mv.Activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.ProcessListAdapter;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityProcessListApprovalBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessListApproval extends AppCompatActivity implements View.OnClickListener {

    private ActivityProcessListApprovalBinding binding;
    private PreferenceHelper preferenceHelper;

    private ArrayList<Task> taskList = new ArrayList<>();
    private ProcessListAdapter mPendingAdapter;
    private ProcessListAdapter mApprovedAdapter;
    private ProcessListAdapter mRejectedAdapter;

 //   private int pageNo = 0;
    private String processId;
    private String processName;
    private String userId;
    private String sortString = "Pending";

    private Button btnPending;
    private Button btnApprove;
    private Button btnReject;

    private TaskContainerModel taskContainerModel;
    private List<TaskContainerModel> pendingProcessList = new ArrayList<>();
    private List<TaskContainerModel> approvedProcessList = new ArrayList<>();
    private List<TaskContainerModel> rejectedProcessList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_list_approval);
        binding.setProcesslistApprove(this);

        if (getIntent().getExtras() != null) {
            processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
            processName = getIntent().getExtras().getString(Constants.PROCESS_NAME);
            userId = getIntent().getExtras().getString(Constants.ID);
        }

        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        //storing process Id to preference to use later
        preferenceHelper.insertString(Constants.PROCESS_ID, processId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.APPROVAL_PROCESS);
        setActionbar(processName);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvProcess.setLayoutManager(mLayoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());

        btnPending = (Button) findViewById(R.id.btn_pending);
        btnApprove = (Button) findViewById(R.id.btn_approve);
        btnReject = (Button) findViewById(R.id.btn_reject);

        btnPending.setOnClickListener(this);
        btnApprove.setOnClickListener(this);
        btnReject.setOnClickListener(this);

//        //by default pending status list will be loaded.
//        btnPending.setBackgroundResource(R.drawable.selected_btn_background);
//        btnApprove.setBackgroundResource(R.drawable.light_grey_btn_background);
//        btnReject.setBackgroundResource(R.drawable.light_grey_btn_background);

        mPendingAdapter = new ProcessListAdapter(pendingProcessList, ProcessListApproval.this, processName);
        mApprovedAdapter = new ProcessListAdapter(approvedProcessList, ProcessListApproval.this, processName);
        mRejectedAdapter = new ProcessListAdapter(rejectedProcessList, ProcessListApproval.this, processName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvProcess.setLayoutManager(layoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());

//        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                pageNo++;
//                getProcessByStatus(sortString, pageNo);
//            }
//        };
//
//        binding.rvProcess.addOnScrollListener(scrollListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionButtons();

        if (Utills.isActionDone()) {
            approvedProcessList.clear();
            rejectedProcessList.clear();
            pendingProcessList.clear();
            Utills.setIsActionDone(false);
        }

        if (approvedProcessList.isEmpty() && rejectedProcessList.isEmpty() && pendingProcessList.isEmpty()) {
            getAllProcessData();
        } else {
            switch (sortString) {
                case "Pending":
                    pendingProcessList.clear();
                    getProcessByStatus("Pending", 0);
                    break;

                case "Approved":
                    approvedProcessList.clear();
                    getProcessByStatus("Approved", 0);
                    break;

                case "Rejected":
                    rejectedProcessList.clear();
                    getProcessByStatus("Rejected", 0);
                    break;
            }
        }
    }

    private void getAllProcessData() {
        if (Utills.isConnected(this)) {
            getProcessByStatus("Pending", 0);
            getProcessByStatus("Approved", 0);
            getProcessByStatus("Rejected", 0);
        } else {
            //offline
            Utills.showToast(getString(R.string.error_no_internet), ProcessListApproval.this);
        }
    }

    private void setActionbar(String Title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    private void setActionButtons() {
        switch (sortString) {
            case "Pending":
                btnPending.setBackgroundResource(R.drawable.selected_btn_background);
                btnApprove.setBackgroundResource(R.drawable.light_grey_btn_background);
                btnReject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;

            case "Approved":
                btnPending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btnApprove.setBackgroundResource(R.drawable.selected_btn_background);
                btnReject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;

            case "Rejected":
                btnPending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btnApprove.setBackgroundResource(R.drawable.light_grey_btn_background);
                btnReject.setBackgroundResource(R.drawable.selected_btn_background);
                setRecyclerView(sortString);
                break;
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
                break;

            case R.id.btn_approve:
                sortString = "Approved";
                break;

            case R.id.btn_reject:
                sortString = "Rejected";
                break;
        }

        setActionButtons();
    }

    private void setRecyclerView(String status) {
        switch (status) {
            case "Pending":
                binding.rvProcess.setAdapter(mPendingAdapter);
                break;

            case "Approved":
                binding.rvProcess.setAdapter(mApprovedAdapter);
                break;

            case "Rejected":
                binding.rvProcess.setAdapter(mRejectedAdapter);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getProcessByStatus(String status, int pageNo) {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessApprovalUrl + "?processId=" + processId + "&UserId=" + userId
                + "&language=" + preferenceHelper.getString(Constants.LANGUAGE)
                + "&pageNo=" + pageNo + "&processAnswerStatus=" + status;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray resultArray = jsonObject.getJSONArray("tsk");

                    for (int j = 0; j < resultArray.length(); j++) {
                        JSONArray jsonArray = resultArray.getJSONArray(j);
                        taskContainerModel = new TaskContainerModel();
                        taskList = new ArrayList<>();
                        StringBuilder sb = new StringBuilder();
                        String prefix = "";

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Task processList = new Task();
                            processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                            processList.setIs_Response_Mnadetory__c(jsonArray.getJSONObject(i).getBoolean("Is_Mandotory"));
                            processList.setTask_type__c(jsonArray.getJSONObject(i).getString("Task_Type"));
                            processList.setTask_Text__c(jsonArray.getJSONObject(i).getString("Question"));
                            processList.setIsHeader(jsonArray.getJSONObject(i).getString("isHeader"));

                            if (!jsonArray.getJSONObject(i).getString("lanTsaskText").equals("null")) {
                                processList.setTask_Text___Lan_c(jsonArray.getJSONObject(i).getString("lanTsaskText"));
                            } else {
                                processList.setTask_Text___Lan_c(jsonArray.getJSONObject(i).getString("Question"));
                            }

                            processList.setPicklist_Value_Lan__c(jsonArray.getJSONObject(i).getString("lanPicklistValue"));
                            if (jsonArray.getJSONObject(i).has("Process_Answer_Status__c")) {
                                processList.setProcess_Answer_Status__c(jsonArray.getJSONObject(i).getString("Process_Answer_Status__c"));
                            }
                            if (jsonArray.getJSONObject(i).has("Picklist_Value")) {
                                processList.setPicklist_Value__c(jsonArray.getJSONObject(i).getString("Picklist_Value"));
                            }
                            if (jsonArray.getJSONObject(i).has("Answer")) {
                                processList.setTask_Response__c(jsonArray.getJSONObject(i).getString("Answer"));
                            }
                            if (jsonArray.getJSONObject(i).getString("isHeader").equals("true")) {
                                if (!processList.getTask_Response__c().equals("Select")) {
                                    sb.append(prefix);
                                    prefix = " , ";
                                    sb.append(processList.getTask_Response__c());
                                }
                            }

                            processList.setMV_Process__c(jsonArray.getJSONObject(i).getString("MV_Process"));
                            if (jsonArray.getJSONObject(i).has("Location_Level")) {
                                processList.setLocationLevel(jsonArray.getJSONObject(i).getString("Location_Level"));
                            }

                            processList.setMV_Task__c_Id(jsonArray.getJSONObject(i).getString("MV_Task"));
                            processList.setTimestamp__c(jsonArray.getJSONObject(i).getString("Timestamp"));
                            processList.setUnique_Id__c(jsonArray.getJSONObject(i).getString("Unique_Idd"));
                            processList.setMTUser__c(jsonArray.getJSONObject(i).getString("MV_User"));
                            processList.setIsApproved__c(jsonArray.getJSONObject(i).getString("IsApproved"));

                            if (jsonArray.getJSONObject(i).has("status")) {
                                processList.setStatus__c(jsonArray.getJSONObject(i).getString("status"));
                            }
                            if (jsonArray.getJSONObject(i).has("IsEditable")) {
                                processList.setIsEditable__c(jsonArray.getJSONObject(i).getString("IsEditable"));
                            }

                            processList.setValidation(jsonArray.getJSONObject(i).getString("Validation_on_text"));
                            processList.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                            taskList.add(processList);
                        }

                        taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                        taskContainerModel.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                        taskContainerModel.setHeaderPosition(sb.toString());

                        //task is with answer
                        taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                        taskContainerModel.setMV_Process__c(processId);
                        taskContainerModel.setUnique_Id(taskList.get(0).getId());

                        switch (status) {
                            case "Pending":
                                pendingProcessList.add(taskContainerModel);
                                break;

                            case "Approved":
                                approvedProcessList.add(taskContainerModel);
                                break;

                            case "Rejected":
                                rejectedProcessList.add(taskContainerModel);
                                break;
                        }
                    }
                    preferenceHelper.insertBoolean(Constants.IS_EDITABLE, false);

                    switch (status) {
                        case "Pending":
                            mPendingAdapter.clearTaskList();
                            mPendingAdapter.notifyDataSetChanged();
                            break;

                        case "Approved":
                            mApprovedAdapter.clearTaskList();
                            mApprovedAdapter.notifyDataSetChanged();
                            break;

                        case "Rejected":
                            mRejectedAdapter.clearTaskList();
                            mRejectedAdapter.notifyDataSetChanged();
                            break;
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
