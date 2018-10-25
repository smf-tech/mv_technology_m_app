package com.mv.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.GroupAdapter;
import com.mv.Model.Community;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityGroupBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private GroupAdapter mAdapter;
    private ActivityGroupBinding binding;
    private List<Community> communityList = new ArrayList<>();
    private List<Community> replicaCommunityList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private TextView textNoData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group);
       // binding.setActivity(this);
        initViews();
        User user = User.getCurrentUser(this);
        if (Utills.isConnected(this))
            getAllCommunities();
        else
            showPopUp();
    }

    private void getAllCommunities() {
        Utills.showProgressDialog(this, "Loading Communities", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.MV_GetCommunities_c_Url;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    communityList.clear();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Community> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Community[].class));
                    if (temp.size()!=0) {
                        for (int i = 0; i < temp.size(); i++) {
                            communityList.add(temp.get(i));
                            replicaCommunityList.add(temp.get(i));
                        }
                        mAdapter.notifyDataSetChanged();
                        textNoData.setVisibility(View.GONE);
                    }else {
                        textNoData.setVisibility(View.VISIBLE);
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

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Showing Alert Message
        alertDialog.show();
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

    private void initViews() {
        setActionbar("My Communities");

        binding.editTextEmail.addTextChangedListener(watch);
        textNoData = (TextView) findViewById(R.id.textNoData);

        preferenceHelper = new PreferenceHelper(this);
      /*  mAdapter = new GroupAdapter(communityList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);*/

    }


    private TextWatcher watch = new TextWatcher() {

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
        List<Community> list = new ArrayList<>();
        communityList.clear();
        communityList.addAll(replicaCommunityList);
        for (int i = 0; i < communityList.size(); i++) {
            if (communityList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                list.add(communityList.get(i));
            }
        }
        communityList.clear();
        communityList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
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

    public void onLayoutGroupClick(int position) {
        preferenceHelper.insertString(PreferenceHelper.COMMUNITYID, communityList.get(position).getId());
        List<Community> list = new ArrayList<>();
        list.addAll(communityList);
        list.remove(position);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(list);
        Intent intent;
        intent = new Intent(this, CommunityHomeActivity.class);
        intent.putExtra(Constants.TITLE, communityList.get(position).getName());
        intent.putExtra(Constants.LIST, json);
        startActivity(intent);
    }
    /*public void onLayoutGroup1Click() {
        Intent intent;
        intent = new Intent(this, CommunityHomeActivity.class);
        intent.putExtra(Constants.TITLE, "Group 1");
        startActivity(intent);
    }



    public void onLayoutGroup3Click() {
        Intent intent;
        intent = new Intent(this, CommunityHomeActivity.class);
        intent.putExtra(Constants.TITLE, "Group 3");
        startActivity(intent);
    }

    public void onLayoutGroup4Click() {
        Intent intent;
        intent = new Intent(this, CommunityHomeActivity.class);
        intent.putExtra(Constants.TITLE, "Group 4");
        startActivity(intent);
    }

    public void onLayoutGroup5Click() {
        Intent intent;
        intent = new Intent(this, CommunityHomeActivity.class);
        intent.putExtra(Constants.TITLE, "Group 5");
        startActivity(intent);
    }*/
}
