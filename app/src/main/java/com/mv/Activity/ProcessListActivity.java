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
import java.util.Calendar;
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
    //private ActivityProgrammeManagmentBinding binding;
    private PreferenceHelper preferenceHelper;
    ArrayList<Task> taskList = new ArrayList<>();
    private ProcessListAdapter mAdapter;
    String proceesId, Processname;
    Context mContext;


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


    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        setActionbar(Processname);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvProcess.setLayoutManager(mLayoutManager);
        binding.rvProcess.setItemAnimator(new DefaultItemAnimator());


    }


    @Override
    protected void onResume() {
        super.onResume();
        resultList.clear();
        if (Utills.isConnected(this))
            getAllProcess();
        else {

            resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(proceesId, Constants.TASK_ANSWER);

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

        if (Utills.isConnected(this))
            getAllTask();
        else {
            TaskContainerModel taskContainerModel = new TaskContainerModel();
            taskContainerModel = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getQuestion(proceesId, Constants.TASK_QUESTION);
            if (taskContainerModel != null) {
                if (preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                    preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                    Intent openClass = new Intent(mContext, LocationSelectionActity.class);
                    openClass.putExtra(Constants.PROCESS_ID, taskList);

                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                    //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                    startActivity(openClass);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } else {
                    preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                    Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                    //openClass.putExtra(Constants.PROCESS_ID, taskList);
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                    //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                    startActivity(openClass);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }

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

    private void getAllProcess() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getprocessAnswerTask?processId=" + proceesId + "&UserId=" + User.getCurrentUser(this).getId();
        // + "/services/apexrest/getprocessAnswerTask?processId=a1Q0k000000O6Ex&UserId=a100k000000KX6y";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {

                    AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().deleteTask("false", proceesId);
                    resultList = new ArrayList<>();
                    resultList = AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().getTask(proceesId, Constants.TASK_ANSWER);
                    idList=new ArrayList<>();
                    for(int k=0;k<resultList.size();k++)
                        idList.add(resultList.get(k).getUnique_Id());
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray resultArray = jsonObject.getJSONArray("tsk");
                    for (int j = 0; j < resultArray.length(); j++) {
                        JSONArray jsonArray = resultArray.getJSONArray(j);
                        taskContainerModel = new TaskContainerModel();
                        taskList = new ArrayList<Task>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Task processList = new Task();
                            processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                            processList.setName(jsonArray.getJSONObject(i).getString("Name"));
                            //  processList.setIs_Completed__c(jsonArray.getJSONObject(i).getBoolean("Is_Completed__c"));
                            processList.setIs_Response_Mnadetory__c(jsonArray.getJSONObject(i).getBoolean("Is_Mandotory__c"));
                            processList.setTask_type__c(jsonArray.getJSONObject(i).getString("Task_Type__c"));
                            processList.setTask_Text__c(jsonArray.getJSONObject(i).getString("Question__c"));
                            if (jsonArray.getJSONObject(i).has("Picklist_Value__c"))
                                processList.setPicklist_Value__c(jsonArray.getJSONObject(i).getString("Picklist_Value__c"));

                            if (jsonArray.getJSONObject(i).has("Answer__c"))
                                processList.setTask_Response__c(jsonArray.getJSONObject(i).getString("Answer__c"));
                            processList.setMV_Process__c(jsonArray.getJSONObject(i).getString("MV_Process__c"));
                            processList.setMV_Task__c_Id(jsonArray.getJSONObject(i).getString("MV_Task__c"));
                            processList.setTimestamp__c(jsonArray.getJSONObject(i).getString("Timestamp__c"));
                            processList.setUnique_Id__c(jsonArray.getJSONObject(i).getString("Unique_Id__c"));
                            processList.setMTUser__c(jsonArray.getJSONObject(i).getString("MV_User__c"));
                            processList.setValidation(jsonArray.getJSONObject(i).getString("Validation_on_text__c"));
                            processList.setIsSave("false");
                            taskList.add(processList);

                        }


                        taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                        taskContainerModel.setIsSave("false");
                        taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                        taskContainerModel.setMV_Process__c(proceesId);
                        taskContainerModel.setUnique_Id(taskList.get(0).getId());
                        if (!idList.contains(taskContainerModel.getUnique_Id()))
                            resultList.add(taskContainerModel);

                    }


                    AppDatabase.getAppDatabase(ProcessListActivity.this).userDao().insertTask(resultList);

                    mAdapter = new ProcessListAdapter(resultList, ProcessListActivity.this);
                    binding.rvProcess.setAdapter(mAdapter);
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    private void getAllTask() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getprocessTask?Id=" + proceesId;
        // + "/services/apexrest/getprocessAnswerTask?processId=a1Q0k000000O6Ex&UserId=a100k000000KX6y";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray resultArray = jsonObject.getJSONArray("tsk");
                    taskContainerModel = new TaskContainerModel();
                    taskList = new ArrayList<>();

                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject resultJsonObj = resultArray.getJSONObject(i);


                        Task processList = new Task();
                        processList.setMV_Task__c_Id(resultJsonObj.getString("Id"));
                        processList.setName(resultJsonObj.getString("Name"));
                        processList.setIs_Completed__c(resultJsonObj.getBoolean("Is_Completed__c"));
                        processList.setIs_Response_Mnadetory__c(resultJsonObj.getBoolean("Is_Response_Mnadetory__c"));
                        if (resultJsonObj.has("Picklist_Value__c"))
                            processList.setPicklist_Value__c(resultJsonObj.getString("Picklist_Value__c"));
                        processList.setMV_Process__c(resultJsonObj.getString("MV_Process__c"));
                        processList.setTask_Text__c(resultJsonObj.getString("Task_Text__c"));
                        processList.setTask_type__c(resultJsonObj.getString("Task_type__c"));
                        processList.setValidation(resultJsonObj.getString("Validaytion_on_text__c"));
                        processList.setIsSave("true");
                        // processList.setTimestamp__c(resultJsonObj.getString("Timestamp__c"));
                        // processList.setMTUser__c(resultJsonObj.getString("MTUser__c"));

                        taskList.add(processList);


                    }

                    taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                    taskContainerModel.setIsSave("true");
                    taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                    taskContainerModel.setMV_Process__c(proceesId);
                    AppDatabase.getAppDatabase(getApplicationContext()).userDao().deleteQuestion(proceesId, Constants.TASK_QUESTION);
                    AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertTask(taskContainerModel);

                    if (taskList.size() > 0) {
                        if (preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                            Intent openClass = new Intent(mContext, LocationSelectionActity.class);
                            openClass.putExtra(Constants.PROCESS_ID, taskList);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            startActivity(openClass);
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        } else {
                            Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                            openClass.putExtra(Constants.PROCESS_ID, taskList);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            startActivity(openClass);
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                    } else {
                        Utills.showToast(getString(R.string.No_Task), mContext);
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
