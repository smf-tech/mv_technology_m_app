package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.OverallReportAdapter;
import com.mv.Model.LocationModel;
import com.mv.Model.OverAllModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityOverallReportBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OverallReportActivity extends AppCompatActivity implements View.OnClickListener {
    private OverallReportAdapter mAdapter;
    private ActivityOverallReportBinding binding;
    private List<OverAllModel> overAllTaskList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    TextView textNoData;
    private Activity context;
    private RecyclerView.LayoutManager mLayoutManager;
    private LocationModel locationModel;
    private String roleList;
    private ImageView img_back, img_list, img_logout, location;
    private TextView toolbar_title;

    private List<String> temp;
    private String title;
    private String processId,fromDate,toDate;
    private int totalExpectedCount;
    private int totalSubmitedCount;
    private Task task;
    private RelativeLayout mToolBar;
    private ArrayList<String> selectedRoleList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_overall_report);
        binding.setActivity(this);

        preferenceHelper = new PreferenceHelper(this);
        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);

        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString(Constants.TITLE);
            processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
            locationModel = getIntent().getExtras().getParcelable(Constants.LOCATION);
            fromDate=getIntent().getExtras().getString("FromDate");
            toDate=getIntent().getExtras().getString("ToDate");
        }

        selectedRoleList = new ArrayList<>(Arrays.asList(getColumnIdex((roleList).split(";"))));
        if (locationModel == null) {
            locationModel = new LocationModel();
            locationModel.setState(User.getCurrentUser(getApplicationContext()).getMvUser().getState());
            locationModel.setDistrict(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
            locationModel.setTaluka(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            Date dateFrom = calendar.getTime();
            CharSequence tof  = DateFormat.format("yyyy-MM-dd", dateFrom.getTime());
            fromDate=tof.toString();

            Date dateTo = new Date();
            CharSequence tot  = DateFormat.format("yyyy-MM-dd", dateTo.getTime());
            toDate=tot.toString();
        }

        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(findViewById(R.id.layout_main), context);

        binding.spinnerRole.setText(roleList);
        binding.spinnerRole.setOnClickListener(v -> showRoleDialog());

        setActionbar("Overall Report");
        mAdapter = new OverallReportAdapter(overAllTaskList, context);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

        if (Utills.isConnected(getApplicationContext())) {
            getDashBoardData(roleList);
        }

        totalExpectedCount = 0;
        totalSubmitedCount = 0;
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


        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.VISIBLE);
        img_list.setOnClickListener(this);
        img_list.setImageResource(R.drawable.filter);
        img_logout.setImageResource(R.drawable.share_report);


    }

    private void getDashBoardData(String role) {
        if (Utills.isConnected(this)) {

            Utills.showProgressDialog(this);

            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getDashboardExpectedCount1?state=" + locationModel.getState()
                    + "&district=" + locationModel.getDistrict() + "&taluka=" + locationModel.getTaluka()
                    + "&role=" + role + "&processId=" + processId + "&dateFrom=" + fromDate +"&dateTo="+ toDate;
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            JSONArray jsonArray = new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            overAllTaskList = Arrays.asList(gson.fromJson(data, OverAllModel[].class));
                                /*for (int i=0;i<jsonArray.length();i++) {
                                    OverAllModel overAllModel=new OverAllModel();
                                    overAllTaskList.add(overAllModel);
                                }*/
                            for (int i = 0; i < overAllTaskList.size(); i++) {
                                totalExpectedCount = totalExpectedCount + overAllTaskList.get(i).getExpectedCount();
                                totalSubmitedCount = totalSubmitedCount + overAllTaskList.get(i).getSubmittedCount();
                            }
                            binding.tvTotalExpectedCount.setText("Total Expected Count:" + totalExpectedCount);
                            binding.tvTotalSubmitedCount.setText("Total Submited Count:" + totalSubmitedCount);
                            mAdapter = new OverallReportAdapter(overAllTaskList, context);
                            binding.recyclerView.setAdapter(mAdapter);
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
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            });

        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.img_list:
                Intent openClass = new Intent(OverallReportActivity.this, IndicatorLocationSelectionActivity.class);
                openClass.putExtra(Constants.TITLE, title);
                openClass.putExtra(Constants.INDICATOR_TASK, task);
                openClass.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                openClass.putExtra(Constants.PROCESS_ID, processId);
                openClass.putExtra(Constants.LOCATION, locationModel);
                openClass.putExtra("FromDate", fromDate);
                openClass.putExtra("ToDate", toDate);

                startActivity(openClass);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
        }

    }

    private void showRoleDialog() {


        if (preferenceHelper.getString(Constants.RoleList) != null && !preferenceHelper.getString(Constants.RoleList).isEmpty()) {
            temp = new ArrayList<>(Arrays.asList(preferenceHelper.getString(Constants.RoleList).split(";")));

        }

        //  final List<Community> temp = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            if (selectedRoleList.contains(temp.get(i))) {
                mSelection[i] = true;
            }
        }


      /* if(temp.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()))
        mSelection[temp.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;
*/
// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                .setTitle("Select Role")
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (which < mSelection.length) {
                        mSelection[which] = isChecked;

                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(getString(R.string.ok), (dialog12, id) -> {
                    StringBuilder sb = new StringBuilder();
                    String prefix = "";
                    for (int i = 0; i < items.length; i++) {
                        if (mSelection[i]) {
                            sb.append(prefix);
                            prefix = ";";
                            sb.append(temp.get(i));

                        }
                    }

                    if (Utills.isConnected(getApplicationContext())) {
                        getDashBoardData(sb.toString());
                    }
                    binding.spinnerRole.setText(sb.toString());
                    roleList=sb.toString();
                    selectedRoleList = new ArrayList<>(Arrays.asList(getColumnIdex((roleList).split(";"))));
                }).setNegativeButton(getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();


        dialog.show();
    }

    private static String[] getColumnIdex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;
    }
}
