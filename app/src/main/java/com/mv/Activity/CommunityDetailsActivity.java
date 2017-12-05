package com.mv.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommunityDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCommunityDetailsBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private Content mContent;
    private PreferenceHelper preferenceHelper;
    private List<Community> communityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_details);
        binding.setActivity(this);
        initViews();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.layout_comment:
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(Constants.ID, mContent.getId());
                startActivity(intent);
                break;
            case R.id.layout_share:
                showGroupDialog();
                break;
            case R.id.layout_like:
                if (!mContent.getIsLike()) {
                    sendLikeAPI(mContent.getId(), !(mContent.getIsLike()));
                    mContent.setIsLike(!mContent.getIsLike());
                    mContent.setLikeCount((mContent.getLikeCount() + 1));
                    binding.likeCount.setText(mContent.getLikeCount() + " "+getString(R.string.likes));
                    binding.heart.setImageResource(R.drawable.like);
                } else {
                    sendDisLikeAPI(mContent.getId(), !mContent.getIsLike());
                    mContent.setIsLike(!mContent.getIsLike());
                    mContent.setLikeCount((mContent.getLikeCount() - 1));
                    binding.likeCount.setText(mContent.getLikeCount() + " "+getString(R.string.likes));
                    binding.heart.setImageResource(R.drawable.dislike);
                }
                break;
        }
    }

    private void sendShareRecord(String contentId, String communityId) {
        if (Utills.isConnected(this)) {
            try {


                Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", User.getCurrentUser(this).getId());
                jsonObject1.put("contentId", contentId);

                JSONArray jsonArrayAttchment = new JSONArray();

                jsonArrayAttchment.put(communityId);
                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("grId", jsonArrayAttchment);


                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/sharedRecords", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast(getString(R.string.post_share_successfully), CommunityDetailsActivity.this);
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    public void showGroupDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.logomulya);
        builderSingle.setTitle(getString(R.string.select_one));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < this.communityList.size(); i++) {
            arrayAdapter.add(this.communityList.get(i).getName());
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendShareRecord(mContent.getId(), communityList.get(which).getId());
            }
        });
        builderSingle.show();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    public void onPostImageClick() {
       /* if (TextUtils.isEmpty(mContent.getAttachmentId())
                || mContent.getAttachmentId().equalsIgnoreCase("null")) {
        } else {
            Intent intent;
            intent = new Intent(this, ActivityImageView.class);
            intent.putExtra(Constants.ID, mContent.getAttachmentId());
            startActivity(intent);
        }*/

    }

    public void onProfileImageClick() {


    }

    private void sendDisLikeAPI(String cotentId, boolean isLike) {
        if (Utills.isConnected(this)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like", isLike);
                jsonObject1.put("MV_Content", cotentId);
                jsonObject1.put("MV_User", User.getCurrentUser(this).getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/removeLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    private void sendLikeAPI(String cotentId, Boolean isLike) {
        if (Utills.isConnected(this)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like__c", isLike);
                jsonObject1.put("MV_Content__c", cotentId);
                jsonObject1.put("MV_User__c", User.getCurrentUser(this).getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("contentlikeList", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    private void initViews() {


        setActionbar(getString(R.string.comunity_detail));
        String json;
        json = getIntent().getExtras().getString(Constants.LIST);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if (!TextUtils.isEmpty(json))
            communityList = Arrays.asList(gson.fromJson(json, Community[].class));
        preferenceHelper = new PreferenceHelper(this);
        mContent = (Content) getIntent().getExtras().getSerializable(Constants.CONTENT);
        binding.layoutComment.setOnClickListener(this);
        binding.layoutShare.setOnClickListener(this);
        binding.layoutLike.setOnClickListener(this);

        if (TextUtils.isEmpty(mContent.getUserAttachmentId())
                ) {
        } else if (mContent.getAttachmentId().equalsIgnoreCase("null")) {
        } else {
            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
            Glide.with(this)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mContent.getUserAttachmentId() + "/Body"))
                    .placeholder(getResources().getDrawable(R.drawable.logomulya))
                    .into(binding.userImage);
        }

        if (TextUtils.isEmpty(mContent.getAttachmentId())
                ) {
            binding.cardImagedetails.setVisibility(View.GONE);


        } else if (mContent.getAttachmentId().equalsIgnoreCase("null")) {
        } else {
            binding.cardImagedetails.setVisibility(View.VISIBLE);
            if (mContent.getSynchStatus() != null
                    && mContent.getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image" + "/" + mContent.getAttachmentId() + ".png");
                if (file.exists()) {
                    Glide.with(this)
                            .load(Uri.fromFile(file))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(binding.cardImagedetails);
                }

            } else {
                Glide.with(this)
                        .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mContent.getAttachmentId() + "/Body"))
                        .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                        .into(binding.cardImagedetails);
            }
            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);

        }
        if (mContent.getIsLike())
            binding.heart.setImageResource(R.drawable.like);
        else
            binding.heart.setImageResource(R.drawable.dislike);
        binding.likeCount.setText(mContent.getLikeCount()+ " "+ getString(R.string.likes));
        binding.txtCommentCnt.setText(mContent.getCommentCount() + " "+getString(R.string.comments));
        binding.Title.setText(getString(R.string.title)+" : " + mContent.getTitle());

       /* if (mContent.getSynchStatus() != null
                && mContent.getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL))
            binding.type.setText(getString(R.string.template_type)+" : " + mContent.getTemplateName());
        else
            binding.type.setText(getString(R.string.template_type)+" : " + mContent.getTemplate());*/

        binding.type.setText("" + mContent.getUserName());
        binding.Description.setText(getString(R.string.description)+" : " + mContent.getDescription());
        binding.postDate.setText(mContent.getTime());

        // binding.userName.setText(mContent.g);
        TypedArray a = getResources().obtainTypedArray(R.array.places_picture);
        Drawable[] mPlacePictures = new Drawable[a.length()];
        for (int i = 0; i < mPlacePictures.length; i++) {
            mPlacePictures[i] = a.getDrawable(i);
        }
        a.recycle();
        binding.cardImagedetails.setImageDrawable(mPlacePictures[getIntent().getExtras().getInt("Position") % mPlacePictures.length]);
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


}
