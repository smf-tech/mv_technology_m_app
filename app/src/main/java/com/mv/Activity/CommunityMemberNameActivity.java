package com.mv.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mv.Adapter.CommentAdapter;
import com.mv.Adapter.CommunityMemberAdapter;
import com.mv.Model.Community;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommentBinding;

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
    public ArrayList<String> CommunityMemberList;
    public ArrayList<String> replicaCommunityList;
    CommunityMemberAdapter adapter;
    private CommunityMemberNameActivity binding;
    RecyclerView recyclerView;
    private TextView toolbar_title,textNoData;
    private ImageView img_back, img_logout;
    EditText edit_text_email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_member_name);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews(getString(R.string.Community_member));
        preferenceHelper = new PreferenceHelper(this);
        if (Utills.isConnected(this))
            GetCommunityMember();
        else
            Utills.showInternetPopUp(CommunityMemberNameActivity.this);
    }




    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

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

    }

    public void GetCommunityMember(){
        Utills.showProgressDialog(this, getString(R.string.loading_members), getString(R.string.progress_please_wait));
        String url = "";
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/userdetails?communityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID);

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                     CommunityMemberList = new ArrayList<>();
                     replicaCommunityList =new ArrayList<>();
                     if(response.body()!=null) {
                         String strResponse = response.body().string();
                         JSONArray jsonArray = new JSONArray(strResponse);
                         if (jsonArray.length() != 0) {

                             for (int i = 0; i <= jsonArray.length(); i++) {
                                 CommunityMemberList.add(jsonArray.getString(i));
                                 adapter = new CommunityMemberAdapter(getApplicationContext(), CommunityMemberList);
                                 recyclerView.setAdapter(adapter);
                                 recyclerView.setHasFixedSize(true);
                                 recyclerView.setLayoutManager(new LinearLayoutManager(CommunityMemberNameActivity.this));
                                 replicaCommunityList.add(CommunityMemberList.get(i));
                             }
                             textNoData.setVisibility(View.GONE);
                             adapter.notifyDataSetChanged();
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
        List<String> list = new ArrayList<>();
        CommunityMemberList.clear();
        replicaCommunityList.clear();
        for (int i = 0; i < replicaCommunityList.size(); i++) {
            CommunityMemberList.add(replicaCommunityList.get(i));
        }
        for (int i = 0; i < CommunityMemberList.size(); i++) {
            if (CommunityMemberList.get(i).toLowerCase().contains(s.toLowerCase())) {
                list.add(CommunityMemberList.get(i));
            }
        }
        CommunityMemberList.clear();
        for (int i = 0; i < list.size(); i++) {
            CommunityMemberList.add(list.get(i));
        }
        if(CommunityMemberList.size()!=0) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
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
