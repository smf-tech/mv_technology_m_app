package com.mv.Fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.mv.Adapter.IndicatorListAdapter;
import com.mv.BR;
import com.mv.Model.DashaBoardListModel;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityNewTemplateBinding;
import com.mv.databinding.FragmentIndicaorBinding;
import com.mv.databinding.FragmentTrainigCalenderBinding;
import com.mv.decorators.EventDecorator;
import com.mv.decorators.HighlightWeekendsDecorator;
import com.mv.decorators.MySelectorDecorator;
import com.mv.decorators.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
    List<DashaBoardListModel> processAllList = new ArrayList<>();
    private IndicatorListAdapter mAdapter;
    private FragmentTrainigCalenderBinding binding;

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_trainig_calender, container, false);
        View view = binding.getRoot();
        //here data must be an instance of the class MarsDataProvider

        binding.setClander(this);
  /*      RelativeLayout mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);*/
        binding.calendarView.setOnDateChangedListener(this);
         binding.calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        Calendar instance = Calendar.getInstance();
         binding.calendarView.setSelectedDate(instance.getTime());

        Calendar instance1 = Calendar.getInstance();
        instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);

        Calendar instance2 = Calendar.getInstance();
        instance2.set(instance2.get(Calendar.YEAR), Calendar.DECEMBER, 31);

         binding.calendarView.state().edit()
                .setMinimumDate(instance1.getTime())
                .setMaximumDate(instance2.getTime())
                .commit();

         binding.calendarView.addDecorators(
                //new MySelectorDecorator(getActivity()),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );

      new ApiSimulator().executeOnExecutor(Executors.newSingleThreadExecutor());
        return view;
    }
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -2);
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
                calendar.add(Calendar.DATE, 5);
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (getActivity().isFinishing()) {
                return;
            }

            binding.calendarView.addDecorator(new EventDecorator(getActivity(), calendarDays));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

    }




/*    private void getAllProcess() {
        Utills.showProgressDialog(getActivity(), "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getProcessDashBoardData";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    processAllList.clear();
                    DashaBoardListModel processList = new DashaBoardListModel();
                    processList.setName("Trainee Feedback");
                    processAllList.add(processList);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        processList = new DashaBoardListModel();
                        JSONObject jsonObject= jsonArray.getJSONObject(i);
                        JSONObject processObj=jsonObject.getJSONObject("process");
                        processList.setId(processObj.getString("Id"));
                        processList.setName(processObj.getString("Name"));
                        JSONArray tasklist=jsonObject.getJSONArray("taskList");
                        for (int j = 0; j < tasklist.length(); j++) {
                            Task task=new Task();
                            task.setId(tasklist.getJSONObject(j).getString("Id"));
                            task.setTask_Text__c(tasklist.getJSONObject(j).getString("Task_Text__c"));
                            task.setTask_type__c(tasklist.getJSONObject(j).getString("Task_type__c"));
                            task.setMV_Process__c(tasklist.getJSONObject(j).getString("MV_Process__c"));
                            processList.getTasksList().add(task);


                        }
                        processAllList.add(processList);
                    }

                    mAdapter.notifyDataSetChanged();
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
    }*/
}
