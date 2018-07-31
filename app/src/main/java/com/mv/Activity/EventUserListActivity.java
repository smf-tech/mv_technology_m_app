package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Adapter.EventUserListAdapter;
import com.mv.Model.EventUser;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityEventUserListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EventUserListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ActivityEventUserListBinding binding;
    private ImageView img_back, img_logout;
    private TextView toolbar_title;
    private ArrayList<EventUser> eventUsers = new ArrayList<>();
    private ArrayList<EventUser> eventUsersOld = new ArrayList<>();
    private ArrayList<EventUser> eventUsersFliter = new ArrayList<>();
    private RelativeLayout mToolBar;
    //private ActivityProgrammeManagmentBinding binding;
    private PreferenceHelper preferenceHelper;
    public String selectedState = "", selectedDisrict = "", selectedRolename = "", selectedTaluka = "", selectedCluster = "", selectedVillage = "", selectedSchool = "", selectedOrganization = "", selectedUserId = "", selectedUserName = "", selectedCatagory = "";
    private EventUserListAdapter mAdapter;
    private ArrayList<String> selectedProcessId = new ArrayList<>();
    private ArrayList<String> selectedRole = new ArrayList<>();
    private int mSelectOrganization = 0, mSelectRole = 0, mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListOrganization, mListRoleName, mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;
    private ArrayList<EventUser> selectedUser = new ArrayList<>();
    private ArrayAdapter<String> district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, state_adapter, organization_adapter, role_adapter, catagory_adapter;
    private Activity context;
    private ArrayList<EventUser> calenderEventUserArrayList = new ArrayList<>();
    String eventID;
    List<EventUser> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_user_list);
        binding.setActivity(this);
        eventID = getIntent().getStringExtra("EventID");
        initViews();
        getEventUser();
    }


    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        selectedState = User.getCurrentUser(getApplicationContext()).getMvUser().getState();
        selectedDisrict = User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict();
        selectedTaluka = User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka();
        selectedCluster = User.getCurrentUser(getApplicationContext()).getMvUser().getCluster();
        selectedVillage = User.getCurrentUser(getApplicationContext()).getMvUser().getVillage();
        selectedSchool = User.getCurrentUser(getApplicationContext()).getMvUser().getSchool_Name();
        selectedOrganization = User.getCurrentUser(getApplicationContext()).getMvUser().getOrganisation();
        // selectedRolename = User.getCurrentUser(getApplicationContext()).getMvUser().getRoll();
        selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex("Select".split(","))));
        selectedProcessId = new ArrayList<String>(Arrays.asList(getColumnIdex(("Other").split(","))));
        binding.spinnerRole.setText("Select");

        binding.rlMoreLocation.setOnClickListener(this);
        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.spinnerOrganization.setOnItemSelectedListener(this);

        binding.spinnerRole.setOnClickListener(this);
        //
        mStateList = new ArrayList<String>();
        mStateList.add("Select");
        //mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        mListDistrict = new ArrayList<String>();
        mListDistrict.add("Select");

        mListTaluka = new ArrayList<String>();
        mListTaluka.add("Select");
        mListCluster = new ArrayList<String>();
        mListCluster.add("Select");
        mListVillage = new ArrayList<String>();
        mListVillage.add("Select");

        mListSchoolName = new ArrayList<String>();
        mListSchoolName.add("Select");

        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);
        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
        setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
        setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
        setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

        mListOrganization = new ArrayList<String>();
        mListOrganization.add("Select");

        mListRoleName = new ArrayList<String>();
        mStateList.clear();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.removeAll(Collections.singleton(null));
        if (mStateList.size() == 0) {
            if (Utills.isConnected(this))
                getState();
        } else {
            mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
            mStateList.add(0, "Select");
            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);
            //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        }
        if (Utills.isConnected(this))
            getOrganization();

        setActionbar("User List");

        checkAllSelected(eventUsers);

        mAdapter = new EventUserListAdapter(eventUsersOld, eventUsers, EventUserListActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

        binding.btnSubmit.setOnClickListener(this);
        binding.editTextEmail.addTextChangedListener(watch);
        binding.cbEventSelectAll.setOnClickListener(this);
    }

    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mStateList.clear();
                            mStateList.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);

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

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mStateList.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(response.body().string());
                            mListDistrict.clear();
                            mListDistrict.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);
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

    private void getOrganization() {
        Utills.showProgressDialog(this, "Loading Organization", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetOrganizationUrl;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListOrganization.clear();
                            mListOrganization.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListOrganization.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mListOrganization, organization_adapter, binding.spinnerOrganization, selectedOrganization);

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

    private void getRole() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/data/v36.0/query/?q=select+Id,Juridictions__c,Name+from+MV_Role__c+where+Organisation__c='" + mListOrganization.get(mSelectOrganization) + "'";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {


                            JSONObject obj = new JSONObject(data);
                            JSONArray jsonArray = obj.getJSONArray("records");
                            mListRoleName.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                mListRoleName.add(jsonObject.getString("Name"));

                            }

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

    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");
                            JSONArray jsonArr = new JSONArray(response.body().string());
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                            Intent intent = new Intent(getApplicationContext(), LocationService.class);
                            // add infos for the service which file to download and where to store
                            intent.putExtra(Constants.State, mStateList.get(mSelectState));
                            intent.putExtra(Constants.DISTRICT, mListDistrict.get(mSelectDistrict));
                            startService(intent);
                            // taluka_adapter.notifyDataSetChanged();
                        }
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

    private void getCluster() {
        Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getCluster(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListCluster.clear();
                            mListCluster.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListCluster.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                        }
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

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListVillage.clear();
                            mListVillage.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListVillage.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                        }
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

    private void getSchool() {
        Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            mListSchoolName.clear();
                            mListSchoolName.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListSchoolName.add(jsonArr.getString(i));
                            }
                            setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                        }
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

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)

            spinner.setSelection(itemList.indexOf(selectedValue));
    }

    public static String[] getColumnIdex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;
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
        list = new ArrayList<>();
        eventUsersFliter.clear();
        for (int i = 0; i < eventUsers.size(); i++) {
            eventUsersFliter.add(eventUsers.get(i));
        }
        list.clear();
        for (int i = 0; i < eventUsersFliter.size(); i++) {
            if (eventUsersFliter.get(i).getUserName().toLowerCase().contains(s.toLowerCase())) {
                list.add(eventUsersFliter.get(i));
            }
        }
        checkAllSelected((ArrayList<EventUser>) list);

        mAdapter = new EventUserListAdapter(eventUsersOld, list, EventUserListActivity.this);
        binding.recyclerView.setAdapter(mAdapter);
    }

    public void saveDataToList(EventUser eventUser, boolean isSelected) {
        if (isSelected) {
            Boolean prsent = false;
            for (int i = 0; i < selectedUser.size(); i++) {
                if (selectedUser.get(i).getUserID().equalsIgnoreCase(eventUser.getUserID())) {
                    prsent = true;
                    break;
                } else {
                    prsent = false;
                }
            }
            if (!prsent)
                selectedUser.add(eventUser);
        } else {
            for (int i = 0; i < selectedUser.size(); i++) {
                if (selectedUser.get(i).getUserID().equalsIgnoreCase(eventUser.getUserID())) {
                    selectedUser.remove(i);
                    break;
                }
            }
        }
        eventUsers.set(eventUsers.indexOf(eventUser), eventUser);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:
                Intent openClass = new Intent(EventUserListActivity.this, CalenderFliterActivity.class);
                // openClass.putExtra(Constants.PROCESS_ID, dashaBoardListModel);
                if (binding.cbEventSelectAll.isChecked() && binding.editTextEmail.getText().toString().trim().length() == 0)
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, eventUsers);
                else
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, selectedUser);
                openClass.putParcelableArrayListExtra(Constants.ALLUSER, calenderEventUserArrayList);
                openClass.putExtra("Role", selectedRolename);
                //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                setResult(RESULT_OK, openClass);
                //  startActivity(openClass);
                finish();
                break;
            case R.id.cb_event_select_all:
                if (((CheckBox) view).isChecked()) {
                    if (binding.editTextEmail.getText().toString().trim().length() == 0) {
                        for (EventUser eventUser : eventUsers) {
                            eventUser.setUserSelected(true);
                            saveDataToList(eventUser, true);
                        }
                    } else {
                        for (EventUser eventUser : list) {
                            eventUser.setUserSelected(true);
                            saveDataToList(eventUser, true);
                        }
                    }

                } else {
                    if (binding.editTextEmail.getText().toString().trim().length() == 0) {
                        for (EventUser eventUser : eventUsers) {
                            eventUser.setUserSelected(false);
                            saveDataToList(eventUser, false);
                        }
                    } else {
                        for (EventUser eventUser : list) {
                            eventUser.setUserSelected(false);
                            saveDataToList(eventUser, false);
                        }
                    }

                }
                mAdapter.notifyDataSetChanged();

                break;
            case R.id.spinner_role:

                showrRoleDialog();

                // sendData();
                break;
            case R.id.rl_more_location:

                if (binding.llLoacationlayout.isShown()) {
                    binding.llLoacationlayout.setVisibility(View.GONE);
                } else
                    binding.llLoacationlayout.setVisibility(View.VISIBLE);

                break;
        }
    }

    private void showrRoleDialog() {
        final List<String> temp = mListRoleName;
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            if (selectedRole.contains(temp.get(i)))
                mSelection[i] = true;
            else
                mSelection[i] = false;
        }

       /* if (mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()) > 0)
            mSelection[mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;
*/
// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
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
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                        if (selectedRolename.length() > 0)
                            binding.spinnerRole.setText(selectedRolename);
                        else
                            binding.spinnerRole.setText("Select");
                        selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex((selectedRolename).split(","))));
                        getAllFilterUser();
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;

                mListTaluka = new ArrayList<>();
                mListTaluka.add(0, "Select");
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                mListCluster = new ArrayList<>();
                mListCluster.add(0, "Select");
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                mListVillage = new ArrayList<>();
                mListVillage.add(0, "Select");
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                mListSchoolName = new ArrayList<>();
                mListSchoolName.add(0, "Select");
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                if (mSelectState != 0) {
                    selectedState = mStateList.get(mSelectState);
                }
                mListDistrict.clear();
                mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                mListDistrict.removeAll(Collections.singleton(null));
                if (mListDistrict.size() == 0) {


                    if (Utills.isConnected(this))
                        getDistrict();
                    else {
                        mListDistrict = new ArrayList<>();
                        mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                        mListDistrict.removeAll(Collections.singleton(null));
                        mListDistrict.add(0, "Select");
                        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                    }

                } else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                }
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                getAllFilterUser();
                break;

            case R.id.spinner_district:
                mSelectDistrict = i;

                mListCluster = new ArrayList<>();
                mListCluster.add(0, "Select");
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                mListVillage = new ArrayList<>();
                mListVillage.add(0, "Select");
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                mListSchoolName = new ArrayList<>();
                mListSchoolName.add(0, "Select");
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                if (mSelectDistrict != 0) {
                    selectedDisrict = mListDistrict.get(mSelectDistrict);
            /*        selectedTaluka="";
                    selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    mListTaluka.clear();
                    mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                    mListTaluka.removeAll(Collections.singleton(null));
                    if (mListTaluka.size() == 0) {

                        if (Utills.isConnected(this))
                            getTaluka();
                        else {
                            mListTaluka.clear();
                            mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                            mListTaluka.add(0, "Select");
                            mListTaluka.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                        }

                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        mListTaluka.removeAll(Collections.singleton(null));
                        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                    }
                } else {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                }


                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                getAllFilterUser();
                break;

            case R.id.spinner_taluka:
                mSelectTaluka = i;

                mListVillage = new ArrayList<>();
                mListVillage.add(0, "Select");
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                mListSchoolName = new ArrayList<>();
                mListSchoolName.add(0, "Select");
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);
            /*        selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    mListCluster.clear();
                    mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                    mListCluster.removeAll(Collections.singleton(null));
                    if (mListCluster.size() == 0) {
                        mListCluster.add(0, "Select");
                        if (Utills.isConnected(this))
                            getCluster();
                        else {

                            mListCluster.clear();
                            mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                            mListCluster.add(0, "Select");
                            mListCluster.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                        }
                    } else {
                        mListCluster.add(0, "Select");
                        setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                    }

                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                }
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                getAllFilterUser();
                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;

                mListSchoolName = new ArrayList<>();
                mListSchoolName.add(0, "Select");
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);
/*                    selectedVillage="";
                    selectedSchool="";*/
                    mListVillage.clear();
                    mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                    mListVillage.removeAll(Collections.singleton(null));
                    if (mListVillage.size() == 0) {
                        if (Utills.isConnected(this))
                            getVillage();
                        else {
                            mListVillage.clear();
                            mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                            mListVillage.add(0, "Select");
                            mListVillage.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                        }
                    } else {
                        mListVillage.add(0, "Select");
                        setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                    }

                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                }

                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                getAllFilterUser();
                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);
                    // selectedSchool="";

                    mListSchoolName.clear();
                    mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                    mListSchoolName.removeAll(Collections.singleton(null));
                    if (mListSchoolName.size() == 0) {
                        if (Utills.isConnected(this))
                            getSchool();
                        else {
                            mListSchoolName.clear();
                            mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                            mListSchoolName.add(0, "Select");
                            mListSchoolName.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                        }
                    } else {
                        mListSchoolName.add(0, "Select");
                        setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                    }

                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                    setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


                }
                getAllFilterUser();
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                selectedSchool = mListSchoolName.get(mSelectSchoolName);
                getAllFilterUser();
                break;
            case R.id.spinner_organization:
                mSelectOrganization = i;
                if (mSelectOrganization != 0) {
                    if (Utills.isConnected(this))
                        getRole();
                } else {
                    mListRoleName.add(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll());
                }
                break;
        }
    }

    private void getAllFilterUser() {
        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String role = "";
        if (selectedRolename.length() > 0) {
            role = selectedRolename;
        } else {
            role = "Select";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserDataForCalnder);
        if (binding.spinnerState != null && binding.spinnerState.getSelectedItem() != null) {
            buffer.append("?state=" + binding.spinnerState.getSelectedItem().toString());
        } else {
            buffer.append("?state=Select");
        }
        if (binding.spinnerDistrict != null && binding.spinnerDistrict.getSelectedItem() != null) {
            buffer.append("&dist=" + binding.spinnerDistrict.getSelectedItem().toString());
        } else {
            buffer.append("?&dist==Select");
        }
        if (binding.spinnerTaluka != null && binding.spinnerTaluka.getSelectedItem() != null) {
            buffer.append("&tal=" + binding.spinnerTaluka.getSelectedItem().toString());
        } else {
            buffer.append("&tal==Select");
        }
        if (binding.spinnerCluster != null && binding.spinnerCluster.getSelectedItem() != null) {
            buffer.append("&cluster=" + binding.spinnerCluster.getSelectedItem().toString());
        } else {
            buffer.append("&cluster=Select");
        }
        if (binding.spinnerVillage != null && binding.spinnerVillage.getSelectedItem() != null) {
            buffer.append("&village=" + binding.spinnerVillage.getSelectedItem().toString());
        } else {
            buffer.append("&village=Select");
        }
        if (binding.spinnerSchoolName != null && binding.spinnerSchoolName.getSelectedItem() != null) {
            buffer.append("&school=" + binding.spinnerSchoolName.getSelectedItem().toString());
        } else {
            buffer.append("&school==Select");
        }
        buffer.append("&role=" + role);

        apiService.getSalesForceData(buffer.toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            eventUsers.clear();
                            calenderEventUserArrayList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                EventUser eventUser = new EventUser();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                eventUser.setRole(jsonObject.getString("role"));
                                eventUser.setUserID(jsonObject.getString("Id"));
                                eventUser.setUserName(jsonObject.getString("userName"));
                                eventUser.setUserSelected(false);
                                eventUsers.add(eventUser);
                                calenderEventUserArrayList = new ArrayList<>();
                            }

                            if (eventUsersOld != null) {
                                for (int i = 0; i < eventUsersOld.size(); i++) {
                                    for (int j = 0; j < eventUsers.size(); j++) {
                                        if (eventUsersOld.get(i).getUserID().equals(eventUsers.get(j).getUserID())) {
                                            eventUsers.get(j).setUserSelected(true);
                                        }
                                    }
                                }
                            }

                            checkAllSelected(eventUsers);

                            mAdapter = new EventUserListAdapter(eventUsersOld, eventUsers, EventUserListActivity.this);
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

    private void getEventUser() {
        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        StringBuffer buffer = new StringBuffer();
        buffer.append(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetEventCalenderMembers_Url);
        //   https://cs57.salesforce.com/services/apexrest/getUserDataForCalnderAttendance?eventId=a1C0k000000Sh1l
        buffer.append("?eventId=" + eventID);

        Log.e("Url", buffer.toString());

        apiService.getSalesForceData(buffer.toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            eventUsers.clear();
                            eventUsersOld.clear();
                            calenderEventUserArrayList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                EventUser eventUser = new EventUser();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                eventUser.setRole(jsonObject.getString("role"));
                                eventUser.setUserID(jsonObject.getString("Id"));
                                eventUser.setUserName(jsonObject.getString("userName"));
                                eventUser.setUserSelected(false);
                                eventUsers.add(eventUser);
                                calenderEventUserArrayList = new ArrayList<>();
                            }
                            eventUsersOld.addAll(eventUsers);

                            if (eventUsersOld != null) {
                                for (int i = 0; i < eventUsersOld.size(); i++) {
                                    for (int j = 0; j < eventUsers.size(); j++) {
                                        if (eventUsersOld.get(i).getUserID().equals(eventUsers.get(j).getUserID())) {
                                            eventUsers.get(j).setUserSelected(true);
                                        }
                                    }
                                }
                            }

                            checkAllSelected(eventUsers);

                            mAdapter = new EventUserListAdapter(eventUsersOld, eventUsers, EventUserListActivity.this);
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

    // chech all the user in list are selected or not
    public void checkAllSelected(ArrayList<EventUser> list) {
        boolean allCheck = true;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).getUserSelected()) {
                allCheck = false;
                break;
            }
        }
        if (allCheck)
            binding.cbEventSelectAll.setChecked(true);
        else
            binding.cbEventSelectAll.setChecked(false);
    }

    // uncheck chech all check box
    public void checkAllDeSelected() {
        binding.cbEventSelectAll.setChecked(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
