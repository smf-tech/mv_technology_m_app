package com.mv.Fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mv.Activity.ClassObservationActivity;
import com.mv.Activity.ScheduleTrainingActivity;
import com.mv.Adapter.IndicatorListAdapter;
import com.mv.Adapter.TemplateAdapter;
import com.mv.Adapter.TrainingAdapter;
import com.mv.BR;
import com.mv.Model.DashaBoardListModel;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Task;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
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

public class IndicatorListFragmet  extends Fragment {
    private PreferenceHelper preferenceHelper;
    List<DashaBoardListModel> processAllList = new ArrayList<>();
    private IndicatorListAdapter mAdapter;
    private ActivityNewTemplateBinding binding;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.activity_new_template, container, false);
        View view = binding.getRoot();
        binding.setVariable(BR.vm, new ParentViewModel());
        RelativeLayout mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(getActivity());
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (Utills.isConnected(getActivity()))
                            getAllProcess();
                    }
                }
        );


        mAdapter = new IndicatorListAdapter( getActivity(),processAllList);
        mLayoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(getActivity()))
            getAllProcess();
        else {
            Utills.showInternetPopUp(getActivity());
        }
    }




    private void getAllProcess() {
        Utills.showProgressDialog(getActivity(), "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getProcessDashBoardData?userId="+User.getCurrentUser(getActivity()).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    processAllList.clear();
                    DashaBoardListModel processList = new DashaBoardListModel();
                    if(User.getCurrentUser(getActivity()).getState().equals("Maharashtra")) {
                        processList.setName("मूल्यवर्धन 4दिवसीय तालुका पातळी कार्यशाळा - प्रशिक्षणार्थी अभिप्राय");
                        processAllList.add(processList);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        processList = new DashaBoardListModel();
                        JSONObject jsonObject= jsonArray.getJSONObject(i);

                        JSONObject processObj=jsonObject.getJSONObject("process");
                        processList.setId(processObj.getString("Id"));
                        processList.setName(processObj.getString("Name"));
                        JSONArray tasklist=jsonObject.getJSONArray("taskList");
                        for (int j = 0; j < tasklist.length(); j++) {
                            Task task=new Task();
                            task.setId(tasklist.getJSONObject(j).getString("Id"));
                            task.setTask_Text__c(tasklist.getJSONObject(j).getString("Task_Text__c"));
                            task.setTask_type__c(tasklist.getJSONObject(j).getString("Task_type__c"));
                            if (tasklist.getJSONObject(j).has("Location_Level__c"))
                                task.setLocationLevel(tasklist.getJSONObject(j).getString("Location_Level__c"));
                            task.setMV_Process__c(tasklist.getJSONObject(j).getString("MV_Process__c"));
                            processList.getTasksList().add(task);


                        }
                        processAllList.add(processList);
                    }

                    mAdapter.notifyDataSetChanged();
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
