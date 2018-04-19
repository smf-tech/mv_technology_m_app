package com.mv.Activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.TeamManagementAdapter;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityTeamManagementUserProfileActivityBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamManagementUserProfileListActivity extends AppCompatActivity implements View.OnClickListener {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();

    ArrayList<Template> repplicaCahart = new ArrayList<>();
    private TeamManagementAdapter mAdapter;
    private ActivityTeamManagementUserProfileActivityBinding binding;
    RecyclerView.LayoutManager mLayoutManager;
    Activity context;
    private RelativeLayout mToolBar;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    public static String approvalType,id,processTitle;
    String url;
    TextView textNoData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_team_management_user_profile_activity);
        binding.setActivity(this);
        approvalType=getIntent().getExtras().getString(Constants.APPROVAL_TYPE);
        initViews();
    }

    private void initViews() {
        textNoData = (TextView) findViewById(R.id.textNoData);
        preferenceHelper = new PreferenceHelper(context);
        if(approvalType.equals(Constants.USER_APPROVAL)) {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetApprovalDataUrl+"?userId=" + User.getCurrentUser(context).getMvUser().getId();

            setActionbar(getString(R.string.team_user_approval));
        }else if(approvalType.equals(Constants.PROCESS_APPROVAL))
        {
            id=getIntent().getExtras().getString(Constants.ID);
            processTitle= getIntent().getExtras().getString(Constants.TITLE);
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                      + Constants.WS_getProcessAprovalUserUrl+"?UserId="+ User.getCurrentUser(context).getMvUser().getId()+ "&processId="+id ;;
            setActionbar(processTitle);
        }
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (Utills.isConnected(context))
                            getAllProcess();
                    }
                }
        );

        binding.editTextEmail.addTextChangedListener(watch);
        mAdapter = new TeamManagementAdapter(processAllList, context);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        if (Utills.isConnected(context))
            getAllProcess();
        else
        {
            Utills.showInternetPopUp(context);
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
    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Users", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
       // String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
         //       + "/services/apexrest/WS_getProcessAprovalUser?UserId="+ User.getCurrentUser(context).getMvUser().getId()+ "&processId=a1I7F000000VeJQUA0" ;;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                if(response.isSuccess()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length() != 0) {


                            processAllList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Template processList = new Template();


                                processList.setId(jsonArray.getJSONObject(i).getString("Id"));
                                if (jsonArray.getJSONObject(i).has("username"))
                                    processList.setName(jsonArray.getJSONObject(i).getString("username"));
                                else if (jsonArray.getJSONObject(i).has("name"))
                                    processList.setName(jsonArray.getJSONObject(i).getString("username"));
                                   if  (jsonArray.getJSONObject(i).has("status"))
                                    processList.setStatus(jsonArray.getJSONObject(i).getString("status"));
                                processAllList.add(processList);
                            }
                       ///     AppDatabase.getAppDatabase(context).userDao().deleteTable();
                          //  AppDatabase.getAppDatabase(context).userDao().insertProcess(processAllList);
                            mAdapter.notifyDataSetChanged();
                            textNoData.setVisibility(View.GONE);
                        }else {
                            textNoData.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    } TextWatcher watch = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            // TODO Auto-generated method stub
            setFilter(s.toString());

        }
    };
    private void setFilter(String s) {
        List<Template> list = new ArrayList<>();
        repplicaCahart.clear();
        for (int i = 0; i < processAllList.size(); i++) {
            repplicaCahart.add(processAllList.get(i));
        }
        for (int i = 0; i < repplicaCahart.size(); i++) {
            if (repplicaCahart.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                list.add(repplicaCahart.get(i));
            }
        }
        repplicaCahart.clear();
        for (int i = 0; i < list.size(); i++) {
            repplicaCahart.add(list.get(i));
        }
        mAdapter = new TeamManagementAdapter( repplicaCahart,context);
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllProcess();
    }
}
