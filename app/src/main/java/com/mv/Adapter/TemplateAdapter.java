package com.mv.Adapter;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.HomeActivity;
import com.mv.Activity.IndicatorTaskList;
import com.mv.Activity.LocationSelectionActity;
import com.mv.Activity.PiachartActivity;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Activity.TemplatesActivity;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

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

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;
    ArrayList<Task> programManagementProcessLists = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof TemplatesActivity)
                        ((TemplatesActivity) mContext).onLayoutTemplateClick(getAdapterPosition());

                    else if (mContext instanceof HomeActivity) {
                        preferenceHelper.insertBoolean(Constants.IS_EDITABLE, teplateList.get(getAdapterPosition()).getIs_Editable__c());
                        preferenceHelper.insertBoolean(Constants.IS_LOCATION, teplateList.get(getAdapterPosition()).getLocation());
                        preferenceHelper.insertBoolean(Constants.IS_MULTIPLE, teplateList.get(getAdapterPosition()).getIs_Multiple_Entry_Allowed__c());

                        preferenceHelper.insertString(Constants.STATE_LOCATION_LEVEL, teplateList.get(getAdapterPosition()).getLocationLevel());

                            Intent openClass = new Intent(mContext, ProcessListActivity.class);
                            openClass.putExtra(Constants.PROCESS_ID, teplateList.get(getAdapterPosition()).getId());
                            openClass.putExtra(Constants.PROCESS_NAME, teplateList.get(getAdapterPosition()).getName());
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    }
                    else if (mContext instanceof IndicatorTaskList) {
                        Intent openClass = new Intent(mContext, PiachartActivity.class);
                        openClass.putExtra(Constants.INDICATOR_TASK,teplateList.get(getAdapterPosition()).getName());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }

                }
            });
        }
    }


    public TemplateAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Template template = teplateList.get(position);
        holder.txtCommunityName.setText(template.getName());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }

    private void  getAllTask(final String proceesId) {
        Utills.showProgressDialog(mContext, "Loading Process", mContext.getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
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
                    programManagementProcessLists.clear();

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

                        // processList.setTimestamp__c(resultJsonObj.getString("Timestamp__c"));
                        // processList.setMTUser__c(resultJsonObj.getString("MTUser__c"));

                        programManagementProcessLists.add(processList);


                    }
                    TaskContainerModel taskContainerModel = new TaskContainerModel();
                    taskContainerModel.setTaskListString(Utills.convertArrayListToString(programManagementProcessLists));
                    taskContainerModel.setIsSave("true");
                    taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                    taskContainerModel.setMV_Process__c(proceesId);
                    AppDatabase.getAppDatabase(mContext).userDao().deleteQuestion(proceesId,Constants.TASK_QUESTION);
                    AppDatabase.getAppDatabase(mContext).userDao().insertTask(taskContainerModel);

                    preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                    if (preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                        preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                        Intent openClass = new Intent(mContext, LocationSelectionActity.class);
                       // openClass.putExtra(Constants.PROCESS_ID, taskList);
                        openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, programManagementProcessLists);
                        //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else {
                        Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                      //  openClass.putExtra(Constants.PROCESS_ID, taskList);
                        openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, programManagementProcessLists);
                        //  openClass.putExtra("stock_list", resultList.get(programManagementProcessLists()).get(0));
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                   /* Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                    openClass.putExtra(Constants.PROCESS_ID, programManagementProcessLists);
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, programManagementProcessLists);
                    preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                    //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);*/
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