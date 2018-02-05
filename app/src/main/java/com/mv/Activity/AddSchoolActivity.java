package com.mv.Activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Model.LocationModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAddSchoolBinding;
import com.mv.databinding.ActivityLoactionSelectionActityBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSchoolActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private ActivityAddSchoolBinding binding;


    private int mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;

    private ArrayAdapter<String> district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, state_adapter, organization_adapter;
    private PreferenceHelper preferenceHelper;
    private RelativeLayout mToolBar;
    private Spinner selectedSpinner;
    String msg = "";
    private int locationState;
    public static String selectedState = "", selectedDisrict = "", selectedTaluka = "", selectedCluster = "", selectedVillage = "", selectedSchool = "";


    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_school);
        binding.setActivity(this);
        initViews();
    }


    private void initViews() {
        setActionbar("Select Location");
        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);

        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCluster = (String) parent.getItemAtPosition(position);

                mListVillage.clear();
                mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), selectedCluster);
                mListVillage.removeAll(Collections.singleton(null));
                if (mListVillage.size() == 0) {
                    if (Utills.isConnected(context))
                        getVillage();
                    else {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), selectedCluster);
                        mListVillage.add(0, "Select");
                        mListVillage.removeAll(Collections.singleton(null));
                        ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                                (context, android.R.layout.select_dialog_item, mListVillage);

                        binding.spinnerVillage.setThreshold(1);
                        binding.spinnerVillage.setAdapter(adapterVillage);
                    }
                } else {
                    mListVillage.add(0, "Select");
                    mListVillage.removeAll(Collections.singleton(null));
                    ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                            (context, android.R.layout.select_dialog_item, mListVillage);

                    binding.spinnerVillage.setThreshold(1);
                    binding.spinnerVillage.setAdapter(adapterVillage);
                }
                binding.spinnerVillage.setText("");
                binding.spinnerSchoolName.setText("");
            }
        });
        binding.spinnerVillage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVillage = (String) parent.getItemAtPosition(position);

                mListSchoolName.clear();
                mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), selectedCluster, selectedVillage);
                mListSchoolName.removeAll(Collections.singleton(null));
                if (mListSchoolName.size() == 0) {
                    if (Utills.isConnected(context))
                        getSchool();
                    else {
                        mListSchoolName.clear();
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), selectedCluster, selectedVillage);
                        mListSchoolName.add(0, "Select");
                        mListSchoolName.removeAll(Collections.singleton(null));
                        ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, mListSchoolName);
                        binding.spinnerSchoolName.setThreshold(1);
                        binding.spinnerSchoolName.setAdapter(adapterSchoolname);
                    }
                } else {
                    mListSchoolName.add(0, "Select");
                    mListSchoolName.removeAll(Collections.singleton(null));
                    ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, mListSchoolName);
                    binding.spinnerSchoolName.setThreshold(1);
                    binding.spinnerSchoolName.setAdapter(adapterSchoolname);

                }
                binding.spinnerSchoolName.setText("");
            }
        });
        binding.spinnerSchoolName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSchool = (String) parent.getItemAtPosition(position);
            }
        });
        binding.btnSubmit.setOnClickListener(this);

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


      /*  if (Utills.isConnected(this))
            getDistrict();
        else {

            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
            mListDistrict.add(0, "Select");
        }
        */
      /*  state_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStateList);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(state_adapter);*/

        mStateList.clear();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.removeAll(Collections.singleton(null));
        if (mStateList.size() == 0) {
            if (Utills.isConnected(this))
                getState();
        }
        else {
            mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
            mStateList.add(0, "Select");
            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);
            //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        }
        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);
        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

        ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, mListVillage);

        binding.spinnerVillage.setThreshold(1);
        binding.spinnerVillage.setAdapter(adapterVillage);
        ArrayAdapter<String> adapterCluster = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, mListCluster);

        binding.spinnerCluster.setThreshold(1);
        binding.spinnerCluster.setAdapter(adapterCluster);

        ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, mListSchoolName);

        binding.spinnerSchoolName.setThreshold(1);
        binding.spinnerSchoolName.setAdapter(adapterSchoolname);


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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:

                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:

                sendLocation();

                // sendData();
                break;

        }
    }

    private void sendLocation() {
        String msg = "";

        if (mSelectState == 0) {
            msg = "Please Select State";
        } else if (mSelectDistrict == 0) {
            msg = "Please Select District";
        } else if (mSelectTaluka == 0) {
            msg = "Please Select Taluka";
        } else if (binding.spinnerCluster.getText().toString().equals("")) {
            msg = "Please Enter Cluster";
        }   else if (binding.spinnerVillage.getText().toString().equals("")) {
            if (mListCluster.contains(binding.spinnerCluster.getText().toString())) {
                msg = "You can Add only new Cluster";
            }
        } else if (binding.spinnerSchoolName.getText().toString().equals("")) {
            if (mListVillage.contains(binding.spinnerVillage.getText().toString())) {
                msg = "You can Add only new Village";
            }
        } else if (mListSchoolName.contains(binding.spinnerSchoolName.getText().toString())) {
            msg = "You can Add only new School";
        }

        if (msg.equals("")) {
            if (Utills.isConnected(context)) {
             submitLocation();
            }
            else {
                Utills.showInternetPopUp(context);
            }

        } else {
            Utills.showToast(msg, context);
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    selectedState = mStateList.get(mSelectState);
                }
                mListDistrict.clear();
                mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                mListDistrict.removeAll(Collections.singleton(null));
                if (mListDistrict.size() == 0) {

                    if (binding.spinnerDistrict.isShown()) {
                        if (Utills.isConnected(this))
                            getDistrict();
                        else {
                            mListDistrict = new ArrayList<>();
                            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                            mListDistrict.removeAll(Collections.singleton(null));
                            mListDistrict.add(0, "Select");
                            setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                        }
                    }
                }
                else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                }
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                binding.spinnerCluster.setText("");
                binding.spinnerVillage.setText("");
                binding.spinnerSchoolName.setText("");

                //    mListDistrict.clear();

                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
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
                        if (binding.spinnerTaluka.isShown()) {
                            if (Utills.isConnected(this))
                                getTaluka();
                            else {
                                mListTaluka.clear();
                                mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                                mListTaluka.add(0, "Select");
                                mListTaluka.removeAll(Collections.singleton(null));
                                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                            }
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

                binding.spinnerCluster.setText("");
                binding.spinnerVillage.setText("");
                binding.spinnerSchoolName.setText("");
                break;


            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);
            /*        selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    if (binding.spinnerCluster.isShown()) {

                        mListCluster.clear();
                        mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListCluster.removeAll(Collections.singleton(null));
                        System.out.println(mListCluster);
                        if (mListCluster.size() == 0) {
                            mListCluster.add(0, "Select");
                            if (Utills.isConnected(this))
                                getCluster();
                            else {

                                mListCluster.clear();
                                mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                                mListCluster.add(0, "Select");
                                mListCluster.removeAll(Collections.singleton(null));
                                ArrayAdapter<String> adapterCluster = new ArrayAdapter<String>
                                        (this, android.R.layout.select_dialog_item, mListCluster);

                                binding.spinnerCluster.setThreshold(1);
                                binding.spinnerCluster.setAdapter(adapterCluster);
                            }
                        } else {
                            mListCluster.add(0, "Select");
                            ArrayAdapter<String> adapterCluster = new ArrayAdapter<String>
                                    (this, android.R.layout.select_dialog_item, mListCluster);

                            binding.spinnerCluster.setThreshold(1);
                            binding.spinnerCluster.setAdapter(adapterCluster);
                        }
                    }
                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    ArrayAdapter<String> adapterCluster = new ArrayAdapter<String>
                            (this, android.R.layout.select_dialog_item, mListCluster);

                    binding.spinnerCluster.setThreshold(1);
                    binding.spinnerCluster.setAdapter(adapterCluster);
                }
                binding.spinnerVillage.setText("");
                binding.spinnerSchoolName.setText("");
                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);
/*                    selectedVillage="";
                    selectedSchool="";*/
                    if (binding.spinnerVillage.isShown()) {

                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                        mListVillage.removeAll(Collections.singleton(null));
                        if (mListVillage.size() == 0) {
                            if (Utills.isConnected(this))
                                getVillage();
                            else {
                                mListVillage.clear();
                                mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                                mListVillage.add(0, "Select");
                                mListVillage.removeAll(Collections.singleton(null));
                                ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                                        (this, android.R.layout.select_dialog_item, mListVillage);

                                binding.spinnerVillage.setThreshold(1);
                                binding.spinnerVillage.setAdapter(adapterVillage);
                            }
                        } else {
                            mListVillage.add(0, "Select");
                            ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                                    (this, android.R.layout.select_dialog_item, mListVillage);

                            binding.spinnerVillage.setThreshold(1);
                            binding.spinnerVillage.setAdapter(adapterVillage);
                        }
                    }
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    ArrayAdapter<String> adapterVillage = new ArrayAdapter<String>
                            (this, android.R.layout.select_dialog_item, mListVillage);

                    binding.spinnerVillage.setThreshold(1);
                    binding.spinnerVillage.setAdapter(adapterVillage);
                }


                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);
                    // selectedSchool="";
                    if (binding.spinnerSchoolName.isShown()) {
                        mListSchoolName.clear();
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                        mListSchoolName.removeAll(Collections.singleton(null));
                        if (mListSchoolName.size() == 0) {
                            if (Utills.isConnected(this))
                                getSchool();
                            else {
                                mListSchoolName.clear();
                                mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                                mListSchoolName.add(0, "Select");
                                mListSchoolName.removeAll(Collections.singleton(null));
                                ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                                        (this, android.R.layout.select_dialog_item, mListSchoolName);

                                binding.spinnerSchoolName.setThreshold(1);
                                binding.spinnerSchoolName.setAdapter(adapterSchoolname);
                            }
                        } else {
                            mListSchoolName.add(0, "Select");
                            ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                                    (this, android.R.layout.select_dialog_item, mListSchoolName);

                            binding.spinnerSchoolName.setThreshold(1);
                            binding.spinnerSchoolName.setAdapter(adapterSchoolname);

                        }
                    }
                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                    ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                            (this, android.R.layout.select_dialog_item, mListSchoolName);

                    binding.spinnerSchoolName.setThreshold(1);
                    binding.spinnerSchoolName.setAdapter(adapterSchoolname);

                }
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                selectedSchool = mListSchoolName.get(mSelectSchoolName);
                break;
        }
    }

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)

            spinner.setSelection(itemList.indexOf(selectedValue));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String abc;

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
        apiService.getDistrict(selectedState).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListDistrict.add(jsonArray.getString(i));
                    }
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

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

        apiService.getTaluka(selectedState, mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListTaluka.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    // add infos for the service which file to download and where to store
                    intent.putExtra(Constants.State, selectedState);
                    intent.putExtra(Constants.DISTRICT, mListDistrict.get(mSelectDistrict));
                    startService(intent);
                    // taluka_adapter.notifyDataSetChanged();
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
        apiService.getCluster(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListCluster.add(jsonArr.getString(i));
                    }
                    ArrayAdapter<String> adapterCluster = new ArrayAdapter<String>
                            (AddSchoolActivity.this, android.R.layout.select_dialog_item, mListCluster);

                    binding.spinnerCluster.setThreshold(1);
                    binding.spinnerCluster.setAdapter(adapterCluster);
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
        apiService.getVillage(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListVillage.add(jsonArr.getString(i));
                    }
                    ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                            (AddSchoolActivity.this, android.R.layout.select_dialog_item, mListSchoolName);

                    binding.spinnerSchoolName.setThreshold(1);
                    binding.spinnerSchoolName.setAdapter(adapterSchoolname);
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

        apiService.getSchool(selectedState, mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");

                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListSchoolName.add(jsonArr.getString(i));
                    }
                    ArrayAdapter<String> adapterSchoolname = new ArrayAdapter<String>
                            (AddSchoolActivity.this, android.R.layout.select_dialog_item, mListSchoolName);

                    binding.spinnerSchoolName.setThreshold(1);
                    binding.spinnerSchoolName.setAdapter(adapterSchoolname);
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


    private void submitLocation() {

        final LocationModel locationModel=new LocationModel();
        locationModel.setState(selectedState);
        locationModel.setDistrict(selectedDisrict);
        locationModel.setTaluka(selectedTaluka);
        locationModel.setCluster(binding.spinnerCluster.getText().toString().toUpperCase());
        locationModel.setVillage( binding.spinnerVillage.getText().toString().toUpperCase());
        locationModel.setSchoolName(binding.spinnerSchoolName.getText().toString().toUpperCase());

        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.submitLocation(selectedState, selectedDisrict, selectedTaluka, binding.spinnerCluster.getText().toString().toUpperCase(), binding.spinnerVillage.getText().toString().toUpperCase(), binding.spinnerSchoolName.getText().toString().toUpperCase()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {

                    if (response.body() != null) {
                        String data = response.body().string();
                        //    Type listType = new TypeToken<ArrayList<LocationModel>>() {}.getType();
                        JSONObject dataObject = new JSONObject(data);
                       if( dataObject.getString("status").equals("1"))
                       {
                           AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertLocation(locationModel);

                           Utills.showToast("Location Inserted successfully",context);

                           finish();
                       }

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
    }

}
