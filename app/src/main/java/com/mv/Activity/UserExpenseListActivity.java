package com.mv.Activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.ExpenseAdapter;
import com.mv.Model.Expense;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityUserExpenseListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class UserExpenseListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityUserExpenseListBinding binding;
    private ExpenseAdapter adapter;
    private List<Expense> mList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    public String mAction;
    private int changePosition;
    private String changeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_expense_list);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.expense_list));
        preferenceHelper = new PreferenceHelper(this);

        if (Utills.isConnected(this))
            getUserExpenseData();
    }

    private void getUserExpenseData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetPendingExpenseData + "?userId=" + User.getCurrentUser(UserExpenseListActivity.this).getMvUser().getId();
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
                                mAction = object.getString("action");
                            }
                            setRecyclerView();

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

    private void setRecyclerView() {
        if (mList.size() == 0) {
            binding.txtNDA.setVisibility(View.VISIBLE);
        } else {
            binding.txtNDA.setVisibility(View.GONE);
        }
        adapter = new ExpenseAdapter(this, mList);
        binding.rvExpense.setAdapter(adapter);
        binding.rvExpense.setHasFixedSize(true);
        binding.rvExpense.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
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


    public void changeStatus(int position, String status) {
        changePosition = position;
        changeStatus = status;
        if (mList.get(changePosition).getStatus().equalsIgnoreCase(changeStatus))
            Utills.showToast("Already " + changeStatus, UserExpenseListActivity.this);
        else
            changeExpense();
    }


    private void changeExpense() {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                Expense expense = mList.get(changePosition);
                expense.setStatus(changeStatus);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(expense);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertExpense", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        mList.get(changePosition).setStatus(changeStatus);
                                        adapter.notifyItemChanged(changePosition);
                                        Utills.showToast("Status of expense changed successfully", UserExpenseListActivity.this);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }

}
