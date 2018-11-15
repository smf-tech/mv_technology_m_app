package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.UserApprovalAdapter;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
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

    private List<Template> processAllList = new ArrayList<>();
    private List<Template> tempList = new ArrayList<>();
    private ArrayList<Template> copyOfProcessAllList = new ArrayList<>();

    private UserApprovalAdapter mAdapter;
    private ActivityTeamManagementUserProfileActivityBinding binding;
    private Activity context;

    public static String approvalType, id, processTitle;
    private String url;
    private TextView textNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_team_management_user_profile_activity);
        binding.setActivity(this);

        if (getIntent().getExtras() != null) {
            approvalType = getIntent().getExtras().getString(Constants.APPROVAL_TYPE);
        }

        initViews();
    }

    private void initViews() {
        binding.btnPending.setOnClickListener(this);
        binding.btnApprove.setOnClickListener(this);
        binding.btnReject.setOnClickListener(this);

        textNoData = (TextView) findViewById(R.id.textNoData);
        PreferenceHelper preferenceHelper = new PreferenceHelper(context);

        if (approvalType.equals(Constants.USER_APPROVAL)) {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.GetApprovalDataUrl + "?userId=" + User.getCurrentUser(context).getMvUser().getId();

            setActionbar(getString(R.string.team_user_approval));

        } else if (approvalType.equals(Constants.PROCESS_APPROVAL)) {
            if (getIntent().getExtras() != null) {
                id = getIntent().getExtras().getString(Constants.ID);
                processTitle = getIntent().getExtras().getString(Constants.TITLE);
            }

            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.WS_getProcessAprovalUserUrl + "?UserId="
                    + User.getCurrentUser(context).getMvUser().getId() + "&processId=" + id;

            setActionbar(processTitle);

            binding.lnrFilter.setVisibility(View.GONE);
        }

        binding.swiperefresh.setOnRefreshListener(() -> {
                    if (Utills.isConnected(context))
                        getAllProcess();
                }
        );

        binding.editTextEmail.addTextChangedListener(watch);
    //    mAdapter = new UserApprovalAdapter(context, processAllList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

//        if (Utills.isConnected(context)) {
//            getAllProcess();
//        } else {
//            Utills.showInternetPopUp(context);
//        }
    }

    private void setActionbar(String Title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.btn_pending:
                String sortString = "false";
                binding.btnPending.setBackgroundResource(R.drawable.selected_btn_background);
                binding.btnApprove.setBackgroundResource(R.drawable.light_grey_btn_background);
                binding.btnReject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;

            case R.id.btn_approve:
                sortString = "true";
                binding.btnPending.setBackgroundResource(R.drawable.light_grey_btn_background);
                binding.btnApprove.setBackgroundResource(R.drawable.selected_btn_background);
                binding.btnReject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView(sortString);
                break;

            case R.id.btn_reject:
                sortString = "Rejected";
                binding.btnPending.setBackgroundResource(R.drawable.light_grey_btn_background);
                binding.btnApprove.setBackgroundResource(R.drawable.light_grey_btn_background);
                binding.btnReject.setBackgroundResource(R.drawable.selected_btn_background);
                setRecyclerView(sortString);
                break;
        }
    }

    private void setRecyclerView(String Status) {
        tempList.clear();
        if(approvalType.equals(Constants.PROCESS_APPROVAL)){
            tempList.addAll(processAllList);
        }else {
            for (int i = 0; i < processAllList.size(); i++) {
                if (processAllList.get(i).getStatus() != null) {
                    if (processAllList.get(i).getStatus().equals(Status)) {
                        tempList.add(processAllList.get(i));
                    }
                }
            }
        }
        if (approvalType.equals(Constants.USER_APPROVAL)) {
            mAdapter = new UserApprovalAdapter(context, tempList,Constants.USER_APPROVAL);
        } else if (approvalType.equals(Constants.PROCESS_APPROVAL)) {
            mAdapter = new UserApprovalAdapter(context, tempList,Constants.PROCESS_APPROVAL);
        }
        binding.recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        if (tempList.size() == 0) {
            Utills.showToast("No data available.", this);
            binding.inputEmail.setVisibility(View.GONE);
        } else {
            binding.inputEmail.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Users", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(context).create(ServiceRequest.class);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);

                if (response.isSuccess()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length() != 0) {
                            processAllList.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                Template processList = new Template();
                                processList.setId(jsonArray.getJSONObject(i).getString("Id"));

                                if (jsonArray.getJSONObject(i).has("username")) {
                                    processList.setName(jsonArray.getJSONObject(i).getString("username"));
                                } else if (jsonArray.getJSONObject(i).has("name")) {
                                    processList.setName(jsonArray.getJSONObject(i).getString("username"));
                                }

                                if (jsonArray.getJSONObject(i).has("status")) {
                                    processList.setStatus(jsonArray.getJSONObject(i).getString("status"));
                                }

                                processAllList.add(processList);
                            }

                            setRecyclerView("false");
                            textNoData.setVisibility(View.GONE);
                        } else {
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
    }

    private TextWatcher watch = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            setFilter(s.toString());
        }
    };

    private void setFilter(String s) {
        List<Template> list = new ArrayList<>();
        copyOfProcessAllList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            copyOfProcessAllList.add(processAllList.get(i));
        }

        for (int i = 0; i < copyOfProcessAllList.size(); i++) {
            if (copyOfProcessAllList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                list.add(copyOfProcessAllList.get(i));
            }
        }

        copyOfProcessAllList.clear();
        copyOfProcessAllList.addAll(list);

        if (approvalType.equals(Constants.USER_APPROVAL)) {
            mAdapter = new UserApprovalAdapter(context, copyOfProcessAllList,Constants.USER_APPROVAL);
        } else if (approvalType.equals(Constants.PROCESS_APPROVAL)) {
            mAdapter = new UserApprovalAdapter(context, copyOfProcessAllList,Constants.PROCESS_APPROVAL);
        }

       // mAdapter = new UserApprovalAdapter(context, copyOfProcessAllList);
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllProcess();
    }
}