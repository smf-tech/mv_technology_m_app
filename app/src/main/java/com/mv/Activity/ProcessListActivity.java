package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonParser;
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

    private ActivityProcessListBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    ArrayList<String> idList;
    private PreferenceHelper preferenceHelper;
    ArrayList<Task> taskList = new ArrayList<>();
    private ProcessListAdapter mAdapter;
    String proceesId, Processname;
    Context mContext;
    TextView textNoData;
    TaskContainerModel taskContainerModel;
    List<TaskContainerModel> resultList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_list);
        binding.setProcesslist(this);
        proceesId = getIntent().getExtras().getString(Constants.PROCESS_ID);
        Processname = getIntent().getExtras().getString(Constants.PROCESS_NAME);
        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {

        preferenceHelper = new PreferenceHelper(this);
        //storing process Id to preference to use later
        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        textNoData = (TextView) findViewById(R.id.textNoData);
        setActionbar(Processname);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvProcess.setLayoutManager(mLayoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());

    }


    @Override
    protected void onResume() {
        super.onResume();
        resultList.clear();
        LocationSelectionActity.selectedState = User.getCurrentUser(getApplicationContext()).getMvUser().getState();
        LocationSelectionActity.selectedDisrict = User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict();
        LocationSelectionActity.selectedTaluka = User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka();
        LocationSelectionActity.selectedCluster = User.getCurrentUser(getApplicationContext()).getMvUser().getCluster();
        LocationSelectionActity.selectedVillage = User.getCurrentUser(getApplicationContext()).getMvUser().getVillage();
        LocationSelectionActity.selectedSchool = User.getCurrentUser(getApplicationContext()).getMvUser().getSchool_Name();
        getAllProcessData();

    }

    public void getAllProcessData() {
        if (Utills.isConnected(this))
            getAllProcess();
        else {
            //offline
            //show in process list only type is answer(exclude question)
            resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(proceesId, Constants.TASK_ANSWER);

            if (resultList.size() > 0) {
                if (preferenceHelper.getBoolean(Constants.IS_MULTIPLE)) {
                    binding.fabAddProcess.setVisibility(View.VISIBLE);
                } else {
                    binding.fabAddProcess.setVisibility(View.GONE);
                }
            }
            mAdapter = new ProcessListAdapter(resultList, ProcessListActivity.this);
            binding.rvProcess.setAdapter(mAdapter);
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
        //plus button click

        //get latest question
        preferenceHelper.insertString(Constants.UNIQUE, "");
        if (Utills.isConnected(this))
            getAllTask();
        else {
            //fill new forms
            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
            TaskContainerModel taskContainerModel = new TaskContainerModel();
            //get  process list only type is question (exclude answer it would always 1 record for on process  )
            taskContainerModel = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getQuestion(proceesId, Constants.TASK_QUESTION);
            if (taskContainerModel != null) {

                Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                openClass.putExtra(Constants.PROCESS_ID, taskList);
                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                startActivity(openClass);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

            } else {
                Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
            }

        }

    }

    @Override
    public void onBackPressed() {
        // Utills.openActivity(ProcessListActivity.this, HomeActivity.class);
        finish();
    }

    public void getAllProcess() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessAnswerDataUrl + "?processId=" + proceesId + "&UserId=" + User.getCurrentUser(this).getMvUser().getId() + "&language=" + preferenceHelper.getString(Constants.LANGUAGE);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().deleteTask("false", proceesId);
                            resultList = new ArrayList<>();
                            resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(proceesId, Constants.TASK_ANSWER);
                            idList = new ArrayList<>();
                            for (int k = 0; k < resultList.size(); k++) {
                                idList.add(resultList.get(k).getUnique_Id());
                            }
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray resultArray = jsonObject.getJSONArray("tsk");
                            for (int j = 0; j < resultArray.length(); j++) {
                                JSONArray jsonArray = resultArray.getJSONArray(j);
                                taskContainerModel = new TaskContainerModel();
                                taskList = new ArrayList<Task>();
                                StringBuffer sb = new StringBuffer();
                                String prefix = "";

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Task processList = new Task();
                                    if((!jsonArray.getJSONObject(i).has("Id")))
                                        break;
                                    processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                                    // processList.setName(jsonArray.getJSONObject(i).getString("Name"));
                                    //  processList.setIs_Completed__c(jsonArray.getJSONObject(i).getBoolean("Is_Completed__c"));
                                    processList.setIs_Response_Mnadetory__c(jsonArray.getJSONObject(i).getBoolean("Is_Mandotory"));
                                    processList.setTask_type__c(jsonArray.getJSONObject(i).getString("Task_Type"));
                                    processList.setTask_Text__c(jsonArray.getJSONObject(i).getString("Question"));
                                    //added next line to get isDeleteallow field from salesforce
                                    processList.setDeleteAllow(jsonArray.getJSONObject(i).getBoolean("IsDeleteAllow"));

                                    processList.setIsHeader(jsonArray.getJSONObject(i).getString("isHeader"));

                                    if (!jsonArray.getJSONObject(i).getString("lanTsaskText").equals("null"))
                                        processList.setTask_Text___Lan_c(jsonArray.getJSONObject(i).getString("lanTsaskText"));
                                    else
                                        processList.setTask_Text___Lan_c(jsonArray.getJSONObject(i).getString("Question"));
                                    processList.setPicklist_Value_Lan__c(jsonArray.getJSONObject(i).getString("lanPicklistValue"));
                                    if (jsonArray.getJSONObject(i).has("Process_Answer_Status__c"))
                                        processList.setProcess_Answer_Status__c(jsonArray.getJSONObject(i).getString("Process_Answer_Status__c"));

                                    if (jsonArray.getJSONObject(i).has("Picklist_Value"))
                                        processList.setPicklist_Value__c(jsonArray.getJSONObject(i).getString("Picklist_Value"));

                                    if (jsonArray.getJSONObject(i).has("Answer"))
                                        processList.setTask_Response__c(jsonArray.getJSONObject(i).getString("Answer"));
                                    if (jsonArray.getJSONObject(i).getString("isHeader").equals("true")) {
                                        if (!processList.getTask_Response__c().equals("Select")) {
                                            sb.append(prefix);
                                            prefix = " , ";
                                            sb.append(processList.getTask_Response__c());
                                        }
                                    }
                                    processList.setMV_Process__c(jsonArray.getJSONObject(i).getString("MV_Process"));
                                    if (jsonArray.getJSONObject(i).has("Location_Level"))
                                        processList.setLocationLevel(jsonArray.getJSONObject(i).getString("Location_Level"));
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
                                taskContainerModel.setMV_Process__c(proceesId);
                                taskContainerModel.setUnique_Id(taskList.get(0).getId());
                                //add delete allow feature to table
                                taskContainerModel.setIsDeleteAllow(taskList.get(0).getDeleteAllow());
                                if (!idList.contains(taskContainerModel.getUnique_Id()))
                                    resultList.add(taskContainerModel);
                              }

                            AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().insertTask(resultList);
                            if (resultList.size() > 0) {
                                if (preferenceHelper.getBoolean(Constants.IS_MULTIPLE)) {
                                    binding.fabAddProcess.setVisibility(View.VISIBLE);
                                } else {
                                    binding.fabAddProcess.setVisibility(View.GONE);
                                }
                            }
                            mAdapter = new ProcessListAdapter(resultList, ProcessListActivity.this);
                            binding.rvProcess.setAdapter(mAdapter);
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


    private void getAllTask() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessTaskUrl + "?Id=" + proceesId + "&language=" + preferenceHelper.getString(Constants.LANGUAGE);

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
                            StringBuffer sb = new StringBuffer();
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
                                if (!resultJsonObj.getString("lanTsaskText").equals("null"))
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("lanTsaskText"));
                                else
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("taskText"));
                                if (resultJsonObj.has("status")) {
                                    processList.setStatus__c(resultJsonObj.getString("status"));
                                }
                                if (resultJsonObj.has("isEditable")) {
                                    processList.setIsEditable__c(resultJsonObj.getString("isEditable"));
                                }
                                processList.setPicklist_Value_Lan__c(resultJsonObj.getString("lanPicklistValue"));
                                if (resultJsonObj.has("Process_Answer_Status__c"))
                                    processList.setProcess_Answer_Status__c(resultJsonObj.getString("Process_Answer_Status__c"));

                                if (resultJsonObj.has("picklistValue"))
                                    processList.setPicklist_Value__c(resultJsonObj.getString("picklistValue"));


                                if (!resultJsonObj.getString("locationLevel").equals("null")) {
                                    processList.setLocationLevel(resultJsonObj.getString("locationLevel"));

                                    if (resultJsonObj.getString("locationLevel").equals("State")) {
                                        processList.setTask_Response__c(user.getMvUser().getState());
                                        //  LocationSelectionActity.selectedState = user.getState();

                                    } else if (resultJsonObj.getString("locationLevel").equals("District")) {
                                        // LocationSelectionActity.selectedDisrict = user.getDistrict();

                                        processList.setTask_Response__c(user.getMvUser().getDistrict());
                                    } else if (resultJsonObj.getString("locationLevel").equals("Taluka")) {
                                        processList.setTask_Response__c(user.getMvUser().getTaluka());
                                        //  LocationSelectionActity.selectedTaluka = user.getTaluka();
                                    } else if (resultJsonObj.getString("locationLevel").equals("Cluster")) {
                                        ///  LocationSelectionActity.selectedCluster = user.getCluster();
                                        processList.setTask_Response__c(user.getMvUser().getCluster());
                                    } else if (resultJsonObj.getString("locationLevel").equals("Village")) {
                                        // LocationSelectionActity.selectedVillage = user.getVillage();

                                        processList.setTask_Response__c(user.getMvUser().getVillage());
                                    } else if (resultJsonObj.getString("locationLevel").equals("School")) {
                                        processList.setTask_Response__c(user.getMvUser().getSchool_Name());
                                        //  LocationSelectionActity.selectedSchool = user.getSchool_Name();
                                    }

                                    if (resultJsonObj.getString("isHeader").equals("true")) {
                                        if (!processList.getTask_Response__c().equals("Select")) {
                                            sb.append(prefix);
                                            prefix = " , ";

                                            sb.append(processList.getTask_Response__c());
                                        }
                                    }
                                }
                                processList.setMV_Process__c(resultJsonObj.getString("mVProcess"));
                                processList.setTask_Text__c(resultJsonObj.getString("taskText"));
                                processList.setTask_type__c(resultJsonObj.getString("tasktype"));
                                processList.setValidation(resultJsonObj.getString("validaytionOnText"));
                                processList.setIsSave(Constants.PROCESS_STATE_SAVE);

                                // processList.setTimestamp__c(resultJsonObj.getString("Timestamp__c"));
                                // processList.setMTUser__c(resultJsonObj.getString("MTUser__c"));

                                taskList.add(processList);


                            }
                            // each task list  convert to String and stored in process task filled
                            taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                            taskContainerModel.setHeaderPosition(sb.toString());
                            taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);
                            //task without answer
                            taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                            taskContainerModel.setMV_Process__c(proceesId);
                            //delete old question
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().deleteQuestion(proceesId, Constants.TASK_QUESTION);
                            //add new question
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertTask(taskContainerModel);

                            if (taskList.size() > 0) {

                                preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                                Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                                openClass.putExtra(Constants.PROCESS_ID, taskList);
                                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
                                //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
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
    public void deleteForm(TaskContainerModel tcm) {
        if (Utills.isConnected(this)) {

            Utills.showProgressDialog(this);
//                JSONObject jsonObject = new JSONObject();
//                JSONArray jsonArray = new JSONArray();
//                JSONObject jsonObject1 = new JSONObject();
//                    jsonObject1.put("commentId", id);

            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
////                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
//            https://cs57.salesforce.com/services/apexrest/deleteProcessAnswer?processAnswerId=a1V0k0000007uEl
            apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/deleteProcessAnswer?processAnswerId="+tcm.getUnique_Id()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();

                    AppDatabase.getAppDatabase(mContext).userDao().deleteSingleTask(tcm.getUnique_Id(), tcm.getMV_Process__c());
                    getAllProcessData();

                    try {
                        if (Utills.isConnected(ProcessListActivity.this)) {
                            getAllProcess();
                        }

                    //    Utills.showToast(getString(R.string.comment_delete), getApplicationContext());
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
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }

}
