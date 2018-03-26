package com.mv.Activity;


import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Asset;
import com.mv.Model.Content;
import com.mv.Model.Template;
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
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendAssetRequestActivity extends AppCompatActivity implements View.OnClickListener {
    private  Spinner spinner_Assetname;
    private TextInputLayout input_quantity,input_tentative_return_date;
    private EditText edit_text_quantity,edit_text_remarks;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title,edit_text_tentative_return_date,edit_text_issue_date;
    private RelativeLayout mToolBar;
    Button btn_send_request;
    private PreferenceHelper preferenceHelper;
    List<Asset> assetList = new ArrayList<>();
    ArrayList<String> assetnameList= new ArrayList<>();
    private ArrayAdapter<String> asset_name_adapter;
    private int selectAssetName =0;
    Asset asset;
   private String id;
   private String  AssetQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_asset_request);
        initviews();
        if (Utills.isConnected(getApplicationContext())){
            getAssetName();
        }else {
            Utills.showInternetPopUp(getApplicationContext());
        }


    }


    private  void  initviews(){
        preferenceHelper = new PreferenceHelper(this);

        edit_text_quantity = (EditText) findViewById(R.id.edit_text_quantity);
        edit_text_issue_date = (TextView )findViewById(R.id.edit_text_issue_date);
        edit_text_tentative_return_date = (TextView) findViewById(R.id.edit_text_tentative_return_date);
        edit_text_remarks = (EditText) findViewById(R.id.edit_text_remarks);
        input_quantity = (TextInputLayout)findViewById(R.id.input_quantity);
        input_tentative_return_date = (TextInputLayout) findViewById(R.id.input_tentative_return_date);
        spinner_Assetname = (Spinner) findViewById(R.id.spinner_Assetname);
        btn_send_request = (Button) findViewById(R.id.btn_send_request);
        edit_text_issue_date.setOnClickListener(this);
        edit_text_tentative_return_date.setOnClickListener(this);
        btn_send_request.setOnClickListener(this);
        setActionbar("Asset Request");

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

    private void getAssetName(){

        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAsset?userId=" + User.getCurrentUser(this).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

                            final List<Asset> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Asset[].class));

                            assetnameList.add("select");

                            for (int i =0;i<temp.size();i++){
                                assetnameList.add(temp.get(i).getName());

                            }


                       asset_name_adapter = new ArrayAdapter<String>(SendAssetRequestActivity.this,
                        android.R.layout.simple_spinner_item, assetnameList);
                            asset_name_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_Assetname.setAdapter(asset_name_adapter);
                            spinner_Assetname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                    selectAssetName =i ;

                                   //String selected = spinner_Assetname.getSelectedItem().toString();
                                    if (i!=0) {


                                        id = temp.get(selectAssetName-1).getAsset_id();
                                        String type = temp.get(selectAssetName-1).getType();
                                        if (type.equalsIgnoreCase("MultiEntry")) {
                                            input_quantity.setVisibility(View.VISIBLE);
                                            input_tentative_return_date.setVisibility(View.GONE);
                                            AssetQuantity = edit_text_quantity.getText().toString().trim();


                                        } else {
                                            AssetQuantity ="1";
                                            input_quantity.setVisibility(View.GONE);
                                            input_tentative_return_date.setVisibility(View.VISIBLE);

                                        }

                                    }

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                        }
                    }
                }catch (JSONException e) {
                    Utills.hideProgressDialog();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.showToast(getString(R.string.error_something_went_wrong),SendAssetRequestActivity.this);

            }
        });

    }


   // public void SendAssetRequest(String AssetID,String Allocation_quantity, String Expected_Issue_Date, String  tentativeReturnDate, String Remark,String Allocation_Status){
        public void SendAssetRequest(){

        try {
            Utills.showProgressDialog(SendAssetRequestActivity.this, "Sending Request", "Please wait");
            JSONObject jsonObject1 = new JSONObject();

            jsonObject1.put("Requested_User__c", User.getCurrentUser(SendAssetRequestActivity.this).getMvUser().getId());
            jsonObject1.put("ASSET__c", id);
            jsonObject1.put("Allocation_Quantity__c", "1");
            jsonObject1.put("Expected_Issue_Date__c", edit_text_issue_date.getText().toString().trim());
            jsonObject1.put("tentativeReturnDate", edit_text_tentative_return_date.getText().toString().trim());
            jsonObject1.put("Remark", edit_text_remarks.getText().toString().trim());
            jsonObject1.put("Allocation_Status__c", "Requested");

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
                        Utills.showToast(getString(R.string.submitted_successfully), SendAssetRequestActivity.this);
                        Intent intent = new Intent(getApplicationContext(),AssetAllocatedListActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong),SendAssetRequestActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.showToast(getString(R.string.error_something_went_wrong),SendAssetRequestActivity.this);

                }
            });
        } catch (JSONException e) {
        e.printStackTrace();
    }
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {
         case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.edit_text_issue_date:
              Utills.showDateDialog(edit_text_issue_date,SendAssetRequestActivity.this);
                break;
            case  R.id.edit_text_tentative_return_date:
                Utills.showDateDialog(edit_text_tentative_return_date,SendAssetRequestActivity.this);

                 break;

            case  R.id.btn_send_request:

                SendAssetRequest();

        }

    }



}
