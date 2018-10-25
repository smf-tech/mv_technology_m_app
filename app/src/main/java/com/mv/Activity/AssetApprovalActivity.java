package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetApprovalActivity extends AppCompatActivity implements View.OnClickListener {
    private Asset asset;
    private PreferenceHelper preferenceHelper;
    private AssetApprovalActivity _context;
    private EditText edit_text_name;
    private EditText edit_text_assetname;
    private EditText edit_text_modelno;
    private EditText edit_text_issue_date;
    private EditText edit_text_specification;
    private EditText edit_text_code;
    private EditText edit_asset_status;
    private EditText asset_reject_remark;
    private Button accept;
    private Button reject;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar,rel_Asset_Name;
    private TextInputLayout input_specification,asset_reject_ly;
    private Spinner spinner_assetstaus;
    private String asset_status;
    private List<String> asset_statuslist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_approval);
        preferenceHelper = new PreferenceHelper(this);
        InitViews();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void InitViews() {
        setActionbar(getString(R.string.asset_approval));
        if(getIntent().getExtras().getSerializable("Assets")!=null){
            asset = (Asset) getIntent().getExtras().getSerializable("Assets");
        }

        asset_statuslist = Arrays.asList(getResources().getStringArray(R.array.array_of_asset_status));

        edit_text_name = (EditText) findViewById(R.id.edit_text_name);
        edit_text_assetname = (EditText) findViewById(R.id.edit_text_assetname);
        edit_text_modelno = (EditText) findViewById(R.id.edit_text_modelno);
        edit_text_issue_date = (EditText) findViewById(R.id.edit_text_issue_date);
        input_specification = (TextInputLayout) findViewById(R.id.input_specification);
        edit_text_specification = (EditText) findViewById(R.id.edit_text_specification);
        edit_text_code = (EditText) findViewById(R.id.edit_text_code);
        edit_asset_status = (EditText) findViewById(R.id.edit_asset_status);
        rel_Asset_Name = (RelativeLayout) findViewById(R.id.rel_AssetName);
        spinner_assetstaus = (Spinner) findViewById(R.id.spinner_assetstaus);
        reject = (Button) findViewById(R.id.reject);
        accept = (Button) findViewById(R.id.accept);
        accept.setOnClickListener(this);
        reject.setOnClickListener(this);
        edit_asset_status.setText(asset.getAllocationStatus());
        edit_text_name.setText(asset.getUsername());
        edit_text_assetname.setText(asset.getAssetName());
        edit_text_modelno.setText(asset.getAssetModel());
        // edit_text_issue_date.setText(asset.getExpectedIssueDate());
        edit_text_specification.setText(asset.getSpecification());
        edit_text_code.setText(asset.getCode());

        if(asset.getAllocationStatus().equalsIgnoreCase("Rejected")) {
            asset_reject_ly = (TextInputLayout) findViewById(R.id.asset_reject_ly);
            asset_reject_remark = (EditText) findViewById(R.id.asset_reject_remark);
            asset_reject_ly.setVisibility(View.VISIBLE);
            asset_reject_remark.setText(asset.getRemark());
        }

        if(User.getCurrentUser(_context).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")||asset.getAllocationStatus().equalsIgnoreCase("Rejected")||
                asset.getAllocationStatus().equalsIgnoreCase("Released")){
            reject.setVisibility(View.GONE);
            accept.setVisibility(View.GONE);
            rel_Asset_Name.setVisibility(View.GONE);
        }

        spinner_assetstaus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                asset_status = asset_statuslist.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void AcceptAsset(String status) {
        Utills.showProgressDialog(this, "Sending", getString(R.string.progress_please_wait));
        JSONObject jsonObject1 = new JSONObject();

        try {
            jsonObject1.put("Allocation_Quantity__c", "1");
            jsonObject1.put("Allocation_Status__c", status);
            jsonObject1.put("Id", asset.getAssetAllocationId());
            jsonObject1.put("Asset_Condition__c", asset_status);
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
                        Utills.showToast(getString(R.string.submitted_successfully), AssetApprovalActivity.this);
                        Intent intent = new Intent(getApplicationContext(), AssetAllocatedListActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), AssetApprovalActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), AssetApprovalActivity.this);

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
            case R.id.accept:
                if (asset_status.equalsIgnoreCase("select"))
                    Utills.showToast("Please select asset status", AssetApprovalActivity.this);
                else
                    AcceptAsset("Accepted");
                break;
            case R.id.reject:
                if (asset_status.equalsIgnoreCase("select"))
                    Utills.showToast("Please select asset status", AssetApprovalActivity.this);
                else
                    AcceptAsset("Rejected");
        }

    }
}
