package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.GroupAdapter;
import com.mv.Adapter.OverallReportAdapter;
import com.mv.Adapter.PichartDescriptiveListAdapter;
import com.mv.Adapter.ProgramMangementAdapter;
import com.mv.Model.Community;
import com.mv.Model.LocationModel;
import com.mv.Model.OverAllModel;
import com.mv.Model.PiaChartModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityGroupBinding;
import com.mv.databinding.ActivityOverallReportBinding;

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

public class OverallReportActivity extends AppCompatActivity implements View.OnClickListener {
    private OverallReportAdapter mAdapter;
    private ActivityOverallReportBinding binding;
    private List<OverAllModel> overAllTaskList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    TextView textNoData;
    Activity context;
    RecyclerView.LayoutManager mLayoutManager;
    LocationModel locationModel;
    String roleList;
    private ImageView img_back, img_list, img_logout, location;
    private TextView toolbar_title;
    List<String> temp;
    String title,processId;
    int totalExpectedCount,totalSubmitedCount;
    Task task;
    private RelativeLayout mToolBar;
    ArrayList<String> selectedRoleList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_overall_report);
        binding.setActivity(this);
        preferenceHelper = new PreferenceHelper(this);
        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);
        title = getIntent().getExtras().getString(Constants.TITLE);
        locationModel = getIntent().getExtras().getParcelable(Constants.LOCATION);
        processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);
        selectedRoleList = new ArrayList<String>(Arrays.asList(getColumnIdex((roleList).split(";"))));
        if (locationModel == null) {
            locationModel = new LocationModel();
            locationModel.setState(User.getCurrentUser(getApplicationContext()).getMvUser().getState());
            locationModel.setDistrict(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
            locationModel.setTaluka(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
        }
        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(findViewById(R.id.layout_main), context);

        binding.spinnerRole.setText(roleList);
        binding.spinnerRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoleDialog();
            }
        });
        setActionbar("Overall Report");
        mAdapter = new OverallReportAdapter(overAllTaskList, context);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

        if (Utills.isConnected(getApplicationContext()))
            getDashBoardData(roleList);
        totalExpectedCount=0;
        totalSubmitedCount=0;

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

                String  url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/getDashboardExpectedCount1?state=" + locationModel.getState()
                        + "&district=" + locationModel.getDistrict() + "&taluka=" + locationModel.getTaluka()+ "&role=" + role+ "&processId=" + processId;
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {        @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            JSONArray jsonArray=new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            overAllTaskList = Arrays.asList(gson.fromJson(data, OverAllModel[].class));
                                /*for (int i=0;i<jsonArray.length();i++) {
                                    OverAllModel overAllModel=new OverAllModel();
                                    overAllTaskList.add(overAllModel);
                                }*/
                            for(int i=0;i<overAllTaskList.size();i++){
                                totalExpectedCount=totalExpectedCount+overAllTaskList.get(i).getExpectedCount();
                                totalSubmitedCount=totalSubmitedCount+overAllTaskList.get(i).getSubmittedCount();
                            }
                            binding.tvTotalExpectedCount.setText("Total Expected Count:"+totalExpectedCount);
                            binding.tvTotalSubmitedCount.setText("Total Submited Count:"+totalSubmitedCount);
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

                startActivity(openClass);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
        }

    }
    private void showRoleDialog() {


        if (preferenceHelper.getString(Constants.RoleList) != null && !preferenceHelper.getString(Constants.RoleList).isEmpty()) {
            temp = new ArrayList<String>(Arrays.asList(preferenceHelper.getString(Constants.RoleList).split(";")));

        }

        //  final List<Community> temp = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            if(selectedRoleList.contains(temp.get(i)))
            {
                mSelection[i]=true;
            }
        }


      /* if(temp.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()))
        mSelection[temp.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;
*/
// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                .setTitle("Select Role")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;

                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        StringBuffer sb = new StringBuffer();
                        String prefix = "";
                        for (int i = 0; i < items.length; i++) {
                            if (mSelection[i]) {
                                sb.append(prefix);
                                prefix = ";";
                                sb.append(temp.get(i));

                            }
                        }

                        if (Utills.isConnected(getApplicationContext()))
                            getDashBoardData(sb.toString());
                        binding.spinnerRole.setText(sb.toString());
                        roleList=sb.toString();
                        selectedRoleList = new ArrayList<String>(Arrays.asList(getColumnIdex((roleList).split(";"))));
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();


        dialog.show();
    }
    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }
}
