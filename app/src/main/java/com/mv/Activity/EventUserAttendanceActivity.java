package com.mv.Activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.EventAttendanceListAdapter;
import com.mv.Model.EventUser;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityEventUserAttendanceBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventUserAttendanceActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityEventUserAttendanceBinding binding;
    private Activity context;
    private ArrayList<EventUser> eventUsers = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
  //  private ArrayList<EventUser> calenderEventUserArrayList = new ArrayList<>();
    private EventAttendanceListAdapter mAdapter;
    private ArrayList<EventUser> selectedUser = new ArrayList<>();
    private ArrayList<EventUser> eventUsersFliter = new ArrayList<>();
    String eventID,PresentUsersID;
    public boolean isAllSelect=true;

    public String selectedState = "Maharashtra", selectedDisrict = "Select", selectedRolename = "TC", selectedTaluka = "Select", selectedCluster = "Select", selectedVillage = "Select", selectedSchool = "Select", selectedOrganization = "SMF", selectedUserId = "", selectedUserName = "", selectedCatagory = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        eventID=getIntent().getStringExtra("EventID");
        PresentUsersID=getIntent().getStringExtra("presentUser");
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_user_attendance);
        binding.setActivity(this);
        binding.btnSubmitt.setOnClickListener(this);

        binding.cbEventSelectAll.setOnClickListener(this);

        preferenceHelper = new PreferenceHelper(this);
        ImageView logOut=(ImageView) findViewById(R.id.img_logout);
        logOut.setVisibility(View.GONE);
        TextView title=(TextView) findViewById(R.id.toolbar_title);
        title.setText("Attendance");
        ImageView back=(ImageView) findViewById(R.id.img_back);

//        if(isAllSelect=true)
//            binding.cbEventSelectAll.setChecked(true);
//        else
//            binding.cbEventSelectAll.setChecked(false);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getAllFilterUserr();
        binding.editTextEmail.addTextChangedListener(watch);
    }

    public void saveDataToList(EventUser eventUser, boolean isSelected) {
       /* for (EventUser eventUser : eventUsers) {
            if(eventUserr.getUserID().equals(eventUser.getUserID()))
                eventUserr.setUserSelected(true);
        }*/
        if (isSelected) {
            if(selectedUser.size()>0){
                boolean present=false;
                for (int i = 0; i < selectedUser.size(); i++) {
                    if (selectedUser.get(i).getUserID().equalsIgnoreCase(eventUser.getUserID())) {
                        present=true;
                        break;
                    }
                }
                if(!present){
                    selectedUser.add(eventUser);
                }
            }else {
                selectedUser.add(eventUser);
            }
        } else {
            for (int i = 0; i < selectedUser.size(); i++) {
                if (selectedUser.get(i).getUserID().equalsIgnoreCase(eventUser.getUserID())) {
                    selectedUser.remove(i);
                    break;
                }
            }
        }
        Log.e("Selected User",selectedUser.toString());
        eventUsers.set(eventUsers.indexOf(eventUser), eventUser);
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
        List<EventUser> list = new ArrayList<>();

        eventUsersFliter.clear();
        eventUsersFliter.addAll(eventUsers);
        list.clear();
        for (int i = 0; i < eventUsersFliter.size(); i++) {
            if (eventUsersFliter.get(i).getUserName().toLowerCase().contains(s.toLowerCase())) {
                list.add(eventUsersFliter.get(i));
            }
        }

        mAdapter = new EventAttendanceListAdapter(list, EventUserAttendanceActivity.this);
        binding.recyclerView.setAdapter(mAdapter);

        checkAllSelected((ArrayList<EventUser>) list);

    }


    private void getAllFilterUserr() {
        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String s = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetEventCalenderMembers_Url;

        StringBuilder buffer = new StringBuilder();
        buffer.append(s);

        s = "?eventId=" + eventID;
        buffer.append(s);

        Log.e("Url",buffer.toString());

        apiService.getSalesForceData(buffer.toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            eventUsers.clear();
                         //   calenderEventUserArrayList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                EventUser eventUser = new EventUser();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                eventUser.setRole(jsonObject.getString("role"));
                                eventUser.setUserID(jsonObject.getString("Id"));
                                eventUser.setUserName(jsonObject.getString("userName"));
                                eventUser.setUserSelected(false);
                                eventUsers.add(eventUser);
                              //  calenderEventUserArrayList = new ArrayList<>();
                            }

                            for (int i=0;i<eventUsers.size();i++) {
                                if(PresentUsersID!=null && PresentUsersID.contains(eventUsers.get(i).getUserID())){
                                    eventUsers.get(i).setUserSelected(true);
                                }
                            }

                            //sorting as per the present users
                            Collections.sort(eventUsers);

                            checkAllSelected(eventUsers);

                            mAdapter = new EventAttendanceListAdapter(eventUsers, EventUserAttendanceActivity.this);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            binding.recyclerView.setLayoutManager(mLayoutManager);
                            binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
                            binding.recyclerView.setAdapter(mAdapter);


                        }
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

    public void checkAllSelected(ArrayList<EventUser> list){
        boolean allCheck=true;
        for(int i=0;i<list.size();i++){
            if(!list.get(i).getUserSelected()){
                allCheck=false;
                break;
            }
        }
        if(allCheck)
            binding.cbEventSelectAll.setChecked(true);
    }

    public void checkAllDeSelected(){
            binding.cbEventSelectAll.setChecked(false);
    }

    public void submitResult(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("EventSelectedUsers",selectedUser);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cb_event_select_all:
                if (((CheckBox) v).isChecked()) {
                    for (EventUser eventUser : eventUsers) {
                        eventUser.setUserSelected(true);
                        saveDataToList(eventUser, true);
                    }
                } else {
                    for (EventUser eventUser : eventUsers) {
                        eventUser.setUserSelected(false);
                        saveDataToList(eventUser, false);
                    }
                }
                mAdapter = new EventAttendanceListAdapter(selectedUser, EventUserAttendanceActivity.this);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_submitt:
                submitResult();
                break;

        }

    }
}
