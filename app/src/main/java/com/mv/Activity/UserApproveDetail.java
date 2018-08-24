package com.mv.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityUserApproveDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserApproveDetail extends AppCompatActivity implements View.OnClickListener {
    private ActivityUserApproveDetailBinding binding;
    private PreferenceHelper preferenceHelper;
    String userId, comment;
    String isSave;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_approve_detail);
        binding.setActivity(this);
        userId = getIntent().getExtras().getString(Constants.ID);
        preferenceHelper = new PreferenceHelper(this);
        setActionbar(getString(R.string.team_user_approval));
        if (Utills.isConnected(this)) {
            getApprovedUserData();
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

    private void getApprovedUserData() {

        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserData_url + "?userId=" + userId;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    String data = response.body().string();
                    mUser = gson.fromJson(data, User.class);

                    binding.editTextName.setText(mUser.getMvUser().getName());
                    binding.editTextMidleName.setText(mUser.getMvUser().getMiddleName());
                    binding.editTextLastName.setText(mUser.getMvUser().getLastName());
                    binding.editTextMobileNumber.setText(mUser.getMvUser().getPhone());
                    binding.editTextEmail.setText(mUser.getMvUser().getEmail());
                    binding.editOrganization.setText(mUser.getMvUser().getOrganisation());
                    binding.editRole.setText(mUser.getMvUser().getRoll());
                    binding.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mUser.getMvUser().getIsApproved() != null && mUser.getMvUser().getIsApproved().equalsIgnoreCase("true")) {
                                Utills.showToast("User Already Approved", UserApproveDetail.this);
                            } else {
                                comment = "";
                                isSave = "true";
                                sendApprovedData();
                            }
                        }
                    });
                    binding.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mUser.getMvUser().getIsApproved() != null
                                    && mUser.getMvUser().getIsApproved().equalsIgnoreCase("false")
                                    && mUser.getMvUser().getApprover_Comment__c() != null
                                    && mUser.getMvUser().getApprover_Comment__c().length() > 0) {
                                Utills.showToast("User Already Rejected", UserApproveDetail.this);
                            } else {
                                showDialog();
                            }

                        }
                    });

                    if (mUser.getMvUser().getState() != null && !(!mUser.getMvUser().getState().isEmpty() || !mUser.getMvUser().getState().equalsIgnoreCase("Select"))) {
                        binding.editState.setText(mUser.getMvUser().getState());
                        binding.layState.setVisibility(View.VISIBLE);
                        binding.txtState.setVisibility(View.VISIBLE);
                    }
                    if (mUser.getMvUser().getDistrict() != null && !(!mUser.getMvUser().getDistrict().isEmpty() || !mUser.getMvUser().getDistrict().equalsIgnoreCase("Select"))) {
                        binding.editDistrict.setText(mUser.getMvUser().getDistrict());
                        binding.layDistrict.setVisibility(View.VISIBLE);
                        binding.txtDistrict.setVisibility(View.VISIBLE);
                    }
                    if (mUser.getMvUser().getTaluka() != null && !(!mUser.getMvUser().getTaluka().isEmpty() || !mUser.getMvUser().getTaluka().equalsIgnoreCase("Select"))) {
                        binding.editTaluka.setText(mUser.getMvUser().getTaluka());
                        binding.layTaluka.setVisibility(View.VISIBLE);
                        binding.txtTaluka.setVisibility(View.VISIBLE);
                    }

                    if (mUser.getMvUser().getCluster() != null && !(!mUser.getMvUser().getCluster().isEmpty() || !mUser.getMvUser().getCluster().equalsIgnoreCase("Select"))) {
                        binding.editCluster.setText(mUser.getMvUser().getCluster());
                        binding.layCluster.setVisibility(View.VISIBLE);
                        binding.txtCluster.setVisibility(View.VISIBLE);
                    }
                    if (mUser.getMvUser().getVillage() != null && !(!mUser.getMvUser().getVillage().isEmpty() || !mUser.getMvUser().getVillage().equalsIgnoreCase("Select"))) {
                        binding.editVillage.setText(mUser.getMvUser().getVillage());
                        binding.layVillage.setVisibility(View.VISIBLE);
                        binding.txtVillage.setVisibility(View.VISIBLE);
                    }
                    if (mUser.getMvUser().getSchool_Name() != null && !(!mUser.getMvUser().getSchool_Name().isEmpty() || !mUser.getMvUser().getSchool_Name().equalsIgnoreCase("Select"))) {
                        binding.editSchool.setText(mUser.getMvUser().getSchool_Name());
                        binding.laySchool.setVisibility(View.VISIBLE);
                        binding.txtDistrict.setVisibility(View.VISIBLE);
                    }

                    if (mUser.getMvUser().getImageId() != null && !(mUser.getMvUser().getImageId().equalsIgnoreCase("null"))) {
                        Glide.with(getApplicationContext())
                                .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mUser.getMvUser().getImageId() + "/Body"))
                                .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                                .into(binding.addImage);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                 /*   try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        mListState.clear();
                        mListState.add("Select");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            mListState.add(jsonArray.getString(i));
                        }
                        state_adapter.notifyDataSetChanged();
                        if (!isAdd && !isStateSet) {
                            isStateSet = true;
                            for (int i = 0; i < mListState.size(); i++) {
                                if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getApprovedUserData())) {
                                    binding.spinnerState.setSelection(i);
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

            /*binding.editTextMidleName.setText(User.getCurrentUser(this).getMiddleName());
            binding.editTextLastName.setText(User.getCurrentUser(this).getLastName());
            binding.editTextMobileNumber.setText(User.getCurrentUser(this).getPhone());*/

    GlideUrl getUrlWithHeaders(String url) {
        //
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
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

    public void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserApproveDetail.this);
        alertDialog.setTitle(getString(R.string.comments));
        alertDialog.setMessage(getString(R.string.enter_comment));

        final EditText input = new EditText(UserApproveDetail.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

//        alertDialog.show();
        AlertDialog dialog = alertDialog.create();
        dialog.show();

        // comment validations added
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isSave = "false";
                comment = input.getText().toString();
                if (!comment.isEmpty()) {
                    sendApprovedData();
                    dialog.dismiss();
                } else {
                    Utills.showToast("Please Enter Comment", UserApproveDetail.this);
                }
            }
        });

    }


    private void sendApprovedData() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", mUser.getMvUser().getId());
                jsonObject1.put("ApprovedBy", User.getCurrentUser(getApplicationContext()).getMvUser().getId());

                JSONArray jsonArrayAttchment = new JSONArray();

                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("isApproved", isSave);
                jsonObject1.put("comment", comment);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.ApproveCommentUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast(getString(R.string.submitted_successfully), UserApproveDetail.this);
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), UserApproveDetail.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), UserApproveDetail.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), UserApproveDetail.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), UserApproveDetail.this);
        }
    }

}
