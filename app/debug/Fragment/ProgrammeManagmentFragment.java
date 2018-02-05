package com.mv.MenuActivity;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
ProgrammeManagmentFragment extends AppCompatActivity {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();
    private TemplateAdapter mAdapter;
    private ActivityNewTemplateBinding binding;
    RecyclerView.LayoutManager mLayoutManager;
    TextView textNoData;

    Activity context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        context=this;
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_new_template);
        binding.setVariable(BR.vm, new ParentViewModel());

        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);
        initViews();
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

        textNoData = (TextView) findViewById(R.id.textNoData);
        mAdapter = new TemplateAdapter(processAllList, context);
         mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(context))
            getAllProcess();
        else
        {
            processAllList.clear();
            processAllList=AppDatabase.getAppDatabase(context).userDao().getProcess();
            mAdapter = new TemplateAdapter(processAllList, context);
             mLayoutManager = new LinearLayoutManager(context);
            binding.recyclerView.setAdapter(mAdapter);
        }
    }






    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getProcess/"+ User.getCurrentUser(context).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length()!=0) {
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
                            AppDatabase.getAppDatabase(context).userDao().deleteTable();
                            AppDatabase.getAppDatabase(context).userDao().insertProcess(processAllList);
                            mAdapter.notifyDataSetChanged();
                            textNoData.setVisibility(View.GONE);
                        }else {
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
