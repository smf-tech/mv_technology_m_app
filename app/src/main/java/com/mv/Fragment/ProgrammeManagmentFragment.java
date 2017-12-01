package com.mv.Fragment;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

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
import com.mv.Adapter.TemplateAdapter;
import com.mv.BR;
import com.mv.Model.ParentViewModel;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class
ProgrammeManagmentFragment extends Fragment {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();
    private TemplateAdapter mAdapter;
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
        initViews();
        return view;
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


        mAdapter = new TemplateAdapter(processAllList, getActivity());
         mLayoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(getActivity()))
            getAllProcess();
        else
        {
            processAllList.clear();
            processAllList=AppDatabase.getAppDatabase(getActivity()).userDao().getProcess();
            mAdapter = new TemplateAdapter(processAllList, getActivity());
             mLayoutManager = new LinearLayoutManager(getActivity());
            binding.recyclerView.setAdapter(mAdapter);
        }
    }






    private void getAllProcess() {
        Utills.showProgressDialog(getActivity(), "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getProcess/"+ User.getCurrentUser(getActivity()).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        processAllList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Template processList = new Template();

                            processList.setType(jsonArray.getJSONObject(i).getJSONObject("attributes").getString("type"));
                            processList.setUrl(jsonArray.getJSONObject(i).getJSONObject("attributes").getString("url"));
                            processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                            processList.setName(jsonArray.getJSONObject(i).getString("Name"));

                            processList.setIs_Editable__c(jsonArray.getJSONObject(i).getBoolean("Is_Editable__c"));
                            processList.setIs_Multiple_Entry_Allowed__c(jsonArray.getJSONObject(i).getBoolean("Is_Multiple_Entry_Allowed__c"));

                            processList.setLocation(jsonArray.getJSONObject(i).getBoolean("Location_Required__c"));

                            if (jsonArray.getJSONObject(i).has("Location_Level__c"))
                                processList.setLocationLevel(jsonArray.getJSONObject(i).getString("Location_Level__c"));

                            processAllList.add(processList);
                        }
                        AppDatabase.getAppDatabase(getActivity()).userDao().deleteTable();
                        AppDatabase.getAppDatabase(getActivity()).userDao().insertProcess(processAllList);
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
