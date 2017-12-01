package com.mv.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityBroadcastBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 30-11-2017.
 */

public class BroadCastActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityBroadcastBinding binding;
    private PreferenceHelper preferenceHelper;
    private Spinner spinner_taluka, spinner_state, spinner_district;
    private int mSelectRole = 0, mSelectState = 0, mSelectDistrict = 0, mSelectTaluka = 0;
    private ArrayList<String> mListRoleName, mListState, mListDistrict, mListTaluka;
    private ArrayAdapter<String> state_adapter, district_adapter, taluka_adapter;
    private boolean[] mSelection = null;
    private String[] items = null;
    private String value;
    private StringBuilder selectedRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_broadcast);
        binding.setActivity(this);
        initViews();
        if (Utills.isConnected(this)) {
            getState();
            getRole();
        } else {
            showPopUp();
        }
    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
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
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListState.clear();
                    mListState.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListState.add(jsonArray.getString(i));
                    }
                    state_adapter.notifyDataSetChanged();

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
                + "/services/data/v36.0/query/?q=select+Name+from+MV_Role__c";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray jsonArray = obj.getJSONArray("records");
                    mListRoleName.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        mListRoleName.add(jsonObject.getString("Name"));
                    }
                    // role_adapter.notifyDataSetChanged();

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

    private void showDialog() {
        items = new String[mListRoleName.size()];
        for (int i = 0; i < mListRoleName.size(); i++) {
            items[i] = mListRoleName.get(i);
        }
        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Select Roles")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;
                            value = buildSelectedItemString(items);
                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("value", "value");
                        binding.txtRole.setText(selectedRoles);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        selectedRoles = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                    selectedRoles.append(",");
                }
                foundOne = true;
                sb.append(i);
                selectedRoles.append(items[i]);
            }
        }
        return sb.toString();
    }

    private void initViews() {
        setActionbar("Add Broadcast");
        preferenceHelper = new PreferenceHelper(this);

        binding.txtRole.setOnClickListener(this);

        spinner_taluka = (Spinner) findViewById(R.id.spinner_taluka);
        spinner_state = (Spinner) findViewById(R.id.spinner_state);
        spinner_district = (Spinner) findViewById(R.id.spinner_district);

        spinner_taluka.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);
        spinner_district.setOnItemSelectedListener(this);

        mListState = new ArrayList<String>();
        mListRoleName = new ArrayList<String>();
        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();

        mListState.add("Select");
        mListDistrict.add("Select");


      /*  role_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListRoleName);
        role_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_role.setAdapter(role_adapter);*/

        state_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListState);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(state_adapter);

        district_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_district.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_taluka.setAdapter(taluka_adapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.txt_role:
                showDialog();
                break;
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    getDistrict();
                }
                mListDistrict.clear();
                mListTaluka.clear();
                mListTaluka.add("Select");
                mListDistrict.add("Select");
                district_adapter.notifyDataSetChanged();
                taluka_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    getTaluka();
                }
                mListTaluka.clear();
                mListTaluka.add("Select");
                taluka_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                break;
        }
    }

    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
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
                    taluka_adapter.notifyDataSetChanged();

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

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mListState.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
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
                    district_adapter.notifyDataSetChanged();

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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onAddImageClick() {

    }


    public void onBtnSubmitClick() {


    }
}
