package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.TemplateAdapter;
import com.mv.BR;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityNewTemplateBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndicatorTaskList extends AppCompatActivity implements View.OnClickListener {

    private ActivityNewTemplateBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    //private ActivityProgrammeManagmentBinding binding;
    private PreferenceHelper preferenceHelper;
    ArrayList<Template> programManagementProcessLists=new ArrayList<>();
    private TemplateAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_template);
        binding.setVariable(BR.vm, new ParentViewModel());
        initViews();
    }


    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        setActionbar(getString(R.string.indicator));
        mAdapter = new TemplateAdapter(programManagementProcessLists, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(this))
            getAllIndicatorTask();
    }

    public void onLayoutScheduleTraining() {
        Intent intent;
        intent = new Intent(IndicatorTaskList.this, ScheduleTrainingActivity.class);
        startActivity(intent);
    }

    public void onLayoutClassObservation() {
        Intent intent;
        intent = new Intent(IndicatorTaskList.this, ClassObservationActivity.class);
        startActivity(intent);
    }

    public void onLayoutOrganizeEvents() {

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
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    private void getAllIndicatorTask() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getSessionData/";

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    programManagementProcessLists.clear();
                    for(int i=0;i<jsonArray.length();i++) {
                        Template processList = new Template();

                        processList.setName(jsonArray.getString(i));
                              programManagementProcessLists.add(processList);
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
