package com.mv.MenuActivity;

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

import com.mv.Adapter.IndicatorListAdapter;
import com.mv.BR;
import com.mv.Model.DashaBoardListModel;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityNewTemplateBinding;

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

/**
 * Created by nanostuffs on 14-11-2017.
 */

public class IndicatorListFragmet extends AppCompatActivity implements View.OnClickListener {
    private PreferenceHelper preferenceHelper;
    List<DashaBoardListModel> processAllList = new ArrayList<>();
    private IndicatorListAdapter mAdapter;
    private ActivityNewTemplateBinding binding;
    RecyclerView.LayoutManager mLayoutManager;

    Activity context;

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_template);
        binding.setVariable(BR.vm, new ParentViewModel());

        setActionbar(getString(R.string.indicator));
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
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


        mAdapter = new IndicatorListAdapter(context, processAllList);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(context))
            getAllProcess();
        else {
            Utills.showInternetPopUp(context);
        }
    }


    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getProcessDashBoardDatademo?userId=" + User.getCurrentUser(context).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        processAllList.clear();
                        DashaBoardListModel processList = new DashaBoardListModel();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            processList = new DashaBoardListModel();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            JSONObject processObj = jsonObject.getJSONObject("process");
                            processList.setId(processObj.getString("Id"));
                            processList.setName(processObj.getString("Name"));
                            processList.setMultiple_Role__c(processObj.getString("Show_Role_In_Mobile_dashboard__c"));
                            JSONArray tasklist = jsonObject.getJSONArray("taskList");
                            for (int j = 0; j < tasklist.length(); j++) {
                                Task task = new Task();
                                task.setId(tasklist.getJSONObject(j).getString("Id"));
                                task.setTask_Text__c(tasklist.getJSONObject(j).getString("Task_Text__c"));
                                task.setTask_type__c(tasklist.getJSONObject(j).getString("Task_type__c"));
                                task.setSection_Name__c(tasklist.getJSONObject(j).getString("Section_Name__c"));
                                if (tasklist.getJSONObject(j).has("Location_Level__c"))
                                    task.setLocationLevel(tasklist.getJSONObject(j).getString("Location_Level__c"));
                                task.setMV_Process__c(tasklist.getJSONObject(j).getString("MV_Process__c"));
                                processList.getTasksList().add(task);


                            }
                            processAllList.add(processList);
                        }

                        mAdapter.notifyDataSetChanged();
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
