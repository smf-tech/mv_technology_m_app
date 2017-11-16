package com.mv.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import com.mv.databinding.ActivityRegistrationBinding;
import com.mv.databinding.ActivityUserApproveDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserApproveDetail extends AppCompatActivity {
    private ActivityUserApproveDetailBinding binding;
    private PreferenceHelper preferenceHelper;
    String userId,comment;
    String isSave;

    User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_approve_detail);
        binding.setActivity(this);
        userId=getIntent().getExtras().getString(Constants.ID);
        preferenceHelper = new PreferenceHelper(this);
        if (Utills.isConnected(this)) {
            getState();
        }

    }

    private void getState() {

        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getUserData?userId="+userId;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    String data=response.body().string();
                    mUser = gson.fromJson(data, User.class);

                    binding.editTextName.setText(mUser.getName());
                    binding.editTextMidleName.setText(mUser.getMiddleName());
                    binding.editTextLastName.setText(mUser.getLastName());
                    binding.editTextMobileNumber.setText(mUser.getPhone());
                    binding.editTextEmail.setText(mUser.getEmail());
                    binding.editOrganization.setText(mUser.getOrganisation());
                    binding.editRole.setText(mUser.getRoll());
                    binding.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            comment="";
                            isSave="true";
                            sendApprovedData();
                        }
                    });
                    binding.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialog();
                        }
                    });

                    if(mUser.getState()!=null&&(! mUser.getState().isEmpty()||!mUser.getState().equalsIgnoreCase("Select")))
                    {
                        binding.editState.setText(mUser.getState());
                        binding.layState.setVisibility(View.VISIBLE);
                        binding.txtState.setVisibility(View.VISIBLE);
                    }
                    if(mUser.getDistrict()!=null&&(! mUser.getDistrict().isEmpty()||!mUser.getDistrict().equalsIgnoreCase("Select")))
                    {
                        binding.editDistrict.setText(mUser.getDistrict());
                        binding.layDistrict.setVisibility(View.VISIBLE);
                        binding.txtDistrict.setVisibility(View.VISIBLE);
                    }
                    if(mUser.getTaluka()!=null&&(! mUser.getTaluka().isEmpty()||!mUser.getTaluka().equalsIgnoreCase("Select")))
                    {
                        binding.editTaluka.setText(mUser.getTaluka());
                        binding.layTaluka.setVisibility(View.VISIBLE);
                        binding.txtTaluka.setVisibility(View.VISIBLE);
                    }

                    if(mUser.getCluster()!=null&&(! mUser.getCluster().isEmpty()||!mUser.getCluster().equalsIgnoreCase("Select")))
                    {
                        binding.editCluster.setText(mUser.getCluster());
                        binding.layCluster.setVisibility(View.VISIBLE);
                        binding.txtCluster.setVisibility(View.VISIBLE);
                    }
                    if(mUser.getVillage()!=null&&(! mUser.getVillage().isEmpty()||!mUser.getVillage().equalsIgnoreCase("Select")))
                    {
                        binding.editVillage.setText(mUser.getVillage());
                        binding.layVillage.setVisibility(View.VISIBLE);
                        binding.txtVillage.setVisibility(View.VISIBLE);
                    }
                    if(mUser.getSchool_Name()!=null&&(! mUser.getSchool_Name().isEmpty()||!mUser.getSchool_Name().equalsIgnoreCase("Select")))
                    {
                        binding.editSchool.setText(mUser.getSchool_Name());
                        binding.laySchool.setVisibility(View.VISIBLE);
                        binding.txtDistrict.setVisibility(View.VISIBLE);
                    }

                    if (mUser.getImageId() != null && !(mUser.getImageId().equalsIgnoreCase("null"))) {
                        Glide.with(getApplicationContext())
                                .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mUser.getImageId() + "/Body"))
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
                            if (mListState.get(i).equalsIgnoreCase(User.getCurrentUser(RegistrationActivity.this).getState())) {
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


    public void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserApproveDetail.this);
        alertDialog.setTitle("Comment");
        alertDialog.setMessage("Please Enter Comment");

        final EditText input = new EditText(UserApproveDetail.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isSave="false";
                        comment = input.getText().toString();
                        if(!comment.isEmpty())
                        {
                            sendApprovedData();
                        }
                        else
                        {
                            Utills.showToast("Please Enter Comment",UserApproveDetail.this);
                        }

                    }

                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


    private void sendApprovedData() {
        if (Utills.isConnected(this)) {
            try {


                Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", mUser.getId());
                jsonObject1.put("ApprovedBy", User.getCurrentUser(getApplicationContext()).getId());

                JSONArray jsonArrayAttchment = new JSONArray();

                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("isApproved", isSave);
                jsonObject1.put("comment", comment);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/ApproveComment", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast(getString(R.string.post_share_successfully), UserApproveDetail.this);
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
