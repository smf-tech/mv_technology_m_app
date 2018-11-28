package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.ProcessListAdapter;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityProcessListBinding;

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

public class ProcessListActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

//    private int pageNo = 0;
    private ArrayList<String> idList;
    private ArrayList<Task> taskList = new ArrayList<>();
    private List<TaskContainerModel> resultList = new ArrayList<>();

    private PreferenceHelper preferenceHelper;
    private ProcessListAdapter mAdapter;
    private String processId, processName;
    private TaskContainerModel taskContainerModel;
    private ActivityProcessListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_list);
        binding.setProcesslist(this);

        if (getIntent().getExtras() != null) {
            processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
            processName = getIntent().getExtras().getString(Constants.PROCESS_NAME);
        }

        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        //storing process Id to preference to use later
        preferenceHelper = new PreferenceHelper(this);
        preferenceHelper.insertString(Constants.PROCESS_ID, processId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);

        setActionbar(processName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvProcess.setLayoutManager(layoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());

//        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                if (Utills.isConnected(ProcessListActivity.this)){
//                    pageNo ++;
//                    getAllProcess(pageNo);
//                } else {
//                    Toast.makeText(ProcessListActivity.this,
//                            "No Internet connection", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//
//        binding.rvProcess.addOnScrollListener(scrollListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocationSelectionActity.selectedState = User.getCurrentUser(getApplicationContext()).getMvUser().getState();
        LocationSelectionActity.selectedDistrict = User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict();
        LocationSelectionActity.selectedTaluka = User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka();
        LocationSelectionActity.selectedCluster = User.getCurrentUser(getApplicationContext()).getMvUser().getCluster();
        LocationSelectionActity.selectedVillage = User.getCurrentUser(getApplicationContext()).getMvUser().getVillage();
        LocationSelectionActity.selectedSchool = User.getCurrentUser(getApplicationContext()).getMvUser().getSchool_Name();

        resultList.clear();
        resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(processId, Constants.TASK_ANSWER);
        getAllProcessData();
    }

    public void getAllProcessData() {
        if (Utills.isConnected(this))
            getAllProcess();
        else {
            //offline show in process list only type is answer(exclude question)
            resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(processId, Constants.TASK_ANSWER);
            if (resultList.size() > 0) {
                if (preferenceHelper.getBoolean(Constants.IS_MULTIPLE)) {
                    binding.fabAddProcess.setVisibility(View.VISIBLE);
                } else {
                    binding.fabAddProcess.setVisibility(View.GONE);
                }
            }

            mAdapter = new ProcessListAdapter(resultList, ProcessListActivity.this, processName);
            binding.rvProcess.setAdapter(mAdapter);
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
        //get latest question
        preferenceHelper.insertString(Constants.UNIQUE, "");
        if (Utills.isConnected(this)) {
            getAllTask();
        } else {
            //fill new forms
            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);

            //get  process list only type is question (exclude answer it would always 1 record for on process  )
            TaskContainerModel taskContainerModel = AppDatabase.getAppDatabase(
                    ProcessListActivity.this).userDao().getQuestion(processId, Constants.TASK_QUESTION);

            if (taskContainerModel != null) {
                Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                openClass.putExtra(Constants.PROCESS_ID, taskList);
                openClass.putExtra(Constants.PROCESS_NAME, processName);
                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID,
                        Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                openClass.putExtra(Constants.PICK_LIST_ID, taskContainerModel.getProAnsListString());

                startActivity(openClass);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else {
                Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getAllProcess() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessAnswerDataUrl + "?processId=" + processId + "&UserId="
                + User.getCurrentUser(this).getMvUser().getId()
                + "&language=" + preferenceHelper.getString(Constants.LANGUAGE);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().deleteTask("false", processId);
                            resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(processId, Constants.TASK_ANSWER);
                            idList = new ArrayList<>();

                            for (int k = 0; k < resultList.size(); k++) {
                                idList.add(resultList.get(k).getUnique_Id());
                            }

                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray resultArray = jsonObject.getJSONArray("tsk");
                            JSONArray pickListArray = jsonObject.getJSONArray("proAnsList");

                            for (int j = 0; j < resultArray.length(); j++) {
                                JSONArray jsonArray = resultArray.getJSONArray(j);
                                taskContainerModel = new TaskContainerModel();
                                taskList = new ArrayList<>();

                                int flag = 0;
                                StringBuilder sb = new StringBuilder();
                                String prefix = "";

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    if ((!jsonArray.getJSONObject(i).has("Id"))) {
                                        break;
                                    }

                                    Task processList = new Task();
                                    processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                                    processList.setIs_Response_Mnadetory__c(jsonArray.getJSONObject(i).getBoolean("Is_Mandotory"));
                                    processList.setTask_type__c(jsonArray.getJSONObject(i).getString("Task_Type"));
                                    processList.setTask_Text__c(jsonArray.getJSONObject(i).getString("Question"));

                                    //added next line to get isDeleteallow field from salesforce
                                    processList.setDeleteAllow(jsonArray.getJSONObject(i).getBoolean("IsDeleteAllow"));
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

                                    if (jsonArray.getJSONObject(i).has("referenceField")) {
                                        processList.setReferenceField(jsonArray.getJSONObject(i).getString("referenceField"));
                                    }
                                    if (jsonArray.getJSONObject(i).has("filterFields")) {
                                        processList.setFilterFields(jsonArray.getJSONObject(i).getString("filterFields"));
                                    }
                                    if (jsonArray.getJSONObject(i).has("aPIFieldName")) {
                                        processList.setaPIFieldName(jsonArray.getJSONObject(i).getString("aPIFieldName"));
                                    }

                                    if (jsonArray.getJSONObject(i).has("Task_Type") &&
                                            jsonArray.getJSONObject(i).getString("Task_Type").equals(Constants.TASK_PICK_LIST)) {

                                        if (jsonArray.getJSONObject(i).has("referenceField") &&
                                                jsonArray.getJSONObject(i).getString("referenceField") != null) {
                                            flag = 1;
                                        }
                                    }

                                    processList.setValidation(jsonArray.getJSONObject(i).getString("Validation_on_text"));
                                    processList.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                                    taskList.add(processList);
                                }

                                if (flag == 1) {
                                    taskContainerModel.setProAnsListString(pickListArray.toString());
                                }

                                taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                                taskContainerModel.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                                taskContainerModel.setHeaderPosition(sb.toString());
                                taskContainerModel.setTaskTimeStamp(taskList.get(0).getTimestamp__c());

                                //task is with answer
                                taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                                taskContainerModel.setMV_Process__c(processId);

                                if (taskList.size() > 0) {
                                    taskContainerModel.setUnique_Id(taskList.get(0).getId());
                                    //add delete allow feature to table
                                    taskContainerModel.setIsDeleteAllow(taskList.get(0).getDeleteAllow());
                                }

                                if (!idList.contains(taskContainerModel.getUnique_Id())) {
                                    resultList.add(taskContainerModel);
                                }
                            }

                            AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().insertTask(resultList);
                            if (resultList.size() > 0) {
                                if (preferenceHelper.getBoolean(Constants.IS_MULTIPLE)) {
                                    binding.fabAddProcess.setVisibility(View.VISIBLE);
                                } else {
                                    binding.fabAddProcess.setVisibility(View.GONE);
                                }
                            }

//                            if (mAdapter != null) {
//                                mAdapter.notifyDataSetChanged();
//                            } else {
                                mAdapter = new ProcessListAdapter(resultList, ProcessListActivity.this, processName);
                                binding.rvProcess.setAdapter(mAdapter);
//                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getAllTask() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessTaskUrl + "?Id=" + processId
                + "&language=" + preferenceHelper.getString(Constants.LANGUAGE)
                + "&userId=" + User.getCurrentUser(this).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray resultArray = jsonObject.getJSONArray("tsk");

                            //list of task
                            taskContainerModel = new TaskContainerModel();
                            taskList = new ArrayList<>();
                            User user = User.getCurrentUser(getApplicationContext());
                            StringBuilder sb = new StringBuilder();
                            String prefix = "";

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultJsonObj = resultArray.getJSONObject(i);

                                //task is each task detail
                                Task processList = new Task();
                                processList.setMV_Task__c_Id(resultJsonObj.getString("id"));
                                processList.setName(resultJsonObj.getString("name"));
                                processList.setIs_Completed__c(resultJsonObj.getBoolean("isCompleted"));

                                processList.setIsHeader(resultJsonObj.getString("isHeader"));
                                processList.setIs_Response_Mnadetory__c(resultJsonObj.getBoolean("isResponseMnadetory"));

                                if (!resultJsonObj.getString("lanTsaskText").equals("null")) {
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("lanTsaskText"));
                                } else {
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("taskText"));
                                }

                                if (resultJsonObj.has("status")) {
                                    processList.setStatus__c(resultJsonObj.getString("status"));
                                }
                                if (resultJsonObj.has("ValidationRule")) {
                                    processList.setValidationRule(resultJsonObj.getString("ValidationRule"));
                                }
                                if (resultJsonObj.has("MinRange")) {
                                    processList.setMinRange(resultJsonObj.getString("MinRange"));
                                }
                                if (resultJsonObj.has("MaxRange")) {
                                    processList.setMaxRange(resultJsonObj.getString("MaxRange"));
                                }
                                if (resultJsonObj.has("LimitValue")) {
                                    processList.setLimitValue(resultJsonObj.getString("LimitValue"));
                                }

                                if (resultJsonObj.has("isEditable")) {
                                    processList.setIsEditable__c(resultJsonObj.getString("isEditable"));
                                }

                                processList.setPicklist_Value_Lan__c(resultJsonObj.getString("lanPicklistValue"));
                                if (resultJsonObj.has("Process_Answer_Status__c")) {
                                    processList.setProcess_Answer_Status__c(resultJsonObj.getString("Process_Answer_Status__c"));
                                }

                                if (resultJsonObj.has("picklistValue")) {
                                    processList.setPicklist_Value__c(resultJsonObj.getString("picklistValue"));
                                }

                                if (!resultJsonObj.getString("locationLevel").equals("null")) {
                                    processList.setLocationLevel(resultJsonObj.getString("locationLevel"));

                                    switch (resultJsonObj.getString("locationLevel")) {
                                        case "State":
                                            processList.setTask_Response__c(user.getMvUser().getState());
                                            break;

                                        case "District":
                                            processList.setTask_Response__c(user.getMvUser().getDistrict());
                                            break;

                                        case "Taluka":
                                            processList.setTask_Response__c(user.getMvUser().getTaluka());
                                            break;

                                        case "Cluster":
                                            processList.setTask_Response__c(user.getMvUser().getCluster());
                                            break;

                                        case "Village":
                                            processList.setTask_Response__c(user.getMvUser().getVillage());
                                            break;

                                        case "School":
                                            processList.setTask_Response__c(user.getMvUser().getSchool_Name());
                                            break;
                                    }

                                    if (resultJsonObj.getString("isHeader").equals("true")) {
                                        if (!processList.getTask_Response__c().equals("Select")) {
                                            sb.append(prefix);
                                            prefix = " , ";
                                            sb.append(processList.getTask_Response__c());
                                        }
                                    }
                                }

                                if (resultJsonObj.has("referenceField")) {
                                    processList.setReferenceField(resultJsonObj.getString("referenceField"));
                                }
                                if (resultJsonObj.has("filterFields")) {
                                    processList.setFilterFields(resultJsonObj.getString("filterFields"));
                                }
                                if (resultJsonObj.has("aPIFieldName")) {
                                    processList.setaPIFieldName(resultJsonObj.getString("aPIFieldName"));
                                }

                                processList.setMV_Process__c(resultJsonObj.getString("mVProcess"));
                                processList.setTask_Text__c(resultJsonObj.getString("taskText"));
                                processList.setTask_type__c(resultJsonObj.getString("tasktype"));
                                processList.setValidation(resultJsonObj.getString("validaytionOnText"));
                                processList.setIsSave(Constants.PROCESS_STATE_SAVE);
                                taskList.add(processList);
                            }

                            // each task list  convert to String and stored in process task filled
                            taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                            taskContainerModel.setHeaderPosition(sb.toString());
                            taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);

                            //task without answer
                            taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                            taskContainerModel.setMV_Process__c(processId);

                            //delete old question
                            AppDatabase.getAppDatabase(getApplicationContext())
                                    .userDao().deleteQuestion(processId, Constants.TASK_QUESTION);

                            //add new question
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertTask(taskContainerModel);

                            JSONArray pickListArray = jsonObject.getJSONArray("proAnsList");
                            if (taskList.size() > 0) {
                                preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                                Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                                openClass.putExtra(Constants.PICK_LIST_ID, pickListArray.toString());
                                openClass.putExtra(Constants.PROCESS_NAME, processName);
                                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
                                startActivity(openClass);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            } else {
                                Utills.showToast(getString(R.string.No_Task), mContext);
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

    //Delete post from salesforece and from local database
    public void deleteForm(TaskContainerModel tcm, int position) {
        if (Utills.isConnected(this)) {
            Utills.showProgressDialog(this);

            ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/deleteProcessAnswer?processAnswerId="
                    + tcm.getUnique_Id()).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    AppDatabase.getAppDatabase(mContext).userDao().deleteSingleTask(tcm.getUnique_Id(), tcm.getMV_Process__c());

                    // Removed entry from db
                    resultList.remove(position);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            });
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }
    }
}