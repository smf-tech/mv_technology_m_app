package com.mv.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.PichartDescriptiveListAdapter;
import com.mv.Adapter.PichartMenuAdapter;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.LocationModel;
import com.mv.Model.PiaChartModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentIndicaorBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PiachartActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, OnChartValueSelectedListener {
    private FragmentIndicaorBinding binding;
    private PreferenceHelper preferenceHelper;
    private PieChart mChart;
    ArrayList<String> key = new ArrayList<>();
    ArrayList<PiaChartModel> piaChartModelArrayList = new ArrayList<>();
    ArrayList<PiaChartModel> repplicaCahart = new ArrayList<>();
    ArrayList<PieEntry> entries;
    Task task;
    File file;
    List<String> temp;
    ImageView imageView;
    Bitmap mbitmap;
    String roleList;
    private ImageView img_back, img_list, img_logout, location;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    RecyclerView rvPiaChartDeatail;
    PichartDescriptiveListAdapter adapter;
    PichartMenuAdapter menuAdapter;
    EditText role;
    LinearLayout llSpinner;
    LocationModel locationModel;
    Activity context;
    String title;
    private String img_str;
    public static String selectedRole;
    ArrayList<String> selectedRoleList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_indicaor);
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);
        selectedRoleList = new ArrayList<String>(Arrays.asList(getColumnIdex((roleList).split(";"))));


        title = getIntent().getExtras().getString(Constants.TITLE);
        locationModel = getIntent().getExtras().getParcelable(Constants.LOCATION);
        if (locationModel == null) {
            locationModel = new LocationModel();
            locationModel.setState(User.getCurrentUser(getApplicationContext()).getMvUser().getState());
            locationModel.setDistrict(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
            locationModel.setTaluka(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
        }
        initPicahrtView();
        if (task == null) {
            if (Utills.isConnected(this))
                getAllIndicatorTask();
            llSpinner.setVisibility(View.GONE);
        } else {
            if (Utills.isConnected(this))
                getDashBoardDataForAll(role.getText().toString());
            llSpinner.setVisibility(View.VISIBLE);
        }
    }
    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }


    private void showCommunityDialog() {
        final List<Community> temp = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
        final String[] items = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i).getName();
        }
        final boolean[] mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(PiachartActivity.this)
                .setTitle("Select Communities")
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
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Content> contentsList = new ArrayList<>();
                        for (int i = 0; i < items.length; i++) {
                            if (mSelection[i]) {
                                Content content = new Content();
                                content.setDescription("");
                                content.setTitle(title);
                                content.setDistrict(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
                                content.setTaluka(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
                                content.setReporting_type("Information Sharing");
                                content.setUser_id(User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                                content.setCommunity_id(temp.get(i).getId());
                                content.setTemplate(preferenceHelper.getString(PreferenceHelper.TEMPLATEID));
                                contentsList.add(content);

                                Log.i("value", "value");
                            }

                        }
                        setdDataToSalesForcce(contentsList);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
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
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(PiachartActivity.this)
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
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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
                            getDashBoardDataForAll(sb.toString());
                        role.setText(sb.toString());
                        roleList=sb.toString();
                        selectedRoleList = new ArrayList<String>(Arrays.asList(getColumnIdex((roleList).split(";"))));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();


        dialog.show();
    }

/*
    private void showrRoleDialog() {
        final List<String> temp = selectedRoleList;
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            if(selectedRole.contains(temp.get(i)))
                mSelection[i] =true;
            else
                mSelection[i] =false;
        }

        if (mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()) > 0)
            mSelection[mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(PiachartActivity.this)
                .setTitle("Select ")
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
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        StringBuffer sb = new StringBuffer();
                        String prefix = "";
                        for (int i = 0; i < items.length; i++) {
                            if (mSelection[i]) {
                                sb.append(prefix);
                                prefix = ",";
                                sb.append(temp.get(i));
                                //now original string is changed
                            }
                        }
                        selectedRolename = sb.toString();
                        binding.spinnerRole.setText(selectedRolename);
                        Log.e("StringValue", selectedRolename);

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }
*/

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.VISIBLE);
        img_logout.setOnClickListener(this);

        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.VISIBLE);
        img_list.setOnClickListener(this);
        img_list.setImageResource(R.drawable.filter);

        img_logout.setImageResource(R.drawable.share_report);
        llSpinner = (LinearLayout) findViewById(R.id.llrole_lay);

        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenShot(v);
            }
        });

    }

    private void initPicahrtView() {
        //setActionbar(task);
        setActionbar(title);
        role = (EditText) findViewById(R.id.spinner_role);
        role.setText(roleList);
        role.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoleDialog();
            }
        });


    /*    if (roleList != null && !roleList.isEmpty()) {
            ArrayList<String> myList = new ArrayList<String>(Arrays.asList(roleList.split(";")));
            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, myList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            role.setAdapter(arrayAdapter);
            if(myList.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()))
            role.setSelection(myList.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()));
        }*/
        preferenceHelper = new PreferenceHelper(PiachartActivity.this);
        mChart = (PieChart) findViewById(R.id.chart1);
        binding.piachartChartView.setVisibility(View.VISIBLE);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        //mChart.setCenterTextTypeface(mTfLight);
        mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        binding.editTextEmail.addTextChangedListener(watch);
        Legend l = mChart.getLegend();
        l.setEnabled(false);
       /*  l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);

        l.setYOffset(0f);*/

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        //  mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);

        rvPiaChartDeatail = (RecyclerView) findViewById(R.id.recycler_view);
        rvPiaChartDeatail.setHasFixedSize(true);
        rvPiaChartDeatail.setLayoutManager(new LinearLayoutManager(this));

        binding.rvMenu.setHasFixedSize(true);
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString(getString(R.string.app_name));
/*        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);*/
        return s;
    }

    TextWatcher watch = new TextWatcher() {

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
        List<PiaChartModel> list = new ArrayList<>();
        repplicaCahart.clear();
        for (int i = 0; i < piaChartModelArrayList.size(); i++) {
            repplicaCahart.add(piaChartModelArrayList.get(i));
        }
        for (int i = 0; i < repplicaCahart.size(); i++) {
            if (repplicaCahart.get(i).getDetail().toLowerCase().contains(s.toLowerCase())) {
                list.add(repplicaCahart.get(i));
            }
        }
        repplicaCahart.clear();
        for (int i = 0; i < list.size(); i++) {
            repplicaCahart.add(list.get(i));
        }
        adapter = new PichartDescriptiveListAdapter(context, repplicaCahart);
        rvPiaChartDeatail.setAdapter(adapter);
    }


    private void setData(ArrayList<PieEntry> entries) {


        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
       /* for (int i = 0; i < count ; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5),
                    mParties[i % mParties.length],
                    getResources().getDrawable(R.drawable.star)));
        }
*/
        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.app_name));

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(11, 111, 206));
        colors.add(Color.rgb(120, 201, 83));
        colors.add(Color.rgb(226, 112, 1));
        colors.add(Color.rgb(168, 69, 220));
        colors.add(Color.rgb(113, 88, 143));
        colors.add(Color.rgb(170, 70, 67));
        colors.add(Color.rgb(65, 152, 175));
        colors.add(Color.rgb(147, 169, 207));
        colors.add(Color.rgb(209, 147, 146));
        colors.add(Color.rgb(185, 205, 150));



        /*for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());*/

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        //  data.setValueTypeface(mTfLight);
        menuAdapter = new PichartMenuAdapter(entries, colors, context);
        binding.rvMenu.setAdapter(menuAdapter);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();


    }

    @Override
    public void onRefresh() {
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.img_list:
                Intent openClass = new Intent(PiachartActivity.this, IndicatorLocationSelectionActivity.class);
                openClass.putExtra(Constants.TITLE, title);
                openClass.putExtra(Constants.INDICATOR_TASK, task);
                openClass.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                openClass.putExtra(Constants.PROCESS_ID, "");
                startActivity(openClass);

                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

    }


    private void getAllIndicatorTask() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getDashboardDatademo?userId=" + User.getCurrentUser(PiachartActivity.this).getMvUser().getId() + "&qustionArea=" + title;


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {


                    binding.piachartChartView.setVisibility(View.VISIBLE);
                    binding.piachartRecyclerView.setVisibility(View.GONE);
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    entries = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        entries.add(new PieEntry(Float.valueOf(jsonArray.getJSONObject(i).getString("value")), jsonArray.getJSONObject(i).getString("key")));
                        if (!jsonArray.getJSONObject(i).getString("value").equals("0.0"))
                            key.add(jsonArray.getJSONObject(i).getString("value"));
                    }


                    if (key.size() > 0) {
                        setData(entries);
                        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
                        binding.tvPiaNoDataAvailable.setVisibility(View.GONE);
                        img_logout.setVisibility(View.VISIBLE);
                        img_list.setVisibility(View.VISIBLE);
                    } else {
                        binding.swipeRefreshLayout.setVisibility(View.GONE);
                        binding.tvPiaNoDataAvailable.setVisibility(View.VISIBLE);
                        img_logout.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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


    private void getDashBoardDataForAll(String role) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);

                final JSONObject jsonObject = new JSONObject();

                jsonObject.put("state", locationModel.getState());
                jsonObject.put("district", locationModel.getDistrict());
                jsonObject.put("taluka", locationModel.getTaluka());
                jsonObject.put("tskId", task.getId());
                jsonObject.put("role", role);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/getchartDatademoNew", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                            JSONObject jsonObject1 = new JSONObject(response.body().string());
                            JSONObject recorObject = jsonObject1.getJSONObject("Records");
                            JSONArray jsonArray = recorObject.getJSONArray("outputdata");
                            JSONObject dataObject = recorObject.getJSONObject("data");
                            binding.processName.setText(dataObject.getString("processName"));
                            if (dataObject.getString("caption").equals("null"))
                                binding.captionName.setText("Caption : N/A");
                            else
                                binding.captionName.setText("Caption : " + dataObject.getString("caption"));
                            binding.countOfDistrict.setText("Count of District : " + dataObject.getString("countOfDistrict"));
                            binding.countOfTaluka.setText("Count of Taluka : " + dataObject.getString("countOfDistrict"));
                            binding.countOfCluster.setText("Count of Cluster : " + dataObject.getString("countOfDistrict"));
                            binding.validFeedback.setText("Valid Feedback Count : " + dataObject.getString("validafeedbackCount"));
                            binding.totalFeedback.setText("Total Feedback Count : " + dataObject.getString("feedbackCount"));

                            if (jsonArray.length() > 0) {
                                entries = new ArrayList<>();
                                piaChartModelArrayList = new ArrayList<>();
                                if (task.getTask_type__c().equals("Selection") || task.getTask_type__c().equals("Checkbox")) {

                                    for (int i = 0; i < jsonArray.length(); i++) {


                                        entries.add(new PieEntry(Float.valueOf(jsonArray.getJSONObject(i).getString("value")), jsonArray.getJSONObject(i).getString("key")));
                                        if (!jsonArray.getJSONObject(i).getString("value").equals("0.0"))
                                            key.add(jsonArray.getJSONObject(i).getString("value"));


                                    }

                                    binding.piachartChartView.setVisibility(View.VISIBLE);
                                    binding.piachartRecyclerView.setVisibility(View.GONE);
                                    if (key.size() > 0) {
                                        setData(entries);

                                        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
                                        binding.tvPiaNoDataAvailable.setVisibility(View.GONE);
                                        img_logout.setVisibility(View.VISIBLE);
                                        img_list.setVisibility(View.VISIBLE);
                                    } else {
                                        img_logout.setVisibility(View.GONE);

                                        binding.swipeRefreshLayout.setVisibility(View.GONE);
                                        binding.tvPiaNoDataAvailable.setVisibility(View.VISIBLE);

                                    }
                                } else {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        PiaChartModel piaChartModel = new PiaChartModel();
                                        piaChartModel.setState(jsonArray.getJSONObject(i).getString("state"));
                                        piaChartModel.setDistrict(jsonArray.getJSONObject(i).getString("district"));
                                        piaChartModel.setTaluka(jsonArray.getJSONObject(i).getString("taluka"));
                                        piaChartModel.setName(jsonArray.getJSONObject(i).getString("Name"));
                                        piaChartModel.setDetail(jsonArray.getJSONObject(i).getString("feedbackdetail"));
                                        if (!jsonArray.getJSONObject(i).getString("feedbackdetail").equals("null"))
                                            piaChartModelArrayList.add(piaChartModel);
                                    }
                                    mChart.setVisibility(View.GONE);
                                    binding.tvPiaNoDataAvailable.setVisibility(View.GONE);
                                    binding.piachartRecyclerView.setVisibility(View.VISIBLE);
                                    adapter = new PichartDescriptiveListAdapter(context, piaChartModelArrayList);
                                    rvPiaChartDeatail.setAdapter(adapter);

                                }
                            } else {
                                img_logout.setVisibility(View.GONE);

                                binding.swipeRefreshLayout.setVisibility(View.GONE);
                                binding.tvPiaNoDataAvailable.setVisibility(View.VISIBLE);
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

    public void screenShot(View view) {
        mbitmap = getBitmapOFRootView(view);

        createImage(mbitmap);
    }

    public Bitmap getBitmapOFRootView(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = rootview.getDrawingCache();
        return bitmap1;
    }

    public void createImage(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        file = new File(Environment.getExternalStorageDirectory() +
                "/capturedscreenandroid.jpg");
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes.toByteArray());
            outputStream.close();


        /*    Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("image*//**//**//**//*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Title : " + title );

            startActivity(Intent.createChooser(shareIntent, "Share Report"));*/
            showCommunityDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setdDataToSalesForcce(ArrayList<Content> content) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < content.size(); i++) {
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    String json = gson.toJson(content.get(i));

                    JSONObject jsonObject1 = new JSONObject(json);

                    JSONArray jsonArrayAttchment = new JSONArray();
                    if (Uri.fromFile(file) != null) {

                        try {
                            jsonObject1.put("isAttachmentPresent", "true");
                            jsonObject1.put("isAttachmentPresent", "true");
                            InputStream iStream = null;
                            iStream = getContentResolver().openInputStream(Uri.fromFile(file));
                            img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                      /*  JSONObject jsonObjectAttachment = new JSONObject();
                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", content.getTitle());
                        jsonObjectAttachment.put("ContentType", "image/png");
                        jsonArrayAttchment.put(jsonObjectAttachment);*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                /*JSONObject jsonObjectAttachment = new JSONObject();
                jsonArrayAttchment.put(jsonObjectAttachment);*/
                    jsonObject1.put("attachments", jsonArrayAttchment);

                    jsonArray.put(jsonObject1);
                }
                jsonObject.put("listVisitsData", jsonArray);
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/insertContent", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                            String str = response.body().string();
                            JSONObject object = new JSONObject(str);
                            JSONArray array = object.getJSONArray("Records");
                            if (array.length() > 0) {
                                JSONArray array1 = new JSONArray();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object1 = array.getJSONObject(0);
                                    if (object1.has("Id") && Uri.fromFile(file) != null) {
                                        JSONObject object2 = new JSONObject();
                                        object2.put("id", object1.getString("Id"));
                                        object2.put("img", img_str);

                                        array1.put(object2);
                                        sendImageToServer(array1);
                                   /* Utills.showToast("Report submitted successfully...", getApplicationContext());
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);*/
                                    }
                                }
                            } else {
                                Utills.showToast("Report submitted successfully...", getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
        }
    }

    private void sendImageToServer(JSONArray jsonArray) {
        Utills.showProgressDialog(this);
        JsonParser jsonParser = new JsonParser();
        JsonArray gsonObject = (JsonArray) jsonParser.parse(jsonArray.toString());
        ServiceRequest apiService =
                ApiClient.getImageClient().create(ServiceRequest.class);
        apiService.sendImageToSalesforce("http://mobileapp.mulyavardhan.org/upload.php", gsonObject).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String str = response.body().string();
                    JSONObject object = new JSONObject(str);
                    if (object.has("status")) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            Utills.showToast("Report submitted successfully...", getApplicationContext());
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
    }
}
