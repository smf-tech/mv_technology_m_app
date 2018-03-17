package com.mv.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.AssetAdapter;
import com.mv.Adapter.CommunityMemberAdapter;
import com.mv.Adapter.TeamManagementAdapter;
import com.mv.Model.Asset;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetAllocatedListActivity extends AppCompatActivity implements View.OnClickListener{
   FloatingActionButton fab_send_asset;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private RecyclerView recycler_view;
    PreferenceHelper preferenceHelper;
    EditText editTextEmail;
    List<Asset> assetList = new ArrayList<>();
    AssetAdapter adapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Asset> repplicaCahart = new ArrayList<>();
    TextView textNoData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocated_list);
        initViews();
    }

    private void initViews(){
        preferenceHelper = new PreferenceHelper(this);
        editTextEmail = (EditText) findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(watch);
        textNoData = (TextView) findViewById(R.id.textNoData);
        fab_send_asset = (FloatingActionButton) findViewById(R.id.fab_send_asset);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        fab_send_asset.setOnClickListener(this);
        setActionbar(getString(R.string.asset_request));
        adapter = new AssetAdapter(assetList, AssetAllocatedListActivity.this);

        mLayoutManager = new LinearLayoutManager(AssetAllocatedListActivity.this);
       recycler_view.setLayoutManager(mLayoutManager);
       recycler_view.setItemAnimator(new DefaultItemAnimator());
       recycler_view.setAdapter(adapter);

        if (Utills.isConnected(AssetAllocatedListActivity.this)){
            GetAssetTransactionList();
        }else {
            Utills.showInternetPopUp(AssetAllocatedListActivity.this);
        }


    }


    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
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
            case R.id.fab_send_asset:
                Intent intent = new Intent(getApplicationContext(),SendAssetRequestActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    private void GetAssetTransactionList(){
        Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";


            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAllTransactionManagement?userId=" +  User.getCurrentUser(this).getMvUser().getId();
           apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
               @Override
               public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                   Utills.hideProgressDialog();
                   String data = null;
                   try {
                       data = response.body().string();
                       if (data != null && data.length() > 0) {
                           JSONArray jsonArray = new JSONArray(data);
                           if(jsonArray.length()!=0){
                               assetList.clear();
                               for (int i = 0; i < jsonArray.length(); i++){
                                   Asset asset = new Asset();
                                   if (jsonArray.getJSONObject(i).has("assetId")) {
                                       asset.setAsset_id(jsonArray.getJSONObject(i).getString("assetId"));
                                   }

                                   if (jsonArray.getJSONObject(i).has("assetName")){
                                       asset.setAssetName(jsonArray.getJSONObject(i).getString("assetName"));
                                   }

                                   if (jsonArray.getJSONObject(i).has("assetModel")){
                                       asset.setAssetModel(jsonArray.getJSONObject(i).getString("assetModel"));
                                   }

                                   if (jsonArray.getJSONObject(i).has("assetCount")) {
                                       asset.setAssetCount(jsonArray.getJSONObject(i).getString("assetCount"));
                                   }
                                   if (jsonArray.getJSONObject(i).has("expectedIssueDate")) {
                                       asset.setExpectedIssueDate(jsonArray.getJSONObject(i).getString("expectedIssueDate"));
                                   }
                                   if (jsonArray.getJSONObject(i).has("assetAllocationId")) {
                                       asset.setAssetAllocationId(jsonArray.getJSONObject(i).getString("assetAllocationId"));
                                   }

                                   if (jsonArray.getJSONObject(i).has("allocationStatus")) {
                                       asset.setAllocationStatus(jsonArray.getJSONObject(i).getString("allocationStatus"));
                                   }

                                   if (jsonArray.getJSONObject(i).has("specification")){
                                       asset.setSpecification(jsonArray.getJSONObject(i).getString("specification"));
                                   }


                                     assetList.add(asset);
                                   repplicaCahart.add(asset);



                               }
                               adapter.notifyDataSetChanged();



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
                   Utills.showToast(getString(R.string.error_something_went_wrong),AssetAllocatedListActivity.this);

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
            setFilter(s.toString());

        }
    };
    private void setFilter(String s) {
        List<Asset> list = new ArrayList<>();
        assetList.clear();
        for (int i = 0; i < repplicaCahart.size(); i++) {
            assetList.add(repplicaCahart.get(i));
        }
        for (int i = 0; i < assetList.size(); i++) {
            if (assetList.get(i).getAssetModel().toLowerCase().contains(s.toLowerCase())) {
                list.add(assetList.get(i));
            }
        }
        assetList.clear();
        for (int i = 0; i < list.size(); i++) {
            assetList.add(list.get(i));
        }
        adapter.notifyDataSetChanged();
        if(assetList.size()==0)
        {
            textNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            textNoData.setVisibility(View.GONE);

        }

    }

}
