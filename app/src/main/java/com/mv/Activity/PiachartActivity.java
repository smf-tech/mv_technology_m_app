package com.mv.Activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.PichartDescriptiveListAdapter;
import com.mv.Model.Community;
import com.mv.Model.PiaChartModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentIndicaorBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    RecyclerView rvPiaChartDeatail;
    PichartDescriptiveListAdapter adapter;
    Activity context;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_indicaor);
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        title = getIntent().getExtras().getString(Constants.TITLE);

        initPicahrtView();
        if (task == null) {

            if (Utills.isConnected(this))
                getAllIndicatorTask();
        } else {

            if (Utills.isConnected(this))
                getDashBoardData();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
        img_logout.setOnClickListener(this);
    }

    private void initPicahrtView() {
        //setActionbar(task);
        setActionbar(title);
        preferenceHelper = new PreferenceHelper(PiachartActivity.this);
        mChart = (PieChart) findViewById(R.id.chart1);
        mChart.setVisibility(View.VISIBLE);
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
         l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);

        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        //  mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);

        rvPiaChartDeatail = (RecyclerView) findViewById(R.id.recycler_view);
        rvPiaChartDeatail.setHasFixedSize(true);
        rvPiaChartDeatail.setLayoutManager(new LinearLayoutManager(this));
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
        colors.add(Color.rgb(11,111,206));
        colors.add(Color.rgb(120,201,83));
        colors.add(Color.rgb(226,112,1));
        colors.add(Color.rgb(168,69,220));
        colors.add(Color.rgb(  113, 88, 143));
        colors.add(Color.rgb(   170, 70, 67));
        colors.add(Color.rgb(  65, 152, 175));
        colors.add(Color.rgb(    147, 169, 207));
        colors.add(Color.rgb( 209, 147, 146));
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
                + "/services/apexrest/getDashboardData?userId=" + User.getCurrentUser(PiachartActivity.this).getId() + "&qustionArea=" + title;


        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {


                    mChart.setVisibility(View.VISIBLE);
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
                    } else {
                        binding.swipeRefreshLayout.setVisibility(View.GONE);
                        binding.tvPiaNoDataAvailable.setVisibility(View.VISIBLE);
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


    private void getDashBoardData() {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);

                final JSONObject jsonObject = new JSONObject();

                jsonObject.put("state", "Maharashtra");
                jsonObject.put("district", User.getCurrentUser(getApplicationContext()).getDistrict());
                jsonObject.put("taluka", User.getCurrentUser(getApplicationContext()).getTaluka());
                jsonObject.put("tskId", task.getId());


                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/getchartData", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            JSONObject jsonObject1 = new JSONObject(response.body().string());
                            JSONArray jsonArray = jsonObject1.getJSONArray("Records");
                            if (jsonArray.length() > 0) {
                                entries = new ArrayList<>();
                                piaChartModelArrayList = new ArrayList<>();
                                if (task.getTask_type__c().equals("Selection") || task.getTask_type__c().equals("Checkbox")) {

                                    for (int i = 0; i < jsonArray.length(); i++) {


                                        entries.add(new PieEntry(Float.valueOf(jsonArray.getJSONObject(i).getString("value")), jsonArray.getJSONObject(i).getString("key")));
                                        if (!jsonArray.getJSONObject(i).getString("value").equals("0.0"))
                                            key.add(jsonArray.getJSONObject(i).getString("value"));


                                    }


                                    mChart.setVisibility(View.VISIBLE);
                                    binding.piachartRecyclerView.setVisibility(View.GONE);
                                    if (key.size() > 0) {
                                        setData(entries);
                                        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
                                        binding.tvPiaNoDataAvailable.setVisibility(View.GONE);
                                    } else {
                                        binding.swipeRefreshLayout.setVisibility(View.GONE);
                                        binding.tvPiaNoDataAvailable.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        PiaChartModel piaChartModel = new PiaChartModel();
                                        piaChartModel.setState(jsonArray.getJSONObject(i).getString("state"));
                                        piaChartModel.setDistrict(jsonArray.getJSONObject(i).getString("district"));
                                        piaChartModel.setTaluka(jsonArray.getJSONObject(i).getString("taluka"));
                                        piaChartModel.setName(jsonArray.getJSONObject(i).getString("fName") + " " + jsonArray.getJSONObject(i).getString("lName"));
                                        piaChartModel.setDetail(jsonArray.getJSONObject(i).getString("feedbackdetail"));

                                        piaChartModelArrayList.add(piaChartModel);
                                    }
                                    mChart.setVisibility(View.GONE);
                                    binding.piachartRecyclerView.setVisibility(View.VISIBLE);
                                    adapter = new PichartDescriptiveListAdapter(context, piaChartModelArrayList);
                                    rvPiaChartDeatail.setAdapter(adapter);

                                }
                            } else {

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
}
