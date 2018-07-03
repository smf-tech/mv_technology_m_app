package com.mv.ActivityMenu;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.ExpandableProcessListAdapter;
import com.mv.Adapter.ProgramMangementAdapter;
import com.mv.BR;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityProgramManagementBinding;

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

public class
ProgrammeManagmentFragment extends AppCompatActivity implements View.OnClickListener {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();
    private ProgramMangementAdapter mAdapter;
    private ActivityProgramManagementBinding binding;
    RecyclerView.LayoutManager mLayoutManager;
    TextView textNoData;
    ArrayList<Task> taskList = new ArrayList<>();
    TaskContainerModel taskContainerModel;
    ExpandableProcessListAdapter adapter;
    ArrayList<String> processCategory = new ArrayList<>();
    HashMap<String, List<Template>> childList = new HashMap<>();
    Activity context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_management);
        binding.setVariable(BR.vm, new ParentViewModel());


        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        setActionbar(getString(R.string.programme_management));
        preferenceHelper = new PreferenceHelper(context);
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (Utills.isConnected(context))
                            getAllProcess();
                    }
                }
        );

        textNoData = (TextView) findViewById(R.id.textNoData);
        mAdapter = new ProgramMangementAdapter(processAllList, context);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(context))
            getAllProcess();
        else {
            processAllList.clear();
            processAllList = AppDatabase.getAppDatabase(context).userDao().getProcess();


            processCategory.clear();

            for (Template template : processAllList) {
                if (!processCategory.contains(template.getCategory__c()))
                    processCategory.add(template.getCategory__c());
            }
            for (int i = 0; i < processCategory.size(); i++) {
                childList.put(processCategory.get(i), AppDatabase.getAppDatabase(context).userDao().getProcessCatagry(processCategory.get(i)));
            }
            mAdapter.notifyDataSetChanged();
            textNoData.setVisibility(View.GONE);
            adapter = new ExpandableProcessListAdapter(context, processCategory, childList);
            binding.rvProcess.setAdapter(adapter);


         /*   mAdapter = new ProgramMangementAdapter(processAllList, context);
            mLayoutManager = new LinearLayoutManager(context);
            binding.recyclerView.setAdapter(mAdapter);*/
        }
    }


    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);
        layoutList.setVisibility(View.GONE);
        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getallprocessandtaskNew" + "?userId=" + User.getCurrentUser(this).getMvUser().getId() + "&language=" + preferenceHelper.getString(Constants.LANGUAGE);
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length() != 0) {
                            processAllList.clear();

                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONObject mainObj = jsonArray.getJSONObject(j);
                                Template processList = new Template();
                                processList.setAnswerCount(mainObj.getString("answerCount"));
                                processList.setSubmittedCount(mainObj.getString("submittedCount"));
                                processList.setExpectedCount(mainObj.getString("expectedCount"));
                                processList.setType(mainObj.getJSONObject("prc").getJSONObject("attributes").getString("type"));
                                processList.setUrl(mainObj.getJSONObject("prc").getJSONObject("attributes").getString("url"));
                                processList.setId(mainObj.getJSONObject("prc").getString("Id"));
                                processList.setName(mainObj.getJSONObject("prc").getString("Name"));
                                if (mainObj.getJSONObject("prc").has("Targated_Date__c"))
                                    processList.setTargated_Date__c(mainObj.getJSONObject("prc").getString("Targated_Date__c"));

                                processList.setIs_Editable__c(mainObj.getJSONObject("prc").getBoolean("Is_Editable__c"));
                                processList.setIs_Multiple_Entry_Allowed__c(mainObj.getJSONObject("prc").getBoolean("Is_Multiple_Entry_Allowed__c"));
                                processList.setLocation(mainObj.getJSONObject("prc").getBoolean("Location_Required__c"));
                                if (!processCategory.contains(mainObj.getJSONObject("prc").getString("Category__c"))) {
                                    processCategory.add(mainObj.getJSONObject("prc").getString("Category__c"));
                                }
                                processList.setCategory__c(mainObj.getJSONObject("prc").getString("Category__c"));

                                if (mainObj.getJSONObject("prc").has("Location_Level__c"))
                                    processList.setLocationLevel(mainObj.getJSONObject("prc").getString("Location_Level__c"));

                                processAllList.add(processList);
                                JSONArray resultArray = mainObj.getJSONArray("tsk");
                                //list of task
                                taskContainerModel = new TaskContainerModel();
                                taskList = new ArrayList<>();
                                User user = User.getCurrentUser(getApplicationContext());
                                StringBuffer sb = new StringBuffer();
                                String prefix = "";
                                for (int i = 0; i < resultArray.length(); i++) {
                                    JSONObject resultJsonObj = resultArray.getJSONObject(i);

                                    //task is each task detail
                                    Task taskList = new Task();
                                    taskList.setMV_Task__c_Id(resultJsonObj.getString("id"));
                                    taskList.setName(resultJsonObj.getString("name"));
                                    taskList.setIs_Completed__c(resultJsonObj.getBoolean("isCompleted"));
                                    taskList.setIs_Response_Mnadetory__c(resultJsonObj.getBoolean("isResponseMnadetory"));
                                    if (!resultJsonObj.getString("lanTsaskText").equals("null"))
                                        taskList.setTask_Text___Lan_c(resultJsonObj.getString("lanTsaskText"));
                                    else
                                        taskList.setTask_Text___Lan_c(resultJsonObj.getString("taskText"));
                                    taskList.setPicklist_Value_Lan__c(resultJsonObj.getString("lanPicklistValue"));
                                    if (resultJsonObj.has("Process_Answer_Status__c"))
                                        taskList.setProcess_Answer_Status__c(resultJsonObj.getString("Process_Answer_Status__c"));

                                    if (resultJsonObj.has("picklistValue"))
                                        taskList.setPicklist_Value__c(resultJsonObj.getString("picklistValue"));
                                    if (resultJsonObj.has("status")) {
                                        taskList.setStatus__c(resultJsonObj.getString("status"));
                                    }

                                    if (resultJsonObj.getString("isHeader").equals("true")) {
                                        sb.append(prefix);
                                        prefix = ",";
                                        sb.append(taskList.getTask_Response__c());
                                    }
                                    taskList.setIsHeader(resultJsonObj.getString("isHeader"));
                                    if (resultJsonObj.has("isEditable")) {
                                        taskList.setIsEditable__c(resultJsonObj.getString("isEditable"));
                                    }
                                    if (resultJsonObj.has("locationLevel")) {
                                        taskList.setLocationLevel(resultJsonObj.getString("locationLevel"));

                                        if (resultJsonObj.getString("locationLevel").equals("State")) {
                                            taskList.setTask_Response__c(user.getMvUser().getState());
                                            //  LocationSelectionActity.selectedState = user.getMvUser().getState();

                                        } else if (resultJsonObj.getString("locationLevel").equals("District")) {
                                            // LocationSelectionActity.selectedDisrict = user.getMvUser().getDistrict();

                                            taskList.setTask_Response__c(user.getMvUser().getDistrict());
                                        } else if (resultJsonObj.getString("locationLevel").equals("Taluka")) {
                                            taskList.setTask_Response__c(user.getMvUser().getTaluka());
                                            //  LocationSelectionActity.selectedTaluka = user.getMvUser().getTaluka();
                                        } else if (resultJsonObj.getString("locationLevel").equals("Cluster")) {
                                            ///  LocationSelectionActity.selectedCluster = user.getMvUser().getCluster();
                                            taskList.setTask_Response__c(user.getMvUser().getCluster());
                                        } else if (resultJsonObj.getString("locationLevel").equals("Village")) {
                                            // LocationSelectionActity.selectedVillage = user.getMvUser().getVillage();

                                            taskList.setTask_Response__c(user.getMvUser().getVillage());
                                        } else if (resultJsonObj.getString("locationLevel").equals("School")) {
                                            taskList.setTask_Response__c(user.getMvUser().getSchool_Name());
                                            //  LocationSelectionActity.selectedSchool = user.getMvUser().getSchool_Name();
                                        }
                                        if (resultJsonObj.getString("isHeader").equals("true")) {
                                            if (!taskList.getTask_Response__c().equals("Select")) {
                                                sb.append(prefix);
                                                prefix = " , ";
                                                sb.append(taskList.getTask_Response__c());
                                            }
                                        }
                                    }
                                    taskList.setMV_Process__c(resultJsonObj.getString("mVProcess"));
                                    taskList.setTask_Text__c(resultJsonObj.getString("taskText"));
                                    taskList.setTask_type__c(resultJsonObj.getString("tasktype"));
                                    taskList.setValidation(resultJsonObj.getString("validaytionOnText"));
                                    taskList.setIsSave(Constants.PROCESS_STATE_SAVE);

                                    // processList.setTimestamp__c(resultJsonObj.getString("Timestamp__c"));
                                    // processList.setMTUser__c(resultJsonObj.getString("MTUser__c"));

                                    ProgrammeManagmentFragment.this.taskList.add(taskList);


                                }
                                // each task list  convert to String and stored in process task filled
                                taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                                taskContainerModel.setHeaderPosition(sb.toString());
                                taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);
                                //task without answer
                                taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                                taskContainerModel.setMV_Process__c(processList.getId());
                                //delete old question
                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().deleteQuestion(processList.getId(), Constants.TASK_QUESTION);
                                //add new question
                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertTask(taskContainerModel);


                            }
                            AppDatabase.getAppDatabase(context).userDao().deleteTable();
                            AppDatabase.getAppDatabase(context).userDao().insertProcess(processAllList);
                            for (int i = 0; i < processCategory.size(); i++) {
                                childList.put(processCategory.get(i), AppDatabase.getAppDatabase(context).userDao().getProcessCatagry(processCategory.get(i)));
                            }
                            mAdapter.notifyDataSetChanged();
                            textNoData.setVisibility(View.GONE);
                            adapter = new ExpandableProcessListAdapter(context, processCategory, childList);
                            binding.rvProcess.setAdapter(adapter);
                        } else {
                            textNoData.setVisibility(View.VISIBLE);
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
