package com.mv.Activity;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
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
    EditText edit_text_specification, edit_text_remarks,edit_text_no,edit_text_name;
    Button btn_allocate_asset;
    PreferenceHelper preferenceHelper;
    Asset asset;
    ArrayList<String> stocklist = new ArrayList<>();
    private ArrayAdapter<String> stock_adapter;
    String stock_id,asset_id;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    TextInputLayout input_no,input_name;
    LinearLayout lnr_asset_manager,lnr_user;
    String Fname, Lname;
    int selectstockid =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_);
        Initviews();
    }

    private void Initviews() {
        preferenceHelper = new PreferenceHelper(this);
        asset = (Asset) getIntent().getExtras().getSerializable("Assets");
        asset_id = asset.getAsset_id();
        spinner_stock = (Spinner) findViewById(R.id.spinner_stock);
        edit_text_specification = (EditText) findViewById(R.id.edit_text_specification);
        edit_text_remarks = (EditText) findViewById(R.id.edit_text_specification);
        btn_allocate_asset = (Button) findViewById(R.id.btn_allocate_asset);
        edit_text_no = (EditText) findViewById(R.id.edit_text_no);
        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        lnr_user = (LinearLayout) findViewById(R.id.lnr_user);
        lnr_asset_manager = (LinearLayout) findViewById(R.id.lnr_asset_manager);
        btn_allocate_asset.setOnClickListener(this);
        edit_text_no.addTextChangedListener(watch);
        if (User.getCurrentUser(AssetAllocation_Activity.this).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")){
            lnr_asset_manager.setVisibility(View.VISIBLE);
            lnr_user.setVisibility(View.GONE);
        }else {
            lnr_user.setVisibility(View.VISIBLE);
            lnr_asset_manager.setVisibility(View.GONE);
        }
        GetStock();
        setActionbar ("Asset Allocation");

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
        Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";


        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.AssetStock + "?assetId=" + asset_id;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Utills.hideProgressDialog();
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            final List<Asset> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Asset[].class));
                            stocklist.add("Select");
                            for (int i = 0; i < temp.size(); i++) {
                                stocklist.add(temp.get(i).getModelNo());

                            }

                            stock_adapter = new ArrayAdapter<String>(AssetAllocation_Activity.this,
                                    android.R.layout.simple_spinner_item, stocklist);
                            stock_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_stock.setAdapter(stock_adapter);

                            spinner_stock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectstockid = i;
                                    if (i!=0) {
                                        stock_id = temp.get(selectstockid-1).getStockId();
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


    private void AllocateStatus() {
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));
        JSONObject jsonObject1 = new JSONObject();

        try {
            if (User.getCurrentUser(AssetAllocation_Activity.this).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")){
                jsonObject1.put("stockId", stock_id);
            }else {
                jsonObject1.put("mobileNo", edit_text_no.getText().toString());
            }

                jsonObject1.put("Allocation_Status__c", "Allocated");
            jsonObject1.put("ASSET__c", asset_id);
            jsonObject1.put("specification",edit_text_specification.getText().toString());
            jsonObject1.put("remarks",edit_text_remarks.getText().toString().trim());
            jsonObject1.put("Asset_Condition__c","");

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
                        Intent intent = new Intent(getApplicationContext(),AssetAllocatedListActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong),AssetAllocation_Activity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong),AssetAllocation_Activity.this);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void GetUSerName(){
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";


        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserThroughMobileNo + "?mobileNo=" + edit_text_no.getText().toString().trim();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                String data = null;
                try {
                    if (response.body()!=null){
                    data = response.body().string();
                    if (data != null && data.length() > 0) {
                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                        Asset asset = gson.fromJson(data, Asset.class);

                        Fname = asset.getName();
                        Lname = asset.getLast_Name__c();
                        edit_text_name.setText(Fname + " " + Lname);

                    }
                        }

                }catch (IOException e) {
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
            if (s.length()==10){
                GetUSerName();


            }

        }
    };
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.btn_allocate_asset:
                AllocateStatus();
        }
    }
}