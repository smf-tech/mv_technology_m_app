package com.mv.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.CommunityMemberAdapter;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
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

public class CommunityMemberNameActivity extends AppCompatActivity implements View.OnClickListener{
    private PreferenceHelper preferenceHelper;
    public ArrayList<String> CommunityMemberList=new ArrayList<>();
    ArrayList<String> repplicaCahart = new ArrayList<>();
    CommunityMemberAdapter adapter;
    private CommunityMemberNameActivity binding;
    RecyclerView recyclerView;
    private TextView toolbar_title,textNoData;
    private ImageView img_back, img_logout;
    EditText edit_text_email;
    String Member_count="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_member_name);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews(getString(R.string.Community_member));
        preferenceHelper = new PreferenceHelper(this);
        /*Check Network Connectivity and Call GetCommunityMember()*/
        if (Utills.isConnected(this)) {
            GetCommunityMember();

        }else {
            /*No Internet Connection Popup*/
            Utills.showInternetPopUp(CommunityMemberNameActivity.this);
        }

    }




    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    /*Initialize all  views */
    private void initViews(String title){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
       toolbar_title.setText(title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.INVISIBLE);
        textNoData = (TextView) findViewById(R.id.textNoData);
        edit_text_email = (EditText) findViewById(R.id.edit_text_email);
        edit_text_email.addTextChangedListener(watch);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CommunityMemberNameActivity.this));

    }


    /*Get Community members by calling userdetails api*/
    public void GetCommunityMember(){
        Utills.showProgressDialog(this, getString(R.string.loading_members), getString(R.string.progress_please_wait));
        String url = "";
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        /*UserDetails Url for getting community members*/

        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.Userdetails_Url+"?communityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                     CommunityMemberList = new ArrayList<>();

                     if(response.body()!=null) {
                         String strResponse = response.body().string();
                         JSONArray jsonArray = new JSONArray(strResponse);
                         if (jsonArray.length() != 0) {

                             final int numberOfItemsInResp = jsonArray.length();
                             for (int i = 0; i < numberOfItemsInResp; i++) {
                                 CommunityMemberList.add(jsonArray.getString(i));   // add response in CommunityMemberList
                             }

                             // set List To Adapter
                             adapter = new CommunityMemberAdapter(getApplicationContext(), CommunityMemberList);
                             recyclerView.setAdapter(adapter);

                             Member_count =String.valueOf(CommunityMemberList.size());
                             toolbar_title.setText(getString(R.string.Community_member)+" " + Member_count);

                             textNoData.setVisibility(View.GONE);
                         } else {
                             textNoData.setVisibility(View.VISIBLE);
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
            }
        });
    }

    // To search community members in searchbox
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
            setFilter(s.toString()); // call to setFilter

        }
    };

/*  This function is used for searching community members by passing string of particular name as input */

    private void setFilter(String s) {
        List<String> list = new ArrayList<>();
        repplicaCahart.clear();
        for (int i = 0; i < CommunityMemberList.size(); i++) {
            repplicaCahart.add(CommunityMemberList.get(i));
        }
        for (int i = 0; i < repplicaCahart.size(); i++) {
            if (repplicaCahart.get(i).toLowerCase().contains(s.toLowerCase())) {
                list.add(repplicaCahart.get(i));
            }
        }
        repplicaCahart.clear();
        for (int i = 0; i < list.size(); i++) {
            repplicaCahart.add(list.get(i));
        }
        adapter = new CommunityMemberAdapter(getApplicationContext(), repplicaCahart);
        recyclerView.setAdapter(adapter);
        if(repplicaCahart.size()==0)
        {
            textNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            textNoData.setVisibility(View.GONE);

        }


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }


}
