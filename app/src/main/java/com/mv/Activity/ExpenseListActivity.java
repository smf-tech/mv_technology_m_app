package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ExpandableExpenseListAdapter;
import com.mv.Model.Expense;
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
import com.mv.databinding.ActivityExpenseListBinding;

import org.json.JSONException;
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

public class ExpenseListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityExpenseListBinding binding;
    private List<Expense> mList = new ArrayList<>();
    private Voucher voucher;
    private PreferenceHelper preferenceHelper;
    private ArrayList<String> headerList;
    private HashMap<String, ArrayList<Expense>> childList;
    private ExpandableExpenseListAdapter evAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_list);
        binding.setActivity(this);
        headerList = new ArrayList<>();
        childList = new HashMap<>();
        headerList.add(getString(R.string.pending));
        headerList.add(getString(R.string.reject));
        headerList.add(getString(R.string.approve));
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.expense_list));
        preferenceHelper = new PreferenceHelper(this);
        voucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
//        binding.rvExpense.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                Log.i("Dy", "" + dy);
//                if (dy < -5 && binding.fabAddProcess.getVisibility() == View.GONE) {
//
//                } else if (dy > 5 && binding.fabAddProcess.getVisibility() == View.VISIBLE) {
//
//                }
//            }
//        });
        binding.evProcess.setOnScrollListener(new AbsListView.OnScrollListener() {
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

//        if (Utills.isConnected(this))
//            getUserExpenseData();
//
//        if (Utills.isConnected(this)) {
//            if (Constants.AccountTeamCode.equals("TeamManagement")) {
//                getUserExpenseDataForTeam();
//                binding.fabAddProcess.setVisibility(View.GONE);
//            } else {
//                getUserExpenseData();
//            }
//        }

        //commented above lines and added following code to work offine properly
        if (Utills.isConnected(this)) {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                getUserExpenseDataForTeam();
                binding.fabAddProcess.setVisibility(View.GONE);
            }else {
                getUserExpenseData();
            }
        }else {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                Utills.showToast(getResources().getString(R.string.error_no_internet),this);
            }else{
                setRecyclerView();
            }
        }
    }

    private void getUserExpenseDataForTeam() {

        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetPendingExpenseData + "?userId="
                + User.getCurrentUser(ExpenseListActivity.this).getMvUser().getId()
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
                                mList = Arrays.asList(gson.fromJson(object.getString("salaries"), Expense[].class));
                            }
                            if (object.has("action")) {
                                Constants.USERACTION = object.getString("action");
                            }
                            setRecyclerViewForTeam();
                            /*if (Arrays.asList(gson.fromJson(str, Expense[].class)) != null) {
//                                AppDatabase.getAppDatabase(ExpenseListActivity.this).userDao().deleteExpense(voucher.getId());
//                                AppDatabase.getAppDatabase(ExpenseListActivity.this).userDao().insertExpense(Arrays.asList(gson.fromJson(str, Expense[].class)));
                                mList = Arrays.asList(gson.fromJson(str, Expense[].class));
                                setRecyclerViewForTeam();
                            }*/
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });

    }

    private void getUserExpenseData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserExpenseData + "?voucherId=" + voucher.getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Expense[].class)).size()>0) {
                                AppDatabase.getAppDatabase(ExpenseListActivity.this).userDao().deleteExpense(voucher.getId());
                                AppDatabase.getAppDatabase(ExpenseListActivity.this).userDao().insertExpense(Arrays.asList(gson.fromJson(str, Expense[].class)));
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
        Log.i("Count", "" + AppDatabase.getAppDatabase(this).userDao().getAllExpense());
        mList = AppDatabase.getAppDatabase(this).userDao().getAllExpense(voucher.getId());
       /* adapter = new ExpenseAdapter(this, mList);
        binding.rvExpense.setAdapter(adapter);
        binding.rvExpense.setHasFixedSize(true);
        binding.rvExpense.setLayoutManager(new LinearLayoutManager(this));*/
        ArrayList<Expense> pendingList = new ArrayList<>();
        ArrayList<Expense> approveList = new ArrayList<>();
        ArrayList<Expense> rejectList = new ArrayList<>();

        for (Expense expense : mList) {
            if (expense.getStatus().equals(Constants.LeaveStatusApprove))
                approveList.add(expense);
            if (expense.getStatus().equals(Constants.LeaveStatusPending))
                pendingList.add(expense);
            if (expense.getStatus().equals(Constants.LeaveStatusRejected))
                rejectList.add(expense);
        }
        childList.put(getString(R.string.pending), pendingList);
        childList.put(getString(R.string.reject), rejectList);
        childList.put(getString(R.string.approve), approveList);
        evAdapter = new ExpandableExpenseListAdapter(this, headerList, childList);
        binding.evProcess.setAdapter(evAdapter);
    }

    private void setRecyclerViewForTeam() {

        ArrayList<Expense> pendingList = new ArrayList<>();
        ArrayList<Expense> approveList = new ArrayList<>();
        ArrayList<Expense> rejectList = new ArrayList<>();

        for (Expense expense : mList) {
            if (expense.getStatus().equals(Constants.LeaveStatusApprove))
                approveList.add(expense);
            if (expense.getStatus().equals(Constants.LeaveStatusPending))
                pendingList.add(expense);
            if (expense.getStatus().equals(Constants.LeaveStatusRejected))
                rejectList.add(expense);
        }
        childList.put(getString(R.string.pending), pendingList);
        childList.put(getString(R.string.reject), rejectList);
        childList.put(getString(R.string.approve), approveList);
        evAdapter = new ExpandableExpenseListAdapter(this, headerList, childList);
        binding.evProcess.setAdapter(evAdapter);

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
        intent = new Intent(this, ExpenseNewActivity.class);
        intent.putExtra(Constants.VOUCHER, voucher);
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
//        if (Constants.AccountTeamCode.equals("TeamManagement")) {
//            getUserExpenseDataForTeam();
//            binding.fabAddProcess.setVisibility(View.GONE);
//        } else {
//            setRecyclerView();
//        }
        //commented above lines and added following code to work properly when back button has pressed.
        if (Utills.isConnected(this)) {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                getUserExpenseDataForTeam();
                binding.fabAddProcess.setVisibility(View.GONE);
            }else {
                getUserExpenseData();
            }
        }else {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                Utills.showToast(getResources().getString(R.string.error_no_internet),this);
            }else{
                setRecyclerView();
            }
        }
    }

    public void editExpense(Expense expense) {
        Intent intent;
        intent = new Intent(this, ExpenseNewActivity.class);
        intent.putExtra(Constants.VOUCHER, voucher);
        intent.putExtra(Constants.EXPENSE, expense);
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        startActivity(intent);
    }

    public void deleteExpense(Expense expense) {
        if (Utills.isConnected(this)) {
            deleteRecord(expense);
        } else {
            Utills.showInternetPopUp(this);
        }
    }

    private void deleteRecord(Expense expense)

    {
        Utills.showProgressDialog(this, "Deleting Expense", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + expense.getId() + "&Object=Expense";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.contains("deleted")) {
                            AppDatabase.getAppDatabase(ExpenseListActivity.this).userDao().deleteExpense(expense);
                            setRecyclerView();
                            Utills.showToast("Expense Deleted Successfully", ExpenseListActivity.this);
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
