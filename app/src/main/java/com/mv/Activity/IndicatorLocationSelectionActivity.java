package com.mv.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.Model.LocationModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityReportLocationSelectionBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndicatorLocationSelectionActivity extends AppCompatActivity implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ActivityReportLocationSelectionBinding binding;
    private Activity context;
    private LocationModel locationModel;
    private Task task;

    private int mSelectState = 1;
    private int mSelectDistrict = 1;
    private int mSelectTaluka = 0;
    private int mSelectVillage = 0;

    private List<String> mListDistrict, mListTaluka, mListVillage, mListSchoolName, mStateList;

    private ArrayAdapter<String> districtAdapter;
    private ArrayAdapter<String> talukaAdapter;
    private ArrayAdapter<String> stateAdapter;

    private String roleList;
    private String title;
    private String processId;
    private String value = "";

    private boolean[] mSelection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_report_location_selection);
        binding.setActivity(this);

        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);

        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString(Constants.TITLE);
            processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
        }

        locationModel = new LocationModel();
        locationModel.setState("");
        locationModel.setDistrict("");
        locationModel.setTaluka("");

        initViews();
    }

    private void initViews() {
        setActionbar();
        Utills.setupUI(findViewById(R.id.layout_main), this);

        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.editMultiselectTaluka.setOnClickListener(this);
        binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
        binding.lyState.setOnClickListener(this);

        mListDistrict = new ArrayList<>();
        mListTaluka = new ArrayList<>();
        mListVillage = new ArrayList<>();
        mListSchoolName = new ArrayList<>();

        mStateList = new ArrayList<>();
        mListDistrict.add(User.getCurrentUser(context).getMvUser().getDistrict());
        mListTaluka.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");

        mStateList = new ArrayList<>(Arrays.asList(getColumnIdex((User.getCurrentUser(getApplicationContext()).getMvUser().getState()).split(","))));
        mStateList.add(0, "Select");

        stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mStateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(stateAdapter);

        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListDistrict);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);

        talukaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListTaluka);
        talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTaluka.setAdapter(talukaAdapter);

        if (Utills.isConnected(this)) {
            getState();
        }

        // code related to date filter
        // set the components - text, image and button
        binding.txtDateFrom.setOnClickListener(v -> showDateDialog(binding.txtDateFrom));
        binding.txtDateTo.setOnClickListener(v -> showDateDialog(binding.txtDateTo));
    }

    //adding date picker dialog for voucher date filteration
    @SuppressLint("SetTextI18n")
    private void showDateDialog(TextView textView) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) ->
                textView.setText(year + "-" + getTwoDigit(monthOfYear + 1)
                        + "-" + getTwoDigit(dayOfMonth)), mYear, mMonth, mDay);
        dpd.show();
    }

    //returns two digit number of month
    private static String getTwoDigit(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    private static String[] getColumnIdex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;
    }

    private void setActionbar() {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Select Location");

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
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
                break;

            case R.id.edit_multiselect_taluka:
                showMultiselectDialog((ArrayList<String>) mListTaluka);
                break;
        }
    }

    private void sendLocation() {
        if (locationModel.getState().equals("") || !locationModel.getState().equals("Select")) {
            switch (processId) {
                case "": {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, PiachartActivity.class);
                    intent.putExtra(Constants.TITLE, title);
                    intent.putExtra(Constants.INDICATOR_TASK, task);
                    intent.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                    intent.putExtra(Constants.LOCATION, locationModel);
                    startActivity(intent);
                    finish();
                    break;
                }

                case "version": {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, VersionReportActivity.class);
                    intent.putExtra(Constants.LOCATION, locationModel);
                    startActivity(intent);
                    finish();
                    break;
                }

                default: {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, OverallReportActivity.class);
                    intent.putExtra(Constants.TITLE, title);
                    intent.putExtra(Constants.INDICATOR_TASK, task);
                    intent.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                    intent.putExtra(Constants.LOCATION, locationModel);
                    intent.putExtra(Constants.PROCESS_ID, processId);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        } else {
            Utills.showToast("Please Select State", IndicatorLocationSelectionActivity.this);
        }
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

                if (mSelectState != 0) {
                    locationModel.setState(adapterView.getItemAtPosition(i).toString());
                    if (Utills.isConnected(this)) {
                        getDistrict();
                    } else {
                        mListDistrict = AppDatabase.getAppDatabase(context).userDao()
                                .getDistrict(User.getCurrentUser(context).getMvUser().getState());
                        mListDistrict.add(0, "Select");
                        districtAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, mListDistrict);
                        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerDistrict.setAdapter(districtAdapter);
                    }
                } else {
                    locationModel.setState("Select");
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                }

                mListTaluka.clear();
                mListVillage.clear();
                mListSchoolName.clear();

                mListTaluka.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");

                districtAdapter.notifyDataSetChanged();
                districtAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListDistrict);
                districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerDistrict.setAdapter(districtAdapter);

                talukaAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListTaluka);
                talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerTaluka.setAdapter(talukaAdapter);
                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    locationModel.setDistrict(adapterView.getItemAtPosition(i).toString());
                    if (Utills.isConnected(this)) {
                        getTaluka();
                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(
                                User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        talukaAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, mListTaluka);
                        talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerTaluka.setAdapter(talukaAdapter);
                    }
                } else {
                    locationModel.setDistrict("Select");
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                }

                mListVillage.clear();
                mListSchoolName.clear();

                mListVillage.add("Select");
                mListSchoolName.add("Select");

                talukaAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListTaluka);
                talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                break;

            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    locationModel.setTaluka(adapterView.getItemAtPosition(i).toString());
                } else {
                    locationModel.setTaluka("Select");
                }

                mListVillage.clear();
                mListSchoolName.clear();
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                break;

            case R.id.spinner_cluster:
                if (i != 0) {
                    if (Utills.isConnected(this)) {
                        getVillage();
                    } else {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(
                                User.getCurrentUser(context).getMvUser().getState(),
                                mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListVillage.add(0, "Select");
                    }
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                }

                mListSchoolName.clear();
                mListSchoolName.add("Select");
                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    if (Utills.isConnected(this)) {
                        getSchool();
                    } else {
                        mListSchoolName.clear();
                        mListSchoolName.add("Select");
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(
                                User.getCurrentUser(context).getMvUser().getState(),
                                mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                                mListVillage.get(mSelectVillage));
                        mListSchoolName.add(0, "Select");
                    }
                }
                break;

            case R.id.spinner_school_name:
                break;
        }
    }

    private void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0) {
            spinner.setSelection(itemList.indexOf(selectedValue));
        }
    }

    private void showMultiselectDialog(ArrayList<String> arrayList) {
        if (arrayList.contains("Select")) {
            arrayList.remove(arrayList.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[arrayList.size()]);
        mSelection = new boolean[(items.length)];
        Arrays.fill(mSelection, false);

        if (value.length() != 0) {
            String[] talukas = value.split(",");
            for (String taluka : talukas) {
                if (arrayList.contains(taluka.trim())) {
                    mSelection[arrayList.indexOf(taluka.trim())] = true;
                }
            }
        }

        // arraylist to keep the selected items
        AlertDialog dialog = new AlertDialog.Builder(IndicatorLocationSelectionActivity.this)
                .setTitle(getString(R.string.taluka))
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        value = buildSelectedItemString(items);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(IndicatorLocationSelectionActivity.this.getString(R.string.ok), (dialog12, id) -> {
                    binding.editMultiselectTaluka.setText(value);
                    locationModel.setTaluka(value);
                }).setNegativeButton(IndicatorLocationSelectionActivity.this.getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(";");
                }

                foundOne = true;
                sb.append(items[i]);
            }
        }
        return sb.toString();
    }

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(User.getCurrentUser(IndicatorLocationSelectionActivity.this).getMvUser().getState(),
                mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), "structure").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListVillage.clear();
                            mListVillage.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListVillage.add(jsonArr.getString(i));
                            }
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
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(User.getCurrentUser(IndicatorLocationSelectionActivity.this).getMvUser().getState(),
                mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka),
                mListVillage.get(mSelectVillage), "structure").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListSchoolName.clear();
                            mListSchoolName.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListSchoolName.add(jsonArr.getString(i));
                            }
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

    private void getState() {
        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getState("structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mStateList.clear();
                            mStateList.add("Select");

                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }

                            setSpinnerAdapter(mStateList, stateAdapter, binding.spinnerState,
                                    User.getCurrentUser(getApplicationContext()).getMvUser().getState());
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
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getDistrict(mStateList.get(mSelectState), "structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListDistrict.clear();
                            mListDistrict.add("Select");

                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }

                            setSpinnerAdapter(mListDistrict, districtAdapter, binding.spinnerDistrict,
                                    User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());

                            if (mListDistrict.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict())) {
                                binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
                            } else {
                                locationModel.setTaluka("Select");
                                binding.editMultiselectTaluka.setText("Select");
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
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),
                "structure").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }

                            setSpinnerAdapter(mListTaluka, talukaAdapter, binding.spinnerTaluka,
                                    User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());

                            if (mListTaluka.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())) {
                                binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
                            } else {
                                locationModel.setTaluka("Select");
                                binding.editMultiselectTaluka.setText("Select");
                            }
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
