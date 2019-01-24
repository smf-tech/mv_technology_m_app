package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.HolldayListAdapter;
import com.mv.Model.HolidayListModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityHolidayListBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HolidayListActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityHolidayListBinding binding;
    private Activity context;
    private PreferenceHelper preferenceHelper;
    private List<HolidayListModel> holidayListModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_holiday_list);
        binding.setClander(this);
        preferenceHelper = new PreferenceHelper(this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        getHolidayList();
        setActionbar(getString(R.string.holiday_list));
        binding.swiperefresh.setOnRefreshListener(
                () -> {
                    if (Utills.isConnected(context))
                        binding.swiperefresh.setRefreshing(false);
                }
        );

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);

        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
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
    private void getHolidayList() {
        if (User.getCurrentUser(getApplicationContext()).getMvUser() == null) {
            return;
        }

        if (Utills.isConnected(HolidayListActivity.this)) {
            Utills.showProgressDialog(HolidayListActivity.this, "Loading Holidays", getString(R.string.progress_please_wait));
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(HolidayListActivity.this).create(ServiceRequest.class);

            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAllHolidays?userId="
                    + User.getCurrentUser(getApplicationContext()).getMvUser().getId();

            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data.length() > 0) {
                                JSONArray jsonArray = new JSONArray(data);
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                AppDatabase.getAppDatabase(HolidayListActivity.this).userDao().deleteHolidayList();

                                List<HolidayListModel> holidayListModels = Arrays.asList(gson.fromJson(jsonArray.toString(), HolidayListModel[].class));
                                AppDatabase.getAppDatabase(HolidayListActivity.this).userDao().insertAllHolidayList(holidayListModels);
//                                holidayListModels = AppDatabase.getAppDatabase(HolidayListActivity.this).userDao().getAllHolidayList();
                                List<HolidayListModel> temp = new ArrayList<>();
                                for (int i = 0; i < holidayListModels.size(); i++) {
                                    if (!holidayListModels.get(i).getCategory__c().equals("Weekly Off")) {
                                        temp.add(holidayListModels.get(i));
                                    }

                                }
                                HolldayListAdapter holldayListAdapter = new HolldayListAdapter(context, temp);
                                binding.recyclerView.setAdapter(holldayListAdapter);
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
    }


}
