package com.mv.ActivityMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Activity.CalenderFliterActivity;
import com.mv.Adapter.HorizontalCalenderAdapter;
import com.mv.Adapter.TraingCalenderAadapter;
import com.mv.Model.CalenderEvent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentTrainigCalenderBinding;
import com.mv.decorators.EventDecorator;
import com.mv.decorators.HighlightWeekendsDecorator;
import com.mv.decorators.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 05-12-2017.
 */

public class TrainingCalender extends AppCompatActivity implements OnDateSelectedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private PreferenceHelper preferenceHelper;
    List<CalenderEvent> dateList = new ArrayList<>();

    public FragmentTrainigCalenderBinding binding;
    SimpleDateFormat formatter;
    SimpleDateFormat formatterNew;
    ArrayList<CalendarDay> dates;
    List<Date> allDate = new ArrayList<>();
    HashMap<CalendarDay, List<CalenderEvent>> eventMap = new HashMap<>();
    List<String> MonthList = new ArrayList<>();
    List<String> YearList = new ArrayList<>();
    TraingCalenderAadapter adapter;
    List<Date> eventDate = new ArrayList<>();
    HorizontalCalenderAdapter horizontalCalenderAdapter;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Activity context;
    private ArrayAdapter<String> district_adapter, taluka_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_trainig_calender);
        binding.setClander(this);
        setActionbar(getString(R.string.training_calendar));

        Calendar.getInstance().get(Calendar.MONTH);
        MonthList = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        YearList = Arrays.asList("2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032");

        setSpinnerAdapter(MonthList, district_adapter, binding.spinnerMonth, MonthList.get(Calendar.getInstance().get(Calendar.MONTH)));
        setSpinnerAdapter(YearList, district_adapter, binding.spinnerYear, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        //setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerYear, selectedDisrict);

        binding.fabAddBroadcast.setOnClickListener(this);
        binding.spinnerMonth.setOnItemSelectedListener(this);
        binding.spinnerYear.setOnItemSelectedListener(this);

        preferenceHelper = new PreferenceHelper(context);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //here data must be an instance of the class MarsDataProvider
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        binding.setClander(this);
  /*      RelativeLayout mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);*/
        binding.calendarView.setOnDateChangedListener(this);
        binding.calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        Calendar instance = Calendar.getInstance();
        binding.calendarView.setSelectedDate(instance.getTime());

        Calendar instance1 = Calendar.getInstance();
        instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);


        binding.calendarView.state().edit()
                .setMinimumDate(instance1.getTime())
                .commit();

        binding.calendarView.addDecorators(
                //new MySelectorDecorator(context),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );
        //rohit code for hide recyclerView
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if (dy < -5 && binding.fabAddBroadcast.getVisibility() != View.VISIBLE) {
                    binding.fabAddBroadcast.show();
                } else if (dy > 5 && binding.fabAddBroadcast.getVisibility() == View.VISIBLE) {
                    binding.fabAddBroadcast.hide();
                }
            }
        });
        formatterNew = new SimpleDateFormat("yyyy-MM-dd");

        //    allDate=  getDates(formatterNew.format(Calendar.getInstance().getTime()), "2018-06-08");
        //   horizontalCalenderAdapter=new HorizontalCalenderAdapter(context, allDate );

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewHorizontal.setLayoutManager(horizontalLayoutManager);
        //    binding.recyclerViewHorizontal.setAdapter(horizontalCalenderAdapter);
        try {
            binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))));


        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllProcess();
    }

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)

            spinner.setSelection(itemList.indexOf(selectedValue));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_month:
                allDate.clear();
                try {
                    allDate.addAll(getDates(binding.spinnerYear.getSelectedItem().toString() + "-0" + (i + 1) + "-01", binding.spinnerYear.getSelectedItem().toString() + "-0" + (i + 2) + "-01"));
                    horizontalCalenderAdapter = new HorizontalCalenderAdapter(context, allDate, allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))), eventDate);
                    binding.recyclerViewHorizontal.setAdapter(horizontalCalenderAdapter);
                    //   binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(Calendar.getInstance().getTime()));

                    binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))));
                    if (allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))) > 0)
                        selectDate(Calendar.getInstance().getTime());
                    else
                        selectDate(new SimpleDateFormat("yyyy-MM-dd").parse(binding.spinnerYear.getSelectedItem().toString() + "-0" + (i + 1) + "-01"));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.spinner_year:
                allDate.clear();
                try {
                    allDate.addAll(getDates(binding.spinnerYear.getSelectedItem().toString() + "-0" + (MonthList.indexOf(binding.spinnerMonth.getSelectedItem().toString()) + 1) + "-01", binding.spinnerYear.getSelectedItem().toString() + "-0" + (MonthList.indexOf(binding.spinnerMonth.getSelectedItem().toString()) + 2) + "-01"));
                    horizontalCalenderAdapter = new HorizontalCalenderAdapter(context, allDate, allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))), eventDate);
                    binding.recyclerViewHorizontal.setAdapter(horizontalCalenderAdapter);
                    //   binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(Calendar.getInstance().getTime()));

                    binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))));
                    if (allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))) > 0)
                        selectDate(Calendar.getInstance().getTime());
                    else
                        selectDate(new SimpleDateFormat("yyyy-MM-dd").parse(binding.spinnerYear.getSelectedItem().toString() + "-0" + (MonthList.indexOf(binding.spinnerMonth.getSelectedItem().toString()) + 1) + "-01"));

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void selectDate(Date date) {
        CalendarDay day = CalendarDay.from(date);
        if (eventMap.get(day) != null) {
            LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(cal.getTime());
            LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
            adapter = new TraingCalenderAadapter(context, eventMap.get(day));
            binding.recyclerView.setAdapter(adapter);
        } else {
            adapter = new TraingCalenderAadapter(context, new ArrayList<CalenderEvent>());
            binding.recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        binding.fabAddBroadcast.show();
        if (eventMap.get(date) != null) {
            adapter = new TraingCalenderAadapter(context, eventMap.get(date));
            binding.recyclerView.setAdapter(adapter);
        } else {
            adapter = new TraingCalenderAadapter(context, new ArrayList<CalenderEvent>());
            binding.recyclerView.setAdapter(adapter);
        }


    }

    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);
        layoutList.setVisibility(View.GONE);
        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.fab_add_broadcast:
                Intent openClass = new Intent(TrainingCalender.this, CalenderFliterActivity.class);
                startActivity(openClass);
                break;
        }
    }

    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getcalenderEventRecords?userId=" + User.getCurrentUser(context).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        eventDate = new ArrayList<>();

                        dates = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            dateList = new ArrayList<>();
                            CalenderEvent calenderEvent = new CalenderEvent();
                            if (jsonArray.getJSONObject(i).has("Id"))
                                calenderEvent.setId(jsonArray.getJSONObject(i).getString("Id"));
                            if (jsonArray.getJSONObject(i).has("Date__c"))
                                calenderEvent.setDate(jsonArray.getJSONObject(i).getString("Date__c"));
                            if (jsonArray.getJSONObject(i).has("Description_New__c"))
                                calenderEvent.setDescription(jsonArray.getJSONObject(i).getString("Description_New__c"));
                            if (jsonArray.getJSONObject(i).has("Title__c"))
                                calenderEvent.setTitle(jsonArray.getJSONObject(i).getString("Title__c"));
                            if (jsonArray.getJSONObject(i).has("MV_User__c"))
                                calenderEvent.setMV_User1__c(jsonArray.getJSONObject(i).getString("MV_User__c"));
                            if (jsonArray.getJSONObject(i).has("State__c"))
                                calenderEvent.setState__c(jsonArray.getJSONObject(i).getString("State__c"));
                            if (jsonArray.getJSONObject(i).has("Event_Time__c"))
                                calenderEvent.setEvent_Time__c(jsonArray.getJSONObject(i).getString("Event_Time__c"));
                            if (jsonArray.getJSONObject(i).has("District__c"))
                                calenderEvent.setDistrict__c(jsonArray.getJSONObject(i).getString("District__c"));
                            if (jsonArray.getJSONObject(i).has("Taluka__c"))
                                calenderEvent.setTaluka__c(jsonArray.getJSONObject(i).getString("Taluka__c"));
                            if (jsonArray.getJSONObject(i).has("Cluster__c"))
                                calenderEvent.setCluster__c(jsonArray.getJSONObject(i).getString("Cluster__c"));
                            if (jsonArray.getJSONObject(i).has("Assigned_By_Name__c"))
                                calenderEvent.setCreatedUserData(jsonArray.getJSONObject(i).getString("Assigned_By_Name__c"));
                            if (jsonArray.getJSONObject(i).has("Total_Form__c"))
                                calenderEvent.setProceesTotalCount(jsonArray.getJSONObject(i).getString("Total_Form__c"));
                            if (jsonArray.getJSONObject(i).has("Filled_Form__c"))
                                calenderEvent.setProceesSubmittedCount(jsonArray.getJSONObject(i).getString("Filled_Form__c"));
                            if (jsonArray.getJSONObject(i).has("Village__c"))
                                calenderEvent.setVillage__c(jsonArray.getJSONObject(i).getString("Village__c"));
                            if (jsonArray.getJSONObject(i).has("Status__c"))
                                calenderEvent.setStatus(jsonArray.getJSONObject(i).getString("Status__c"));
                            if (jsonArray.getJSONObject(i).has("School__c"))
                                calenderEvent.setSchool__c(jsonArray.getJSONObject(i).getString("School__c"));
                            if (jsonArray.getJSONObject(i).has("Is_Event_for_All_Role__c"))
                                calenderEvent.setIs_Event_for_All_Role__c(jsonArray.getJSONObject(i).getString("Is_Event_for_All_Role__c"));
                            if (jsonArray.getJSONObject(i).has("Role__c"))
                                calenderEvent.setRole__c(jsonArray.getJSONObject(i).getString("Role__c"));
                            if (jsonArray.getJSONObject(i).has("Assigned_User_Ids__c"))
                                calenderEvent.setAssigned_User_Ids__c(jsonArray.getJSONObject(i).getString("Assigned_User_Ids__c"));
                            if (jsonArray.getJSONObject(i).has("Assign_id_name__c"))
                                calenderEvent.setAssign_id_name__c(jsonArray.getJSONObject(i).getString("Assign_id_name__c"));
                            if (jsonArray.getJSONObject(i).has("organization__c"))
                                calenderEvent.setOrganization__c(jsonArray.getJSONObject(i).getString("organization__c"));
                            if (jsonArray.getJSONObject(i).has("MV_Process__c"))
                                calenderEvent.setMV_Process__c(jsonArray.getJSONObject(i).getString("MV_Process__c"));
                            if (jsonArray.getJSONObject(i).has("Event_End_Time__c"))
                                calenderEvent.setEvent_End_Time__c(jsonArray.getJSONObject(i).getString("Event_End_Time__c"));
                            if (jsonArray.getJSONObject(i).has("End_Date__c"))
                                calenderEvent.setEnd_Date__c(jsonArray.getJSONObject(i).getString("End_Date__c"));
                            if (jsonArray.getJSONObject(i).has("End_Date__c") && jsonArray.getJSONObject(i).has("Date__c")
                                    && jsonArray.getJSONObject(i).getString("End_Date__c") != null
                                    && jsonArray.getJSONObject(i).getString("End_Date__c").length() > 0
                                    && jsonArray.getJSONObject(i).getString("Date__c") != null
                                    && jsonArray.getJSONObject(i).getString("Date__c").length() > 0) {
                                List<Date> Dates = getDaysBetweenDates(formatter.parse(jsonArray.getJSONObject(i).getString("Date__c")),
                                        formatter.parse(jsonArray.getJSONObject(i).getString("End_Date__c")));
                                for (Date date : Dates) {
                                    dateList = new ArrayList<>();
                                    CalendarDay day = CalendarDay.from(date);
                                    Log.i("Current Date", formatter.format(date));
                                    if (eventMap.get(day) != null)
                                        dateList = eventMap.get(day);
                                    eventDate.add(date);
                                    dateList.add(calenderEvent);
                                    eventMap.put(day, dateList);
                                    dates.add(day);
                                }

                            } else {
                                CalendarDay day = CalendarDay.from(formatter.parse(jsonArray.getJSONObject(i).getString("Date__c")));
                                if (eventMap.get(day) != null)
                                    dateList = eventMap.get(day);
                                eventDate.add(formatterNew.parse(jsonArray.getJSONObject(i).getString("Date__c")));
                                dateList.add(calenderEvent);
                                eventMap.put(day, dateList);
                                dates.add(day);
                            }
                        }
                        AppDatabase.getAppDatabase(context).userDao().deleteCalender();
                        AppDatabase.getAppDatabase(context).userDao().insertCalendr(dateList);
                        binding.calendarView.addDecorator(new EventDecorator(TrainingCalender.this, dates, null));
                        horizontalCalenderAdapter = new HorizontalCalenderAdapter(context, allDate, allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))), eventDate);
                        binding.recyclerViewHorizontal.setAdapter(horizontalCalenderAdapter);
                        binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))));
                        selectDate(Calendar.getInstance().getTime());

                        Calendar instance = Calendar.getInstance();
                        if (eventMap.get(CalendarDay.from(instance)) != null) {
                            adapter = new TraingCalenderAadapter(context, eventMap.get(CalendarDay.from(instance)));
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    public List<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startdate);
        while (calendar.getTime().before(enddate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        dates.add(enddate);
        return dates;
    }

    private String getCurrentDate() {
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        return formattedDate;
    }

    public void removeEvent(String date) {

        if (AppDatabase.getAppDatabase(getApplicationContext()).userDao().getCalenderList(date).size() == 0) {
            try {
                binding.calendarView.removeDecorators();
                CalendarDay day = CalendarDay.from(formatter.parse(date));
                dates.remove(day);
                binding.calendarView.addDecorator(new EventDecorator(TrainingCalender.this, dates, null));

                horizontalCalenderAdapter = new HorizontalCalenderAdapter(context, allDate, allDate.indexOf(formatter.parse(formatterNew.format(Calendar.getInstance().getTime()))), eventDate);
                binding.recyclerViewHorizontal.setAdapter(horizontalCalenderAdapter);
                binding.recyclerViewHorizontal.getLayoutManager().scrollToPosition(allDate.indexOf(formatter.parse(date)));
                selectDate(Calendar.getInstance().getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

    }

    private List<Date> getDates(String dateString1, String dateString2) {
        LocaleManager.setNewLocale(this, Constants.LANGUAGE_ENGLISH);
        Log.d("Start Date", dateString1);
        Log.d("End Date", dateString2);
        ArrayList<Date> dates = new ArrayList<Date>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        LocaleManager.setNewLocale(this, preferenceHelper.getString(Constants.LANGUAGE));
        return dates;
    }

}
