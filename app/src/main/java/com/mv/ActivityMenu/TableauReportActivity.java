package com.mv.ActivityMenu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.TableauReportsListAdapter;
import com.mv.Model.Report;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableauReportActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity context;
    private TableauReportsListAdapter mAdapter;
    private PreferenceHelper preferenceHelper;

    private List<String> reportsHeaderList = new ArrayList<>();
    private Map<String, List<Report>> reportsList = new HashMap<>();

    private int lastExpandedPosition = -1;

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_expandable_list);
        setActionbar(getString(R.string.indicator));
    }

    private void setActionbar(String title) {
        String str = title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }

        findViewById(R.id.layoutList).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.toolbar_title)).setText(str);

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
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(context);
        mAdapter = new TableauReportsListAdapter(context, reportsHeaderList, reportsList);

        if (Utills.isConnected(context)) {
            getAllReportProcess();
        } else {
            Utills.showInternetPopUp(context);
        }

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListView.setAdapter(mAdapter);
        expListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);
        expListView.setOnGroupExpandListener(groupPosition -> {
            if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                expListView.collapseGroup(lastExpandedPosition);
            }
            lastExpandedPosition = groupPosition;
        });
        expListView.setOnGroupCollapseListener(groupPosition -> {});
    }

    private void getAllReportProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));

        ServiceRequest apiService = ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAllTableauReports?userId="
                + User.getCurrentUser(context).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.isSuccess() && response.body() != null) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        reportsHeaderList.clear();
                        reportsList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Report reportModel = new Report();
                            reportModel.setId(jsonObject.getString("Id"));
                            reportModel.setReportNameC(jsonObject.getString("Report_Name__c"));
                            reportModel.setCategoryC(jsonObject.getString("Category__c"));
                            reportModel.setTableauLinkC(jsonObject.getString("Tableau_Link__c"));

                            if (reportsList.containsKey(jsonObject.getString("Category__c"))) {
                                List<Report> item = reportsList.get(jsonObject.getString("Category__c"));
                                item.add(reportModel);
                                reportsList.put(jsonObject.getString("Category__c"), item);
                            } else {
                                List<Report> item = new ArrayList<>();
                                item.add(reportModel);
                                reportsList.put(jsonObject.getString("Category__c"), item);
                                reportsHeaderList.add(jsonObject.getString("Category__c"));
                            }
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException | IOException e) {
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
