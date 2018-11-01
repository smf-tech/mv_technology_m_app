package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.AdavanceAdapter;
import com.mv.Adapter.ExpandableAdvanceListAdapter;
import com.mv.Model.Adavance;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAdavanceListBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class AdavanceListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityAdavanceListBinding binding;
    private AdavanceAdapter adapter;
    private List<Adavance> mList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private Voucher voucher;
    private ArrayList<String> headerList;
    private HashMap<String, ArrayList<Adavance>> childList;
    private ExpandableAdvanceListAdapter evAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_adavance_list);
        binding.setActivity(this);

        headerList = new ArrayList<>();
        childList = new HashMap<>();
        headerList.add(getString(R.string.pending));
        headerList.add(getString(R.string.reject));
        headerList.add(getString(R.string.approve));

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.adavance_list));
        voucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
        preferenceHelper = new PreferenceHelper(this);

//        binding.rvAdavance.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy < -5 && binding.fabAddProcess.getVisibility() != View.VISIBLE) {
//                    binding.fabAddProcess.show();
//                } else if (dy > 5 && binding.fabAddProcess.getVisibility() == View.VISIBLE) {
//                    binding.fabAddProcess.hide();
//                }
//            }
//        });

        binding.evAdavance.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (mLastFirstVisibleItem < firstVisibleItem) {
                    binding.fabAddProcess.setVisibility(View.GONE);
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    binding.fabAddProcess.setVisibility(View.VISIBLE);
                }
                mLastFirstVisibleItem = firstVisibleItem;

            }
        });

//        if (Utills.isConnected(this)) {
//            if (Constants.AccountTeamCode.equals("TeamManagement")) {
//                getUserAdavanceDataForTeam();
//                binding.fabAddProcess.setVisibility(View.GONE);
//            } else {
//                getUserAdavanceData();
//            }
//        }
//      commented above lines and added following code to work offine properly
        if (Utills.isConnected(this)) {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                getUserAdavanceDataForTeam();
                binding.fabAddProcess.setVisibility(View.GONE);
            }else {
                getUserAdvanceData();
            }
        }else {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                Utills.showToast(getResources().getString(R.string.error_no_internet),this);
            }else{
                setRecyclerView();
            }
        }
    }

    //Sapret call for Team Management
    private void getUserAdavanceDataForTeam() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetPendingAdavanceData + "?userId="
                + User.getCurrentUser(AdavanceListActivity.this).getMvUser().getId()
                + "&voucherId=" + voucher.getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONObject object = new JSONObject(str);
                            if (object.has("salaries") && !(object.getString("salaries").equalsIgnoreCase("null"))) {
                                mList = Arrays.asList(gson.fromJson(object.getString("salaries"), Adavance[].class));
                            }
                            if (object.has("action")) {
                                Constants.USERACTION = object.getString("action");
                            }
                            setRecyclerViewForTeam();
                           /* if (Arrays.asList(gson.fromJson(str, Adavance[].class)) != null) {
//                                AppDatabase.getAppDatabase(AdavanceListActivity.this).userDao().deleteAllAdavance();
//                                AppDatabase.getAppDatabase(AdavanceListActivity.this).userDao().insertAdavance(Arrays.asList(gson.fromJson(str, Adavance[].class)));
                                mList = Arrays.asList(gson.fromJson(str, Adavance[].class));
                                setRecyclerViewForTeam();
                            }*/
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void getUserAdvanceData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserAdavanceData + "?voucherId=" + voucher.getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Adavance[].class)).size()>0) {
                                AppDatabase.getAppDatabase(AdavanceListActivity.this).userDao().deleteAllAdavance();
                                AppDatabase.getAppDatabase(AdavanceListActivity.this).userDao().insertAdavance(Arrays.asList(gson.fromJson(str, Adavance[].class)));
                                setRecyclerView();
                            }
                        }
                    }

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

    private void setRecyclerView() {
        mList = AppDatabase.getAppDatabase(this).userDao().getAllAdavance();
       /* adapter = new AdavanceAdapter(this, mList);
        binding.rvAdavance.setAdapter(adapter);
        binding.rvAdavance.setHasFixedSize(true);
        binding.rvAdavance.setLayoutManager(new LinearLayoutManager(this));
*/
        ArrayList<Adavance> pendingList = new ArrayList<>();
        ArrayList<Adavance> approveList = new ArrayList<>();
        ArrayList<Adavance> rejectList = new ArrayList<>();

        for (Adavance adavance : mList) {
            if (adavance.getStatus().equals(Constants.LeaveStatusApprove))
                approveList.add(adavance);
            if (adavance.getStatus().equals(Constants.LeaveStatusPending))
                pendingList.add(adavance);
            if (adavance.getStatus().equals(Constants.LeaveStatusRejected))
                rejectList.add(adavance);
        }
        childList.put(getString(R.string.pending), pendingList);
        childList.put(getString(R.string.reject), rejectList);
        childList.put(getString(R.string.approve), approveList);
        evAdapter = new ExpandableAdvanceListAdapter(this, headerList, childList);
        binding.evAdavance.setAdapter(evAdapter);
    }

    //Sapret call for Team Management
    private void setRecyclerViewForTeam() {

        ArrayList<Adavance> pendingList = new ArrayList<>();
        ArrayList<Adavance> approveList = new ArrayList<>();
        ArrayList<Adavance> rejectList = new ArrayList<>();

        for (Adavance adavance : mList) {
            if (adavance.getStatus().equals(Constants.LeaveStatusApprove))
                approveList.add(adavance);
            if (adavance.getStatus().equals(Constants.LeaveStatusPending))
                pendingList.add(adavance);
            if (adavance.getStatus().equals(Constants.LeaveStatusRejected))
                rejectList.add(adavance);
        }
        childList.put(getString(R.string.pending), pendingList);
        childList.put(getString(R.string.reject), rejectList);
        childList.put(getString(R.string.approve), approveList);
        evAdapter = new ExpandableAdvanceListAdapter(this, headerList, childList);
        binding.evAdavance.setAdapter(evAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    public void onAddClick() {
        Intent intent;
        intent = new Intent(this, AdavanceNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
        intent.putExtra(Constants.VOUCHER, voucher);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {

        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = Title;
        if (Title != null && Title.contains("\n"))
            str = Title.replace("\n", " ");
        toolbar_title.setText(str);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (Constants.AccountTeamCode.equals("TeamManagement")) {
//            getUserAdavanceDataForTeam();
//            binding.fabAddProcess.setVisibility(View.GONE);
//        } else {
//            setRecyclerView();
//        }
        //commented above lines and added following code to work properly when back button has pressed.
        if (Utills.isConnected(this)) {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                getUserAdavanceDataForTeam();
                binding.fabAddProcess.setVisibility(View.GONE);
            }else {
                getUserAdvanceData();
            }
        }else {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                Utills.showToast(getResources().getString(R.string.error_no_internet),this);
            }else{
                setRecyclerView();
            }
        }
    }

    public void editAdavance(Adavance adavance) {
        Intent intent;
        intent = new Intent(this, AdavanceNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        intent.putExtra(Constants.VOUCHER, voucher);
        intent.putExtra(Constants.ADAVANCE, adavance);
        startActivity(intent);
    }

    public void deleteAdavance(Adavance adavance) {
        if (Utills.isConnected(this)) {
            deleteRecord(adavance);
        } else {
            Utills.showInternetPopUp(this);
        }
    }

    private void deleteRecord(Adavance adavance) {
        Utills.showProgressDialog(this, "Deleting Adavance", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + adavance.getId() + "&Object=Adavance";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.contains("deleted")) {
                            AppDatabase.getAppDatabase(AdavanceListActivity.this).userDao().deleteAdavance(adavance);
                            setRecyclerView();
                            Utills.showToast("Adavance Deleted Successfully", AdavanceListActivity.this);
                        }
                    }

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
