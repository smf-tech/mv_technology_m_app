package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.VoucherAdapter;
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
import com.mv.databinding.ActivityVoucherListBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class VoucherListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityVoucherListBinding binding;
    private VoucherAdapter adapter;
    private List<Voucher> mList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_list);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.voucher_list));
        preferenceHelper = new PreferenceHelper(this);
        binding.rvVoucher.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < -5 && binding.fabAddProcess.getVisibility() != View.VISIBLE) {
                    binding.fabAddProcess.show();
                } else if (dy > 5 && binding.fabAddProcess.getVisibility() == View.VISIBLE) {
                    binding.fabAddProcess.hide();
                }
            }
        });

        if (Utills.isConnected(this)){
            if(Constants.AccountTeamCode.equals("TeamManagement")){
                getUserVoucherDataForTeam();
                binding.fabAddProcess.setVisibility(View.GONE);
            } else {
                getUserVoucherData();
            }
        }

    }

    //Sapret call for Team Management
    private void getUserVoucherDataForTeam() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserVoucherData + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Voucher[].class)) != null) {
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteAllVoucher();
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().insertVoucher();
                                mList=Arrays.asList(gson.fromJson(str, Voucher[].class));
                                setRecyclerViewForTeam();
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

    private void getUserVoucherData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserVoucherData + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Voucher[].class)) != null) {
                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteAllVoucher();
                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().insertVoucher(Arrays.asList(gson.fromJson(str, Voucher[].class)));
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
        mList = AppDatabase.getAppDatabase(this).userDao().getAllVoucher();
        adapter = new VoucherAdapter(this, mList);
        binding.rvVoucher.setAdapter(adapter);
        binding.rvVoucher.setHasFixedSize(true);
        binding.rvVoucher.setLayoutManager(new LinearLayoutManager(this));
    }
    private void setRecyclerViewForTeam() {
        adapter = new VoucherAdapter(this, mList);
        binding.rvVoucher.setAdapter(adapter);
        binding.rvVoucher.setHasFixedSize(true);
        binding.rvVoucher.setLayoutManager(new LinearLayoutManager(this));
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
        intent = new Intent(this, VoucherNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
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
        setRecyclerView();
    }

    public void editVoucher(int position) {
        Intent intent;
        intent = new Intent(this, VoucherNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        intent.putExtra(Constants.VOUCHER, mList.get(position));
        startActivity(intent);
    }

    public void deleteVoucher(int position) {
        if (Utills.isConnected(this)) {
            deleteRecord(position);
        } else {
            Utills.showInternetPopUp(this);
        }

    }

    private void deleteRecord(int position) {
        Utills.showProgressDialog(this, "Deleting Voucher", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + mList.get(position).getId() + "&Object=Voucher";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.contains("deleted")) {
                            AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteVoucher(mList.get(position));
                            AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteExpense(mList.get(position).getId());
                            mList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Utills.showToast("Voucher Deleted Successfully", VoucherListActivity.this);
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
