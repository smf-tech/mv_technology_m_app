package com.mv.Activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.CommentAdapter;
import com.mv.Adapter.CommunityMemberAdapter;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommentBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityMemberNameActivity extends AppCompatActivity implements View.OnClickListener{
    private PreferenceHelper preferenceHelper;
    public ArrayList<String> CommunityMemberList;
    CommunityMemberAdapter adapter;
    private CommunityMemberNameActivity binding;
    RecyclerView recyclerView;
    private TextView toolbar_title;
    private ImageView img_back, img_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_member_name);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
        preferenceHelper = new PreferenceHelper(this);

        GetCommunityMember();


    }
    private void initViews(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Members");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.INVISIBLE);

    }

    public void GetCommunityMember(){
        Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        String url = "";
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/userdetails?communityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID);
        Log.e("url",url);
        Log.e("community id",preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                     CommunityMemberList = new ArrayList<>();
                    String strResponse = response.body().string();
                    JSONArray jsonArray = new JSONArray(strResponse);
                    for (int i=0;i<=jsonArray.length();i++){
                        CommunityMemberList.add(jsonArray.getString(i));

                        Log.e("member",CommunityMemberList.get(i));
                        adapter = new CommunityMemberAdapter(getApplicationContext(),CommunityMemberList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(CommunityMemberNameActivity.this));
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
