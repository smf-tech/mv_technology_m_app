package com.mv.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Asset;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetAllocation_Activity extends AppCompatActivity implements View.OnClickListener {
    Spinner spinner_stock;
    EditText edit_text_username, edit_text_assetname, edit_text_no, edit_text_name,edit_asset_status;
    Button btn_allocate_asset,btn_reject_asset;
    PreferenceHelper preferenceHelper;
    Asset asset;
    ArrayList<String> stocklist = new ArrayList<>();
    private ArrayAdapter<String> stock_adapter;
    String stock_id, asset_id;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    TextInputLayout input_no, input_name;
    LinearLayout lnr_asset_manager, lnr_user;
    String Fname, Lname, Id;
    int selectstockid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_);
        Initviews();
    }

    private void Initviews() {
        preferenceHelper = new PreferenceHelper(this);

        spinner_stock = (Spinner) findViewById(R.id.spinner_stock);
        edit_text_username = (EditText) findViewById(R.id.edit_text_username);
        edit_text_assetname = (EditText) findViewById(R.id.edit_text_assetname);
        btn_allocate_asset = (Button) findViewById(R.id.btn_allocate_asset);
        btn_reject_asset = (Button) findViewById(R.id.btn_reject_asset);
        edit_text_no = (EditText) findViewById(R.id.edit_text_no);
        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        edit_asset_status = (EditText) findViewById(R.id.edit_asset_status);
        lnr_user = (LinearLayout) findViewById(R.id.lnr_user);
        lnr_asset_manager = (LinearLayout) findViewById(R.id.lnr_asset_manager);
        btn_allocate_asset.setOnClickListener(this);
        btn_reject_asset.setOnClickListener(this);
        edit_text_no.addTextChangedListener(watch);

        if (getIntent().getExtras() != null) {
            asset = (Asset) getIntent().getExtras().getSerializable("Assets");
            asset_id = asset != null ? asset.getAsset_id() : null;
            edit_asset_status.setText(asset != null ? asset.getAllocationStatus() : "");
        }

        if (asset != null && User.getCurrentUser(AssetAllocation_Activity.this).getMvUser() != null &&
                User.getCurrentUser(AssetAllocation_Activity.this).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
            lnr_asset_manager.setVisibility(View.VISIBLE);
            lnr_user.setVisibility(View.GONE);
            btn_allocate_asset.setText(getResources().getString(R.string.allocate));
            edit_text_username.setText(asset.getUsername());
            edit_text_assetname.setText(asset.getAssetName());
            setActionbar(getResources().getString(R.string.asset_allocation));
        } else {
            lnr_user.setVisibility(View.VISIBLE);
            lnr_asset_manager.setVisibility(View.GONE);
            btn_allocate_asset.setText(getResources().getString(R.string.reallocate));
            setActionbar(getResources().getString(R.string.asset_reallocation));
        }

        GetStock();
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

    private void GetStock() {
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url;


        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.AssetStock + "?assetId=" + asset_id;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Utills.hideProgressDialog();
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            final List<Asset> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Asset[].class));
                            stocklist.add("Select");
                            for (int i = 0; i < temp.size(); i++) {
                                if (temp.get(i).getCode() != null)
                                    stocklist.add(temp.get(i).getCode());
                            }
                            stock_adapter = new ArrayAdapter<String>(AssetAllocation_Activity.this,
                                    android.R.layout.simple_spinner_item, stocklist);
                            stock_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_stock.setAdapter(stock_adapter);
                            spinner_stock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectstockid = i;
                                    if (i != 0) {
                                        stock_id = temp.get(selectstockid - 1).getStockId();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    Utills.hideProgressDialog();
                    e.printStackTrace();
                } catch (IOException e) {
                    Utills.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), AssetAllocation_Activity.this);

            }
        });
    }

    private void setStatus(String status) {
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));
        JSONObject jsonObject1 = new JSONObject();

        try {
            if (User.getCurrentUser(AssetAllocation_Activity.this).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
                jsonObject1.put("ASSET_STOCK__c", stock_id);
            } else {
                jsonObject1.put("Requested_User__c", Id);
                jsonObject1.put("Allocation_Quantity__c", "1");
                jsonObject1.put("ASSET_STOCK__c", asset.getStockId());
            }

            jsonObject1.put("Allocation_Status__c", status);
            jsonObject1.put("ASSET__c", asset_id);
            jsonObject1.put("Id", asset.getAssetAllocationId());
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("assetAlloc", jsonObject1);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject2.toString());


            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.saveAssetRequest, gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        Utills.showToast(getString(R.string.submitted_successfully), AssetAllocation_Activity.this);
                        finish();
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), AssetAllocation_Activity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), AssetAllocation_Activity.this);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void GetUSerName() {
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url;


        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserThroughMobileNo + "?mobileNo=" + edit_text_no.getText().toString().trim();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                String data;
                try {
                    if (response.body() != null) {
                        data = response.body().string();
                        if (data.length() > 0) {
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            Asset asset = gson.fromJson(data, Asset.class);
                            Id = asset.getAsset_id();
                            Fname = asset.getName();
                            Lname = asset.getLast_Name__c();
                            edit_text_name.setText(Fname + " " + Lname);
                        }
                    } else {
                        edit_text_name.setText("");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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
            if (s.length() == 10) {
                GetUSerName();


            }

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_allocate_asset:
                if (isValid())
                    setStatus("Allocated");
                break;
            case R.id.btn_reject_asset:
//                if (isValid())
                    setStatus("Rejected");
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private boolean isValid() {
        String str = "";
        if (User.getCurrentUser(AssetAllocation_Activity.this).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
            if (selectstockid == 0)
                str = "Please select Stock";
        } else {
            if (edit_text_no.getText().toString().length() == 0)
                str = "Please enter Mobile Number";
            else if (edit_text_no.getText().toString().length() != 10)
                str = "Please enter 10 digit Mobile Number";
            else if (edit_text_name.getText().toString().length() == 0)
                str = "Please enter mobile number of existing user";
        }
        if (str.length() == 0)
            return true;
        Utills.showToast(str, AssetAllocation_Activity.this);
        return false;
    }
}