package com.mv.Fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.mv.Adapter.IndicatorListAdapter;
import com.mv.Adapter.PichartDescriptiveListAdapter;
import com.mv.Model.CalenderEvent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 05-12-2017.
 */

public class TrainingCalender extends Fragment implements OnDateSelectedListener {
    private PreferenceHelper preferenceHelper;
    List<CalenderEvent> dateList = new ArrayList<>();
    private IndicatorListAdapter mAdapter;
    private FragmentTrainigCalenderBinding binding;
    SimpleDateFormat formatter;
    ArrayList<CalendarDay> dates;
    HashMap<CalendarDay, List<CalenderEvent>> eventMap = new HashMap<>();

    PichartDescriptiveListAdapter adapter;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_trainig_calender, container, false);
        View view = binding.getRoot();
        preferenceHelper = new PreferenceHelper(getActivity());

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                //new MySelectorDecorator(getActivity()),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );

        getAllProcess();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if( eventMap.get(date)!=null) {
            adapter = new PichartDescriptiveListAdapter(getActivity(), eventMap.get(date));
            binding.recyclerView.setAdapter(adapter);
        }
        else
        {
            adapter = new PichartDescriptiveListAdapter(getActivity(), new ArrayList<CalenderEvent>());
            binding.recyclerView.setAdapter(adapter);
        }


    }


    private void getAllProcess() {
        Utills.showProgressDialog(getActivity(), "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getCalenderData?userId=" + User.getCurrentUser(getActivity()).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if(response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());

                        eventMap = new HashMap<>();
                        dates = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            CalenderEvent calenderEvent = new CalenderEvent();
                            calenderEvent.setId(jsonArray.getJSONObject(i).getString("Id"));
                            calenderEvent.setDate(jsonArray.getJSONObject(i).getString("Date__c"));
                            calenderEvent.setDescription(jsonArray.getJSONObject(i).getString("Description__c"));
                            calenderEvent.setMV_User1__c(jsonArray.getJSONObject(i).getString("MV_User1__c"));
                            CalendarDay day = CalendarDay.from(formatter.parse(jsonArray.getJSONObject(i).getString("Date__c")));
                            dateList = new ArrayList<>();
                            if (eventMap.get(jsonArray.getJSONObject(i).getString("Date__c")) != null)
                                dateList = eventMap.get(jsonArray.getJSONObject(i).getString("Date__c"));
                            dateList.add(calenderEvent);
                            eventMap.put(day, dateList);
                            dates.add(day);

                        }
                        binding.calendarView.addDecorator(new EventDecorator(Color.RED, dates));
                        Calendar instance = Calendar.getInstance();

                        if (eventMap.get(instance.getTime()) != null) {
                            adapter = new PichartDescriptiveListAdapter(getActivity(), eventMap.get(instance.getTime()));
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
}
